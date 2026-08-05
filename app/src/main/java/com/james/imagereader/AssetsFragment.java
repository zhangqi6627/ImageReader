package com.james.imagereader;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AssetsFragment extends Fragment {
    private final static String TAG = "BaseFragment";
    private RecyclerView rv_albums;
    private AlbumsAdapter albumsAdapter;
    private List<AssetInfo> assetInfos = new ArrayList<>();
    private BaseActivity mActivity;
    private LinearLayoutManager layoutManager;
    private AssetCoverLoader coverLoader;
    private String type;
    private AssetSortType sortType = AssetSortType.NAME_ASC;
    private AssetDisplayMode displayMode = AssetDisplayMode.LIST;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_layout, container, false);
        rv_albums = fragmentView.findViewById(R.id.rv_albums);
        if (getContext() instanceof BaseActivity) {
            mActivity = (BaseActivity) getContext();
        }
        coverLoader = new AssetCoverLoader();
        assert getArguments() != null;
        type = getArguments().getString("type");
        sortType = AssetSortType.fromPosition(getArguments().getInt("sortType", AssetSortType.NAME_ASC.ordinal()));
        displayMode = AssetDisplayMode.fromPosition(getArguments().getInt("displayMode", AssetDisplayMode.LIST.ordinal()));
        setupLayoutManager();
        albumsAdapter = new AlbumsAdapter();
        rv_albums.setAdapter(albumsAdapter);

        rv_albums.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                int position = layoutManager.findLastVisibleItemPosition();
                View itemView = layoutManager.findViewByPosition(position);
                if (position == RecyclerView.NO_POSITION || itemView == null) {
                    return;
                }
                int offset = itemView.getTop();
                mActivity.saveData(type + ".position", position);
                mActivity.saveData(type + ".offset", offset);
            }
        });

        reload(sortType, false);
        return fragmentView;
    }

    private void setupLayoutManager() {
        if (displayMode == AssetDisplayMode.GRID) {
            layoutManager = new GridLayoutManager(mActivity, 2);
        } else {
            layoutManager = new LinearLayoutManager(mActivity);
            rv_albums.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL));
        }
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rv_albums.setLayoutManager(layoutManager);
    }

    public void reload(AssetSortType sortType) {
        reload(sortType, true);
    }

    private void reload(AssetSortType sortType, final boolean scrollToTop) {
        this.sortType = sortType;
        new Thread(new Runnable() {
            @Override
            public void run() {
                assetInfos = AssetsProvider.getInstance(mActivity).getAssetsInfoFromDB(type, AssetsFragment.this.sortType);
                mHandler.sendMessage(mHandler.obtainMessage(0, scrollToTop ? 1 : 0, 0));
            }
        }).start();
    }

    private void scanAssetsInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(2000);
                long timeBegin = System.currentTimeMillis();
                AssetsProvider.getInstance(mActivity).getAssetsInfoFromStorage();
                long timeSecond = System.currentTimeMillis();
                mHandler.sendMessage(mHandler.obtainMessage(1, (int) ((timeSecond - timeBegin) / 1000), 0));
                // 扫描数据库中的记录，如果应用不存在就删除记录，并且更新RecyclerView
                //AssetsProvider.getInstance(mContext).deleteItemIfNotExist();
                AssetsProvider.getInstance(mActivity).getAssetsInfoFromDB("");
                long timeEnd = System.currentTimeMillis();
                mHandler.sendMessage(mHandler.obtainMessage(2, (int) ((timeEnd - timeSecond) / 1000), 0));
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    albumsAdapter.notifyDataSetChanged();
                    if (msg.arg1 == 1) {
                        layoutManager.scrollToPositionWithOffset(0, 0);
                    } else {
                        int position = mActivity.loadData(type + ".position");
                        int offset = mActivity.loadData(type + ".offset");
                        layoutManager.scrollToPositionWithOffset(position, offset);
                    }
                    break;
                case 1:

                    break;
                case 2:
                    break;
            }

        }
    };

    public final static int REQUEST_VIEW_IMAGE = 101;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (coverLoader != null) {
            coverLoader.shutdown();
        }
    }

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
                reload(sortType);
            }
        }
    }

    class AlbumsAdapter extends RecyclerView.Adapter<AlbumsHolder> {
        @NonNull
        @Override
        public AlbumsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_album, parent, false);
            return new AlbumsHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull AlbumsHolder holder, int position) {
            AssetInfo assetInfo = assetInfos.get(position);
            String packageName = assetInfo.getPackageName();
            String displayName = assetInfo.getDisplayName().replace(".apk", "");
            holder.tv_title.setText(new File(displayName).getName());
            holder.rootLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mIntent = new Intent(mActivity, ImagesActivity.class);
                    mIntent.putExtra("displayName", displayName);
                    mIntent.putExtra("packageName", packageName);
                    mIntent.putExtra("albumIndex", position);
                    startActivityForResult(mIntent, REQUEST_VIEW_IMAGE);
                }
            });
            holder.rootLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
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
            if (displayMode == AssetDisplayMode.GRID) {
                holder.iv_album.setVisibility(View.VISIBLE);
                coverLoader.loadCover(assetInfo.getDisplayName(), holder.iv_album);
            } else {
                holder.iv_album.setTag(null);
                holder.iv_album.setImageDrawable(null);
                holder.iv_album.setVisibility(View.GONE);
            }
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
