package com.jieli.watchtesttool.ui.device;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.bluetooth_connect.util.BluetoothUtil;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.data.bean.ScanDevice;
import com.jieli.watchtesttool.util.AppUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 发现设备适配器
 * @since 2021/3/10
 */
public class ScanDeviceAdapter extends BaseQuickAdapter<ScanDevice, BaseViewHolder> {

    public ScanDeviceAdapter() {
        super(R.layout.item_scan_device);
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    @Override
    protected void convert(@NotNull BaseViewHolder viewHolder, ScanDevice scanDevice) {
        if (null == scanDevice || null == scanDevice.getDevice()) return;
        TextView tvScanDeviceMsg = viewHolder.getView(R.id.tv_scan_device_msg);
        String msg = String.format(Locale.getDefault(), "%s(%s)", AppUtil.getDeviceName(scanDevice.getDevice()), scanDevice.getDevice().getAddress());
        tvScanDeviceMsg.setText(msg);
        boolean isConnected = scanDevice.getConnectStatus() == StateCode.CONNECTION_OK;
        tvScanDeviceMsg.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, isConnected ? R.drawable.ic_check_blue : 0, 0);
    }

    public ScanDevice getItemByDevice(BluetoothDevice device) {
        if (null == device) return null;
        List<ScanDevice> deviceList = getData();
        if (deviceList.isEmpty()) return null;
        ScanDevice target = null;
        for (ScanDevice scanDevice : deviceList) {
            if (BluetoothUtil.deviceEquals(device, scanDevice.getDevice())) {
                target = scanDevice;
                break;
            }
        }
        return target;
    }

    public void updateScanDeviceConnectStatus(BluetoothDevice device, int status) {
        if (null == device) return;
        ScanDevice target = getItemByDevice(device);
        if (target != null) {
            target.setConnectStatus(status);
            notifyItemChanged(getItemPosition(target));
        }
    }

}
