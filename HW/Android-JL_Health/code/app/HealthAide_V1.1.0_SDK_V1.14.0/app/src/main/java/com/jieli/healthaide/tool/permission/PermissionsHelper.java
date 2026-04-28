package com.jieli.healthaide.tool.permission;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;

import com.jieli.bluetooth_connect.util.JL_Log;
import com.jieli.component.permission.PermissionManager;
import com.jieli.component.utils.SystemUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.healthaide.util.PermissionUtil;
import com.jieli.jl_dialog.Jl_Dialog;

/**
 * 权限辅助类
 *
 * @author zqjasonZhong
 * @date 2020/3/25
 */
public class PermissionsHelper {
    private final static String TAG = "PermissionsHelper";
    private AppCompatActivity mActivity;

    private OnPermissionListener mListener;
    private final Handler mHandler;

    private PermissionManager mPermissionManager;

    /**
     * 应用请求的权限列表
     */
    public final static String[] sPermissions = {
            /*  Manifest.permission.READ_EXTERNAL_STORAGE,
              Manifest.permission.WRITE_EXTERNAL_STORAGE,*/

            /*  Manifest.permission.ACCESS_COARSE_LOCATION,
              Manifest.permission.ACCESS_FINE_LOCATION,*/

            /* Manifest.permission.READ_PHONE_STATE,*/

            /*   Manifest.permission.READ_CONTACTS,*/
    };

    public String[] mPermissions;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        destroy();
    }

    public PermissionsHelper(AppCompatActivity activity) {
        mActivity = SystemUtil.checkNotNull(activity);
        mHandler = new Handler(Looper.getMainLooper());
        mPermissions = sPermissions;
    }

    public static boolean checkAppPermissionsIsAllow(Context context) {
        if (context == null) return false;
        return PermissionUtil.isHasLocationPermission(context);
    }

    public void destroy() {
        if (mPermissionManager != null) {
            mPermissionManager.release();
            mPermissionManager = null;
        }
        mListener = null;
        mActivity = null;
        mHandler.removeCallbacksAndMessages(null);
    }

    private boolean hasPermission(String[] permissions, String target) {
        if (permissions == null || target == null) return false;
        for (String permission : permissions) {
            if (target.equals(permission)) {
                return true;
            }
        }
        return false;
    }

    public void checkAppRequestPermissions(OnPermissionListener listener) {
        checkAppRequestPermissions(sPermissions, listener);
    }

    public void checkAppRequestPermissions(String[] permissions, OnPermissionListener listener) {
        if (mActivity == null) return;
        if (permissions != null) mPermissions = permissions;
        mListener = listener;
        getPermissionManager().permissions(permissions).callback(new PermissionManager.OnPermissionStateCallback() {
            @Override
            public void onSuccess() {
                if (!hasPermission(permissions, Manifest.permission.ACCESS_COARSE_LOCATION) || PermissionUtil.checkGpsProviderEnable(mActivity)) {
                    callbackPermissionSuccess(mPermissions);
                } else {
                    JL_Log.i(TAG, "checkAppRequestPermissions", "onGPSError");
                    showNotifyGPSDialog();
                }
            }

            @Override
            public void onFailed(boolean isShouldShowDialog, String permission, Intent intent) {
                JL_Log.i(TAG, "onFailed", "isShouldShowDialog : " + isShouldShowDialog + ", " + permission);
                if (isShouldShowDialog) {
                    showToPermissionSettingDialog(permission, intent);
                } else {
                    callbackPermissionFailed(permission);
                }
            }

            @Override
            public void onError(int code, String message) {

            }
        }).request();
    }

    public String getPermissionName(String permission) {
        String name = permission;
        switch (permission) {
            case Manifest.permission.READ_CONTACTS:
            case Manifest.permission.WRITE_CONTACTS:
                name = getString(R.string.permission_contacts);
                break;
            case Manifest.permission.RECORD_AUDIO:
                name = getString(R.string.permission_mic);
                break;
            case Manifest.permission.READ_PHONE_STATE:
                name = getString(R.string.permission_read_phone_state);
                break;
            case Manifest.permission.ACCESS_COARSE_LOCATION:
            case Manifest.permission.ACCESS_FINE_LOCATION:
                name = getString(R.string.permission_location);
                break;
            case Manifest.permission.READ_EXTERNAL_STORAGE:
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                name = getString(R.string.permission_storage);
                break;
            case Manifest.permission.WRITE_SETTINGS:
                name = getString(R.string.permission_phone_settings);
                break;
        }
        return name;
    }

    private PermissionManager getPermissionManager() {
        if (mPermissionManager == null) {
            mPermissionManager = PermissionManager.with(mActivity);
        }
        return mPermissionManager;
    }

    private void showToPermissionSettingDialog(final String permission, final Intent intent) {
        if (mActivity == null || mActivity.isDestroyed() || mActivity.isFinishing()) return;
        String sb = getString(R.string.permissions_tips_01) +
                getPermissionName(permission) +
                getString(R.string.permission);
        new Jl_Dialog.Builder()
                .title(getString(R.string.tips))
                .content(sb)
                .cancel(false)
                .left(getString(R.string.cancel))
                .leftColor(mActivity.getResources().getColor(R.color.text_secondary_color))
                .right(getString(R.string.go_setting))
                .rightColor(mActivity.getResources().getColor(R.color.auxiliary_error))
                .leftClickListener((v, dialogFragment) -> {
                    dialogFragment.dismiss();
                    callbackPermissionFailed(permission);
                })
                .rightClickListener((v, dialogFragment) -> {
                    dialogFragment.dismiss();
                    if (Manifest.permission.WRITE_SETTINGS.equals(permission)) {
                        mActivity.startActivityForResult(intent, HealthConstant.REQUEST_CODE_PERMISSIONS);
                    } else {
                        checkAppRequestPermissions(mPermissions, mListener);
                    }
                })
                .build().show(mActivity.getSupportFragmentManager(), "request_permission");
    }

    /**
     * 显示打开定位服务(gps)提示框
     */
    private void showNotifyGPSDialog() {
        if (mActivity == null || mActivity.isDestroyed() || mActivity.isFinishing()) return;
        new Jl_Dialog.Builder()
                .title(getString(R.string.tips))
                .content(getString(R.string.open_gpg_tip))
                .cancel(false)
                .left(getString(R.string.cancel))
                .leftColor(mActivity.getResources().getColor(R.color.text_secondary_color))
                .right(getString(R.string.go_setting))
                .rightColor(mActivity.getResources().getColor(R.color.auxiliary_error))
                .leftClickListener((v, dialogFragment) -> {
                    dialogFragment.dismiss();
                    callbackPermissionFailed(Manifest.permission.ACCESS_FINE_LOCATION);
                })
                .rightClickListener((v, dialogFragment) -> {
                    dialogFragment.dismiss();
                    mActivity.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), HealthConstant.REQUEST_CODE_CHECK_GPS);
                })
                .build()
                .show(mActivity.getSupportFragmentManager(), "notify_gps_dialog");
    }

    private String getString(int res) {
        if (mActivity == null) return null;
        return mActivity.getString(res);
    }

    private void callbackPermissionSuccess(final String[] permissions) {
        if (permissions != null && mListener != null) {
            mHandler.post(() -> {
                if (mListener != null) mListener.onPermissionsSuccess(permissions);
            });
        }
    }

    private void callbackPermissionFailed(final String permission) {
        if (permission != null && mListener != null) {
            mHandler.post(() -> {
                if (mListener != null) mListener.onPermissionFailed(permission);
            });
        }
    }

    public interface OnPermissionListener {

        void onPermissionsSuccess(String[] permissions);

        void onPermissionFailed(String permission);
    }
}
