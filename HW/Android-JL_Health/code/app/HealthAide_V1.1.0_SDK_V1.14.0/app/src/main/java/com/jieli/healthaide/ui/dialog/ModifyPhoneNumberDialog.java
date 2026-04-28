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
 * 更新手机号界面
 *
 * @author zqjasonZhong
 * @since 2021/3/5
 */
public class ModifyPhoneNumberDialog extends BaseDialogFragment {
    private TextView tvCancel;
    private TextView tvConfirm;
    private OnModifyPhoneNumberDialogListener mListener;
    private String phoneNumber = "";
    private int modifyType = 0;

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
        View view = inflater.inflate(R.layout.dialog_modify_phone_number, container, false);
        TextView tvPhoneNumber = view.findViewById(R.id.tv_current_bind_phone_value);
        tvPhoneNumber.setText(phoneNumber);
        tvCancel = view.findViewById(R.id.tv_cancel);
        tvConfirm = view.findViewById(R.id.tv_confirm);
        TextView tvCurrentBindPhone = view.findViewById(R.id.tv_current_bind_phone);
        TextView tvDialogModifyNumber = view.findViewById(R.id.tv_dialog_modify_number);
        tvCurrentBindPhone.setText(modifyType == 0 ? R.string.current_binded_phone_number : R.string.current_binded_email_address);
        tvDialogModifyNumber.setText(modifyType == 0 ? R.string.modify_binded_phone_number : R.string.modify_binded_email_address);
        tvCancel.setOnClickListener(view1 -> {
            if (mListener != null) {
                mListener.onCancel();
            }
        });
        tvConfirm.setOnClickListener(view1 -> {
            if (mListener != null) {
                mListener.onChange();
            }
        });

        return view;
    }

    public void setCurrentPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;
    }

    public int getModifyType() {
        return modifyType;
    }

    public void setModifyType(int modifyType) {
        this.modifyType = modifyType;
    }

    @Override
    public void dismiss() {
        if (getDialog() != null)
            getDialog().hide();
        super.dismiss();
    }

    public void setOnModifyPhoneNumberDialogListener(OnModifyPhoneNumberDialogListener listener) {
        mListener = listener;
    }

    public interface OnModifyPhoneNumberDialogListener {

        void onChange();

        void onCancel();

    }
}
