package com.james.imagereader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetsProvider {
    public final static String TAG = "AssetsProvider";
    private final Context mContext;
    private static AssetsProvider assetsProvider;
    private final DatabaseHelper mDatabaseHelper;

    private AssetsProvider(Context context) {
        mContext = context;
        mDatabaseHelper = new DatabaseHelper(mContext);
    }

    public static AssetsProvider getInstance(Context context) {
        if (assetsProvider == null) {
            assetsProvider = new AssetsProvider(context);
        }
        return assetsProvider;
    }

    public List<AssetInfo> getAssetsInfoFromApk(String type) {
        List<PackageInfo> packageInfoList = mContext.getPackageManager().getInstalledPackages(0);
        List<AssetInfo> assetInfos = new ArrayList<>();
        tabTypes = new HashMap<>();
        SQLiteDatabase mDatabase = mDatabaseHelper.getWritableDatabase();
        for (PackageInfo packageInfo : packageInfoList) {
            if (packageInfo.packageName.contains("com.golds.assets." + type)) {
                String pkgName = packageInfo.packageName;
                long pkgSize = new File(packageInfo.applicationInfo.sourceDir).length();
                String displayName = getAssetString(pkgName, "app_name");
                if ("0".equalsIgnoreCase(displayName)) {
                    continue;
                }
                int imageCount = Integer.parseInt(getAssetString(pkgName, "image_count"));
                AssetInfo assetInfo = new AssetInfo(pkgName, pkgSize, displayName, imageCount);
                assetInfos.add(assetInfo);
                tabTypes.put(pkgName.split("\\.")[3], 0);
                mDatabase.insert(DatabaseHelper.TABLE_NAME, null, assetInfo.getContentValues());
            }
        }
        return assetInfos;
    }

    public Map<String, Integer> getTabTypes() {
        return tabTypes;
    }

    private Map<String, Integer> tabTypes = new HashMap<>();

    public List<AssetInfo> getAssetsInfoFromDB(String type) {
        List<PackageInfo> packageInfoList = mContext.getPackageManager().getInstalledPackages(0);
        List<AssetInfo> assetInfos = new ArrayList<>();
        tabTypes = new HashMap<>();
        String selection = DatabaseHelper.COLUMN_PACKAGE_NAME + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + type + "%"};
        if (TextUtils.isEmpty(type)) {
            selection = null;
            selectionArgs = null;
        }
        Cursor mCursor = mDatabaseHelper.getReadableDatabase().query(DatabaseHelper.TABLE_NAME, DatabaseHelper.COLUMNS, selection, selectionArgs, null, null, DatabaseHelper.COLUMN_DISPLAY_NAME);
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                String packageName = mCursor.getString(mCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PACKAGE_NAME));
                String mType = packageName.split("\\.")[3];
                tabTypes.put(mType, 0);
                if (packageName.contains("com.golds.assets." + type + ".")) {
                    String displayName = mCursor.getString(mCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DISPLAY_NAME));
                    long packageSize = mCursor.getLong(mCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PACKAGE_SIZE));
                    int progress = mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROGRESS));
                    int offset = mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OFFSET));
                    int imageCount = mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_COUNT));
                    int favorite = mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FAVORITE));
                    AssetInfo assetInfo = new AssetInfo(packageName, packageSize, displayName, imageCount, favorite == 1, progress, offset);
                    Log.e(TAG, "mType: " + mType + " assetInfo: " + assetInfo);
                    assetInfos.add(assetInfo);
                }
            } while (mCursor.moveToNext());
            mCursor.close();
        }
        if (!TextUtils.isEmpty(type)) {
            tabTypes.put(type, assetInfos.size());
        }
        return assetInfos;
    }

    public void deleteItemIfNotExist() {
        Cursor cursor = mDatabaseHelper.getReadableDatabase().query(DatabaseHelper.TABLE_NAME, new String[]{"packageName"}, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String packageName = cursor.getString(cursor.getColumnIndexOrThrow("packageName"));
                if (!Utils.isAppInstalled(mContext, packageName)) {
                    mDatabaseHelper.getWritableDatabase().delete(DatabaseHelper.TABLE_NAME, "packageName=?", new String[]{packageName});
                }
            } while (cursor.moveToNext());
        }
    }

    protected String getAssetString(String packageName, String idName) {
        Resources mResources = loadPackageResource(packageName);
        if (mResources == null) {
            return "0";
        }
        int strId = mResources.getIdentifier(idName, "string", packageName);
        return mResources.getString(strId);
    }

    protected int getAssetInt(String packageName, String idName) {
        Resources mResources = loadPackageResource(packageName);
        int strId = mResources.getIdentifier(idName, "integer", packageName);
        return mResources.getInteger(strId);
    }

    protected Resources loadPackageResource(String packageName) {
        String dexPath = null;
        try {
            ApplicationInfo applicationInfo = mContext.getPackageManager().getApplicationInfo(packageName, 0);
            dexPath = applicationInfo.sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //check path
        if (TextUtils.isEmpty(dexPath)) {
            return null;
        }

        Context pluginContext = null;
        try {
            pluginContext = mContext.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        PackageInfo pluginPackageArchiveInfo = mContext.getPackageManager().getPackageArchiveInfo(dexPath, PackageManager.GET_ACTIVITIES);
        AssetManager assets = null;
        try {
            assets = AssetManager.class.newInstance();
            Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assets, dexPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Resources(assets, mContext.getResources().getDisplayMetrics(), mContext.getResources().getConfiguration());
    }
}
