package com.china.reader.imagereader.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.china.reader.imagereader.R;
import com.china.reader.imagereader.adapter.AlbumAdapter;
import com.china.reader.imagereader.bean.ImagePage;
import com.china.reader.imagereader.common.BaseActivity;
import com.china.reader.imagereader.common.ImageUtils;
import com.china.reader.imagereader.common.LogUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AlbumsActivity extends BaseActivity {
    @BindView(R.id.rv_image_title_list)
    RecyclerView rv_imagelist;
    private AlbumAdapter albumAdapter;
    private int currentPageIndex;
    private int webIndex = WEB_INDEX_1;
    private boolean isFavorite;
    private List<ImagePage> imagePages = new ArrayList<>();

    @OnClick(R.id.btn_last)
    void lastPage() {
        if (currentPageIndex > 0) {
            loadPage(--currentPageIndex);
        }
    }

    @OnClick(R.id.btn_next)
    void nextPage() {
        loadPage(++currentPageIndex);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_title_list);
        ButterKnife.bind(this);
        webIndex = getIntent().getIntExtra(BaseActivity.KEY_WEB_INDEX, WEB_INDEX_1);
        isFavorite = getIntent().getBooleanExtra(BaseActivity.KEY_WEB_ISFAV, false);
        //
        rv_imagelist.setHasFixedSize(true);
        rv_imagelist.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        rv_imagelist.setItemAnimator(new DefaultItemAnimator());
        albumAdapter = new AlbumAdapter(mContext, imagePages);
        rv_imagelist.setAdapter(albumAdapter);
        albumAdapter.setItemClickListener(new AlbumAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TextView tv_title = view.findViewById(R.id.tv_title);
                tv_title.setTextColor(Color.GRAY);
                Object mTag = tv_title.getTag();
                if (mTag instanceof ImagePage) {
                    Intent intent = new Intent(mContext, ImagesActivity.class);
                    intent.putExtra("imagePage", (ImagePage) mTag);
                    startActivity(intent);
                }
            }
        });
        rv_imagelist.setVerticalScrollBarEnabled(true);
        rv_imagelist.setHorizontalScrollBarEnabled(true);
        if (webIndex == WEB_INDEX_1) {
            currentPageIndex = getSharedPref().getInt("pageIndex1", 0);
        } else if (webIndex == WEB_INDEX_2) {
            currentPageIndex = getSharedPref().getInt("pageIndex2", 0);
        } else if (webIndex == WEB_INDEX_4) {
            currentPageIndex = getSharedPref().getInt("pageIndex4", 0);
        }
        LogUtils.e("AlbumsActivity->currentPageIndex:" + currentPageIndex);
        loadPage(currentPageIndex);
    }

    private void loadPage(final int pageIndex) {
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                AlbumsActivity.this.imagePages = ImageUtils.getImagePages(webIndex, pageIndex * 10 + 1, pageIndex * 10 + 10, isFavorite);
                currentPageIndex = pageIndex;
                sendEmptyMessage(MSG_GET_IMAGE_TITLE_LIST);
                //preload next page
                ImageUtils.getImagePages(webIndex, pageIndex * 10 + 11, pageIndex * 10 + 20, isFavorite);
            }
        }).start();
        if (!isFavorite) {
            if (webIndex == WEB_INDEX_1) {
                getSharedPref().edit().putInt("pageIndex1", pageIndex).commit();
            } else if (webIndex == WEB_INDEX_2) {
                getSharedPref().edit().putInt("pageIndex2", pageIndex).commit();
            } else if (webIndex == WEB_INDEX_4) {
                getSharedPref().edit().putInt("pageIndex4", pageIndex).commit();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPage(currentPageIndex);
    }

    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case MSG_GET_IMAGE_TITLE_LIST:
                albumAdapter.setTitles(imagePages);
                hideProgressDialog();
                break;
        }
    }
}
