package com.jieli.watchtesttool.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.util.BluetoothUtil;
import com.jieli.bluetooth_connect.util.ConnectUtil;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.watchtesttool.WatchApplication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 工具类
 * @since 2021/4/20
 */
public class AppUtil {

    public static BluetoothDevice getRemoteDevice(String address) {
        return BluetoothUtil.getRemoteDevice(WatchApplication.getWatchApplication(), address);
    }

    public static String printBtDeviceInfo(BluetoothDevice device) {
        return BluetoothUtil.printBtDeviceInfo(WatchApplication.getWatchApplication(), device);
    }

    /**
     * 创建文件路径
     *
     * @param context  上下文
     * @param dirNames 文件夹名
     * @return 路径
     */
    public static String createFilePath(Context context, String... dirNames) {
        if (context == null || dirNames == null || dirNames.length == 0) return null;
        File file = context.getExternalFilesDir(null);
        if (file == null || !file.exists()) return null;
        StringBuilder filePath = new StringBuilder(file.getPath());
        if (filePath.toString().endsWith("/")) {
            filePath = new StringBuilder(filePath.substring(0, filePath.lastIndexOf("/")));
        }
        for (String dirName : dirNames) {
            filePath.append("/").append(dirName);
            file = new File(filePath.toString());
            if (!file.exists() || file.isFile()) {//文件不存在
                if (!file.mkdir()) {
                    Log.w("jieli", "create dir failed. filePath = " + filePath);
                    break;
                }
            }
        }
        return filePath.toString();
    }

    /**
     * 获取指定文件类型的路径
     *
     * @param dirPath 目录路径
     * @param suffix  文件后续
     * @return 文件路径
     */
    public static String obtainUpdateFilePath(String dirPath, String suffix) {
        if (null == dirPath) return null;
        File dir = new File(dirPath);
        if (!dir.exists()) return null;
        if (dir.isFile()) {
            if (dirPath.endsWith(suffix)) {
                return dirPath;
            } else {
                return null;
            }
        } else if (dir.isDirectory()) {
            String filePath = null;
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    filePath = obtainUpdateFilePath(file.getPath(), suffix);
                    if (filePath != null) {
                        break;
                    }
                }
            }
            return filePath;
        }
        return null;
    }

    /**
     * 获取设备名称
     *
     * @param device 蓝牙设备
     * @return 设备名
     */
    @SuppressLint("MissingPermission")
    public static String getDeviceName(BluetoothDevice device) {
        if (null == device) return null;
        if (!ConnectUtil.isHasConnectPermission(WatchApplication.getWatchApplication()))
            return device.getAddress();
        String name = device.getName();
        if (null == name) {
            name = device.getAddress();
        }
        return name;
    }

    /**
     * 获取文件名
     *
     * @param filePath 文件路径
     * @return 文件名
     */
    public static String getFileNameByPath(String filePath) {
        if (filePath == null) return null;
        int index = filePath.lastIndexOf("/");
        if (index > -1) {
            return filePath.substring(index + 1);
        } else {
            return filePath;
        }
    }

    /**
     * 转换成手表的连接状态
     *
     * @param status 系统连接状态
     * @return 手表连接状态
     */
    public static int convertWatchConnectStatus(int status) {
        int newStatus;
        switch (status) {
            case BluetoothConstant.CONNECT_STATE_CONNECTING:
                newStatus = StateCode.CONNECTION_CONNECTING;
                break;
            case BluetoothConstant.CONNECT_STATE_CONNECTED:
                newStatus = StateCode.CONNECTION_OK;
                break;
            default:
                newStatus = StateCode.CONNECTION_DISCONNECT;
                break;
        }
        return newStatus;
    }

    /**
     * 转换成OTA的连接状态
     *
     * @param status 系统连接状态
     * @return OTA连接状态
     */
    public static int convertOtaConnectStatus(int status) {
        int newStatus;
        switch (status) {
            case BluetoothConstant.CONNECT_STATE_CONNECTING:
                newStatus = com.jieli.jl_bt_ota.constant.StateCode.CONNECTION_CONNECTING;
                break;
            case BluetoothConstant.CONNECT_STATE_CONNECTED:
                newStatus = com.jieli.jl_bt_ota.constant.StateCode.CONNECTION_OK;
                break;
            default:
                newStatus = com.jieli.jl_bt_ota.constant.StateCode.CONNECTION_DISCONNECT;
                break;
        }
        return newStatus;
    }

    /**
     * 复制assets资源
     *
     * @param context 上下文
     * @param oldPath assets路径
     * @param newPath 复制资源路径
     */
    public static void copyAssets(Context context, String oldPath, String newPath) {
        try {
            String[] fileNames = context.getAssets().list(oldPath);// 获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {// 如果是目录
                File file = new File(newPath);
                if (!file.exists()) {
                    boolean ret = file.mkdirs();// 如果文件夹不存在，则递归
                    if (!ret) return;
                }
                for (String fileName : fileNames) {
                    copyAssets(context, oldPath + File.separator + fileName, newPath + File.separator + fileName);
                }
            } else {// 如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取文件大小描述
     *
     * @param fileSize 文件大小
     * @return 体积描述
     */
    @NonNull
    public static String getFileSizeDesc(long fileSize) {
        if (fileSize >= 1024 * 1024) {
            double value = fileSize / (1024.0 * 1024.0);
            return String.format(Locale.getDefault(), "%.1f MB", value);
        } else {
            double value = fileSize / 1024.0;
            return String.format(Locale.getDefault(), "%.1f KB", value);
        }
    }

    @NonNull
    public static String formatOTATime(int time) {
        return String.format(Locale.getDefault(), "%s:%s", formatTime(time / 60), formatTime(time % 60));
    }

    @NonNull
    public static String formatTime(int time) {
        if (time <= 0) {
            return "00";
        } else if (time < 10) {
            return "0" + time;
        } else {
            return String.valueOf(time);
        }
    }

    public static int getTextValue(@NonNull EditText editText) {
        return getTextValue(editText, 0);
    }

    public static int getTextValue(@NonNull EditText editText, int defaultValue) {
        String text = editText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return defaultValue;
        int count = defaultValue;
        try {
            count = Integer.parseInt(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public static int getTextViewHeight(@NonNull TextView textView) {
        Layout layout = textView.getLayout();
        if (null == layout) return 0;
        int desired = layout.getLineTop(textView.getLineCount());
        int padding = textView.getCompoundPaddingTop() + textView.getCompoundPaddingBottom();
        return desired + padding;
    }

    public static String getDirPath(@NonNull String filePath) {
        int index = filePath.lastIndexOf(File.separator);
        if (index == -1) return filePath;
        return filePath.substring(0, index);
    }

    public static String readJsonByPath(@NonNull File file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int size;
            while ((size = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, size);
            }
            String content = outputStream.toString();
            inputStream.close();
            outputStream.close();
            return content;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
