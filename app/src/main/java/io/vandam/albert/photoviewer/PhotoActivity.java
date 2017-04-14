package io.vandam.albert.photoviewer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import io.vandam.albert.photoviewer.domain.Photo;
import io.vandam.albert.photoviewer.domain.Venues;
import io.vandam.albert.photoviewer.gesture.OnSwipeTouchListener;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class PhotoActivity extends AppCompatActivity {
    Venues venueList;
    int venueId;
    int photoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_photo);

        View container = findViewById(R.id.container);
        container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
        container.setOnTouchListener(new OnSwipeTouchListener(PhotoActivity.this) {
            public void onSwipeRight() {
                photoId--;
                if (photoId < 0) {
                    photoId = venueList.get(venueId).getPhotos().size() - 1;
                }

                showPhoto();
            }

            public void onSwipeLeft() {
                photoId++;
                if (photoId >= venueList.get(venueId).getPhotos().size()) {
                    photoId = 0;
                }

                showPhoto();
            }
        });

        venueList = Venues.getInstance();

        // Get the AppIntent that started this activity and extract the string
        Intent intent = getIntent();
        venueId = intent.getIntExtra(AppIntent.SHOW_VENUE, -1);
        photoId = intent.getIntExtra(AppIntent.SHOW_PHOTO, -1);

        if (venueId == -1 || photoId == -1) {
            Toast.makeText(this.getApplicationContext(), "Unknown venue/photo requested", Toast.LENGTH_LONG).show();

        } else {
            showPhoto();
        }
    }

    private void showPhoto() {
        Photo photo = venueList.get(venueId).getPhotos().get(photoId);

        ImageView imageView = (ImageView) findViewById(R.id.photo);
        imageView.setImageBitmap(photo.getBitmap());

        Date date = new Date(photo.getCreated());

        String meta = "Uploaded by " + photo.getUploader() + " on ";
        meta += DateFormat.getDateInstance(DateFormat.LONG, Locale.US).format(date);

        TextView credit = (TextView) findViewById(R.id.credit);
        credit.setText(meta);
    }
}
