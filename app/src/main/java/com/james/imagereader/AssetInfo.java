package com.james.imagereader;

import android.content.ContentValues;

/*
TODO：把AssetInfo改造成 Map形式的？
 */
public class AssetInfo {
    private String packageName;
    private long packageSize;
    private String displayName;
    private int progress;
    private int imageCount;
    private boolean favorite;
    private int offset;
    private ContentValues contentValues = new ContentValues();
    public AssetInfo() {
    }
    public AssetInfo(String packageName, long packageSize, String displayName, int imageCount) {
        this.packageName = packageName;
        this.packageSize = packageSize;
        this.displayName = displayName;
        this.imageCount = imageCount;
        contentValues.put(DatabaseHelper.COLUMN_DISPLAY_NAME, "");
    }
    public AssetInfo(String packageName, long packageSize, String displayName, int imageCount, boolean favorite, int progress, int offset) {
        this.packageName = packageName;
        this.packageSize = packageSize;
        this.displayName = displayName;
        this.imageCount = imageCount;
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

    public int getImageCount() {
        return imageCount;
    }

    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
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
        contentValues.put("imageCount", imageCount);
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
                ", images=" + imageCount +
                ", favorite=" + favorite +
                ", offset=" + offset +
                '}';
    }
}