package io.vandam.albert.photoviewer.domain;

import android.graphics.Bitmap;

public class Photo {
    private int viewId;
    private String id;
    private String url;
    private Bitmap bitmap;
    private long created;
    private String uploader;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getViewId() {
        return viewId;
    }

    void setViewId(int viewId) {
        this.viewId = viewId;
    }

    public boolean hasBitmap() {
        return bitmap != null;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader.trim();
    }
}
