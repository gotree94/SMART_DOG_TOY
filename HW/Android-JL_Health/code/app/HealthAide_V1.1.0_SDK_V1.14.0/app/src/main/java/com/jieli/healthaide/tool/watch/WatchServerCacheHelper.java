package com.jieli.healthaide.tool.watch;

import com.jieli.jl_health_http.HttpClient;
import com.jieli.jl_health_http.HttpConstant;
import com.jieli.jl_health_http.api.WatchApi;
import com.jieli.jl_health_http.model.OtaFileMsg;
import com.jieli.jl_health_http.model.WatchConfigure;
import com.jieli.jl_health_http.model.WatchFileList;
import com.jieli.jl_health_http.model.WatchFileMsg;
import com.jieli.jl_health_http.model.WatchProduct;
import com.jieli.jl_health_http.model.param.DialParam;
import com.jieli.jl_health_http.model.param.WatchFileListParam;
import com.jieli.jl_health_http.model.response.ALiPayMsgResponse;
import com.jieli.jl_health_http.model.response.BaseResponse;
import com.jieli.jl_health_http.model.response.BooleanResponse;
import com.jieli.jl_health_http.model.response.DialPayRecordListResponse;
import com.jieli.jl_health_http.model.response.OtaFileMsgResponse;
import com.jieli.jl_health_http.model.response.WatchFileListResponse;
import com.jieli.jl_health_http.model.response.WatchFileResponse;
import com.jieli.jl_health_http.model.response.WatchProductResponse;
import com.jieli.jl_health_http.model.watch.ALiPayMsg;
import com.jieli.jl_health_http.model.watch.DialPayRecordList;
import com.jieli.jl_health_http.tool.WriteDataToFileTask;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.util.JL_Log;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 表盘服务器缓存器
 * @since 2021/4/30
 */
public class WatchServerCacheHelper {


    private static final String TAG = "watch_http";
    private static volatile WatchServerCacheHelper instance;
    private final WatchApi mWatchApi = HttpClient.createWatchApi();
    private final Map<String, WatchProduct> watchProductMap = new HashMap<>(); //缓存手表产品信息
    private final Map<String, WatchFileMsg> watchFileMap = new HashMap<>();    //缓存设备表盘列表信息

    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    public static final int ERR_INVALID_PARAMETER = 1;     //无效参数
    public static final int ERR_HTTP_BAD_CODE = 2;         //网络错误码
    public static final int ERR_HTTP_FORMAT = 3;           //网络数据异常
    public static final int ERR_HTTP_EXCEPTION = 4;        //网络异常
    public static final int ERR_BAD_RESPONSE = 5;          //错误回复
    public static final int ERR_NOT_PATH = 6;              //没有对应路径
    public static final int ERR_THREAD_POOL_SHUTDOWN = 7;  //线程池已关闭
    public static final int ERR_NOT_SUPPORT_WAY = 8;       //不支持的支付方式
    public static final int ERR_DIAL_PAYMENT = 9;          //表盘已支付
    public static final int ERR_LOAD_FINISH = 10;          //加载内容完成
    public static final int ERR_NOT_SUPPORT_VERSION = 11;  //不支持的版本
    public static final int ERR_REMOTE_NOT_CONNECT = 12;   //远端未连接

    //    public static final boolean USE_SHOP_API = false;  //是否使用支付宝功能  用isSupportDialPayment()代替
    public static final boolean IS_SAND_BOX = false;   //是否沙盒支付模式


    public static WatchServerCacheHelper getInstance() {
        if (null == instance) {
            synchronized (WatchServerCacheHelper.class) {
                if (null == instance) {
                    instance = new WatchServerCacheHelper();
                }
            }
        }
        return instance;
    }

    public void destroy() {
        clearCache();
        if (mExecutorService != null && !mExecutorService.isShutdown()) {
            mExecutorService.shutdownNow();
        }
        instance = null;
    }

    public void cleanWatchFileCache() {
        watchFileMap.clear();
    }

    public void clearCache() {
        cleanWatchFileCache();
        watchProductMap.clear();
    }

    public WatchProduct getCacheWatchProduct(int uid, int pid) {
        return watchProductMap.get(getWatchProductKey(uid, pid));
    }

    public WatchConfigure getCacheWatchConfigure(int uid, int pid) {
        WatchProduct product = getCacheWatchProduct(uid, pid);
        if (null == product) return null;
        return product.getWatchConfigure();
    }

    public boolean isSupportOnlineOTA(WatchManager manager) {
        if (null == manager) return false;
        DeviceInfo deviceInfo = manager.getDeviceInfo();
        if (null == deviceInfo) return false;
        return isSupportOnlineOTA(deviceInfo.getUid(), deviceInfo.getPid());
    }

    public boolean isSupportOnlineOTA(int uid, int pid) {
        WatchConfigure configure = getCacheWatchConfigure(uid, pid);
        if (null == configure || null == configure.getSupport_ota()) return false;
        return configure.getSupport_ota();
    }

    public boolean isSupportDialPayment(WatchManager manager) {
        if (null == manager) return false;
        DeviceInfo deviceInfo = manager.getDeviceInfo();
        if (null == deviceInfo) return false;
        WatchConfigure configure = getCacheWatchConfigure(deviceInfo.getUid(), deviceInfo.getPid());
        if (null == configure || null == configure.getSupport_dial_payment()) return false;
        return configure.getSupport_dial_payment();
    }

    public WatchFileMsg getCacheWatchServerMsg(WatchManager manager, String uuid) {
        if (null == uuid) return null;
        return watchFileMap.get(getWatchFileKey(manager, uuid));
    }

    public void getWatchProductMsg(int uid, int pid, final IWatchHttpCallback<WatchProduct> callback) {
        WatchProduct product = getCacheWatchProduct(uid, pid);
        if (null != product) {
            if (null != callback) callback.onSuccess(product);
            return;
        }
        mWatchApi.queryWatchProductInfo(pid, uid).enqueue(new WatchProductCallback(callback));
        /*if (USE_SHOP_API) {
            mWatchApi.queryWatchProductInfo(pid, uid).enqueue(new WatchProductCallback(callback));
        } else {
            mWatchApi.queryWatchProduct(uid, pid).enqueue(new WatchProductCallback(callback));
        }*/
    }

    public void queryWatchInfoByUUID(WatchManager manager, String uuid, IWatchHttpCallback<WatchFileMsg> callback) {
        if (null == uuid) {
            callbackFailed(callback, ERR_INVALID_PARAMETER, "Invalid parameter: uuid");
            return;
        }
        DeviceInfo deviceInfo = manager.getDeviceInfo();
        if (null == deviceInfo) {
            callbackFailed(callback, ERR_REMOTE_NOT_CONNECT, "Device is not connect.");
            return;
        }
        WatchFileMsg watchFile = getCacheWatchServerMsg(manager, uuid);
        if (null != watchFile) {
            if (null != callback) callback.onSuccess(watchFile);
            return;
        }
        int uid = deviceInfo.getUid();
        int pid = deviceInfo.getPid();
        String key = getWatchFileKey(uuid, uid, pid);
        if (isSupportDialPayment(manager)) {
//            mWatchApi.getWatchFileByUUID(uuid).enqueue(new WatchFileCallback(uuid, callback));
            mWatchApi.getWatchFileByUUID(uuid, uid, pid).enqueue(new WatchFileCallback(key, callback));
        } else {
            mWatchApi.queryWatchFileByUUID(uuid, uid, pid).enqueue(new WatchFileCallback(key, callback));
//            mWatchApi.queryWatchFileByUUID(uuid).enqueue(new WatchFileCallback(uuid, callback));
        }
    }

    public void queryWatchFileListByPage(WatchManager manager, WatchFileListParam param, final IWatchHttpCallback<WatchFileList> callback) {
        if (null == param) {
            callbackFailed(callback, ERR_INVALID_PARAMETER, "Invalid parameter : WatchFileListParam");
            return;
        }
        if (isSupportDialPayment(manager)) {
            if (!(param instanceof DialParam)) {
                callbackFailed(callback, ERR_INVALID_PARAMETER, "Invalid parameter : DialParam");
                return;
            }
            DialParam dialParam = (DialParam) param;
            if (null == dialParam.getDialID()) {
                callbackFailed(callback, ERR_INVALID_PARAMETER, "Invalid parameter : DialID");
                return;
            }
            mWatchApi.getWatchFileListByPage(dialParam.getDialID(), dialParam.getPage(), dialParam.getSize(), dialParam.isFree())
                    .enqueue(new WatchFileListCallback(manager, callback));
        } else {
            mWatchApi.queryWatchFileList(param).enqueue(new WatchFileListCallback(manager, callback));
        }
    }

    public void queryOtaMsg(int pid, int vid, final IWatchHttpCallback<OtaFileMsg> callback) {
        if (!isSupportOnlineOTA(vid, pid)) {
            callbackFailed(callback, ERR_NOT_SUPPORT_WAY, "Server does not support");
            return;
        }
        mWatchApi.queryOtaMsg(pid, vid).enqueue(new Callback<OtaFileMsgResponse>() {
            @Override
            public void onResponse(@NotNull Call<OtaFileMsgResponse> call, @NotNull Response<OtaFileMsgResponse> response) {
                handleRequestOk(response, callback);
            }

            @Override
            public void onFailure(@NotNull Call<OtaFileMsgResponse> call, @NotNull Throwable t) {
                callbackFailed(callback, ERR_HTTP_EXCEPTION, t.getMessage());
            }
        });
    }

    public void query4gOtaMessage(int pid, int vid, int networkVid, IWatchHttpCallback<OtaFileMsg> callback) {
        if (!isSupportOnlineOTA(vid, pid)) {
            callbackFailed(callback, ERR_NOT_SUPPORT_WAY, "Server does not support");
            return;
        }
        mWatchApi.query4gOtaMessage(pid, vid, networkVid).enqueue(new Callback<OtaFileMsgResponse>() {
            @Override
            public void onResponse(@NotNull Call<OtaFileMsgResponse> call, @NotNull Response<OtaFileMsgResponse> response) {
                handleRequestOk(response, callback);
            }

            @Override
            public void onFailure(@NotNull Call<OtaFileMsgResponse> call, @NotNull Throwable t) {
                callbackFailed(callback, ERR_HTTP_EXCEPTION, t.getMessage());
            }
        });
    }

    public void downloadFile(String uri, final String outPath, final OnDownloadListener callback) {
        if (uri == null || (!uri.startsWith("http://") && !uri.startsWith("https://")) || outPath == null) {
            callbackFailed(callback, ERR_INVALID_PARAMETER, "Invalid parameter: uri = " + uri);
            return;
        }
        JL_Log.d(TAG, "downloadFile", "uri = " + uri + ", outPath = " + outPath);
        mWatchApi.downloadFileByUrl(uri).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    callbackFailed(callback, ERR_HTTP_BAD_CODE, "http code error:" + response.code());
                    return;
                }
                final ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    callbackFailed(callback, ERR_HTTP_FORMAT, "response body is error.");
                    return;
                }
                JL_Log.d(TAG, "downloadFile", "body = " + responseBody);
                if (!mExecutorService.isShutdown()) {
                    mExecutorService.submit(new WriteDataToFileTask(responseBody, outPath,
                            new WriteDataToFileTask.OnWriteDataListener() {

                                @Override
                                public void onStart(long threadID) {
                                    JL_Log.d(TAG, "downloadFile", "WriteDataToFileTask => onStart");
                                    if (callback != null) {
                                        callback.onStart();
                                    }
                                }

                                @Override
                                public void onProgress(long threadID, float progress) {
                                    if (callback != null) {
                                        callback.onProgress((int) progress);
                                    }
                                }

                                @Override
                                public void onStop(long threadID, String outputPath) {
                                    JL_Log.w(TAG, "downloadFile", "onStop = " + outputPath);
                                    callbackSuccess(callback, outputPath);
                                }

                                @Override
                                public void onError(long threadID, int code, String message) {
                                    JL_Log.e(TAG, "downloadFile", "WriteDataToFileTask error. code = " + code + ", message = " + message);
                                    int errCode = ERR_HTTP_EXCEPTION;
                                    if (code == 1) {
                                        errCode = ERR_INVALID_PARAMETER;
                                    } else if (code == 2) {
                                        errCode = ERR_NOT_PATH;
                                    }
                                    callbackFailed(callback, errCode, message);
                                }
                            }));
                } else {
                    JL_Log.e(TAG, "downloadFile", "Thread pool is shut down.");
                    callbackFailed(callback, ERR_THREAD_POOL_SHUTDOWN, "Thread pool is shut down");
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                callbackFailed(callback, ERR_HTTP_EXCEPTION, t.getMessage());
            }
        });
    }

    public void getDialPayRecordListByPage(WatchManager manager, int page, int size, IWatchHttpCallback<DialPayRecordList> callback) {
        if (!isSupportDialPayment(manager)) {
            callbackFailed(callback, ERR_NOT_SUPPORT_WAY, "Server does not support");
            return;
        }
        mWatchApi.getDialPayRecordListByPage(page, size).enqueue(new Callback<DialPayRecordListResponse>() {
            @Override
            public void onResponse(@NotNull Call<DialPayRecordListResponse> call, @NotNull Response<DialPayRecordListResponse> response) {
                handleRequestOk(response, callback);
            }

            @Override
            public void onFailure(@NotNull Call<DialPayRecordListResponse> call, @NotNull Throwable t) {
                callbackFailed(callback, ERR_HTTP_EXCEPTION, t.getMessage());
            }
        });
    }

    public void getDialPaymentListByPage(WatchManager manager, String dialId, int page, int size, IWatchHttpCallback<WatchFileList> callback) {
        if (!isSupportDialPayment(manager)) {
            callbackFailed(callback, ERR_NOT_SUPPORT_WAY, "Server does not support");
            return;
        }
        if (null == dialId) {
            callbackFailed(callback, ERR_INVALID_PARAMETER, "Invalid parameter: dialId");
            return;
        }
        mWatchApi.queryDialPaymentListByPage(dialId, page, size).enqueue(new Callback<WatchFileListResponse>() {
            @Override
            public void onResponse(@NotNull Call<WatchFileListResponse> call, @NotNull Response<WatchFileListResponse> response) {
                handleRequestOk(response, callback);
            }

            @Override
            public void onFailure(@NotNull Call<WatchFileListResponse> call, @NotNull Throwable t) {
                callbackFailed(callback, ERR_HTTP_EXCEPTION, t.getMessage());
            }
        });
    }

    public void downloadDial(WatchManager manager, String uuid, final String outPath, final OnDownloadListener callback) {
        if (!isSupportDialPayment(manager)) {
            callbackFailed(callback, ERR_NOT_SUPPORT_WAY, "Server does not support");
            return;
        }
        if (null == uuid) {
            callbackFailed(callback, ERR_INVALID_PARAMETER, "Invalid parameter : uuid");
            return;
        }
        mWatchApi.queryWatchFileUrl(uuid).enqueue(new Callback<WatchFileResponse>() {
            @Override
            public void onResponse(@NotNull Call<WatchFileResponse> call, @NotNull Response<WatchFileResponse> response) {
                handleRequestOk(response, new IWatchHttpCallback<WatchFileMsg>() {
                    @Override
                    public void onSuccess(WatchFileMsg result) {
                        downloadFile(result.getUrl(), outPath, callback);
                    }

                    @Override
                    public void onFailed(int code, String message) {
                        callbackFailed(callback, code, message);
                    }
                });
            }

            @Override
            public void onFailure(@NotNull Call<WatchFileResponse> call, @NotNull Throwable t) {
                callbackFailed(callback, ERR_HTTP_EXCEPTION, t.getMessage());
            }
        });
    }

    public void dialPaymentByAliPay(String id, boolean isSandBox, IWatchHttpCallback<ALiPayMsg> callback) {
        /*if (!isSupportDialPayment(manager)) { //考虑到付款功能，必然支持表盘支付，无需判断
            callbackFailed(callback, ERR_NOT_SUPPORT_WAY, "Server does not support");
            return;
        }*/
        if (null == id) {
            callbackFailed(callback, ERR_INVALID_PARAMETER, "Invalid parameter : id");
            return;
        }
        mWatchApi.dialPaymentByAliPay(id, isSandBox).enqueue(new Callback<ALiPayMsgResponse>() {
            @Override
            public void onResponse(@NotNull Call<ALiPayMsgResponse> call, @NotNull Response<ALiPayMsgResponse> response) {
                handleRequestOk(response, callback);
            }

            @Override
            public void onFailure(@NotNull Call<ALiPayMsgResponse> call, @NotNull Throwable t) {
                callbackFailed(callback, ERR_HTTP_EXCEPTION, t.getMessage());
            }
        });
    }

    public void dialPaymentByFree(WatchManager manager, String id, IWatchHttpCallback<Boolean> callback) {
        if (!isSupportDialPayment(manager)) {
            callbackFailed(callback, ERR_NOT_SUPPORT_WAY, "Server does not support");
            return;
        }
        if (null == id) {
            callbackFailed(callback, ERR_INVALID_PARAMETER, "Invalid parameter : id");
            return;
        }
        mWatchApi.dialPaymentByFree(id).enqueue(new Callback<BooleanResponse>() {
            @Override
            public void onResponse(@NotNull Call<BooleanResponse> call, @NotNull Response<BooleanResponse> response) {
                handleRequestOk(response, callback);
            }

            @Override
            public void onFailure(@NotNull Call<BooleanResponse> call, @NotNull Throwable t) {
                callbackFailed(callback, ERR_HTTP_EXCEPTION, t.getMessage());
            }
        });
    }

    public void checkPaymentStateByAliPay(String outTradeNo, IWatchHttpCallback<Boolean> callback) {
        /*if (!isSupportDialPayment(manager)) { //考虑到付款功能，必然支持表盘支付，无需判断
            callbackFailed(callback, ERR_NOT_SUPPORT_WAY, "Server does not support");
            return;
        }*/
        if (null == outTradeNo) {
            callbackFailed(callback, ERR_INVALID_PARAMETER, "Invalid parameter : outTradeNo");
            return;
        }
        mWatchApi.checkPaymentStateByAliPay(outTradeNo).enqueue(new Callback<BooleanResponse>() {
            @Override
            public void onResponse(@NotNull Call<BooleanResponse> call, @NotNull Response<BooleanResponse> response) {
                handleRequestOk(response, callback);
            }

            @Override
            public void onFailure(@NotNull Call<BooleanResponse> call, @NotNull Throwable t) {
                callbackFailed(callback, ERR_HTTP_EXCEPTION, t.getMessage());
            }
        });
    }

    public void deleteDialPaymentRecord(WatchManager manager, String dialId, IWatchHttpCallback<Boolean> callback) {
        if (!isSupportDialPayment(manager)) {
            callbackFailed(callback, ERR_NOT_SUPPORT_WAY, "Server does not support");
            return;
        }
        if (null == dialId) {
            callbackFailed(callback, ERR_INVALID_PARAMETER, "Invalid parameter : dialId");
            return;
        }
        mWatchApi.deletePaymentRecord(dialId).enqueue(new Callback<BooleanResponse>() {
            @Override
            public void onResponse(@NotNull Call<BooleanResponse> call, @NotNull Response<BooleanResponse> response) {
                handleRequestOk(response, callback);
            }

            @Override
            public void onFailure(@NotNull Call<BooleanResponse> call, @NotNull Throwable t) {
                callbackFailed(callback, ERR_HTTP_EXCEPTION, t.getMessage());
            }
        });
    }

    private <T, R extends BaseResponse<T>> T handleRequestOk(Response<R> response, IWatchHttpCallback<T> callback) {
        if (!response.isSuccessful()) {
            callbackFailed(callback, ERR_HTTP_BAD_CODE, "http code error:" + response.code());
            return null;
        }
        R responseBody = response.body();
        if (null == responseBody) {
            callbackFailed(callback, ERR_HTTP_FORMAT, "response body is error.");
            return null;
        }
        if (responseBody.getCode() != HttpConstant.HTTP_OK) {
            callbackFailed(callback, ERR_BAD_RESPONSE, responseBody.getCode() + "," + responseBody.getMsg());
            return null;
        }
        T result = responseBody.getT();
        if (null == result) {
            callbackFailed(callback, ERR_HTTP_FORMAT, "response format is error.");
            return null;
        }
        callbackSuccess(callback, result);
        return result;
    }

    private <T> void callbackSuccess(IWatchHttpCallback<T> callback, T result) {
        if (callback != null) callback.onSuccess(result);
    }

    private <T> void callbackFailed(IWatchHttpCallback<T> callback, int code, String message) {
        if (callback != null) callback.onFailed(code, message);
    }

    private String getWatchProductKey(int vid, int pid) {
        return "vid_" + vid + "_pid_" + pid;
    }

    private String getWatchFileKey(WatchManager manager, String uuid) {
        DeviceInfo deviceInfo = manager.getDeviceInfo();
        int uid = deviceInfo == null ? 0 : deviceInfo.getUid();
        int pid = deviceInfo == null ? 0 : deviceInfo.getPid();
        return getWatchFileKey(uuid, uid, pid);
    }

    private String getWatchFileKey(String uuid, int uid, int pid) {
        return uuid + "_vid_" + uid + "_pid_" + pid;
    }

    private final class WatchProductCallback implements Callback<WatchProductResponse> {
        private final IWatchHttpCallback<WatchProduct> callback;

        public WatchProductCallback(IWatchHttpCallback<WatchProduct> callback) {
            this.callback = callback;
        }

        @Override
        public void onResponse(@NotNull Call<WatchProductResponse> call, @NotNull Response<WatchProductResponse> response) {
            WatchProduct watchProduct = handleRequestOk(response, callback);
            if (null == watchProduct) return;
            watchProductMap.put(getWatchProductKey(watchProduct.getVid(), watchProduct.getPid()), watchProduct);
        }

        @Override
        public void onFailure(@NotNull Call<WatchProductResponse> call, @NotNull Throwable t) {
            callbackFailed(callback, ERR_HTTP_EXCEPTION, t.getMessage());
        }
    }

    private final class WatchFileCallback implements Callback<WatchFileResponse> {
        private final String key;
        private final IWatchHttpCallback<WatchFileMsg> callback;

        public WatchFileCallback(String key, IWatchHttpCallback<WatchFileMsg> callback) {
            this.key = key;
            this.callback = callback;
        }

        @Override
        public void onResponse(@NotNull Call<WatchFileResponse> call, @NotNull Response<WatchFileResponse> response) {
            WatchFileMsg watchFileMsg = handleRequestOk(response, callback);
            if (null == watchFileMsg) return;
            watchFileMap.put(key, watchFileMsg);
        }

        @Override
        public void onFailure(@NotNull Call<WatchFileResponse> call, @NotNull Throwable t) {
            callbackFailed(callback, ERR_HTTP_EXCEPTION, t.getMessage());
        }
    }

    private final class WatchFileListCallback implements Callback<WatchFileListResponse> {
        private final WatchManager manager;
        private final IWatchHttpCallback<WatchFileList> callback;

        public WatchFileListCallback(WatchManager manager, IWatchHttpCallback<WatchFileList> callback) {
            this.manager = manager;
            this.callback = callback;
        }

        @Override
        public void onResponse(@NotNull Call<WatchFileListResponse> call, @NotNull Response<WatchFileListResponse> response) {
            WatchFileList fileList = handleRequestOk(response, callback);
            if (null == fileList) return;
            List<WatchFileMsg> list = fileList.getRecords();
            if (null == list) {
                callbackFailed(callback, ERR_HTTP_FORMAT, "response body is error.");
                return;
            }
            for (WatchFileMsg msg : list) {
                String key = getWatchFileKey(manager, msg.getUuid());
                if (watchFileMap.containsKey(key)) continue;
                watchFileMap.put(msg.getUuid(), msg);
            }
        }

        @Override
        public void onFailure(@NotNull Call<WatchFileListResponse> call, @NotNull Throwable t) {
            callbackFailed(callback, ERR_HTTP_EXCEPTION, t.getMessage());
        }
    }


    public interface IWatchHttpCallback<T> {

        void onSuccess(T result);

        void onFailed(int code, String message);
    }

    public interface OnDownloadListener extends IWatchHttpCallback<String> {

        void onStart();

        void onProgress(int progress);
    }
}
