package com.james.imagereader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class ZipActivity extends BaseActivity {
    private final ArrayList<String> imageList = new ArrayList<>();
    private RecyclerView rv_image;
    private int screenWidth;
    private LinearLayoutManager layoutManager;
    private TextView tv_progress;
    private ImageView iv_cover;
    private int imageCount;
    private int progress;
    private int offset;
    private final DecimalFormat decimalFormat = new DecimalFormat("#0.00");
    private final NativeApi mNativeApi = new NativeApi();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);
        String zipFilePath = UriUtils.getFileAbsolutePath(this, getIntent().getData());
        //String zipFolderName = new File(zipFilePath).getName().replace(".zip", "");
        File cacheFolder = new File(imageReaderPath + File.separator + "cache");
        //Utils.unZip(zipFilePath, cacheFolder.getAbsolutePath());
        UriUtils.unzip(zipFilePath, cacheFolder.getAbsolutePath(), mNativeApi.getPassword());
        File[] imageFiles = cacheFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jpg") || name.endsWith(".webp") || name.endsWith(".png");
            }
        });
        for (File imageFile : imageFiles) {
            imageList.add(imageFile.getAbsolutePath());
        }

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
        // progress
        tv_progress = findViewById(R.id.tv_progress);
        imageCount = imageList.size();
        layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rv_image.setLayoutManager(layoutManager);
        rv_image.setAdapter(new ImageAdapter());
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        layoutManager.scrollToPositionWithOffset(progress, offset);
        rv_image.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
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
        int mProgress = progress + 1;
        String percent = decimalFormat.format((mProgress * 100 / (float) imageCount));
        tv_progress.setText(String.valueOf("P" + mProgress + "/" + imageCount + " " + percent + "%"));
    }

    class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder> {
        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ImageViewHolder(View.inflate(mContext, R.layout.item_list_image, null));
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, final int position) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            Rect rect = new Rect(0, 0, 100, 100);
            options.inSampleSize = 1;
            Bitmap mBitmap = BitmapFactory.decodeFile(imageList.get(position), options);
            int bWidth = options.outWidth;
            int bHeight = options.outHeight;
            int iWidth = screenWidth;
            int iHeight = iWidth * bHeight / bWidth;
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(iWidth, iHeight);
            holder.iv_photo.setLayoutParams(layoutParams);
            holder.iv_photo.setImageBitmap(mBitmap);
        }

        @Override
        public int getItemCount() {
            return imageCount;
        }
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        AssetsImageView iv_photo;
        TextView tv_name;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_photo = itemView.findViewById(R.id.iv_photo);
            tv_name = itemView.findViewById(R.id.tv_name);
        }
    }
}
