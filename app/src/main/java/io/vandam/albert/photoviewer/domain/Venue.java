package io.vandam.albert.photoviewer.domain;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

public class Venue {
    private String id;
    private String name;
    private String category;
    private Bitmap categoryIcon;
    private String categoryIconUrl;
    private List<Photo> photos = new ArrayList<>();
    private double latitude;
    private double longitude;
    private Marker marker;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryIconUrl() {
        return categoryIconUrl;
    }

    public void setCategoryIconUrl(String categoryIconUrl) {
        this.categoryIconUrl = categoryIconUrl;
    }

    public int addPhoto(Photo photo) {
        int id = photos.size();
        photo.setViewId(id);
        photos.add(id, photo);
        return id;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public Bitmap getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(Bitmap categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    public boolean hasIcon() {
        return categoryIcon != null;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

}
