package com.jieli.healthaide.ui.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.base.BaseDialogFragment;
import com.jieli.healthaide.ui.login.EmailCodeViewModel;
import com.jieli.healthaide.ui.login.SendCodeViewModel;
import com.jieli.healthaide.ui.login.bean.SmsCounter;
import com.jieli.healthaide.ui.widget.CustomTextWatcher;
import com.jieli.healthaide.util.FormatUtil;
import com.jieli.healthaide.util.phone.PhoneUtil;
import com.jieli.jl_health_http.model.ImageInfo;
import com.jieli.jl_health_http.tool.OnResultCallback;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * 身份验证
 *
 * @author Zhanghuanming
 * @since 2021/3/5
 */
public class UserAuthenticationDialog extends BaseDialogFragment {
    private TextView tvCancel;
    private TextView tvConfirm;
    private TextView tvRegisterSendSms;
    private EditText etRegisterVerificationCode;
    private OnListener mListener;
    private String phoneNumber = "";
    private int mAuthenticationType = 0;
    private EmailCodeViewModel mEmailCodeViewModel;
    private SendCodeViewModel smsCodeViewModel;

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
        View view = inflater.inflate(R.layout.dialog_user_authentication, container, false);
        TextView tvPhoneNumber = view.findViewById(R.id.tv_authentication_tip_value);
        tvRegisterSendSms = view.findViewById(R.id.tv_send_sms_code);
        String phoneString = "";
        if (mAuthenticationType == 0) {
            if (!TextUtils.isEmpty(phoneNumber) && phoneNumber.length() > 10) {
                phoneString = phoneNumber.replace(phoneNumber.substring(3, 9), "******");
            }
        } else {
            phoneString = phoneNumber;
        }
        tvPhoneNumber.setText(phoneString);
        etRegisterVerificationCode = view.findViewById(R.id.et_sms_verification_code);
        etRegisterVerificationCode.setHint(mAuthenticationType == 0 ? R.string.sms_verification_code : R.string.input_identifying_code);
        tvRegisterSendSms.setText(mAuthenticationType == 0 ? R.string.send_sms_code : R.string.send_identifying_code);
        tvCancel = view.findViewById(R.id.tv_cancel);
        tvConfirm = view.findViewById(R.id.tv_confirm);
        tvRegisterSendSms.setOnClickListener(view1 -> {
            if (mAuthenticationType == 0) {
                if (PhoneUtil.isPhoneNumberValid(getContext(), phoneNumber)) {
                    if (smsCodeViewModel != null) {
                        ImageCaptchaDialog dialog = getImageVerifyDialog();
                        dialog.show(getChildFragmentManager(), ImageCaptchaDialog.class.getSimpleName());
                    }
                } else {
                    showTips(R.string.phone_tips_format_err);
                }
            } else {
                if (FormatUtil.checkEmailAddress(phoneNumber)) {
                    if (mEmailCodeViewModel != null) {
                        mEmailCodeViewModel.sendEmailCode(phoneNumber);
                    }
                } else {
                    showTips(R.string.email_address_tips_format_err);
                }
            }
        });
        tvCancel.setOnClickListener(view1 -> {
            if (mListener != null) {
                mListener.onCancel();
            }
        });
        tvConfirm.setOnClickListener(view1 -> {
            /*if (mListener != null) {
                mListener.onChange();
            }*/
            String code = etRegisterVerificationCode.getText().toString().trim();
            if (mAuthenticationType == 0) {
                if (FormatUtil.checkSmsCode(code)) {
                    smsCodeViewModel.checkSmsCode(phoneNumber, code);
                } else {
                    showEditError(etRegisterVerificationCode, getString(R.string.sms_code_tips_format_err));
                }
            } else {
                if (FormatUtil.checkEmailIdentifyCode(code)) {
                    mEmailCodeViewModel.checkEmailCode(phoneNumber, code);
                } else {
                    showEditError(etRegisterVerificationCode, getString(R.string.identifying_code_tips_format_err));
                }
            }

        });
        etRegisterVerificationCode.addTextChangedListener(mVerificationCodeTextWatcher);
        etRegisterVerificationCode.setOnFocusChangeListener(mOnFocusChangeListener);
        return view;
    }

    @Override
    public void dismiss() {
        if (getDialog() != null)
            getDialog().hide();
        super.dismiss();
    }

    public void setAuthenticationType(int authenticationType) {
        mAuthenticationType = authenticationType;
    }

    public void setCurrentPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setOnListener(OnListener listener) {
        mListener = listener;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mAuthenticationType == 0) {
            smsCodeViewModel = new ViewModelProvider(this).get(SendCodeViewModel.class);
            smsCodeViewModel.codeCheckResultLiveData.observe(getViewLifecycleOwner(), integer -> {
                switch (integer) {
                    case SendCodeViewModel.CHECK_FAIL:
                        break;
                    case SendCodeViewModel.CHECK_SUCCESS:
                        if (mListener != null) {
                            mListener.onCheckSmsCodeSuccess();
                        }
                        break;
                }
            });
            smsCodeViewModel.codeCounterMutableLiveData.observe(getViewLifecycleOwner(), smsCounter -> {
                switch (smsCounter.getOp()) {
                    case SmsCounter.OP_IDLE:
                        tvRegisterSendSms.setText(getString(R.string.send_sms_code));
                        tvRegisterSendSms.setTextColor(getResources().getColor(R.color.main_color));
                        tvRegisterSendSms.setEnabled(true);
                        break;
                    case SmsCounter.OP_COUNTER:
                        tvRegisterSendSms.setTextColor(getResources().getColor(R.color.text_secondary_disable_color));
                        tvRegisterSendSms.setEnabled(false);
                        String timeFormat = Math.round(smsCounter.getTime()) + "s";
                        JL_Log.d(TAG, "smsCounterMutableLiveData#OP_COUNTER", "timeFormat = " + timeFormat);
                        String text = getString(R.string.resend_sms_code, timeFormat);
                        tvRegisterSendSms.setText(text);
                        break;
                    case SmsCounter.OP_SEND_CODE:
                        if (mListener != null) {
                            mListener.onSending();
                        }

                        break;
                    case SmsCounter.OP_SEND_CODE_FINISH:
                        if (mListener != null) {
                            mListener.onSendFinish();
                        }
                    case SmsCounter.OP_SEND_CODE_ERROR:
                        if (mListener != null) {
                            mListener.onSendError();
                        }

                        break;
                }
            });
        } else {
            mEmailCodeViewModel = new ViewModelProvider(this).get(EmailCodeViewModel.class);
            mEmailCodeViewModel.emailCodeCheckResultLiveData.observe(getViewLifecycleOwner(), integer -> {
                switch (integer) {
                    case SendCodeViewModel.CHECK_FAIL:
                        break;
                    case SendCodeViewModel.CHECK_SUCCESS:
                        if (mListener != null) {
                            mListener.onCheckSmsCodeSuccess();
                        }
                        break;
                }
            });
            mEmailCodeViewModel.emailCounterMutableLiveData.observe(getViewLifecycleOwner(), smsCounter -> {
                switch (smsCounter.getOp()) {
                    case SmsCounter.OP_IDLE:
                        tvRegisterSendSms.setText(getString(R.string.send_identifying_code));
                        tvRegisterSendSms.setTextColor(getResources().getColor(R.color.main_color));
                        tvRegisterSendSms.setEnabled(true);
                        break;
                    case SmsCounter.OP_COUNTER:
                        tvRegisterSendSms.setTextColor(getResources().getColor(R.color.text_secondary_disable_color));
                        tvRegisterSendSms.setEnabled(false);
                        String timeFormat = Math.round(smsCounter.getTime()) + "s";
                        JL_Log.d(TAG, "emailCounterMutableLiveData#OP_COUNTER", "timeFormat = " + timeFormat);
                        String text = getString(R.string.resend_identifying_code, timeFormat);
                        tvRegisterSendSms.setText(text);
                        break;
                    case SmsCounter.OP_SEND_CODE:
                        if (mListener != null) {
                            mListener.onSending();
                        }

                        break;
                    case SmsCounter.OP_SEND_CODE_FINISH:
                        if (mListener != null) {
                            mListener.onSendFinish();
                        }
                    case SmsCounter.OP_SEND_CODE_ERROR:
                        if (mListener != null) {
                            mListener.onSendError();
                        }

                        break;
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;
    }

    private void showEditError(EditText editText, String error) {
        if (null == editText) return;
        editText.setError(error);
    }

    private @NonNull ImageCaptchaDialog getImageVerifyDialog() {
        ImageCaptchaDialog dialog = new ImageCaptchaDialog(new OnResultCallback<ImageInfo>() {
            @Override
            public void onResult(ImageInfo result) {
                smsCodeViewModel.sendSmsCode(false, phoneNumber, result);
            }

            @Override
            public void onError(int code, String message) {
                showTips(message);
            }
        });
        dialog.setCancelable(false);
        return dialog;
    }

    private final TextWatcher mVerificationCodeTextWatcher = new CustomTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            if (null == s) return;
            String text = s.toString();

            if (mAuthenticationType == 0) {
                boolean ret = FormatUtil.checkSmsCode(text);
                if (ret || text.length() < 6) {
                    showEditError(etRegisterVerificationCode, null);
                } else {
                    showEditError(etRegisterVerificationCode, getString(R.string.sms_code_tips_format_err));
                }
            } else {
                boolean ret = FormatUtil.checkEmailIdentifyCode(text);
                if (ret || text.length() < 8) {
                    showEditError(etRegisterVerificationCode, null);
                } else {
                    showEditError(etRegisterVerificationCode, getString(R.string.identifying_code_tips_format_err));
                }
            }
        }
    };


    private final View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) return;
            if (mAuthenticationType == 0) {
                if (v == etRegisterVerificationCode) {
                    if (!FormatUtil.checkSmsCode(etRegisterVerificationCode.getText().toString().trim())) {
                        showEditError(etRegisterVerificationCode, getString(R.string.sms_code_tips_format_err));
                    }
                }
            } else {
                if (v == etRegisterVerificationCode) {
                    if (!FormatUtil.checkEmailIdentifyCode(etRegisterVerificationCode.getText().toString().trim())) {
                        showEditError(etRegisterVerificationCode, getString(R.string.identifying_code_tips_format_err));
                    }
                }
            }
        }
    };

    public interface OnListener {
        void onSendSmsCode();

        void onChange();

        void onCancel();

        void onSending();

        void onSendFinish();

        void onSendError();

        void onCheckSmsCodeSuccess();
    }
}
