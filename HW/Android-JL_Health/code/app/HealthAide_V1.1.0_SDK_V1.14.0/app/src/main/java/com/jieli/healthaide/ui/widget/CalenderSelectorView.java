package com.jieli.healthaide.ui.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.LayoutDirection;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.text.TextUtilsCompat;

import com.jieli.healthaide.R;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/5/21 8:34 AM
 * @desc :
 */
public class CalenderSelectorView extends ConstraintLayout {

    public static final int TYPE_DAY = 0;
    public static final int TYPE_WEEK = 1;
    public static final int TYPE_MONTH = 2;
    public static final int TYPE_YEAR = 3;
    private OnValueChangeListener listener;
    private int type = TYPE_DAY;

    private long time;

    private long leftTime;
    private long rightTime;
    private long maxTime;

    public long getLeftTime() {
        return leftTime;
    }

    public long getRightTime() {
        return rightTime;
    }

    public void setListener(OnValueChangeListener listener) {
        this.listener = listener;
    }

    public void updateTime(long time) {
        setTime(time);
        onTimeChange();
    }

    public void setType(int type) {
        this.type = type;
        setTime(this.time);
        onTimeChange();
    }

    public CalenderSelectorView(@NonNull Context context) {
        super(context);
        init();
    }

    public CalenderSelectorView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CalenderSelectorView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CalenderSelectorView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_calender_selector, this, true);
        boolean isRTL = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL;
        if (isRTL) {
            ImageView imageView = findViewById(R.id.ibt_calender_last);
            imageView.setImageResource(R.mipmap.ic_right_withe);
            ImageView imageView1 = findViewById(R.id.ibt_calender_next);
            imageView1.setImageResource(R.mipmap.ic_left_withe);
        }
        findViewById(R.id.ibt_calender_last).setOnClickListener(v -> handleEvent(-1));
        findViewById(R.id.ibt_calender_next).setOnClickListener(v -> handleEvent(1));
        setMaxTime(System.currentTimeMillis());
        setTime(System.currentTimeMillis());
    }

    public void setTime(long time) {
        this.time = time;
        calculateTime();
        TextView tvTime = findViewById(R.id.tv_time);
        if (tvTime != null) {
            tvTime.setText(getTimeTextByType());
        }
    }

    public void setMaxTime(long maxTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(maxTime);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        this.maxTime = calendar.getTimeInMillis();
        setTime(Math.min(this.time, this.maxTime));
    }

    private void calculateTime() {
        Calendar tmp = Calendar.getInstance();
        tmp.setTimeInMillis(time);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, tmp.get(Calendar.YEAR));
        c.set(Calendar.MONTH, tmp.get(Calendar.MONTH));
        c.set(Calendar.DAY_OF_MONTH, tmp.get(Calendar.DAY_OF_MONTH));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.get(Calendar.DAY_OF_MONTH);//vivo x6s需要这样，否则获取周时间会有问题。猜测是低版本的手机对setFirstDayOfWeek函数的兼容不好，需要主动调用一下calendar的complete()
        switch (type) {
            case TYPE_DAY:
                leftTime = c.getTimeInMillis();
                c.add(Calendar.DAY_OF_MONTH, 1);
                rightTime = c.getTimeInMillis();
                break;
            case TYPE_WEEK:
                c.setFirstDayOfWeek(Calendar.MONDAY);
                c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                leftTime = c.getTimeInMillis();
                c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                c.set(Calendar.HOUR_OF_DAY, 23);
                c.set(Calendar.MINUTE, 59);
                c.set(Calendar.SECOND, 59);
                rightTime = c.getTimeInMillis();
                break;
            case TYPE_MONTH:
                c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
                leftTime = c.getTimeInMillis();
                c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                c.set(Calendar.HOUR_OF_DAY, 24);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                rightTime = c.getTimeInMillis();
                break;
            case TYPE_YEAR:
                c.set(Calendar.DAY_OF_YEAR, c.getActualMinimum(Calendar.DAY_OF_YEAR));
                leftTime = c.getTimeInMillis();
                c.set(Calendar.DAY_OF_YEAR, c.getActualMaximum(Calendar.DAY_OF_YEAR));
                c.set(Calendar.HOUR_OF_DAY, 24);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                rightTime = c.getTimeInMillis();
                break;
        }
    }

    private boolean isOverRange(Calendar c) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(maxTime);
        calendar.add(Calendar.YEAR, -98);
        long minTime = calendar.getTimeInMillis();
        long curTime = c.getTimeInMillis();
//        c = Calendar.getInstance();
//        long minTime = 0;
//        long maxTime = 0;
//        c.set(minYear, minMonth, minDay);
//        minTime = c.getTimeInMillis();
//        c.set(maxYear, maxMonth, maxDay);
//        maxTime = c.getTimeInMillis();
        return !(curTime >= minTime && curTime <= maxTime);
    }

    private void handleEvent(int flag) {
        calculateTime();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        switch (type) {
            case TYPE_DAY:
                c.add(Calendar.DAY_OF_MONTH, flag);
                break;
            case TYPE_WEEK:
                c.add(Calendar.WEEK_OF_YEAR, flag);
                break;
            case TYPE_MONTH:
                c.add(Calendar.MONTH, flag);
                break;
            case TYPE_YEAR:
                c.add(Calendar.YEAR, flag);
                break;
        }
        if (isOverRange(c)) return;
        setTime(c.getTimeInMillis());
        onTimeChange();
    }

    private void onTimeChange() {
        if (listener == null) return;
        listener.onChange(type, leftTime, rightTime);
    }

    private String getTimeTextByType() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(leftTime);
        switch (type) {
            case TYPE_DAY: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return getDayText(c);
                } else {
                    return getDayTextAndroidO(c);
                }
            }
            case TYPE_WEEK:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return getWeekText(c);
                } else {
                    return getWeekTextAndroidO(c);
                }
            case TYPE_MONTH:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return getMonthText(c);
                } else {
                    return getMonthTextAndroidO(c);
                }
            case TYPE_YEAR:
                return getYearText(c);
        }
        return "";
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getDayText(Calendar c) {
        LocalDate localDate = LocalDate.parse(getAllTimeInfo(c));
        if (isLocaleChinese()) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
            String localDateBeginStr = localDate.format(dateTimeFormatter);
            DayOfWeek dayOfWeek = localDate.getDayOfWeek();
            String[] weeks = getResources().getStringArray(R.array.alarm_weeks);
            return CalendarUtil.formatString("%s %s", localDateBeginStr, weeks[dayOfWeek.getValue() - 1]);
        } else {
            DayOfWeek dayOfWeek = localDate.getDayOfWeek();
            Month month = localDate.getMonth();
            return CalendarUtil.formatString("%s %s %d,%d", dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                    , month.getDisplayName(TextStyle.FULL, Locale.getDefault()), localDate.getDayOfMonth(), localDate.getYear());
        }
    }

    private String getDayTextAndroidO(Calendar c) {
        Date date = c.getTime();
        if (isLocaleChinese()) {
            SimpleDateFormat format = CustomTimeFormatUtil.dateFormat("yyyy年MM月dd日");
            String dayText = format.format(date);
            int week = c.get(Calendar.DAY_OF_WEEK);
            String[] weeks = getResources().getStringArray(R.array.alarm_weeks);
            return CalendarUtil.formatString("%s %s", dayText, weeks[week - 1]);
        } else {
            SimpleDateFormat format = CustomTimeFormatUtil.dateFormat("dd MM,yyyy");
            String dayText = format.format(date);
            int week = c.get(Calendar.DAY_OF_WEEK);
            String[] weeks = getResources().getStringArray(R.array.alarm_weeks);
            return CalendarUtil.formatString("%s %s", weeks[week - 1], dayText);
        }
    }

    private String getAllTimeInfo(Calendar c) {
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        return CalendarUtil.formatString("%d-%02d-%02d", year, month, day);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getWeekText(Calendar c) {
        c.setTimeInMillis(leftTime);
        LocalDate localDateBegin = LocalDate.parse(getAllTimeInfo(c));
        c.setTimeInMillis(rightTime);
        LocalDate localDateEnd = LocalDate.parse(getAllTimeInfo(c));
        if (isLocaleChinese()) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
            String localDateBeginStr = localDateBegin.format(dateTimeFormatter);
            String localDateEndStr = localDateEnd.format(dateTimeFormatter);
            return CalendarUtil.formatString("%s-%s", localDateBeginStr, localDateEndStr);
        } else {
            int yearBegin = localDateBegin.getYear();
            int yearEnd = localDateEnd.getYear();
            Month monthBegin = localDateBegin.getMonth();
            Month monthEnd = localDateEnd.getMonth();
            String monthBeginStr = monthBegin.getDisplayName(TextStyle.FULL, Locale.getDefault());
            String monthEndStr = monthEnd.getDisplayName(TextStyle.FULL, Locale.getDefault());
            String time;
            if (yearBegin != yearEnd) {//年不同就代表月不同//年月不同
                time = CalendarUtil.formatString("%s %d,%d - %s %d,%d", monthBeginStr, localDateBegin.getDayOfMonth(), yearBegin, monthEndStr, localDateEnd.getDayOfMonth(), yearEnd);
            } else if (localDateBegin.getMonthValue() != localDateEnd.getMonthValue()) {//年相同，月不同
                time = CalendarUtil.formatString("%s %d - %s %d,%d", monthBeginStr, localDateBegin.getDayOfMonth(), monthEndStr, localDateEnd.getDayOfMonth(), yearEnd);
            } else {
                time = CalendarUtil.formatString("%s %d-%d,%d", monthBeginStr, localDateBegin.getDayOfMonth(), localDateEnd.getDayOfMonth(), yearEnd);
            }
            return time;
        }
    }

    private String getWeekTextAndroidO(Calendar c) {
        c.setTimeInMillis(leftTime);
        Date dateStart = c.getTime();
        c.setTimeInMillis(rightTime);
        Date dateEnd = c.getTime();
        if (isLocaleChinese()) {
            SimpleDateFormat format = CustomTimeFormatUtil.dateFormat("yyyy年MM月dd日");
            String localDateBeginStr = format.format(dateStart);
            String localDateEndStr = format.format(dateEnd);
            return CalendarUtil.formatString("%s-%s", localDateBeginStr, localDateEndStr);
        } else {
            int yearBegin = dateStart.getYear();
            int yearEnd = dateEnd.getYear();
            int monthBegin = dateStart.getMonth();
            int monthEnd = dateEnd.getMonth();
            String monthBeginStr = CustomTimeFormatUtil.getMonthFull(monthBegin);
            String monthEndStr = CustomTimeFormatUtil.getMonthFull(monthEnd);
            String time;
            if (yearBegin != yearEnd) {//年不同就代表月不同//年月不同
                time = CalendarUtil.formatString("%s %d,%d - %s %d,%d", monthBeginStr, dateStart.getDay(), yearBegin, monthEndStr, dateEnd.getDay(), yearEnd);
            } else if (monthBegin != monthEnd) {//年相同，月不同
                time = CalendarUtil.formatString("%s %d - %s %d,%d", monthBeginStr, dateStart.getDay(), monthEndStr, dateEnd.getDay(), yearEnd);
            } else {
                time = CalendarUtil.formatString("%s %d-%d,%d", monthBeginStr, dateStart.getDay(), dateEnd.getDay(), yearEnd);
            }
            return time;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getMonthText(Calendar c) {
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        if (isLocaleChinese()) {
            return CalendarUtil.formatString("%d年 %d月", year, month);
        } else {
            LocalDate localDate = LocalDate.parse(getAllTimeInfo(c));
            Month monthEnglish = localDate.getMonth();
            String monthEnglishStr = monthEnglish.getDisplayName(TextStyle.FULL, Locale.getDefault());
            return CalendarUtil.formatString("%s %d", monthEnglishStr, year);
        }
    }

    private String getMonthTextAndroidO(Calendar c) {
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        if (isLocaleChinese()) {
            return CalendarUtil.formatString("%d年 %d月", year, month);
        } else {
            String monthString = CustomTimeFormatUtil.getMonthFull(month);
            return CalendarUtil.formatString("%s %d", monthString, year);
        }
    }


    private String getYearText(Calendar c) {
        int year = c.get(Calendar.YEAR);
        if (isLocaleChinese()) {
            return CalendarUtil.formatString("%d年1月-%d年12月", year, year);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                String JANUARY = Month.JANUARY.getDisplayName(TextStyle.FULL, Locale.getDefault());
                String DECEMBER = Month.DECEMBER.getDisplayName(TextStyle.FULL, Locale.getDefault());
                return CalendarUtil.formatString("%s - %s %d", JANUARY, DECEMBER, year);
            }
            return null;
        }
    }

    private boolean isLocaleChinese() {
        return Locale.getDefault().getLanguage().equalsIgnoreCase("zh")
                && Locale.getDefault().getCountry().equalsIgnoreCase("cn");
    }

    public static interface OnValueChangeListener {
        void onChange(int type, long leftTime, long rightTime);
    }
}
