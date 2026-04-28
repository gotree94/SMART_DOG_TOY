package com.jieli.healthaide.util;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;

/**
 * 健康APP常量
 *
 * @author zqjasonZhong
 * @since 2021/3/8
 */
public class HealthConstant {

    public final static String WATCH_TAG = "zzc_watch";
    public final static int REQUEST_CODE_PERMISSIONS = 2333;
    public final static int REQUEST_CODE_CHECK_GPS = 2334;
    public final static int REQUEST_CODE_CAMERA = 2335;
    public final static int REQUEST_CODE_STORAGE = 2336;

    public final static int DEFAULT_CONNECT_WAY = BluetoothConstant.PROTOCOL_TYPE_BLE;
    public final static boolean ONLY_CONNECT_BLE = false; //是否仅连接BLE

    public final static String DIR_UPDATE = "upgrade";
    public final static String DIR_WATCH = "watch";
    public final static String DIR_USER = "user";
    public final static String DIR_NFC = "nfc";
    public final static String DIR_LOG = "log";
    public final static String DIR_NETWORK = "network";
    //Watch configure
    public final static int WATCH_MAX_COUNT = 10; //最多表盘数
    //是否仅一个自定义表盘背景
    public final static boolean IS_SINGLE_CUSTOM_DIAL_BG = true;
    //是否使用测试服务器
    public final static boolean USE_TEST_SERVER = false;
    //测试功能
    public final static boolean TEST_DEVICE_FUNCTION = false;
    //同步设备电量
    public final static boolean SYNC_DEV_POWER = true;
    //测试NFC功能
    public final static boolean TEST_NFC_FUNCTION = false;
    //是否打开删除表盘支付记录
    public final static boolean DELETE_DIAL_RECORD = false;

    /*表盘类型*/
    public static final int DIAL_TYPE_FREE = 0;
    public static final int DIAL_TYPE_PAY = 1;
    public static final int DIAL_TYPE_RECORD = 2;

    //default package name
    public final static String PACKAGE_NAME_SYS_MESSAGE = "com.android.mms";
    public final static String PACKAGE_NAME_WECHAT = "com.tencent.mm";
    public final static String PACKAGE_NAME_QQ = "com.tencent.mobileqq";
    public final static String PACKAGE_NAME_DING_DING = "com.alibaba.android.rimet";


    public static final String EXTRA_WATCH_INFO = "watch_info";
    public static final String ACTION_PAYMENT_SUCCESS = "com.jieli.healthaide.dial_payment_success";
    public static final String ACTION_RECONNECT_DEVICE = "com.jieli.healthaide.reconnect_device";

    public static final String ACTION_UPDATE_RESOURCE_SUCCESS = "com.jieli.healthaide.update_resource_success";
    public static final String KEY_WEATHER_PUSH = "key_weather_push";

    public static final String KEY_ASSERT_RES_SYNC = "key_assert_res_sync";//更新assert资源文件

    /**
     * 是否同意隐私协议
     */
    public static final String KEY_AGREE_PRIVACY_POLICY = "key_agree_privacy_policy";

    public static final String KEY_DEVICE_ADDRESS = "key_device_address";

    //测试AI表盘功能
    public final static boolean TEST_AI_DIAL_FUNCTION = false;

}
