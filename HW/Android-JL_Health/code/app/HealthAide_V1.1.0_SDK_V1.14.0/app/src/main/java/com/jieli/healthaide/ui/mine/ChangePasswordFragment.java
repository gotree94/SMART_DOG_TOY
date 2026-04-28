package com.jieli.healthaide.ui.mine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentChangePasswordBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.login.ResetPasswordFragment;
import com.jieli.healthaide.ui.login.ResetPasswordViewModel;
import com.jieli.healthaide.ui.widget.CustomTextWatcher;
import com.jieli.healthaide.util.FormatUtil;

import java.util.Objects;

/**
 * @ClassName: ChangePasswordFragment
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/4/2 9:46
 */
public class ChangePasswordFragment extends BaseFragment {
    private FragmentChangePasswordBinding fragmentChangePasswordBinding;
    private ResetPasswordViewModel mViewModel;
    private final ActivityResultLauncher<Intent> mResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result != null && result.getResultCode() == Activity.RESULT_OK) {//忘记密码-修改成功，返回上一层
            requireActivity().onBackPressed();
        }
    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentChangePasswordBinding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        fragmentChangePasswordBinding.tilPasswordNew.setOnClickListener(view -> {
            TransformationMethod method = hasPasswordTransformation() ? new PasswordTransformationMethod() : null;
            fragmentChangePasswordBinding.tietPasswordNewReconfirm.setTransformationMethod(method);
        });
        fragmentChangePasswordBinding.tietPasswordOld.addTextChangedListener(passwordCheck);
        fragmentChangePasswordBinding.tietPasswordNew.addTextChangedListener(passwordCheckNewPassword);
        fragmentChangePasswordBinding.tietPasswordNewReconfirm.addTextChangedListener(passwordCheck);
        fragmentChangePasswordBinding.clRegisterTopbar.tvTopbarTitle.setText(R.string.change_password);
        fragmentChangePasswordBinding.clRegisterTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        fragmentChangePasswordBinding.btnConfirm.setOnClickListener(v -> {
            String oldPassword = Objects.requireNonNull(fragmentChangePasswordBinding.tietPasswordOld.getText()).toString().trim();
            String newPassword = Objects.requireNonNull(fragmentChangePasswordBinding.tietPasswordNew.getText()).toString().trim();
            String newPasswordReconfirm = Objects.requireNonNull(fragmentChangePasswordBinding.tietPasswordNewReconfirm.getText()).toString().trim();
            if (TextUtils.equals(newPassword, newPasswordReconfirm)) {
                mViewModel.resetPassword(oldPassword, newPassword);
            } else {
                fragmentChangePasswordBinding.tvError.setText(R.string.password_is_inconsistent);
                fragmentChangePasswordBinding.tvError.setVisibility(View.VISIBLE);
            }
        });
        fragmentChangePasswordBinding.tvForgetPassword.setOnClickListener(view -> ContentActivity.startContentActivityForResult(ChangePasswordFragment.this, ResetPasswordFragment.class.getCanonicalName(), null, mResultLauncher));
        return fragmentChangePasswordBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ResetPasswordViewModel.class);
        mViewModel.stateLiveData.observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case ResetPasswordViewModel.STATE_INPUT_PASSWORD:
                    dismissWaitDialog();
                    break;
                case ResetPasswordViewModel.STATE_RESET_PASSWORD: {
                    showWaitDialog();
                    break;
                }
                case ResetPasswordViewModel.STATE_RESET_PASSWORD_FINISH: {
                    dismissWaitDialog();
                    showTips(R.string.modified_success);
                    back();
                }
                break;
            }
        });
    }

    private final TextWatcher passwordCheckNewPassword = new CustomTextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            super.beforeTextChanged(s, start, count, after);
            TransformationMethod method = hasPasswordTransformation() ? new PasswordTransformationMethod() : null;
            fragmentChangePasswordBinding.tietPasswordNewReconfirm.setTransformationMethod(method);
            passwordCheck.beforeTextChanged(s, start, count, after);
        }

        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            passwordCheck.afterTextChanged(s);
        }
    };
    private final TextWatcher passwordCheck = new CustomTextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            super.beforeTextChanged(s, start, count, after);
            fragmentChangePasswordBinding.tvError.setVisibility(View.GONE);
        }

        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            String pass1 = Objects.requireNonNull(fragmentChangePasswordBinding.tietPasswordOld.getText()).toString().trim();
            String pass2 = Objects.requireNonNull(fragmentChangePasswordBinding.tietPasswordNew.getText()).toString().trim();
            String pass3 = Objects.requireNonNull(fragmentChangePasswordBinding.tietPasswordNewReconfirm.getText()).toString().trim();
            boolean ret = !TextUtils.isEmpty(pass1) && !TextUtils.isEmpty(pass2) && !TextUtils.isEmpty(pass3);
            ret = ret
                    && FormatUtil.checkPassword(pass2) && FormatUtil.checkPassword(pass3);
            fragmentChangePasswordBinding.btnConfirm.setEnabled(ret);
        }
    };
    private boolean hasPasswordTransformation() {
        EditText editText = fragmentChangePasswordBinding.tilPasswordNew.getEditText();
        return editText != null && editText.getTransformationMethod() instanceof PasswordTransformationMethod;
    }
}
