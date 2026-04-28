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
import com.contrarywind.listener.OnItemSelectedListener;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.DialogChooseNumberBinding;
import com.jieli.healthaide.ui.base.BaseDialogFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择性别界面
 *
 * @author zqjasonZhong
 * @since 2021/3/5
 */
public class ChooseNumberDialog extends BaseDialogFragment {

    private DialogChooseNumberBinding binding;
    private OnItemSelectedListener itemSelectedListener;

    private int start = 0;
    private int end = 100;
    private String title;
    private String unit;
    private int currentValue = 0;
    private int step = 1;

    public ChooseNumberDialog(int start, int end, String title, String unit, int currentValue, OnItemSelectedListener itemSelectedListener) {
        this.itemSelectedListener = itemSelectedListener;
        this.start = start;
        this.end = end;
        this.title = title;
        this.unit = unit;
        this.currentValue = currentValue;
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
        View view = inflater.inflate(R.layout.dialog_choose_number, container, false);
        binding = DialogChooseNumberBinding.bind(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.tvDialogChoseNumberTitle.setText(title);


        List<Integer> list = new ArrayList<>();
        for (int i = start; i <= end; i += step) {
            list.add(i);
        }
        binding.wheelviewNumberChose.setLabel(unit);
        binding.wheelviewNumberChose.setOnItemSelectedListener(index -> currentValue = list.get(index));
        binding.wheelviewNumberChose.setItemsVisibleCount(5);
        binding.wheelviewNumberChose.setTextColorCenter(getResources().getColor(R.color.text_important_color));
        binding.wheelviewNumberChose.setTextColorOut(getResources().getColor(R.color.text_secondary_disable_color));
        binding.wheelviewNumberChose.setTextSize(26);
        binding.wheelviewNumberChose.setLineSpacingMultiplier(2);
        binding.wheelviewNumberChose.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        binding.wheelviewNumberChose.setDividerColor(getResources().getColor(R.color.line_color));
        binding.wheelviewNumberChose.setCurrentItem(list.indexOf(currentValue));
        binding.wheelviewNumberChose.setAdapter(new WheelAdapter<Integer>() {
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
        });
        binding.btnNumberChoseCancel.setOnClickListener(v -> dismiss());
        binding.btnNumberChoseSure.setOnClickListener(v -> {
            binding.wheelviewNumberChose.cancelFuture();
            currentValue = list.get(binding.wheelviewNumberChose.getCurrentItem());
            itemSelectedListener.onItemSelected(currentValue);
            dismiss();
        });
    }

    public void setStep(int step) {
        this.step = step;
    }
}
