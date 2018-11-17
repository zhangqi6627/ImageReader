package com.china.reader.imagereader.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.china.reader.imagereader.R;
import com.china.reader.imagereader.bean.ImagePage;
import com.china.reader.imagereader.common.ImageUtils;
import com.china.reader.imagereader.common.LogUtils;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ItemViewHolder> {
    private Context mContext;
    private List<ImagePage> imagePages;
    private LayoutInflater layoutInflater;

    public AlbumAdapter(Context context, List<ImagePage> imagePages) {
        mContext = context;
        this.imagePages = imagePages;
        layoutInflater = LayoutInflater.from(mContext);
    }

    public void setTitles(List<ImagePage> imagePages) {
        this.imagePages = imagePages;
        LogUtils.e( "AlbumAdapter.setTitles:" + imagePages.size());
        notifyDataSetChanged();
    }

    private OnRecyclerItemClickListener onRecyclerItemClickListener;

    public interface OnRecyclerItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setItemClickListener(OnRecyclerItemClickListener onRecyclerItemClickListener) {
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ItemViewHolder(layoutInflater.inflate(R.layout.item_image_title, viewGroup, false), onRecyclerItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder itemViewHolder, int i) {
        final ImagePage imagePage = imagePages.get(i);
        String title = imagePage.getTitle();
        LogUtils.e("AlbumAdapter->onBindViewHolder() title:"+title);
        itemViewHolder.tv_title.setText(imagePage.getPageIndex() + " " + title);
        //itemViewHolder.tv_title.setText(imagePage.getPageIndex() + "");
        itemViewHolder.tv_title.setTag(imagePage);
        itemViewHolder.tv_title.setTextColor(imagePage.getIsViewed() ? Color.GRAY : Color.BLACK);
        //CheckBox favorites
        itemViewHolder.cb_fav.setChecked(imagePage.getIsFavorite());
        itemViewHolder.cb_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtils.e( "AlbumAdapter->onClick(1)");
                imagePage.setIsFavorite(!imagePage.getIsFavorite());
                ImageUtils.updateImagePage(imagePage);
                itemViewHolder.cb_fav.setChecked(imagePage.getIsFavorite());
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagePages.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tv_title;
        private CheckBox cb_fav;
        private AlbumAdapter.OnRecyclerItemClickListener itemClickListener;

        public ItemViewHolder(View itemView, AlbumAdapter.OnRecyclerItemClickListener onRecyclerItemClickListener) {
            super(itemView);
            this.itemClickListener = onRecyclerItemClickListener;
            itemView.setOnClickListener(this);
            tv_title = itemView.findViewById(R.id.tv_title);
            cb_fav = itemView.findViewById(R.id.cb_fav);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener == null) return;
            itemClickListener.onItemClick(itemView, getAdapterPosition());
        }
    }
}
