package com.james.imagereader;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.view.View;

public class SortSpinner extends AppCompatSpinner {
    public SortSpinner(Context context) {
        super(context);
    }

    public SortSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SortSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setSelection(int position) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position);
        if (sameSelected && getOnItemSelectedListener() != null) {
            View selectedView = getSelectedView();
            getOnItemSelectedListener().onItemSelected(this, selectedView, position, getSelectedItemId());
        }
    }

    @Override
    public void setSelection(int position, boolean animate) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position, animate);
        if (sameSelected && getOnItemSelectedListener() != null) {
            View selectedView = getSelectedView();
            getOnItemSelectedListener().onItemSelected(this, selectedView, position, getSelectedItemId());
        }
    }
}
