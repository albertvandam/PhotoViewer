package io.vandam.albert.photoviewer;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cz.msebera.android.httpclient.Header;
import io.vandam.albert.photoviewer.domain.Photo;
import io.vandam.albert.photoviewer.domain.Venue;
import io.vandam.albert.photoviewer.domain.Venues;
import io.vandam.albert.photoviewer.rest.RestClient;

public class VenueActivity extends AppCompatActivity {
    Venue venue;
    int venueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue);

        Venues venueList = Venues.getInstance();

        // Get the AppIntent that started this activity and extract the string
        Intent intent = getIntent();
        venueId = intent.getIntExtra(AppIntent.SHOW_VENUE, -1);

        if (venueId == -1) {
            Toast.makeText(this.getApplicationContext(), "Unknown venue requested", Toast.LENGTH_LONG).show();

        } else {
            venue = venueList.get(venueId);

            LinearLayout venueButton = (LinearLayout) findViewById(R.id.venue);
            getLayoutInflater().inflate(R.layout.venue, venueButton);

            ImageView categoryIcon = (ImageView) venueButton.findViewById(R.id.categoryIcon);
            categoryIcon.setImageBitmap(venue.getCategoryIcon());

            TextView venueName = (TextView) venueButton.findViewById(R.id.venueName);
            venueName.setText(venue.getName());

            TextView venueCategoryView = (TextView) venueButton.findViewById(R.id.venueCategory);
            venueCategoryView.setText(venue.getCategory());

            getVenue();
        }
    }

    private void getVenue() {
        if (venue.getPhotos().size() == 0) {
            String url = "https://api.foursquare.com/v2/venues/" +
                    venue.getId() + // venue id
                    "?client_id=" + getString(R.string.foursquare_client_id) + // client id
                    "&client_secret=" + getString(R.string.foursquare_client_secret) + // client key
                    "&v=20170412"; // api version

            RestClient restClient = RestClient.getInstance();
            restClient.get(url, new RequestParams(), new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        JSONObject photoListJSON = response.getJSONObject("response").getJSONObject("venue").getJSONObject("photos");

                        if (photoListJSON.getInt("count") == 0) {
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "No photos found for " + venue.getName(), Toast.LENGTH_LONG).show();

                        } else {
                            JSONArray photoGroupsJSON = photoListJSON.getJSONArray("groups");

                            for (int i = 0; i < photoGroupsJSON.length(); i++) {
                                JSONObject photoGroup = photoGroupsJSON.getJSONObject(i);

                                if (photoGroup.getInt("count") > 0) {
                                    JSONArray photoItems = photoGroup.getJSONArray("items");

                                    for (int j = 0; j < photoItems.length(); j++) {
                                        JSONObject photoJSON = photoItems.getJSONObject(j);

                                        Photo photo = new Photo();
                                        photo.setId(photoJSON.getString("id"));
                                        photo.setUrl(photoJSON.getString("prefix") + "800x600" + photoJSON.getString("suffix"));
                                        photo.setCreated(photoJSON.getLong("createdAt") * 1000);

                                        JSONObject uploaderJSON = photoJSON.getJSONObject("user");
                                        photo.setUploader(getStringFromJson(uploaderJSON, "firstName") + " " + getStringFromJson(uploaderJSON, "lastName"));

                                        int photoId = venue.addPhoto(photo);

                                        new ImageDownloader().execute(photoId);

                                    }
                                }
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

        } else {
            for (int i = 0; i < venue.getPhotos().size(); i++) {
                new ImageDownloader().execute(i);
            }
        }
    }

    private String getStringFromJson(JSONObject jsonObject, String property) {
        try {
            return jsonObject.getString(property);

        } catch (JSONException e) {
            return "";
        }
    }

    private class ImageDownloader extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            int photoId = (int) params[0];

            Photo photo = venue.getPhotos().get(photoId);

            if (!photo.hasBitmap()) {
                try {
                    URL url = new URL(photo.getUrl());

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream is = connection.getInputStream();

                    photo.setBitmap(BitmapFactory.decodeStream(is));

                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

                    photo.setBitmap(null);
                }
            }

            return photo;
        }

        @Override
        protected void onPostExecute(Object result) {
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);

            Photo photo = (Photo) result;

            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setAdjustViewBounds(true);
            imageView.setPadding(5, 5, 5, 5);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageBitmap(photo.getBitmap());
            imageView.setId(photo.getViewId());
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), PhotoActivity.class);
                    intent.putExtra(AppIntent.SHOW_VENUE, venueId);
                    intent.putExtra(AppIntent.SHOW_PHOTO, v.getId());
                    startActivity(intent);
                }
            });

            LinearLayout list = (LinearLayout) findViewById(R.id.thelist);
            list.addView(imageView);
        }
    }
}
