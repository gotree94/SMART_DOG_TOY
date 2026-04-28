package com.jieli.healthaide.ui.device.health;

import android.widget.CompoundButton;

/**
 * @ClassName: HealthOptionItem
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/7/13 11:26
 */
public class HealthOptionItem {
    private int type;
    private String title;
    private String hintText;
    private CharSequence tailString;
    private boolean showNext;
    private boolean showSw;
    private int leftImg;

    private int rightImg;
    private CompoundButton.OnCheckedChangeListener swCheckListener;

    private boolean swChecked;

    public void setSwChecked(boolean swChecked) {
        this.swChecked = swChecked;
    }

    public boolean isSwChecked() {
        return swChecked;
    }


    public void setRightImg(int rightImg) {
        this.rightImg = rightImg;
    }

    public int getRightImg() {
        return rightImg;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CharSequence getTailString() {
        return tailString;
    }

    public void setTailString(CharSequence tailString) {
        this.tailString = tailString;
    }

    public boolean isShowNext() {
        return showNext;
    }

    public void setShowNext(boolean showNext) {
        this.showNext = showNext;
    }

    public boolean isShowSw() {
        return showSw;
    }

    public void setShowSw(boolean showSw) {
        this.showSw = showSw;
    }

    public int getLeftImg() {
        return leftImg;
    }

    public void setLeftImg(int leftImg) {
        this.leftImg = leftImg;
    }

    public String getHintText() {
        return hintText;
    }

    public void setHintText(String hintText) {
        this.hintText = hintText;
    }

    public CompoundButton.OnCheckedChangeListener getSwCheckListener() {
        return swCheckListener;
    }

    public void setSwCheckListener(CompoundButton.OnCheckedChangeListener swCheckListener) {
        this.swCheckListener = swCheckListener;
    }
}
