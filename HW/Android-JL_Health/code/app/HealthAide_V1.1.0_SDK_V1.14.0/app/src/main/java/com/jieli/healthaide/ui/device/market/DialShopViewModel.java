package com.jieli.healthaide.ui.device.market;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.net.NetworkStateHelper;
import com.jieli.healthaide.tool.watch.WatchServerCacheHelper;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.healthaide.ui.device.bean.WatchOpData;
import com.jieli.healthaide.ui.device.market.bean.BooleanResult;
import com.jieli.healthaide.ui.device.market.bean.DialListMsg;
import com.jieli.healthaide.ui.device.market.bean.WatchListResult;
import com.jieli.healthaide.ui.device.watch.WatchViewModel;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_fatfs.FatFsErrCode;
import com.jieli.jl_fatfs.interfaces.OnFatFileProgressListener;
import com.jieli.jl_fatfs.utils.FatUtil;
import com.jieli.jl_health_http.HttpConstant;
import com.jieli.jl_health_http.model.WatchFileList;
import com.jieli.jl_health_http.model.WatchFileMsg;
import com.jieli.jl_health_http.model.WatchProduct;
import com.jieli.jl_health_http.model.param.DialParam;
import com.jieli.jl_health_http.model.watch.DialPayRecord;
import com.jieli.jl_health_http.model.watch.DialPayRecordList;
import com.jieli.jl_rcsp.constant.WatchError;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 表盘商城逻辑处理
 * @since 2022/6/17
 */
public class DialShopViewModel extends WatchViewModel {
    private final static String TAG = DialShopViewModel.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private final Context context;
    private final NetworkStateHelper mNetworkStateHelper = NetworkStateHelper.getInstance();

    public final MutableLiveData<WatchListResult> watchListResultMLD = new MutableLiveData<>();
    public final MutableLiveData<BooleanResult> deleteResultMLD = new MutableLiveData<>();
    private final Map<Integer, DialListMsg> dialListMsgMap = new HashMap<>();

    private boolean isRequestServer; //请求服务器表盘列表
    private final List<String> deleteRecordList = new ArrayList<>();

    public final static int PAGE_NUM = 6;

    public DialShopViewModel(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void release() {
        super.release();
        cleanCache();
    }

    public boolean isPayListEmpty() {
        DialListMsg listMsg = dialListMsgMap.get(HealthConstant.DIAL_TYPE_PAY);
        return listMsg == null || listMsg.getList().isEmpty();
    }

    public boolean isNetworkNotAvailable() {
        return mNetworkStateHelper.getNetWorkStateModel() == null || !mNetworkStateHelper.getNetWorkStateModel().isAvailable();
    }

    public void cleanCache() {
        dialListMsgMap.clear();
    }

    public void updatePayList(WatchInfo watchInfo) {
        if (null == watchInfo) return;
        boolean isChange = false;
        DialListMsg dialListMsg = getDialListMsg(HealthConstant.DIAL_TYPE_PAY);
        List<WatchInfo> list = null == dialListMsg ? new ArrayList<>() : dialListMsg.getList();
        for (WatchInfo cacheInfo : list) {
            boolean isMatch = HealthUtil.isMatchInfo(cacheInfo, watchInfo);
            JL_Log.i(TAG, "updatePayList", "isMatch = " + isMatch);
            if (isMatch) {
                isChange = true;
                JL_Log.i(TAG, "updatePayList", "cacheInfo = " + cacheInfo + ", watchInfo = " + watchInfo);
                cacheInfo.setStatus(watchInfo.getStatus())
                        .setVersion(watchInfo.getVersion())
                        .setCustomBgFatPath(watchInfo.getCustomBgFatPath())
                        .setWatchFile(watchInfo.getWatchFile())
                        .setServerFile(watchInfo.getServerFile())
                        .setUpdateUUID(watchInfo.getUpdateUUID())
                        .setUpdateFile(watchInfo.getUpdateFile())
                        .setCircleDial(watchInfo.isCircleDial());
                JL_Log.i(TAG, "updatePayList", "after cacheInfo = " + cacheInfo);
                break;
            }
        }
        JL_Log.i(TAG, "updatePayList", "isChange = " + isChange);
        if (isChange) {
            mWatchListMLD.postValue(mWatchListMLD.getValue());
        }
    }

    public void loadServerDialList(int dialType) {
        DialListMsg dialListMsg = getDialListMsg(dialType);
        if (null == dialListMsg) {
            dialListMsg = new DialListMsg();
        } else if (dialListMsg.isLoadFinish()) {
            postServerSuccess(dialType, dialListMsg);
            return;
        }
        loadServerDialList(dialType, dialListMsg.getCurrentPage() + 1);
    }

    public void loadServerDialList(int dialType, int page) {
        if (dialType == HealthConstant.DIAL_TYPE_RECORD) {
            queryDialPaymentListByPage(page);
        } else {
            queryServerDialListByPage(dialType == HealthConstant.DIAL_TYPE_FREE, page);
        }
    }

    public void dialPayByFree(WatchInfo watchInfo) {
        if (null == watchInfo || null == watchInfo.getServerFile()) {
            int err = FatFsErrCode.RES_INVALID_PARAMETER;
            postWatchOpEnd(WatchOpData.OP_CREATE_FILE, null, err, WatchError.getErrorDesc(err));
            return;
        }
        mWatchServerCacheHelper.dialPaymentByFree(mWatchManager, watchInfo.getServerFile().getId(), new WatchServerCacheHelper.IWatchHttpCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                downloadDial(watchInfo);
            }

            @Override
            public void onFailed(int code, String message) {
                JL_Log.e(TAG, "dialPaymentByFree", "onFailed ---> code : " + code + ", " + message);
                if (HealthUtil.getHttpErrorCode(message) == HttpConstant.ERROR_REPEAT_BUY_DIAL) {
                    downloadDial(watchInfo);
                    return;
                }
                postWatchOpEnd(WatchOpData.OP_CREATE_FILE, watchInfo.getName(), code, message);
            }
        });
    }

    public void deletePaymentRecord() {
        if (!deleteRecordList.isEmpty()) {
            JL_Log.i(tag, "deletePaymentRecord", "正在删除购买记录中，请勿重复操作");
            return;
        }
        mWatchServerCacheHelper.getDialPayRecordListByPage(mWatchManager, 1, 20, new WatchServerCacheHelper.IWatchHttpCallback<DialPayRecordList>() {
            @Override
            public void onSuccess(DialPayRecordList result) {
                List<DialPayRecord> list = result.getRecords();
                if (null == list || list.isEmpty()) {
                    dialListMsgMap.remove(HealthConstant.DIAL_TYPE_RECORD);
                    BooleanResult booleanResult = new BooleanResult();
                    booleanResult.setResult(true);
                    deleteResultMLD.postValue(booleanResult);
                    return;
                }
                //首先添加任务入列表
                for (DialPayRecord record : list) {
                    deleteRecordList.add(record.getId());
                }
                //开始执行任务
                for (DialPayRecord record : list) {
                    mWatchServerCacheHelper.deleteDialPaymentRecord(mWatchManager, record.getId(), new DeleteResultCallback(record.getId()));
                    SystemClock.sleep(100);
                }
            }

            @Override
            public void onFailed(int code, String message) {
                BooleanResult result = new BooleanResult();
                result.setResult(false);
                result.setCode(code);
                result.setMessage(message);
                deleteResultMLD.postValue(result);
            }
        });
    }

    public void downloadDial(WatchInfo watchInfo) {
        handleDial(WatchOpData.OP_CREATE_FILE, watchInfo);
    }

    public void updateDial(WatchInfo watchInfo) {
        handleDial(WatchOpData.OP_REPLACE_FILE, watchInfo);
    }

    public List<WatchInfo> mergeWatchList(int dialType) {
        List<WatchInfo> list = getDialList(dialType);
        for (WatchInfo info : list) {
            WatchInfo localWatchInfo = getLocalWatchInfo(info.getUuid());
            JL_Log.i(TAG, "mergeWatchList", "dialType = " + dialType + ", " + info + ",\nlocalWatchInfo = " + localWatchInfo);
            if (null == localWatchInfo) continue;
            info.setWatchFile(localWatchInfo.getWatchFile())
                    .setCustomBgFatPath(localWatchInfo.getCustomBgFatPath())
                    .setCircleDial(localWatchInfo.isCircleDial());
            if (info.getStatus() != localWatchInfo.getStatus()) {
                info.setStatus(localWatchInfo.getStatus());
            }
            if (localWatchInfo.hasUpdate()) {
                info.setUpdateUUID(localWatchInfo.getUpdateUUID());
                info.setUpdateFile(localWatchInfo.getUpdateFile());
            }
        }
        return list;
    }

    private List<WatchInfo> getMyDevices() {
        return mWatchListMLD.getValue();
    }

    private DialListMsg getDialListMsg(int dialType) {
        return dialListMsgMap.get(dialType);
    }

    private List<WatchInfo> getDialList(int dialType) {
        DialListMsg dialListMsg = getDialListMsg(dialType);
        if (null == dialListMsg) return new ArrayList<>();
        ArrayList<WatchInfo> clone = new ArrayList<>();
        for (WatchInfo info : dialListMsg.getList()) {
            clone.add(info.clone());
        }
        return clone;
    }

    private void queryServerDialListByPage(boolean isFree, int page) {
        final int dialType = isFree ? HealthConstant.DIAL_TYPE_FREE : HealthConstant.DIAL_TYPE_PAY;
        DeviceInfo deviceInfo = getDeviceInfo(getConnectedDevice());
        if (null == deviceInfo) {
            postServerErrorEvent(dialType, FatFsErrCode.RES_REMOTE_NOT_CONNECT);
            return;
        }
        WatchProduct watchProduct = mWatchServerCacheHelper.getCacheWatchProduct(deviceInfo.getUid(), deviceInfo.getPid());
        if (null == watchProduct) {
            postServerErrorEvent(dialType, FatFsErrCode.RES_INVALID_PARAMETER);
            return;
        }
        if (isRequestServer) return;
        if (checkPageIsInValid(dialType, page)) return;
        DialParam param = new DialParam();
        param.setDialID(watchProduct.getId());
        param.setFree(isFree);
        param.setPage(page);
        param.setSize(PAGE_NUM);
        mWatchServerCacheHelper.queryWatchFileListByPage(mWatchManager, param, new CustomWatchHttpCallback(dialType));
    }

    private void queryDialPaymentListByPage(int page) {
        final int dialType = HealthConstant.DIAL_TYPE_RECORD;
        if (isRequestServer) return;
        if (checkPageIsInValid(dialType, page)) return;
        DeviceInfo deviceInfo = getDeviceInfo(getConnectedDevice());
        if (null == deviceInfo) {
            postServerErrorEvent(dialType, FatFsErrCode.RES_REMOTE_NOT_CONNECT);
            return;
        }
        WatchProduct watchProduct = mWatchServerCacheHelper.getCacheWatchProduct(deviceInfo.getUid(), deviceInfo.getPid());
        if (null == watchProduct) {
            postServerErrorEvent(dialType, FatFsErrCode.RES_INVALID_PARAMETER);
            return;
        }
        mWatchServerCacheHelper.getDialPaymentListByPage(mWatchManager, watchProduct.getId(), page, PAGE_NUM, new CustomWatchHttpCallback(dialType));
    }

    private boolean checkPageIsInValid(int dialType, int page) {
        DialListMsg dialListMsg = getDialListMsg(dialType);
        if (null != dialListMsg && dialListMsg.getTotalPage() >= 0) {
            if (page > dialListMsg.getTotalPage()) {
                if (dialListMsg.getTotalPage() == 0 && dialType == HealthConstant.DIAL_TYPE_FREE) {
                    loadServerDialList(HealthConstant.DIAL_TYPE_PAY);
                    return true;
                }
                postServerErrorEvent(dialType, FatFsErrCode.RES_INVALID_PARAMETER, "Invalid parameter : page = " + page);
                return true;
            } else if (page == dialListMsg.getTotalPage()) {
                if (dialListMsg.isLoadFinish()) {
                    postServerSuccess(dialType, dialListMsg);
                    return true;
                }
            } else if (page <= dialListMsg.getCurrentPage()) {
                postServerSuccess(dialType, dialListMsg);
                return true;
            }
        }
        return false;
    }

    private void handleServerDialList(int dialType, WatchFileList result) {
        DialListMsg dialListMsg = getDialListMsg(dialType);
        if (dialListMsg != null && result.getCurrent() == 1) { //第一页
            dialListMsg.getList().clear();
        }
        if (null == dialListMsg) {
            dialListMsg = new DialListMsg();
            dialListMsgMap.put(dialType, dialListMsg);
        }
        if (dialListMsg.isLoadFinish()) {
            postServerSuccess(dialType, dialListMsg);
            return;
        }
        List<WatchInfo> watchInfos = obtainWatchInfoList(dialType, result.getRecords());
        dialListMsg.setSize(result.getTotal());
        dialListMsg.setCurrentPage(result.getCurrent());
        dialListMsg.setTotalPage(result.getPages());
        if (!watchInfos.isEmpty()) {
            for (WatchInfo info : watchInfos) {
                if (!dialListMsg.getList().contains(info)) {
                    dialListMsg.getList().add(info);
                }
            }
        }
        //回调结果
        postServerSuccess(dialType, dialListMsg);
    }

    private void handleDial(final int op, WatchInfo watchInfo) {
        if (null == watchInfo || null == watchInfo.getServerFile()) {
            postWatchOpEnd(op, null, FatFsErrCode.RES_INVALID_PARAMETER, FatUtil.getFatFsErrorCodeMsg(FatFsErrCode.RES_INVALID_PARAMETER));
            return;
        }
        final String outPath = HealthUtil.createFilePath(context, HealthConstant.DIR_WATCH) + "/" + watchInfo.getName();
        postWatchOpStart(op, FatUtil.getFatFilePath(outPath));
        mWatchServerCacheHelper.downloadDial(mWatchManager, watchInfo.getServerFile().getId(), outPath, new WatchServerCacheHelper.OnDownloadListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onSuccess(String result) {
                if (op == WatchOpData.OP_CREATE_FILE) {
                    createDial(outPath);
                } else if (op == WatchOpData.OP_REPLACE_FILE) {
                    replaceDial(outPath);
                } else { //unknown operation
                    onFailed(FatFsErrCode.RES_OP_NOT_ALLOW, FatUtil.getFatFsErrorCodeMsg(FatFsErrCode.RES_OP_NOT_ALLOW));
                }
            }

            @Override
            public void onFailed(int code, String message) {
                JL_Log.e(TAG, "handleDial", "onFailed ---> code : " + code + ", " + message + ", outPath = " + outPath);
                postWatchOpEnd(op, outPath, code, message);
            }
        });
    }

    private WatchInfo getLocalWatchInfo(String uuid) {
        if (TextUtils.isEmpty(uuid)) return null;
        List<WatchInfo> localDeviceList = getMyDevices();
        if (null != localDeviceList && !localDeviceList.isEmpty()) {
            for (WatchInfo info : localDeviceList) {
                if (uuid.equals(info.getUuid())) {
                    return info;
                }
            }
        }
        return null;
    }

    private List<WatchInfo> obtainWatchInfoList(int dialType, List<WatchFileMsg> serverList) {
        if (null == serverList || serverList.isEmpty()) return new ArrayList<>();
        List<WatchInfo> list = new ArrayList<>();
        for (WatchFileMsg serverFile : serverList) {
            boolean isPay = dialType == HealthConstant.DIAL_TYPE_RECORD || serverFile.isPayStatus();
            WatchInfo watchInfo = new WatchInfo()
                    .setStatus(isPay ? WatchInfo.WATCH_STATUS_NONE_EXIST : WatchInfo.WATCH_STATUS_NOT_PAYMENT)
                    .setServerFile(serverFile)
                    .setUuid(serverFile.getUuid())
                    .setVersion(serverFile.getVersion());
            mWatchManager.updateWatchInfo(watchInfo);
            list.add(watchInfo);
        }
        return list;
    }

    private void postServerSuccess(int dialType, DialListMsg dialListMsg) {
        WatchListResult requestResult = new WatchListResult(dialType);
        requestResult.setCode(0);
        requestResult.setResult(dialListMsg);
        watchListResultMLD.postValue(requestResult);
    }

    private void postServerErrorEvent(int dialType, int code) {
        postServerErrorEvent(dialType, code, FatUtil.getFatFsErrorCodeMsg(code));
    }

    private void postServerErrorEvent(int dialType, int code, String message) {
        WatchListResult result = new WatchListResult(dialType);
        result.setCode(code);
        result.setMessage(message);
        watchListResultMLD.postValue(result);
    }

    private void createDial(String dialPath) {
        if (isCallWorkState()) {
            postWatchOpEnd(WatchOpData.OP_CREATE_FILE, dialPath, FatFsErrCode.RES_DEVICE_IS_BUSY, context.getString(R.string.call_phone_error_tips));
            return;
        }
        mWatchManager.addFatFile(dialPath, false, new OnFatFileProgressListener() {
            @Override
            public void onStart(String filePath) {
                //postWatchOpStart(WatchOpData.OP_CREATE_FILE, filePath);
            }

            @Override
            public void onProgress(float progress) {
                postWatchOpProgress(WatchOpData.OP_CREATE_FILE, dialPath, progress);
            }

            @Override
            public void onStop(int result) {
                JL_Log.w(TAG, "createDial", "onStop ---> result = " + result);
                if (result == FatFsErrCode.RES_OK) {
                    enableCurrentWatch(FatUtil.getFatFilePath(dialPath));
                }
                postWatchOpEnd(WatchOpData.OP_CREATE_FILE, dialPath, result, FatUtil.getFatFsErrorCodeMsg(result));
            }
        });
    }

    private void replaceDial(String filePath) {
        if (isCallWorkState()) {
            postWatchOpEnd(WatchOpData.OP_REPLACE_FILE, filePath, FatFsErrCode.RES_DEVICE_IS_BUSY, context.getString(R.string.call_phone_error_tips));
            return;
        }
        mWatchManager.replaceWatchFile(filePath, new OnFatFileProgressListener() {
            @Override
            public void onStart(String filePath) {
                //postWatchOpStart(WatchOpData.OP_REPLACE_FILE, filePath);
            }

            @Override
            public void onProgress(float progress) {
                postWatchOpProgress(WatchOpData.OP_REPLACE_FILE, filePath, progress);
            }

            @Override
            public void onStop(int result) {
                if (result == FatFsErrCode.RES_OK) {
                    mWatchManager.updateWatchFileListByDevice(new OnWatchOpCallback<ArrayList<WatchInfo>>() {
                        @Override
                        public void onSuccess(ArrayList<WatchInfo> result) {
                            enableCurrentWatch(FatUtil.getFatFilePath(filePath));
                            postWatchOpEnd(WatchOpData.OP_REPLACE_FILE, filePath, FatFsErrCode.RES_OK, FatUtil.getFatFsErrorCodeMsg(FatFsErrCode.RES_OK));
                        }

                        @Override
                        public void onFailed(BaseError error) {
                            postWatchOpEnd(WatchOpData.OP_REPLACE_FILE, filePath, FatFsErrCode.RES_RCSP_SEND, FatUtil.getFatFsErrorCodeMsg(FatFsErrCode.RES_RCSP_SEND));
                        }
                    });
                } else {
                    postWatchOpEnd(WatchOpData.OP_REPLACE_FILE, filePath, result, FatUtil.getFatFsErrorCodeMsg(result));
                }
            }
        });
    }

    private void removeRequestId(String id) {
        if (deleteRecordList.remove(id)) {
            if (deleteRecordList.isEmpty()) {
                dialListMsgMap.remove(HealthConstant.DIAL_TYPE_RECORD);
                mWatchServerCacheHelper.cleanWatchFileCache();
                BooleanResult result = new BooleanResult();
                result.setResult(true);
                deleteResultMLD.postValue(result);
                context.sendBroadcast(new Intent(WatchDialFragment.RemovePaymentReceiver.ACTION_REMOVE_PAYMENT));
            }
        }
    }

    private class DeleteResultCallback implements WatchServerCacheHelper.IWatchHttpCallback<Boolean> {
        private final String id; //购买记录ID

        public DeleteResultCallback(String id) {
            this.id = id;
        }

        @Override
        public void onSuccess(Boolean result) {
            removeRequestId(id);
        }

        @Override
        public void onFailed(int code, String message) {
            removeRequestId(id);
        }
    }

    private class CustomWatchHttpCallback implements WatchServerCacheHelper.IWatchHttpCallback<WatchFileList> {
        private final int dialType;

        CustomWatchHttpCallback(int dialType) {
            isRequestServer = true;
            this.dialType = dialType;
        }

        @Override
        public void onSuccess(WatchFileList result) {
            isRequestServer = false;
            handleServerDialList(dialType, result);
        }

        @Override
        public void onFailed(int code, String message) {
            isRequestServer = false;
            postServerErrorEvent(dialType, code, message);
        }
    }

    public static class DialShopViewModelFactory implements ViewModelProvider.Factory {
        private final Context context;

        public DialShopViewModelFactory(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new DialShopViewModel(context);
        }
    }
}
