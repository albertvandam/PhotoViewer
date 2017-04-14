package io.vandam.albert.photoviewer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import io.vandam.albert.photoviewer.domain.Venue;
import io.vandam.albert.photoviewer.domain.Venues;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, OnMarkerClickListener {
    Venues venueList;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_list:
                finish();
                break;

            default:
                break;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        venueList = Venues.getInstance();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLngBounds bounds = null;

        LatLng currentLocation = new LatLng(MainActivity.currentLatitude, MainActivity.currentLongitude);
        googleMap.addMarker(new MarkerOptions().position(currentLocation).title(getString(R.string.my_location)).icon(BitmapDescriptorFactory.defaultMarker(30)));

        for (Venue venue : venueList.getAll()) {
            LatLng location = new LatLng(venue.getLatitude(), venue.getLongitude());

            if (bounds == null) {
                bounds = new LatLngBounds(currentLocation, location);
            } else {
                bounds.including(location);
            }

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(location);
            markerOptions.title(venue.getName());

            venue.setMarker(googleMap.addMarker(markerOptions));
        }

        googleMap.setOnMarkerClickListener(this);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        int venueCounter = 0;
        boolean found = false;

        while (!found && venueCounter < venueList.size()) {
            if (venueList.get(venueCounter).getMarker().equals(marker)) {
                found = true;

                Intent intent = new Intent(getApplicationContext(), VenueActivity.class);
                intent.putExtra(AppIntent.SHOW_VENUE, venueCounter);
                startActivity(intent);
            }

            venueCounter++;
        }

        return found;
    }
}
