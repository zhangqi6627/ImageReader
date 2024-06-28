package com.james.imagereader;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.content.res.Resources;
import android.media.ImageReader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.james.imagereader.R;

import java.util.List;

public class AlbumsActivity extends BaseActivity {
    private final static String TAG = "AlbumsActivity";
    private final Context mContext = AlbumsActivity.this;
    private RecyclerView rv_albums;
    private AlbumsAdapter albumsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);
        rv_albums = findViewById(R.id.rv_albums);
        albumsAdapter = new AlbumsAdapter(mContext, getInstalledPackages());
        rv_albums.setAdapter(albumsAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rv_albums.setLayoutManager(layoutManager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        albumsAdapter.updateAlbums(getInstalledPackages());
        rv_albums.setAdapter(albumsAdapter);
    }

    class AlbumsAdapter extends RecyclerView.Adapter<AlbumsHolder> {
        private List<String> mAlbums;
        private final Context mContext;
        public AlbumsAdapter(Context context, List<String> albums) {
            mContext = context;
            mAlbums = albums;
        }
        public void updateAlbums(List<String> albums) {
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
            holder.ll_content.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.e(TAG, "packageName : " + packageName);
                    uninstall(packageName);
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mAlbums.size();
        }
    }
    private void uninstall(String packageName) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent sender = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        PackageInstaller mPackageInstaller = getPackageManager().getPackageInstaller();
        mPackageInstaller.uninstall(packageName, sender.getIntentSender());// 卸载APK
    }

    static class AlbumsHolder extends RecyclerView.ViewHolder {
        LinearLayout ll_content;
        ImageView iv_album;
        TextView tv_title;
        TextView tv_progress;
        public AlbumsHolder(@NonNull View itemView) {
            super(itemView);
            ll_content = itemView.findViewById(R.id.ll_content);
            iv_album = itemView.findViewById(R.id.iv_album);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_progress = itemView.findViewById(R.id.tv_progress);
        }
    }
    private String getAssetString(String packageName, String idName) {
        Resources mResources = loadPackageResource(packageName);
        int strId = mResources.getIdentifier(idName, "string", packageName);
        return mResources.getString(strId);
    }
}