package com.jieli.healthaide.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.IntRange;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.util.BluetoothUtil;
import com.jieli.bluetooth_connect.util.ConnectUtil;
import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.jl_bt_ota.constant.ErrorCode;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.constant.WatchError;

import java.io.File;
import java.util.Arrays;

/**
 * @author zqjasonZhong
 * @since 2021/3/9
 */
public class HealthUtil {

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
                //根据修改时间倒序进行排序
                Arrays.sort(files, (o1, o2) -> Long.compare(o2.lastModified(), o1.lastModified()));
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
        if (!ConnectUtil.isHasConnectPermission(HealthApplication.getAppViewModel().getApplication()))
            return device.getAddress();
        String name = device.getName();
        if (null == name) {
            name = device.getAddress();
        }
        return name;
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
     * 创建指定尺寸图像
     *
     * @param path         图像路径
     * @param targetWidth  目标宽度
     * @param targetHeight 目标高度
     * @return 图像
     */
    public static Bitmap createScaleBitmap(String path, int targetWidth, int targetHeight) {
        if (path == null || targetWidth <= 0 || targetHeight <= 0) {
            return null;
        }

        // Step 1: 获取原始尺寸并计算采样率
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);
        options.inJustDecodeBounds = false;

        // Step 2: 加载采样后的 bitmap
        Bitmap bmpSrc = BitmapFactory.decodeFile(path, options);
        if (bmpSrc == null) return null;

        int srcWidth = bmpSrc.getWidth();
        int srcHeight = bmpSrc.getHeight();

        if (srcWidth == targetWidth && srcHeight == targetHeight) {
            return bmpSrc;
        }

        // Step 3: 缩放比例及变换矩阵
        float widthScale = targetWidth * 1.0f / srcWidth;
        float heightScale = targetHeight * 1.0f / srcHeight;
        float scale = Math.max(widthScale, heightScale);

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Step 4: 居中绘制
        float dx = (targetWidth - srcWidth * scale) / 2;
        float dy = (targetHeight - srcHeight * scale) / 2;
        matrix.postTranslate(dx, dy);

        // Step 5: 绘制新 bitmap
        Bitmap bmpRet = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmpRet);
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG); // 增强图像质量
        canvas.drawBitmap(bmpSrc, matrix, paint);

        // 注意：这里未主动 recycle bmpSrc，因为其生命周期由外部管理；如确定不再使用，请手动 recycle

        return bmpRet;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
    /**
     * 保存缩放图像
     *
     * @param path         源图像路径
     * @param targetWidth  目标宽度
     * @param targetHeight 目标高度
     * @param quality      压缩比例
     * @return 输出图像路径
     */
    public static String saveScaleBitmap(String path, int targetWidth, int targetHeight, @IntRange(from = 0, to = 100) int quality) {
        Bitmap bitmap = createScaleBitmap(path, targetWidth, targetHeight);
        if (null == bitmap) return null;
        boolean ret = BitmapUtil.bitmapToFile(bitmap, path, quality);
        return ret ? path : null;
    }

    /**
     * 更新图片
     *
     * @param context   上下文
     * @param imageView 图片控件
     * @param uri       图片链接
     */
    public static void updateWatchImg(Context context, ImageView imageView, String uri) {
        if (imageView == null || context == null) return;
        boolean isSetBitmap = false;
        if (uri != null) {
            boolean isGif = uri.endsWith(".gif");
            if (uri.startsWith("res:")) { //资源路径
                String string = uri.substring("res:".length());
                int resId = 0;
                if (TextUtils.isDigitsOnly(string)) {
                    try {
                        resId = Integer.parseInt(string);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    imageView.setImageResource(resId);
                    isSetBitmap = true;
                }
                if (!isSetBitmap) {
                    imageView.setImageResource(R.drawable.ic_watch_6);
                }
            } else if (uri.startsWith("http://") || uri.startsWith("https://")) { //网络路径
                if (isGif) {
                    Glide.with(context).asGif().load(uri)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .override(ValueUtil.dp2px(context, 108), ValueUtil.dp2px(context, 108))
                            .placeholder(R.drawable.ic_watch_6)
                            .error(R.drawable.ic_watch_6)
                            .into(imageView);
                } else {
                    Glide.with(context).asBitmap().load(uri)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .override(ValueUtil.dp2px(context, 108), ValueUtil.dp2px(context, 108))
                            .placeholder(R.drawable.ic_watch_6)
                            .error(R.drawable.ic_watch_6)
                            .into(imageView);
                }
            } else { //本地路径
                if (isGif) {
                    Glide.with(context).asGif().load(new File(uri))
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .override(ValueUtil.dp2px(context, 108), ValueUtil.dp2px(context, 108))
                            .placeholder(R.drawable.ic_watch_6)
                            .error(R.drawable.ic_watch_6)
                            .into(imageView);
                } else {
                    Glide.with(context).asBitmap().load(new File(uri))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .override(ValueUtil.dp2px(context, 108), ValueUtil.dp2px(context, 108))
                            .placeholder(R.drawable.ic_watch_6)
                            .error(R.drawable.ic_watch_6)
                            .into(imageView);
                }
            }
        } else {
            imageView.setImageResource(R.drawable.ic_watch_6);
        }
    }

    public static String getPriceFormat(int price) {
        float value = price / 100f;
        return CalendarUtil.formatString("%.2f", value);
    }

    public static boolean isMatchInfo(WatchInfo watchInfo, WatchInfo watchInfo1) {
        if (null == watchInfo || null == watchInfo1) return false;
        return (watchInfo.getUuid() != null && watchInfo.getUuid().equalsIgnoreCase(watchInfo1.getUuid()))
                || (watchInfo.getName() != null && watchInfo.getName().equalsIgnoreCase(watchInfo1.getName())
                && watchInfo.getVersion() != null && watchInfo.getVersion().equalsIgnoreCase(watchInfo1.getVersion()));
    }

    public static int getHttpErrorCode(String message) {
        String[] array = message.split(",");
        if (array.length < 2) return 0;
        return Integer.parseInt(array[0]);
    }

    public static BluetoothDevice getRemoteDevice(String address) {
        return BluetoothUtil.getRemoteDevice(HealthApplication.getAppViewModel().getApplication(), address);
    }

    public static String printBtDeviceInfo(BluetoothDevice device) {
        return BluetoothUtil.printBtDeviceInfo(HealthApplication.getAppViewModel().getApplication(), device);
    }

    public static String getOTAErrDesc(Context context, int code) {
        switch (code) {
            case ErrorCode.SUB_ERR_DEVICE_LOW_VOLTAGE:
                return context.getString(R.string.ota_err_low_power, "30%");
            case ErrorCode.SUB_ERR_CHECK_UPGRADE_FILE:
                return context.getString(R.string.ota_err_file_info);
            case ErrorCode.SUB_ERR_UPGRADE_SAME_FILE:
            case ErrorCode.SUB_ERR_UPGRADE_FILE_VERSION_SAME:
                return context.getString(R.string.ota_err_file_same_version);
            case ErrorCode.SUB_ERR_TWS_NOT_CONNECT:
                return context.getString(R.string.ota_err_tws_not_connect);
            case ErrorCode.SUB_ERR_HEADSET_NOT_IN_CHARGING_BIN:
                return context.getString(R.string.ota_err_not_in_charging_bin);
            case ErrorCode.SUB_ERR_CHECK_RECEIVED_DATA_FAILED:
                return context.getString(R.string.ota_err_data_crc_check);
            case ErrorCode.SUB_ERR_UPGRADE_KEY_NOT_MATCH:
                return context.getString(R.string.ota_err_key_mismatch);
            case ErrorCode.SUB_ERR_UPGRADE_TYPE_NOT_MATCH:
                return context.getString(R.string.ota_err_uboot_mismatch);
            case ErrorCode.SUB_ERR_UPGRADE_DATA_LEN:
                return context.getString(R.string.ota_err_data_len);
            case ErrorCode.SUB_ERR_UPGRADE_FLASH_READ:
                return context.getString(R.string.ota_err_flash_io);
            case ErrorCode.SUB_ERR_UPGRADE_CMD_TIMEOUT:
                return context.getString(R.string.ota_err_cmd_timeout);
            case ErrorCode.SUB_ERR_RECONNECT_TIMEOUT:
                return context.getString(R.string.ota_err_reconnect_device_timeout);
            case ErrorCode.SUB_ERR_WAITING_COMMAND_TIMEOUT:
                return context.getString(R.string.ota_err_waiting_for_cmd_timeout);
            case WatchError.ERR_RESPONSE_TIMEOUT:
            case ErrorCode.SUB_ERR_SEND_TIMEOUT:
                return context.getString(R.string.ota_err_send_data_timeout);
            case ErrorCode.SUB_ERR_DATA_NOT_FOUND:
                return context.getString(R.string.ota_err_update_data);
            case ErrorCode.SUB_ERR_FILE_NOT_FOUND:
                return context.getString(R.string.ota_err_not_found_file);
            case WatchError.ERR_RESPONSE_BAD_STATUS:
            case ErrorCode.SUB_ERR_RESPONSE_BAD_STATUS:
                return context.getString(R.string.ota_err_bad_status);
            case WatchError.ERR_RESPONSE_BAD_RESULT:
            case ErrorCode.SUB_ERR_RESPONSE_BAD_RESULT:
                return context.getString(R.string.ota_err_bad_result);
            case ErrorCode.SUB_ERR_UPGRADE_UNKNOWN:
                return context.getString(R.string.ota_err_unknown);
            case WatchError.ERR_REMOTE_NOT_CONNECT:
            case ErrorCode.SUB_ERR_REMOTE_NOT_CONNECTED:
                return context.getString(R.string.ota_err_device_not_connect);
            case ErrorCode.SUB_ERR_OTA_IN_HANDLE:
                return "OTA is in progress.";
            default:
                return "";
        }
    }
}
