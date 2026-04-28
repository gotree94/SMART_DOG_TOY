package com.jieli.healthaide.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.LayoutDirection;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.core.text.TextUtilsCompat;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.WeekBar;
import com.jieli.healthaide.R;

import java.util.Locale;

/**
 * @ClassName: CustomWeekBar
 * @Description: 选择日期弹窗的自定义星期栏的布局
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/3/1 11:09
 */

public class CustomWeekBar extends WeekBar {

    private int mPreSelectedIndex;

    public CustomWeekBar(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.custom_week_bar, this, true);
        setBackgroundColor(Color.WHITE);
    }

    @Override
    protected void onDateSelected(Calendar calendar, int weekStart, boolean isClick) {
        getChildAt(mPreSelectedIndex).setSelected(false);
        int viewIndex = getViewIndexByCalendar(calendar, weekStart);
        getChildAt(viewIndex).setSelected(true);
        mPreSelectedIndex = viewIndex;
    }

    /**
     * 当周起始发生变化，使用自定义布局需要重写这个方法，避免出问题
     *
     * @param weekStart 周起始
     */
    @Override
    protected void onWeekStartChange(int weekStart) {
        for (int i = 0; i < getChildCount(); i++) {
            ((TextView) getChildAt(i)).setText(getWeekString(i, weekStart));
        }
    }

    /**
     * 或者周文本，这个方法仅供父类使用
     *
     * @param index     index
     * @param weekStart weekStart
     * @return 或者周文本
     */
    private String getWeekString(int index, int weekStart) {
        String[] weeks = getContext().getResources().getStringArray(R.array.alarm_weeks_simple);
        boolean isRTL = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL;
        if (isRTL){//todo 没做完全的适配
//            index=index == 0 ? 6 : index - 1;
            return  weeks[6-index];
        }else {//todo 2345671
            return weeks[index];
        }




//        if (weekStart == 1) {//todo 1234567
//            return  weeks[index == 0 ? 6 : index - 1];
////            return weeks[index];  //7123456
//        } else if (weekStart == 2) {//todo 2345671
//            //1234567
////            return weeks[index == 6 ? 0 : index + 1];
//            return weeks[index];
//        }//todo 7123456
//        return weeks[index < 2 ? 5+index : index - 2];//0:6,1:7,2:1
//        //6712345
////        return weeks[index == 0 ? 6 : index - 1];
    }
}
