package com.china.reader.imagereader.activity;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.china.reader.imagereader.R;
import com.china.reader.imagereader.adapter.ImagePagerAdapter;
import com.china.reader.imagereader.adapter.ImageRecyclerAdapter;
import com.china.reader.imagereader.bean.ImageBean;
import com.china.reader.imagereader.bean.ImagePage;
import com.china.reader.imagereader.common.BaseActivity;
import com.china.reader.imagereader.common.GoogleAdListener;
import com.china.reader.imagereader.common.GreenDaoManager;
import com.china.reader.imagereader.common.ImageUtils;
import com.china.reader.imagereader.common.LogUtils;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;


public class ImagesActivity extends BaseActivity {
    private ArrayList<ImageBean> imageBeans = new ArrayList<ImageBean>();
    private ImageRecyclerAdapter adapter;
    private String pageUrlFormat;
    private ImagePage imagePage;
    private int pageIndex;
    private int webIndex;
    private boolean isFavorite;

    @BindView(R.id.cb_fav)
    CheckBox cb_fav;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.adView)
    AdView mAdView;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.adView1)
    AdView mAdView1;
    @BindView(R.id.tv_indicator)
    TextView tv_indicator;

    @OnCheckedChanged(R.id.cb_fav)
    void onCheckFav(CompoundButton compoundButton, boolean isChecked) {
        LogUtils.e("ImagesActivity->onCheckFav()" + isChecked);
        imagePage.setIsFavorite(isChecked);
        ImageUtils.updateImagePage(imagePage);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // full screen, remove status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image_list);
        ButterKnife.bind(this);

        imagePage = (ImagePage) getIntent().getSerializableExtra(BaseActivity.KEY_IMAGE_PAGE);
        webIndex = imagePage.getWebIndex();
        pageIndex = imagePage.getPageIndex();
        isFavorite = imagePage.getIsFavorite();

        // load images
        List<ImagePage> imagePages = GreenDaoManager.getInstance().getDaoSession().getImagePageDao().queryRaw("where WEB_INDEX=? and PAGE_INDEX=?", webIndex + "", pageIndex + "");
        if (imagePages.size() > 0) {
            imagePage = imagePages.get(0);
        }

        // setChecked
        cb_fav.setChecked(isFavorite);
        // recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LogUtils.e("onScrollStateChanged:" + newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Picasso.get().resumeTag(mContext);
                } else {
                    Picasso.get().pauseTag(mContext);
                }
            }
        });
        adapter = new ImageRecyclerAdapter(mContext, imageBeans);
        adapter.setItemClickListener(new ImageRecyclerAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                LogUtils.e("ImagesActivity->onItemClick(" + position + ")");
            }
        });
        recyclerView.setAdapter(adapter);


        loadImages();
        initBannerAd();
    }

    public TextView getIndicatorView() {
        return tv_indicator;
    }

    // google banner ads
    private void initBannerAd() {

        mAdView.setAdListener(GoogleAdListener.getInstance(mContext));
        mAdView.loadAd(GoogleAdListener.getAdMobRequest());

        mAdView1.setAdListener(GoogleAdListener.getInstance(mContext));
        mAdView1.loadAd(GoogleAdListener.getAdMobRequest());
    }

    private void loadImages() {
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                imageBeans = ImageUtils.getImageBeans(imagePage);
                sendEmptyMessage(MSG_GET_IMAGE_LIST);
            }
        }).start();
    }

    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case MSG_GET_IMAGE_LIST:
                adapter.setDataList(imageBeans);
                imagePage.setIsViewed(true);
                ImageUtils.updateImagePage(imagePage);
                hideProgressDialog();
                viewPager.setAdapter(new ImagePagerAdapter(mContext, imageBeans));
                break;
        }
    }
}
