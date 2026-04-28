package com.jieli.healthaide.ui.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.contrarywind.adapter.WheelAdapter;
import com.contrarywind.view.WheelView;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.DialogChooseDateBinding;
import com.jieli.healthaide.ui.base.BaseDialogFragment;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 选择性别界面
 *
 * @author zqjasonZhong
 * @since 2021/3/5
 */
public class ChooseDateDialog extends BaseDialogFragment {

    private DialogChooseDateBinding binding;

    private final int currYear;
    private final int currMonth;
    private final int currDay;

    private int year = Calendar.getInstance().get(Calendar.YEAR);
    private int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
    private int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

    private final OnDateSelected onDateSelected;
    private int title = R.string.date_of_birth;

    public ChooseDateDialog(int year, int month, int day, OnDateSelected onDateSelected) {
        Calendar calendar = Calendar.getInstance();
        currYear = calendar.get(Calendar.YEAR);
        currMonth = calendar.get(Calendar.MONTH) + 1;
        currDay = calendar.get(Calendar.DAY_OF_MONTH);
        if (year > 0 && month > 0 && day > 0) {
            if (year > currYear) year = currYear;
            this.year = year;
            if (year == currYear && month > currMonth) month = currMonth;
            this.month = month;
            if (year == currYear && month == currMonth && day > currDay) day = currDay;
            this.day = day;
        }
        this.onDateSelected = onDateSelected;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requireDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = requireDialog().getWindow();
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
        View view = inflater.inflate(R.layout.dialog_choose_date, container, false);
        binding = DialogChooseDateBinding.bind(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateTimeSelector(true, year, month, day);
        if (title == -1) {
            binding.tvDialogChoseDateTitle.setText("");
        } else {
            binding.tvDialogChoseDateTitle.setText(title);
        }
        binding.wheelviewDateChoseYear.setOnItemSelectedListener(index -> {
            JL_Log.i(TAG, "onViewCreated", "Chose year : " + index);
            WheelAdapter<Integer> yearAdapter = binding.wheelviewDateChoseYear.getAdapter();
            int year = yearAdapter.getItem(index);

            WheelAdapter<Integer> monthAdapter = binding.wheelviewDateChoseMonth.getAdapter();
            int month = monthAdapter.getItem(binding.wheelviewDateChoseMonth.getCurrentItem());

            WheelAdapter<Integer> dayAdapter = binding.wheelviewDateChoseDay.getAdapter();
            int day = dayAdapter.getItem(binding.wheelviewDateChoseDay.getCurrentItem());
            updateTimeSelector(false, year, month, day);
        });

        binding.wheelviewDateChoseMonth.setOnItemSelectedListener(index -> {
            JL_Log.i(TAG, "onViewCreated", "Chose month : " + index);
            WheelAdapter<Integer> yearAdapter = binding.wheelviewDateChoseYear.getAdapter();
            int year = yearAdapter.getItem(binding.wheelviewDateChoseYear.getCurrentItem());

            WheelAdapter<Integer> monthAdapter = binding.wheelviewDateChoseMonth.getAdapter();
            int month = monthAdapter.getItem(index);
            int maxDay = year == currYear && month == currMonth ? currDay : CalendarUtil.getDaysOfMonth(CalendarUtil.isLeapYear(year), month);
            updateDayData(maxDay);
        });

        binding.btnNumberChoseCancel.setOnClickListener(v -> dismiss());
        binding.btnNumberChoseSure.setOnClickListener(v -> {
            WheelAdapter<Integer> yearAdapter = binding.wheelviewDateChoseYear.getAdapter();
            int year = yearAdapter.getItem(binding.wheelviewDateChoseYear.getCurrentItem());

            WheelAdapter<Integer> monthAdapter = binding.wheelviewDateChoseMonth.getAdapter();
            int month = monthAdapter.getItem(binding.wheelviewDateChoseMonth.getCurrentItem());

            WheelAdapter<Integer> dayAdapter = binding.wheelviewDateChoseDay.getAdapter();
            int day = dayAdapter.getItem(binding.wheelviewDateChoseDay.getCurrentItem());

            if (onDateSelected != null) {
                onDateSelected.onSelected(year, month, day);
            }
            dismiss();
        });
    }

    public void setTitle(int title) {
        this.title = title;
    }

    private void updateTimeSelector(boolean isInit, int year, int month, int day) {
        List<Integer> yearData = new ArrayList<>();
        for (int i = currYear; i >= currYear - 200; i--) {
            yearData.add(i);
        }
        if (isInit) {
            initStyle(binding.wheelviewDateChoseYear, yearData);
            binding.wheelviewDateChoseYear.setLabel(getString(R.string.calendar_type_year));
            binding.wheelviewDateChoseYear.setCurrentItem(yearData.indexOf(year));
        }

        List<Integer> monthData = new ArrayList<>();
        int monthCount = year == currYear ? currMonth : 12;
        if (month > monthCount) month = currMonth;
        for (int i = 1; i <= monthCount; i++) {
            monthData.add(i);
        }
        if (isInit) {
            initStyle(binding.wheelviewDateChoseMonth, monthData);
            binding.wheelviewDateChoseMonth.setLabel(getString(R.string.calendar_type_month));
            binding.wheelviewDateChoseMonth.setCurrentItem(monthData.indexOf(month));
        } else {
            ((NumberAdapter) binding.wheelviewDateChoseMonth.getAdapter()).setList(monthData);
            binding.wheelviewDateChoseMonth.setCurrentItem(month - 1);
            binding.wheelviewDateChoseMonth.invalidate();
        }

        List<Integer> dayData = new ArrayList<>();
        int dayCount = year == currYear && month == currMonth ? currDay : CalendarUtil.getDaysOfMonth(CalendarUtil.isLeapYear(year), month);
        if (day > dayCount) day = dayCount;
        for (int i = 1; i <= dayCount; i++) {
            dayData.add(i);
        }
        if (isInit) {
            initStyle(binding.wheelviewDateChoseDay, dayData);
            binding.wheelviewDateChoseDay.setLabel(getString(R.string.calendar_type_day));
            binding.wheelviewDateChoseDay.setCurrentItem(Math.max(dayData.indexOf(day), 0));
        } else {
            ((NumberAdapter) binding.wheelviewDateChoseDay.getAdapter()).setList(dayData);
            binding.wheelviewDateChoseDay.setCurrentItem(day - 1);
            binding.wheelviewDateChoseDay.invalidate();
        }
    }

    private void updateDayData(int maxDay) {
        List<Integer> days = new ArrayList<>();
        for (int i = 1; i <= maxDay; i++) {
            days.add(i);
        }
        NumberAdapter numberAdapter = (NumberAdapter) binding.wheelviewDateChoseDay.getAdapter();
        numberAdapter.setList(days);
        if (binding.wheelviewDateChoseDay.getCurrentItem() + 1 >= maxDay) {
            binding.wheelviewDateChoseDay.setCurrentItem(maxDay - 1);
        }
        binding.wheelviewDateChoseDay.invalidate();
    }


    private void initStyle(WheelView wheelView, List<Integer> list) {
        wheelView.setItemsVisibleCount(5);
        wheelView.setTextColorCenter(getResources().getColor(R.color.text_important_color));
        wheelView.setTextColorOut(getResources().getColor(R.color.text_secondary_disable_color));
        wheelView.setTextSize(24);
        wheelView.setCyclic(false);
        wheelView.setLineSpacingMultiplier(2);
        wheelView.setLabelTextSize(15);
        wheelView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        wheelView.setDividerColor(getResources().getColor(R.color.line_color));
        wheelView.setAdapter(new NumberAdapter(list));
    }


    private static class NumberAdapter implements WheelAdapter<Integer> {
        private List<Integer> list;

        public NumberAdapter(List<Integer> list) {
            this.list = list;
        }

        public void setList(List<Integer> list) {
            this.list = list;
        }

        @Override
        public int getItemsCount() {
            return list.size();
        }

        @Override
        public Integer getItem(int index) {
            return list.get(index);
        }

        @Override
        public int indexOf(Integer o) {
            return list.indexOf(o);
        }

    }

    public interface OnDateSelected {
        void onSelected(int year, int month, int day);
    }
}
