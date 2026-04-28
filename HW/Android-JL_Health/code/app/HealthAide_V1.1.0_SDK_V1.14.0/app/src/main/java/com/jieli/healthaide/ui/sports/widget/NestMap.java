package com.jieli.healthaide.ui.sports.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.MapView;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/12/21
 * @desc :
 */
public class NestMap extends MapView {
    public NestMap(Context context) {
        super(context);
    }

    public NestMap(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public NestMap(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public NestMap(Context context, AMapOptions aMapOptions) {
        super(context, aMapOptions);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.onInterceptTouchEvent(ev);
    }
}
