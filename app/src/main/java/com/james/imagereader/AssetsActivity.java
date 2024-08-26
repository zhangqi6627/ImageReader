package com.james.imagereader;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.SystemClock;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.view.ViewPager;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * TODO：
 *  #01.按包名分类
 *  #02.数据库
 *  #03.显示apk的体积大小
 *  04.网格布局，显示封面缩略图
 *  #05.根据名称排序
 *  #06.如何去掉已经被卸载的apk?
 *  #07.自定义View显示进度条
 *  08.添加Text功能
 *  09.添加 fress/reading/done 页面
 *  10.添加分享功能，生成海报
 *  11.添加开屏动画
 *  12.添加网络功能
 *  13.添加 订阅/图源/书源 功能
 *  14.添加广告？寻找广告合作商
 *  15.添加视频播放功能
 *  16.添加横屏功能
 *  17.添加图片处理滤镜功能
 *  18.在保护页上添加 TG/FB/X/Tiktok 等链接
 *  19.添加积分功能
 *  20.添加OTA升级功能
 *  #21.JobIntentService 执行完成之后通知RV刷新？Toast显示
 *  #22.插入数据库的时候用事务处理，会导致数据库查询卡住
 *  #23.TAB 下面显示每种类型的apk数量?
 *  24.图片显示还有问题，有些图片没有显示屏幕宽度
 *  25.遍历扫描指定路径下的所有apk文件
 *  26.判断是否能上google
 *  27.如果不能显示国外的广告就显示自己的广告
 */
public class AssetsActivity extends BaseActivity {
    private final static String TAG = "AlbumsActivity";
    private ViewPager viewPager;
    private TabsAdapter tabsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabsAdapter = new TabsAdapter(this, getSupportFragmentManager(), new HashMap<>());
        viewPager.setAdapter(tabsAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    protected void onPermissionGranted() {
        super.onPermissionGranted();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<AssetInfo> assetInfos = AssetsProvider.getInstance(AssetsActivity.this).getAssetsInfoFromDB("");
                mHandler.sendEmptyMessageDelayed(0, 0);
            }
        });
        int savedFileCount = loadData("fileCount");
        int realFileCount = Objects.requireNonNull(getAssetsFolder().list()).length;
        if (savedFileCount != realFileCount) {
            showToast("检测到文件夹有变化，开始扫描");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //SystemClock.sleep(2000);
                    long timeBegin = System.currentTimeMillis();
                    AssetsProvider.getInstance(AssetsActivity.this).getAssetsInfoFromStorage();
                    long timeSecond = System.currentTimeMillis();
                    mHandler.sendMessage(mHandler.obtainMessage(1, (int) ((timeSecond - timeBegin) / 1000), 0));
                    // 扫描数据库中的记录，如果应用不存在就删除记录，并且更新RecyclerView
                    //AssetsProvider.getInstance(mContext).deleteItemIfNotExist();
                    AssetsProvider.getInstance(AssetsActivity.this).getAssetsInfoFromDB("");
                    long timeEnd = System.currentTimeMillis();
                    mHandler.sendMessage(mHandler.obtainMessage(2, (int) ((timeEnd - timeSecond) / 1000), 0));
                }
            }).start();
        }
    }

    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case 0:
                showToast("数据库加载完成");
                tabsAdapter.setTabs(AssetsProvider.getInstance(AssetsActivity.this).getTabTypes());
                break;
            case 1:
                showToast("插件扫描完成:" + msg.arg1 + "s");
                tabsAdapter.setTabs(AssetsProvider.getInstance(AssetsActivity.this).getTabTypes());
                break;
            case 2:
                showToast("数据库更新完毕:" + msg.arg1 + "s");
                break;
        }
    }
}