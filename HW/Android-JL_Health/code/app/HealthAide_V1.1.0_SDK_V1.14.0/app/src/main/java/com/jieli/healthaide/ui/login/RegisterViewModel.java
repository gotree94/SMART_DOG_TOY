package com.jieli.healthaide.ui.login;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jieli.component.utils.ToastUtil;
import com.jieli.jl_health_http.HttpClient;
import com.jieli.jl_health_http.model.response.BooleanResponse;
import com.jieli.jl_health_http.model.response.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/9/21 4:10 PM
 * @desc :
 */
public class RegisterViewModel extends ViewModel {

    public boolean isHidPassword = true;

    public final static int OP_STATE_UNKNOWN = -1;
    public final static int OP_STATE_BUSY = 0;
    public final static int OP_STATE_FINISH = 1;
    public final static int OP_STATE_IDLE = 2;


    public MutableLiveData<Integer> opStateLiveData = new MutableLiveData<Integer>(OP_STATE_UNKNOWN);


    public void register(String mobil, String password, String code) {
        opStateLiveData.postValue(OP_STATE_BUSY);
        checkSmsCode(mobil, password, code);//先校验smsCode是否正确
    }


    private void checkSmsCode(String mobile, String password, String code) {
        HttpClient.createUserApi().checkSmsCode(mobile, code).enqueue(new Callback<BooleanResponse>() {
            @Override
            public void onResponse(Call<BooleanResponse> call, Response<BooleanResponse> response) {
                if (response.code() != 200) {
                    onFailure(call, new RuntimeException("http状态码异常"));
                } else if (response.body().getCode() != 0) {
                    onFailure(call, new RuntimeException(response.body().getMsg()));
                } else {
                    registerActual(mobile, password, code);
                }
            }

            @Override
            public void onFailure(Call<BooleanResponse> call, Throwable t) {
                ToastUtil.showToastShort(t.getMessage());
                opStateLiveData.postValue(OP_STATE_IDLE);
            }
        });
    }


    private void registerActual(String mobile, String password, String code) {
        HttpClient.createUserApi().register(mobile, password, code).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.code() != 200) {
                    onFailure(call, new RuntimeException("http状态码异常"));
                } else if (response.body().getCode() != 0) {
                    onFailure(call, new RuntimeException(response.body().getMsg()));
                } else {
                    opStateLiveData.postValue(OP_STATE_FINISH);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                ToastUtil.showToastShort(t.getMessage());
                opStateLiveData.postValue(OP_STATE_IDLE);
            }
        });
    }


}