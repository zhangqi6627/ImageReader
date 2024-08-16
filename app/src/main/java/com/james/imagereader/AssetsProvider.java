package com.james.imagereader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetsProvider {
    public final static String TAG = "AssetsProvider";
    private final BaseActivity mContext;
    private static AssetsProvider assetsProvider;
    private final DatabaseHelper mDatabaseHelper;

    private AssetsProvider(BaseActivity context) {
        mContext = context;
        mDatabaseHelper = new DatabaseHelper(mContext);
    }

    public static AssetsProvider getInstance(BaseActivity context) {
        if (assetsProvider == null) {
            assetsProvider = new AssetsProvider(context);
        }
        return assetsProvider;
    }

    protected String getPackageName(String apkPath) {
        PackageInfo info = mContext.getPackageManager().getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            try {
                return appInfo.packageName;
            } catch (Exception e) {
                Log.e("zq8888", e.toString());
            }
        }
        return null;
    }

    public List<AssetInfo> getAssetsInfoFromStorage() {
        List<AssetInfo> assetInfos = new ArrayList<>();
        tabTypes = new HashMap<>();
        SQLiteDatabase mDatabase = mDatabaseHelper.getWritableDatabase();
        File assetsFolder = mContext.getAssetsFolder();
        File[] apkFiles = assetsFolder.listFiles();
        if (apkFiles == null || apkFiles.length == 0) {
            return null;
        }
        for (File apkFile : apkFiles) {
            String pkgName = getPackageName(apkFile.getAbsolutePath());
            long pkgSize = apkFile.length();
            String displayName = apkFile.getName();
            int imageCount = Integer.parseInt(mContext.getAssetString(apkFile.getAbsolutePath(), "image_count"));
            AssetInfo assetInfo = new AssetInfo(pkgName, pkgSize, displayName, imageCount);
            assetInfos.add(assetInfo);
            String typeName = pkgName.split("\\.")[3];
            tabTypes.merge(typeName, 1, Integer::sum);
            try {
                mDatabase.replaceOrThrow(DatabaseHelper.TABLE_NAME, null, assetInfo.getContentValues());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        mContext.saveData("fileCount", apkFiles.length);
        return assetInfos;
    }

    public List<AssetInfo> getAssetsInfoFromInstalledPackage() {
        List<PackageInfo> packageInfoList = mContext.getPackageManager().getInstalledPackages(0);
        List<AssetInfo> assetInfos = new ArrayList<>();
        tabTypes = new HashMap<>();
        SQLiteDatabase mDatabase = mDatabaseHelper.getWritableDatabase();
        mDatabase.beginTransaction();
        for (PackageInfo packageInfo : packageInfoList) {
            if (packageInfo.packageName.contains("com.golds.assets.")) {
                String pkgName = packageInfo.packageName;
                long pkgSize = new File(packageInfo.applicationInfo.sourceDir).length();
                String displayName = mContext.getAssetString(pkgName, "app_name");
                if ("0".equalsIgnoreCase(displayName)) {
                    continue;
                }
                int imageCount = mContext.getAssetInt(pkgName, "image_count");
                AssetInfo assetInfo = new AssetInfo(pkgName, pkgSize, displayName, imageCount);
                assetInfos.add(assetInfo);
                String typeName = pkgName.split("\\.")[3];
                tabTypes.merge(typeName, 1, Integer::sum);
                try {
                    int updateResult = mDatabase.update(DatabaseHelper.TABLE_NAME, assetInfo.getContentValues(), "packageName=?", new String[]{assetInfo.getPackageName()});
                    if (updateResult <= 0) {
                        mDatabase.replaceOrThrow(DatabaseHelper.TABLE_NAME, null, assetInfo.getContentValues());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
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
                if (selection == null || packageName.contains("com.golds.assets." + type + ".")) {
                    String displayName = mCursor.getString(mCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DISPLAY_NAME));
                    long packageSize = mCursor.getLong(mCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PACKAGE_SIZE));
                    int progress = mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROGRESS));
                    int offset = mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OFFSET));
                    int imageCount = mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_COUNT));
                    int favorite = mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FAVORITE));
                    AssetInfo assetInfo = new AssetInfo(packageName, packageSize, displayName, imageCount, favorite == 1, progress, offset);
                    assetInfos.add(assetInfo);
                }
                tabTypes.merge(mType, 1, Integer::sum);
            } while (mCursor.moveToNext());
            mCursor.close();
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
}
