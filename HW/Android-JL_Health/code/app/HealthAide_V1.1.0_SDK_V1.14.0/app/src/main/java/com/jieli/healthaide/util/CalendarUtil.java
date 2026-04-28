package com.jieli.healthaide.util;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarUtil {

    public static  final  long DAY_TIME = 24*60*60*1000;
    // 判断是否为闰年
    public static boolean isLeapYear(int year) {
        if (year % 100 == 0 && year % 400 == 0) {
            return true;
        } else if (year % 100 != 0 && year % 4 == 0) {
            return true;
        }
        return false;
    }

    //得到某月有多少天数
    public static int getDaysOfMonth(int year, int month) {

        return getDaysOfMonth(isLeapYear(year),month);
    }
    //得到某月有多少天数
    public static int getDaysOfMonth(boolean isLeapyear, int month) {
        int daysOfMonth = 0;
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                daysOfMonth = 31;
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                daysOfMonth = 30;
                break;
            case 2:
                if (isLeapyear) {
                    daysOfMonth = 29;
                } else {
                    daysOfMonth = 28;
                }
        }
        return daysOfMonth;
    }

    //指定某年中的某月的第一天是星期几
    public static int getWeekdayOfMonth(int year, int month) {
        int dayOfWeek = 0;
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, 1);
        dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayOfWeek == 0) {
            dayOfWeek = 7;
        }
        return dayOfWeek;
    }

    public static int getTodayWeek() {
        int week = 0;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        week = cal.get(Calendar.WEEK_OF_YEAR);
        return week;
    }


    public static String formatSeconds(long seconds) {

        return formatString("%02d:%02d:%02d", seconds/3600, seconds/60%60,  seconds%60);
    }




    /**
     * 去掉时间部分取0
     *
     * @param time
     * @return
     */
    public static long removeTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    @SuppressLint("SimpleDateFormat")
    public static SimpleDateFormat serverDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
    }

    public static String formatString(String format, Object... args){
        return String.format(Locale.ENGLISH, format, args);
    }

}