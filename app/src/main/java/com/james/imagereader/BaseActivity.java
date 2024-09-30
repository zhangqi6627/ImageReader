package com.james.imagereader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class BaseActivity extends AppCompatActivity {
    protected final Context mContext = BaseActivity.this;
    private final String TAG = BaseActivity.this.getClass().getName();
    public final static int PERMISSION_REQUEST_CODE = 3;

    private Toast mToast;

    protected void showToast(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        }
        mToast.setText(msg);
        mToast.show();
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

    @SuppressLint("HandlerLeak")
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
        // 先判断有没有权限
        if (Environment.isExternalStorageManager()) {
            onPermissionGranted();
        } else {
            Toast.makeText(this, "NO MANAGE_EXTERNAL_STORAGE GRANTED!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + this.getPackageName()));
            startActivityForResult(intent, 3);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) || Environment.isExternalStorageManager()) {
                onPermissionGranted();
            } else {
                showToast("No permission");
            }
        }
    }

    protected void onPermissionGranted() {

    }

    protected String getPackageName(String apkPath) {
        PackageInfo info = mContext.getPackageManager().getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            try {
                return appInfo.packageName;
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
        return null;
    }

    protected Resources loadPackageResource(String nameOrPath) {
        return new Resources(getPluginAssets(getApkPath(nameOrPath)), getResources().getDisplayMetrics(), getResources().getConfiguration());
    }

    protected String getApkPath(String nameOrPath) {
        if (nameOrPath != null && nameOrPath.endsWith(".apk")) {
            return nameOrPath;
        }
        String dexPath = null;
        try {
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(nameOrPath, 0);
            dexPath = applicationInfo.sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return dexPath;
    }

    protected void log(String msg) {
        Log.e(TAG, Thread.currentThread().getStackTrace()[2].getClassName() + "-->" + Thread.currentThread().getStackTrace()[2].getMethodName() + "()-->" + Thread.currentThread().getStackTrace()[2].getLineNumber() + "msg: " + msg);
    }
    protected String imageReaderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "0000ImageReader";

    protected File[] getAssetsApkFiles() {
        File imageReaderFolder = new File(imageReaderPath);
        if (!imageReaderFolder.exists() && imageReaderFolder.mkdir()) {
            Log.e(TAG, "mkdir success");
        }
        scanAssetsFiles(imageReaderFolder);
        return assetsApkFiles.toArray(new File[]{});
    }

    private Set<File> assetsApkFiles = new HashSet<>();

    protected void scanAssetsFiles(File file) {
        for (File subFile : file.listFiles()) {
            if (subFile.isDirectory()) {
                scanAssetsFiles(subFile);
            } else if (subFile.getName().endsWith(".apk")) {
                assetsApkFiles.add(subFile);
            }
        }
    }

    protected AssetManager getPluginAssets(String dexPath) {
        AssetManager assets = null;
        try {
            assets = AssetManager.class.newInstance();
            Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assets, dexPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return assets;
    }

    protected void uninstall(String packageName) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DELETE);//设置action为卸载已安装的包
        intent.setData(Uri.parse("package:" + packageName));//设置
        startActivityForResult(intent, 102);
    }

    protected long getPackageSize(String nameOrPath) {
        return new File(getApkPath(nameOrPath)).length();
    }

    protected String getAssetString(String nameOrPath, String idName) {
        Resources mResources = loadPackageResource(nameOrPath);
        String mPackageName = nameOrPath;
        if (nameOrPath.endsWith(".apk")) {
            mPackageName = getPackageName(nameOrPath);
        }
        return mResources.getString(mResources.getIdentifier(idName, "string", mPackageName));
    }

    protected int getAssetInt(String nameOrPath, String idName) {
        Resources mResources = loadPackageResource(nameOrPath);
        String mPackageName = nameOrPath;
        if (nameOrPath.endsWith(".apk")) {
            mPackageName = getPackageName(nameOrPath);
        }
        return mResources.getInteger(mResources.getIdentifier(idName, "integer", mPackageName));
    }
}
