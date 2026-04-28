package com.jieli.watchtesttool.tool.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.watchtesttool.WatchApplication;
import com.jieli.watchtesttool.util.WatchTestConstant;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 功能配置辅助类
 * @since 2022/5/20
 */
public class ConfigHelper {
    private static volatile ConfigHelper instance;
    private final SharedPreferences preferences;

    private static final String KEY_SPP_CONNECT_WAY = "key_spp_connect_way";
    private static final String KEY_USE_DEVICE_AUTH = "key_use_device_auth";
    private static final String KEY_BAN_AUTO_TEST = "key_ban_auto_test";
    private static final String KEY_TEST_FILE_TRANSFER = "key_test_file_transfer";
    private static final String KEY_TEST_FILE_BROWSE = "key_test_file_browse";
    private static final String KEY_TEST_SMALL_FILE_TRANSFER = "key_test_small_file_transfer";
    private static final String KEY_TEST_WATCH_OP = "key_test_watch_op";
    private static final String KEY_TEST_OTA = "key_test_ota";
    private static final String KEY_FILTER_DEVICE = "key_filter_device";
    private static final String KEY_TEMP_CONNECT_WAY = "key_temp_connect_way";
    private static final String KEY_ADJUST_BLE_MTU = "key_adjust_ble_mtu";
    private static final String KEY_TEST_MESSAGE_SYNC = "key_test_message_sync";

    private static final String KEY_ADV_FILTER_PREFIX = "key_adv_filter_prefix";

    private static final String KEY_FILE_NAME_ENCODE = "key_file_name_encode";

    private static final String KEY_TEST_RECORD = "key_test_record";
    private static final String KEY_DECODE_AUDIO_DATA = "key_decode_audio_data";
    private static final String KEY_SAVE_RAW_AUDIO_FILE = "key_save_raw_audio_file";

    private ConfigHelper(@NonNull Context context) {
        this.preferences = context.getSharedPreferences("watch_config_data", Context.MODE_PRIVATE);
    }

    public static ConfigHelper getInstance() {
        if (null == instance) {
            synchronized (ConfigHelper.class) {
                if (null == instance) {
                    instance = new ConfigHelper(WatchApplication.getWatchApplication());
                }
            }
        }
        return instance;
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public boolean isSPPConnectWay() {
        return preferences.getBoolean(KEY_SPP_CONNECT_WAY, WatchTestConstant.DEFAULT_CONNECT_WAY == BluetoothConstant.PROTOCOL_TYPE_SPP);
    }

    public void setSppConnectWay(boolean isSppConnectWay) {
        preferences.edit().putBoolean(KEY_SPP_CONNECT_WAY, isSppConnectWay).apply();
    }

    public boolean isUseDeviceAuth() {
        return preferences.getBoolean(KEY_USE_DEVICE_AUTH, WatchTestConstant.USE_DEVICE_AUTH);
    }

    public void setUseDeviceAuth(boolean isUseDeviceAuth) {
        preferences.edit().putBoolean(KEY_USE_DEVICE_AUTH, isUseDeviceAuth).apply();
    }

    public boolean isBanAutoTest() {
        return preferences.getBoolean(KEY_BAN_AUTO_TEST, WatchTestConstant.BAN_AUTO_TEST);
    }

    public void setBanAutoTest(boolean isBanAutoTest) {
        preferences.edit().putBoolean(KEY_BAN_AUTO_TEST, isBanAutoTest).apply();
    }

    public boolean isTestFileTransfer() {
        return preferences.getBoolean(KEY_TEST_FILE_TRANSFER, WatchTestConstant.TEST_FILE_TRANSFER);
    }

    public void setTestFileTransfer(boolean isTestFileTransfer) {
        preferences.edit().putBoolean(KEY_TEST_FILE_TRANSFER, isTestFileTransfer).apply();
    }

    public boolean isTestFileBrowse() {
        return preferences.getBoolean(KEY_TEST_FILE_BROWSE, WatchTestConstant.TEST_FILE_BROWSE);
    }

    public void setTestFileBrowse(boolean isTestFileBrowse) {
        preferences.edit().putBoolean(KEY_TEST_FILE_BROWSE, isTestFileBrowse).apply();
    }

    public boolean isTestSmallFileTransfer() {
        return preferences.getBoolean(KEY_TEST_SMALL_FILE_TRANSFER, WatchTestConstant.TEST_SMALL_FILE_TRANSFER);
    }

    public void setTestSmallFileTransfer(boolean isTestSmallFileTransfer) {
        preferences.edit().putBoolean(KEY_TEST_SMALL_FILE_TRANSFER, isTestSmallFileTransfer).apply();
    }

    public boolean isTestWatchOp() {
        return preferences.getBoolean(KEY_TEST_WATCH_OP, WatchTestConstant.TEST_WATCH_OP);
    }

    public void setTestWatchOp(boolean isTestWatchOp) {
        preferences.edit().putBoolean(KEY_TEST_WATCH_OP, isTestWatchOp).apply();
    }

    public boolean isTestOTA() {
        return preferences.getBoolean(KEY_TEST_OTA, WatchTestConstant.TEST_OTA_FUNC);
    }

    public void setTestOTA(boolean isTestOTA) {
        preferences.edit().putBoolean(KEY_TEST_OTA, isTestOTA).apply();
    }

    public boolean isFilterDevice() {
        return preferences.getBoolean(KEY_FILTER_DEVICE, WatchTestConstant.FILTER_DEVICE);
    }

    public void setFilterDevice(boolean isFilterDevice) {
        preferences.edit().putBoolean(KEY_FILTER_DEVICE, isFilterDevice).apply();
    }

    public int getTempConnectWay() {
        return preferences.getInt(KEY_TEMP_CONNECT_WAY, -1);
    }

    public void setTempConnectWay(int tempConnectWay) {
        SharedPreferences.Editor editor = preferences.edit();
        if (tempConnectWay < 0) {
            editor.remove(KEY_TEMP_CONNECT_WAY).apply();
        } else {
            editor.putInt(KEY_TEMP_CONNECT_WAY, tempConnectWay);
        }
        editor.apply();
    }

    public int getBleMtu() {
        return preferences.getInt(KEY_ADJUST_BLE_MTU, WatchTestConstant.DEFAULT_BLE_MTU);
    }

    public void setBleMtu(@IntRange(from = 20, to = 514) int mtu) {
        preferences.edit().putInt(KEY_ADJUST_BLE_MTU, mtu).apply();
    }

    public boolean isTestMessageSync() {
        return preferences.getBoolean(KEY_TEST_MESSAGE_SYNC, false);
    }

    public void setTestMessageSync(boolean isEnable) {
        preferences.edit().putBoolean(KEY_TEST_MESSAGE_SYNC, isEnable).apply();
    }

    public String getAdvFilterPrefix() {
        return preferences.getString(KEY_ADV_FILTER_PREFIX, "");
    }

    public void setAdvFilterPrefix(String prefix) {
        preferences.edit().putString(KEY_ADV_FILTER_PREFIX, prefix).apply();
    }

    public String getEncodeName() {
        return preferences.getString(KEY_FILE_NAME_ENCODE, "");
    }

    public void setFileNameEncode(String encode) {
        preferences.edit().putString(KEY_FILE_NAME_ENCODE, encode).apply();
    }

    public boolean isUseOtherEncode() {
        return !TextUtils.isEmpty(getEncodeName());
    }

    public boolean isTestRecord() {
        return preferences.getBoolean(KEY_TEST_RECORD, WatchTestConstant.TEST_RECORD);
    }

    public void setTestRecord(boolean testRecord) {
        preferences.edit().putBoolean(KEY_TEST_RECORD, testRecord).apply();
    }

    public boolean isDecodeAudioData() {
        return preferences.getBoolean(KEY_DECODE_AUDIO_DATA, false);
    }

    public void setDecodeAudioData(boolean isDecode) {
        preferences.edit().putBoolean(KEY_DECODE_AUDIO_DATA, isDecode).apply();
    }

    public boolean isSaveRawAudioFile() {
        return preferences.getBoolean(KEY_SAVE_RAW_AUDIO_FILE, false);
    }

    public void setSaveRawAudioFile(boolean isSaveFile) {
        preferences.edit().putBoolean(KEY_SAVE_RAW_AUDIO_FILE, isSaveFile).apply();
    }
}
