package com.example.dyang.galleryphotolist;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * This GridView is nested as a item of ListView
 * This GridView is not scrollable, so need to calculate it's high
 */
public class NestedGridView extends GridView {
    public NestedGridView(Context context) {
        super(context);

    }

    public NestedGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);  //calculate the high of the GridView
    }
}

