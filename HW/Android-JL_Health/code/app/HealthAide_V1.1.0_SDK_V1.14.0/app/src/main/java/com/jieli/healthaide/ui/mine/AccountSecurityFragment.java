package com.jieli.healthaide.ui.mine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentAccountSecurityBinding;
import com.jieli.healthaide.tool.config.ConfigHelper;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.dialog.DeleteAccountDialog;
import com.jieli.healthaide.ui.dialog.ModifyPhoneNumberDialog;
import com.jieli.healthaide.ui.dialog.UserAuthenticationDialog;
import com.jieli.healthaide.util.phone.PhoneUtil;
import com.jieli.jl_health_http.model.UserLoginInfo;

import java.util.Objects;

/**
 * @ClassName: AccountSecurityFragment
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/4/1 10:08
 */
public class AccountSecurityFragment extends BaseFragment {
    private FragmentAccountSecurityBinding fragmentAccountSecurityBinding;
    private UserLoginInfoViewModel userLoginInfoViewModel;
    //private SmsCodeViewModel smsCodeViewModel;
    public static int KEY_USER_LOGIN_INFO_VIEW_MODEL = 100;
    private final int loginType = ConfigHelper.getInstance().getLoginType();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        fragmentAccountSecurityBinding = FragmentAccountSecurityBinding.inflate(inflater, container, false);
        fragmentAccountSecurityBinding.layoutModifyPhoneNumber.getRoot().setVisibility(loginType == 0 ? View.VISIBLE : View.GONE);
        fragmentAccountSecurityBinding.layoutModifyEmailAddress.getRoot().setVisibility(loginType == 1 ? View.VISIBLE : View.GONE);
        fragmentAccountSecurityBinding.layoutTopbar.tvTopbarTitle.setText(R.string.account_security);
        fragmentAccountSecurityBinding.layoutModifyPhoneNumber.tvSettingTarget2.setText(R.string.modify_phone_number);
        fragmentAccountSecurityBinding.layoutModifyEmailAddress.tvSettingTarget2.setText(R.string.modify_email_address);
        fragmentAccountSecurityBinding.layoutChangePassword.tvSettingTarget2.setText(R.string.change_password);
        initOnClick();
        fragmentAccountSecurityBinding.layoutTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        return fragmentAccountSecurityBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        userLoginInfoViewModel = new ViewModelProvider(this).get(UserLoginInfoViewModel.class);
        userLoginInfoViewModel.userLoginInfoLiveData.observe(getViewLifecycleOwner(), this::updateUserLoginInfoView);
        userLoginInfoViewModel.httpStateLiveData.observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case UserInfoViewModel.HTTP_STATE_REQUESTING:
                case UserInfoViewModel.HTTP_STATE_UPDATING:
                    showWaitDialog();
                    break;
                default:
                    dismissWaitDialog();
                    break;
            }
        });
        userLoginInfoViewModel.getUserLoginInfo();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        String mobile = Objects.requireNonNull(data).getStringExtra(BindNewPhoneFragment.MSG_CHANGE_MOBILE);
        if (!TextUtils.isEmpty(mobile)) {
            userLoginInfoViewModel.updateMobileLocal(mobile);
        }
        String email = data.getStringExtra(BindNewEmailFragment.MSG_CHANGE_EMAIL);
        if (!TextUtils.isEmpty(email)) {
            userLoginInfoViewModel.updateEmailLocal(email);
        }
    }

    private void initOnClick() {
        fragmentAccountSecurityBinding.layoutModifyEmailAddress.getRoot().setOnClickListener(view -> showModifyPhoneNumberDialog());
        fragmentAccountSecurityBinding.layoutModifyPhoneNumber.getRoot().setOnClickListener(view -> showModifyPhoneNumberDialog());
        fragmentAccountSecurityBinding.layoutChangePassword.getRoot().setOnClickListener(view -> ContentActivity.startContentActivity(requireContext(), ChangePasswordFragment.class.getCanonicalName()));
        fragmentAccountSecurityBinding.tvCancelAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void updateUserLoginInfoView(UserLoginInfo userLoginInfo) {
        fragmentAccountSecurityBinding.layoutModifyPhoneNumber.tvHint.setText(userLoginInfo.getMobile());
        String email = userLoginInfo.getEmail();
        fragmentAccountSecurityBinding.layoutModifyEmailAddress.tvHint.setText(email);
    }

    private void showModifyPhoneNumberDialog() {
        ModifyPhoneNumberDialog modifyPhoneNumberDialog = new ModifyPhoneNumberDialog();
        modifyPhoneNumberDialog.setModifyType(loginType);
        modifyPhoneNumberDialog.setCancelable(true);
        String number = "";
        if (loginType == 0) {
            number = Objects.requireNonNull(userLoginInfoViewModel.userLoginInfoLiveData.getValue()).getMobile();
            int geo = PhoneUtil.getCountryCode(getContext(), number);
            number = "+" + geo + " " + number;
        } else {
            number = Objects.requireNonNull(userLoginInfoViewModel.userLoginInfoLiveData.getValue()).getEmail();
        }
        modifyPhoneNumberDialog.setCurrentPhoneNumber(number);
        modifyPhoneNumberDialog.setOnModifyPhoneNumberDialogListener(new ModifyPhoneNumberDialog.OnModifyPhoneNumberDialogListener() {
            @Override
            public void onChange() {
                showUserAuthenticationDialog();
                modifyPhoneNumberDialog.dismiss();
            }

            @Override
            public void onCancel() {
                modifyPhoneNumberDialog.dismiss();
            }
        });
        modifyPhoneNumberDialog.show(getChildFragmentManager(), ModifyPhoneNumberDialog.class.getCanonicalName());
    }

    private void showUserAuthenticationDialog() {
        UserAuthenticationDialog userAuthenticationDialog = new UserAuthenticationDialog();
        userAuthenticationDialog.setAuthenticationType(loginType);
        userAuthenticationDialog.setCancelable(true);
        String number = "";
        if (loginType == 0) {
            number = Objects.requireNonNull(userLoginInfoViewModel.userLoginInfoLiveData.getValue()).getMobile();
        } else {
            number = Objects.requireNonNull(userLoginInfoViewModel.userLoginInfoLiveData.getValue()).getEmail();
        }
        userAuthenticationDialog.setCurrentPhoneNumber(number);
        userAuthenticationDialog.setOnListener(new UserAuthenticationDialog.OnListener() {
            @Override
            public void onSendSmsCode() {
            }

            @Override
            public void onChange() {

            }

            @Override
            public void onCancel() {
                userAuthenticationDialog.dismiss();
            }

            @Override
            public void onSending() {
                showWaitDialog();
            }

            @Override
            public void onSendFinish() {
                dismissWaitDialog();
            }

            @Override
            public void onSendError() {
                dismissWaitDialog();
            }

            @Override
            public void onCheckSmsCodeSuccess() {
                userAuthenticationDialog.dismiss();
                if (loginType == 0) {
                    ContentActivity.startContentActivityForResult(AccountSecurityFragment.this, BindNewPhoneFragment.class.getCanonicalName(), null, KEY_USER_LOGIN_INFO_VIEW_MODEL);
                } else {
                    ContentActivity.startContentActivityForResult(AccountSecurityFragment.this, BindNewEmailFragment.class.getCanonicalName(), null, KEY_USER_LOGIN_INFO_VIEW_MODEL);
                }
            }
        });
        userAuthenticationDialog.show(getChildFragmentManager(), ModifyPhoneNumberDialog.class.getCanonicalName());
    }

    private void showDeleteAccountDialog() {
        DeleteAccountDialog deleteAccountDialog = new DeleteAccountDialog();
        deleteAccountDialog.setOnDeleteAccountrDialogListener(new DeleteAccountDialog.OnDeleteAccountrDialogListener() {
            @Override
            public void onConfirm() {
                userLoginInfoViewModel.deleteAccount(requireActivity());
                deleteAccountDialog.dismiss();
            }

            @Override
            public void onCancel() {
                deleteAccountDialog.dismiss();
            }
        });
        deleteAccountDialog.show(getChildFragmentManager(), DeleteAccountDialog.class.getCanonicalName());
    }
}
