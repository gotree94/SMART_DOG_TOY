package com.jieli.healthaide.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentLoginByCodeBinding;
import com.jieli.healthaide.tool.config.ConfigHelper;
import com.jieli.healthaide.ui.base.BaseActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.home.HomeActivity;
import com.jieli.healthaide.ui.login.bean.LoginMsg;
import com.jieli.healthaide.ui.widget.AgreePrivacyPolicyView;
import com.jieli.healthaide.ui.widget.SendCodeView;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.jl_filebrowse.interfaces.OperatCallback;
import com.jieli.jl_health_http.model.ImageInfo;


/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/9/21 4:10 PM
 * @desc : 验证码登录
 */
public class LoginByCodeFragment extends BaseFragment {
    private FragmentLoginByCodeBinding binding;
    private SendCodeViewModel sendCodeViewModel;
    private LoginViewModel loginViewModel;

    private SendCodeView sendCodeView;
    private AgreePrivacyPolicyView agreePrivacyPolicyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginByCodeBinding.inflate(inflater, container, false);
        sendCodeView = new SendCodeView(this, binding.viewSendCode, SendCodeView.WAY_AUTO,
                new SendCodeView.OnSendCodeListener() {
                    @Override
                    public void sendCode(String account, int way, ImageInfo imageInfo) {
                        if (null == sendCodeViewModel) return;
                        if (way == SendCodeView.WAY_EMAIL_CODE) {
                            sendCodeViewModel.sendEmailCode(false, account);
                            return;
                        }
                        sendCodeViewModel.sendSmsCode(false, account, imageInfo);
                    }

                    @Override
                    public void onLoginReady(boolean isReady) {
                        ConfigHelper.getInstance().setCacheAccount(sendCodeView.getAccount());
                        binding.btnLogin.setEnabled(isReady);
                    }
                });
        agreePrivacyPolicyView = new AgreePrivacyPolicyView(requireContext(), binding.viewAgreePolicy);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sendCodeViewModel = new ViewModelProvider(this).get(SendCodeViewModel.class);
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        initUI();
        addObserver();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            setCustomBackPress();
            syncAgreeState();
        }
    }

    private void initUI() {
        binding.tvLoginByPassword.setOnClickListener(v -> toLoginByPassword());
        binding.btnLogin.setOnClickListener(v -> login());
        sendCodeView.setAccount(loginViewModel.getCacheInputNumber());
        binding.btnLogin.setEnabled(sendCodeView.isReadyOk());

        setCustomBackPress();
        syncAgreeState();
    }

    private void addObserver() {
        sendCodeViewModel.codeCounterMutableLiveData.observe(getViewLifecycleOwner(), state -> sendCodeView.updateStateUI(state));
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
        if (null == agreePrivacyPolicyView) return false;
        return agreePrivacyPolicyView.isAgree();
    }

    private void syncAgreeState(){
        Bundle bundle = getArguments();
        boolean isAgree = false;
        if (null != bundle) {
            isAgree = bundle.getBoolean(HealthConstant.KEY_AGREE_PRIVACY_POLICY, false);
        }
        agreePrivacyPolicyView.setAgree(isAgree);
    }

    private void setCustomBackPress(){
        final BaseActivity activity = (BaseActivity) requireActivity();
        activity.setOnBackPressIntercept(() -> {
            toLoginByPassword();
            return true;
        });
    }

    private void login() {
        if (null == sendCodeView) return;
        boolean isAgree = isAgree();
        if (!isAgree) {
            showTips(getString(R.string.check_policy_status_tips));
            return;
        }
        loginViewModel.loginByCode(sendCodeView.getAccount(), sendCodeView.getCode());
    }

    private void toLoginByPassword() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(HealthConstant.KEY_AGREE_PRIVACY_POLICY, isAgree());
        BaseActivity baseActivity = (BaseActivity) requireActivity();
        baseActivity.setOnBackPressIntercept(null);
        baseActivity.replaceFragment(R.id.launcher_container, LoginByPasswordFragment.class.getCanonicalName(), bundle);
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
