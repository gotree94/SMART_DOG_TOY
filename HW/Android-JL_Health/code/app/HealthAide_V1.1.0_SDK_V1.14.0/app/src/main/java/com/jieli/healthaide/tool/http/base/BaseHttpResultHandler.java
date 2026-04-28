package com.jieli.healthaide.tool.http.base;

import androidx.annotation.NonNull;

import com.jieli.healthaide.tool.watch.WatchServerCacheHelper;
import com.jieli.jl_health_http.HttpConstant;
import com.jieli.jl_health_http.model.response.BaseResponse;
import com.jieli.jl_health_http.tool.OnResultCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * BaseHttpResultHandler
 *
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc Http回复结果处理器
 * @since 2024/7/26
 */
public class BaseHttpResultHandler<X, T extends BaseResponse<X>> implements Callback<T> {
    /**
     * 结果回调
     */
    private final OnResultCallback<X> callback;
    /**
     * 是否判断空结果
     */
    private final boolean isAllowEmptyValue;

    public BaseHttpResultHandler(OnResultCallback<X> callback) {
        this(false, callback);
    }

    public BaseHttpResultHandler(boolean isAllowEmptyValue, OnResultCallback<X> callback) {
        this.isAllowEmptyValue = isAllowEmptyValue;
        this.callback = callback;
    }

    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        if (!response.isSuccessful()) {
            if (null != callback)
                callback.onError(WatchServerCacheHelper.ERR_HTTP_BAD_CODE, "http code error:" + response.code());
            return;
        }
        T responseBody = response.body();
        if (null == responseBody) {
            if (null != callback)
                callback.onError(WatchServerCacheHelper.ERR_HTTP_FORMAT, "response body is error.");
            return;
        }
        if (responseBody.getCode() != HttpConstant.HTTP_OK) {
            if (null != callback)
                callback.onError(responseBody.getCode(), "Server reply a bad code : " + responseBody.getCode()
                        + ", " + responseBody.getMsg());
            return;
        }
        final X result = responseBody.getT();
        if (isAllowEmptyValue && result == null) {
            if (null != callback)
                callback.onError(WatchServerCacheHelper.ERR_BAD_RESPONSE, "Server replied with an empty result.");
            return;
        }
        if (null != callback) callback.onResult(result);
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        if (null != callback)
            callback.onError(WatchServerCacheHelper.ERR_HTTP_EXCEPTION, t.getMessage());
    }
}
