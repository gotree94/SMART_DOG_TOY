package com.jieli.healthaide.ui.mine;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.jieli.component.ActivityManager;
import com.jieli.component.utils.ToastUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.login.LoginActivity;
import com.jieli.healthaide.util.HttpErrorUtil;
import com.jieli.jl_health_http.HttpClient;
import com.jieli.jl_health_http.HttpConstant;
import com.jieli.jl_health_http.model.UserInfo;
import com.jieli.jl_health_http.model.response.BooleanResponse;
import com.jieli.jl_health_http.model.response.UserInfoResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/10/21 1:36 PM
 * @desc :
 */
public class UserInfoViewModel extends ViewModel {
    static final int HTTP_STATE_UPDATING = 0;
    static final int HTTP_STATE_UPDATED_FINISH = 1;
    static final int HTTP_STATE_UPDATED_ERROR = 2;
    static final int HTTP_STATE_REQUESTING = 3;
    static final int HTTP_STATE_REQUEST_FINISH = 4;
    static final int HTTP_STATE_REQUEST_ERROR = 5;

    public MutableLiveData<UserInfo> userInfoLiveData = new MutableLiveData<>();
    MutableLiveData<Integer> httpStateLiveData = new MutableLiveData<>(-1);


    public LiveData<UserInfo> getUserInfoLiveData() {
        return userInfoLiveData;
    }

    public void getUserInfo() {
        httpStateLiveData.postValue(HTTP_STATE_REQUESTING);
        HttpClient.createUserApi().getUserInfo().enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserInfoResponse> call, @NonNull Response<UserInfoResponse> response) {
                if (response.code() != 200) {
//                    onFailure(call, new RuntimeException("http code error:" + response.code()));
                    HttpErrorUtil.showErrorToast(response.code());
                    httpStateLiveData.postValue(HTTP_STATE_REQUEST_ERROR);
                    return;
                }
                UserInfoResponse userInfoResponse = response.body();
                if (userInfoResponse == null) {
                    onFailure(call, new Throwable("response is null."));
                    return;
                }
                if (userInfoResponse.getCode() != HttpConstant.HTTP_OK) {
                    onFailure(call, new RuntimeException(userInfoResponse.getMsg()));
                } else {
                    String text = userInfoResponse.getT();
                    UserInfo userInfo;
                    if (TextUtils.isEmpty(text)) {
                        userInfo = new UserInfo();
                    } else {
                        try {
                            userInfo = new Gson().fromJson(text, UserInfo.class);
                        } catch (Exception e) {
                            userInfo = new UserInfo();
                        }
                    }
                    userInfoLiveData.postValue(userInfo);
                    httpStateLiveData.postValue(HTTP_STATE_REQUEST_FINISH);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserInfoResponse> call, Throwable t) {
                ToastUtil.showToastShort(t.getMessage());
                httpStateLiveData.postValue(HTTP_STATE_REQUEST_ERROR);
            }
        });
    }


    public void updateUserInfo(UserInfo userInfo) {
        httpStateLiveData.postValue(HTTP_STATE_UPDATING);
        HttpClient.createUserApi().updateUserInfo(userInfo).enqueue(new Callback<BooleanResponse>() {
            @Override
            public void onResponse(@NonNull Call<BooleanResponse> call, @NonNull Response<BooleanResponse> response) {
                if (response.code() != 200) {
//                    onFailure(call, new RuntimeException("http code error:" + response.code()));
                    onFailure(call, new Throwable("Bad code : " + response.code()));
                    return;
                }
                BooleanResponse booleanResponse = response.body();
                if (booleanResponse == null) {
                    onFailure(call, new Throwable("response is null."));
                    return;
                }
                if (booleanResponse.getCode() != HttpConstant.HTTP_OK) {
                    onFailure(call, new RuntimeException(booleanResponse.getMsg()));
                } else {
                    userInfoLiveData.postValue(userInfo);
                    httpStateLiveData.postValue(HTTP_STATE_UPDATED_FINISH);
                    //ToastUtil.showToastShort(booleanResponse.getMsg());
                    HttpClient.removeCache(HttpClient.createUserApi().getUserInfo().request());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BooleanResponse> call, Throwable t) {
                ToastUtil.showToastShort(t.getMessage());
                httpStateLiveData.postValue(HTTP_STATE_UPDATED_ERROR);
            }
        });
    }


    public void logout() {
        ActivityManager.getInstance().popAllActivity();
        HttpClient.clearToken();
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
                if (booleanResponse == null) {
                    onFailure(call, new Throwable("response is null."));
                    return;
                }
                if (booleanResponse.getCode() != HttpConstant.HTTP_OK) {
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


    public UserInfo copyUserInfo() {
        UserInfo source = userInfoLiveData.getValue();
        String sourceString = new Gson().toJson(source);
        return new Gson().fromJson(sourceString, UserInfo.class);
    }


}
