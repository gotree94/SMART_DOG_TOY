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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 选择性别界面
 *
 * @author zqjasonZhong
 * @since 2021/3/5
 */
public class ChooseTimeDialog extends BaseDialogFragment {

    private DialogChooseDateBinding binding;

    private int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    private int min = Calendar.getInstance().get(Calendar.MINUTE);

    private OnTimeSelected onTimeSelected;

    public ChooseTimeDialog(int hour, int minute, OnTimeSelected onTimeSelected) {
        if (hour > 0 && minute > 0) {
            this.hour = hour;
            this.min = minute;
        }
        this.onTimeSelected = onTimeSelected;
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
        View view = inflater.inflate(R.layout.dialog_choose_date, container, false);
        binding = DialogChooseDateBinding.bind(view);
        binding.tvDialogChoseDateTitle.setVisibility(View.GONE);
        binding.wheelviewDateChoseMonth.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Calendar calendar = Calendar.getInstance();
        int y = calendar.get(Calendar.YEAR);

        List<Integer> hourData = new ArrayList<>();
        for (int i = 1; i <= 24; i++) {
            hourData.add(i);
        }

        List<Integer> minuteData = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            minuteData.add(i);
        }

        initStyle(binding.wheelviewDateChoseYear, hourData);
        binding.wheelviewDateChoseYear.setLabel(getString(R.string.hour));
        binding.wheelviewDateChoseYear.setCurrentItem(hourData.indexOf(this.hour));

        initStyle(binding.wheelviewDateChoseDay, minuteData);
        binding.wheelviewDateChoseDay.setLabel(getString(R.string.min));
        binding.wheelviewDateChoseDay.setCurrentItem(Math.max(minuteData.indexOf(this.min), 0));
        binding.btnNumberChoseCancel.setOnClickListener(v -> dismiss());
        binding.btnNumberChoseSure.setOnClickListener(v -> {
            WheelAdapter<Integer> hourAdapter = binding.wheelviewDateChoseYear.getAdapter();
            int hour = hourAdapter.getItem(binding.wheelviewDateChoseYear.getCurrentItem());

            WheelAdapter<Integer> minAdapter = binding.wheelviewDateChoseDay.getAdapter();
            int min = minAdapter.getItem(binding.wheelviewDateChoseDay.getCurrentItem());

            if (onTimeSelected != null) {
                onTimeSelected.onSelected(hour, min);
            }
            dismiss();
        });
    }


    private void initStyle(WheelView wheelView, List<Integer> list) {
        wheelView.setItemsVisibleCount(5);
        wheelView.setTextColorCenter(getResources().getColor(R.color.text_important_color));
        wheelView.setTextColorOut(getResources().getColor(R.color.text_secondary_disable_color));
        wheelView.setTextSize(24);
        wheelView.setCyclic(false);
        wheelView.setLineSpacingMultiplier(2);
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

    public static interface OnTimeSelected {
        void onSelected(int hour, int minute);
    }
}
