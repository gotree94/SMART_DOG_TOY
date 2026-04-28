package com.jieli.healthaide.ui.device.bean;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import com.jieli.bluetooth_connect.bean.history.HistoryRecord;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 设备连接记录
 * @since 2021/3/10
 */
public final class DeviceHistoryRecord implements Parcelable {
    private HistoryRecord historyRecord;
    private BluetoothDevice connectedDev;
    private String productUrl;
    private int status;
    private int battery;
    private int source;
    private String serverId;

    public final static int SOURCE_LOCAL = 0;
    public final static int SOURCE_SERVER = 1;

    public DeviceHistoryRecord(@NotNull HistoryRecord record) {
        setHistoryRecord(record);
    }

    protected DeviceHistoryRecord(Parcel in) {
        historyRecord = in.readParcelable(HistoryRecord.class.getClassLoader());
        connectedDev = in.readParcelable(BluetoothDevice.class.getClassLoader());
        productUrl = in.readString();
        status = in.readInt();
        battery = in.readInt();
        source = in.readInt();
        serverId = in.readString();
    }

    public static final Creator<DeviceHistoryRecord> CREATOR = new Creator<DeviceHistoryRecord>() {
        @Override
        public DeviceHistoryRecord createFromParcel(Parcel in) {
            return new DeviceHistoryRecord(in);
        }

        @Override
        public DeviceHistoryRecord[] newArray(int size) {
            return new DeviceHistoryRecord[size];
        }
    };

    @NotNull
    public HistoryRecord getHistoryRecord() {
        return historyRecord;
    }

    public void setHistoryRecord(@NotNull HistoryRecord historyRecord) {
        this.historyRecord = historyRecord;
    }

    public BluetoothDevice getConnectedDev() {
        return connectedDev;
    }

    public void setConnectedDev(BluetoothDevice connectedDev) {
        this.connectedDev = connectedDev;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    @Override
    public String toString() {
        return "DeviceHistoryRecord{" +
                "historyRecord=" + historyRecord +
                ", connectedDev=" + connectedDev +
                ", productUrl='" + productUrl + '\'' +
                ", status=" + status +
                ", battery=" + battery +
                ", source=" + source +
                ", serverId='" + serverId + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(historyRecord, flags);
        dest.writeParcelable(connectedDev, flags);
        dest.writeString(productUrl);
        dest.writeInt(status);
        dest.writeInt(battery);
        dest.writeInt(source);
        dest.writeString(serverId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceHistoryRecord record = (DeviceHistoryRecord) o;
        return source == record.source &&
                Objects.equals(historyRecord, record.historyRecord);
    }

    @Override
    public int hashCode() {
        return Objects.hash(historyRecord, source);
    }
}
