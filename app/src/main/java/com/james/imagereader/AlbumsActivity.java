package com.james.imagereader;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.james.imagereader.R;

import java.util.List;

public class AlbumsActivity extends BaseActivity {
    private final Context mContext = AlbumsActivity.this;
    private RecyclerView rv_albums;
    private AlbumsAdapter albumsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);
        TextView tv_title = getWindow().getDecorView().findViewById(android.R.id.title);
        tv_title.setText("aaa");
        setTitle("hall");
        rv_albums = findViewById(R.id.rv_albums);
        albumsAdapter = new AlbumsAdapter(mContext, getInstalledPackages());
        rv_albums.setAdapter(albumsAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rv_albums.setLayoutManager(layoutManager);
    }

    class AlbumsAdapter extends RecyclerView.Adapter<AlbumsHolder> {
        private final List<String> mAlbums;
        private final Context mContext;
        public AlbumsAdapter(Context context, List<String> albums) {
            mContext = context;
            mAlbums = albums;
        }
        @NonNull
        @Override
        public AlbumsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = View.inflate(mContext, R.layout.item_list_album, null);
            return new AlbumsHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AlbumsHolder holder, int position) {
            String packageName = mAlbums.get(position);
            String app_name = getAssetString(packageName, "app_name");
            holder.tv_title.setText(app_name);
            holder.tv_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mIntent = new Intent(mContext, ImagesActivity.class);
                    mIntent.putExtra("packageName", packageName);
                    startActivity(mIntent);
                }
            });
            int progress = loadData(packageName);
            holder.tv_progress.setText(""+progress);
        }

        @Override
        public int getItemCount() {
            return mAlbums.size();
        }
    }

    static class AlbumsHolder extends RecyclerView.ViewHolder {
        ImageView iv_album;
        TextView tv_title;
        TextView tv_progress;
        public AlbumsHolder(@NonNull View itemView) {
            super(itemView);
            iv_album = itemView.findViewById(R.id.iv_album);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_progress = itemView.findViewById(R.id.tv_progress);
        }
    }
    private String getAssetString(String packageName, String strName) {
        Resources mResources = loadPackageResource(packageName);
        int strId = mResources.getIdentifier(strName, "string", packageName);
        return mResources.getString(strId);
    }
}