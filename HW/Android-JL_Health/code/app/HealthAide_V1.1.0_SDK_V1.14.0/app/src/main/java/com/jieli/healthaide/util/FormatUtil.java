package com.jieli.healthaide.util;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 格式化工具类
 *
 * @author zqjasonZhong
 * @since 2021/3/3
 */
public class FormatUtil {

    /**
     * 检测是否手机号码
     *
     * @param phoneNumber 手机号码
     * @return 结果
     */
    public static boolean checkPhoneNumber(String phoneNumber) {
        if (null == phoneNumber || phoneNumber.length() != 11) return false;
        Pattern p = Pattern.compile("^1[3-9]\\d{9}$");
        Matcher m = p.matcher(phoneNumber);
        return m.matches();
    }

    /**
     * 检测是否邮箱地址
     *
     * @param emailAddress 邮箱地址
     * @return 结果
     */
    public static boolean checkEmailAddress(String emailAddress) {
        if (null == emailAddress || emailAddress.isEmpty()) return false;
        Pattern p = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
        Matcher m = p.matcher(emailAddress);
        return m.matches();
    }

    /**
     * 格式化时间：yyyy-MM-dd HH:mm:ss
     *
     * @param time 时间戳
     * @return 时间文本
     */
    public static String formatterTime(long time) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        return formater.format(new Date(time));
    }

    /**
     * 检测密码是否符合格式
     *
     * @param password 密码
     * @return 结果
     */
    public static boolean checkPassword(String password) {
        if (null == password) return false;
        String regex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,12}$";
        return password.matches(regex);
    }

    /**
     * 检测验证码是否符合格式
     *
     * @param code 验证码
     * @return 结果
     */
    public static boolean checkSmsCode(String code) {
        if (null == code) return false;
        return code.length() == 6 && TextUtils.isDigitsOnly(code);
    }
    /**
     * 检测邮箱验证码是否符合格式
     *
     * @param code 验证码
     * @return 结果
     */
    public static boolean checkEmailIdentifyCode(String code) {
        if (null == code) return false;
        return code.length() == 8 && TextUtils.isDigitsOnly(code);
    }

    public static String paceFormat(long seconds){
        int min = (int) (seconds/60);
        int sec = (int) (seconds%60);
        return String.format(Locale.ENGLISH, "%02d'%02d\"",min,sec);
    }
}
