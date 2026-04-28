package com.jieli.healthaide.ui.device.adapter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.util.BluetoothUtil;
import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.watch.WatchServerCacheHelper;
import com.jieli.healthaide.ui.device.bean.ScanDevice;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_health_http.model.WatchProduct;
import com.jieli.jl_rcsp.util.JL_Log;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 发现设备适配器
 * @since 2021/3/10
 */
public class ScanDeviceAdapter extends BaseQuickAdapter<ScanDevice, BaseViewHolder> {
    private static final String TAG = ScanDeviceAdapter.class.getSimpleName();

    public ScanDeviceAdapter() {
        super(R.layout.item_scan_device);
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    @Override
    protected void convert(@NotNull BaseViewHolder viewHolder, ScanDevice scanDevice) {
        if (null == scanDevice) return;
        ImageView ivProduct = viewHolder.getView(R.id.iv_item_scan_device_product_image);
        updateDeviceProduct(ivProduct, scanDevice);
        viewHolder.setText(R.id.tv_item_scan_device_name, HealthUtil.getDeviceName(scanDevice.getDevice()));

        TextView textView = viewHolder.getView(R.id.tv_item_scan_device_connect_status);
        int text = R.string.connect;
        int textColor = R.color.btn_purple_to_gray_selector;
        int bgRes = R.drawable.bg_btn_purple_selector;
        switch (scanDevice.getConnectStatus()) {
            case BluetoothConstant.CONNECT_STATE_CONNECTING:
                text = R.string.status_connecting;
                textColor = R.color.text_secondary_disable_color;
                bgRes = 0;
                break;
            case BluetoothConstant.CONNECT_STATE_CONNECTED:
                text = R.string.status_connected;
                textColor = R.color.auxiliary_state;
                bgRes = 0;
                break;
        }
        textView.setText(text);
        textView.setTextColor(getContext().getResources().getColorStateList(textColor));
        textView.setBackgroundResource(bgRes);
        addChildClickViewIds(R.id.tv_item_scan_device_connect_status);
        bindViewClickListener(viewHolder, R.id.tv_item_scan_device_connect_status);
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

    private void updateImageView(Context context, ImageView imageView, String url) {
        if (imageView == null) return;
        if (null == url) {
            imageView.setImageResource(R.drawable.ic_watch_product);
            return;
        }
        JL_Log.d(TAG, "updateImageView", "imageView = " + imageView + ", url = " + url);
        boolean isGif = url.endsWith(".gif");
        if (isGif) {
            Glide.with(context).asGif().load(url)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .override(ValueUtil.dp2px(getContext(), 54), ValueUtil.dp2px(getContext(), 54))
                    .error(R.drawable.ic_watch_product)
                    .into(imageView);
        } else {
            Glide.with(context).asBitmap().load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(ValueUtil.dp2px(getContext(), 54), ValueUtil.dp2px(getContext(), 54))
                    .error(R.drawable.ic_watch_product)
                    .into(imageView);
        }
    }

    private void updateDeviceProduct(final ImageView imageView, final ScanDevice device) {
        updateImageView(HealthApplication.getAppViewModel().getApplication(), imageView, null);
        if (null != device.getBleScanMessage() && device.getBleScanMessage().getUid() > 0 && device.getBleScanMessage().getPid() > 0) {
            WatchServerCacheHelper.getInstance().getWatchProductMsg(device.getBleScanMessage().getUid(), device.getBleScanMessage().getPid(),
                    new WatchServerCacheHelper.IWatchHttpCallback<WatchProduct>() {
                        @Override
                        public void onSuccess(WatchProduct result) {
                            String productUrl = result.getIcon();
                            updateImageView(HealthApplication.getAppViewModel().getApplication(), imageView, productUrl);
                        }

                        @Override
                        public void onFailed(int code, String message) {

                        }
                    });
        }
    }

}
