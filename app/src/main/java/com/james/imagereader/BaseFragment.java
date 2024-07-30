package com.james.imagereader;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class BaseFragment extends Fragment {
    private final static String TAG = "BaseFragment";
    private RecyclerView rv_albums;
    private AlbumsAdapter albumsAdapter;
    private List<AssetInfo> assetInfos = new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_layout, null);
        rv_albums = fragmentView.findViewById(R.id.rv_albums);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rv_albums.setLayoutManager(layoutManager);
        albumsAdapter = new AlbumsAdapter(getContext(), assetInfos);
        rv_albums.setAdapter(albumsAdapter);
        assert getArguments() != null;
        String type = (String) getArguments().get("type");
        new Thread(new Runnable() {
            @Override
            public void run() {
                assetInfos = AssetsProvider.getInstance(getContext()).getAssetPackagesFromDB(type);
                mHandler.sendEmptyMessage(0);
            }
        }).start();
        return fragmentView;
    }
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            albumsAdapter.updateAlbums(assetInfos);
        }
    };

    class AlbumsAdapter extends RecyclerView.Adapter<AlbumsHolder> {
        private List<AssetInfo> mAlbums;
        private final Context mContext;
        public AlbumsAdapter(Context context, List<AssetInfo> albums) {
            mContext = context;
            mAlbums = albums;
        }
        public void updateAlbums(List<AssetInfo> albums) {
            mAlbums = albums;
            notifyDataSetChanged();
        }
        @NonNull
        @Override
        public AlbumsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = View.inflate(mContext, R.layout.item_list_album, null);
            return new AlbumsHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AlbumsHolder holder, int position) {
            AssetInfo assetInfo = mAlbums.get(position);
            String packageName = assetInfo.getPackageName();
            String displayName = assetInfo.getDisplayName();
            holder.tv_title.setText(displayName);
            holder.rootLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mIntent = new Intent(mContext, ImagesActivity.class);
                    mIntent.putExtra("packageName", packageName);
                    startActivity(mIntent);
                }
            });
            holder.rootLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    /*
                     * TODO:
                     *  #1.卸载
                     *  2.收藏
                     */
                    //uninstall(packageName);
                    return true;
                }
            });
            int progress = assetInfo.getProgress();
            holder.tv_progress.setText(String.valueOf(progress + "/" + assetInfo.getImages()));
            holder.tv_size.setText(Utils.readableFileSize(assetInfo.getPackageSize()));
        }

        @Override
        public int getItemCount() {
            return mAlbums.size();
        }
    }
    static class AlbumsHolder extends RecyclerView.ViewHolder {
        View rootLayout;
        LinearLayout ll_content;
        ImageView iv_album;
        TextView tv_title;
        TextView tv_progress;
        TextView tv_size;
        public AlbumsHolder(@NonNull View itemView) {
            super(itemView);
            rootLayout = itemView;
            ll_content = itemView.findViewById(R.id.ll_content);
            iv_album = itemView.findViewById(R.id.iv_album);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_progress = itemView.findViewById(R.id.tv_progress);
            tv_size = itemView.findViewById(R.id.tv_size);
        }
    }
}
