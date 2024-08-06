package com.james.imagereader;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Method;

public class BaseActivity extends AppCompatActivity {
    protected final Context mContext = BaseActivity.this;
    private final String TAG = BaseActivity.this.getClass().getName();

    private Toast mToast;

    protected void showToast(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        }
        mToast.setText(msg);
        mToast.show();
    }

    protected void showDialog() {
        // TODO:
    }

    private DatabaseHelper mDatabaseHelper;

    protected DatabaseHelper getDBHelper() {
        if (mDatabaseHelper == null) {
            mDatabaseHelper = new DatabaseHelper(mContext);
        }
        return mDatabaseHelper;
    }

    protected int loadData(String key) {
        return getSharedPreferences("records", Context.MODE_PRIVATE).getInt(key, 0);
    }

    protected void saveData(String key, int value) {
        getSharedPreferences("records", Context.MODE_PRIVATE).edit().putInt(key, value).apply();
    }

    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            BaseActivity.this.handleMessage(msg);
        }
    };

    protected void handleMessage(Message msg) {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
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
        AssetManager assets = null;
        try {
            assets = AssetManager.class.newInstance();
            Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assets, dexPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Resources(assets, getResources().getDisplayMetrics(), getResources().getConfiguration());
    }

    protected void uninstall(String packageName) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DELETE);//设置action为卸载已安装的包
        intent.setData(Uri.parse("package:" + packageName));//设置
        startActivityForResult(intent, 102);
    }

    protected long getPackageSize(String packageName) {
        long packageSize = 0;
        try {
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(packageName, 0);
            packageSize = new File(applicationInfo.sourceDir).length();
            Log.e("zq8888", "packageSize: " + packageSize);
            //assetInfo.setPackageSize(packageSize);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        return packageSize;
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

    public int getStatusBarHeight(Context context) {
        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height").get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return statusHeight;
    }
}
