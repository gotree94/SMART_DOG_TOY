package com.jieli.healthaide.ui.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.bumptech.glide.Glide;
import com.jieli.component.thread.Priority;
import com.jieli.component.thread.ThreadManager;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.HttpErrorUtil;
import com.jieli.jl_filebrowse.interfaces.OperatCallback;
import com.jieli.jl_health_http.HttpClient;
import com.jieli.jl_health_http.HttpConstant;
import com.jieli.jl_health_http.model.UserLoginInfo;
import com.jieli.jl_health_http.model.response.UserLoginInfoResponse;
import com.jieli.jl_rcsp.util.JL_Log;

import java.text.ParseException;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 健康APPViewModel
 *
 * @author zqjasonZhong
 * @since 2021/3/3
 */
public class HealthAppViewModel extends AndroidViewModel {
    private static final String TAG = HealthAppViewModel.class.getSimpleName();
    private UserLoginInfo profile = null;

    public MutableLiveData<UserLoginInfo> userLoginInfoLiveData = new MutableLiveData<>();

    public HealthAppViewModel(@NonNull Application application) {
        super(application);
    }

    public void requestProfile(OperatCallback callback) {
        HttpClient.createUserApi().getUserLoginInfo().enqueue(new Callback<UserLoginInfoResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserLoginInfoResponse> call, @NonNull Response<UserLoginInfoResponse> response) {
                if (!response.isSuccessful()) {
                    HttpErrorUtil.showErrorToast(response.code());
                    onFailure(call, new RuntimeException("get profile failed httpCode = " + response.code()));
                    return;
                }
                UserLoginInfoResponse userLoginInfoResponse = response.body();
                if (null == userLoginInfoResponse) {
                    onFailure(call, new RuntimeException("Response body is null."));
                    return;
                }
                if (userLoginInfoResponse.getCode() != HttpConstant.HTTP_OK) {
                    onFailure(call, new RuntimeException("get profile failed code = " + userLoginInfoResponse.getCode()));
                    return;
                }
                profile = userLoginInfoResponse.getT();
                userLoginInfoLiveData.postValue(profile);
                JL_Log.e(TAG, "requestProfile", "register = " + getRegisterTime() + "\t" + profile.getRegisterTime());
                if (callback != null) callback.onSuccess();
            }

            @Override
            public void onFailure(@NonNull Call<UserLoginInfoResponse> call, Throwable t) {
                t.printStackTrace();
                if (callback != null) callback.onError(-1);
            }


        });
    }

    public String getUid() {
        return profile == null ? "" : profile.getId();
    }

    public long getRegisterTime() {
        long registerTime = 0L;
        try {
            Date date = CalendarUtil.serverDateFormat().parse(profile.getRegisterTime());
            if (date != null) registerTime = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return registerTime;
    }

    //数据缓存清理
    public void cleanCache() {
        //todo 清理缓存
        ThreadManager.getInstance().postRunnable(Priority.High, () -> {
            HttpClient.cleanCache();//网络
            Glide.get(getApplication()).clearDiskCache();
            //是否要清理数据库，待商榷
        });
    }
}
