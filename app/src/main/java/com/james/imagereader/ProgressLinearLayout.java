package com.james.imagereader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class ProgressLinearLayout extends LinearLayout {
    public ProgressLinearLayout(Context context) {
        super(context);
    }

    public ProgressLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ProgressLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ProgressLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    private int mProgress;
    private int mProgressMax;
    public void setProgress(int progress, int progressMax) {
        mProgress = progress;
        mProgressMax = progressMax;
        postInvalidate();
    }
    private Paint mPaint;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setColor(0xffD8ECE9);
            //mPaint.setColor(0xffFCD25C);
            mPaint.setColor(0xff9CC576);
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        canvas.drawRect(0,0, (float) (width * mProgress) / mProgressMax, height, mPaint);
    }
}
