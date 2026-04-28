package com.jieli.healthaide.ui.mine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentBindNewEmailAddressBinding;
import com.jieli.healthaide.ui.login.EmailCodeFragment;
import com.jieli.healthaide.ui.widget.CustomTextWatcher;
import com.jieli.healthaide.util.FormatUtil;

/**
 * @ClassName: BindNewPhoneFragment
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/4/1 20:15
 */
public class BindNewEmailFragment extends EmailCodeFragment {
    private FragmentBindNewEmailAddressBinding fragmentBindNewPhoneNumberBinding;
    private UserLoginInfoViewModel userLoginInfoViewModel;
    private String changeMobile;
    public static final String MSG_CHANGE_EMAIL = "MSG_CHANGE_EMAIL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        fragmentBindNewPhoneNumberBinding = FragmentBindNewEmailAddressBinding.inflate(inflater, container, false);
        fragmentBindNewPhoneNumberBinding.clRegisterTopbar.tvTopbarTitle.setText(R.string.bind_new_email_address);
        fragmentBindNewPhoneNumberBinding.layoutSmsCode.etVerificationCode.setHint(R.string.input_identifying_code);
        fragmentBindNewPhoneNumberBinding.layoutSmsCode.etPhoneNumber.addTextChangedListener(loginBtnStatusListener);
        fragmentBindNewPhoneNumberBinding.layoutSmsCode.etVerificationCode.addTextChangedListener(loginBtnStatusListener);
        fragmentBindNewPhoneNumberBinding.btnConfirm.setOnClickListener(v -> {
            changeMobile = fragmentBindNewPhoneNumberBinding.layoutSmsCode.etPhoneNumber.getText().toString().trim();
            String code = fragmentBindNewPhoneNumberBinding.layoutSmsCode.etVerificationCode.getText().toString().trim();
            userLoginInfoViewModel.updateUserLoginEmail(changeMobile, code);
        });
        fragmentBindNewPhoneNumberBinding.clRegisterTopbar.tvTopbarLeft.setOnClickListener(v -> back());
        return fragmentBindNewPhoneNumberBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        userLoginInfoViewModel = new ViewModelProvider(this).get(UserLoginInfoViewModel.class);
        userLoginInfoViewModel.httpStateLiveData.observe(getViewLifecycleOwner(), integer -> {
            if (integer == UserLoginInfoViewModel.HTTP_STATE_UPDATED_FINISH) {
                back(300, () -> {
                    Intent intent = new Intent();
                    intent.putExtra(MSG_CHANGE_EMAIL, changeMobile);
                    requireActivity().setResult(Activity.RESULT_OK, intent);
                    showTips(R.string.bind_new_email_address_success);
                });
            }
        });
    }

    private final TextWatcher loginBtnStatusListener = new CustomTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            boolean ret = FormatUtil.checkEmailAddress(fragmentBindNewPhoneNumberBinding.layoutSmsCode.etPhoneNumber.getText().toString().trim())
                    && FormatUtil.checkEmailIdentifyCode(fragmentBindNewPhoneNumberBinding.layoutSmsCode.etVerificationCode.getText().toString().trim());
            fragmentBindNewPhoneNumberBinding.btnConfirm.setEnabled(ret);
        }
    };
}
