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
 * @ClassName: DeleteAiCloudHistoryDialog
 * @Description: 删除Ai云记录
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/11/25 13:58
 */
public class DeleteAiCloudHistoryDialog extends BaseDialogFragment {
    private TextView tvCancel;
    private TextView tvConfirm;
    private OnDialogListener mListener;
    private String contentString = "";

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
                lp.width = Math.round(0.76f * getScreenWidth());
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.CENTER;
                //设置dialog的动画
//                lp.windowAnimations = R.style.BottomToTopAnim;
                window.setAttributes(lp);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
        View view = inflater.inflate(R.layout.dialog_delete_ai_cloud_history, container, false);
        TextView tvContent = view.findViewById(R.id.tv_content);
        tvContent.setText(contentString);
        tvCancel = view.findViewById(R.id.tv_cancel);
        tvConfirm = view.findViewById(R.id.tv_confirm);
        tvCancel.setOnClickListener(view1 -> {
            if (mListener != null) {
                mListener.onCancel();
            }
        });
        tvConfirm.setOnClickListener(view1 -> {
            if (mListener != null) {
                mListener.onConfirm();
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;
    }

    @Override
    public void dismiss() {
        if (getDialog() != null)
            getDialog().hide();
        super.dismiss();
    }

    public void setContentString(String contentStringSrc) {
        this.contentString = contentStringSrc;
    }

    public void setOnDialogListener(OnDialogListener listener) {
        mListener = listener;
    }

    public interface OnDialogListener {

        void onConfirm();

        void onCancel();

    }
}
