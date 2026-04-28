package com.jieli.healthaide.ui.dialog;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder;
import com.haibin.calendarview.CalendarView;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentCalendarDialogBinding;
import com.jieli.healthaide.databinding.ItemCalendarBinding;
import com.jieli.healthaide.ui.base.BaseDialogFragment;
import com.jieli.healthaide.ui.widget.CustomDayMonthView;
import com.jieli.healthaide.ui.widget.CustomWeekBar;
import com.jieli.healthaide.ui.widget.CustomWeekMonthView;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * 日历选择器弹窗
 *
 * @author ZhangHuanMing
 * @since 2021/3/5
 */
public class CalendarDialog extends BaseDialogFragment implements CalendarView.OnCalendarRangeSelectListener, CalendarView.OnMonthChangeListener {
    public static final int CALENDAR_VIEW_TYPE_DAY = 0;
    public static final int CALENDAR_VIEW_TYPE_WEEK = 1;
    public static final int CALENDAR_VIEW_TYPE_MONTH = 2;
    public static final int CALENDAR_VIEW_TYPE_YEAR = 3;
    private final int MSG_DISMISS = 1;
    private FragmentCalendarDialogBinding binding;
    private int maxYear = Calendar.getInstance().get(Calendar.YEAR);
    private int maxMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
    private int maxDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    private int year = maxYear;
    private int month = maxMonth;
    private int day = maxDay;
    private OnDateSelected onDateSelected;
    private CalendarAdapter calendarAdapter;
    private int viewType = CALENDAR_VIEW_TYPE_DAY;
    private int firstYearPosition = 0;
    private int monthCurrentYear = year;
    private Handler mHandler = new Handler(msg -> {
        switch (msg.what) {
            case MSG_DISMISS:
                dismiss();
                break;
        }
        return true;
    });

    /**
     * @param month 1~12
     */
    public CalendarDialog(int year, int month, int day, int viewType, OnDateSelected onDateSelected) {
        Log.d(TAG, "CalendarDialog: year : " + year + " month : " + month + " day: " + day);
        if (year > 0 && month > 0 && day > 0) {
            this.year = year;
            this.month = month;
            this.day = day;
            monthCurrentYear = year;
        }
        this.viewType = viewType;
        this.onDateSelected = onDateSelected;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            Window window = getDialog().getWindow();
            if (window != null) {
                //去掉dialog默认的padding
                window.getDecorView().setPadding(0, 0, 0, 0);
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.width = Math.round(0.9f * getScreenWidth());
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.CENTER;
                //设置dialog的动画
//                lp.windowAnimations = R.style.BottomToTopAnim;
                window.setAttributes(lp);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
        View view = inflater.inflate(R.layout.fragment_calendar_dialog, container, false);
        binding = FragmentCalendarDialogBinding.bind(view);
        switch (viewType) {
            case CALENDAR_VIEW_TYPE_DAY:
            default:
                binding.calendarView.setMonthView(CustomDayMonthView.class);
                binding.calendarView.setWeekBar(CustomWeekBar.class);
                updateSelectedDayRange();
                break;
            case CALENDAR_VIEW_TYPE_WEEK:
                binding.calendarView.setWeekBar(CustomWeekBar.class);
                binding.calendarView.setMonthView(CustomWeekMonthView.class);
                Calendar sunDay = getSunDay(Calendar.getInstance());
                maxYear = sunDay.get(Calendar.YEAR);
                maxMonth = sunDay.get(Calendar.MONTH) + 1;
                maxDay = sunDay.get(Calendar.DAY_OF_MONTH);
                updateSelectedWeekRange();
                break;
            case CALENDAR_VIEW_TYPE_MONTH://
                showMonthView();
                break;
            case CALENDAR_VIEW_TYPE_YEAR://todo 显示年份
                showYearView();
                break;
        }
        binding.calendarView.setRange(maxYear - 98, 1, 1, maxYear, maxMonth, maxDay);
        initOnClick();
        updateTitle();
        return view;
    }

    private void initOnClick() {
        binding.btPrev.setOnClickListener(view -> onPrev());
        binding.btNext.setOnClickListener(view -> onNext());
        binding.calendarView.setOnMonthChangeListener(this);
        if (null != calendarAdapter) {
            calendarAdapter.setOnItemClickListener((adapter, view1, position) -> {
                CalendarData calendarData = calendarAdapter.getData().get(position);
                if (calendarData.status != 2) {
                    onDateSelected(calendarData.year, calendarData.month, day);
                    dismissDialog();
                }
            });
        }
        binding.rvMonthAndYear.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.d(TAG, "onScrollStateChanged  onScrolled: dx : " + dx + " dy :" + dy);
                if (dy != 0) {
                    firstYearPosition = recyclerView.getChildLayoutPosition(recyclerView.getChildAt(0));
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                    View itemView = recyclerView.getChildAt(0);
                    int itemHeight = itemView.getHeight();
                    if (itemView.getTop() > -(itemHeight / 2)) {
                        firstYearPosition = recyclerView.getChildLayoutPosition(recyclerView.getChildAt(0));
                    } else {
                        firstYearPosition = recyclerView.getChildLayoutPosition(recyclerView.getChildAt(3));
                    }
                    Log.d(TAG, "onScrollStateChanged: top : " + itemView.getTop());
                    Log.d(TAG, "onScrollStateChanged: firstYearPosition : " + firstYearPosition);
                    scrollPager(firstYearPosition, false);
                    updateTitle();
                }
            }
        });
    }

    private void onPrev() {
        switch (viewType) {
            case CALENDAR_VIEW_TYPE_DAY:
                binding.calendarView.scrollToPre();
                break;
            case CALENDAR_VIEW_TYPE_WEEK:
                binding.calendarView.scrollToPre();
                break;
            case CALENDAR_VIEW_TYPE_MONTH:
                monthCurrentYear--;
                calendarAdapter.setNewInstance(getMonthViewData());
                break;
            case CALENDAR_VIEW_TYPE_YEAR://todo 显示年份
                scrollPager(firstYearPosition - 12, false);
                break;
        }
        updateTitle();
    }

    private void onNext() {
        switch (viewType) {
            case CALENDAR_VIEW_TYPE_DAY:
                binding.calendarView.scrollToNext();
                break;
            case CALENDAR_VIEW_TYPE_WEEK:
                binding.calendarView.scrollToNext();
                break;
            case CALENDAR_VIEW_TYPE_MONTH:
                monthCurrentYear++;
                calendarAdapter.setNewInstance(getMonthViewData());
                break;
            case CALENDAR_VIEW_TYPE_YEAR://todo 显示年份
                scrollPager(firstYearPosition + 12, false);
                break;
        }
        updateTitle();
    }

    private void updateTitle() {
        String content = null;
        switch (viewType) {
            case CALENDAR_VIEW_TYPE_DAY:
            case CALENDAR_VIEW_TYPE_WEEK:
//                if (isLocaleChinese()) {
//                    content = String.format(Locale.getDefault(), "%d年 %d月", year, month);
//                } else {
//                    String monthString = CustomTimeFormatUtil.getMonthFull(month);
//                    content = String.format("%s %d", monthString, year);
//                }
                Calendar c = Calendar.getInstance();
//                c.setTimeInMillis(leftTime);
                c.set(year,month-1,1,0,0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    content = getMonthText(c);
                } else {
                    content = getMonthTextAndroidO(c);
                }
                break;
            case CALENDAR_VIEW_TYPE_MONTH:
                content = String.valueOf(monthCurrentYear);
                break;
            case CALENDAR_VIEW_TYPE_YEAR:
                int leftYear = maxYear - 98 + firstYearPosition;
                int rightYear = leftYear + 11;
                content = String.format("%d - %d", leftYear, rightYear);
                break;
        }
        binding.tvCurrentCalendar.setText(content);
    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getMonthText(Calendar c) {
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        if (isLocaleChinese()) {
            return String.format(Locale.getDefault(), "%d年 %d月", year, month);
        } else {
            LocalDate localDate = LocalDate.parse(getAllTimeInfo(c));
            Month monthEnglish = localDate.getMonth();
            String monthEnglishStr = monthEnglish.getDisplayName(TextStyle.FULL, Locale.getDefault());
            return String.format("%s %d", monthEnglishStr, year);
        }
    }

    @SuppressLint("DefaultLocale")
    private String getMonthTextAndroidO(Calendar c) {
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        if (isLocaleChinese()) {
            return String.format(Locale.getDefault(), "%d年 %d月", year, month);
        } else {
            String monthString = CustomTimeFormatUtil.getMonthFull(month);
            return String.format("%s %d", monthString, year);
        }
    }

    private String getAllTimeInfo(Calendar c) {
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        return String.format(Locale.ENGLISH, "%d-%02d-%02d", year, month, day);
    }

    private void updateSelectedDayRange() {
        binding.calendarView.setSelectCalendarRange(year, month, day, year, month, day);
        binding.calendarView.setOnCalendarRangeSelectListener(this);
    }

    private void updateSelectedWeekRange() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        int mondayPlus = getMondayPlus(calendar);
        Calendar calendarMonday = (Calendar) calendar.clone();
        calendarMonday.add(Calendar.DATE, mondayPlus);
        int sundayMinus = 6 + mondayPlus;
        Calendar calendarSunday = (Calendar) calendar.clone();
        calendarSunday.add(Calendar.DATE, sundayMinus);
        int minYear = calendarMonday.get(Calendar.YEAR);
        int minYearMonth = calendarMonday.get(Calendar.MONTH) + 1;
        int minYearDay = calendarMonday.get(Calendar.DAY_OF_MONTH);
        int maxYear = calendarSunday.get(Calendar.YEAR);
        int maxYearMonth = calendarSunday.get(Calendar.MONTH) + 1;
        int maxYearDay = calendarSunday.get(Calendar.DAY_OF_MONTH);
        binding.calendarView.setSelectCalendarRange(minYear, minYearMonth, minYearDay, maxYear, maxYearMonth, maxYearDay);
        binding.calendarView.setOnCalendarRangeSelectListener(this);
    }

    private int getMondayPlus(Calendar calendar) {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 1) {
            return -6;
        } else {
            return 2 - dayOfWeek;
        }
    }

    private void showMonthView() {
        showMonthOrYearView(false);
    }

    private void showYearView() {
        showMonthOrYearView(true);
        int initPosition = year - (maxYear - 98) - 5;
        scrollPager(initPosition, false);
    }

    private void showMonthOrYearView(boolean isYear) {
        binding.calendarLayout.setVisibility(View.GONE);
        binding.rvMonthAndYear.setVisibility(View.VISIBLE);
        binding.rvMonthAndYear.setLayoutManager(new GridLayoutManager(getContext(), 3));
        calendarAdapter = new CalendarAdapter();
        binding.rvMonthAndYear.setAdapter(calendarAdapter);
        ArrayList<CalendarData> dataArrayList = new ArrayList<>();
        if (isYear) {
            for (int i = 98; i >= 0; i--) {//这里大概率要做无限RecyclerView
                int data = maxYear - i;
                int status = year == data ? 1 : 0;
                String content = String.valueOf(data);
                dataArrayList.add(new CalendarData(content, status, data, month));
            }
        } else {
            dataArrayList = getMonthViewData();
        }
        calendarAdapter.setNewInstance(dataArrayList);
    }

    /**
     * 获取一年的月份数据
     */
    private ArrayList<CalendarData> getMonthViewData() {
        ArrayList<CalendarData> dataArrayList = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            int status = 0;
            if (monthCurrentYear > maxYear) {
                status = 2;
            } else if (monthCurrentYear == maxYear) {
                if (i + 1 > maxMonth) {
                    status = 2;
                }
            }
            if (monthCurrentYear == year && (i + 1) == month) {
                status = 1;
            }
            String content =
                    getContext()
                            .getResources()
                            .getStringArray(com.haibin.calendarview.R.array.month_string_array)[i];
            dataArrayList.add(new CalendarData(content, status, monthCurrentYear, i + 1));
        }
        return dataArrayList;
    }

    private void scrollPager(int position, boolean isSmooth) {
        int maxScrollPosition = 99 - 12;
        if (position < 0) {
            position = 0;
        } else if (position > maxScrollPosition) {
            position = maxScrollPosition;
        }
        firstYearPosition = position;
        if (isSmooth) {
            binding.rvMonthAndYear.smoothScrollToPosition(position);
        } else {
            binding.rvMonthAndYear.scrollToPosition(position);
        }
        GridLayoutManager mLayoutManager =
                (GridLayoutManager) binding.rvMonthAndYear.getLayoutManager();
        mLayoutManager.scrollToPositionWithOffset(position, 0);
    }

    private void onDateSelected(int year, int month, int day) {
        if (null != onDateSelected) {
            onDateSelected.onSelected(year, month, day);
        }
    }

    @Override
    public void onCalendarSelectOutOfRange(com.haibin.calendarview.Calendar calendar) {

    }

    @Override
    public void onSelectOutOfRange(com.haibin.calendarview.Calendar calendar, boolean isOutOfMinRange) {

    }

    @Override
    public void onCalendarRangeSelect(com.haibin.calendarview.Calendar calendar, boolean isEnd) {
        binding.calendarView.setOnCalendarRangeSelectListener(null);//先移除回调避免死循环
        switch (viewType) {
            case CALENDAR_VIEW_TYPE_DAY:
                year = calendar.getYear();
                month = calendar.getMonth();
                day = calendar.getDay();
                updateSelectedDayRange();
                break;
            case CALENDAR_VIEW_TYPE_WEEK:
                if (!isEnd) {
                    year = calendar.getYear();
                    month = calendar.getMonth();
                    day = calendar.getDay();
                }
                updateSelectedWeekRange();
                break;
        }
        onDateSelected(calendar.getYear(), calendar.getMonth(), calendar.getDay());
        dismissDialog();
    }

    @Override
    public void onMonthChange(int year, int month) {
        this.year = year;
        this.month = month;
        updateTitle();
    }

    private void dismissDialog() {
        mHandler.removeMessages(MSG_DISMISS);
        mHandler.sendEmptyMessageDelayed(MSG_DISMISS, 500);
    }

    /**
     * 系统语言是不是中文
     */
    private boolean isLocaleChinese() {
        if (Locale.getDefault().getLanguage().equalsIgnoreCase("zh")
                && Locale.getDefault().getCountry().equalsIgnoreCase("cn")) {
            return true;
        }
        return false;
    }

    private Calendar getSunDay(Calendar calendar) {
        int mondayPlus = getMondayPlus(calendar);
        int sundayMinus = 6 + mondayPlus;
        Calendar calendar1 = (Calendar) calendar.clone();
        calendar1.add(Calendar.DATE, sundayMinus);
        calendar1.set(Calendar.HOUR_OF_DAY, 23);
        calendar1.set(Calendar.MINUTE, 59);
        calendar1.set(Calendar.MILLISECOND, 59);
        return calendar1;
    }

    public static interface OnDateSelected {
        void onSelected(int year, int month, int day);
    }

    private class CalendarData {
        /**
         * 当前数据的状态，未选中且未超过最大范围(0)，已经选中(1)，超过最大范围(2)
         */
        int status;
        String content;
        int year;
        int month;

        CalendarData(String content, int status, int year, int month) {
            this.content = content;
            this.status = status;
            this.year = year;
            this.month = month;
        }
    }

    private class CalendarAdapter extends BaseQuickAdapter<CalendarData, BaseDataBindingHolder<ItemCalendarBinding>> {

        public CalendarAdapter() {
            super(R.layout.item_calendar);
        }

        @Override
        protected void convert(@NotNull BaseDataBindingHolder<ItemCalendarBinding> itemCalendarBindingBaseDataBindingHolder, CalendarData calendarData) {
            ItemCalendarBinding binding = itemCalendarBindingBaseDataBindingHolder.getDataBinding();
            int colorResId;
            switch (calendarData.status) {
                case 0:
                default:
                    colorResId = R.color.black_242424;
                    break;
                case 1:
                    colorResId = R.color.purple_805BEB;
                    break;
                case 2:
                    colorResId = R.color.gray_9E9E9E;
                    break;
            }
            binding.tvCalendar.setTextColor(getResources().getColor(colorResId));
            binding.tvCalendar.setText(calendarData.content);
        }
    }
}
