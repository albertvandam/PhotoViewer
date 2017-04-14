package io.vandam.albert.photoviewer;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import io.vandam.albert.photoviewer.domain.Venue;
import io.vandam.albert.photoviewer.domain.Venues;
import io.vandam.albert.photoviewer.rest.RestClient;

/**
 * Main activity
 */
public class MainActivity extends AppCompatActivity {
    /**
     * Current location
     */
    public static double currentLongitude, currentLatitude;

    /**
     * Location manager
     */
    LocationManager locationManager;

    /**
     * List of venues
     */
    Venues venueList;
    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            currentLongitude = location.getLongitude();
            currentLatitude = location.getLatitude();

            if (venueList.size() == 0) {
                getVenues();
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_map:
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        venueList = Venues.getInstance();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    }

    private boolean mayUseLocation() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    AppPermissions.LOCATION);

            return false;
        }

        if (!isLocationEnabled()) {
            showEnableLocationAlert();

            return false;
        }

        return true;
    }

    private void showEnableLocationAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\n" +
                        "Please Enable Location to use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        getLocation();
    }

    private void getLocation() {
        if (mayUseLocation()) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            String provider = locationManager.getBestProvider(criteria, true);

            if (provider != null) {
                try {
                    long updateInterval = 5 * 60 * 1000; // 5 minutes
                    float updateRadius = 100; // meters
                    locationManager.requestLocationUpdates(provider, updateInterval, updateRadius, locationListener);

                } catch (SecurityException e) {
                    Toast.makeText(this.getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(this.getApplicationContext(), "No location provider available", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length != 0) {
            switch (requestCode) {
                case AppPermissions.LOCATION:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        getLocation();

                    } else {
                        Toast.makeText(this.getApplicationContext(), "Location permission not allowed", Toast.LENGTH_LONG).show();
                    }
                    break;
            }

        } else {
            Toast.makeText(this.getApplicationContext(), "Location permission request cancelled", Toast.LENGTH_LONG).show();
        }
    }

    private void getVenues() {
        TextView status = (TextView) findViewById(R.id.status);
        status.setText(R.string.loadingVenues);

        List<String> categories = new ArrayList<>();
        categories.add("4d4b7104d754a06370d81259"); // entertainment
        categories.add("4d4b7105d754a06374d81259"); // food
        categories.add("4d4b7105d754a06376d81259"); // nightlife
        categories.add("4d4b7105d754a06377d81259"); // outdoors & recreation

        String url = "https://api.foursquare.com/v2/venues/search?" +
                "ll=" + currentLatitude + "," + currentLongitude + // location
                "&limit=100" + // limit
                "&intent=browse" + // intent
                "&categoryId=" + StringUtils.join(categories, ",") + // categories
                "&radius=5000" + // radius around location in meter
                "&client_id=" + getString(R.string.foursquare_client_id) + // client id
                "&client_secret=" + getString(R.string.foursquare_client_secret) + // client key
                "&v=20170412"; // api version

        RestClient restClient = RestClient.getInstance();
        restClient.get(url, new RequestParams(), new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);

                try {
                    JSONArray venues = response.getJSONObject("response").getJSONArray("venues");
                    if (venues.length() == 0) {
                        Toast.makeText(getApplicationContext(), "No venues found within 5km from you", Toast.LENGTH_LONG).show();

                    } else {
                        for (int i = 0; i < venues.length(); i++) {
                            JSONObject venueJSON = venues.getJSONObject(i);

                            String venueCategory = "Unknown";
                            String venueCategoryIcon = "";

                            JSONArray venueCategoryJSON = venueJSON.getJSONArray("categories");
                            if (venueCategoryJSON.length() != 0) {
                                venueCategory = venueCategoryJSON.getJSONObject(0).getString("name");

                                JSONObject venueCategoryIconJSON = venueCategoryJSON.getJSONObject(0).getJSONObject("icon");
                                venueCategoryIcon = venueCategoryIconJSON.getString("prefix") + "bg_88" + venueCategoryIconJSON.getString("suffix");
                            }

                            Venue venue = new Venue();
                            venue.setId(venueJSON.getString("id"));
                            venue.setName(venueJSON.getString("name"));
                            venue.setCategory(venueCategory);
                            venue.setCategoryIconUrl(venueCategoryIcon);

                            JSONObject location = venueJSON.getJSONObject("location");
                            venue.setLatitude(location.getDouble("lat"));
                            venue.setLongitude(location.getDouble("lng"));

                            int venueId = venueList.add(venue);

                            new ImageDownloader().execute(venueId);
                        }
                    }

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    String err = errorResponse.getJSONObject("meta").getString("errorDetail");

                    Toast.makeText(getApplicationContext(), err, Toast.LENGTH_LONG).show();

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private class ImageDownloader extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            int venueId = (int) params[0];

            Venue venue = venueList.get(venueId);

            if (!venue.hasIcon()) {
                try {
                    URL url = new URL(venue.getCategoryIconUrl());

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream is = connection.getInputStream();

                    venue.setCategoryIcon(BitmapFactory.decodeStream(is));

                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

                    venue.setCategoryIcon(null);
                }
            }

            return venueId;
        }

        @Override
        protected void onPostExecute(Object result) {
            int venueId = (int) result;

            Venue venue = venueList.get(venueId);

            LinearLayout venueButton = new LinearLayout(getApplicationContext());
            getLayoutInflater().inflate(R.layout.venue, venueButton);

            ImageView categoryIcon = (ImageView) venueButton.findViewById(R.id.categoryIcon);
            categoryIcon.setImageBitmap(venue.getCategoryIcon());

            TextView venueName = (TextView) venueButton.findViewById(R.id.venueName);
            venueName.setText(venue.getName());

            TextView venueCategoryView = (TextView) venueButton.findViewById(R.id.venueCategory);
            venueCategoryView.setText(venue.getCategory());

            venueButton.setId(venueId);
            venueButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), VenueActivity.class);
                    intent.putExtra(AppIntent.SHOW_VENUE, v.getId());
                    startActivity(intent);
                }
            });

            LinearLayout list = (LinearLayout) findViewById(R.id.thelist);
            list.addView(venueButton);
        }
    }
}
