package com.china.reader.imagereader.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.china.reader.imagereader.ConfigApplication;
import com.china.reader.imagereader.R;
import com.china.reader.imagereader.bean.ImageBean;
import com.china.reader.imagereader.bean.ImagePage;
import com.china.reader.imagereader.common.BaseActivity;
import com.china.reader.imagereader.common.GoogleAdListener;
import com.china.reader.imagereader.common.ImageUtils;
import com.china.reader.imagereader.common.LogUtils;
import com.china.reader.imagereader.common.UpdateAppHttpUtil;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.vector.update_app.UpdateAppManager;
import com.vector.update_app.listener.ExceptionHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MainActivity extends BaseActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        MobileAds.initialize(this, ConfigApplication.GOOGLE_ADS_ID);
        initBannerAd();

        // request runtime permissions
        verifyStoragePermissions(this);

        //check for version update
        //checkVersionUpdate();


    }

    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case 0x123:
                showSuperToast("解析完成");
                break;
        }
    }

    @OnClick(R.id.btn_image4_download)
    public void loadData(View view){
        // read from assets files
        //1.download file if not exists
        //2.parse file
        //3.load file into database
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtils.e("read1");
                try {
                    //parse file
                    InputStream inputStream = getResources().getAssets().open("mmjpg.json");
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String tmpStr = "";
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((tmpStr = bufferedReader.readLine()) != null) {
                        stringBuilder.append(tmpStr);
                    }

                    JSONArray albumJsonArray = JSONArray.parseArray(stringBuilder.toString());
                    List<JSONObject> albumList = albumJsonArray.toJavaList(JSONObject.class);
                    for (JSONObject albumJsonObj : albumList) {
                        LogUtils.e("albumJsonObj:" + albumJsonObj);
                        String title = albumJsonObj.getString("Title");
                        int webIndex = albumJsonObj.getIntValue("WebIndex");
                        int pageIndex = albumJsonObj.getIntValue("PageIndex");
                        String albumImage = albumJsonObj.getString("AlbumImage");
                        //load file into database
                        ImagePage imagePage = new ImagePage(webIndex, pageIndex, title, albumImage);
                        try {
                            ImageUtils.insertImagePage(imagePage);
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }
                        JSONArray imagesArray = albumJsonObj.getJSONArray("Images");
                        List<String> imageUrls = imagesArray.toJavaList(String.class);
                        ImageBean imageBean = null;
                        for (String imageUrl : imageUrls) {
                            imageBean = new ImageBean(webIndex, pageIndex, imageUrl);
                            try {
                                ImageUtils.insertImageBean(imageBean);
                            } catch (Exception e) {
                                e.printStackTrace();
                                continue;
                            }
                        }
                    }
                    sendEmptyMessage(0x123);
                } catch (IOException e) {
                    e.printStackTrace();
                    LogUtils.e("read3:" + e);
                }
                LogUtils.e("read4:");
            }
        }).start();
    }

    @OnClick({R.id.btn_image1, R.id.btn_image1_fav, R.id.btn_image2, R.id.btn_image2_fav, R.id.btn_image3, R.id.btn_image3_fav, R.id.btn_image4, R.id.btn_image4_fav})
    public void goToAlbumPage(View view) {
        int webIndex = BaseActivity.WEB_INDEX_1;
        boolean isFav = false;
        boolean needLogin = false;
        switch (view.getId()) {
            case R.id.btn_image1:
                webIndex = BaseActivity.WEB_INDEX_1;
                isFav = false;
                break;
            case R.id.btn_image1_fav:
                webIndex = BaseActivity.WEB_INDEX_1;
                isFav = true;
                break;
            case R.id.btn_image2:
                webIndex = BaseActivity.WEB_INDEX_2;
                isFav = false;
                break;
            case R.id.btn_image2_fav:
                webIndex = BaseActivity.WEB_INDEX_2;
                isFav = true;
                break;
            case R.id.btn_image3:
                webIndex = BaseActivity.WEB_INDEX_3;
                isFav = false;
                break;
            case R.id.btn_image3_fav:
                webIndex = BaseActivity.WEB_INDEX_3;
                isFav = true;
                break;
            case R.id.btn_image4:
                webIndex = BaseActivity.WEB_INDEX_4;
                isFav = false;
                break;
            case R.id.btn_image4_fav:
                webIndex = BaseActivity.WEB_INDEX_4;
                isFav = true;
                break;
        }
        goToImageList(webIndex, isFav, needLogin);
    }

    // request storage runtime permission
    private void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initBannerAd() {
        // google banner ads
        AdView mAdView = findViewById(R.id.adView);
        mAdView.loadAd(GoogleAdListener.getAdMobRequest());
        mAdView.setAdListener(GoogleAdListener.getInstance(mContext));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                String username = data.getStringExtra("username");
                ConfigApplication.username = username;
                showToast("username:" + username);
            }
        }
    }

    // jump to ImageListActivity
    private void goToImageList(int webIndex, boolean isFav, boolean needLogin) {
        if (needLogin) {
            startActivityForResult(new Intent(mContext, LoginActivity.class), 1);
        } else {
            Intent intent = new Intent(mContext, AlbumsActivity.class);
            intent.putExtra(BaseActivity.KEY_WEB_INDEX, webIndex);
            intent.putExtra(BaseActivity.KEY_WEB_ISFAV, isFav);
            startActivity(intent);
        }
    }

    @OnClick(R.id.btn_novel)
    void goToNovel() {
        startActivity(new Intent(mContext, NovelListActivity.class));
        //showImageToast("积分+1", R.mipmap.ic_launcher);
    }

    @OnClick(R.id.btn_login)
    void login() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void checkVersionUpdate() {
        new UpdateAppManager.Builder().setActivity(this)
                .setUpdateUrl("http://45.32.165.125/version.txt")//version.txt
                .handleException(new ExceptionHandler() {
                    @Override
                    public void onException(Exception e) {
                        e.printStackTrace();
                    }
                })
                .setHttpManager(new UpdateAppHttpUtil())
                .build()
                .update();
    }

    private String requestUrl(String url) throws IOException {
        return new OkHttpClient().newCall(new Request.Builder().url(url).build()).execute().body().string();
    }
}