package com.jieli.healthaide.ui.login;

import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jieli.component.utils.ToastUtil;
import com.jieli.healthaide.ui.login.bean.LoginMsg;
import com.jieli.healthaide.ui.login.bean.SmsCounter;
import com.jieli.healthaide.util.HttpErrorUtil;
import com.jieli.jl_health_http.HttpClient;
import com.jieli.jl_health_http.HttpConstant;
import com.jieli.jl_health_http.model.response.BooleanResponse;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/9/21 1:45 PM
 * @desc :
 */
public class EmailCodeViewModel extends ViewModel {
    private final String tag = EmailCodeViewModel.class.getSimpleName();
    public static final int CHECK_FAIL = 0;
    public static final int CHECK_SUCCESS = 1;
    public MutableLiveData<SmsCounter> emailCounterMutableLiveData = new MutableLiveData<>(new SmsCounter());
    public MutableLiveData<Integer> emailCodeCheckResultLiveData = new MutableLiveData<>();
    private CountDownTimer mCountDownTimer;

    public void sendEmailCode(String email) {
        if (Objects.requireNonNull(emailCounterMutableLiveData.getValue()).getOp() != SmsCounter.OP_IDLE) {
            return;
        }
        getEmailCode(email);
    }

    public void checkEmailCode(String email, String code) {
        HttpClient.createUserApi().checkEmailCode(email, code).enqueue(new Callback<BooleanResponse>() {
            @Override
            public void onResponse(@NonNull Call<BooleanResponse> call, @NonNull Response<BooleanResponse> response) {
                if (response.code() != 200) {
//                    onFailure(call, new RuntimeException("http code error:" + response.code()));
                    HttpErrorUtil.showErrorToast(response.code());
                    return;
                }
                BooleanResponse booleanResponse = response.body();
                if (Objects.requireNonNull(booleanResponse).getCode() != HttpConstant.HTTP_OK) {
                    onFailure(call, new RuntimeException(Objects.requireNonNull(response.body()).getMsg()));
                } else {
                    //验证码校验成功才真正去登录
                    emailCodeCheckResultLiveData.postValue(CHECK_SUCCESS);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BooleanResponse> call, Throwable t) {
                ToastUtil.showToastShort(t.getMessage());
                LoginMsg errMsg = new LoginMsg(LoginMsg.STATE_LOGIN_ERROR, "logining error");
            }
        });
    }

    private void getEmailCode(String email) {
        SmsCounter counter = new SmsCounter(SmsCounter.OP_SEND_CODE, 0);
        emailCounterMutableLiveData.postValue(counter);
        HttpClient.createUserApi().getEmailCode(email).enqueue(new Callback<BooleanResponse>() {
            @Override
            public void onResponse(@NonNull Call<BooleanResponse> call, @NonNull Response<BooleanResponse> response) {
                if (response.code() != 200) {
                    onFailure(call, new RuntimeException("http状态码异常"));
                } else if (Objects.requireNonNull(response.body()).getCode() != 0) {
                    onFailure(call, new RuntimeException(response.body().getMsg()));
                } else {
                    SmsCounter counter = new SmsCounter(SmsCounter.OP_SEND_CODE_FINISH, 0);
                    emailCounterMutableLiveData.setValue(counter);
                    startCountDownActual();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BooleanResponse> call, Throwable t) {
                ToastUtil.showToastShort(t.getMessage());
                SmsCounter counter = new SmsCounter(SmsCounter.OP_SEND_CODE_ERROR, 0);
                emailCounterMutableLiveData.setValue(counter);

                SmsCounter idleCounter = new SmsCounter(SmsCounter.OP_IDLE, 0);
                emailCounterMutableLiveData.postValue(idleCounter);
            }
        });

    }


    private void startCountDownActual() {
        //发送sms code
        int time = 120; //服务器每2分钟可以请求一次验证码
        SmsCounter counter = new SmsCounter(SmsCounter.OP_COUNTER, time);
        emailCounterMutableLiveData.postValue(counter);
        mCountDownTimer = new CountDownTimer(time * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                SmsCounter counter = new SmsCounter(SmsCounter.OP_COUNTER, (int) (millisUntilFinished / 1000));
                emailCounterMutableLiveData.postValue(counter);
            }

            @Override
            public void onFinish() {
                JL_Log.i(tag, "onFinish", "");
                SmsCounter counter = new SmsCounter(SmsCounter.OP_IDLE, 0);
                emailCounterMutableLiveData.postValue(counter);
                mCountDownTimer = null;
            }
        };
        mCountDownTimer.start();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }
}
