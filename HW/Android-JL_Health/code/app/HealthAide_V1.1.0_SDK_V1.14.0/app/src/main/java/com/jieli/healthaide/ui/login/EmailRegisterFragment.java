package com.jieli.healthaide.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentEmailRegisterBinding;
import com.jieli.healthaide.tool.config.ConfigHelper;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.mine.ImproveUserInfoFragment;
import com.jieli.healthaide.ui.widget.AgreePrivacyPolicyView;
import com.jieli.healthaide.ui.widget.CustomTextWatcher;
import com.jieli.healthaide.util.FormatUtil;
import com.jieli.healthaide.util.HealthConstant;

/**
 * Email注册 Fragment
 */
public class EmailRegisterFragment extends EmailCodeFragment {

    private EmailRegisterViewModel mViewModel;
    private FragmentEmailRegisterBinding mFragmentBinding;
    private AgreePrivacyPolicyView agreePrivacyPolicyView;

    public static EmailRegisterFragment newInstance() {
        return new EmailRegisterFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mFragmentBinding = FragmentEmailRegisterBinding.inflate(inflater, container, false);
        agreePrivacyPolicyView = new AgreePrivacyPolicyView(requireContext(), mFragmentBinding.viewAgreePolicy, isAgree -> {
            Intent intent = new Intent();
            intent.putExtra(HealthConstant.KEY_AGREE_PRIVACY_POLICY, isAgree);
            requireActivity().setResult(Activity.RESULT_OK, intent);
        });
        return mFragmentBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EmailRegisterViewModel.class);
        initUI();
        updateEditPwdState();
        mViewModel.opStateLiveData.observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case RegisterViewModel.OP_STATE_FINISH:
                    showTips(R.string.register_succeed);
                    ConfigHelper.getInstance().setLoginType(1);//登录方式是邮箱地址
                    requireActivity().finish();
                    ContentActivity.startContentActivity(requireContext(), ImproveUserInfoFragment.class.getCanonicalName());
                case RegisterViewModel.OP_STATE_IDLE:
                    dismissWaitDialog();
                    break;
                case RegisterViewModel.OP_STATE_BUSY:
                    showWaitDialog();
                    break;
            }
        });
        Bundle bundle = getArguments();
        boolean isAgree = false;
        if (null != bundle) {
            isAgree = bundle.getBoolean(HealthConstant.KEY_AGREE_PRIVACY_POLICY, false);
        }
        agreePrivacyPolicyView.setAgree(isAgree);
    }

    private void initUI() {
        mFragmentBinding.clRegisterTopbar.tvTopbarTitle.setText(R.string.email_register);
        mFragmentBinding.clRegisterTopbar.tvTopbarRight.setText(R.string.phone_register);
        mFragmentBinding.clRegisterTopbar.tvTopbarRight.setTextColor(getResources().getColor(R.color.text_secondary_color));
        mFragmentBinding.clRegisterTopbar.tvTopbarRight.setOnClickListener(mOnClickListener);
        mFragmentBinding.clRegisterTopbar.tvTopbarLeft.setOnClickListener(mOnClickListener);

        mFragmentBinding.etRegisterPassword.addTextChangedListener(mPasswordTextWatcher);
        mFragmentBinding.etRegisterPassword.setOnFocusChangeListener(mOnFocusChangeListener);
        mFragmentBinding.ivRegisterHidePassword.setOnClickListener(mOnClickListener);
        mFragmentBinding.btnRegister.setOnClickListener(mOnClickListener);

        mFragmentBinding.etRegisterPassword.addTextChangedListener(registerStateListener);
        mFragmentBinding.layoutSmsCode.etPhoneNumber.addTextChangedListener(registerStateListener);
        mFragmentBinding.layoutSmsCode.etVerificationCode.addTextChangedListener(registerStateListener);
    }

    private boolean isAgree() {
        if (null == agreePrivacyPolicyView) return false;
        return agreePrivacyPolicyView.isAgree();
    }

    private void updateEditPwdState() {
        if (mViewModel.isHidPassword) {
            mFragmentBinding.ivRegisterHidePassword.setImageResource(R.drawable.ic_password_eye_close);
            mFragmentBinding.etRegisterPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            mFragmentBinding.ivRegisterHidePassword.setImageResource(R.drawable.ic_password_eye_open);
            mFragmentBinding.etRegisterPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

        }
        // 切换后将EditText光标置于末尾
        Spannable charSequence = mFragmentBinding.etRegisterPassword.getText();
        if (charSequence != null) {
            Selection.setSelection(charSequence, charSequence.length());
        }
    }

    private final TextWatcher registerStateListener = new CustomTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            boolean ret = FormatUtil.checkEmailAddress(mFragmentBinding.layoutSmsCode.etPhoneNumber.getText().toString().trim())
                    && FormatUtil.checkEmailIdentifyCode(mFragmentBinding.layoutSmsCode.etVerificationCode.getText().toString().trim())
                    && FormatUtil.checkPassword(mFragmentBinding.etRegisterPassword.getText().toString().trim());

            mFragmentBinding.btnRegister.setEnabled(ret);
            mFragmentBinding.btnRegister.setBackgroundResource(ret ? R.drawable.btn_purple_shape : R.drawable.btn_gray_shape);
            mFragmentBinding.btnRegister.setTextColor(getResources().getColor(ret ? R.color.white : R.color.text_secondary_color));

        }
    };


    private void showEditError(EditText editText, String error) {
        if (null == editText) return;
        editText.setError(error);
    }


    private final TextWatcher mPasswordTextWatcher = new CustomTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            if (null == s) return;
            String text = s.toString();
            boolean ret = FormatUtil.checkPassword(text);
            if (ret || text.length() < 6) {
                showEditError(mFragmentBinding.etRegisterPassword, null);
            } else if (!ret && text.length() > 12) {
                showEditError(mFragmentBinding.etRegisterPassword, getString(R.string.password_tips_format_err));
            } else if (!ret && (text.length() > 6 && text.length() < 12)) {
                showEditError(mFragmentBinding.etRegisterPassword, getString(R.string.input_password_tips));
            }
        }
    };

    private final View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) return;
            if (v == mFragmentBinding.etRegisterPassword) {
                String text = mFragmentBinding.etRegisterPassword.getText().toString().trim();
                if (!FormatUtil.checkPassword(text)) {
                    if (text.length() < 6 || text.length() > 12) {
                        showEditError(mFragmentBinding.etRegisterPassword, getString(R.string.password_tips_format_err));
                    } else {
                        showEditError(mFragmentBinding.etRegisterPassword, getString(R.string.input_password_tips));
                    }
                }
            }
        }
    };

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mFragmentBinding.clRegisterTopbar.tvTopbarRight) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(HealthConstant.KEY_AGREE_PRIVACY_POLICY, isAgree());
                ContentActivity.startContentActivity(requireContext(), RegisterFragment.class.getCanonicalName(), bundle);
                requireActivity().finish();
            } else if (v == mFragmentBinding.clRegisterTopbar.tvTopbarLeft) {
                requireActivity().finish();
            } else if (v == mFragmentBinding.ivRegisterHidePassword) {
                mViewModel.isHidPassword = !mViewModel.isHidPassword;
                updateEditPwdState();
            } else if (v == mFragmentBinding.btnRegister) {
                boolean isAgree = isAgree();
                if (!isAgree) {
                    showTips(getString(R.string.check_policy_status_tips));
                    return;
                }
                String mobile = mFragmentBinding.layoutSmsCode.etPhoneNumber.getText().toString().trim();
                String password = mFragmentBinding.etRegisterPassword.getText().toString().trim();
                String code = mFragmentBinding.layoutSmsCode.etVerificationCode.getText().toString().trim();
                mViewModel.register(mobile, password, code);
            }
        }
    };
}