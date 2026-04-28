package com.jieli.healthaide.ui.home;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.tool.config.ConfigHelper;
import com.jieli.jl_filebrowse.interfaces.OperatCallback;
import com.jieli.jl_health_http.HttpClient;
import com.jieli.jl_health_http.model.response.LoginResponse;
import com.jieli.jl_health_http.util.NetworkUtil;
import com.jieli.jl_rcsp.util.JL_Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/22/21 9:37 AM
 * @desc :
 */
public class LauncherViewModel extends ViewModel {
    private final ConfigHelper mConfigHelper = ConfigHelper.getInstance();
    MutableLiveData<Boolean> tokenStateLiveData = new MutableLiveData<>();

    public boolean isAgreePolicy() {
        return mConfigHelper.isAgreePolicy();
    }

    public void setAgreePolicy(boolean isAgree) {
        mConfigHelper.setAgreePolicy(isAgree);
    }

    public void refreshToken() {

        String token = HttpClient.getToken();
        //没有登录信息，直接结束
        if (TextUtils.isEmpty(token)) {
            JL_Log.e("LauncherViewModel", "refreshToken", "no token");
            tokenStateLiveData.postValue(false);
            return;
        }
        if (!NetworkUtil.checkNetWorkConnected()) {
            //无网络
            tokenStateLiveData.postValue(true);
            return;
        }

        HttpClient.createUserApi().refreshToken().enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                /*if (response.isSuccessful() && response.body().getCode() == HttpConstant.HTTP_OK) {
                    requestProfile();
                } else {
                    requestProfile();
//                    tokenStateLiveData.postValue(false);
                }*/
                requestProfile();
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                tokenStateLiveData.postValue(false);
            }
        });
    }

    private void requestProfile() {
        HealthApplication.getAppViewModel().requestProfile(new OperatCallback() {
            @Override
            public void onSuccess() {
                tokenStateLiveData.postValue(true);
            }

            @Override
            public void onError(int code) {
                //当无网络时，会使用缓存，如果使用缓存也是返回错误，可认为数据异常，返回登录页面
                tokenStateLiveData.postValue(false);
            }
        });

    }
}
