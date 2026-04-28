package com.jieli.healthaide.ui.device.watch;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.jieli.component.utils.ValueUtil;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc
 * @since 2021/4/6
 */
public class CustomPagerTransformer implements ViewPager2.PageTransformer {
    private final int maxTranslateOffsetX;
    private final ViewPager2 viewPager2;

    public CustomPagerTransformer(ViewPager2 viewPager2) {
        if(null == viewPager2){
            throw new NullPointerException("viewPager2 can not be null.");
        }
        this.viewPager2 = viewPager2;
        maxTranslateOffsetX = ValueUtil.dp2px(viewPager2.getContext(), 180);
    }

    @Override
    public void transformPage(@NonNull View page, float position) {

        int leftInScreen = page.getLeft() - viewPager2.getScrollX();
        int centerXInViewPager = leftInScreen + page.getMeasuredWidth() / 2;
        int offsetX = centerXInViewPager - viewPager2.getMeasuredWidth() / 2;
        float offsetRate = (float) offsetX * 0.38f / viewPager2.getMeasuredWidth();
        float scaleFactor = 1 - Math.abs(offsetRate);
        if (scaleFactor > 0) {
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);
            page.setTranslationX(-maxTranslateOffsetX * offsetRate);
        }
    }
}
