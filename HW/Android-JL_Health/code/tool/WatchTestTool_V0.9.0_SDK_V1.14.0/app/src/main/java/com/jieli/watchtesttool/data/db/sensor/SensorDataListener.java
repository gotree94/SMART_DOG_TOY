package com.jieli.watchtesttool.data.db.sensor;

import android.bluetooth.BluetoothDevice;

import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspEventListener;
import com.jieli.watchtesttool.WatchApplication;
import com.jieli.watchtesttool.data.db.SensorDbBase;
import com.jieli.watchtesttool.util.AppUtil;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/7/21
 * @desc :
 */
public class SensorDataListener extends OnRcspEventListener {

    @Override
    public void onSensorLogDataChange(BluetoothDevice device, int type, byte[] data) {
        super.onSensorLogDataChange(device, type, data);
        SensorEntity entity = new SensorEntity();
        entity.setData(data);
        entity.setDevName(AppUtil.getDeviceName(device));
        entity.setMac(device.getAddress());
        entity.setType(String.valueOf(type) );
        entity.setTime(System.currentTimeMillis());
        SensorDbBase.buildDb(WatchApplication.getWatchApplication()).sensorDao().insert(entity);
    }
}
