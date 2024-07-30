package com.james.imagereader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO：
 *  #1.按包名分类
 *  #2.数据库
 *  #3.显示apk的体积大小
 *  4.网格布局
 *  #5.根据名称排序
 *  #6.如何去掉已经被卸载的apk?
 */
public class AlbumsActivity extends BaseActivity {
    private final static String TAG = "AlbumsActivity";
    private final Context mContext = AlbumsActivity.this;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TabsAdapter tabsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabsAdapter = new TabsAdapter(getSupportFragmentManager(), new HashSet<String>());
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                AssetsProvider.getInstance(mContext).getAssetPackagesFromDB("");
                mHandler.sendEmptyMessage(0);
            }
        }).start();
        Intent workIntent = new Intent();
        workIntent.putExtra("work","loadAssets");
        AssetsIntentService.enqueueWork(mContext, workIntent);
    }
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Set<String> types = AssetsProvider.getInstance(mContext).getTypes();
            tabsAdapter.setTabs(types);
        }
    };
}