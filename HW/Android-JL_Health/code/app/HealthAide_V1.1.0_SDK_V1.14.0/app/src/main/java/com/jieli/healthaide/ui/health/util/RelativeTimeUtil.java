package com.jieli.healthaide.ui.health.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;

/**
 * @ClassName: RelativeTimeUtil
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/6/3 15:57
 */
public class RelativeTimeUtil {
    /**
     * 根据时间戳获取天的时间段
     * @return 时间段：0-n
     */
    public static int getRelativeTimeOfDay(long timestamp, int space) {
        int timeQuantum = 0;
        int timeOfDay = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Instant instant = null;
            instant = Instant.ofEpochMilli(timestamp);
            ZoneId zone = ZoneId.systemDefault();
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
            LocalTime localTime = localDateTime.toLocalTime();
            int hour = localTime.getHour();
            int minute = localTime.getMinute();
            timeOfDay = hour * 60 + minute;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            timeOfDay = hour * 60 + minute;
        }
        timeQuantum = timeOfDay / space;
        return timeQuantum;
    }

    /**
     * 根据时间戳获取周的天
     * @return
     */
    public static int getRelativeDayOfWeek(long timestamp) {
        int dayOfWeek = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Instant instant = null;
            instant = Instant.ofEpochMilli(timestamp);
            ZoneId zone = ZoneId.systemDefault();
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
            LocalDate localDate = localDateTime.toLocalDate();
            dayOfWeek = localDate.getDayOfWeek().getValue();
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            if (dayOfWeek == 0) {
                dayOfWeek = 7;
            }
        }
        return dayOfWeek;
    }

    /**
     * 根据时间戳获取月的天
     */
    public static int getRelativeDayOfMonth(long timestamp) {
        int dayOfMonth = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Instant instant = null;
            instant = Instant.ofEpochMilli(timestamp);
            ZoneId zone = ZoneId.systemDefault();
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
            LocalDate localDate = localDateTime.toLocalDate();
            dayOfMonth = localDate.getDayOfMonth();
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        }
        return dayOfMonth;
    }
    /**
     * 根据时间戳获取月的天
     */
    public static int getRelativeMonthOfYear(long timestamp) {
        int monthOfYear = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Instant instant = null;
            instant = Instant.ofEpochMilli(timestamp);
            ZoneId zone = ZoneId.systemDefault();
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
            LocalDate localDate = localDateTime.toLocalDate();
            monthOfYear = localDate.getMonthValue();
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            monthOfYear = calendar.get(Calendar.MONTH);
        }
        return monthOfYear;
    }
}
