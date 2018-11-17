package com.china.reader.imagereader.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.china.reader.imagereader.R;
import com.china.reader.imagereader.adapter.NovelTitleAdapter;
import com.china.reader.imagereader.common.BaseActivity;
import com.china.reader.imagereader.common.NovelUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NovelListActivity extends BaseActivity{

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private NovelTitleAdapter novelTitleAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel_list);
        ButterKnife.bind(this);

        Log.e("zhangqi8888", "NovelListActivity->onCreate(1)");

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("zhangqi8888", "NovelListActivity->onCreate(2)");
                NovelUtils.getNovelTitles("http://mayi6.top/t_%d.html", 1, 10);
            }
        }).start();
        //novelTitleAdapter = new NovelTitleAdapter(mContext);

    }
}
