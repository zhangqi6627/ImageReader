package com.china.reader.imagereader.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.china.reader.imagereader.R;

import java.util.List;

public class NovelTitleAdapter extends RecyclerView.Adapter<NovelTitleAdapter.ItemViewHolder> {

    private LayoutInflater layoutInflater;

    private List<String> novelTitles;

    private Context mContext;

    public NovelTitleAdapter(Context context, List<String> novelTitles) {
        mContext = context;
        layoutInflater = LayoutInflater.from(mContext);
        this.novelTitles = novelTitles;
    }

    @NonNull
    @Override
    public NovelTitleAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new NovelTitleAdapter.ItemViewHolder(layoutInflater.inflate(R.layout.item_novel_title, viewGroup, false), onRecyclerItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull NovelTitleAdapter.ItemViewHolder viewHolder, int i) {
        viewHolder.tv_title.setText("hello world");
        viewHolder.tv_title.setText(novelTitles.get(i));
    }

    @Override
    public int getItemCount() {
        return novelTitles.size();
    }

    interface OnRecyclerItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnRecyclerItemClickListener onRecyclerItemClickListener;

    public void setOnItemClickListener(OnRecyclerItemClickListener onRecyclerItemClickListener) {
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tv_title;
        private OnRecyclerItemClickListener itemClickListener;

        public ItemViewHolder(View itemView, OnRecyclerItemClickListener onRecyclerItemClickListener) {
            super(itemView);
            this.itemClickListener = onRecyclerItemClickListener;
            itemView.setOnClickListener(this);
            tv_title = itemView.findViewById(R.id.tv_title);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener == null) return;
            itemClickListener.onItemClick(itemView, getAdapterPosition());
        }
    }
}
