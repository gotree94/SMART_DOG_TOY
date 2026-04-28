package com.jieli.healthaide.ui.mine;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jieli.component.ActivityManager;
import com.jieli.component.utils.ToastUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.login.LoginActivity;
import com.jieli.healthaide.util.HttpErrorUtil;
import com.jieli.jl_health_http.HttpClient;
import com.jieli.jl_health_http.HttpConstant;
import com.jieli.jl_health_http.model.UserLoginInfo;
import com.jieli.jl_health_http.model.response.BooleanResponse;
import com.jieli.jl_health_http.model.response.UserLoginInfoResponse;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @ClassName: UserLoginInfoViewModel
 * @Description: 用户的登录信息
 * 对应http ：/health/v1/api/basic/user/profile
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/4/1 10:53
 */
public class UserLoginInfoViewModel extends ViewModel {
    //static final int HTTP_STATE_UPDATING = 0;
    static final int HTTP_STATE_UPDATED_FINISH = 1;
    static final int HTTP_STATE_UPDATED_ERROR = 2;
    static final int HTTP_STATE_REQUESTING = 3;
    static final int HTTP_STATE_REQUEST_FINISH = 4;
    static final int HTTP_STATE_REQUEST_ERROR = 5;
    MutableLiveData<UserLoginInfo> userLoginInfoLiveData = new MutableLiveData<>(new UserLoginInfo());
    MutableLiveData<Integer> httpStateLiveData = new MutableLiveData<>(-1);

    public void getUserLoginInfo() {
        httpStateLiveData.postValue(HTTP_STATE_REQUESTING);
        HttpClient.createUserApi().getUserLoginInfo().enqueue(new Callback<UserLoginInfoResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserLoginInfoResponse> call, @NonNull Response<UserLoginInfoResponse> response) {
                if (response.code() != 200) {
                    HttpErrorUtil.showErrorToast(response.code());
                    httpStateLiveData.postValue(HTTP_STATE_UPDATED_ERROR);
                    return;
                }
                UserLoginInfoResponse userLoginInfoResponse = response.body();
                if (Objects.requireNonNull(userLoginInfoResponse).getCode() != HttpConstant.HTTP_OK) {
                    onFailure(call, new RuntimeException(userLoginInfoResponse.getMsg()));
                } else {
                    UserLoginInfo userLoginInfo = userLoginInfoResponse.getT();
                    userLoginInfoLiveData.postValue(userLoginInfo);
                    httpStateLiveData.postValue(HTTP_STATE_REQUEST_FINISH);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserLoginInfoResponse> call, Throwable t) {
                ToastUtil.showToastShort(t.getMessage());
                httpStateLiveData.postValue(HTTP_STATE_REQUEST_ERROR);
            }
        });
    }

    public void updateUserLoginMobile(String mobile, String code) {
        HttpClient.createUserApi().updateMobile(mobile, code).enqueue(new Callback<BooleanResponse>() {
            @Override
            public void onResponse(@NonNull Call<BooleanResponse> call, @NonNull Response<BooleanResponse> response) {
                if (response.code() != 200) {
                    HttpErrorUtil.showErrorToast(response.code());
                    httpStateLiveData.postValue(HTTP_STATE_UPDATED_ERROR);
                    return;
                }
                BooleanResponse booleanResponse = response.body();
                if (Objects.requireNonNull(booleanResponse).getCode() != HttpConstant.HTTP_OK) {
                    onFailure(call, new RuntimeException(booleanResponse.getMsg()));
                } else {
                    boolean result = booleanResponse.getT();
                    if (result) {
                        //更新成功
                        httpStateLiveData.postValue(HTTP_STATE_UPDATED_FINISH);
                    } else {
                        //更新失败
                        httpStateLiveData.postValue(HTTP_STATE_UPDATED_ERROR);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<BooleanResponse> call, Throwable t) {
                ToastUtil.showToastShort(t.getMessage());
                httpStateLiveData.postValue(HTTP_STATE_UPDATED_ERROR);
            }
        });
    }

    public void updateMobileLocal(String mobile) {
        UserLoginInfo userLoginInfo = userLoginInfoLiveData.getValue();
        Objects.requireNonNull(userLoginInfo).setMobile(mobile);
        userLoginInfoLiveData.postValue(userLoginInfo);
    }

    public void updateUserLoginEmail(String email, String code) {
        HttpClient.createUserApi().updateEmail(email, code).enqueue(new Callback<BooleanResponse>() {
            @Override
            public void onResponse(@NonNull Call<BooleanResponse> call, @NonNull Response<BooleanResponse> response) {
                if (response.code() != 200) {
                    HttpErrorUtil.showErrorToast(response.code());
                    httpStateLiveData.postValue(HTTP_STATE_UPDATED_ERROR);
                    return;
                }
                BooleanResponse booleanResponse = response.body();
                if (Objects.requireNonNull(booleanResponse).getCode() != HttpConstant.HTTP_OK) {
                    onFailure(call, new RuntimeException(booleanResponse.getMsg()));
                } else {
                    boolean result = booleanResponse.getT();
                    if (result) {
                        //更新成功
                        httpStateLiveData.postValue(HTTP_STATE_UPDATED_FINISH);
                    } else {
                        //更新失败
                        httpStateLiveData.postValue(HTTP_STATE_UPDATED_ERROR);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<BooleanResponse> call, Throwable t) {
                ToastUtil.showToastShort(t.getMessage());
                httpStateLiveData.postValue(HTTP_STATE_UPDATED_ERROR);
            }
        });
    }

    public void updateEmailLocal(String email) {
        UserLoginInfo userLoginInfo = userLoginInfoLiveData.getValue();
        Objects.requireNonNull(userLoginInfo).setEmail(email);
        userLoginInfoLiveData.postValue(userLoginInfo);
    }

    public void deleteAccount(Activity activity) {
        HttpClient.createUserApi().deleteAccount().enqueue(new Callback<BooleanResponse>() {
            @Override
            public void onResponse(@NonNull Call<BooleanResponse> call, @NonNull Response<BooleanResponse> response) {
                if (response.code() != 200) {
                    HttpErrorUtil.showErrorToast(response.code());
                    return;
                }
                BooleanResponse booleanResponse = response.body();
                if (Objects.requireNonNull(booleanResponse).getCode() != HttpConstant.HTTP_OK) {
                    onFailure(call, new RuntimeException(booleanResponse.getMsg()));
                } else {
                    logout();
                    ToastUtil.showToastShort(activity.getString(R.string.account_deleted_successfully));
                    activity.startActivity(new Intent(activity, LoginActivity.class));
                }
            }

            @Override
            public void onFailure(@NonNull Call<BooleanResponse> call, Throwable t) {
                ToastUtil.showToastShort(t.getMessage());
            }
        });
    }

    public void logout() {
        ActivityManager.getInstance().popAllActivity();
        HttpClient.clearToken();
    }
}
