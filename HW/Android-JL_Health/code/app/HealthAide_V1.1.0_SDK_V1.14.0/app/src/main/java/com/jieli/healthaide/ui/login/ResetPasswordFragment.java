package com.jieli.healthaide.ui.login;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentResetPasswordBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.dialog.WaitingDialog;
import com.jieli.healthaide.ui.widget.CustomTextWatcher;
import com.jieli.healthaide.util.FormatUtil;

import java.util.Objects;

public class ResetPasswordFragment extends SmsCodeFragment {

    private ResetPasswordViewModel mViewModel;
    private FragmentResetPasswordBinding resetPasswordBinding;

    public static ResetPasswordFragment newInstance() {
        return new ResetPasswordFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        resetPasswordBinding = FragmentResetPasswordBinding.inflate(inflater, container, false);
        resetPasswordBinding.layoutGetSmsCode.etVerificationCode.addTextChangedListener(smsCodeCheck);
        resetPasswordBinding.layoutGetSmsCode.etPhoneNumber.addTextChangedListener(smsCodeCheck);
        resetPasswordBinding.tietPassword2.addTextChangedListener(passwordCheck);
        resetPasswordBinding.tietPassword1.addTextChangedListener(passwordCheck);
        resetPasswordBinding.clResetpasswordTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        resetPasswordBinding.tvUseEmailSearchPassword.setOnClickListener(v -> {
            requireActivity().finish();
            ContentActivity.startContentActivity(requireContext(), ResetEmailPasswordFragment.class.getCanonicalName());
        });
        resetPasswordBinding.layoutGetSmsCode.etPhoneNumber.setHint(R.string.hint_input_bind_phone_number);
        return resetPasswordBinding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ResetPasswordViewModel.class);
        mViewModel.stateLiveData.observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case ResetPasswordViewModel.STATE_INPUT_SMS_CODE:
                    resetPasswordBinding.clResetPassword.setVisibility(View.INVISIBLE);
                    resetPasswordBinding.layoutGetSmsCode.getRoot().setVisibility(View.VISIBLE);
                    resetPasswordBinding.btnResetPasswordCheck.setVisibility(View.VISIBLE);
                    resetPasswordBinding.btnResetPasswordReset.setVisibility(View.GONE);
                    resetPasswordBinding.clResetpasswordTopbar.tvTopbarTitle.setText(R.string.retrieve_password);
                    dismissWaitDialog();
                    break;
                case ResetPasswordViewModel.STATE_CHECK_SMS_CODE:
                    showWaitDialog();
                    break;
                case ResetPasswordViewModel.STATE_INPUT_PASSWORD:
                    resetPasswordBinding.clResetPassword.setVisibility(View.VISIBLE);
                    resetPasswordBinding.layoutGetSmsCode.getRoot().setVisibility(View.INVISIBLE);
                    resetPasswordBinding.btnResetPasswordCheck.setVisibility(View.GONE);
                    resetPasswordBinding.btnResetPasswordReset.setVisibility(View.VISIBLE);
                    resetPasswordBinding.tvUseEmailSearchPassword.setVisibility(View.GONE);
                    resetPasswordBinding.clResetpasswordTopbar.tvTopbarTitle.setText(R.string.set_password);
                    dismissWaitDialog();
                    break;
                case ResetPasswordViewModel.STATE_RESET_PASSWORD: {
                    showWaitDialog();
                    break;
                }
                case ResetPasswordViewModel.STATE_RESET_PASSWORD_FINISH: {
                    dismissWaitDialog();
                    requireActivity().setResult(Activity.RESULT_OK);
                    requireActivity().onBackPressed();
                }
                break;
            }
        });

        resetPasswordBinding.btnResetPasswordReset.setOnClickListener(v -> {
            String password1 = Objects.requireNonNull(resetPasswordBinding.tietPassword1.getText()).toString().trim();
            String password2 = Objects.requireNonNull(resetPasswordBinding.tietPassword2.getText()).toString().trim();
            if (!TextUtils.equals(password1, password2)) {
                resetPasswordBinding.tvErrorTip.setVisibility(View.VISIBLE);
                return;
            }
            String mobile = resetPasswordBinding.layoutGetSmsCode.etPhoneNumber.getText().toString().trim();
            String code = resetPasswordBinding.layoutGetSmsCode.etVerificationCode.getText().toString().trim();
            mViewModel.resetPassword(mobile, password1, code);
        });

        resetPasswordBinding.btnResetPasswordCheck.setOnClickListener(v -> {
            String mobile = resetPasswordBinding.layoutGetSmsCode.etPhoneNumber.getText().toString().trim();
            String code = resetPasswordBinding.layoutGetSmsCode.etVerificationCode.getText().toString().trim();
            mViewModel.checkSmsCode(mobile, code);
        });
    }

    @Override
    public boolean isRegisterUser() {
        return false;
    }

    private final TextWatcher smsCodeCheck = new CustomTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            String code = resetPasswordBinding.layoutGetSmsCode.etVerificationCode.getText().toString().trim();
            String mobile = resetPasswordBinding.layoutGetSmsCode.etPhoneNumber.getText().toString().trim();
            boolean ret = FormatUtil.checkSmsCode(code)
                    && FormatUtil.checkPhoneNumber(mobile);
            resetPasswordBinding.btnResetPasswordCheck.setEnabled(ret);
        }
    };

    private final TextWatcher passwordCheck = new CustomTextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            super.beforeTextChanged(s, start, count, after);
            resetPasswordBinding.tvErrorTip.setVisibility(View.GONE);
        }

        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            String pass1 = Objects.requireNonNull(resetPasswordBinding.tietPassword1.getText()).toString().trim();
            String pass2 = Objects.requireNonNull(resetPasswordBinding.tietPassword2.getText()).toString().trim();
            boolean ret = FormatUtil.checkPassword(pass1)
                    && FormatUtil.checkPassword(pass2);
            resetPasswordBinding.btnResetPasswordReset.setEnabled(ret);
        }
    };

}