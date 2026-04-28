package com.jieli.healthaide.ui.widget;

import android.text.Editable;
import android.text.InputType;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.jieli.component.utils.ToastUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.ViewSendCodeBinding;
import com.jieli.healthaide.ui.dialog.ImageCaptchaDialog;
import com.jieli.healthaide.ui.dialog.WaitingDialog;
import com.jieli.healthaide.ui.login.bean.SmsCounter;
import com.jieli.healthaide.util.FormatUtil;
import com.jieli.jl_health_http.model.ImageInfo;
import com.jieli.jl_health_http.tool.OnResultCallback;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * SendCodeView
 *
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 发送验证码控件处理
 * @since 2024/11/19
 */
public class SendCodeView {

    /**
     * 自行选择
     */
    public static final int WAY_AUTO = 0;
    /**
     * 短信验证码
     */
    public static final int WAY_SMS_CODE = 1;
    /**
     * 邮箱验证码
     */
    public static final int WAY_EMAIL_CODE = 2;

    /**
     * 页面
     */
    @NonNull
    private final Fragment fragment;
    /**
     * 发送验证码布局
     */
    @NonNull
    private final ViewSendCodeBinding binding;
    /**
     * 发送验证码方式
     */
    private final int way;
    /**
     * 发送验证码监听器
     */
    @NonNull
    private final OnSendCodeListener listener;

    /**
     * 等待框
     */
    private WaitingDialog mWaitingDialog;
    /**
     * 是否登录准备完成
     */
    private boolean isReadyOk = false;

    public SendCodeView(@NonNull Fragment fragment, @NonNull ViewSendCodeBinding binding,
                        int way, @NonNull OnSendCodeListener listener) {
        this.fragment = fragment;
        this.binding = binding;
        this.way = way;
        this.listener = listener;
        initUI();
    }

    public boolean isReadyOk() {
        return isReadyOk;
    }

    public String getAccount() {
        return binding.etAccount.getText().toString().trim();
    }

    public String getCode() {
        return binding.etCode.getText().toString().trim();
    }

    public void setAccount(String account) {
        if (null == account) return;
        binding.etAccount.setText(account);
        binding.etAccount.setSelection(account.length());
    }

    public void updateStateUI(@NonNull SmsCounter smsCounter) {
        switch (smsCounter.getOp()) {
            case SmsCounter.OP_IDLE: {
                binding.viewSendLine.setVisibility(View.INVISIBLE);
                binding.tvSendCode.setText(fragment.getString(R.string.send_sms_code));
                binding.tvSendCode.setTextColor(ContextCompat.getColor(fragment.requireContext(), R.color.main_color));
                binding.tvSendCode.setEnabled(true);
                binding.etCode.setText("");
                break;
            }
            case SmsCounter.OP_COUNTER: {
                binding.viewSendLine.setVisibility(View.VISIBLE);
                binding.tvSendCode.setTextColor(ContextCompat.getColor(fragment.requireContext(), R.color.text_secondary_disable_color));
                binding.tvSendCode.setEnabled(false);
                String timeFormat = smsCounter.getTime() + "s";
                String text = fragment.getString(R.string.resend_sms_code, timeFormat);
                binding.tvSendCode.setText(text);
                break;
            }
            case SmsCounter.OP_SEND_CODE: {
                showWaitDialog();
                break;
            }
            default: {
                dismissWaitDialog();
                break;
            }
        }
    }

    private void initUI() {
        binding.etAccount.addTextChangedListener(new CustomTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (null == s) return;
                String text = s.toString().trim();
                final int size = text.length();
                binding.ivClearAccount.setVisibility(size > 0 ? View.VISIBLE : View.INVISIBLE);
                if (size == 0) return;
                checkLoginReady();
            }
        });
        binding.etCode.addTextChangedListener(new CustomTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (null == s) return;
                String text = s.toString().trim();
                final int size = text.length();
                binding.ivClearCode.setVisibility(size > 0 ? View.VISIBLE : View.INVISIBLE);
                if (size == 0) return;
                checkCodeIsValid(getAccount(), text);
                checkLoginReady();
            }
        });
        binding.ivClearAccount.setOnClickListener(v -> {
            binding.etAccount.setText("");
            binding.etAccount.setFocusable(true);
            binding.etAccount.requestFocus();
        });
        binding.ivClearCode.setOnClickListener(v -> {
            binding.etCode.setText("");
            binding.etCode.setFocusable(true);
            binding.etCode.requestFocus();
        });
        binding.tvSendCode.setOnClickListener(v -> handleSendCodeEvent());

        switch (way) {
            case WAY_SMS_CODE: {
                binding.etAccount.setInputType(InputType.TYPE_CLASS_PHONE);
                binding.etAccount.setHint(R.string.hint_input_phone_number);
                break;
            }
            case WAY_EMAIL_CODE: {
                binding.etAccount.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                binding.etAccount.setHint(R.string.hint_input_email);
                break;
            }
            default: {
                binding.etAccount.setInputType(InputType.TYPE_CLASS_TEXT);
                binding.etAccount.setHint(R.string.hint_input_phone_number_or_email);
                break;
            }
        }
    }

    private void checkLoginReady() {
        String account = getAccount();
        String code = getCode();
        boolean ret = FormatUtil.checkPhoneNumber(account) && FormatUtil.checkSmsCode(code) ||
                (FormatUtil.checkEmailAddress(account) && FormatUtil.checkEmailIdentifyCode(code));
        if (ret != isReadyOk) {
            isReadyOk = ret;
            listener.onLoginReady(ret);
        }
    }

    private String checkAccountIsValid(@NonNull String account) {
        String error = null;
        switch (way) {
            case WAY_SMS_CODE: {
                boolean ret = FormatUtil.checkPhoneNumber(account);
                if (!ret) {
                    error = fragment.getString(R.string.phone_tips_format_err);
                }
                break;
            }
            case WAY_EMAIL_CODE: {
                boolean ret = FormatUtil.checkEmailAddress(account);
                if (!ret) {
                    error = fragment.getString(R.string.email_address_tips_format_err);
                }
                break;
            }
            default: {
                boolean ret = FormatUtil.checkPhoneNumber(account) || FormatUtil.checkEmailAddress(account);
                if (!ret) {
                    error = fragment.getString(R.string.phone_or_email_tips_format_err);
                }
                break;
            }
        }
        binding.etAccount.setError(error);
        return error;
    }

    private void checkCodeIsValid(@NonNull String account, @NonNull String code) {
        String error = null;
        final int size = code.length();
        if (FormatUtil.checkPhoneNumber(account)) {
            boolean ret = FormatUtil.checkSmsCode(code);
            if (!ret && size >= 6) {
                error = fragment.getString(R.string.sms_code_tips_format_err);
            }
        } else {
            boolean ret = FormatUtil.checkEmailIdentifyCode(code);
            if (!ret && size >= 8) {
                error = fragment.getString(R.string.identifying_code_tips_format_err);
            }
        }
        binding.etCode.setError(error);
    }

    private void showWaitDialog() {
        if (!fragment.isAdded()) return;
        if (mWaitingDialog == null) {
            mWaitingDialog = new WaitingDialog();
        }
        if (!mWaitingDialog.isShow()) {
            mWaitingDialog.show(fragment.getChildFragmentManager(), WaitingDialog.class.getCanonicalName());
        }
    }

    private void dismissWaitDialog() {
        if (!fragment.isAdded()) return;
        if (mWaitingDialog != null) {
            if (mWaitingDialog.isShow()) {
                mWaitingDialog.dismiss();
            }
            mWaitingDialog = null;
        }
    }

    private void showTips(String content) {
        JL_Log.d(fragment.getClass().getSimpleName(), "showTips", content);
        ToastUtil.showToastLong(content);
    }

    private void showCaptchaDialog(String account) {
        ImageCaptchaDialog dialog = new ImageCaptchaDialog(new OnResultCallback<ImageInfo>() {
            @Override
            public void onResult(ImageInfo result) {
                listener.sendCode(account, WAY_SMS_CODE, result);
            }

            @Override
            public void onError(int code, String message) {
                showTips(message);
            }
        });
        dialog.setCancelable(false);
        dialog.show(fragment.getChildFragmentManager(), ImageCaptchaDialog.class.getSimpleName());
    }

    private void handleSendCodeEvent() {
        String account = binding.etAccount.getText().toString().trim();
        String error = checkAccountIsValid(account);
        if (error != null) return;
        boolean isPhoneNumber = FormatUtil.checkPhoneNumber(account);
        boolean isEmailAddress = FormatUtil.checkEmailAddress(account);
        switch (way) {
            case WAY_SMS_CODE: {
                if (isPhoneNumber) {
                    showCaptchaDialog(account);
                    return;
                }
                showTips(fragment.getString(R.string.phone_tips_format_err));
                break;
            }
            case WAY_EMAIL_CODE: {
                if (isEmailAddress) {
                    listener.sendCode(account, WAY_EMAIL_CODE, null);
                    return;
                }
                showTips(fragment.getString(R.string.email_address_tips_format_err));
                break;
            }
            default: {
                if (isPhoneNumber) {
                    showCaptchaDialog(account);
                    return;
                }
                if (isEmailAddress) {
                    listener.sendCode(account, WAY_EMAIL_CODE, null);
                    return;
                }
                showTips(fragment.getString(R.string.phone_or_email_tips_format_err));
            }
        }

    }

    public interface OnSendCodeListener {

        /**
         * 发送验证码
         *
         * @param account   String 账号
         * @param way       int 获取验证码方式
         * @param imageInfo ImageInfo 图像验证信息
         */
        void sendCode(String account, int way, ImageInfo imageInfo);

        /**
         * 回调登录准备状态
         *
         * @param isReady boolean 是否准备完成
         */
        void onLoginReady(boolean isReady);
    }
}
