package com.jieli.healthaide.ui.login.bean;

/**
 * 用户属性
 *
 * @author zqjasonZhong
 * @since 2021/3/5
 */
public class UserItem extends SettingsItem {
    private final int type;

    public final static int USER_TYPE_NICKNAME = 1;
    public final static int USER_TYPE_SEX = 2;
    public final static int USER_TYPE_BIRTHDAY = 3;
    public final static int USER_TYPE_STATURE = 4;
    public final static int USER_TYPE_WEIGHT = 5;
    public final static int USER_TYPE_TARGET = 6;

    public UserItem(int type){
        this.type = type;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "UserItem{" +
                "type=" + type +
                "} " + super.toString();
    }
}
