package com.jieli.healthaide.ui.device.history;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.bluetooth_connect.bean.history.HistoryRecord;
import com.jieli.bluetooth_connect.interfaces.callback.OnHistoryRecordCallback;
import com.jieli.healthaide.tool.history.HistoryRecordManager;
import com.jieli.healthaide.ui.device.bean.DeviceHistoryRecord;
import com.jieli.healthaide.ui.device.watch.WatchViewModel;
import com.jieli.jl_health_http.model.device.DevMessage;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.util.JL_Log;

import org.jetbrains.annotations.NotNull;

public class HistoryRecordViewModel extends WatchViewModel {
    public DeviceHistoryRecord historyRecord;
    public final MutableLiveData<HistoryRemoveResult> mRemoveResultMLD = new MutableLiveData<>();
    public final MutableLiveData<Boolean> mRecordGoneMLD = new MutableLiveData<>();

    public HistoryRecordViewModel(Fragment fragment) {
        mHistoryRecordChangeMLD.observe(fragment.getViewLifecycleOwner(), integer -> {
            if (null == historyRecord) return;
            if (historyRecord.getSource() == DeviceHistoryRecord.SOURCE_SERVER) {
                DevMessage devMessage = HistoryRecordManager.getInstance().findDevMessageById(historyRecord.getServerId());
                if (devMessage == null) {
                    mRecordGoneMLD.postValue(true);
                }
            } else {
                HistoryRecord record = historyRecord.getHistoryRecord();
                HistoryRecord cache = mBluetoothHelper.getBluetoothOp().getHistoryRecord(record.getAddress());
                if (cache == null) {
                    mRecordGoneMLD.postValue(true);
                }
            }
        });
    }

    public void removeHistory() {
        if (null == historyRecord) return;
        if (historyRecord.getSource() == DeviceHistoryRecord.SOURCE_SERVER) {
            HistoryRecordManager.getInstance().removeDeviceMsg(historyRecord.getServerId(), new OnOperationCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    HistoryRemoveResult removeResult = new HistoryRemoveResult();
                    removeResult.setResult(true);
                    removeResult.setRecord(historyRecord);
                    mRemoveResultMLD.postValue(removeResult);
                }

                @Override
                public void onFailed(BaseError error) {
                    JL_Log.w(tag, "removeHistory", "remove service failed. code : " + error.getSubCode() + ", message = " + error.getMessage());
                    HistoryRemoveResult result = new HistoryRemoveResult();
                    result.setResult(false);
                    result.setRecord(historyRecord);
                    result.setCode(error.getSubCode());
                    result.setMessage(error.getMessage());
                    mRemoveResultMLD.postValue(result);
                }
            });
            return;
        }
        mBluetoothHelper.removeHistoryRecord(historyRecord.getHistoryRecord().getAddress(), new OnHistoryRecordCallback() {
            @Override
            public void onSuccess(HistoryRecord record) {
                HistoryRemoveResult result = new HistoryRemoveResult();
                result.setResult(true);
                result.setRecord(historyRecord);
                mRemoveResultMLD.postValue(result);
            }

            @Override
            public void onFailed(int code, String message) {
                JL_Log.w(tag, "removeHistoryRecord", "remove local failed. code : " + code + ", message = " + message);
                HistoryRemoveResult result = new HistoryRemoveResult();
                result.setResult(false);
                result.setRecord(historyRecord);
                result.setCode(code);
                result.setMessage(message);
                mRemoveResultMLD.postValue(result);
            }
        });
    }

    public void getHistoryProductMsg() {
        if (historyRecord == null) return;
        JL_Log.d(tag, "getHistoryProductMsg", "" + historyRecord);
        int uid = historyRecord.getHistoryRecord().getUid();
        int pid = historyRecord.getHistoryRecord().getPid();
        getWatchProductMsg(uid, pid);
    }

    public static class HistoryRemoveResult {
        private boolean result;
        private DeviceHistoryRecord record;
        private int code;
        private String message;

        public boolean isResult() {
            return result;
        }

        public void setResult(boolean result) {
            this.result = result;
        }

        public DeviceHistoryRecord getRecord() {
            return record;
        }

        public void setRecord(DeviceHistoryRecord record) {
            this.record = record;
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

        @NotNull
        @Override
        public String toString() {
            return "HistoryRemoveResult{" +
                    "result=" + result +
                    ", record=" + record +
                    ", code=" + code +
                    ", message='" + message + '\'' +
                    '}';
        }
    }

    public static class HistoryConnectStatus {
        private int connectStatus;
        private DeviceHistoryRecord record;
        private int code;
        private String message;

        public int getConnectStatus() {
            return connectStatus;
        }

        public void setConnectStatus(int connectStatus) {
            this.connectStatus = connectStatus;
        }

        public DeviceHistoryRecord getRecord() {
            return record;
        }

        public void setRecord(DeviceHistoryRecord record) {
            this.record = record;
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

        @NotNull
        @Override
        public String toString() {
            return "HistoryConnectStatus{" +
                    "connectStatus=" + connectStatus +
                    ", record=" + record +
                    ", code=" + code +
                    ", message='" + message + '\'' +
                    '}';
        }
    }

    public static final class HistoryRecordViewModelFactory implements ViewModelProvider.Factory {
        private final Fragment mFragment;

        public HistoryRecordViewModelFactory(Fragment fragment) {
            mFragment = fragment;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new HistoryRecordViewModel(mFragment);
        }
    }
}