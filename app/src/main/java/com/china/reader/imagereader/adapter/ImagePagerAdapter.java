package com.china.reader.imagereader.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.china.reader.imagereader.R;
import com.china.reader.imagereader.bean.ImageBean;
import com.china.reader.imagereader.common.ImageUtils;

import java.util.List;

public class ImagePagerAdapter extends PagerAdapter {
    private Context mContext;
    private List<ImageBean> imageBeans;

    public ImagePagerAdapter(Context context, List<ImageBean> imageBeans) {
        this.mContext = context;
        this.imageBeans = imageBeans;
    }

    @Override
    public int getCount() {
        return imageBeans.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_pager_image, null);
        ImageView imageView = itemView.findViewById(R.id.imageView);
        TextView textView = itemView.findViewById(R.id.textView);
        ImageUtils.downShow(imageBeans.get(position), imageView);
        textView.setText(String.format("(%02d/%02d)", (position + 1), imageBeans.size()));
        container.addView(itemView);
        return itemView;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return super.getPageTitle(position);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        //super.destroyItem(container, position, object);
    }
}
