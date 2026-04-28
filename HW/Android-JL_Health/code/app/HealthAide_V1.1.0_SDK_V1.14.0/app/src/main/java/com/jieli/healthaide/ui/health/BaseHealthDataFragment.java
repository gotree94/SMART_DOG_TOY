package com.jieli.healthaide.ui.health;

import androidx.fragment.app.Fragment;

import com.jieli.healthaide.ui.dialog.CalendarDialog;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;

import java.util.Calendar;

/**
 * @ClassName: BaseHealthDataFragment
 * @Description: 基础的健康DataFragment
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/7/9 10:35
 */
public abstract class BaseHealthDataFragment extends Fragment {
    /**
     * 日期选择器的结果回调，时间段的起始时间
     * 处理需要外面的CalendarSelectorView换算对应的时间段
     * @param month 1~12
     */
    protected abstract void onCalendarDialogChangeDate(int year, int month, int day);

    protected abstract int getTimeType();

    protected abstract Calendar getCurrentCalendar();

    public void calendarSelect() {
        int calendarType;
        switch (getTimeType()) {
            case CalenderSelectorView.TYPE_DAY:
            default:
                calendarType = CalendarDialog.CALENDAR_VIEW_TYPE_DAY;
                break;
            case CalenderSelectorView.TYPE_WEEK:
                calendarType = CalendarDialog.CALENDAR_VIEW_TYPE_WEEK;
                break;
            case CalenderSelectorView.TYPE_MONTH:
                calendarType = CalendarDialog.CALENDAR_VIEW_TYPE_MONTH;
                break;
            case CalenderSelectorView.TYPE_YEAR:
                calendarType = CalendarDialog.CALENDAR_VIEW_TYPE_YEAR;
                break;
        }
        Calendar calendar = getCurrentCalendar();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        CalendarDialog
                dialog = new CalendarDialog(year, month, day, calendarType, (resultYear, resultMonth, resultDay) -> {
            onCalendarDialogChangeDate(resultYear, resultMonth, resultDay);
        });
        dialog.show(getFragmentManager(), dialog.getClass().getCanonicalName());
    }
}
