package com.james.imagereader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AssetsProvider {
    public final static String TAG = "AssetsProvider";
    private Context mContext;
    private static AssetsProvider assetsProvider;
    private DatabaseHelper mDatabaseHelper;
    private AssetsProvider(Context context) {
        mContext = context;
        mDatabaseHelper = new DatabaseHelper(mContext);
    }
    public static AssetsProvider getInstance(Context context) {
        if (assetsProvider == null) {
            assetsProvider = new AssetsProvider(context);
        }
        return  assetsProvider;
    }
    public List<AssetInfo> getAssetPackages(String type) {
        List<PackageInfo> packageInfoList = mContext.getPackageManager().getInstalledPackages(0);
        List<AssetInfo> assetInfos = new ArrayList<>();
        types = new HashSet<>();
        //mDatabaseHelper.getWritableDatabase().beginTransaction();
        for (PackageInfo packageInfo : packageInfoList) {
            if (packageInfo.packageName.contains("com.golds.assets." + type)) {
                String pkgName = packageInfo.packageName;
                long pkgSize = new File(packageInfo.applicationInfo.sourceDir).length();
                String displayName = getAssetString(pkgName, "app_name");
                int imageCount = Integer.parseInt(getAssetString(pkgName, "image_count"));
                AssetInfo assetInfo = new AssetInfo(pkgName, pkgSize, displayName, imageCount);
                assetInfos.add(assetInfo);
                String tabType = pkgName.split("\\.")[3];
                types.add(tabType);
                mDatabaseHelper.getWritableDatabase().insert(DatabaseHelper.TABLE_NAME, null, assetInfo.getContentValues());
            }
        }
        //mDatabaseHelper.getWritableDatabase().endTransaction();
        return assetInfos;
    }
    public Set<String> getTypes() {
        return types;
    }
    private Set<String> types = new HashSet<>();
    public List<AssetInfo> getAssetPackagesFromDB(String type) {
        List<PackageInfo> packageInfoList = mContext.getPackageManager().getInstalledPackages(0);
        List<AssetInfo> assetInfos = new ArrayList<>();
        types = new HashSet<>();
        Log.e("zq8888", "type1: " + type);
        String selection = "packageName LIKE ?";
        String[] selectionArgs = new String[] { "%" + type + "%" };
        if (TextUtils.isEmpty(type)) {
            selection = null;
            selectionArgs = null;
        }
        Cursor mCursor = mDatabaseHelper.getReadableDatabase().query(DatabaseHelper.TABLE_NAME, new String[]{"packageName", "displayName", "progress", "offset", "favorite", "imageCount", "packageSize"}, selection, selectionArgs, null, null, "displayName");
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                String packageName = mCursor.getString(mCursor.getColumnIndexOrThrow("packageName"));
                String mType = packageName.split("\\.")[3];
                types.add(mType);
                if (packageName.contains("com.golds.assets." + type)) {
                    String displayName = mCursor.getString(mCursor.getColumnIndexOrThrow("displayName"));
                    long packageSize = mCursor.getLong(mCursor.getColumnIndexOrThrow("packageSize"));
                    int progress = mCursor.getInt(mCursor.getColumnIndexOrThrow("progress"));
                    int offset = mCursor.getInt(mCursor.getColumnIndexOrThrow("offset"));
                    int imageCount = mCursor.getInt(mCursor.getColumnIndexOrThrow("imageCount"));
                    int favorite = mCursor.getInt(mCursor.getColumnIndexOrThrow("favorite"));
                    AssetInfo assetInfo = new AssetInfo(packageName, packageSize, displayName, imageCount, favorite == 1, progress, offset);
                    Log.e(TAG, "mType: " + mType + " assetInfo: " + assetInfo);
                    assetInfos.add(assetInfo);
                }
            } while (mCursor.moveToNext());
            mCursor.close();
        }
        return assetInfos;
    }
    protected String getAssetString(String packageName, String idName) {
        Resources mResources = loadPackageResource(packageName);
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
