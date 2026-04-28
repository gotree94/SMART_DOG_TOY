package com.jieli.healthaide.ui.device.watch;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.jieli.bluetooth_connect.bean.history.HistoryRecord;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.constant.BluetoothError;
import com.jieli.bluetooth_connect.impl.BluetoothManager;
import com.jieli.bluetooth_connect.interfaces.IBluetoothOperation;
import com.jieli.bluetooth_connect.interfaces.callback.OnHistoryRecordCallback;
import com.jieli.bluetooth_connect.util.BluetoothUtil;
import com.jieli.bluetooth_connect.util.CHexConverter;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.bluetooth.BluetoothEventListener;
import com.jieli.healthaide.tool.history.HistoryRecordManager;
import com.jieli.healthaide.tool.history.OnServiceRecordListener;
import com.jieli.healthaide.tool.watch.OnDeviceConfigureListener;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.tool.watch.WatchServerCacheHelper;
import com.jieli.healthaide.ui.device.BluetoothViewModel;
import com.jieli.healthaide.ui.device.bean.DevPowerMsg;
import com.jieli.healthaide.ui.device.bean.DevRecordListBean;
import com.jieli.healthaide.ui.device.bean.DeviceConnectionData;
import com.jieli.healthaide.ui.device.bean.DeviceHistoryRecord;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.healthaide.ui.device.bean.WatchOpData;
import com.jieli.healthaide.ui.device.bean.WatchStatus;
import com.jieli.healthaide.ui.device.history.HistoryRecordViewModel;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_fatfs.FatFsErrCode;
import com.jieli.jl_fatfs.interfaces.OnFatFileProgressListener;
import com.jieli.jl_fatfs.model.FatFile;
import com.jieli.jl_fatfs.utils.FatUtil;
import com.jieli.jl_health_http.model.WatchFileMsg;
import com.jieli.jl_health_http.model.WatchProduct;
import com.jieli.jl_health_http.model.device.DevMessage;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.constant.WatchConstant;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.BatteryInfo;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.model.response.TargetInfoResponse;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 手表操作ViewModel
 * @since 2021/3/10
 */
public class WatchViewModel extends BluetoothViewModel {
    protected final WatchManager mWatchManager = WatchManager.getInstance();
    protected final WatchServerCacheHelper mWatchServerCacheHelper = WatchServerCacheHelper.getInstance();
    public final MutableLiveData<DeviceConnectionData> mConnectionDataMLD = new MutableLiveData<>();
    public final MutableLiveData<WatchStatus> mWatchStatusMLD = new MutableLiveData<>();
    public final MutableLiveData<ArrayList<WatchInfo>> mWatchListMLD = new MutableLiveData<>();
    public final MutableLiveData<WatchOpData> mWatchOpDataMLD = new MutableLiveData<>();
    public final MutableLiveData<BaseError> mErrorMLD = new MutableLiveData<>();
    public final MutableLiveData<WatchProductMsgResult> mWatchProductMsgResultMLD = new MutableLiveData<>();
    public final MutableLiveData<DevPowerMsg> mDevPowerMsgMLD = new MutableLiveData<>();
    public final MutableLiveData<DevRecordListBean> mHistoryRecordListMLD = new MutableLiveData<>();
    public final MutableLiveData<Integer> mHistoryRecordChangeMLD = new MutableLiveData<>();
    public final MutableLiveData<HistoryRecordViewModel.HistoryConnectStatus> mHistoryConnectStatusMLD = new MutableLiveData<>();
    public final MutableLiveData<BluetoothDevice> mDeviceConfigureMLD = new MutableLiveData<>();

    private final HistoryRecordManager mHistoryRecordManager = HistoryRecordManager.getInstance();

    private boolean isRequestWatchMsg;
    private DeviceHistoryRecord reconnectRecord;

    private final int MSG_RECONNECT_HISTORY_TIMEOUT = 2034;
    private final Handler mUIHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (MSG_RECONNECT_HISTORY_TIMEOUT == msg.what && reconnectRecord != null) {
                BluetoothDevice device = getRemoteDevice(reconnectRecord.getHistoryRecord().getAddress());
                if (isConnectedDevice(device)) {
                    callbackReconnectHistorySuccess();
                } else {
                    callbackReconnectHistoryFailure();
                }
                reconnectRecord = null;
            }
            return true;
        }
    });

    public WatchViewModel() {
        super();
        mWatchManager.addOnDeviceConfigureListener(mOnDeviceConfigureListener);
        mBluetoothHelper.addBluetoothEventListener(mEventListener);
        mWatchManager.registerOnWatchCallback(mOnWatchCallback);
        mHistoryRecordManager.addOnServiceRecordListener(mRecordListener);
    }

    public void release() {
        mHistoryRecordManager.removeOnServiceRecordListener(mRecordListener);
        mBluetoothHelper.removeBluetoothEventListener(mEventListener);
        mWatchManager.removeOnDeviceConfigureListener(mOnDeviceConfigureListener);
        mWatchManager.unregisterOnWatchCallback(mOnWatchCallback);
    }

    public boolean isBleChangeSpp() {
        return mWatchManager.isBleChangeSpp();
    }

    public DeviceInfo getDeviceInfo(BluetoothDevice device) {
        return mWatchManager.getDeviceInfo(device);
    }

    public WatchConfigure getWatchConfigure(BluetoothDevice device) {
        return mWatchManager.getWatchConfigure(device);
    }

    public boolean isWatchSystemInit(BluetoothDevice device) {
        if (!isConnectedDevice(device)) return false;
        return mWatchManager.isWatchSystemInit(device);
    }

    public boolean isSupportDialPayment() {
        return mWatchServerCacheHelper.isSupportDialPayment(mWatchManager);
    }

    public int getConnectedDeviceConnectWay(BluetoothDevice device) {
        if (!mBluetoothHelper.isConnectedBtDevice(device)) return -1;
        BluetoothManager btOp = mBluetoothHelper.getBluetoothOp();
        return btOp.isConnectedSppDevice(device) ? BluetoothConstant.PROTOCOL_TYPE_SPP : btOp.isConnectedGattOverBrEdrDevice(device)
                ? BluetoothConstant.PROTOCOL_TYPE_GATT_OVER_BR_EDR : BluetoothConstant.PROTOCOL_TYPE_BLE;
    }

    public void syncHistoryRecordList() {
        List<HistoryRecord> historyRecordList = mBluetoothHelper.getBluetoothOp().getHistoryRecordList();
        List<DeviceHistoryRecord> list = convertDeviceHistoryRecordByLocal(historyRecordList);
        mHistoryRecordListMLD.setValue(new DevRecordListBean(list, obtainUsingIndex(list))); //返回本地数据
        mHistoryRecordManager.syncHistoryRecordList();//请求服务器数据
    }

    public void listWatchList() {
        mWatchManager.listWatchFileList(new OnWatchOpCallback<ArrayList<WatchInfo>>() {
            @Override
            public void onSuccess(ArrayList<WatchInfo> result) {
                List<String> taskList = new ArrayList<>();
                JL_Log.d(tag, "listWatchList", CalendarUtil.formatString("===============>>start[%d]<<===============", result.size()));
                for (WatchInfo info : result) {
                    JL_Log.d(tag, "listWatchList", info.toString());
                    if (!TextUtils.isEmpty(info.getUuid()) && mWatchServerCacheHelper.getCacheWatchServerMsg(mWatchManager, info.getUuid()) == null) {
                        taskList.add(info.getUuid());
                        JL_Log.d(tag, "listWatchList", "add Task --> " + info.getUuid());
                    }
                }
                JL_Log.d(tag, "listWatchList", "===============>>end<<===============");
                if (!taskList.isEmpty()) {
                    if (!isRequestWatchMsg) {
                        isRequestWatchMsg = true;
                        //开始请求表盘信息
                        requestWatchMsgList(taskList);
                    }
                }

                mWatchListMLD.postValue(result);

                mWatchManager.getCurrentWatchMsg(new OnWatchOpCallback<WatchInfo>() {
                    @Override
                    public void onSuccess(WatchInfo result) {
                        updateUsingWatch(result);
                    }

                    @Override
                    public void onFailed(BaseError error) {
                        callbackFailed(error.getSubCode(), error.getMessage());
                    }
                });
            }

            @Override
            public void onFailed(BaseError error) {
                JL_Log.e(tag, "listWatchList", "onFailed: " + error);
                callbackFailed(error.getSubCode(), error.getMessage());
            }
        });
    }

    public void syncWatchList() {
        mWatchManager.watchInfoList.clear();
        listWatchList();
    }

    public void enableCurrentWatch(String fatPath) {
        mWatchManager.setCurrentWatchInfo(fatPath, new OnWatchOpCallback<FatFile>() {
            @Override
            public void onSuccess(FatFile result) {
                WatchInfo watchInfo = mWatchManager.getWatchInfoByFatFile(result);
                JL_Log.d(tag, "enableCurrentWatch", "watchInfo = " + watchInfo + ", result = " + result);
                updateUsingWatch(watchInfo);
            }

            @Override
            public void onFailed(BaseError error) {
                JL_Log.w(tag, "enableCurrentWatch", "onFailed = " + error);
                callbackFailed(error.getSubCode(), error.getMessage());
            }
        });
    }

    public void deleteWatch(WatchInfo info) {
        if (null == info) {
            postWatchOpEnd(WatchOpData.OP_DELETE_FILE, null, FatFsErrCode.RES_INVALID_PARAMETER, FatUtil.getFatFsErrorCodeMsg(FatFsErrCode.RES_INVALID_PARAMETER));
            return;
        }
        String filePath = info.getWatchFile() == null ? null : info.getWatchFile().getPath();
        if (mWatchListMLD.getValue() == null || mWatchListMLD.getValue().size() <= 2) {
            postWatchOpEnd(WatchOpData.OP_DELETE_FILE, filePath, FatFsErrCode.RES_OP_NOT_ALLOW, "Device has at least two dial faces");
            return;
        }
        if (isCallWorkState()) {
            postWatchOpEnd(WatchOpData.OP_DELETE_FILE, filePath, FatFsErrCode.RES_DEVICE_IS_BUSY, HealthApplication.getAppViewModel().getApplication().getString(R.string.call_phone_error_tips));
            return;
        }
        mWatchManager.deleteWatch(info, new OnFatFileProgressListener() {
            @Override
            public void onStart(String filePath) {
                postWatchOpStart(WatchOpData.OP_DELETE_FILE, filePath);
            }

            @Override
            public void onProgress(float progress) {
                postWatchOpProgress(WatchOpData.OP_DELETE_FILE, filePath, progress);
            }

            @Override
            public void onStop(int result) {
                postWatchOpEnd(WatchOpData.OP_DELETE_FILE, filePath, result, FatUtil.getFatFsErrorCodeMsg(result));
            }
        });
    }

    /**
     * 获取手表产品信息
     */
    public void getWatchProductMsg(int uid, int pid) {
        JL_Log.i(tag, "getWatchProductMsg", "uid = " + uid + ", pid = " + pid);
        mWatchServerCacheHelper.getWatchProductMsg(uid, pid, new WatchServerCacheHelper.IWatchHttpCallback<WatchProduct>() {
            @Override
            public void onSuccess(WatchProduct result) {
                JL_Log.i(tag, "getWatchProductMsg", "result = " + result);
                WatchProductMsgResult msgResult = new WatchProductMsgResult();
                msgResult.setOk(true);
                msgResult.setProduct(result);
                mWatchProductMsgResultMLD.postValue(msgResult);
            }

            @Override
            public void onFailed(int code, String message) {
                JL_Log.e(tag, "getWatchProductMsg", "onFailed = " + code + ", message = " + message);
                WatchProductMsgResult msgResult = new WatchProductMsgResult();
                msgResult.setOk(false);
                msgResult.setCode(code);
                msgResult.setMessage(message);
                mWatchProductMsgResultMLD.postValue(msgResult);
            }
        });
    }

    public void reconnectHistory(DeviceHistoryRecord historyRecord) {
        if (null == historyRecord) return;
        HistoryRecordViewModel.HistoryConnectStatus status = new HistoryRecordViewModel.HistoryConnectStatus();
        status.setConnectStatus(StateCode.CONNECTION_CONNECTING);
        status.setRecord(historyRecord);
        mHistoryConnectStatusMLD.postValue(status);
        if (historyRecord.getSource() == DeviceHistoryRecord.SOURCE_SERVER) {
            this.reconnectRecord = historyRecord;
            HistoryRecordManager.getInstance().reconnectHistory(historyRecord.getHistoryRecord(), mOnHistoryRecordCallback);
            return;
        }
        //直连设备
       /* if (!mUIHandler.hasMessages(MSG_RECONNECT_HISTORY_TIMEOUT)) {
            JL_Log.e(TAG, "reconnectHistory : " + historyRecord.getHistoryRecord());
            boolean isSpecialWay = historyRecord.getHistoryRecord().getDevType() == JL_DeviceType.JL_DEVICE_TYPE_WATCH
                    && historyRecord.getHistoryRecord().getConnectType() == BluetoothConstant.PROTOCOL_TYPE_SPP;
            BluetoothDevice device = isSpecialWay ?
                    getRemoteDevice(historyRecord.getHistoryRecord().getMappedAddress()) :
                    getRemoteDevice(historyRecord.getHistoryRecord().getAddress());
            if (device == null) {
                callbackReconnectHistoryFailure();
                return;
            }
            int connectWay = isSpecialWay ? BluetoothConstant.PROTOCOL_TYPE_BLE : historyRecord.getHistoryRecord().getConnectType();
            JL_Log.e(TAG, "reconnectHistory : device : " + device + ", connectWay = " + connectWay);
            if (mBluetoothHelper.getBluetoothOp().connectBtDevice(device, connectWay)) {//开始回连设备
                this.reconnectRecord = historyRecord;
                mUIHandler.sendEmptyMessageDelayed(MSG_RECONNECT_HISTORY_TIMEOUT, 36 * 1000);
            } else { //回连失败
                callbackReconnectHistoryFailure();
            }
        }*/
        connectHistoryRecord(historyRecord.getHistoryRecord(), mOnHistoryRecordCallback);
    }

    public boolean isCallWorkState() {
        DeviceInfo deviceInfo = getDeviceInfo(getConnectedDevice());
        return deviceInfo != null && deviceInfo.getPhoneStatus() == WatchConstant.DEVICE_PHONE_STATUS_CALLING;
    }

    private WatchInfo getUsingWatch() {
        WatchInfo usingWatch = null;
        ArrayList<WatchInfo> list = mWatchListMLD.getValue();
        if (list != null && !list.isEmpty()) {
            for (WatchInfo info : list) {
                if (info.getStatus() == WatchInfo.WATCH_STATUS_USING) {
                    usingWatch = info;
                    break;
                }
            }
        }
        return usingWatch;
    }

    private List<DeviceHistoryRecord> convertDeviceHistoryRecordByLocal(List<HistoryRecord> historyList) {
        if (historyList == null || historyList.isEmpty()) {
            return new ArrayList<>();
        }
        List<DeviceHistoryRecord> list = new ArrayList<>();
        for (HistoryRecord record : historyList) {
            DeviceHistoryRecord deviceHistoryRecord = new DeviceHistoryRecord(record);
            deviceHistoryRecord.setSource(DeviceHistoryRecord.SOURCE_LOCAL);
            BluetoothDevice device = null;
            if (CHexConverter.checkBluetoothAddress(record.getUpdateAddress())) {
                device = getRemoteDevice(record.getUpdateAddress());
                int status = getDeviceConnection(device);
                if (status == BluetoothConstant.CONNECT_STATE_DISCONNECT) { //当前状态为无效状态
                    device = null;
                }
            }
            if (null == device) {
                device = getRemoteDevice(record.getAddress());
            }
            if (device != null) {
                int status = getDeviceConnection(device);
                JL_Log.i(tag, "convertDeviceHistoryRecordByLocal", "status : " + status + ", " + HealthUtil.printBtDeviceInfo(device));
                deviceHistoryRecord.setStatus(status);
                if (status == BluetoothConstant.CONNECT_STATE_CONNECTED) {
                    deviceHistoryRecord.setConnectedDev(device);
                    TargetInfoResponse deviceInfo = mWatchManager.getDeviceInfo(device);
                    if (deviceInfo != null) {
                        int battery = deviceInfo.getQuantity();
                        deviceHistoryRecord.setBattery(battery);
                    }
                } else {
                    deviceHistoryRecord.setBattery(0);
                }
            }
            list.add(deviceHistoryRecord);
        }
        return list;
    }

    private int obtainUsingIndex(List<DeviceHistoryRecord> list) {
        int index = 0;
        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                DeviceHistoryRecord historyRecord = list.get(i);
                if (historyRecord.getStatus() == BluetoothConstant.CONNECT_STATE_CONNECTED) {
                    BluetoothDevice device = getRemoteDevice(historyRecord.getHistoryRecord().getAddress());
                    if (device != null && BluetoothUtil.deviceEquals(device, getConnectedDevice())) {
                        index = i;
                        break;
                    }
                }
            }
        }
        return index;
    }

    private void updateUsingWatch(WatchInfo watchInfo) {
        if (watchInfo == null || watchInfo.getWatchFile() == null) return;
        List<WatchInfo> cache = mWatchListMLD.getValue();
        if (null == cache) cache = new ArrayList<>();
        ArrayList<WatchInfo> list = new ArrayList<>(cache);
        if (!list.isEmpty()) {
            for (WatchInfo info : list) {
                if (info.getStatus() <= WatchInfo.WATCH_STATUS_NONE_EXIST || info.getWatchFile() == null)
                    continue;
                if (info.getWatchFile().getPath().equals(watchInfo.getWatchFile().getPath())) {
                    info.setStatus(WatchInfo.WATCH_STATUS_USING);
                } else {
                    info.setStatus(WatchInfo.WATCH_STATUS_EXIST);
                }
            }
        }
        mWatchListMLD.postValue(list);
    }

    private void updateWatchMsg(WatchFileMsg fileMsg) {
        if (null == fileMsg) return;
        ArrayList<WatchInfo> list = mWatchListMLD.getValue();
        if (list != null && !list.isEmpty()) {
            for (WatchInfo info : list) {
                if (info.getUuid().equals(fileMsg.getUuid())) {
                    info.setServerFile(fileMsg);
                    break;
                }
            }
        }
    }

    private void requestWatchMsgList(final List<String> taskList) {
        if (taskList.isEmpty()) {
            isRequestWatchMsg = false;
            mWatchListMLD.postValue(mWatchListMLD.getValue());
            return;
        }
        String uuid = taskList.remove(0);
        JL_Log.d(tag, "requestWatchMsgList", "uuid = " + uuid);
        mWatchServerCacheHelper.queryWatchInfoByUUID(mWatchManager, uuid, new WatchServerCacheHelper.IWatchHttpCallback<WatchFileMsg>() {
            @Override
            public void onSuccess(WatchFileMsg result) {
                JL_Log.d(tag, "requestWatchMsgList", "result = " + result);
                updateWatchMsg(result);
                requestWatchMsgList(taskList);
            }

            @Override
            public void onFailed(int code, String message) {
                JL_Log.w(tag, "requestWatchMsgList", "code = " + code + ", message = " + message);
                requestWatchMsgList(taskList);
            }
        });
    }

    private void callbackFailed(int code, String message) {
        BaseError error = new BaseError();
        error.setSubCode(code);
        error.setMessage(message);
        mErrorMLD.postValue(error);
    }

    protected void postWatchOpStart(int op, String filePath) {
        WatchOpData opData = new WatchOpData();
        opData.setOp(op);
        opData.setState(WatchOpData.STATE_START);
        opData.setFilePath(filePath);
        mWatchOpDataMLD.setValue(opData);
    }

    protected void postWatchOpProgress(int op, String filePath, float progress) {
        WatchOpData opData = new WatchOpData();
        opData.setOp(op);
        opData.setFilePath(filePath);
        opData.setState(WatchOpData.STATE_PROGRESS);
        opData.setProgress(progress);
        mWatchOpDataMLD.setValue(opData);
    }

    protected void postWatchOpEnd(int op, String filePath, int result, String message) {
        WatchOpData opData = new WatchOpData();
        opData.setOp(op);
        opData.setFilePath(filePath);
        opData.setState(WatchOpData.STATE_END);
        opData.setResult(result);
        opData.setMessage(message);
        mWatchOpDataMLD.setValue(opData);
    }

    private void callbackReconnectHistoryFailure() {
        callbackReconnectHistoryFailure(BluetoothError.ERR_NOT_CONNECT_REMOTE, "Connect device failed.");
    }

    private void callbackReconnectHistoryFailure(int code, String message) {
        if (reconnectRecord == null) return;
        HistoryRecordViewModel.HistoryConnectStatus status = new HistoryRecordViewModel.HistoryConnectStatus();
        status.setConnectStatus(StateCode.CONNECTION_DISCONNECT);
        status.setRecord(reconnectRecord);
        status.setCode(code);
        status.setMessage(message);
        mHistoryConnectStatusMLD.postValue(status);
        reconnectRecord = null;
    }

    private void callbackReconnectHistorySuccess() {
        if (reconnectRecord == null) return;
        HistoryRecordViewModel.HistoryConnectStatus status = new HistoryRecordViewModel.HistoryConnectStatus();
        status.setConnectStatus(StateCode.CONNECTION_OK);
        status.setRecord(reconnectRecord);
        mHistoryConnectStatusMLD.postValue(status);
        reconnectRecord = null;
    }

    private BluetoothDevice getRemoteDevice(String address) {
        return HealthUtil.getRemoteDevice(address);
    }

    private final Runnable cbDeviceConnected = () -> {
        final BluetoothDevice device = getConnectedDevice();
        boolean isBleChangeSpp = isBleChangeSpp();
        JL_Log.i(tag, "cbDeviceConnected", "isBleChangeSpp = " + isBleChangeSpp);
        if (isBleChangeSpp) return;
        WatchStatus watchStatus = new WatchStatus(device);
        watchStatus.setException(0);
        mConnectionDataMLD.setValue(new DeviceConnectionData(device, BluetoothConstant.CONNECT_STATE_CONNECTED));
        mWatchStatusMLD.postValue(watchStatus);
        if (HealthConstant.SYNC_DEV_POWER) mWatchManager.requestDevicePower(null); //请求同步电量
    };

    private final BluetoothEventListener mEventListener = new BluetoothEventListener() {
        @Override
        public void onConnection(BluetoothDevice device, int status) {
            boolean isBleChangeSpp = isBleChangeSpp();
            JL_Log.i(tag, "onConnection", "device = " + HealthUtil.printBtDeviceInfo(device) + ", status = " + status + ", isBleChangeSpp = " + isBleChangeSpp);
            if (mUIHandler.hasMessages(MSG_RECONNECT_HISTORY_TIMEOUT) && reconnectRecord != null) {
                BluetoothDevice reconnectDevice = getRemoteDevice(reconnectRecord.getHistoryRecord().getAddress());
                if (BluetoothUtil.deviceEquals(reconnectDevice, device)) {
                    if (status == BluetoothConstant.CONNECT_STATE_CONNECTED) {
                        callbackReconnectHistorySuccess();
                        mUIHandler.removeMessages(MSG_RECONNECT_HISTORY_TIMEOUT);
                    } else if (status == BluetoothConstant.CONNECT_STATE_DISCONNECT) {
                        callbackReconnectHistoryFailure();
                        mUIHandler.removeMessages(MSG_RECONNECT_HISTORY_TIMEOUT);
                    }
                }
            }
            if (status == BluetoothConstant.CONNECT_STATE_DISCONNECT) {
                mUIHandler.removeCallbacks(cbDeviceConnected);
            }
            if (status != BluetoothConstant.CONNECT_STATE_CONNECTED && !isBleChangeSpp) {
                mConnectionDataMLD.setValue(new DeviceConnectionData(device, status));
            }
        }

        @Override
        public void onHistoryRecord(int op, HistoryRecord record) {
            if (op == 2 && isBleChangeSpp()) return;
            mHistoryRecordChangeMLD.postValue(op);
        }
    };

    private final OnWatchCallback mOnWatchCallback = new OnWatchCallback() {
        @Override
        public void onRcspInit(BluetoothDevice device, boolean isInit) {
            JL_Log.i(tag, "onRcspInit", "device : " + device + ", isInit = " + isInit);
        }

        @Override
        public void onWatchSystemInit(int code) {
            JL_Log.i(tag, "onWatchSystemInit", "code = " + code);
            final BluetoothDevice device = mWatchManager.getConnectedDevice();
            WatchStatus watchStatus = new WatchStatus(device);
            watchStatus.setException(code);
            if (code != 0) {
                mWatchStatusMLD.postValue(watchStatus);
                mBluetoothHelper.disconnectDevice(device);
            } else { //初始化成功
                mUIHandler.removeCallbacks(cbDeviceConnected);
                mUIHandler.postDelayed(cbDeviceConnected, 500L);
            }
        }

        @Override
        public void onWatchSystemException(BluetoothDevice device, int sysStatus) {
            JL_Log.i(tag, "onWatchSystemException", "device : " + device + ", sysStatus : " + sysStatus);
        }

        @Override
        public void onMandatoryUpgrade(BluetoothDevice device) {
            mConnectionDataMLD.setValue(new DeviceConnectionData(device, BluetoothConstant.CONNECT_STATE_CONNECTED));
        }

        @Override
        public void onCurrentWatchInfo(BluetoothDevice device, String fatFilePath) {
            JL_Log.d(tag, "onCurrentWatchInfo", "device : " + HealthUtil.printBtDeviceInfo(device) + ", fatFilePath = " + fatFilePath);
            if (null == fatFilePath) return;
            WatchInfo currentWatch = getUsingWatch();
            JL_Log.i(tag, "onCurrentWatchInfo", "currentWatch = " + currentWatch);
            /*
             * 因为每次切模式，小机都会发送当前表盘信息过来。不知道是否是固件BUG
             * 现在判断当前表盘相同就不更新
             */
            /*if (currentWatch == null || currentWatch.getWatchFile() == null || !fatFilePath.equals(currentWatch.getWatchFile().getPath()))*/
            {
                WatchInfo watchInfo = mWatchManager.getWatchInfoByPath(fatFilePath);
                JL_Log.i(tag, "onCurrentWatchInfo", "update  >>> watchInfo = " + watchInfo);
                updateUsingWatch(watchInfo);
            }
        }

        @Override
        public void onDevicePower(BluetoothDevice device, BatteryInfo batteryInfo) {
            JL_Log.i(tag, "onDevicePower", "" + batteryInfo);
            mDevPowerMsgMLD.postValue(new DevPowerMsg(device, batteryInfo));
        }
    };

    private final OnServiceRecordListener mRecordListener = new OnServiceRecordListener() {
        @Override
        public void onServiceRecord(List<DeviceHistoryRecord> recordList) {
            DevRecordListBean recordListBean = mHistoryRecordListMLD.getValue();
            if (recordListBean == null || recordListBean.getList() == null || recordListBean.getList().isEmpty()) {
                mHistoryRecordListMLD.setValue(new DevRecordListBean(recordList, obtainUsingIndex(recordList)));
                return;
            }
            List<DeviceHistoryRecord> records = recordListBean.getList();
            for (DeviceHistoryRecord record : recordList) {
                if (!records.contains(record)) {
                    records.add(record);
                }
            }
            mHistoryRecordListMLD.setValue(new DevRecordListBean(records, obtainUsingIndex(records)));
        }

        @Override
        public void onRecordChange(int op, DevMessage devMessage) {
            if (op == 1) {
                mHistoryRecordChangeMLD.postValue(op);
            }
        }
    };

    private final OnHistoryRecordCallback mOnHistoryRecordCallback = new OnHistoryRecordCallback() {
        @Override
        public void onSuccess(HistoryRecord record) {
            HistoryRecordViewModel.HistoryConnectStatus status = new HistoryRecordViewModel.HistoryConnectStatus();
            status.setConnectStatus(StateCode.CONNECTION_OK);
            status.setRecord(reconnectRecord);
            mHistoryConnectStatusMLD.postValue(status);
            reconnectRecord = null;
        }

        @Override
        public void onFailed(int code, String message) {
            HistoryRecordViewModel.HistoryConnectStatus status = new HistoryRecordViewModel.HistoryConnectStatus();
            status.setConnectStatus(StateCode.CONNECTION_DISCONNECT);
            status.setRecord(reconnectRecord);
            status.setCode(code);
            status.setMessage(message);
            mHistoryConnectStatusMLD.postValue(status);
            reconnectRecord = null;
        }
    };

    private final OnDeviceConfigureListener mOnDeviceConfigureListener = mDeviceConfigureMLD::postValue;

    public static class WatchProductMsgResult {
        private boolean isOk;
        private int code;
        private String message;
        private WatchProduct product;

        public boolean isOk() {
            return isOk;
        }

        public void setOk(boolean ok) {
            isOk = ok;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public WatchProduct getProduct() {
            return product;
        }

        public void setProduct(WatchProduct product) {
            this.product = product;
        }
    }
}
