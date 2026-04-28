package com.jieli.healthaide.ui.mine.entries;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/10/21 9:16 AM
 * @desc :
 */
public class CommonItem {

    private int type;
    private String title;
    private CharSequence tailString;
    private boolean showNext;
    private boolean showSw;
    private int leftImg;

    private int rightImg;


    private String nextFragmentName;


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


    public void setNextFragmentName(String nextFragmentName) {
        this.nextFragmentName = nextFragmentName;
    }

    public String getNextFragmentName() {
        return nextFragmentName;
    }
}
