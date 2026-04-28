package com.jieli.healthaide.ui.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.login.bean.SmsCounter;
import com.jieli.healthaide.ui.widget.CustomTextWatcher;
import com.jieli.healthaide.util.FormatUtil;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @author : ZHM
 * @desc : 邮箱验证码 fragment基类
 */
public abstract class EmailCodeFragment extends BaseFragment {

    private EmailCodeViewModel mViewModel;
    private EditText etRegisterPhoneNumber;
    private EditText etRegisterVerificationCode;
    private TextView tvRegisterSendSms;
    private ImageView ivRegisterClearVerificationCode;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EmailCodeViewModel.class);
        init();
    }

    private void init() {
        if (getView() == null) return;
        View root = requireView();

        etRegisterPhoneNumber = root.findViewById(R.id.et_phone_number);
        etRegisterVerificationCode = root.findViewById(R.id.et_verification_code);
        tvRegisterSendSms = root.findViewById(R.id.tv_send_sms);
        ivRegisterClearVerificationCode = root.findViewById(R.id.iv_clear_verification_code);

        etRegisterVerificationCode.addTextChangedListener(mVerificationCodeTextWatcher);
        etRegisterPhoneNumber.addTextChangedListener(mEmailAddressTextWatcher);
        etRegisterPhoneNumber.setOnFocusChangeListener(mOnFocusChangeListener);
        etRegisterVerificationCode.setOnFocusChangeListener(mOnFocusChangeListener);


        tvRegisterSendSms.setOnClickListener(v -> handleSendSmsCodeClick());

        ivRegisterClearVerificationCode.setOnClickListener(v -> {
            etRegisterVerificationCode.setText("");
            etRegisterVerificationCode.setFocusable(true);
            etRegisterVerificationCode.requestFocus();
        });

        mViewModel.emailCounterMutableLiveData.observe(getViewLifecycleOwner(), smsCounter -> {
            switch (smsCounter.getOp()) {
                case SmsCounter.OP_IDLE:
                    tvRegisterSendSms.setText(getString(R.string.send_identifying_code));
                    tvRegisterSendSms.setTextColor(getResources().getColor(R.color.main_color));
                    tvRegisterSendSms.setEnabled(true);
                    ivRegisterClearVerificationCode.setVisibility(View.INVISIBLE);
                    break;
                case SmsCounter.OP_COUNTER:
                    tvRegisterSendSms.setTextColor(getResources().getColor(R.color.text_secondary_disable_color));
                    tvRegisterSendSms.setEnabled(false);
                    ivRegisterClearVerificationCode.setVisibility(View.VISIBLE);

                    String timeFormat = Math.round(smsCounter.getTime()) + "s";
                    JL_Log.d(tag, "emailCounterMutableLiveData", "OP_COUNTER ---> timeFormat = " + timeFormat);
                    String text = getString(R.string.resend_identifying_code, timeFormat);
                    tvRegisterSendSms.setText(text);
                    break;
                case SmsCounter.OP_SEND_CODE:
                    showWaitDialog();
                    break;
                case SmsCounter.OP_SEND_CODE_FINISH:
                case SmsCounter.OP_SEND_CODE_ERROR:
                    dismissWaitDialog();
                    break;
            }
        });
    }


    private void showEditError(EditText editText, String error) {
        if (null == editText) return;
        editText.setError(error);
    }


    private final TextWatcher mEmailAddressTextWatcher = new CustomTextWatcher() {


        @Override
        public void afterTextChanged(Editable s) {
            if (null == s) return;
            String text = s.toString();
            boolean ret = FormatUtil.checkEmailAddress(text);
            if (ret || text.length() < 11) {
                showEditError(etRegisterPhoneNumber, null);
            } else {
                showEditError(etRegisterPhoneNumber, getString(R.string.email_address_tips_format_err));
            }
        }
    };

    private final TextWatcher mVerificationCodeTextWatcher = new CustomTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            if (null == s) return;
            String text = s.toString();
            boolean ret = FormatUtil.checkEmailIdentifyCode(text);
            if (ret || text.length() < 8) {
                showEditError(etRegisterVerificationCode, null);
            } else {
                showEditError(etRegisterVerificationCode, getString(R.string.identifying_code_tips_format_err));
            }
        }
    };


    private final View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) return;
            if (v == etRegisterPhoneNumber) {
                if (!FormatUtil.checkEmailAddress(etRegisterPhoneNumber.getText().toString().trim())) {
                    showEditError(etRegisterPhoneNumber, getString(R.string.email_address_tips_format_err));
                }
            } else if (v == etRegisterVerificationCode) {
                if (!FormatUtil.checkEmailIdentifyCode(etRegisterVerificationCode.getText().toString().trim())) {
                    showEditError(etRegisterVerificationCode, getString(R.string.identifying_code_tips_format_err));
                }
            }
        }
    };

    private void handleSendSmsCodeClick() {
        String phoneNumber = etRegisterPhoneNumber.getText().toString().trim();
        if (FormatUtil.checkEmailAddress(phoneNumber)) {
            mViewModel.sendEmailCode(phoneNumber);
        } else {
            showTips(R.string.email_address_tips_format_err);
        }
    }

}