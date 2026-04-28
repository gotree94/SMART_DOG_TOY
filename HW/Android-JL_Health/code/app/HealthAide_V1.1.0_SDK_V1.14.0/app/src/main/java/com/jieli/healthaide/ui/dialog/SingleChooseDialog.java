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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.base.BaseDialogFragment;

import java.util.List;

/**
 * 选择性别界面
 *
 * @author zqjasonZhong
 * @since 2021/3/5
 */
public class SingleChooseDialog<T> extends BaseDialogFragment {

    private OnSingleChooseListener<T> mListener;

    private List<T> data;

    private T selectItem;
    private String title;

    public SingleChooseDialog(String title, List<T> data, T selectItem, OnSingleChooseListener<T> mListener) {
        this.mListener = mListener;
        this.data = data;
        this.selectItem = selectItem;
        this.title = title;
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
        View view = inflater.inflate(R.layout.dialog_single_chose, container, false);

        return view;
    }


    private void initItemView() {
        LinearLayout parent = requireView().findViewById(R.id.ll_single_chose_title);
        TextView titleView = createTextView(title);
        titleView.setGravity(Gravity.CENTER);
        titleView.setTextSize(18);
        parent.addView(titleView);
        for (T t : data) {
            parent.addView(createItemView(t));
        }
    }


    private TextView createItemView(T t) {
        TextView textView = createTextView(t.toString());
        textView.setTag(t);
        textView.setOnClickListener(v -> {
            if (v.getTag() != null) {
                T t1 = (T) v.getTag();
                selectItem = t1;
                updateSelectView();
                if (mListener != null) {
                    mListener.onSelected(this, selectItem);
                }
            }
        });
        return textView;
    }

    private TextView createTextView(String text) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView textView = new TextView(getContext());
        textView.setLayoutParams(params);
        int padding = ValueUtil.dp2px(getContext(), 10);
        textView.setPadding(padding, padding, padding, padding);
        textView.setText(text);
        textView.setTextSize(16);
        textView.setTextColor(getResources().getColor(R.color.text_important_color));
        return textView;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initItemView();
        updateSelectView();
    }

    private void updateSelectView() {
        LinearLayout parent = requireView().findViewById(R.id.ll_single_chose_title);
        int count = parent.getChildCount();
        for (int i = 1; i < count; i++) {
            TextView textView = (TextView) parent.getChildAt(i);
            if (selectItem != null && textView.getText().equals(selectItem.toString())) {
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_choose_blue, 0);
            } else {
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
            }
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;
    }


    public interface OnSingleChooseListener<T> {
        void onSelected(SingleChooseDialog dialog, T value);
    }


}
