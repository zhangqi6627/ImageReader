package com.james.imagereader;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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
                LogUtils.e("TAG", e.toString());
            }
        }
        return null;
    }

    public List<AssetInfo> getAssetsInfoFromStorage() {
        List<AssetInfo> assetInfos = new ArrayList<>();
        File[] apkFiles = mContext.getAssetsApkFiles();
        if (apkFiles == null || apkFiles.length == 0) {
            return null;
        }
        SQLiteDatabase mDatabase = mDatabaseHelper.getWritableDatabase();
        for (File apkFile : apkFiles) {
            String apkFilePath = apkFile.getAbsolutePath();
            String pkgName = getPackageName(apkFilePath);
            long pkgSize = apkFile.length();
            int imageCount = Integer.parseInt(mContext.getAssetString(apkFilePath, "image_count"));
            AssetInfo assetInfo = new AssetInfo(pkgName, pkgSize, apkFilePath, imageCount);
            assetInfo.setDisplayName(apkFilePath);
            assetInfos.add(assetInfo);
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
        return getTabTypesFromDB();
    }

    public synchronized List<AssetInfo> getAssetsInfoFromDB(String type) {
        return getAssetsInfoFromDB(type, AssetSortType.NAME_ASC);
    }

    public synchronized List<AssetInfo> getAssetsInfoFromDB(String type, AssetSortType sortType) {
        List<AssetInfo> assetInfos = new ArrayList<>();
        String selection = DatabaseHelper.COLUMN_PACKAGE_NAME + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + type + "%"};
        if (TextUtils.isEmpty(type)) {
            selection = null;
            selectionArgs = null;
        }
        Cursor mCursor = mDatabaseHelper.getReadableDatabase().query(DatabaseHelper.TABLE_NAME, DatabaseHelper.COLUMNS, selection, selectionArgs, null, null, DatabaseHelper.COLUMN_DISPLAY_NAME);
        if (mCursor != null) {
            try {
                if (mCursor.moveToFirst()) {
                    do {
                        String packageName = mCursor.getString(mCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PACKAGE_NAME));
                        if (selection == null || isAssetPackageOfType(packageName, type)) {
                            String displayName = mCursor.getString(mCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DISPLAY_NAME));

                            if (!new File(displayName).exists()) {
                                continue;
                            }
                            LogUtils.e(TAG, Thread.currentThread().getStackTrace()[2].getClassName()+"-->"+Thread.currentThread().getStackTrace()[2].getMethodName()+"()-->"+Thread.currentThread().getStackTrace()[2].getLineNumber() + " displayName: " + displayName);
                            long packageSize = mCursor.getLong(mCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PACKAGE_SIZE));
                            int progress = mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROGRESS));
                            int offset = mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OFFSET));
                            int imageCount = mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_COUNT));
                            int favorite = mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FAVORITE));
                            AssetInfo assetInfo = new AssetInfo(packageName, packageSize, displayName, imageCount, favorite == 1, progress, offset);
                            assetInfo.setLastReadTime(mContext.loadLongData(getLastReadTimeKey(packageName)));
                            assetInfos.add(assetInfo);
                        }
                    } while (mCursor.moveToNext());
                }
            } finally {
                mCursor.close();
            }
        }
        Collections.sort(assetInfos, sortType.getComparator());
        return assetInfos;
    }

    public synchronized Map<String, Integer> getTabTypesFromDB() {
        Map<String, Integer> result = new LinkedHashMap<>();
        Cursor cursor = mDatabaseHelper.getReadableDatabase().query(
                DatabaseHelper.TABLE_NAME,
                DatabaseHelper.COLUMNS,
                null,
                null,
                null,
                null,
                DatabaseHelper.COLUMN_PACKAGE_NAME);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        String packageName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PACKAGE_NAME));
                        String typeName = getTypeFromPackageName(packageName);
                        if (typeName == null) {
                            continue;
                        }
                        String displayName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DISPLAY_NAME));
                        if (!new File(displayName).exists()) {
                            continue;
                        }
                        result.merge(typeName, 1, Integer::sum);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        return result;
    }

    private boolean isAssetPackageOfType(String packageName, String type) {
        if (TextUtils.isEmpty(type)) {
            return true;
        }
        String typeName = getTypeFromPackageName(packageName);
        return type.equals(typeName);
    }

    private String getTypeFromPackageName(String packageName) {
        if (TextUtils.isEmpty(packageName) || !packageName.startsWith("com.golds.assets.")) {
            return null;
        }
        String[] nameParts = packageName.split("\\.");
        if (nameParts.length <= 3) {
            return null;
        }
        return nameParts[3];
    }

    public static String getLastReadTimeKey(String packageName) {
        return "lastReadTime." + packageName;
    }

    public synchronized boolean deleteAssetFileAndRecord(AssetInfo assetInfo) {
        if (assetInfo == null || TextUtils.isEmpty(assetInfo.getDisplayName())) {
            return false;
        }
        File apkFile = new File(assetInfo.getDisplayName());
        boolean fileDeleted = !apkFile.exists() || (apkFile.isFile() && apkFile.delete());
        if (!fileDeleted) {
            return false;
        }
        mDatabaseHelper.deleteAssetInfo(assetInfo.getPackageName());
        File[] apkFiles = mContext.getAssetsApkFiles();
        mContext.saveData("fileCount", apkFiles == null ? 0 : apkFiles.length);
        return true;
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
