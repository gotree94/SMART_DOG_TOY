package com.jieli.healthaide.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: CustomTimeFormatUtil
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/3/19 14:44
 */
public class CustomTimeFormatUtil {
    private static String[] monthArrayShort = new String[]{
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"
    };
    private static String[] monthArrayFull = new String[]{
            "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
    };

    public static boolean isLocaleChinese() {
        if (Locale.getDefault().getLanguage().equalsIgnoreCase("zh")
                && Locale.getDefault().getCountry().equalsIgnoreCase("cn")) {
            return true;
        }
        return false;
    }

    /**
     * 获取月份的简写
     *
     * @param month 范围（1~12）
     * @return 1月 （中文） Jan（英文）
     */
    @SuppressLint("DefaultLocale")
    public static String getMonthShort(int month) {
        String monthShort = null;
        if (1 <= month && month <= 12) {
            if (isLocaleChinese()) {
                monthShort = 1 + "月";
            } else {
                monthShort = monthArrayShort[month - 1];
            }
        }
        return monthShort;
    }

    /**
     * 获取月份的全称
     *
     * @param month 范围（1~12）
     * @return 1月 （中文） January（英文）
     */
    @SuppressLint("DefaultLocale")
    public static String getMonthFull(int month) {
        String monthShort = null;
        if (1 <= month && month <= 12) {
            if (isLocaleChinese()) {
                monthShort = 1 + "月";
            } else {
                monthShort = monthArrayFull[month - 1];
            }
        }
        return monthShort;
    }

    /**
     * 根据本地语言显示 一个月中的一天 例如：1日（中文） 1st（英语）
     */
    public static String getMonthDayByLocale(int monthDay) {
        String monthDayStr;
        if (isLocaleChinese()) {
            monthDayStr = CalendarUtil.formatString("%d日", monthDay);
        } else {
            if (monthDay % 10 == 1) {
                monthDayStr = CalendarUtil.formatString("%dst", monthDay);
            } else if (monthDay % 10 == 2) {
                monthDayStr = CalendarUtil.formatString("%dnd", monthDay);
            } else {
                monthDayStr = CalendarUtil.formatString("%dth", monthDay);
            }
        }
        return monthDayStr;
    }

    /**
     * 根据本地语言显示 一年中的某一个月 例如：1月（中文） Jan（英语）
     */
    public static String getYearMonthByLocale(int month) {
        String monthDayStr = "";
        if (isLocaleChinese()) {
            monthDayStr = CalendarUtil.formatString("%d月", month);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDate localDate = LocalDate.of(2020, month, 1);
                Month monthTemp = localDate.getMonth();
                monthDayStr = monthTemp.getDisplayName(TextStyle.SHORT, Locale.getDefault());
            }
        }
        return monthDayStr;
    }

    /**
     * @param value    1-1440(day)
     * @param timeType CalenderSelectorView.TYPE_***
     * @return
     * @description 获取某一时刻（07:09），暂只需支持天的
     */
    public static String getMoment(float value, int timeType) {
        if (timeType == CalenderSelectorView.TYPE_DAY) {
            final SimpleDateFormat mFormatDay = dateFormat("HH:mm");
            long leftMillis = TimeUnit.MINUTES.toMillis((long) (value - 481));//(8 * 60 + 1)
            return mFormatDay.format(new Date(leftMillis));
        }
        return "";
    }

    /**
     * @param value    1-48(day)
     * @param timeType CalenderSelectorView.TYPE_***
     * @return
     * @description 获取某一半小时（07:30），暂只需支持天的
     */
    public static String getHalfHourTimeInterval(float value, int timeType) {
        if (timeType == CalenderSelectorView.TYPE_DAY) {
            final SimpleDateFormat mFormatDay = dateFormat("HH:mm");
            long leftMillis = TimeUnit.MINUTES.toMillis((long) (value * 30 - 510));
            String leftTime = mFormatDay.format(new Date(leftMillis));
            long rightMillis = TimeUnit.MINUTES.toMillis((long) (value * 30 - 480));
            String rightTime = mFormatDay.format(new Date(rightMillis));
            return leftTime + "-" + rightTime;
        }
        return "";
    }

    /**
     * @param leftTimeLong 时间段的起始时间戳
     * @param value        1-24(day),1-7(week),1-31(month),1-12(year)
     * @param timeType     CalenderSelectorView.TYPE_***
     * @return
     * @description 获取时间段 ：00:00-01:00,01月05日，5月
     */
    public static String getTimeInterval(long leftTimeLong, float value, int timeType) {
        switch (timeType) {
            case CalenderSelectorView.TYPE_DAY:
                final SimpleDateFormat mFormatDay = dateFormat("HH:mm");
                long leftMillis = TimeUnit.HOURS.toMillis((long) (value - 9));
                String leftTime = mFormatDay.format(new Date(leftMillis));
                value++;
                long rightMillis = TimeUnit.HOURS.toMillis((long) (value - 9));
                String rightTime = mFormatDay.format(new Date(rightMillis));
                return leftTime + "-" + rightTime;
            case CalenderSelectorView.TYPE_WEEK:
            case CalenderSelectorView.TYPE_MONTH:
                String leftTimeWeek = null;
                if (CustomTimeFormatUtil.isLocaleChinese()) {
                    final SimpleDateFormat mFormatWeek = dateFormat("MM月dd日");
                    long leftMillisWeek = TimeUnit.DAYS.toMillis((long) (value - 1));
                    leftTimeWeek = mFormatWeek.format(new Date(leftTimeLong + leftMillisWeek));
                } else {
                    LocalDate localDate;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        final SimpleDateFormat mFormatWeek = dateFormat("YYYY-MM-dd");
                        long leftMillisWeek = TimeUnit.DAYS.toMillis((long) (value - 1));
                        leftTimeWeek = mFormatWeek.format(new Date(leftTimeLong + leftMillisWeek));
                        localDate = LocalDate.parse(leftTimeWeek);
                        Month monthEnglish = localDate.getMonth();
                        leftTimeWeek = monthEnglish.getDisplayName(TextStyle.FULL, Locale.getDefault());
                        leftTimeWeek = CalendarUtil.formatString("%s %d", leftTimeWeek, localDate.getDayOfMonth());
                    }
                }
                return leftTimeWeek;
            case CalenderSelectorView.TYPE_YEAR:
                String leftTimeYear = null;
                if (CustomTimeFormatUtil.isLocaleChinese()) {
                    leftTimeYear = CalendarUtil.formatString("%d月", (int) value);
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        LocalDate localDate = LocalDate.of(2020, (int) value, 1);
                        Month monthEnglish = localDate.getMonth();
                        leftTimeYear = monthEnglish.getDisplayName(TextStyle.FULL, Locale.getDefault());
                    }
                }
                return leftTimeYear;
        }
        return "";
    }

    /**
     * @param
     * @return
     * @description 获取最近一个月的四个周的周一和周日 demo:2/15-2/21
     */
    public static ArrayList<String> getLastMonthMondayAndSunday() {
        ArrayList<String> resultArray = new ArrayList<>();
        String lastWeek1;
        String lastWeek2;
        String lastWeek3;
        String lastWeek4;

        // 求这个日期上一周的周一、周日
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate now = LocalDate.now();
            lastWeek1 = getMondayAndSundayByLocalDate(now.minusDays(7));
            lastWeek2 = getMondayAndSundayByLocalDate(now.minusDays(14));
            lastWeek3 = getMondayAndSundayByLocalDate(now.minusDays(21));
            lastWeek4 = getMondayAndSundayByLocalDate(now.minusDays(28));
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(3, -1);
            lastWeek1 = getMondayAndSundayByLocalDateAndroidO((Calendar) calendar.clone());
            calendar.add(3, -1);
            lastWeek2 = getMondayAndSundayByLocalDateAndroidO((Calendar) calendar.clone());
            calendar.add(3, -1);
            lastWeek3 = getMondayAndSundayByLocalDateAndroidO((Calendar) calendar.clone());
            calendar.add(3, -1);
            lastWeek4 = getMondayAndSundayByLocalDateAndroidO((Calendar) calendar.clone());
        }
        resultArray.add(lastWeek4);
        resultArray.add(lastWeek3);
        resultArray.add(lastWeek2);
        resultArray.add(lastWeek1);
        return resultArray;
    }

    /**
     * @param
     * @return
     * @description 求这个日期的周一、周日
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getMondayAndSundayByLocalDate(LocalDate todayOfLastWeek) {
        LocalDate monday = todayOfLastWeek.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY)).plusDays(1);
        LocalDate sunday = todayOfLastWeek.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).minusDays(1);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd");
        return monday.format(dateTimeFormatter) + "-" + sunday.format(dateTimeFormatter);
    }

    /**
     * @param
     * @return
     * @description 求这个日期的周一、周日
     */
    public static String getMondayAndSundayByLocalDateAndroidO(Calendar calendar) {
        return getMonday(calendar) + "-" + getSunday(calendar);
    }

    private static int getMondayPlus(Calendar calendar) {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 1) {
            return -6;
        } else {
            return 2 - dayOfWeek;
        }
    }

    public static String getMonday(Calendar calendar) {
        int mondayPlus = getMondayPlus(calendar);
        Calendar calendar1 = (Calendar) calendar.clone();
        calendar1.add(Calendar.DATE, mondayPlus);
        Date monday = calendar1.getTime();
        SimpleDateFormat format = dateFormat("MM/dd");
        return format.format(monday);
    }

    public static String getSunday(Calendar calendar) {
        int mondayPlus = getMondayPlus(calendar);
        int sundayMinus = 6 + mondayPlus;
        Calendar calendar1 = (Calendar) calendar.clone();
        calendar1.add(Calendar.DATE, sundayMinus);
        Date sunday = calendar1.getTime();
        SimpleDateFormat format = dateFormat("MM/dd");
        return format.format(sunday);
    }

    /**
     * 根据时间戳获取一天的开始时间（00：00：00）
     *
     * @param
     * @return
     */
    public static long getADayStartTime(long time) {
        return getADayTime(time, 0);
    }

    /**
     * 根据时间戳获取一天的结束时间（23：59：59）
     *
     * @param
     * @return
     */
    public static long getADayEndTime(long time) {
        return getADayTime(time, 1);
    }

    private static long getADayTime(long time, int type) {
        Date date = new Date(time);
        long resultTime;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final SimpleDateFormat mFormatWeek = dateFormat("YYYY-MM-dd");
            String timeString = mFormatWeek.format(date);
            LocalDate localDate = LocalDate.parse(timeString);
            LocalDateTime localDateTime;
            if (type == 0) {
                localDateTime = localDate.atTime(0, 0, 0);
            } else {
                localDateTime = localDate.atTime(23, 59, 59);
            }
            resultTime = localDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        } else {
            date.setHours(0);
            date.setMinutes(0);
            date.setSeconds(0);
            resultTime = date.getTime();
        }
        resultTime = (resultTime / 1000) * 1000;
        return resultTime;
    }

    /**
     * 根据时间值获取 *小时*分 /  *分
     *
     * @param time 毫秒 /不是真实时间戳
     * @return
     */
    @SuppressLint({"StringFormatMatches"})
    public static String getFormatTime(long time, Context context) {
        String ret;
        int min = (int) (time / (60 * 1000));
        int hour = min / 60;
        min = min % 60;
        if (hour > 0) {
            ret = context.getResources().getString(R.string.time_duration_type1, hour, min);
        } else {
            ret = context.getResources().getString(R.string.time_duration_type2, min);
        }
        return ret;
    }

    /**
     * 根据时间戳获取 *小时*分 /  *分
     *
     * @param timeStamp 时间戳
     * @return
     */
    @SuppressLint("StringFormatMatches")
    public static String getFormatTimeByTimeStamp(long timeStamp) {
        Date date = new Date(timeStamp);
        @SuppressLint("SimpleDateFormat")
        final SimpleDateFormat mFormatWeek = new SimpleDateFormat("HH:mm");
        return mFormatWeek.format(date);
    }

    /**
     * 将日期格式设置为英文
     */
    public static SimpleDateFormat dateFormat(String pattern) {
        return new SimpleDateFormat(pattern, Locale.ENGLISH);
    }
}
