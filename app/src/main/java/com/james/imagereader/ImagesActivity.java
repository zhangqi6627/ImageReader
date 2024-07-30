package com.james.imagereader;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * TODO: 沉浸式
 */
public class ImagesActivity extends BaseActivity {
    private ArrayList<String> imageList = new ArrayList<>();
    private RecyclerView rv_image;
    private int screenWidth;
    private ImageAdapter myAdapter;
    private AssetManager pluginAsset;
    private String assetPackage;
    private TextView tv_progress;
    private ImageView iv_cover;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);
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
        tv_progress = findViewById(R.id.tv_progress);
        assetPackage = getIntent().getStringExtra("packageName");
        pluginAsset = loadPackageResource(assetPackage).getAssets();
        try {
            String[] imageFiles = pluginAsset.list("imgs");
            assert imageFiles != null;
            for (String imageFile : imageFiles) {
                if (imageFile.endsWith(".jpg") || imageFile.endsWith(".webp")) {
                    imageList.add(imageFile);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(ImagesActivity.this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rv_image.setLayoutManager(layoutManager);
        myAdapter = new ImageAdapter(imageList);
        rv_image.setAdapter(myAdapter);
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        int lastPosition = loadData(assetPackage);
        if (lastPosition < 0) {
            lastPosition = 0;
        }
        int top = loadData(assetPackage+".top");
        layoutManager.scrollToPositionWithOffset(lastPosition, top);
        rv_image.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    mHandler.removeMessages(0);
                    countTimer = 0;
                }
                int itemPosition = layoutManager.findFirstVisibleItemPosition();
                int top = Objects.requireNonNull(layoutManager.findViewByPosition(itemPosition)).getTop();
                saveData(assetPackage, itemPosition);
                saveData(assetPackage+".top", top);
            }
        });
    }

    private int countTimer = 0;
    private boolean isRunning = true;
    private void startScroll(){
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
    private void stopScroll(){
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
            rv_image.scrollBy(scrollSpeed,scrollSpeed);

        }
    };
    private int scrollSpeed = 0;

    class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder> {
        private ArrayList<String> imageList;
        private final DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        private int itemCount = 0;
        public ImageAdapter(ArrayList<String> imageList) {
            this.imageList = imageList;
            itemCount = imageList.size();
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = View.inflate(ImagesActivity.this, R.layout.item_list_image, null);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position, @NonNull List<Object> payloads) {
            super.onBindViewHolder(holder, position, payloads);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, final int position) {
            int newPosition = position + 1;
            String percent = decimalFormat.format((newPosition * 100 / (float)itemCount));
            tv_progress.setText(String.valueOf("P" + newPosition + "/" + itemCount + " " + percent + "%"));
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
            return itemCount;
        }
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_photo;
        TextView tv_name;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_photo = itemView.findViewById(R.id.iv_photo);
            iv_photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scrollSpeed = (scrollSpeed + 5) % 25;
                }
            });
            iv_photo.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    /*
                     * TODO:
                     *  #1.卸载
                     *  2.收藏
                     *  3.goto
                     *  4.分享
                     */
                    uninstall(assetPackage);
                    return true;
                }
            });
            tv_name = itemView.findViewById(R.id.tv_name);
        }
    }
}
