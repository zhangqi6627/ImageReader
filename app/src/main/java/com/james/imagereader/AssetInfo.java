package com.james.imagereader;

import android.content.ContentValues;

public class AssetInfo {
    private String packageName;
    private long packageSize;
    private String displayName;
    private int progress;
    private int images;
    private boolean favorite;
    private int offset;
    public AssetInfo(String packageName, long packageSize, String displayName, int images) {
        this.packageName = packageName;
        this.packageSize = packageSize;
        this.displayName = displayName;
        this.images = images;
    }
    public AssetInfo(String packageName, long packageSize, String displayName, int images, boolean favorite, int progress, int offset) {
        this.packageName = packageName;
        this.packageSize = packageSize;
        this.displayName = displayName;
        this.images = images;
        this.favorite = favorite;
        this.progress = progress;
        this.offset = offset;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public long getPackageSize() {
        return packageSize;
    }

    public void setPackageSize(long packageSize) {
        this.packageSize = packageSize;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getImages() {
        return images;
    }

    public void setImages(int images) {
        this.images = images;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("packageName", packageName);
        contentValues.put("displayName", displayName);
        contentValues.put("packageSize", packageSize);
        contentValues.put("imageCount", images);
        contentValues.put("progress", progress);
        contentValues.put("favorite", favorite);
        contentValues.put("offset", offset);
        return contentValues;
    }

    @Override
    public String toString() {
        return "AssetInfo{" +
                "packageName='" + packageName + '\'' +
                ", packageSize=" + packageSize +
                ", displayName='" + displayName + '\'' +
                ", progress=" + progress +
                ", images=" + images +
                ", favorite=" + favorite +
                ", offset=" + offset +
                '}';
    }
}