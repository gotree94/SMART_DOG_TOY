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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.base.BaseDialogFragment;

/**
 * 选择性别界面
 *
 * @author zqjasonZhong
 * @since 2021/3/5
 */
public class ChooseSexDialog extends BaseDialogFragment {
    private TextView tvMan;
    private TextView tvWoman;
    private String cSex;
    private OnSexChooseListener mListener;

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
        View view = inflater.inflate(R.layout.dialog_choose_sex, container, false);
        tvMan = view.findViewById(R.id.tv_dialog_sex_man);
        tvWoman = view.findViewById(R.id.tv_dialog_sex_woman);

        tvMan.setOnClickListener(mOnClickListener);
        tvWoman.setOnClickListener(mOnClickListener);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (cSex == null) {
            cSex = getString(R.string.man);
        }
        chooseSex(cSex, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;
    }

    public void setCurrentSex(String sex) {
        this.cSex = sex;
        chooseSex(cSex, true);
    }

    public void setOnSexChooseListener(OnSexChooseListener listener) {
        mListener = listener;
    }

    private void chooseSex(String sex, boolean isCallback) {
        if (!isAdded() || isDetached()) return;
        if (getString(R.string.man).equals(sex)) {
            tvMan.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_choose_blue, 0);
            tvWoman.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            tvMan.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
            tvWoman.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_choose_blue, 0);
        }
        if (isCallback && mListener != null) mListener.onSelected(sex);
    }

    private final View.OnClickListener mOnClickListener = v -> {
        String sex = ((TextView) v).getText().toString().trim();
        chooseSex(sex, true);
    };

    public interface OnSexChooseListener {

        void onSelected(String sex);
    }
}
