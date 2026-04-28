package com.jieli.healthaide.ui.login;

import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jieli.component.utils.ToastUtil;
import com.jieli.healthaide.tool.http.base.BaseHttpResultHandler;
import com.jieli.healthaide.ui.login.bean.SmsCounter;
import com.jieli.jl_health_http.HttpClient;
import com.jieli.jl_health_http.model.ImageInfo;
import com.jieli.jl_health_http.tool.OnResultCallback;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/9/21 1:45 PM
 * @desc : 发送验证码逻辑实现
 */
public class SendCodeViewModel extends ViewModel {
    private final String tag = SendCodeViewModel.class.getSimpleName();
    public static final int CHECK_FAIL = 0;
    public static final int CHECK_SUCCESS = 1;
    public MutableLiveData<SmsCounter> codeCounterMutableLiveData = new MutableLiveData<>(new SmsCounter());
    public MutableLiveData<Integer> codeCheckResultLiveData = new MutableLiveData<>();
    private CountDownTimer mCountDownTimer;

    public void sendSmsCode(boolean isRegister, String phoneNumber, ImageInfo imageInfo) {
        final SmsCounter smsCounter = codeCounterMutableLiveData.getValue();
        final int op = null == smsCounter ? SmsCounter.OP_IDLE : smsCounter.getOp();
        if (op != SmsCounter.OP_IDLE) {
            return;
        }
        getSmsCode(isRegister, phoneNumber, imageInfo);
    }

    public void checkSmsCode(String mobile, String code) {
        HttpClient.createUserApi().checkSmsCode(mobile, code).enqueue(new BaseHttpResultHandler<>(true,
                new OnResultCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        //验证码校验成功才真正去登录
                        codeCheckResultLiveData.postValue(CHECK_SUCCESS);
                    }

                    @Override
                    public void onError(int code, String message) {
//                        ToastUtil.showToastLong(message);
                        codeCheckResultLiveData.postValue(CHECK_FAIL);
                    }
                }));
    }

    public void sendEmailCode(boolean isRegister, String emailAddress) {
        final SmsCounter smsCounter = codeCounterMutableLiveData.getValue();
        final int op = null == smsCounter ? SmsCounter.OP_IDLE : smsCounter.getOp();
        if (op != SmsCounter.OP_IDLE) {
            return;
        }
        getEmailCode(isRegister, emailAddress);
    }

    public void checkEmailCode(String email, String code) {
        HttpClient.createUserApi().checkEmailCode(email, code).enqueue(new BaseHttpResultHandler<>(true,
                new OnResultCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        //验证码校验成功才真正去登录
                        codeCheckResultLiveData.postValue(CHECK_SUCCESS);
                    }

                    @Override
                    public void onError(int code, String message) {
                        codeCheckResultLiveData.postValue(CHECK_FAIL);
                    }
                }));
    }

    private void getSmsCode(boolean isRegister, String mobile, ImageInfo imageInfo) {
        SmsCounter counter = new SmsCounter(SmsCounter.OP_SEND_CODE, 0);
        codeCounterMutableLiveData.postValue(counter);
        if (isRegister) {
            if (null == imageInfo) {
                getRegisterSmsCode(mobile);
            } else {
                getRegisterSmsCode(mobile, imageInfo);
            }
        } else {
            if (null == imageInfo) {
                getSmsCode(mobile);
            } else {
                getSmsCode(mobile, imageInfo);
            }
        }
    }

    private void getEmailCode(boolean isRegister, String email) {
        SmsCounter counter = new SmsCounter(SmsCounter.OP_SEND_CODE, 0);
        codeCounterMutableLiveData.postValue(counter);
        if (isRegister) {
            getRegisterEmailCode(email);
        } else {
            getEmailCode(email);
        }
    }


    private void startCountDownActual() {
        //发送sms code
        int time = 120; //服务器每2分钟可以请求一次验证码
        SmsCounter counter = new SmsCounter(SmsCounter.OP_COUNTER, time);
        codeCounterMutableLiveData.postValue(counter);
        mCountDownTimer = new CountDownTimer(time * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                SmsCounter counter = new SmsCounter(SmsCounter.OP_COUNTER, (int) (millisUntilFinished / 1000));
                codeCounterMutableLiveData.postValue(counter);
            }

            @Override
            public void onFinish() {
                JL_Log.i(tag, "onFinish", "");
                SmsCounter counter = new SmsCounter(SmsCounter.OP_IDLE, 0);
                codeCounterMutableLiveData.postValue(counter);
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

    private void onSendSmsCodeSuccess() {
        SmsCounter counter = new SmsCounter(SmsCounter.OP_SEND_CODE_FINISH, 0);
        codeCounterMutableLiveData.setValue(counter);
        startCountDownActual();
    }

    private void onSendSmsCodeFailed(int code, String message) {
        ToastUtil.showToastShort(message);
        SmsCounter counter = new SmsCounter(SmsCounter.OP_SEND_CODE_ERROR, 0);
        codeCounterMutableLiveData.setValue(counter);

        SmsCounter idleCounter = new SmsCounter(SmsCounter.OP_IDLE, 0);
        codeCounterMutableLiveData.postValue(idleCounter);
    }

    @Deprecated
    private void getSmsCode(String mobile) {
        HttpClient.createUserApi().getSmsCode(mobile).enqueue(new BaseHttpResultHandler<>(sendCodeCallback));
    }

    @Deprecated
    private void getRegisterSmsCode(String mobile) {
        HttpClient.createUserApi().getRegisterSmsCode(mobile).enqueue(new BaseHttpResultHandler<>(sendCodeCallback));
    }

    private void getSmsCode(String mobile, @NonNull ImageInfo info) {
        HttpClient.createUserApi().getSmsCodeWithCheck(mobile, info.getCode(), info.getValue())
                .enqueue(new BaseHttpResultHandler<>(sendCodeCallback));
    }

    private void getRegisterSmsCode(String mobile, @NonNull ImageInfo info) {
        HttpClient.createUserApi().getRegisterSmsCodeWithCheck(mobile, info.getCode(), info.getValue())
                .enqueue(new BaseHttpResultHandler<>(sendCodeCallback));
    }

    private void getEmailCode(String emailAddress) {
        HttpClient.createUserApi().getEmailCode(emailAddress).enqueue(new BaseHttpResultHandler<>(sendCodeCallback));
    }

    private void getRegisterEmailCode(String emailAddress) {
        HttpClient.createUserApi().getRegisterEmailCode(emailAddress).enqueue(new BaseHttpResultHandler<>(sendCodeCallback));
    }

    private final OnResultCallback<Boolean> sendCodeCallback = new OnResultCallback<Boolean>() {
        @Override
        public void onResult(Boolean result) {
            onSendSmsCodeSuccess();
        }

        @Override
        public void onError(int code, String message) {
            onSendSmsCodeFailed(code, message);
        }
    };
}
