package com.james.imagereader;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AssetsFragment extends Fragment {
    private final static String TAG = "BaseFragment";
    private RecyclerView rv_albums;
    private AlbumsAdapter albumsAdapter;
    private List<AssetInfo> assetInfos = new ArrayList<>();
    private BaseActivity mActivity;
    private LinearLayoutManager layoutManager;
    private String type;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_layout, null);
        rv_albums = fragmentView.findViewById(R.id.rv_albums);
        if (getContext() instanceof BaseActivity) {
            mActivity = (BaseActivity) getContext();
        }
        rv_albums.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL));
        assert getArguments() != null;
        type = getArguments().getString("type");
        layoutManager = new LinearLayoutManager(mActivity);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rv_albums.setLayoutManager(layoutManager);
        albumsAdapter = new AlbumsAdapter();
        rv_albums.setAdapter(albumsAdapter);

        rv_albums.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                int position = layoutManager.findLastVisibleItemPosition();
                int offset = layoutManager.findViewByPosition(position).getTop();
                mActivity.saveData(type + ".position", position);
                mActivity.saveData(type + ".offset", offset);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                assetInfos = AssetsProvider.getInstance(mActivity).getAssetsInfoFromDB(type);
                AssetsProvider.getInstance(mActivity).getTabTypes().put(type, assetInfos.size());
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
            albumsAdapter.notifyDataSetChanged();
            int position = mActivity.loadData(type + ".position");
            int offset = mActivity.loadData(type + ".offset");
            layoutManager.scrollToPositionWithOffset(position, offset);
        }
    };

    public final static int REQUEST_VIEW_IMAGE = 101;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_VIEW_IMAGE) {
                int albumIndex = data.getIntExtra("albumIndex", 0);
                boolean uninstalled = data.getBooleanExtra("uninstalled", false);
                if (uninstalled) {
                    albumsAdapter.notifyItemRemoved(albumIndex);
                    return;
                }
                AssetInfo assetInfo = assetInfos.get(albumIndex);
                assetInfos.set(albumIndex, mActivity.getDBHelper().getAssetInfo(assetInfo.getPackageName()));
                albumsAdapter.notifyItemChanged(albumIndex);
            }
        }
    }

    class AlbumsAdapter extends RecyclerView.Adapter<AlbumsHolder> {
        @NonNull
        @Override
        public AlbumsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AlbumsHolder(View.inflate(mActivity, R.layout.item_list_album, null));
        }

        @Override
        public void onBindViewHolder(@NonNull AlbumsHolder holder, int position) {
            AssetInfo assetInfo = assetInfos.get(position);
            String packageName = assetInfo.getPackageName();
            String displayName = assetInfo.getDisplayName();
            holder.tv_title.setText(displayName);
            holder.rootLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!Utils.isAppInstalled(mActivity, packageName)) {
                        Toast.makeText(mActivity, "App not installed or has been uninstalled.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Intent mIntent = new Intent(mActivity, ImagesActivity.class);
                    mIntent.putExtra("packageName", packageName);
                    mIntent.putExtra("albumIndex", position);
                    startActivityForResult(mIntent, REQUEST_VIEW_IMAGE);
                }
            });
            holder.rootLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mActivity.uninstall(packageName);
                    return true;
                }
            });
            int progress = assetInfo.getProgress();
            if (progress != 0) {
                progress++;
            }
            int imageCount = assetInfo.getImageCount();
            holder.tv_progress.setText(String.valueOf(progress + "/" + imageCount));
            holder.tv_size.setText(Utils.readableFileSize(assetInfo.getPackageSize()));
            holder.ll_content.setProgress(progress, imageCount);
            holder.cb_fav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    assetInfo.setFavorite(isChecked);
                    mActivity.getDBHelper().updateAssetInfo(assetInfo);
                }
            });
            holder.cb_fav.setChecked(assetInfo.isFavorite());
        }

        @Override
        public int getItemCount() {
            return assetInfos.size();
        }
    }

    static class AlbumsHolder extends RecyclerView.ViewHolder {
        View rootLayout;
        ProgressLinearLayout ll_content;
        ImageView iv_album;
        TextView tv_title;
        TextView tv_progress;
        TextView tv_size;
        CheckBox cb_fav;

        public AlbumsHolder(@NonNull View itemView) {
            super(itemView);
            rootLayout = itemView;
            ll_content = itemView.findViewById(R.id.ll_content);
            iv_album = itemView.findViewById(R.id.iv_album);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_progress = itemView.findViewById(R.id.tv_progress);
            tv_size = itemView.findViewById(R.id.tv_size);
            cb_fav = itemView.findViewById(R.id.cb_fav);
        }
    }
}
