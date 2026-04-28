package com.jieli.healthaide.tool.history;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.jieli.bluetooth_connect.bean.history.HistoryRecord;
import com.jieli.bluetooth_connect.interfaces.callback.OnHistoryRecordCallback;

import java.util.Objects;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 回连任务
 * @since 2021/7/26
 */
public class ReconnectTask {
    private final HistoryRecord record;
    private OnHistoryRecordCallback callback;
    private long startTime;
    private BluetoothDevice connectDev;

    public ReconnectTask(@NonNull HistoryRecord record) {
        this.record = record;
    }

    public HistoryRecord getRecord() {
        return record;
    }

    public OnHistoryRecordCallback getCallback() {
        return callback;
    }

    public void setCallback(OnHistoryRecordCallback callback) {
        this.callback = callback;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public BluetoothDevice getConnectDev() {
        return connectDev;
    }

    public void setConnectDev(BluetoothDevice connectDev) {
        this.connectDev = connectDev;
    }

    @Override
    public String toString() {
        return "ReconnectTask{" +
                "record=" + record +
                ", callback=" + callback +
                ", startTime=" + startTime +
                ", connectDev=" + connectDev +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReconnectTask that = (ReconnectTask) o;
        return Objects.equals(record, that.record) &&
                Objects.equals(callback, that.callback);
    }

    @Override
    public int hashCode() {
        return Objects.hash(record, callback);
    }
}
