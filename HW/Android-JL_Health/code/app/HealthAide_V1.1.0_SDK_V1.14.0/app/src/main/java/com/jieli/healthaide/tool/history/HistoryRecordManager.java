package com.jieli.healthaide.tool.history;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jieli.bluetooth_connect.bean.ble.BleScanMessage;
import com.jieli.bluetooth_connect.bean.history.HistoryRecord;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.interfaces.callback.OnHistoryRecordCallback;
import com.jieli.bluetooth_connect.util.BluetoothUtil;
import com.jieli.healthaide.tool.bluetooth.BluetoothEventListener;
import com.jieli.healthaide.tool.bluetooth.BluetoothHelper;
import com.jieli.healthaide.tool.config.ConfigHelper;
import com.jieli.healthaide.tool.http.base.BaseHttpResultHandler;
import com.jieli.healthaide.tool.net.NetWorkStateModel;
import com.jieli.healthaide.tool.net.NetworkStateHelper;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.tool.watch.WatchServerCacheHelper;
import com.jieli.healthaide.ui.device.bean.DeviceHistoryRecord;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_health_http.HttpClient;
import com.jieli.jl_health_http.HttpConstant;
import com.jieli.jl_health_http.model.device.DevConfig;
import com.jieli.jl_health_http.model.device.DevMessage;
import com.jieli.jl_health_http.model.device.IdConfig;
import com.jieli.jl_health_http.tool.OnResultCallback;
import com.jieli.jl_rcsp.constant.WatchError;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 历史设备管理
 * @since 2021/7/15
 */
public class HistoryRecordManager implements NetworkStateHelper.Listener {
    private final static String TAG = HistoryRecordManager.class.getSimpleName();
    private volatile static HistoryRecordManager manager;
    private final BluetoothHelper mBluetoothHelper = BluetoothHelper.getInstance();
    private final WatchManager mWatchManager = WatchManager.getInstance();
    private final ConfigHelper mConfigHelper = ConfigHelper.getInstance();

    private final List<OnServiceRecordListener> mRecordListeners = new ArrayList<>();
    private List<DevMessage> mServiceDevMsgList; //缓存服务器历史列表
    private final List<HistoryTask> mTaskList = Collections.synchronizedList(new ArrayList<>());
    private final Gson gson = new GsonBuilder().create();
    private ReconnectTask reconnectTask;
    private final List<String> boundedDeviceList = new ArrayList<>();
    private boolean isSyncService;  //是否正在同步服务器数据

    private final static int RECONNECT_TIMEOUT = 36 * 1000; //重连超时
    private final static int SCAN_TIMEOUT_LIMIT = 1000;     //最小扫描设备时间
    private final static int MSG_RECONNECT_TASK_TIMEOUT = 0x001;
    private final Handler mUIHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_RECONNECT_TASK_TIMEOUT) {
                if (isReconnecting()) {
                    cbReconnectHistoryFailed(reconnectTask.getCallback(), WatchError.ERR_RESPONSE_TIMEOUT,
                            "Reconnect device timeout");
                }
            }
            return true;
        }
    });

    private HistoryRecordManager() {
        NetworkStateHelper.getInstance().registerListener(this);
        mBluetoothHelper.addBluetoothEventListener(mEventListener);
        mWatchManager.registerOnWatchCallback(mOnWatchCallback);
    }

    public static HistoryRecordManager getInstance() {
        if (null == manager) {
            synchronized (HistoryRecordManager.class) {
                if (null == manager) {
                    manager = new HistoryRecordManager();
                }
            }
        }
        return manager;
    }

    public void addOnServiceRecordListener(OnServiceRecordListener listener) {
        if (listener == null || mRecordListeners.contains(listener)) return;
        mRecordListeners.add(listener);
    }

    public void removeOnServiceRecordListener(OnServiceRecordListener listener) {
        if (listener == null || mRecordListeners.isEmpty()) return;
        mRecordListeners.remove(listener);
    }

    public void syncHistoryRecordList() {
        if (!isSyncService) {
            isSyncService = true;
            HttpClient.createDeviceApi().queryBoundDeviceList()
                    .enqueue(new BaseHttpResultHandler<>(new OnResultCallback<List<DevMessage>>() {
                        @Override
                        public void onResult(List<DevMessage> result) {
                            if (result == null) {
                                result = new ArrayList<>();
                            }
                            mServiceDevMsgList = filterBoundDeviceList(filterRemoveHistoryIds(result));
                            checkServiceDevList(mServiceDevMsgList);
                            isSyncService = false;
                        }

                        @Override
                        public void onError(int code, String message) {
                            JL_Log.e(TAG, "syncHistoryRecordList", "onError ---> " + code + ", " + message);
                            isSyncService = false;
                        }
                    }));
        } else {
            JL_Log.i(TAG, "syncHistoryRecordList", "queryBoundDeviceList is running.");
//            checkServiceDevList(mServiceDevMsgList);
        }
    }

    public void resetCacheServiceList() {
        if (mServiceDevMsgList != null) {
            mServiceDevMsgList.clear();
        }
    }

    public void release() {
        resetCacheServiceList();
        NetworkStateHelper.getInstance().unregisterListener(this);
        mBluetoothHelper.removeBluetoothEventListener(mEventListener);
        mWatchManager.unregisterOnWatchCallback(mOnWatchCallback);
        mUIHandler.removeCallbacksAndMessages(null);
        boundedDeviceList.clear();
        manager = null;
    }

    @Override
    public void onNetworkStateChange(NetWorkStateModel model) {
        if (model.isAvailable()) {
            syncHistoryRecordList();
        }
    }

    public void reconnectHistory(@NonNull HistoryRecord record, OnHistoryRecordCallback callback) {
        if (isReconnecting()) {
            cbReconnectHistoryFailed(callback, WatchError.ERR_IN_PROGRESS);
            return;
        }
        reconnectTask = new ReconnectTask(record);
        reconnectTask.setCallback(callback);
        reconnectTask.setStartTime(getCurrentTime());
        boolean ret = mBluetoothHelper.getBluetoothOp().startBLEScan(BluetoothConstant.DEFAULT_SCAN_TIMEOUT * 2);
        if (ret) {
            JL_Log.d(TAG, "reconnectHistory", "start. " + reconnectTask);
            mUIHandler.removeMessages(MSG_RECONNECT_TASK_TIMEOUT);
            mUIHandler.sendEmptyMessageDelayed(MSG_RECONNECT_TASK_TIMEOUT, RECONNECT_TIMEOUT);
        } else {
            cbReconnectHistoryFailed(callback, WatchError.ERR_USE_SYSTEM_API, "Failed to start Reconnect task.");
        }
    }

    public void removeDeviceMsg(final String id, final OnOperationCallback<Boolean> callback) {
        final HistoryTask task = new HistoryTask(HistoryTask.OP_REMOVE, id, null);
        if (hasTask(task)) {
            JL_Log.d(TAG, "removeDeviceMsg", "has task." + task);
            return;
        }
        JL_Log.d(TAG, "removeDeviceMsg", "id = " + id);
        saveRemoveHistoryID(id);
        HttpClient.createDeviceApi().unbindDevice(new IdConfig(id))
                .enqueue(new BaseHttpResultHandler<>(new OnResultCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        removeTask(task);
                        deleteRemoveHistoryId(task.getId());
                        if (callback != null) {
                            callback.onSuccess(true);
                        }
                        JL_Log.i(TAG, "removeDeviceMsg", "onResult ---> " + task.getId());
                    }

                    @Override
                    public void onError(int code, String message) {
                        JL_Log.e(TAG, "removeDeviceMsg", "onError ---> code = " + code + ", " + message);
                        if (message != null && message.contains(String.valueOf(HttpConstant.ERROR_DEVICE_NOT_EXIST))
                                && findDevMessageById(id) == null) {
                            JL_Log.i(TAG, "removeDeviceMsg", "cache no device, success = " + id);
                            deleteRemoveHistoryId(id);
                            if (callback != null) {
                                callback.onSuccess(true);
                            }
                        } else {
                            if (callback != null) {
                                callback.onFailed(new BaseError(code, message));
                            }
                        }
                        removeTask(task);
                    }
                }));
    }

    private List<DevMessage> filterRemoveHistoryIds(@NonNull List<DevMessage> list) {
        return filterIds(list, getRemoveHistoryID(), HistoryTask.OP_REMOVE);
    }

    private List<DevMessage> filterBoundDeviceList(@NonNull List<DevMessage> list) {
        return filterIds(list, boundedDeviceList, HistoryTask.OP_ADD);
    }

    private List<DevMessage> filterIds(@NonNull List<DevMessage> list, @NonNull List<String> filterList, int op) {
        if (filterList.isEmpty()) {
            return list;
        }
        List<DevMessage> deleteList = new ArrayList<>();
        for (DevMessage devMessage : list) {
            if (op == HistoryTask.OP_REMOVE && isMatchHistoryID(filterList, devMessage.getId()) ||
                    (op == HistoryTask.OP_ADD && isMatchHistoryID(filterList, devMessage.getMac()))) {
                deleteList.add(devMessage);
            }
        }
        if (!deleteList.isEmpty()) {
            JL_Log.d(TAG, "filterIds", "deleteList = " + deleteList.size());
            if (op == HistoryTask.OP_REMOVE) {
                for (DevMessage message : deleteList) {
                    JL_Log.i(TAG, "filterIds", "id = " + message.getId());
                    removeDeviceMsg(message.getId(), null);
                }
            }
            list.removeAll(deleteList);
        }
        return list;
    }

    private void checkServiceDevList(@NonNull List<DevMessage> list) {
        JL_Log.i(TAG, "checkServiceDevList", "start");
        for (DevMessage devMessage : list) {
            JL_Log.d(TAG, "checkServiceDevList", "" + devMessage);
        }
        JL_Log.i(TAG, "checkServiceDevList", "end");
        List<HistoryRecord> historyList = mBluetoothHelper.getBluetoothOp().getHistoryRecordList();
        if (historyList == null || historyList.isEmpty()) { //本地记录为空时, 以服务列表为主
            postServiceRecordList(list);
            return;
        }
        //本地记录不为空时，以本地记录为主，同步服务器备份
        //同步信息
        List<DevMessage> serviceList = new ArrayList<>();
        for (DevMessage message : list) {
            boolean isExist = false;
            for (HistoryRecord record : historyList) {
                if (isMatch(record, message)) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                serviceList.add(message);
            }
        }
        if (!serviceList.isEmpty()) {
            postServiceRecordList(serviceList);
        }
        //添加 -- （仅添加，不删除）
        for (HistoryRecord record : historyList) {
            boolean isExist = isContainsDevMsg(list, record) != null;
            if (!isExist) {
                addDeviceMsg(record);
            }
        }
    }

    private void addDeviceMsg(HistoryRecord record) {
        final DevConfig config = convertHistory(record);
        if (boundedDeviceList.contains(config.getMac())) {
            JL_Log.d(TAG, "addDeviceMsg", "device is bound. mac = " + config.getMac());
            return;
        }
        final HistoryTask task = new HistoryTask(HistoryTask.OP_ADD, null, record);
        if (hasTask(task)) {
            JL_Log.d(TAG, "addDeviceMsg", "has task." + task);
            return;
        }
        JL_Log.d(TAG, "addDeviceMsg", "" + task);
        HttpClient.createDeviceApi().bindDevice(config)
                .enqueue(new BaseHttpResultHandler<>(new OnResultCallback<DevMessage>() {
                    @Override
                    public void onResult(DevMessage result) {
                        if (result == null) {
                            onError(WatchServerCacheHelper.ERR_BAD_RESPONSE, "No Device Message.");
                            return;
                        }
               /* if (!mServiceDevMsgList.contains(result)) {
                    mServiceDevMsgList.add(result);
                }*/
                        JL_Log.d(TAG, "addDeviceMsg", "bond device success." + result);
                        removeTask(task);
                        postRecordChange(0, result);
                    }

                    @Override
                    public void onError(int code, String message) {
                        JL_Log.e(TAG, "addDeviceMsg", "onError ---> code : " + code + ", " + message);
                        if (code == HttpConstant.ERROR_DEVICE_BOUND) {
                            if (config.getMac() != null && !boundedDeviceList.contains(config.getMac())) {
                                boundedDeviceList.add(config.getMac());
                            }
                        }
                        removeTask(task);
                    }
                }));
    }

    private void updateConnectTime(BluetoothDevice device) {
        if (mServiceDevMsgList == null || !mBluetoothHelper.isConnectedBtDevice(device)) return;
        DeviceInfo deviceInfo = mWatchManager.getDeviceInfo(device);
        if (deviceInfo == null) return;
        DevMessage devMessage = null;
        for (DevMessage message : mServiceDevMsgList) {
            if (message.getMac() != null && message.getMac().equals(deviceInfo.getEdrAddr())/*
                    && message.getVid() == deviceInfo.getVid()
                    && message.getPid() == deviceInfo.getPid()*/) {
                devMessage = message;
                break;
            }
        }
        if (devMessage == null) {
            JL_Log.w(TAG, "updateConnectTime", "not found DevMessage.");
            return;
        }
        final DevMessage devMsg = devMessage;
        HttpClient.createDeviceApi().updateDeviceTime(new IdConfig(devMessage.getId()))
                .enqueue(new BaseHttpResultHandler<>(new OnResultCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        JL_Log.i(TAG, "updateConnectTime", "onResult ---> true");
                    }

                    @Override
                    public void onError(int code, String message) {
                        JL_Log.e(TAG, "updateConnectTime", "onError ---> code : " + code + ", " + message);
                        if (code == HttpConstant.ERROR_DEVICE_NOT_EXIST) {
                            mServiceDevMsgList.remove(devMsg);
                        }
                    }
                }));
    }

    private DevMessage isContainsDevMsg(List<DevMessage> messageList, HistoryRecord record) {
        if (messageList == null || record == null) return null;
        for (DevMessage message : messageList) {
            if (isMatch(record, message)) {
                return message;
            }
        }
        return null;

    }

    private boolean isMatch(HistoryRecord record, DevMessage message) {
        return message.getMac() != null && message.getMac().equals(record.getClassicAddress());
        /*return message.getMac() != null && message.getMac().equals(getCacheEdrAddr(record)) && message.getPid() == record.getPid()
                && (message.getVid() == record.getVid() || message.getVid() == record.getUid());*/
    }

    private DevConfig convertHistory(HistoryRecord record) {
        DevConfig config = new DevConfig();
        config.setPid(record.getPid());
        config.setVid(record.getVid());
        config.setMac(record.getClassicAddress());
        config.setType(String.valueOf(record.getDevType()));

        ConfigData configData = new ConfigData();
        configData.setName(record.getName());

        AndroidConfigData androidConfigData = new AndroidConfigData();
        androidConfigData.setBle(record.getBleAddress());
        androidConfigData.setConnectWay(record.getConnectType());
        androidConfigData.setSdkType(record.getSdkFlag());

        config.setConfigData(configData.toString());
        config.setAndroidConfigData(androidConfigData.toString());
        return config;
    }

    private ConfigData convertConfigData(String json) {
        ConfigData configData = null;
        if (!TextUtils.isEmpty(json)) {
            try {
                configData = gson.fromJson(json, ConfigData.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return configData;
    }

    private AndroidConfigData convertAndroidConfigData(String json) {
        AndroidConfigData configData = null;
        if (!TextUtils.isEmpty(json)) {
            try {
                configData = gson.fromJson(json, AndroidConfigData.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return configData;
    }

    private List<DeviceHistoryRecord> convertDeviceHistoryRecordByService(List<DevMessage> serviceList) {
        if (serviceList == null || serviceList.isEmpty()) {
            return new ArrayList<>();
        }
        List<DeviceHistoryRecord> list = new ArrayList<>();
        for (DevMessage devMessage : serviceList) {
            if (!BluetoothAdapter.checkBluetoothAddress(devMessage.getMac())) continue;
            ConfigData configData = convertConfigData(devMessage.getConfigData());
            AndroidConfigData androidConfigData = convertAndroidConfigData(devMessage.getAndroidConfigData());
            HistoryRecord record = new HistoryRecord();
            record.setVid(devMessage.getVid());
            record.setPid(devMessage.getPid());
            record.setAddress(devMessage.getMac());
            if (configData != null && configData.getName() != null) {
                record.setName(configData.getName());
            }
            if (androidConfigData != null && BluetoothAdapter.checkBluetoothAddress(androidConfigData.getBle())) {
                int connectWay = androidConfigData.getConnectWay();
                int connectType = connectWay == BluetoothConstant.PROTOCOL_TYPE_GATT_OVER_BR_EDR ?
                        BluetoothConstant.PROTOCOL_TYPE_GATT_OVER_BR_EDR : connectWay == BluetoothConstant.PROTOCOL_TYPE_SPP ?
                        BluetoothConstant.PROTOCOL_TYPE_SPP : BluetoothConstant.PROTOCOL_TYPE_BLE;
                record.setConnectType(connectType);
                record.setSdkFlag(androidConfigData.getSdkType());
                if (record.getConnectType() == BluetoothConstant.PROTOCOL_TYPE_BLE) {
                    record.setAddress(androidConfigData.getBle());
                    record.setMappedAddress(devMessage.getMac());
                } else {
                    record.setMappedAddress(androidConfigData.getBle());
                }
            } else {
                record.setConnectType(BluetoothConstant.PROTOCOL_TYPE_BLE);
            }
            DeviceHistoryRecord deviceHistoryRecord = new DeviceHistoryRecord(record);
            deviceHistoryRecord.setSource(DeviceHistoryRecord.SOURCE_SERVER);
            deviceHistoryRecord.setServerId(devMessage.getId());
            list.add(deviceHistoryRecord);
        }
        return list;
    }

    public DevMessage findDevMessageById(String id) {
        if (TextUtils.isEmpty(id)) return null;
        if (mServiceDevMsgList == null || mServiceDevMsgList.isEmpty()) return null;
        for (DevMessage message : mServiceDevMsgList) {
            if (id.equals(message.getId())) {
                return message;
            }
        }
        return null;
    }

    private void postServiceRecordList(List<DevMessage> list) {
        final List<DeviceHistoryRecord> deviceHistoryRecordList = convertDeviceHistoryRecordByService(list);
        mUIHandler.post(() -> {
            if (!mRecordListeners.isEmpty()) {
                for (OnServiceRecordListener listener : new ArrayList<>(mRecordListeners)) {
                    listener.onServiceRecord(deviceHistoryRecordList);
                }
            }
        });
    }

    private void postRecordChange(final int op, final DevMessage devMessage) {
        mUIHandler.post(() -> {
            if (mRecordListeners.isEmpty()) return;
            for (OnServiceRecordListener listener : new ArrayList<>(mRecordListeners)) {
                listener.onRecordChange(op, devMessage);
            }
        });
    }

    private int isHasTask(HistoryTask task) {
        if (null == task || mTaskList.isEmpty()) return -1;
        for (int index = 0; index < mTaskList.size(); index++) {
            HistoryTask cache = mTaskList.get(index);
            if (cache.getOp() == task.getOp()) {
                if (cache.getOp() == HistoryTask.OP_REMOVE) {
                    if (cache.getId() != null && task.getId() != null && cache.getId().equals(task.getId())) {
                        return index;
                    }
                } else {
                    if (cache.getRecord() != null && task.getRecord() != null
                            && cache.getRecord().equals(task.getRecord())) {
                        return index;
                    }
                }
            }
        }
        return -1;
    }

    private boolean hasTask(HistoryTask task) {
        if (isHasTask(task) == -1) {
            return !mTaskList.add(task);
        }
        return true;
    }

    private void removeTask(HistoryTask task) {
        int index = isHasTask(task);
        if (index != -1 && mTaskList.remove(index) != null) {
            if (task.getOp() == HistoryTask.OP_REMOVE) {
                DevMessage devMessage = findDevMessageById(task.getId());
                JL_Log.w(TAG, "removeTask", "devMessage = " + devMessage + ", task = " + task);
                if (devMessage != null) {
                    mServiceDevMsgList.remove(devMessage);
                    postRecordChange(task.getOp(), devMessage);
                }
            }
        }
        if (mTaskList.isEmpty()) {
            resetCacheServiceList();
            syncHistoryRecordList();
        }
    }

    private boolean isReconnecting() {
        return reconnectTask != null;
    }

    private boolean checkReconnectDevice(BluetoothDevice device, BleScanMessage bleScanMessage) {
        if (reconnectTask == null || device == null) return false;
        boolean ret = TextUtils.equals(device.getAddress(), reconnectTask.getRecord().getAddress())
                || TextUtils.equals(device.getAddress(), reconnectTask.getRecord().getMappedAddress())
                || TextUtils.equals(device.getAddress(), reconnectTask.getRecord().getUpdateAddress());
        if (!ret && bleScanMessage != null) {
            ret = TextUtils.equals(bleScanMessage.getEdrAddr(), reconnectTask.getRecord().getAddress())
                    || TextUtils.equals(bleScanMessage.getEdrAddr(), reconnectTask.getRecord().getMappedAddress());

            if (!ret && bleScanMessage.isOTA()) {
                ret = TextUtils.equals(bleScanMessage.getOtaBleAddress(), reconnectTask.getRecord().getAddress())
                        || TextUtils.equals(bleScanMessage.getOtaBleAddress(), reconnectTask.getRecord().getMappedAddress());
            }
        }
        return ret;
    }

    private void cbReconnectHistoryFailed(OnHistoryRecordCallback callback, int error) {
        cbReconnectHistoryFailed(callback, error, WatchError.getErrorDesc(error));
    }

    private void cbReconnectHistoryFailed(OnHistoryRecordCallback callback, int error, String message) {
        JL_Log.w(TAG, "cbReconnectHistoryFailed", "code : " + error + ", " + message + ", " + reconnectTask);
        if (null != callback) {
            callback.onFailed(error, message);
        }
        reconnectTask = null;
    }

    private void cbReconnectHistorySuccess(OnHistoryRecordCallback callback) {
        if (isReconnecting()) {
            JL_Log.i(TAG, "cbReconnectHistorySuccess", "" + reconnectTask);
            if (null != callback) {
                callback.onSuccess(reconnectTask.getRecord());
            }
            reconnectTask = null;
        }
    }

    private long getCurrentTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    private boolean isMatchHistoryID(List<String> list, String string) {
        for (String cache : list) {
            if (cache != null && cache.equals(string)) {
                return true;
            }
        }
        return false;
    }

    private List<String> getRemoveHistoryID() {
        String string = mConfigHelper.getRemoveHistoryId();
        if (TextUtils.isEmpty(string)) {
            return new ArrayList<>();
        }
        String[] ids = string.split(",");
        return new ArrayList<>(Arrays.asList(ids));
    }

    private void saveRemoveHistoryID(String id) {
        if (TextUtils.isEmpty(id)) return;
        List<String> ids = getRemoveHistoryID();
        if (!ids.contains(id)) {
            ids.add(id);
            StringBuilder content = new StringBuilder();
            for (String item : ids) content.append(item).append(",");
            mConfigHelper.setRemoveHistoryId(content.toString());
        }
    }

    private void deleteRemoveHistoryId(String id) {
        if (TextUtils.isEmpty(id)) return;
        List<String> ids = getRemoveHistoryID();
        if (ids.remove(id)) {
            if (ids.isEmpty()) {
                mConfigHelper.setRemoveHistoryId(null);
            } else {
                StringBuilder content = new StringBuilder();
                for (String item : ids) content.append(item).append(",");
                mConfigHelper.setRemoveHistoryId(content.toString());
            }
        }
    }

    private final BluetoothEventListener mEventListener = new BluetoothEventListener() {

        @Override
        public void onHistoryRecord(int op, HistoryRecord record) {
            JL_Log.d(TAG, "onHistoryRecord", "op = " + op + ", record = " + record);
            if (mServiceDevMsgList == null) return;
            DevMessage message = isContainsDevMsg(mServiceDevMsgList, record);
            JL_Log.d(TAG, "onHistoryRecord", "op = " + op + ", message = " + message);
            if (op == BluetoothConstant.HISTORY_OP_DELETE) {
                if (message != null) removeDeviceMsg(message.getId(), null);
            }
        }

        @Override
        public void onBtDiscoveryStatus(boolean bBle, boolean bStart) {
            if (!isReconnecting()) return;
            if (!bStart && reconnectTask.getConnectDev() == null) {
                long left = RECONNECT_TIMEOUT - (getCurrentTime() - reconnectTask.getStartTime());
                if (left >= SCAN_TIMEOUT_LIMIT) {
                    mBluetoothHelper.getBluetoothOp().startBLEScan(left);
                }
            }
        }

        @Override
        public void onBtDiscovery(BluetoothDevice device, BleScanMessage bleScanMessage) {
            if (!isReconnecting()) return;
            boolean isReconnectDevice = checkReconnectDevice(device, bleScanMessage);
            JL_Log.d(TAG, "onBtDiscovery", "device = " + device + ", bleScanMessage = " + bleScanMessage + ", isReconnectDevice : " + isReconnectDevice);
            if (isReconnectDevice) {
                BluetoothDevice targetDev = device;
                if (bleScanMessage != null && bleScanMessage.getConnectWay() == BluetoothConstant.PROTOCOL_TYPE_SPP) {
                    targetDev = HealthUtil.getRemoteDevice(bleScanMessage.getEdrAddr());
                    if (targetDev == null) {
                        targetDev = device;
                    }
                }
                reconnectTask.setConnectDev(targetDev);
                mBluetoothHelper.getBluetoothOp().stopBLEScan();
                if (mBluetoothHelper.connectDevice(device, bleScanMessage)) {
                    mUIHandler.removeMessages(MSG_RECONNECT_TASK_TIMEOUT);
                } else {
                    reconnectTask.setConnectDev(null);
                    onBtDiscoveryStatus(true, false);
                }
            }
        }

        @Override
        public void onConnection(BluetoothDevice device, int status) {
            if (!isReconnecting()) return;
            JL_Log.d(TAG, "onConnection", "device = " + device + ", status = " + status);
            if (BluetoothUtil.deviceEquals(device, reconnectTask.getConnectDev())) {
                if (status == BluetoothConstant.CONNECT_STATE_DISCONNECT) {
                    cbReconnectHistoryFailed(reconnectTask.getCallback(), WatchError.ERR_REMOTE_NOT_CONNECT);
                }
            }
        }
    };

    private final OnWatchCallback mOnWatchCallback = new OnWatchCallback() {
        @Override
        public void onWatchSystemInit(int code) {
            final BluetoothDevice connectedDevice = mWatchManager.getConnectedDevice();
            if (isReconnecting() && BluetoothUtil.deviceEquals(connectedDevice, reconnectTask.getConnectDev())) {
                if (code == 0) {
                    cbReconnectHistorySuccess(reconnectTask.getCallback());
                } else {
                    cbReconnectHistoryFailed(reconnectTask.getCallback(), WatchError.ERR_FAT_JNI_INIT);
                }
            }
            if (code == 0) {
                if (mServiceDevMsgList != null && null != connectedDevice) {
                    HistoryRecord record = mBluetoothHelper.getBluetoothOp().getHistoryRecord(connectedDevice.getAddress());
                    if (null != record) {
                        DevMessage message = isContainsDevMsg(mServiceDevMsgList, record);
                        JL_Log.i(TAG, "onWatchSystemInit", "record = " + record + ",\n message = " + message);
                        if (message == null && !mBluetoothHelper.isNeedUpdateDevice(record)) {
                            addDeviceMsg(record);
                        }
                    }
                }
                updateConnectTime(connectedDevice);
            }
        }
    };
}
