package com.jieli.healthaide.ui.device.market;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.watch.WatchServerCacheHelper;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.healthaide.ui.device.bean.WatchOpData;
import com.jieli.healthaide.ui.device.market.bean.DialListMsg;
import com.jieli.healthaide.ui.device.market.bean.WatchListResult;
import com.jieli.healthaide.ui.device.watch.WatchViewModel;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.jl_fatfs.FatFsErrCode;
import com.jieli.jl_fatfs.interfaces.OnFatFileProgressListener;
import com.jieli.jl_fatfs.utils.FatUtil;
import com.jieli.jl_health_http.HttpConstant;
import com.jieli.jl_health_http.model.WatchFileList;
import com.jieli.jl_health_http.model.WatchFileMsg;
import com.jieli.jl_health_http.model.WatchProduct;
import com.jieli.jl_health_http.model.param.DialParam;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.model.response.ExternalFlashMsgResponse;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 手表市场ViewModel
 * @since 2021/3/12
 */
@Deprecated
public class WatchMarketViewModel extends WatchViewModel {
    private final static String TAG = WatchMarketViewModel.class.getSimpleName();
    public final MutableLiveData<WatchListResult> mWatchMarketResultMLD = new MutableLiveData<>();
    public final BluetoothDevice mTargetDev;
    private final Fragment mFragment;
    private final DialListMsg mDialListMsg = new DialListMsg();

    private final static int PAGE_NUM = 15;
    private boolean isRequestServer;

    public WatchMarketViewModel(Fragment fragment) {
        super();

        mFragment = fragment;
        mTargetDev = getConnectedDevice();
    }

    @Override
    public void release() {
        super.release();
    }

    @Override
    public void listWatchList() {
        super.listWatchList();
    }

    public void resetServiceParam() {
        mDialListMsg.setSize(0);
        mDialListMsg.setCurrentPage(-1);
        mDialListMsg.setCurrentPage(0);
        mDialListMsg.getList().clear();
    }

    public void loadServiceWatchList() {
        if (mDialListMsg.isLoadFinish()) {
            publishWatchMarketFail(WatchServerCacheHelper.ERR_LOAD_FINISH, mFragment.getString(R.string.last_page));
            return;
        }
        getServiceWatchList(mDialListMsg.getCurrentPage() + 1);
    }

    /**
     * 获取服务器表盘列表
     *
     * @param page 页数
     */
    public void getServiceWatchList(int page) {
        if (!isConnectedDevice(mTargetDev)) {
            publishWatchMarketFail(WatchServerCacheHelper.ERR_REMOTE_NOT_CONNECT, mFragment.getString(R.string.device_is_disconnected));
            return;
        }
        if (mDialListMsg.isLoadFinish()) {
            publishWatchMarketFail(WatchServerCacheHelper.ERR_LOAD_FINISH, mFragment.getString(R.string.last_page));
            return;
        }
        DeviceInfo deviceInfo = getDeviceInfo(mTargetDev);
        if (null == deviceInfo) return;
        int vid = deviceInfo.getUid();//2
        int pid = deviceInfo.getPid();//49
        List<String> versionList = new ArrayList<>();
        DialParam param = new DialParam();
        ExternalFlashMsgResponse externalFlashMsg = mWatchManager.getExternalFlashMsg(mTargetDev);
        if (null != externalFlashMsg) {
            String[] versions = externalFlashMsg.getMatchVersions();
            if (versions != null) {
                versionList.addAll(Arrays.asList(versions));
            }
        }
        if (versionList.isEmpty()) {
            publishWatchMarketFail(WatchServerCacheHelper.ERR_NOT_SUPPORT_VERSION, mFragment.getString(R.string.server_none_device_support_version));
            return;
        }
        if (isRequestServer) {
            return;
        }
        WatchProduct product = mWatchServerCacheHelper.getCacheWatchProduct(vid, pid);
        if (null != product) {
            param.setDialID(product.getId());
        }
        param.setPage(page);
        param.setPid(pid);
        param.setVid(vid);
        param.setSize(PAGE_NUM);
        param.setVersions(versionList);
        param.setFree(true);
        JL_Log.e(tag, "getServiceWatchList", "param : " + param);
        isRequestServer = true;
        mWatchServerCacheHelper.queryWatchFileListByPage(mWatchManager, param, new WatchServerCacheHelper.IWatchHttpCallback<WatchFileList>() {
            @Override
            public void onSuccess(WatchFileList result) {
                isRequestServer = false;
                if (mDialListMsg.isLoadFinish()) {
                    JL_Log.w(tag, "getServiceWatchList", "load finish.");
                    publishWatchMarketFail(WatchServerCacheHelper.ERR_LOAD_FINISH, mFragment.getString(R.string.last_page));
                    return;
                }
                if (result.getCurrent() == 1) {
                    mDialListMsg.getList().clear();
                }

                List<WatchFileMsg> list = result.getRecords();
                mDialListMsg.setTotalPage(result.getPages());
                if (list == null) {
                    onFailed(HttpConstant.ERROR_MISSING_PARAMETER, "Missing param");
                    return;
                }
                mDialListMsg.setCurrentPage(result.getCurrent());
                mDialListMsg.setSize(result.getTotal());
                JL_Log.i(tag, "getServiceWatchList", "mDialListMsg >> " + mDialListMsg + ",\n result = " + result);
                if (!list.isEmpty()) {
                    List<WatchInfo> watchInfos = mergeWatchList(getDeviceWatchList(), list);
                    if (watchInfos != null && !watchInfos.isEmpty()) {
                        for (WatchInfo info : watchInfos) {
                            if (!mDialListMsg.getList().contains(info)) {
                                mDialListMsg.getList().add(info);
                            }
                        }
                    }
                } else {
                    mDialListMsg.setCurrentPage(1);
                    mDialListMsg.setTotalPage(1);
                    mDialListMsg.setSize(getDeviceWatchList().size());
                    mDialListMsg.getList().clear();
                    mDialListMsg.getList().addAll(getDeviceWatchList());
                }
                WatchListResult watchListResult = new WatchListResult(HealthConstant.DIAL_TYPE_FREE);
                watchListResult.setCode(0);
                watchListResult.setResult(mDialListMsg);
                mWatchMarketResultMLD.postValue(watchListResult);
            }

            @Override
            public void onFailed(int code, String message) {
                isRequestServer = false;
                publishWatchMarketFail(code, message);
            }
        });
    }

    public void downloadWatch(String uri, final String outPath) {
        postWatchOpStart(WatchOpData.OP_CREATE_FILE, FatUtil.getFatFilePath(outPath));
        /*if(FileUtil.checkFileExist(outPath)){
            insertWatchFile(outPath);
        }else*/
        {
            mWatchServerCacheHelper.downloadFile(uri, outPath, new WatchServerCacheHelper.OnDownloadListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onProgress(int progress) {

                }

                @Override
                public void onSuccess(String result) {
                    JL_Log.i(TAG, "downloadWatch", "result = " + result);
                    insertWatchFile(result);
                }

                @Override
                public void onFailed(int code, String message) {
                    JL_Log.w(TAG, "downloadWatch", "onFailed ---> code : " + code + ", message = " + message);
                    postWatchOpEnd(WatchOpData.OP_CREATE_FILE, outPath, code, message);
                }
            });
        }
    }

    public void updateWatch(String uri, String outPath) {
        postWatchOpStart(WatchOpData.OP_REPLACE_FILE, FatUtil.getFatFilePath(outPath));
        mWatchServerCacheHelper.downloadFile(uri, outPath, new WatchServerCacheHelper.OnDownloadListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onSuccess(String result) {
                JL_Log.i(TAG, "updateWatch", "result = " + result);
                replaceWatchFile(result);
            }

            @Override
            public void onFailed(int code, String message) {
                JL_Log.w(TAG, "updateWatch", "onFailed ---> code : " + code + ", message = " + message);
                postWatchOpEnd(WatchOpData.OP_REPLACE_FILE, outPath, code, message);
            }
        });
    }

    private List<WatchInfo> getDeviceWatchList() {
        if (null == mWatchListMLD.getValue()) return new ArrayList<>();
        return mWatchListMLD.getValue();
    }

    private void insertWatchFile(final String filePath) {
        if (isCallWorkState()) {
            postWatchOpEnd(WatchOpData.OP_CREATE_FILE, filePath, FatFsErrCode.RES_DEVICE_IS_BUSY, HealthApplication.getAppViewModel().getApplication().getString(R.string.call_phone_error_tips));
            return;
        }
        mWatchManager.addFatFile(filePath, false, new OnFatFileProgressListener() {
            @Override
            public void onStart(String filePath) {
                //postWatchOpStart(WatchOpData.OP_CREATE_FILE, filePath);
            }

            @Override
            public void onProgress(float progress) {
                postWatchOpProgress(WatchOpData.OP_CREATE_FILE, filePath, progress);
            }

            @Override
            public void onStop(int result) {
                JL_Log.e(TAG, "insertWatchFile", "onStop ---> result = " + result);
                if (result == 0) {
//                    listWatchList();
                    enableCurrentWatch(FatUtil.getFatFilePath(filePath));
                }
                postWatchOpEnd(WatchOpData.OP_CREATE_FILE, filePath, result, FatUtil.getFatFsErrorCodeMsg(result));
            }
        });
    }

    private void replaceWatchFile(final String filePath) {
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

    private List<WatchInfo> mergeWatchList(List<WatchInfo> devWatchList, List<WatchFileMsg> list) {
        if (null == devWatchList && null == list) return new ArrayList<>();
        if (null == list || list.isEmpty()) return devWatchList;
        List<WatchInfo> resultList = new ArrayList<>();
        if (devWatchList == null || devWatchList.isEmpty()) {
            for (WatchFileMsg fileMsg : list) {
                WatchInfo watch = new WatchInfo()
                        .setStatus(WatchInfo.WATCH_STATUS_NOT_PAYMENT)
                        .setUuid(fileMsg.getUuid())
                        .setServerFile(fileMsg)
                        .setVersion(fileMsg.getVersion());
                resultList.add(watch);
            }
            return resultList;
        }
        List<WatchFileMsg> deleteList = new ArrayList<>();
        for (WatchInfo info : devWatchList) {
            String infoName = info.getName() == null ? null : info.getName().toUpperCase();
            String infoVersion = info.getVersion() == null ? null : info.getVersion().toUpperCase();
            for (WatchFileMsg fileMsg : list) {
                String watchName = fileMsg.getName() == null ? null : fileMsg.getName().toUpperCase();
                String watchVersion = fileMsg.getVersion() == null ? null : fileMsg.getVersion().toUpperCase();
                if ((null != info.getUuid() && info.getUuid().equalsIgnoreCase(fileMsg.getUuid())
                        || null != infoName && infoName.equalsIgnoreCase(watchName))
                        && null != infoVersion && infoVersion.equalsIgnoreCase(watchVersion)) {
                    info.setServerFile(fileMsg);
                    deleteList.add(fileMsg);
                    break;
                }
            }
            resultList.add(info);
        }
        if (!deleteList.isEmpty()) {
            list.removeAll(deleteList);
        }
        for (WatchFileMsg fileMsg : list) {
            WatchInfo watch = new WatchInfo()
                    .setServerFile(fileMsg)
                    .setUuid(fileMsg.getUuid())
                    .setStatus(WatchInfo.WATCH_STATUS_NONE_EXIST)
                    .setVersion(fileMsg.getVersion());
            boolean isExist = false;
            String watchName = fileMsg.getName() == null ? null : fileMsg.getName().toUpperCase();
            String watchVersion = fileMsg.getVersion() == null ? null : fileMsg.getVersion().toUpperCase();
            for (WatchInfo watchInfo : resultList) {
                String infoName = watchInfo.getName() == null ? null : watchInfo.getName().toUpperCase();
                String infoVersion = watchInfo.getVersion() == null ? null : watchInfo.getVersion().toUpperCase();
                if (watchName != null && watchName.equals(infoName) && watchInfo.getStatus() >= WatchInfo.WATCH_STATUS_EXIST) {
                    isExist = true;
                    if (watchVersion != null && infoVersion != null) {
                        int ret = watchVersion.compareTo(infoVersion);
                        if (ret > 0) {//服务器版本大
                            watchInfo.setUpdateUUID(fileMsg.getUuid())
                                    .setUpdateFile(fileMsg);
                        }
                    }
                    break;
                }
            }
            if (!isExist) {
                resultList.add(watch);
            }
        }
        return resultList;
    }

    private void publishWatchMarketFail(int code, String message) {
        WatchListResult result = new WatchListResult(HealthConstant.DIAL_TYPE_FREE);
        result.setCode(code);
        result.setMessage(message);
        mWatchMarketResultMLD.postValue(result);
    }

    private List<WatchInfo> obtainWatchInfoList(List<WatchFileMsg> serverList) {
        if (null == serverList || serverList.isEmpty()) return new ArrayList<>();
        List<WatchInfo> list = new ArrayList<>();
        for (WatchFileMsg serverFile : serverList) {
            WatchInfo watchInfo = new WatchInfo()
                    .setStatus(WatchInfo.WATCH_STATUS_NONE_EXIST)
                    .setServerFile(serverFile)
                    .setUuid(serverFile.getUuid())
                    .setVersion(serverFile.getVersion());
            mWatchManager.updateWatchInfo(watchInfo);
            list.add(watchInfo);
        }
        return list;
    }

    public static class WatchMarketViewModelFactory implements ViewModelProvider.Factory {
        private final Fragment mFragment;

        public WatchMarketViewModelFactory(Fragment fragment) {
            mFragment = fragment;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new WatchMarketViewModel(mFragment);
        }
    }
}