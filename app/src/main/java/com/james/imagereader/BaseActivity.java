package com.james.imagereader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Method;

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
        int length = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).list().length;
        Log.e("zq8888", "downloads: " + length);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (Environment.isExternalStorageManager()) {
                Toast.makeText(this, "MANAGE_EXTERNAL_STORAGE GRANTED!", Toast.LENGTH_LONG).show();
                onPermissionGranted();
            } else {
                Toast.makeText(this, "NO MANAGE_EXTERNAL_STORAGE GRANTED!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent, 3);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
            } else {
                // 请求读取外部存储的权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }
        //Bitmap.createScaledBitmap()
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) || Environment.isExternalStorageManager()) {
                    onPermissionGranted();
                } else {
                    showToast("No permission");
                }
                Log.e("zq8888", "onRequest()");
                break;
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
                Log.e("zq8888", e.toString());
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

    protected File getAssetsFolder() {
        File imageReaderFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ImageReader/");
        if (!imageReaderFolder.exists() && imageReaderFolder.mkdir()) {
            Log.e("zq8888", "mkdir success");
        }
        return imageReaderFolder;
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
        int strId = mResources.getIdentifier(idName, "string", mPackageName);
        return mResources.getString(strId);
    }

    protected int getAssetInt(String nameOrPath, String idName) {
        Resources mResources = loadPackageResource(nameOrPath);
        String mPackageName = nameOrPath;
        if (nameOrPath.endsWith(".apk")) {
            mPackageName = getPackageName(nameOrPath);
        }
        Log.e("zq8888", "packageName: " + mPackageName + " nameOrPath:" + nameOrPath);
        int strId = mResources.getIdentifier(idName, "integer", mPackageName);
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
