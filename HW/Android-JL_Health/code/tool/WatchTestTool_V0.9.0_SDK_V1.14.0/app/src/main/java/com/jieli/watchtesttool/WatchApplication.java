package com.jieli.watchtesttool;

import android.app.Application;
import android.content.Context;

import com.jieli.bluetooth_connect.bean.BluetoothOption;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.impl.BluetoothCore;
import com.jieli.component.ActivityManager;
import com.jieli.component.utils.ToastUtil;
import com.jieli.watchtesttool.tool.config.ConfigHelper;
import com.jieli.watchtesttool.util.AppUtil;
import com.jieli.watchtesttool.util.WLog;


/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 手表自动化测试入口
 * @since 2021/4/20
 */
public class WatchApplication extends Application {
    private static WatchApplication watchApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        watchApplication = this;
        ActivityManager.init(this);
        ToastUtil.init(this);
        WLog.configureLog(this, BuildConfig.DEBUG, BuildConfig.DEBUG);
        initBluetoothCore(this);
//        new LogcatTask(this).start();
    }

    public String getLogFileDir() {
//        return this.getExternalCacheDir() + File.separator + "log";
        return AppUtil.createFilePath(this, "logcat");
    }

    public static WatchApplication getWatchApplication() {
        return watchApplication;
    }

    private void initBluetoothCore(Context context){
        final ConfigHelper configHelper = ConfigHelper.getInstance();
        BluetoothOption bluetoothOption = BluetoothOption.createDefaultOption()
                .setPriority(configHelper.isSPPConnectWay() ? BluetoothConstant.PROTOCOL_TYPE_SPP : BluetoothConstant.PROTOCOL_TYPE_BLE)
                .setScanFilterData("")
                .setMtu(configHelper.getBleMtu())
                .setNeedChangeBleMtu(false)
                .setBleScanStrategy(configHelper.isFilterDevice() ? BluetoothConstant.ALL_FILTER : BluetoothConstant.NONE_FILTER)
                .setUseMultiDevice(false)
                .setUseDeviceAuth(configHelper.isUseDeviceAuth());
        if(!BluetoothCore.isInit()){
            BluetoothCore.init(context, bluetoothOption);
        }
    }
}
