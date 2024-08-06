package com.james.imagereader;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public final static String DATABASE_NAME = "AssetsInfo.db";
    public final static String TABLE_NAME = "assets";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    public final static String COLUMN_ID = "id";
    public final static String COLUMN_PACKAGE_NAME = "packageName";
    public final static String COLUMN_PACKAGE_SIZE = "packageSize";
    public final static String COLUMN_DISPLAY_NAME = "displayName";
    public final static String COLUMN_IMAGE_COUNT = "imageCount";
    public final static String COLUMN_PROGRESS = "progress";
    public final static String COLUMN_OFFSET = "offset";
    public final static String COLUMN_FAVORITE = "favorite";
    public final static String[] COLUMNS = new String[]{COLUMN_PACKAGE_NAME, COLUMN_PACKAGE_SIZE, COLUMN_DISPLAY_NAME, COLUMN_IMAGE_COUNT, COLUMN_PROGRESS, COLUMN_OFFSET, COLUMN_FAVORITE};

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TABLE_NAME + "(id INTEGER PRIMARY KEY AUTOINCREMENT, packageName varchar(80) unique, displayName varchar(120), packageSize long, imageCount integer, progress integer, offset integer, favorite boolean);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void updateAssetInfo(AssetInfo assetInfo) {
        getWritableDatabase().update(DatabaseHelper.TABLE_NAME, assetInfo.getContentValues(), COLUMN_PACKAGE_NAME + "=?", new String[]{assetInfo.getPackageName()});
    }

    public AssetInfo getAssetInfo(String packageName) {
        AssetInfo assetInfo = new AssetInfo();
        Cursor cursor = getReadableDatabase().query(TABLE_NAME, COLUMNS, COLUMN_PACKAGE_NAME + "=?", new String[]{packageName}, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            assetInfo.setPackageName(packageName);
            long packageSize = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PACKAGE_SIZE));
            assetInfo.setPackageSize(packageSize);
            String displayName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISPLAY_NAME));
            assetInfo.setDisplayName(displayName);
            int imageCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_COUNT));
            assetInfo.setImageCount(imageCount);
            int progress = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS));
            assetInfo.setProgress(progress);
            int offset = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OFFSET));
            assetInfo.setOffset(offset);
            boolean favorite = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAVORITE)) == 1;
            assetInfo.setFavorite(favorite);
            cursor.close();
        }
        return assetInfo;
    }
    public int getTypeCount(String type) {
        try (Cursor mCursor = getReadableDatabase().rawQuery("select count(*) from " + DatabaseHelper.TABLE_NAME + " where packageName LIKE ?;", new String[]{"%" + type + "%"})) {
            if (mCursor != null && mCursor.moveToFirst()) {
                mCursor.getCount();
                return mCursor.getInt(0);
            }
        }
        return 0;
    }
}
