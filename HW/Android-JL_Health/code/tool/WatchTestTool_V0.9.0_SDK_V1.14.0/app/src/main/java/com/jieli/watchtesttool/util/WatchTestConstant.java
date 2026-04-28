package com.jieli.watchtesttool.util;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 手表常量定义
 * @since 2021/4/20
 */
public class WatchTestConstant {

    /*功能配置区*/
    //默认连接方式
    public final static int DEFAULT_CONNECT_WAY = BluetoothConstant.PROTOCOL_TYPE_BLE;
    //默认BLE的MTU
    public final static int DEFAULT_BLE_MTU = BluetoothConstant.BLE_MTU_MAX;
    //是否过滤设备
    public final static boolean FILTER_DEVICE = true;
    //是否使用设备认证
    public final static boolean USE_DEVICE_AUTH = true;
    //是否禁止自动测试功能
    public final static boolean BAN_AUTO_TEST = true;
    //测试大文件传输功能
    public final static boolean TEST_FILE_TRANSFER = true;
    //测试文件浏览
    public final static boolean TEST_FILE_BROWSE = false;
    //测试表盘操作功能
    public final static boolean TEST_WATCH_OP = true;
    //测试OTA相关功能
    public final static boolean TEST_OTA_FUNC = true;
    //测试小文件传输功能
    public final static boolean TEST_SMALL_FILE_TRANSFER = false;
    //测试录音功能
    public final static boolean TEST_RECORD = false;


    public final static String DIR_WATCH = "watch";        //表盘测试文件夹
    public final static String DIR_WATCH_BG = "watch_bg";  //表盘自定义背景测试文件夹
    public final static String DIR_MUSIC = "music";        //音乐文件测试文件夹
    public final static String DIR_CONTACTS = "contacts";  //联系人测试文件夹
    public final static String DIR_UPDATE = "upgrade";     //升级文件
    public final static String DIR_RECORD = "record";      //录音文件
    public final static String DIR_MESSAGE = "message";    //设备信息

    public final static String DIR_BR23 = "BR23";          //BR23资源文件夹
    public final static String DIR_BR28 = "BR28";          //BR28资源文件夹
    public final static String DIR_BR35 = "BR35";          //BR35资源文件夹
    public final static String DIR_NETWORK = "network";    //网络升级文件夹

    public final static String VERSION_W001 = "W001";      //版本1
    public final static String VERSION_W002 = "W002";      //版本2

    public final static String KEY_FORCED_UPDATE_FLAG = "forced_update_flag_1";  //强制更新资源标志
}
