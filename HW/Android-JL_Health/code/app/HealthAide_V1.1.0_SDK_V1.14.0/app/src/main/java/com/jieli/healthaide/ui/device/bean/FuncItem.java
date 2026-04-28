package com.jieli.healthaide.ui.device.bean;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 功能属性
 * @since 2021/3/11
 */
public class FuncItem {

    public final static int FUNC_HEALTH = 0;
    public final static int FUNC_MUSIC = 1;
    public final static int FUNC_ALARM = 2;
    public final static int FUNC_CONTACTS = 3;
    public final static int FUNC_OTA = 4;
    public final static int FUNC_FILE = 5;
    public final static int FUNC_NFC = 6;
    public final static int FUNC_AI_CLOUD = 7;
    public final static int FUNC_MORE = 8;
    public final static int FUNC_AI_DIAL = 9;

    private String name;
    private int func;
    private int resId;

    public FuncItem(int func) {
        setFunc(func);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFunc() {
        return func;
    }

    public void setFunc(int func) {
        this.func = func;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    @Override
    public String toString() {
        return "FuncItem{" +
                "name='" + name + '\'' +
                ", func=" + func +
                ", resId=" + resId +
                '}';
    }
}
