package com.china.reader.imagereader.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.china.reader.imagereader.R;
import com.china.reader.imagereader.bean.ImageBean;
import com.china.reader.imagereader.common.ImageUtils;
import com.china.reader.imagereader.common.LogUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;
import java.util.List;

/**
 * 图片列表适配器
 * Created by Kee on 2018/8/4.
 */
public class ImageRecyclerAdapter extends RecyclerView.Adapter<ImageRecyclerAdapter.ImageHolder> {

    private List<ImageBean> dataList;
    private Context context;
    private LayoutInflater inflater;
    private OnRecyclerItemClickListener onRecyclerItemClickListener;

    public interface OnRecyclerItemClickListener {
        void onItemClick(View v, int position);
    }

    public ImageRecyclerAdapter(Context context, List<ImageBean> dataList) {
        this.context = context;
        this.dataList = dataList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void setDataList(List<ImageBean> dataList) {
        this.dataList = dataList;
        notifyItemInserted(getItemCount());
    }

    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageHolder imageHolder = new ImageHolder(inflater.inflate(R.layout.item_image, parent, false), onRecyclerItemClickListener);
        return imageHolder;
    }

    @Override
    public void onViewRecycled(@NonNull ImageHolder holder) {
        super.onViewRecycled(holder);
        Object objTag = holder.image.getTag();
        LogUtils.e("ImageRecyclerAdapter->onViewRecycled(0)");
        if (objTag instanceof AsyncTask) {
            ((AsyncTask) objTag).cancel(true);
            LogUtils.e("ImageRecyclerAdapter->onViewRecycled(1)");
        }
    }

    @Override
    public void onBindViewHolder(final ImageHolder holder, final int position) {
        //red:network blue:disk green:memory
        //Picasso.get().setIndicatorsEnabled(true);
        final ImageBean imageBean = dataList.get(position);
        holder.image.setImageResource(R.mipmap.top_6);
        //
        AsyncTask<ImageBean, Void, File> asyncTask = new AsyncTask<ImageBean, Void, File>() {
            @Override
            protected File doInBackground(ImageBean... imageBeans) {
                String imageUrl = imageBeans[0].getImageUrl();
                int webIndex = imageBeans[0].getWebIndex();
                int pageIndex = imageBeans[0].getPageIndex();
                String imageName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                File directory = new File(String.format(ImageUtils.DIRECTORY_PATH_FORMAT, webIndex, pageIndex));
                File imageFile = new File(directory.getAbsolutePath(), imageName);
                if (imageFile.exists()) {
                    return imageFile;
                }
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                if (directory.exists()) {
                    ImageUtils.downloadImage(imageBeans[0]);
                }
                return imageFile;
            }

            @Override
            protected void onPostExecute(File bitmapFile) {
                super.onPostExecute(bitmapFile);
                ImageView imageView = holder.image;
                final RequestCreator requestCreator = Picasso.get().load(bitmapFile);
                requestCreator.config(Bitmap.Config.RGB_565).tag(context).transform(WidthTransformation.getInstance(imageView.getContext(), imageBean, imageView.getWidth())).into(imageView);
                if (imageBean.getWidth() != 0) {
                    return;
                }
            }
        };
        holder.image.setTag(asyncTask);
        asyncTask.execute(imageBean);
        //
    }

    public void setItemClickListener(OnRecyclerItemClickListener onRecyclerItemClickListener) {
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    public static class ImageHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView image;
        private OnRecyclerItemClickListener itemClickListener;

        public ImageHolder(View itemView, OnRecyclerItemClickListener onRecyclerItemClickListener) {
            super(itemView);
            this.itemClickListener = onRecyclerItemClickListener;
            itemView.setOnClickListener(this);
            image = (ImageView) itemView.findViewById(R.id.image);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener == null) return;
            itemClickListener.onItemClick(itemView, getAdapterPosition());
        }
    }
}

