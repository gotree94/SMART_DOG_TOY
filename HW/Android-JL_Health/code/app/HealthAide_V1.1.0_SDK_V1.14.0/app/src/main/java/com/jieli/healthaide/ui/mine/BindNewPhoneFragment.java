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
import com.jieli.healthaide.databinding.FragmentBindNewPhoneNumberBinding;
import com.jieli.healthaide.ui.login.SmsCodeFragment;
import com.jieli.healthaide.ui.widget.CustomTextWatcher;
import com.jieli.healthaide.util.FormatUtil;

/**
 * @ClassName: BindNewPhoneFragment
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/4/1 20:15
 */
public class BindNewPhoneFragment extends SmsCodeFragment {
    private FragmentBindNewPhoneNumberBinding fragmentBindNewPhoneNumberBinding;
    private UserLoginInfoViewModel userLoginInfoViewModel;
    private String changeMobile;
    public static final String MSG_CHANGE_MOBILE = "MSG_CHANGE_MOBILE";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        fragmentBindNewPhoneNumberBinding = FragmentBindNewPhoneNumberBinding.inflate(inflater, container, false);
        fragmentBindNewPhoneNumberBinding.clRegisterTopbar.tvTopbarTitle.setText(R.string.bind_new_phone_number);
        fragmentBindNewPhoneNumberBinding.layoutSmsCode.etPhoneNumber.addTextChangedListener(loginBtnStatusListener);
        fragmentBindNewPhoneNumberBinding.layoutSmsCode.etVerificationCode.addTextChangedListener(loginBtnStatusListener);
        fragmentBindNewPhoneNumberBinding.btnConfirm.setOnClickListener(v -> {
            changeMobile = fragmentBindNewPhoneNumberBinding.layoutSmsCode.etPhoneNumber.getText().toString().trim();
            String code = fragmentBindNewPhoneNumberBinding.layoutSmsCode.etVerificationCode.getText().toString().trim();
            userLoginInfoViewModel.updateUserLoginMobile(changeMobile, code);
        });
        fragmentBindNewPhoneNumberBinding.clRegisterTopbar.tvTopbarLeft.setOnClickListener(v -> back());
        return fragmentBindNewPhoneNumberBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userLoginInfoViewModel = new ViewModelProvider(this).get(UserLoginInfoViewModel.class);
        userLoginInfoViewModel.httpStateLiveData.observe(getViewLifecycleOwner(), integer -> {
            if (integer == UserLoginInfoViewModel.HTTP_STATE_UPDATED_FINISH) {
                back(300, () -> {
                    Intent intent = new Intent();
                    intent.putExtra(MSG_CHANGE_MOBILE, changeMobile);
                    requireActivity().setResult(Activity.RESULT_OK, intent);
                    showTips(R.string.bind_new_phone_number_success);
                });
            }
        });
    }

    @Override
    public boolean isRegisterUser() {
        return false;
    }

    private final TextWatcher loginBtnStatusListener = new CustomTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            boolean ret = FormatUtil.checkPhoneNumber(fragmentBindNewPhoneNumberBinding.layoutSmsCode.etPhoneNumber.getText().toString().trim())
                    && FormatUtil.checkSmsCode(fragmentBindNewPhoneNumberBinding.layoutSmsCode.etVerificationCode.getText().toString().trim());
            fragmentBindNewPhoneNumberBinding.btnConfirm.setEnabled(ret);
        }
    };
}
