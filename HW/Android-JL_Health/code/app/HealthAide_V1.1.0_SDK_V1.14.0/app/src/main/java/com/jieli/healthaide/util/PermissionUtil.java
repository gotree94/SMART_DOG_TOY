package com.jieli.healthaide.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

/**
 * 权限工具类
 *
 * @author zqjasonZhong
 * @date 2020/4/8
 */
public class PermissionUtil {
    /**
     * 是否具有读取位置权限
     *
     * @param context 上下文
     * @return 结果
     */
    public static boolean isHasLocationPermission(Context context) {
//        if (Build.VERSION.SDK_INT >= 31) return true;
        return isHasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    /**
     * 是否具有读写存储器权限
     *
     * @param context 上下文
     * @return 结果
     */
    public static boolean isHasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= 28) {
            if (Build.VERSION.SDK_INT >= 33) {
                return isHasPermission(context, Manifest.permission.READ_MEDIA_IMAGES) || isHasPermission(context, Manifest.permission.READ_MEDIA_AUDIO)
                        || isHasPermission(context, Manifest.permission.READ_MEDIA_VIDEO);
            }
            return isHasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        return isHasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) && isHasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    /**
     * 是否具有读写联系人列表的权限
     *
     * @param context 上下文
     * @return 结果
     */
   /* public static boolean isContactPermission(Context context) {
        return isHasPermission(context, Manifest.permission.READ_CONTACTS) && isHasPermission(context, Manifest.permission.READ_CALL_LOG);
    }*/

    /**
     * 是否具有录音的权限
     *
     * @param context 上下文
     * @return 结果
     */
    public static boolean isRecordPermission(Context context) {
        return isHasPermission(context, Manifest.permission.RECORD_AUDIO);
    }

    /**
     * 是否具有指定权限
     *
     * @param context    上下文
     * @param permission 权限
     *                   <p>参考{@link Manifest.permission}</p>
     * @return 结果
     */
    public static boolean isHasPermission(Context context, String permission) {
        return context != null && ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 检查GPS位置功能是否使能
     *
     * @param context 上下文
     * @return 结果
     */
    public static boolean checkGpsProviderEnable(Context context) {
        if (context == null) return false;
        LocationManager locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locManager != null && locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * 检查指定权限是否还能弹框提示
     *
     * @param activity   显示界面
     * @param permission 指定权限
     * @return 结果
     */
    public static boolean checkPermissionShouldShowDialog(Activity activity, String permission) {
        return permission != null && ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }
}
