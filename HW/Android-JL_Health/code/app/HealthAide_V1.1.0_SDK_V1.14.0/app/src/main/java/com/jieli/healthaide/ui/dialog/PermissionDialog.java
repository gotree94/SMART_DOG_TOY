package com.jieli.healthaide.ui.dialog;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.base.BaseDialogFragment;

import java.util.Locale;

import permissions.dispatcher.PermissionRequest;

/**
 * @ClassName: PermissionDialog
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/11/24 10:31
 */
public class PermissionDialog extends BaseDialogFragment {
    private final String permission;
    private final PermissionRequest request;
    private final OnPermissionClickListener listener;

    public PermissionDialog(String permission, PermissionRequest request) {
        this(permission, request, null);
    }

    public PermissionDialog(String permission, PermissionRequest request, OnPermissionClickListener listener) {
        this.permission = permission;
        this.request = request;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            Window window = getDialog().getWindow();
            if (window != null) {
                //去掉dialog默认的padding
                window.getDecorView().setPadding(0, 0, 0, 0);
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.width = Math.round(0.9f * getScreenWidth());
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.CENTER;
                //设置dialog的动画
//                lp.windowAnimations = R.style.BottomToTopAnim;
                window.setAttributes(lp);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            View view = inflater.inflate(R.layout.dialog_permission, container, false);
            TextView tvDialogTitle = view.findViewById(R.id.tv_dialog_permission_title);
            TextView tvDialogContent = view.findViewById(R.id.tv_dialog_permission_content);
            Button btNoRequire = view.findViewById(R.id.btn_chose_no_require);
            Button btRequire = view.findViewById(R.id.btn_chose_require);
            boolean isShouldRequest = (request != null || listener != null);
            String text = getString(R.string.permission_system_set);
            switch (permission) {
                case Manifest.permission.CAMERA:
                    tvDialogTitle.setText(getString(R.string.permissions_camera));
                    tvDialogContent.setText(isShouldRequest ? String.format(Locale.ENGLISH, "%s:\n%s",
                            getString(R.string.instructions_for_use), getString(R.string.permissions_camera_explain))
                            : getString(R.string.permission_describe_camera, getString(R.string.permission_system_set)));
                    break;
                case Manifest.permission.READ_CONTACTS:
                    tvDialogTitle.setText(getString(R.string.permissions_read_contact));
                    tvDialogContent.setText(isShouldRequest ? String.format(Locale.ENGLISH, "%s:\n%s",
                            getString(R.string.instructions_for_use), getString(R.string.permissions_read_contact_explain))
                            : getString(R.string.permission_describe_contacts, text));
                    break;
                case Manifest.permission.READ_EXTERNAL_STORAGE:
                case Manifest.permission.READ_MEDIA_IMAGES:
                case Manifest.permission.READ_MEDIA_AUDIO:
                case Manifest.permission.READ_MEDIA_VIDEO:
                    tvDialogTitle.setText(getString(R.string.permissions_storage));
                    tvDialogContent.setText(isShouldRequest ? String.format(Locale.ENGLISH, "%s:\n%s",
                            getString(R.string.instructions_for_use), getString(R.string.permissions_storage_explain))
                            : getString(R.string.permission_describe_external_storage, text));
                    break;
                case Manifest.permission.ACCESS_COARSE_LOCATION:
                case Manifest.permission.ACCESS_FINE_LOCATION:
                    tvDialogTitle.setText(getString(R.string.permissions_location));
                    tvDialogContent.setText(isShouldRequest ? String.format(Locale.ENGLISH, "%s:\n%s",
                            getString(R.string.instructions_for_use), getString(R.string.permissions_location_explain))
                            : getString(R.string.permission_describe_location, text));
                    break;
                case Manifest.permission.BLUETOOTH_CONNECT:
                case Manifest.permission.BLUETOOTH_SCAN:
                    tvDialogTitle.setText(getString(R.string.permissions_bluetooth));
                    tvDialogContent.setText(isShouldRequest ? String.format(Locale.ENGLISH, "%s:\n%s",
                            getString(R.string.instructions_for_use), getString(R.string.permissions_bluetooth_explain))
                            : String.format(Locale.ENGLISH, "%s%s%s",
                            getString(R.string.permissions_tips_01), getString(R.string.permission_bluetooth),
                            getString(R.string.permission)));
                    break;
            }
            btNoRequire.setOnClickListener(v -> dismiss());
            btRequire.setOnClickListener(v -> {
                if (null == listener) {
                    if (request != null) {
                        request.proceed();
                    } else {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                } else {
                    listener.onRequest(permission);
                }
                dismiss();
            });
            return view;
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * 权限请求点击事件监听器
     */
    public interface OnPermissionClickListener {

        /**
         * 请求权限
         *
         * @param permission 权限
         */
        void onRequest(String permission);
    }

}
