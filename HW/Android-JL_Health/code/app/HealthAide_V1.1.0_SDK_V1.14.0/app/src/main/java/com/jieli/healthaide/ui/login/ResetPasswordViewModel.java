package com.jieli.healthaide.ui.login;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jieli.component.utils.ToastUtil;
import com.jieli.jl_health_http.HttpClient;
import com.jieli.jl_health_http.model.response.BooleanResponse;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordViewModel extends ViewModel {

    public static final int STATE_INPUT_SMS_CODE = 0;
    public static final int STATE_CHECK_SMS_CODE = 1;
    public static final int STATE_INPUT_PASSWORD = 2;
    public static final int STATE_RESET_PASSWORD = 3;
    public static final int STATE_RESET_PASSWORD_FINISH = 4;

    public MutableLiveData<Integer> stateLiveData = new MutableLiveData<>(STATE_INPUT_SMS_CODE);

    public void checkSmsCode(String mobile, String code) {
        stateLiveData.postValue(STATE_CHECK_SMS_CODE);
        HttpClient.createUserApi().checkSmsCode(mobile, code).enqueue(new Callback<BooleanResponse>() {
            @Override
            public void onResponse(@NonNull Call<BooleanResponse> call, @NonNull Response<BooleanResponse> response) {
                if (response.code() != 200) {
                    onFailure(call, new RuntimeException("http状态码异常"));
                } else if (Objects.requireNonNull(response.body()).getCode() != 0) {
                    onFailure(call, new RuntimeException(response.body().getMsg()));
                } else {
                    stateLiveData.postValue(STATE_INPUT_PASSWORD);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BooleanResponse> call, Throwable t) {
                ToastUtil.showToastShort(t.getMessage());
                stateLiveData.postValue(STATE_INPUT_SMS_CODE);
            }
        });

    }

    public void resetPassword(String mobile, String password, String code) {
        stateLiveData.postValue(STATE_RESET_PASSWORD);
        HttpClient.createUserApi().resetpassword(mobile, password, code).enqueue(new Callback<BooleanResponse>() {
            @Override
            public void onResponse(@NonNull Call<BooleanResponse> call, @NonNull Response<BooleanResponse> response) {
                if (response.code() != 200) {
                    onFailure(call, new RuntimeException("http状态码异常"));
                } else if (Objects.requireNonNull(response.body()).getCode() != 0) {
                    onFailure(call, new RuntimeException(response.body().getMsg()));
                } else {
                    stateLiveData.postValue(STATE_RESET_PASSWORD_FINISH);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BooleanResponse> call, Throwable t) {
                ToastUtil.showToastShort(t.getMessage());
                stateLiveData.postValue(STATE_INPUT_PASSWORD);
            }
        });

    }

    public void resetPassword(String oldPassword, String newPassword) {
        HttpClient.createUserApi().updatePassword(oldPassword, newPassword).enqueue(new Callback<BooleanResponse>() {
            @Override
            public void onResponse(@NonNull Call<BooleanResponse> call, @NonNull Response<BooleanResponse> response) {
                if (response.code() != 200) {
                    onFailure(call, new RuntimeException("http状态码异常"));
                } else if (Objects.requireNonNull(response.body()).getCode() != 0) {
                    onFailure(call, new RuntimeException(response.body().getMsg()));
                } else {
                    stateLiveData.postValue(STATE_RESET_PASSWORD_FINISH);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BooleanResponse> call, Throwable t) {
                ToastUtil.showToastShort(t.getMessage());
                stateLiveData.postValue(STATE_INPUT_PASSWORD);
            }
        });
    }
}