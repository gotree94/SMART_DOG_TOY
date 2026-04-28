package com.jieli.watchtesttool.data.bean;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/11/4
 * @desc :
 */
public class CalendarUtil {
    @SuppressLint("SimpleDateFormat")
    public static SimpleDateFormat serverDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

}
