package com.jieli.healthaide.demos;

import android.bluetooth.BluetoothDevice;

import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_fatfs.interfaces.OnFatFileProgressListener;
import com.jieli.jl_fatfs.model.FatFile;
import com.jieli.jl_fatfs.utils.FatUtil;
import com.jieli.jl_rcsp.constant.WatchConstant;
import com.jieli.jl_rcsp.interfaces.watch.OnUpdateResourceCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.WatchFileContent;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.model.device.settings.v0.NetworkInfo;
import com.jieli.jl_rcsp.model.response.ExternalFlashMsgResponse;
import com.jieli.jl_rcsp.tool.DeviceStatusManager;
import com.jieli.jl_rcsp.util.WatchFileUtil;

import org.junit.Test;

import java.util.ArrayList;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 表盘操作Demo
 * @since 2021/12/1
 */
public class WatchDemo {

    public void handleException() {
        //获取手表处理器对象
        WatchManager mWatchManager = WatchManager.getInstance();
        final OnWatchCallback watchCallback = new OnWatchCallback() {
            @Override
            public void onRcspInit(BluetoothDevice device, boolean isInit) {
                super.onRcspInit(device, isInit);
                //回调RCSP协议初始化状态
                //首先会初始化RCSP协议
                //RCSP初始化失败，按以下情况排查:
                //1. 是否没有通过设备认证
                //2. 是否发送数据异常
            }

            @Override
            public void onWatchSystemInit(int code) {
                super.onWatchSystemInit(code);
                //回调手表系统初始化状态
                //检测到支持手表功能，会进行手表系统初始化
                //手表系统初始化失败，按以下情况排查:
                //1. 是否设备离线
                //2. 设备是否支持手表功能
                //3. 手表系统是否发送异常
            }

            @Override
            public void onWatchSystemException(BluetoothDevice device, int sysStatus) {
                super.onWatchSystemException(device, sysStatus);
                //回调手表系统异常
                //AC695N_WATCH_SDK可能会回调系统异常，需要进行恢复系统的操作，恢复成功后才能正常使用手表功能。
                //AC701N_WATCH_SDK一般不会回调系统异常，内部处理了。
            }

            @Override
            public void onMandatoryUpgrade(BluetoothDevice device) {
                super.onMandatoryUpgrade(device);
                //回调设备需要强制升级
                //此情况一般发生在单备份升级异常后。导致设备处于强制升级状态。
                //此时设备仅有BLE，而且BLE仅支持OTA功能。只有强制升级成功后，才能正常使用
            }

            @Override
            public void onResourceUpdateUnfinished(BluetoothDevice device) {
                super.onResourceUpdateUnfinished(device);
                //回调设备资源未更新完成
                //此情况一般发生在更新资源失败后，导致设备处于更正资源状态。
                //此情况设备手表系统能运行，但是部分资源受损，需要更新资源完成，才能正常使用。
                //部分资源受损，强行使用功能，可能导致设备死机。
            }

            @Override
            public void onNetworkModuleException(BluetoothDevice device, NetworkInfo info) {
                super.onNetworkModuleException(device, info);
                //回调网络模块发送异常
                //此情况一般发送在网络模块升级失败后，设备检测到网络模块(4G模块)异常
                //此情况建议强制升级网络模块，网络模块升级完成后，才能正常使用网络功能。
            }
        };
        //注册手表事件监听器
        mWatchManager.registerOnWatchCallback(watchCallback);

        //不需要使用时，注销回调
        //mWatchManager.unregisterOnWatchCallback(watchCallback);
    }

    public void isMandatoryUpgrade() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        final DeviceInfo deviceInfo = watchManager.getDeviceInfo();
        if (null == deviceInfo) return; //设备未初始化完成
        deviceInfo.isMandatoryUpgrade(); //设备是否需要强制升级
    }

    public void isWatchSystemException() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //获取外挂Flash信息
        final ExternalFlashMsgResponse externalFlashMsg = watchManager.getExternalFlashMsg(watchManager.getConnectedDevice());
        if (null == externalFlashMsg) return; //设备不支持手表功能
        boolean isSysException = externalFlashMsg.getSysStatus() != 0; //判断手表系统是否异常，0是正常，其他值为异常
    }

    public void isUpdateResourceException() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        final DeviceInfo deviceInfo = watchManager.getDeviceInfo();
        if (null == deviceInfo) return; //设备未初始化完成
        //判断设备是否处于更新资源模式
        boolean isUpdateResource = deviceInfo.getExpandMode() == WatchConstant.EXPAND_MODE_RES_OTA;
    }

    public void isNetworkModeException() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        final DeviceInfo deviceInfo = watchManager.getDeviceInfo();
        if (null == deviceInfo) return; //设备未初始化完成
        //获取网络模块信息
        final NetworkInfo networkInfo = deviceInfo.getNetworkInfo();
        if(null == networkInfo){//设备不支持网络模块功能
            //可能还没更新，也可以按照2.18.2 接口查询下
            return;
        }
        boolean isNetworkModeException = networkInfo.isMandatoryOTA(); //网络模块是否需要强制升级
    }

    @Test
    void listWatchs() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        watchManager.listWatchList(new OnWatchOpCallback<ArrayList<FatFile>>() {
            @Override
            public void onSuccess(ArrayList<FatFile> result) {
                //成功回调
                //result 是结果，watch前缀的是表盘文件，bgp_w前缀的是自定义背景文件
                //可以过滤获取所有Watch文件
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
            }
        });
    }

    @Test
    void createWatch(String filePath) {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //filePath是表盘文件路径，必须存在
        //isNoNeedCheck：是否跳过文件校验
        // - false： 表盘文件需要文件校验
        // - true :  自定义背景文件不需要文件校验，但需要转换工具进行算法转换
        //OnFatFileProgressListener：进度监听器
        watchManager.createWatchFile(filePath, false, new OnFatFileProgressListener() {
            @Override
            public void onStart(String filePath) {
                //回调开始
            }

            @Override
            public void onProgress(float progress) {
                //回调进度
            }

            @Override
            public void onStop(int result) {
                //回调结束
                //result : 0 --- 成功  非0是错误码，参考WatchError
            }
        });
    }

    @Test
    void deleteWatch(String watchPath) {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //watchPath: 设备存在的表盘文件路径
        //OnFatFileProgressListener：进度监听器
        watchManager.deleteWatchFile(watchPath, new OnFatFileProgressListener() {
            @Override
            public void onStart(String filePath) {
                //回调开始
            }

            @Override
            public void onProgress(float progress) {
                //回调进度
            }

            @Override
            public void onStop(int result) {
                //回调结束
                //result : 0 --- 成功  非0是错误码，参考WatchError
            }
        });
    }

    @Test
    void readWatch(String watchPath) {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //watchPath: 设备存在的表盘文件路径
        //OnWatchOpCallback:结果回调
        //读取文件是一个耗时流程，需要等待一段时间才有结果。
        watchManager.openWatchFile(watchPath, new OnWatchOpCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] result) {
                //成功回调
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调, error是错误信息
            }
        });
    }

    @Test
    void getCurrentWatchInfo() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        watchManager.getCurrentWatchInfo(new OnWatchOpCallback<FatFile>() {
            @Override
            public void onSuccess(FatFile result) {
                //成功回调 - FatFile是表盘信息
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
            }
        });
    }

    @Test
    void getWatchExtraMessage(String watchPath) {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //watchPath:设备表盘文件的路径
        watchManager.getWatchMessage(watchPath, new OnWatchOpCallback<String>() {
            @Override
            public void onSuccess(String result) {
                //成功回调，result是表盘额外信息
                //格式：[version],[uuid]
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
            }
        });
    }

    @Test
    void setCurrentWatch(String watchPath) {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        // watchPath：必须存在于设备的表盘文件路径
        watchManager.setCurrentWatchInfo(watchPath, new OnWatchOpCallback<FatFile>() {
            @Override
            public void onSuccess(FatFile result) {
                //成功回调 - FatFile是表盘信息
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
            }
        });
    }

    @Test
    void getCustomWatchBgInfo(String watchPath) {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //watchPath:设备表盘文件的路径
        watchManager.getCustomWatchBgInfo(watchPath, new OnWatchOpCallback<String>() {
            @Override
            public void onSuccess(String result) {
                //成功回调，result是背景文件的路径
                //如果result是“null”，则是空路径，不存在自定义背景
                //如果result不是“null”，则是背景文件的路径
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
            }
        });
    }

    @Test
    void enableCustomWatchBg(String watchBgPath) {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //watchBgPath: 表盘背景路径
        //若watchBgPath为“/null”，则视为当前表盘清除自定义背景的绑定
        //若watchBgPath不为“/null”，则为当前表盘绑定自定义背景
        watchManager.enableCustomWatchBg(watchBgPath, new OnWatchOpCallback<FatFile>() {
            @Override
            public void onSuccess(FatFile result) {
                //成功回调 - FatFile是自定义背景文件信息
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
            }
        });
    }

    @Test
    void updateResource(String resourcePath) {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //resourcePath: 资源路径 （zip压缩包）
        //OnUpdateResourceCallback: 更新资源进度监听器
        watchManager.updateWatchResource(resourcePath, new OnUpdateResourceCallback() {
            @Override
            public void onStart(String filePath, int total) {
                //回调开始
                //filePath -- 资源文件路径
                //total -- 更新文件总数
            }

            @Override
            public void onProgress(int index, String filePath, float progress) {
                //回调进度
                //index -- 序号 (从0开始)
                //filePath -- 资源文件路径
                //progress -- 更新进度
            }

            @Override
            public void onStop(String otaFilePath) {
                //回调更新结束
                //otaFilePath: 固件升级文件路径
                //若固件升级文件不为null,则意味着需要进行OTA升级
            }

            @Override
            public void onError(int code, String message) {
                //回调错误事件
                //code -- 错误码 (参考WatchError)
                //message -- 错误信息
            }
        });
    }

    @Test
    void restoreSystem() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        final WatchManager watchManager = WatchManager.getInstance();
        //注册手表事件回调
        watchManager.registerOnWatchCallback(new OnWatchCallback() {
            //系统异常回调
            @Override
            public void onWatchSystemException(BluetoothDevice device, int sysStatus) {
                if (sysStatus != 0) { //系统状态不为0，则是系统异常
                    //监听到系统异常，才调用此方法恢复系统
                    //正常情况，禁止调用此方法
                    watchManager.restoreWatchSystem(new OnFatFileProgressListener() {
                        @Override
                        public void onStart(String filePath) {
                            //回调开始
                        }

                        @Override
                        public void onProgress(float progress) {
                            //回调进度
                        }

                        @Override
                        public void onStop(int result) {
                            //回调结果
                            //result : 0 --- 成功  非0是错误码，参考WatchError
                        }
                    });
                }
            }
        });
    }

    @Test
    public void addWatch(String watchFilePath) {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        final WatchManager watchManager = WatchManager.getInstance();
        //执行插入表盘操作并等待结果回调
        watchManager.createWatchFile(watchFilePath, false, new OnFatFileProgressListener() {
            @Override
            public void onStart(String filePath) {
                //回调开始插入表盘
            }

            @Override
            public void onProgress(float progress) {
                //回调插入表盘进度
            }

            @Override
            public void onStop(int result) {
                //回调插入表盘结果
                if (result == 0) { //插入表盘成功
                    //设置当前表盘为插入表盘
                    watchManager.setCurrentWatchInfo(FatUtil.getFatFilePath(watchFilePath), new OnWatchOpCallback<FatFile>() {
                        @Override
                        public void onSuccess(FatFile result) {
                            //设置当前表盘成功
                        }

                        @Override
                        public void onFailed(BaseError error) {
                            //操作失败
                        }
                    });
                } else {
                    //插入文件失败
                }
            }
        });
    }

    @Test
    public void addWatchBg(String bgFilePath) {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        final WatchManager watchManager = WatchManager.getInstance();
        //执行插入自定义表盘背景文件操作并等待结果回调
        watchManager.createWatchFile(bgFilePath, true, new OnFatFileProgressListener() {
            @Override
            public void onStart(String filePath) {
                //回调开始插入表盘背景文件
            }

            @Override
            public void onProgress(float progress) {
                //回调插入表盘背景文件进度
            }

            @Override
            public void onStop(int result) {
                //回调插入表盘背景文件结果
                if (result == 0) { //插入表盘背景文件成功
                    //设置表盘背景文件为当前表盘的自定义背景
                    watchManager.enableCustomWatchBg(FatUtil.getFatFilePath(bgFilePath), new OnWatchOpCallback<FatFile>() {
                        @Override
                        public void onSuccess(FatFile result) {
                            //激活自定义背景成功
                        }

                        @Override
                        public void onFailed(BaseError error) {
                            //操作失败
                        }
                    });
                } else {
                    //插入文件失败
                }
            }
        });
    }

    public void getCurrentDialAllInfo() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        final WatchManager watchManager = WatchManager.getInstance();
        //查询当前表盘信息
        watchManager.getCurrentWatchInfo(new OnWatchOpCallback<FatFile>() {
            @Override
            public void onSuccess(FatFile result) {
                //回调当前表盘信息
                //查询当前表盘的自定义背景信息
                watchManager.getCustomWatchBgInfo(result.getPath(), new OnWatchOpCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        //result 如果是null, 就是默认背景
                        //result 若不是null， 则是自定义背景文件的路径
                    }

                    @Override
                    public void onFailed(BaseError error) {
                        //操作失败
                    }
                });
            }

            @Override
            public void onFailed(BaseError error) {
                //操作失败
            }
        });
    }

    @Test
    public void testMandatoryOTA() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        final WatchManager watchManager = WatchManager.getInstance();
        //注册手表事件回调
        watchManager.registerOnWatchCallback(new OnWatchCallback() {
            @Override
            public void onMandatoryUpgrade(BluetoothDevice device) {
                //设备处于强制升级状态
                //1.跳转到升级界面并进行OTA升级
                //2.单备份案子需要仅升级固件文件，需要特殊处理下
                DeviceInfo deviceInfo = watchManager.getDeviceInfo(device); //获取设备新
                if (!deviceInfo.isSupportDoubleBackup()) { //单备份升级
                    String dirPath = "存放资源升级文件的文件夹路径";
                    String otaFilePath = WatchFileUtil.obtainUpdateFilePath(dirPath, ".ufw"); //寻找升级文件
                    if (otaFilePath != null) {
                        //开始OTA流程
                        //startOTA();
                    }
                } else { //双备份升级
                    //开始OTA流程
                    //startOTA();
                }
            }
        });
    }

    @Test
    public void getWatchSystemInfo() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        final WatchManager watchManager = WatchManager.getInstance();
        //获取缓存的手表系统信息
        ExternalFlashMsgResponse watchSysInfo = DeviceStatusManager.getInstance().getExtFlashMsg(watchManager.getConnectedDevice());
        if (null == watchSysInfo) return;
        int screenWidth = watchSysInfo.getScreenWidth();    //手表屏幕的宽度
        int screenHeight = watchSysInfo.getScreenHeight();  //手表屏幕的高度
    }

    @Test
    public void getWatchSysLeftSize() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        final WatchManager watchManager = WatchManager.getInstance();
        //获取手表系统剩余空间
        watchManager.getWatchSysLeftSize(new OnWatchOpCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                //手表剩余空间
            }

            @Override
            public void onFailed(BaseError error) {
                //错误信息
            }
        });
    }

    @Test
    public void getWatchFileSize(String fatFilePath) {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        final WatchManager watchManager = WatchManager.getInstance();
        //fatFilePath : 表盘文件路径(必须存在)
        //获取表盘文件的大小
        watchManager.getWatchFileSize(fatFilePath, new OnWatchOpCallback<WatchFileContent>() {
            @Override
            public void onSuccess(WatchFileContent result) {
                result.getFileSize(); //表盘文件的大小
                result.getCrc();      //表盘文件的CRC (AC695X的SDK, 此此字段无效)
            }

            @Override
            public void onFailed(BaseError error) {
                //错误信息
            }
        });
    }

}
