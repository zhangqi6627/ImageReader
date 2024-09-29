package com.james.imagereader;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.BitmapCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

/**
 * TODO: 沉浸式
 *  1.子线程加载assets
 *  2.多加几种图片格式
 *  #3.把 progress 和 offset 保存到数据库
 *  4.如何判断图片是否完整？
 *  5.滚动手法能否做的更好一些
 *  6.优化滑动速度
 *  7.左滑右滑上一章/下一章
 */
public class ImagesActivity extends BaseActivity {
    private final ArrayList<String> imageList = new ArrayList<>();
    private RecyclerView rv_image;
    private int screenWidth;
    private LinearLayoutManager layoutManager;
    private AssetManager pluginAsset;
    private Resources pluginResources;
    private String packageName;
    private RelativeLayout rl_toolbar;
    private TextView tv_progress;
    private ImageView iv_cover;
    private CheckBox cb_fav;
    private int imageCount;
    private int albumIndex;
    private int progress;
    private int offset;
    private AssetInfo assetInfo;
    private long packageSize;
    private final DecimalFormat decimalFormat = new DecimalFormat("#0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        packageName = getIntent().getStringExtra("packageName");
        assetInfo = getDBHelper().getAssetInfo(packageName);
        albumIndex = getIntent().getIntExtra("albumIndex", 0);
        String apkPath = assetInfo.getDisplayName();

        setContentView(R.layout.activity_images);
        rl_toolbar = findViewById(R.id.rl_toolbar);
        rv_image = findViewById(R.id.rv_image);
        iv_cover = findViewById(R.id.iv_cover);
        iv_cover.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                iv_cover.setVisibility(View.GONE);
                rv_image.setVisibility(View.VISIBLE);
                return true;
            }
        });
        // 卸载
        findViewById(R.id.btn_uninstall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uninstall(packageName);
            }
        });
        assetInfo.setPackageSize(new File(apkPath).length());
        // favorite
        cb_fav = findViewById(R.id.cb_fav);
        cb_fav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                assetInfo.setFavorite(isChecked);
            }
        });
        cb_fav.setChecked(assetInfo.isFavorite());
        // progress
        tv_progress = findViewById(R.id.tv_progress);
        pluginAsset = getPluginAssets(apkPath);
        try {
            String[] imageFiles = pluginAsset.list("imgs");
            assert imageFiles != null;
            for (String imageFile : imageFiles) {
                if (imageFile.endsWith(".jpg") || imageFile.endsWith(".webp")) {
                    imageList.add(imageFile);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String appName = getAssetString(apkPath, "app_name");
        imageCount = imageList.size();
        assetInfo.setImageCount(imageCount);
        layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rv_image.setLayoutManager(layoutManager);
        rv_image.setAdapter(new ImageAdapter());
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        progress = assetInfo.getProgress();
        offset = assetInfo.getOffset();
        layoutManager.scrollToPositionWithOffset(progress, offset);
        rv_image.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    mHandler.removeMessages(0);
                    countTimer = 0;
                }
                updateProgress();
            }
        });
        rv_image.post(new Runnable() {
            @Override
            public void run() {
                updateProgress();
            }
        });
    }

    private void updateProgress() {
        progress = layoutManager.findLastVisibleItemPosition();
        View lastView = layoutManager.findViewByPosition(progress);
        if (lastView != null) {
            offset = lastView.getTop();
        }
        assetInfo.setProgress(progress);
        assetInfo.setOffset(offset);
        int mProgress = progress + 1;
        String percent = decimalFormat.format((mProgress * 100 / (float) imageCount));
        tv_progress.setText(String.valueOf("P" + mProgress + "/" + imageCount + " " + percent + "%"));
    }

    @Override
    public void onBackPressed() {
        getDBHelper().updateAssetInfo(assetInfo);
        Intent intent = new Intent();
        intent.putExtra("albumIndex", albumIndex);
        setResult(Activity.RESULT_OK, intent);
        super.onBackPressed();
    }

    private int countTimer = 0;
    private boolean isRunning = true;

    private void startScroll() {
        isRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    SystemClock.sleep(8);
                    if (countTimer > 250) {
                        mHandler.sendEmptyMessage(0);
                    }
                    countTimer++;
                }
            }
        }).start();
    }

    private void stopScroll() {
        isRunning = false;
        countTimer = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startScroll();
    }

    @Override
    protected void onPause() {
        rv_image.setVisibility(View.INVISIBLE);
        iv_cover.setVisibility(View.VISIBLE);
        super.onPause();
        stopScroll();
    }

    @Override
    protected void onStop() {
        rv_image.setVisibility(View.INVISIBLE);
        iv_cover.setVisibility(View.VISIBLE);
        super.onStop();
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            rv_image.scrollBy(scrollSpeed, scrollSpeed);
        }
    };
    private int scrollSpeed = 0;

    class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder> {
        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ImageViewHolder(View.inflate(mContext, R.layout.item_list_image, null));
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, final int position) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            InputStream imageStream = null;
            Rect rect = new Rect(0, 0, 100, 100);
            options.inSampleSize = 1;
            try {
                imageStream = pluginAsset.open("imgs/" + imageList.get(position));
                Bitmap mBitmap = BitmapFactory.decodeStream(imageStream, rect, options);
                int bWidth = options.outWidth;
                int bHeight = options.outHeight;
                int iWidth = screenWidth;
                int iHeight = iWidth * bHeight / bWidth;
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(iWidth, iHeight);
                holder.iv_photo.setLayoutParams(layoutParams);
                holder.iv_photo.setImageBitmap(mBitmap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (imageStream != null) {
                    try {
                        imageStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return imageCount;
        }
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        AssetsImageView iv_photo;
        TextView tv_name;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_photo = itemView.findViewById(R.id.iv_photo);
            iv_photo.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    /*
                     * TODO:
                     *  #1.卸载
                     *  2.保存
                     *  3.goto
                     *  4.分享
                     */
                    uninstall(packageName);
                    return true;
                }
            });
            iv_photo.setOnActionListener(new AssetsImageView.OnActionListener() {
                @Override
                public boolean onSingleClick(MotionEvent motionEvent) {
                    if (rl_toolbar.getVisibility() == View.VISIBLE) {
                        rl_toolbar.setVisibility(View.GONE);
                    } else {
                        rl_toolbar.setVisibility(View.VISIBLE);
                    }
                    return true;
                }

                @Override
                public boolean onDoubleClick(MotionEvent motionEvent) {
                    if (cb_fav != null) {
                        boolean newChecked = !cb_fav.isChecked();
                        cb_fav.setChecked(newChecked);
                        if (newChecked) {
                            showToast("收藏成功！");
                        } else {
                            showToast("取消收藏！");
                        }
                    }
                    return true;
                }

                @Override
                public boolean onSwipeLeft() {
                    Log.e("zq8888", "onSwipeLeft()");
                    return true;
                }

                @Override
                public boolean onSwipeRight() {
                    Log.e("zq8888", "onSwipeRight()");
                    return true;
                }
            });
            tv_name = itemView.findViewById(R.id.tv_name);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("zq8888", "onActivityResult(1)" + requestCode + " result:" + resultCode + " data:" + data);
        if (requestCode == 102) {
            if (!Utils.isAppInstalled(this, packageName)) {
                showToast("卸载完成");
                Intent intent = new Intent();
                intent.putExtra("albumIndex", albumIndex);
                intent.putExtra("uninstalled", true);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
    }
}
