package com.jieli.healthaide.ui.login;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jieli.component.utils.ToastUtil;
import com.jieli.healthaide.tool.config.ConfigHelper;
import com.jieli.healthaide.tool.http.base.BaseHttpResultHandler;
import com.jieli.healthaide.ui.login.bean.LoginMsg;
import com.jieli.healthaide.util.FormatUtil;
import com.jieli.healthaide.util.HttpErrorUtil;
import com.jieli.jl_health_http.HttpClient;
import com.jieli.jl_health_http.HttpConstant;
import com.jieli.jl_health_http.model.response.LoginResponse;
import com.jieli.jl_health_http.tool.OnResultCallback;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/9/21 4:06 PM
 * @desc :
 */
public class LoginViewModel extends ViewModel {

    private final ConfigHelper mConfigHelper = ConfigHelper.getInstance();

    MutableLiveData<LoginMsg> loginMsgMutableLiveData = new MutableLiveData<>(new LoginMsg());

    public boolean isBanRequestPermission(String permission) {
        return mConfigHelper.isBanRequestPermission(permission);
    }

    public void setBanRequestPermission(String permission) {
        mConfigHelper.setBanRequestPermission(permission);
    }

    public void setCacheMobile(String number) {
        mConfigHelper.setCacheAccount(number);
    }

    public void loginByAccount(String account, String password) {
        if (isLogin()) return;
        mConfigHelper.setLoginAccount(account);
        LoginMsg loginMsg = new LoginMsg(LoginMsg.STATE_LOGINING, "logining");
        loginMsgMutableLiveData.postValue(loginMsg);
        if (FormatUtil.checkEmailAddress(account)) {
            ConfigHelper.getInstance().setLoginType(1);//登录方式是邮箱地址
            HttpClient.createUserApi().loginByEmailAndPassword(account, password).enqueue(getLoginCallback());
        } else if (FormatUtil.checkPhoneNumber(account)) {
            ConfigHelper.getInstance().setLoginType(0);//登录方式是手机号
            HttpClient.createUserApi().loginByPassword(account, password).enqueue(getLoginCallback());
        }else {
            LoginMsg errMsg = new LoginMsg(LoginMsg.STATE_LOGIN_ERROR, "Unknown login way.  account : " + account);
            loginMsgMutableLiveData.postValue(errMsg);
        }
    }

    public void loginByCode(String mobile, String code) {
        if (isLogin()) return;
        mConfigHelper.setLoginAccount(mobile);
        LoginMsg loginMsg = new LoginMsg(LoginMsg.STATE_LOGINING, "logining");
        loginMsgMutableLiveData.postValue(loginMsg);
        if (FormatUtil.checkPhoneNumber(mobile)) {
            ConfigHelper.getInstance().setLoginType(0);//登录方式是手机号
            HttpClient.createUserApi().checkSmsCode(mobile, code).enqueue(new BaseHttpResultHandler<>(new OnResultCallback<Boolean>() {
                @Override
                public void onResult(Boolean result) {
                    //验证码校验成功才真正去登录
                    HttpClient.createUserApi().loginBySms(mobile, code).enqueue(getLoginCallback());
                }

                @Override
                public void onError(int code, String message) {
                    LoginMsg errMsg = new LoginMsg(LoginMsg.STATE_LOGIN_ERROR, "Failed to login. code : " + code + ", " + message);
                    loginMsgMutableLiveData.postValue(errMsg);
                }
            }));
        } else if (FormatUtil.checkEmailAddress(mobile)) {
            ConfigHelper.getInstance().setLoginType(1);//登录方式是邮箱地址
            HttpClient.createUserApi().checkEmailCode(mobile, code).enqueue(new BaseHttpResultHandler<>(new OnResultCallback<Boolean>() {
                @Override
                public void onResult(Boolean result) {
                    //验证码校验成功才真正去登录
                    HttpClient.createUserApi().loginByEmail(mobile, code).enqueue(getLoginCallback());
                }

                @Override
                public void onError(int code, String message) {
                    LoginMsg errMsg = new LoginMsg(LoginMsg.STATE_LOGIN_ERROR, "Failed to login. code : " + code + ", " + message);
                    loginMsgMutableLiveData.postValue(errMsg);
                }
            }));
        } else {
            LoginMsg errMsg = new LoginMsg(LoginMsg.STATE_LOGIN_ERROR, "Unknown login way.  account : " + mobile);
            loginMsgMutableLiveData.postValue(errMsg);
        }
    }

    //获取缓存的账号，如果有登录过使用登录的，没有就使用输入缓存
    public String getCacheInputNumber() {
        String text = mConfigHelper.getLoginAccount();
        if (TextUtils.isEmpty(text)) {
            text = mConfigHelper.getCacheAccount();

        }
        return text;
    }

    @NotNull
    private Callback<LoginResponse> getLoginCallback() {
        HttpClient.cleanCache();//登录前清理http缓存
        return new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.code() != 200) {
//                    onFailure(call, new RuntimeException("http code error:" + response.code()));
                    HttpErrorUtil.showErrorToast(response.code());
                    LoginMsg errMsg = new LoginMsg(LoginMsg.STATE_LOGIN_ERROR, "logining error");
                    loginMsgMutableLiveData.postValue(errMsg);
                    return;

                }
                LoginResponse loginResponse = response.body();
                if (Objects.requireNonNull(loginResponse).getCode() != HttpConstant.HTTP_OK) {
                    onFailure(call, new RuntimeException(Objects.requireNonNull(response.body()).getMsg()));
                } else {
                    LoginMsg finishMsg = new LoginMsg(LoginMsg.STATE_LOGIN_FINISH, "logining finish");
                    loginMsgMutableLiveData.postValue(finishMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                ToastUtil.showToastShort(t.getMessage());
                LoginMsg errMsg = new LoginMsg(LoginMsg.STATE_LOGIN_ERROR, "logining error");
                loginMsgMutableLiveData.postValue(errMsg);
            }
        };
    }


    private boolean isLogin() {
        LoginMsg loginMsg = loginMsgMutableLiveData.getValue();
        return loginMsg != null && loginMsg.getState() == LoginMsg.STATE_LOGINING;
    }


}
