package com.james.imagereader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class AssetsImageView extends AppCompatImageView {
    private GestureDetector gestureDetector;
    public AssetsImageView(Context context) {
        super(context);
        initGestureDetector();
    }

    public AssetsImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initGestureDetector();
    }

    public AssetsImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initGestureDetector();
    }

    private void initGestureDetector() {

        gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                Log.e("zq8888", "onDoubleTap(1)");
                if (mOnActionListener != null) {
                    return mOnActionListener.onDoubleClick(e);
                }
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                Log.e("zq8888", "onSingleTapConfirmed(1)");
                if (mOnActionListener != null) {
                    return mOnActionListener.onSingleClick(e);
                }
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                mOnActionListener.onSwipeRight();
                            } else {
                                mOnActionListener.onSwipeLeft();
                            }
                            result = true;
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }

            @Override
            public void onLongPress(@NonNull MotionEvent e) {
                super.onLongPress(e);

            }
        });
    }
    private static final int SWIPE_THRESHOLD = 80;
    private static final int SWIPE_VELOCITY_THRESHOLD = 80;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(event);
            return true;
        }
        return super.onTouchEvent(event);
    }
    interface OnActionListener {
        boolean onSingleClick(MotionEvent motionEvent);
        boolean onDoubleClick(MotionEvent motionEvent);
        boolean onSwipeLeft();
        boolean onSwipeRight();
    }
    private OnActionListener mOnActionListener;
    public void setOnActionListener(OnActionListener onActionListener) {
        mOnActionListener = onActionListener;
    }
}
