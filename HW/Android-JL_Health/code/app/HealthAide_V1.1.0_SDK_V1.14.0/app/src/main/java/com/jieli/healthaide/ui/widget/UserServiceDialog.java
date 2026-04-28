package com.jieli.healthaide.ui.widget;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.DialogUserServiceBinding;
import com.jieli.healthaide.ui.base.BaseDialogFragment;


/**
 * 用户服务协议弹窗
 *
 * @author zqjasonZhong
 * @since 2020/5/20
 */
public class UserServiceDialog extends BaseDialogFragment {

    private DialogUserServiceBinding mBinding;
    private final OnUserServiceListener mListener;

    public UserServiceDialog(@NonNull OnUserServiceListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null) {
            //设置dialog的基本样式参数
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            Window window = getDialog().getWindow();
            if (window != null) {
                //去掉dialog默认的padding
                window.getDecorView().setPadding(0, 0, 0, 0);
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.CENTER;
                //设置dialog的动画
                window.setAttributes(lp);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
        mBinding = DialogUserServiceBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setCancelable(false);
        initUI();
    }

    private void initUI() {
        String appName = getString(R.string.app_name);
        mBinding.tvUserServiceTitle.setText(getString(R.string.declaration, "\"" + appName + "\""));
        mBinding.tvUserServiceAgree.setOnClickListener(v -> onAgree());
        mBinding.tvUserServiceExit.setOnClickListener(v -> onExit());
        String text = getString(R.string.user_declaration, appName, appName);
        String userService = getString(R.string.user_service_name);
        String privacyPolicy = getString(R.string.privacy_policy_name);
        int startPos = text.indexOf("####");
        if (startPos == -1) return;
        int endPos = startPos + userService.length();
        text = text.replace("####", userService);
        int startPos1 = text.indexOf("****");
        if (startPos1 == -1) return;
        text = text.replace("****", privacyPolicy);
        int endPos1 = startPos1 + privacyPolicy.length();
        SpannableString span = new SpannableString(text);
        span.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                onUserService();
            }
        }, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.auxiliary_widget)), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        span.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                onPrivacyPolicy();
            }
        }, startPos1, endPos1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.auxiliary_widget)), startPos1, endPos1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mBinding.tvUserServiceContent.append(span);
        mBinding.tvUserServiceContent.setMovementMethod(LinkMovementMethod.getInstance());
        mBinding.tvUserServiceContent.setLongClickable(false);
    }

    private void onUserService() {
        mListener.onUserService();
    }

    private void onPrivacyPolicy() {
        mListener.onPrivacyPolicy();
    }

    private void onExit() {
        mListener.onExit(this);
        dismiss();
    }

    private void onAgree() {
        mListener.onAgree(this);
        dismiss();
    }

    public interface OnUserServiceListener {
        void onUserService();

        void onPrivacyPolicy();

        void onExit(DialogFragment dialogFragment);

        void onAgree(DialogFragment dialogFragment);
    }

}
