package com.china.reader.imagereader.adapter;

import android.content.Context;
import android.graphics.Bitmap;

import com.china.reader.imagereader.bean.ImageBean;
import com.china.reader.imagereader.common.ImageUtils;
import com.china.reader.imagereader.common.LogUtils;
import com.squareup.picasso.Transformation;

public class WidthTransformation implements Transformation {

    private Context mContext;
    private int targetWidth;
    private ImageBean imageBean;

    private WidthTransformation(Context context, ImageBean imageBean, int targetWidth) {
        this.mContext = context;
        this.targetWidth = targetWidth;
        this.imageBean = imageBean;
    }

    private static WidthTransformation widthTransformation;

    public static WidthTransformation getInstance(Context context, ImageBean imageBean, int targetWidth) {
        if (widthTransformation == null) {
            widthTransformation = new WidthTransformation(context, imageBean, targetWidth);
        }
        return widthTransformation;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        LogUtils.e("WidthTransformation->transform()->source.getHeight()=" + source.getHeight());
        LogUtils.e("WidthTransformation->transform()->source.getWidth()=" + source.getWidth());
        LogUtils.e("WidthTransformation->transform()->targetWidth=" + targetWidth);
        if (source.getWidth() == 0) {
            return source;
        }
        //按照设置的宽度比例来缩放
        double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
        int targetHeight = (int) (targetWidth * aspectRatio);
        if (targetHeight != 0 && targetWidth != 0) {
            Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
            if (result != source) {
                // Same bitmap is returned if sizes are the same
                source.recycle();
            }
            if (imageBean.getWidth() == 0) {
                imageBean.setWidth(result.getWidth());
            }
            if( imageBean.getHeight() == 0){
                imageBean.setHeight(result.getHeight());
            }
            ImageUtils.updateImageBean(imageBean);
            return result;
        } else {
            if (imageBean.getWidth() == 0) {
                imageBean.setWidth(source.getWidth());
            }
            if( imageBean.getHeight() == 0){
                imageBean.setHeight(source.getHeight());
            }
            ImageUtils.updateImageBean(imageBean);
            return source;
        }
    }

    @Override
    public String key() {
        return "WidthTransformation";
    }
}