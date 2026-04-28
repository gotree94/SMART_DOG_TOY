package com.jieli.healthaide.ui.login;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentLoginByPasswordBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.home.HomeActivity;
import com.jieli.healthaide.ui.login.bean.LoginMsg;
import com.jieli.healthaide.ui.widget.AgreePrivacyPolicyView;
import com.jieli.healthaide.ui.widget.CustomTextWatcher;
import com.jieli.healthaide.util.FormatUtil;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.healthaide.util.PermissionUtil;
import com.jieli.jl_filebrowse.interfaces.OperatCallback;
import com.jieli.jl_rcsp.util.JL_Log;


/**
 * Des:
 * Author: Bob
 * Date:21-3-2
 * UpdateRemark:
 */
public class LoginByPasswordFragment extends BaseFragment {
    private FragmentLoginByPasswordBinding loginBinding;

    private LoginViewModel loginViewModel;
    private AgreePrivacyPolicyView agreePolicyView;

    private static final int REQUEST_CODE_PERMISSION = 1243;

    private final ActivityResultLauncher<Intent> registerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                JL_Log.d(tag, "registerLauncher", "intent : " + result.getData());
                if(!isFragmentValid() || result.getData() == null || agreePolicyView == null) return;
                boolean isAgree = result.getData().getBooleanExtra(HealthConstant.KEY_AGREE_PRIVACY_POLICY, false);
                agreePolicyView.setAgree(isAgree);
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        loginBinding = FragmentLoginByPasswordBinding.inflate(inflater, container, false);
        agreePolicyView = new AgreePrivacyPolicyView(requireContext(), loginBinding.viewAgreePolicy);
        return loginBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        addObserver();
        initUI();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            String permission = Manifest.permission.READ_PHONE_STATE;
            if (!PermissionUtil.isHasPermission(requireContext(), permission)
                /*&& !PermissionUtil.checkPermissionShouldShowDialog(requireActivity(), permission)*/) {
                loginViewModel.setBanRequestPermission(permission);
            }
            login();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            syncAgreeState();
        }
    }

    private final TextWatcher usernameTextWatcher = new CustomTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            if (null == s) return;
            String text = s.toString();
            boolean isUsernameInputting = FormatUtil.checkPhoneNumber(text) || FormatUtil.checkEmailAddress(text);
            loginViewModel.setCacheMobile(text.trim());

            if (isUsernameInputting) {
                showError(loginBinding.tilUsername, null);
            } else if (text.isEmpty()) {
                showError(loginBinding.tilUsername, getString(R.string.phone_or_email_tips_empty));
            } else {
                showError(loginBinding.tilUsername, getString(R.string.phone_or_email_tips_format_err));
            }
            updateLoginState();
        }
    };

    private final TextWatcher passwordTextWatcher = new CustomTextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
            if (null == s) return;
            String text = s.toString();
            boolean isPasswordInputting = checkPassword(text);
            if (isPasswordInputting) {
                showError(loginBinding.tilPassword, null);
            } else if (text.isEmpty()) {
                showError(loginBinding.tilPassword, getString(R.string.password_tips_empty));
            } else {
                showError(loginBinding.tilPassword, getString(R.string.password_tips_format_err));
            }
            updateLoginState();
        }
    };


    private void initUI() {
        loginBinding.tietUsername.addTextChangedListener(usernameTextWatcher);
        loginBinding.tietPassword.addTextChangedListener(passwordTextWatcher);
        loginBinding.btnLogin.setOnClickListener(v -> login());
        loginBinding.btnRegister.setOnClickListener(v -> toRegisterFragment());
        loginBinding.tvLoginByCode.setOnClickListener(v -> toLoginByCode());
        loginBinding.tvForgotPassword.setOnClickListener(v -> toResetPassword());
        loginBinding.tietUsername.setText(loginViewModel.getCacheInputNumber());

        syncAgreeState();
        updateLoginState();
    }

    private void addObserver() {
        loginViewModel.loginMsgMutableLiveData.observe(getViewLifecycleOwner(), loginMsg -> {
            switch (loginMsg.getState()) {
                case LoginMsg.STATE_LOGINING:
                    showWaitDialog();
                    break;
                case LoginMsg.STATE_LOGIN_FINISH:
                    toHomeActivity();
                case LoginMsg.STATE_IDLE:
                case LoginMsg.STATE_LOGIN_ERROR:
                    dismissWaitDialog();
                    break;
            }
        });
    }

    private boolean isAgree() {
        if (null == agreePolicyView) return false;
        return agreePolicyView.isAgree();
    }

    private void syncAgreeState() {
        Bundle bundle = getArguments();
        boolean isAgree = false;
        if (null != bundle) {
            isAgree = bundle.getBoolean(HealthConstant.KEY_AGREE_PRIVACY_POLICY, false);
        }
        agreePolicyView.setAgree(isAgree);
    }

    private String getTextFromView(EditText editText) {
        if (null == editText) return "";
        Editable editable = editText.getText();
        if (null == editable) return "";
        return editable.toString().trim();
    }

    private void updateLoginState() {
        boolean ret = (FormatUtil.checkPhoneNumber(getTextFromView(loginBinding.tietUsername))
                || FormatUtil.checkEmailAddress(getTextFromView(loginBinding.tietUsername)))
                && checkPassword(getTextFromView(loginBinding.tietPassword));
        loginBinding.btnLogin.setEnabled(ret);
    }

    private void showError(TextInputLayout textInputLayout, String error) {
        if (null == textInputLayout) return;
        textInputLayout.setError(error);
    }

    private boolean checkPassword(String password) {
        if (null == password) return false;
        return password.length() >= 6 && password.length() <= 12;
    }

    private void login() {
        boolean isAgree = isAgree();
        if (!isAgree) {
            showTips(getString(R.string.check_policy_status_tips));
            return;
        }
        loginViewModel.loginByAccount(getTextFromView(loginBinding.tietUsername), getTextFromView(loginBinding.tietPassword));
    }

    private void toLoginByCode() {
        BaseActivity baseActivity = (BaseActivity) requireActivity();
        Bundle bundle = new Bundle();
        bundle.putBoolean(HealthConstant.KEY_AGREE_PRIVACY_POLICY, isAgree());
        baseActivity.replaceFragment(R.id.launcher_container, LoginByCodeFragment.class.getCanonicalName(), bundle);
    }

    private void toResetPassword() {
        ContentActivity.startContentActivity(requireContext(), ResetPasswordFragment.class.getCanonicalName());
    }

    private void toRegisterFragment() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(HealthConstant.KEY_AGREE_PRIVACY_POLICY, isAgree());
        ContentActivity.startContentActivityForResult(this, RegisterFragment.class.getCanonicalName(), bundle, registerLauncher);
    }

    private void toHomeActivity() {
        HealthApplication.getAppViewModel().requestProfile(new OperatCallback() {
            @Override
            public void onSuccess() {
                startActivity(new Intent(requireActivity(), HomeActivity.class));
                requireActivity().finish();
            }

            @Override
            public void onError(int code) {
                showTips(getString(R.string.save_failed));
            }
        });
    }
}
