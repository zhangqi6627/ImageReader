package com.james.imagereader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends Activity {
    private final Context mContext = BaseActivity.this;
    private final String TAG = BaseActivity.this.getClass().getName();
    protected void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    protected int loadData(String key) {
        return getSharedPreferences("records", Context.MODE_PRIVATE).getInt(key, 0);
    }
    protected void saveData(String key, int value) {
        getSharedPreferences("records", Context.MODE_PRIVATE).edit().putInt(key, value).apply();
    }

    private AlertDialog dialog;
    protected List<String> getInstalledPackages() {
        List<String> packageList = new ArrayList<>();
        List<PackageInfo> packageInfoList = getPackageManager().getInstalledPackages(0);
        for (PackageInfo packageInfo : packageInfoList) {
            if (packageInfo.packageName.contains("com.google.imageassets")) {
                Log.e(TAG, "packageName2:" + packageInfo.packageName);
                packageList.add(packageInfo.packageName);
            }
        }
        return packageList;
    }

    protected Resources loadPackageResource(String packageName) {
        String dexPath = null;
        try {
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(packageName, 0);
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
            pluginContext = createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        PackageInfo pluginPackageArchiveInfo = getPackageManager().getPackageArchiveInfo(dexPath, PackageManager.GET_ACTIVITIES);
        Log.d(TAG, "loadApk: dexPath " + dexPath);
        Log.d(TAG, "loadApk: pluginPackageArchiveInfo " + pluginPackageArchiveInfo);

        //加载apk的资源
        AssetManager assets = null;
        try {
            assets = AssetManager.class.newInstance();
            Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assets, dexPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Resources pluginResources = new Resources(assets, getResources().getDisplayMetrics(), getResources().getConfiguration());
        Log.d(TAG, "loadApk: pluginResources " + pluginResources);
        return pluginResources;
    }
}
