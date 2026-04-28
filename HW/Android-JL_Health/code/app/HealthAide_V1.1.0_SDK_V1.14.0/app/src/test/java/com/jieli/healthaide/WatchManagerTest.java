package com.jieli.healthaide;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.util.ConnectUtil;
import com.jieli.healthaide.tool.bluetooth.BluetoothEventListener;
import com.jieli.healthaide.tool.bluetooth.BluetoothHelper;
import com.jieli.jl_fatfs.interfaces.OnFatFileProgressListener;
import com.jieli.jl_fatfs.model.FatFile;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.impl.RcspAuth;
import com.jieli.jl_rcsp.impl.WatchOpImpl;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.settings.v0.NetworkInfo;
import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.jl_rcsp.util.RcspUtil;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 手表功能实现简易demo
 * @since 2022/1/7
 */
public class WatchManagerTest {
    WatchOpImpl demo;         //手表系统对象
    boolean isSystemInit = false;  //手表系统是否初始化成功

    @Test
    public void initTest() {
        isSystemInit = false;
        //初始化手表功能对象
        demo = WatchManagerByCustom.getInstance();
        //注册事件监听器
        demo.registerOnWatchCallback(new OnWatchCallback() {

            @Override
            public void onRcspInit(BluetoothDevice device, boolean isInit) {
                super.onRcspInit(device, isInit);
                //回调RCSP协议初始化结果
            }

            @Override
            public void onMandatoryUpgrade(BluetoothDevice device) {
                super.onMandatoryUpgrade(device);
                //回调设备处于强制升级模式

            }

            @Override
            public void onWatchSystemInit(int code) {
                super.onWatchSystemInit(code); //TODO: 系统初始化结果回调
                isSystemInit = code == 0;
                if (isSystemInit) {//系统初始化成功
                    //TODO: 可以操作其他操作，参考testWatchOp
//                    demo.isWatchSystemOk(); //判断手表管理对象是否初始化
                } else { //系统初始化失败

                }
            }

            @Override
            public void onWatchSystemException(BluetoothDevice device, int sysStatus) {
                super.onWatchSystemException(device, sysStatus); //TODO: 系统异常回调, 0为正常，其他为错误码
                if (sysStatus != 0) {
                    //恢复系统
                    //耗时流程，正在恢复系统过程中，不能操作设备功能。请限制用户操作设备
                    demo.restoreWatchSystem(new OnFatFileProgressListener() {
                        @Override
                        public void onStart(String filePath) { //开始恢复

                        }

                        @Override
                        public void onProgress(float progress) { //恢复进度回调

                        }

                        @Override
                        public void onStop(int result) { //恢复结束
                            //result -- 结果码， 0 -- 正常结束，其他为错误码，参考
                        }
                    });
                }
            }
        });
    }

    @Test
    public void testWatchOp() {
        if (!isSystemInit) {
            System.out.println("系统还没初始化！！！");
            return;
        }
        //遍历表盘文件列表
        demo.listWatchList(new OnWatchOpCallback<ArrayList<FatFile>>() {
            @Override
            public void onSuccess(ArrayList<FatFile> result) {
                ArrayList<FatFile> watchList = filterWatchFiles(result); //表盘文件列表
            }

            @Override
            public void onFailed(BaseError error) { //遍历文件失败回调

            }
        });
        //删除表盘文件
        /*FatFile watch = watchList.isEmpty() ? null : watchList.get(0);
        if (null != watch) {
            demo.deleteWatchFile(watch.getPath(), new OnFatFileProgressListener() {
                @Override
                public void onStart(String filePath) {

                }

                @Override
                public void onProgress(float progress) {

                }

                @Override
                public void onStop(int result) {

                }
            });
        }*/
        //其他操作参考demo
    }

    /**
     * 获得表盘文件
     *
     * @param list 文件列表
     * @return 表盘文件
     */
    //TODO: 文件格式说明
    //文件前缀: watch 或 WATCH  --- 表明是表盘文件
    //文件前缀：bgp_w 或 BGP_W  --- 说明是自定义背景文件
    //其他文件：系统文件，不能被删除，如果误删除，会出现系统异常。
    private ArrayList<FatFile> filterWatchFiles(ArrayList<FatFile> list) {
        if (null == list || list.isEmpty()) return new ArrayList<>();
        ArrayList<FatFile> result = new ArrayList<>();
        for (FatFile fatFile : list) {
            if (fatFile.getName().startsWith("watch") || fatFile.getName().startsWith("WATCH")) { //仅获取表盘文件
                result.add(fatFile);
            }
        }
        return result;
    }
}

class WatchManagerByCustom extends WatchOpImpl {
    private static final String TAG = WatchManagerByCustom.class.getSimpleName();
    /**
     * 是否使用设备认证流程
     */
    public static final boolean IS_USE_DEVICE_AUTH = true;
    /**
     * 单例对象
     */
    private static volatile WatchManagerByCustom instance;

    public static WatchManagerByCustom getInstance() {
        if (null == instance) {
            synchronized (WatchManagerByCustom.class) {
                if (null == instance) {
                    instance = new WatchManagerByCustom();
                }
            }
        }
        return instance;
    }

    /**
     * 蓝牙实现初始化
     */
    //FIXME: 替换成你们的连接实现
    public BleManager mBleManager = BleManager.getInstance();
    /**
     * 设备认证协助类
     */
    private final RcspAuth rcspAuth;
    /**
     * 是否设备认证通过
     */
    public final Map<String, Boolean> authResultMap = new HashMap<>();

    private final OnWatchCallback onWatchCallback = new OnWatchCallback() {

        @Override
        public void onRcspInit(BluetoothDevice device, boolean isInit) {
            //回调RCSP协议初始化状态
            //首先会初始化RCSP协议
            //RCSP初始化失败，按以下情况排查:
            //1. 是否没有通过设备认证
            //2. 是否发送数据异常
        }

        @Override
        public void onWatchSystemInit(int code) {
            //回调手表系统初始化状态
            //检测到支持手表功能，会进行手表系统初始化
            //手表系统初始化失败，按以下情况排查:
            //1. 是否设备离线
            //2. 设备是否支持手表功能
            //3. 手表系统是否发送异常
        }

        @Override
        public void onWatchSystemException(BluetoothDevice device, int sysStatus) {
            //回调手表系统异常
            //AC695N_WATCH_SDK可能会回调系统异常，需要进行恢复系统的操作，恢复成功后才能正常使用手表功能。
            //AC701N_WATCH_SDK一般不会回调系统异常，内部处理了。
        }

        @Override
        public void onMandatoryUpgrade(BluetoothDevice device) {
            //回调设备需要强制升级
            //此情况一般发生在单备份升级异常后。导致设备处于强制升级状态。
            //此时设备仅有BLE，而且BLE仅支持OTA功能。只有强制升级成功后，才能正常使用
        }

        @Override
        public void onResourceUpdateUnfinished(BluetoothDevice device) {
            //回调设备资源未更新完成
            //此情况一般发生在更新资源失败后，导致设备处于更正资源状态。
            //此情况设备手表系统能运行，但是部分资源受损，需要更新资源完成，才能正常使用。
            //部分资源受损，强行使用功能，可能导致设备死机。
        }

        @Override
        public void onNetworkModuleException(BluetoothDevice device, NetworkInfo info) {
            //回调网络模块发送异常
            //此情况一般发送在网络模块升级失败后，设备检测到网络模块(4G模块)异常
            //此情况建议强制升级网络模块，网络模块升级完成后，才能正常使用网络功能。
        }
    };

    //func FUNC_WATCH:手表功能
    //FUNC_RCSP：仅仅使用rcsp协议
    //FUNC_FILE_BROWSE：使用rcsp协议和目录浏览功能
    private WatchManagerByCustom() {
        super(WatchOpImpl.FUNC_WATCH);  //初始化手表功能
        //TODO：设置数据回调
        mBleManager.setOnBleEventListener(new BleManager.OnBleEventListener() {
            @Override
            public void onConnect(BluetoothDevice device, int status) {
                System.out.printf(Locale.ENGLISH, "设备[%s]的连接状态: %d\n", device, status);
                //移除设备认证标志
                authResultMap.remove(device.getAddress());
                if (status == BluetoothProfile.STATE_CONNECTED) { //连接成功回调
                    if (!isAuthPass(device)) {
                        //开启设备认证
                        rcspAuth.stopAuth(device, false);
                        rcspAuth.startAuth(device);
                        return;
                    }
                }
                //TODO: 透传设备认证状态
                //TODO: 连接状态需要转换成jl_watch库的连接状态

                /*
                 * {@link StateCode#CONNECTION_DISCONNECT}    --- 未连接
                 * {@link StateCode#CONNECTION_OK}            --- 连接成功
                 * {@link StateCode#CONNECTION_CONNECTING}    --- 连接中
                 * {@link StateCode#CONNECTION_FAILED}        --- 连接失败
                 */
                int newStatus = RcspUtil.changeConnectStatus(status);
                System.out.printf(Locale.ENGLISH, "原连接状态: %d ==> 转换后连接状态: %d\n", status, newStatus);
                notifyBtDeviceConnection(device, newStatus);
            }

            @Override
            public void onReceiveData(BluetoothDevice device, byte[] data) {
                boolean isAuthPass = isAuthPass(device);
                System.out.printf(Locale.ENGLISH, "是否通过认证: %s,\n接收到设备[%s]的数据[%s]\n", isAuthPass, device, CHexConver.byte2HexStr(data));
                if (!isAuthPass) { //还没通过设备认证
                    //TODO: 透传数据到RcspAuth
                    rcspAuth.handleAuthData(device, data);
                    return;
                }
                //已通过设备认证
                //TODO: 透传数据到杰理健康SDK
                notifyReceiveDeviceData(device, data); //透传设备获取的数据
            }
        });

        //实现设备认证流程
        rcspAuth = new RcspAuth(this::sendDataToDevice, new RcspAuth.OnRcspAuthListener() {
            @Override
            public void onInitResult(boolean result) {

            }

            @Override
            public void onAuthSuccess(BluetoothDevice device) {
                if (null == device) return;
                //设备认证通过
                authResultMap.put(device.getAddress(), true);
                //设备已连接成功
                notifyBtDeviceConnection(device, StateCode.CONNECTION_OK);
            }

            @Override
            public void onAuthFailed(BluetoothDevice device, int code, String message) {
                if (null == device) return;
                //设备认证失败
                //code --- 错误码
                //message --- 错误描述
                authResultMap.put(device.getAddress(), false);
                //断开设备连接
                mBleManager.disconnect(); //断开连接
            }
        });
        //增加RCSP事件监听
        registerOnWatchCallback(onWatchCallback);
    }

    public boolean isAuthPass(BluetoothDevice device) {
        if (null == device) return false;
        if (!IS_USE_DEVICE_AUTH) return true; //不需要设备认证
        Boolean result = authResultMap.get(device.getAddress());
        return result != null && result;
    }

    @Override
    public BluetoothDevice getConnectedDevice() {
        //TODO: 实现连接设备对象返回
        return mBleManager.getConnectedDevice();
    }

    @Override
    public boolean sendDataToDevice(BluetoothDevice device, byte[] data) {
        //TODO: 实现裸数据发送
        //注意，BLE实现发送数据时，数据可能超过BLE的MTU限制。
        //需要做 MTU分包 和 队列式发数 等处理
        JL_Log.d(TAG, "sendDataToDevice", "device : " + device + ", data = " + CHexConver.byte2HexStr(data));
        return mBleManager.sendBleData(data);
    }

    @Override
    public void release() {
        super.release();
        unregisterOnWatchCallback(onWatchCallback);
        mBleManager.setOnBleEventListener(null); //移除回调
        rcspAuth.destroy();
        instance = null;
    }
}


class BleManager {
    private static volatile BleManager instance;

    public static BleManager getInstance() {
        if (null == instance) {
            synchronized (BleManager.class) {
                if (null == instance) {
                    instance = new BleManager();
                }
            }
        }
        return instance;
    }

    private final Context mContext;

    private BluetoothGatt mBluetoothGatt;
    private OnBleEventListener mOnBleEventListener;

    private final UUID serviceUUID = UUID.fromString("你们的服务UUID");
    private final UUID writeUUID = UUID.fromString("你们的写特征UUID");
    private final UUID notifyUUID = UUID.fromString("你们的通知特征UUID");

    private int connection = BluetoothProfile.STATE_DISCONNECTED;
    private int bleMtu = 20;
    private boolean isSending;
    private final LinkedBlockingQueue<byte[]> sendQueue = new LinkedBlockingQueue<>();

    //FIXME：BluetoothGatt回调处理，参考代码，不完整
    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            final BluetoothDevice device = gatt.getDevice();
            if (null == device) return;
            //TODO：回调连接状态
            if (status == 0 && newState == BluetoothProfile.STATE_CONNECTED) {
                mBluetoothGatt = gatt;
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mBluetoothGatt = null;
                gatt.close();
            }
            //可以连接成功就回调，或者完成你们库连接成功同步信息后再回调连接成功状态
            onConnection(device, newState);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            //TODO：回调发送数据状态
            if (writeUUID.equals(characteristic.getUuid())) {
                isSending = false;
                if (status == 0) {
                    if (sendQueue.isEmpty()) {
                        //TODO: 发送数据完成
                        return;
                    }
                    if (!writeDataToBle()) {
                        //TODO: 发送失败
                    }
                } else {
                    //TODO: 发送失败, 可以尝试重发
                    sendQueue.clear();
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            //TODO：回调接收到的裸数据
            if (notifyUUID.equals(characteristic.getUuid())) {
                //直接返回接收到的裸数据
                if (mOnBleEventListener != null) {
                    mOnBleEventListener.onReceiveData(gatt.getDevice(), characteristic.getValue());
                }
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            //TODO：回调MTU改变
            if (status == 0 && (mtu - 3) != bleMtu) {
                bleMtu = mtu - 3;
            }
        }
    };

    private BleManager() {
        mContext = HealthApplication.getAppViewModel().getApplication();
    }

    public int getBleMtu() {
        return bleMtu;
    }

    public BluetoothGatt getBluetoothGatt() {
        return mBluetoothGatt;
    }

    public BluetoothDevice getConnectedDevice() {
        if (null == mBluetoothGatt || connection != BluetoothProfile.STATE_CONNECTED) return null;
        return mBluetoothGatt.getDevice();
    }

    public void setOnBleEventListener(OnBleEventListener onBleEventListener) {
        mOnBleEventListener = onBleEventListener;
    }

    public void connect(BluetoothDevice device) {
        if (null == device || !ConnectUtil.isHasConnectPermission(mContext)) return;
        BluetoothDevice connectedDevice = getConnectedDevice();
        if (null != connectedDevice) {
            if (connectedDevice.getAddress().equalsIgnoreCase(device.getAddress())) {
                if (mOnBleEventListener != null) {
                    mOnBleEventListener.onConnect(device, BluetoothProfile.STATE_CONNECTED);
                }
                return;
            }
            disconnect();
            SystemClock.sleep(500); //延时500毫秒
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBluetoothGatt = device.connectGatt(mContext, false, mBluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
        } else {
            mBluetoothGatt = device.connectGatt(mContext, false, mBluetoothGattCallback);
        }
        onConnection(device, null != mBluetoothGatt ? BluetoothProfile.STATE_CONNECTING : BluetoothProfile.STATE_DISCONNECTED);
    }

    public void disconnect() {
        if (null == mBluetoothGatt) return;
        if (connection == BluetoothProfile.STATE_CONNECTED) {
            mBluetoothGatt.disconnect();
            return;
        }
        final BluetoothDevice device = mBluetoothGatt.getDevice();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        onConnection(device, BluetoothProfile.STATE_DISCONNECTED);
    }

    //FIXME: 参考发送数据实现，不完整。请替换你们库的发送数据实现
    public boolean sendBleData(byte[] data) {
        int offset = 0;
        while (offset < data.length) {
            int left = data.length - offset;
            int packetLen = Math.min(left, bleMtu); //根据MTU分包
            byte[] packet = Arrays.copyOfRange(data, offset, offset + packetLen);
            try {
                sendQueue.add(packet); //添加到发数队列
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            offset += packetLen;
        }
        return writeDataToBle();
    }

    public void destroy() {
        isSending = false;
        sendQueue.clear();
        bleMtu = 20;
        setOnBleEventListener(null);
        disconnect();
        instance = null;
    }

    private void onConnection(BluetoothDevice device, int state) {
        connection = state;
        if (mOnBleEventListener != null) {
            mOnBleEventListener.onConnect(device, state);
        }
    }

    private boolean writeDataToBle() {
        boolean ret = isSending;
        if (!ret) {
            if (mBluetoothGatt == null || sendQueue.isEmpty()) return false;
            byte[] data = sendQueue.poll();
            if (data == null) return false;
            //建议队列式按MTU分包发数
            BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(serviceUUID).getCharacteristic(writeUUID);
            if (null == characteristic) return false;
            characteristic.setValue(data);
            isSending = mBluetoothGatt.writeCharacteristic(characteristic);
            ret = isSending;
        }
        if (!ret) sendQueue.clear();
        return ret;
    }

    interface OnBleEventListener {

        void onConnect(BluetoothDevice device, int status);

        void onReceiveData(BluetoothDevice device, byte[] data);
    }
}

class WatchManagerByJL extends WatchOpImpl {
    private static final String TAG = WatchManagerByJL.class.getSimpleName();
    /**
     * 是否使用设备认证流程
     */
    public static final boolean IS_USE_DEVICE_AUTH = true;

    /**
     * 单例对象
     */
    private static volatile WatchManagerByJL instance;

    public static WatchManagerByJL getInstance() {
        if (null == instance) {
            synchronized (WatchManagerByJL.class) {
                if (null == instance) {
                    instance = new WatchManagerByJL();
                }
            }
        }
        return instance;
    }

    /**
     * 杰理蓝牙连接库初始化
     */
    private final BluetoothHelper jlBtOp = BluetoothHelper.getInstance();
    /**
     * 设备认证协助类
     */
    private final RcspAuth rcspAuth;
    /**
     * 是否设备认证通过
     */
    public final Map<String, Boolean> authResultMap = new HashMap<>();

    private final BluetoothEventListener btEventListener = new BluetoothEventListener() {
        @Override
        public void onConnection(BluetoothDevice device, int status) {
            if (null == device) return;
            //移除设备认证标志
            authResultMap.remove(device.getAddress());
            if (status == BluetoothConstant.CONNECT_STATE_CONNECTED) { //连接成功
                if (!isAuthPass(device)) {//设备需要认证，但没有通过认证
                    //开启设备认证
                    rcspAuth.stopAuth(device, false);
                    rcspAuth.startAuth(device);
                    return;
                }
            }
            //TODO: 透传设备认证状态
            //TODO: 连接状态需要转换成jl_watch库的连接状态

            /* {@link StateCode#CONNECTION_DISCONNECT}    --- 未连接
             * {@link StateCode#CONNECTION_OK}            --- 连接成功
             * {@link StateCode#CONNECTION_CONNECTING}    --- 连接中
             * {@link StateCode#CONNECTION_FAILED}        --- 连接失败
             */
            int newStatus = RcspUtil.changeConnectStatus(status);
            Log.i(TAG, String.format(Locale.ENGLISH, "原连接状态: %d ==> 转换后连接状态: %d", status, newStatus));
            notifyBtDeviceConnection(device, newStatus);
        }

        @Override
        public void onReceiveData(BluetoothDevice device, byte[] data) {
            //回调接收到的数据
            if (!isAuthPass(device)) { //设备还没通过设备认证
                //TODO: 透传数据到RcspAuth
                rcspAuth.handleAuthData(device, data);
                return;
            }
            //TODO: 透传数据到杰理健康SDK
            notifyReceiveDeviceData(device, data);
        }
    };

    private final OnWatchCallback onWatchCallback = new OnWatchCallback() {

        @Override
        public void onRcspInit(BluetoothDevice device, boolean isInit) {
            //回调RCSP协议初始化状态
            //首先会初始化RCSP协议
            //RCSP初始化失败，按以下情况排查:
            //1. 是否没有通过设备认证
            //2. 是否发送数据异常
        }

        @Override
        public void onWatchSystemInit(int code) {
            //回调手表系统初始化状态
            //检测到支持手表功能，会进行手表系统初始化
            //手表系统初始化失败，按以下情况排查:
            //1. 是否设备离线
            //2. 设备是否支持手表功能
            //3. 手表系统是否发送异常
        }

        @Override
        public void onWatchSystemException(BluetoothDevice device, int sysStatus) {
            //回调手表系统异常
            //AC695N_WATCH_SDK可能会回调系统异常，需要进行恢复系统的操作，恢复成功后才能正常使用手表功能。
            //AC701N_WATCH_SDK一般不会回调系统异常，内部处理了。
        }

        @Override
        public void onMandatoryUpgrade(BluetoothDevice device) {
            //回调设备需要强制升级
            //此情况一般发生在单备份升级异常后。导致设备处于强制升级状态。
            //此时设备仅有BLE，而且BLE仅支持OTA功能。只有强制升级成功后，才能正常使用
        }

        @Override
        public void onResourceUpdateUnfinished(BluetoothDevice device) {
            //回调设备资源未更新完成
            //此情况一般发生在更新资源失败后，导致设备处于更正资源状态。
            //此情况设备手表系统能运行，但是部分资源受损，需要更新资源完成，才能正常使用。
            //部分资源受损，强行使用功能，可能导致设备死机。
        }

        @Override
        public void onNetworkModuleException(BluetoothDevice device, NetworkInfo info) {
            //回调网络模块发送异常
            //此情况一般发送在网络模块升级失败后，设备检测到网络模块(4G模块)异常
            //此情况建议强制升级网络模块，网络模块升级完成后，才能正常使用网络功能。
        }
    };

    //func FUNC_WATCH:手表功能
    //FUNC_RCSP：仅仅使用rcsp协议
    //FUNC_FILE_BROWSE：使用rcsp协议和目录浏览功能
    private WatchManagerByJL() {
        super(FUNC_WATCH);
        //初始化RCSP认证类
        rcspAuth = new RcspAuth(this::sendDataToDevice, new RcspAuth.OnRcspAuthListener() {
            @Override
            public void onInitResult(boolean result) {

            }

            @Override
            public void onAuthSuccess(BluetoothDevice device) {
                if (null == device) return;
                //设备认证通过
                authResultMap.put(device.getAddress(), true);
                //设备已连接成功
                btEventListener.onConnection(device, BluetoothConstant.CONNECT_STATE_CONNECTED);
            }

            @Override
            public void onAuthFailed(BluetoothDevice device, int code, String message) {
                if (null == device) return;
                //设备认证失败
                //code --- 错误码
                //message --- 错误描述
                authResultMap.put(device.getAddress(), false);
                //断开设备连接
                jlBtOp.disconnectDevice(device);
            }
        });
        //增加蓝牙事件监听
        jlBtOp.addBluetoothEventListener(btEventListener);
        //增加RCSP事件监听
        registerOnWatchCallback(onWatchCallback);
    }

    public boolean isAuthPass(BluetoothDevice device) {
        if (null == device) return false;
        if (!IS_USE_DEVICE_AUTH) return true; //不需要设备认证
        Boolean result = authResultMap.get(device.getAddress());
        return result != null && result;
    }

    @Override
    public BluetoothDevice getConnectedDevice() {
        //TODO: 操作中的设备对象
        return jlBtOp.getConnectedBtDevice();
    }

    @Override
    public boolean sendDataToDevice(BluetoothDevice device, byte[] data) {
        //TODO: 实现蓝牙发数功能
        return jlBtOp.sendDataToDevice(device, data);
    }

    @Override
    public void release() {
        super.release();
        authResultMap.clear();
        unregisterOnWatchCallback(onWatchCallback);
        jlBtOp.removeBluetoothEventListener(btEventListener);
        rcspAuth.destroy();
        instance = null;
    }
}
