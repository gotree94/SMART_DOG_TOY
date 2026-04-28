package com.jieli.healthaide.ui.device.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.watch.WatchServerCacheHelper;
import com.jieli.healthaide.ui.device.bean.DeviceHistoryRecord;
import com.jieli.jl_health_http.model.WatchProduct;
import com.jieli.jl_rcsp.util.JL_Log;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 连接历史记录适配器
 * @since 2021/3/10
 */
public class DeviceHistoryAdapter extends BaseQuickAdapter<DeviceHistoryRecord, BaseViewHolder> {

    private final static String TAG = DeviceHistoryAdapter.class.getSimpleName();

    public DeviceHistoryAdapter() {
        super(R.layout.item_history_device);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder viewHolder, DeviceHistoryRecord deviceHistoryRecord) {
        if (deviceHistoryRecord == null) return;
        viewHolder.setText(R.id.tv_item_history_device_name, deviceHistoryRecord.getHistoryRecord().getName());
        ImageView imageView = viewHolder.getView(R.id.iv_item_history_device_img);
        updateDeviceProduct(imageView, deviceHistoryRecord);
        TextView tvStatus = viewHolder.getView(R.id.tv_item_history_device_status);
        int status = deviceHistoryRecord.getStatus();
        int textId;
        int textColorId;
        switch (status) {
            case BluetoothConstant.CONNECT_STATE_CONNECTING:
                textId = R.string.status_connecting;
                textColorId = R.color.text_important_color;
                break;
            case BluetoothConstant.CONNECT_STATE_CONNECTED:
                textId = R.string.status_connected;
//                textColorId = R.color.auxiliary_state;
                textColorId = R.color.text_important_color;
                break;
            default:
                textId = R.string.status_disconnect;
                textColorId = R.color.gray_9E9E9E;
                break;
        }
        tvStatus.setText(textId);
        tvStatus.setTextColor(getContext().getResources().getColor(textColorId));
        TextView tvBattery = viewHolder.getView(R.id.tv_item_history_device_battery);
        TextView tvReconnect = viewHolder.getView(R.id.tv_item_history_device_reconnect);
        boolean isConnected = status == BluetoothConstant.CONNECT_STATE_CONNECTED;
        imageView.setAlpha(!isConnected ? 0.5f : 1.0f);
        tvBattery.setVisibility(isConnected ? View.VISIBLE : View.GONE);
        tvReconnect.setVisibility(status == BluetoothConstant.CONNECT_STATE_DISCONNECT ? View.VISIBLE : View.GONE);
        if (isConnected) {
            String value = getContext().getString(R.string.battery_format, deviceHistoryRecord.getBattery());
            tvBattery.setText(value);
        }
        addChildClickViewIds(R.id.tv_item_history_device_reconnect);
        bindViewClickListener(viewHolder, R.id.tv_item_history_device_reconnect);
    }

    public DeviceHistoryRecord getItemByDevice(BluetoothDevice device) {
        if (null == device) return null;
        List<DeviceHistoryRecord> historyRecordList = getData();
        if (historyRecordList.isEmpty()) return null;
        DeviceHistoryRecord target = null;
        String devAddress = device.getAddress();
        for (DeviceHistoryRecord historyRecord : historyRecordList) {
            JL_Log.i(TAG, "getItemByDevice", "history : " + historyRecord + ",\n" + devAddress);
            if (devAddress.equals(historyRecord.getHistoryRecord().getAddress())
                    || devAddress.equals(historyRecord.getHistoryRecord().getMappedAddress())
                    || devAddress.equals(historyRecord.getHistoryRecord().getUpdateAddress())) {
                target = historyRecord;
                break;
            }
        }
        return target;
    }

    /**
     * 判断是否有对应的历史记录
     *
     * @param device 映射的蓝牙设备
     * @return 历史记录
     */
    public boolean checkHistoryDataIsChange(BluetoothDevice device, int connectWay) {
        DeviceHistoryRecord record = getItemByDevice(device);
        if (null == record) return false;
        return record.getHistoryRecord().getConnectType() != connectWay;
    }

    private void updateImageView(Context context, ImageView imageView, String url) {
        if (imageView == null) return;
        if (null == url) {
            imageView.setImageResource(R.drawable.ic_watch_big);
            return;
        }
        JL_Log.d(TAG, "updateImageView", "imageView = " + imageView + ", url = " + url);
        boolean isGif = url.endsWith(".gif");
        if (isGif) {
            Glide.with(context).asGif().load(url)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .error(R.drawable.ic_watch_big)
                    .into(imageView);
        } else {
            Glide.with(context).asBitmap().load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.ic_watch_big)
                    .into(imageView);
        }
    }

    private void updateDeviceProduct(final ImageView imageView, final DeviceHistoryRecord record) {
        updateImageView(HealthApplication.getAppViewModel().getApplication(), imageView, null);
        WatchServerCacheHelper.getInstance().getWatchProductMsg(record.getHistoryRecord().getUid(), record.getHistoryRecord().getPid(),
                new WatchServerCacheHelper.IWatchHttpCallback<WatchProduct>() {
                    @Override
                    public void onSuccess(WatchProduct result) {
                        String productUrl = result.getIcon();
                        record.setProductUrl(productUrl);
                        updateImageView(HealthApplication.getAppViewModel().getApplication(), imageView, productUrl);
                    }

                    @Override
                    public void onFailed(int code, String message) {

                    }
                });
    }
}
