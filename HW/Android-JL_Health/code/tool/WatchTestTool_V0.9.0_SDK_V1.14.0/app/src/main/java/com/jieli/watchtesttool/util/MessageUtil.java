package com.jieli.watchtesttool.util;

import android.content.Context;

import com.jieli.watchtesttool.R;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 信息工具类
 * @since 2023/1/31
 */
public class MessageUtil {
    public static final String PACKAGE_NAME_SYS_MESSAGE = "com.android.mms";
    public static final String PACKAGE_NAME_WECHAT = "com.tencent.mm";
    public static final String PACKAGE_NAME_QQ = "com.tencent.mobileqq";
    public static final String PACKAGE_NAME_DING_DING = "com.alibaba.android.rimet";

    /**
     * 默认标识
     */
    public static final int FLAG_DEFAULT = 0;
    /**
     * 短信标识
     */
    public static final int FLAG_SYS_MESSAGE = 1;
    /**
     * 微信标识
     */
    public static final int FLAG_WECHAT = 2;
    /**
     * QQ标识
     */
    public static final int FLAG_QQ = 3;
    /**
     * 钉钉标识
     */
    public static final int FLAG_DING_DING = 4;

    /**
     * 获取包名
     *
     * @param flag 标识
     * @return 包名
     */
    public static String getPackageName(int flag) {
        String packageName = "";
        switch (flag) {
            case FLAG_SYS_MESSAGE:
                packageName = PACKAGE_NAME_SYS_MESSAGE;
                break;
            case FLAG_WECHAT:
                packageName = PACKAGE_NAME_WECHAT;
                break;
            case FLAG_QQ:
                packageName = PACKAGE_NAME_QQ;
                break;
            case FLAG_DING_DING:
                packageName = PACKAGE_NAME_DING_DING;
                break;
        }
        return packageName;
    }

    /**
     * 获取APP名称
     *
     * @param context 上下文
     * @param flag    标识
     * @return APP名称
     */
    public static String getAppName(Context context, int flag) {
        String appName;
        switch (flag) {
            case FLAG_SYS_MESSAGE:
                appName = context.getString(R.string.app_sms);
                break;
            case FLAG_WECHAT:
                appName = context.getString(R.string.app_wx);
                break;
            case FLAG_QQ:
                appName = context.getString(R.string.app_qq);
                break;
            case FLAG_DING_DING:
                appName = context.getString(R.string.app_ding_ding);
                break;
            default:
                appName = context.getString(R.string.app_default);
                break;
        }
        return appName;
    }
}
