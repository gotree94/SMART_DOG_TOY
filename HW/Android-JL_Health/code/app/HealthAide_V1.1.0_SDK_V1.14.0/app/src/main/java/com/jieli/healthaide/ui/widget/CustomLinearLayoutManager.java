package com.jieli.healthaide.ui.widget;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 自定义LinearLayoutManager, 方便控制是否滑动
 * @since 2021/3/10
 */
public class CustomLinearLayoutManager extends LinearLayoutManager {
    private boolean isBanScroll = true;
    private final Context mContext;

    public CustomLinearLayoutManager(Context context) {
        super(context);
        mContext = context;
    }

    public CustomLinearLayoutManager(Context context, @RecyclerView.Orientation int orientation,
                                     boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        mContext = context;
    }

    @Override
    public boolean canScrollHorizontally() {
        if (isBanScroll) {
            return super.canScrollHorizontally();
        } else {
            return false;
        }
    }

    @Override
    public boolean canScrollVertically() {
        if (isBanScroll) {
            return super.canScrollVertically();
        } else {
            return false;
        }
    }

    public boolean isBanScroll() {
        return isBanScroll;
    }

    public void setBanScroll(boolean enable) {
        isBanScroll = enable;
    }

    public void scrollToPosition(int position) {
        TopSmoothScroller smoothScroller = new TopSmoothScroller(mContext);
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }

    public static class TopSmoothScroller extends LinearSmoothScroller {

        public TopSmoothScroller(Context context) {
            super(context);
        }

        @Override
        protected int getHorizontalSnapPreference() {
            return SNAP_TO_START;
        }

        @Override
        protected int getVerticalSnapPreference() {
            return SNAP_TO_START;
        }
    }
}
