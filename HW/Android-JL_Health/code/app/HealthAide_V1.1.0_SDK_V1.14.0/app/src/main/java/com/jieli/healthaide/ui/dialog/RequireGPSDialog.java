package com.jieli.healthaide.ui.dialog;

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

import permissions.dispatcher.PermissionRequest;

/**
 * @ClassName: RequireGPSDialog
 * @Description: 提示用户打开GPS
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/11/23 17:01
 */
public class RequireGPSDialog extends BaseDialogFragment {
    public static int VIEW_TYPE_DEVICE = 1;
    public static int VIEW_TYPE_SPORT = 2;
    private TextView btNoMovement;
    private TextView btRequirePermission;
    private TextView btStartSport;
    private OnGPSChooseListener mListener;
    private int viewType;
    private PermissionRequest request;
    private boolean isLocationService = false;

    public RequireGPSDialog(int viewType, PermissionRequest request) {
        this.viewType = viewType;
        this.request = request;
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
                lp.width = viewType == VIEW_TYPE_SPORT ? WindowManager.LayoutParams.MATCH_PARENT : Math.round(0.9f * getScreenWidth());
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = viewType == VIEW_TYPE_SPORT ? Gravity.BOTTOM : Gravity.CENTER;
                //设置dialog的动画
//                lp.windowAnimations = R.style.BottomToTopAnim;
                window.setAttributes(lp);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            View view = null;
            if (viewType == VIEW_TYPE_SPORT) {
                view = inflater.inflate(R.layout.dialog_require_gps, container, false);
                TextView tvDialogTitle = view.findViewById(R.id.tv_dialog_gps_title);
                TextView tvDialogContent = view.findViewById(R.id.tv_dialog_gps_content);
                btNoMovement = view.findViewById(R.id.tv_dialog_gps_no_movement);
                btRequirePermission = view.findViewById(R.id.btn_gps_chose_require);
                btStartSport = view.findViewById(R.id.btn_gps_chose_sport);
                btNoMovement.setOnClickListener(v -> dismiss());
                btRequirePermission.setOnClickListener(v -> onRequirePermission());
                btStartSport.setOnClickListener(v -> onStartSport());
                tvDialogTitle.setText(isLocationService ? R.string.open_gps_services : R.string.open_gps_permission);
                tvDialogContent.setText(isLocationService ? R.string.permission_describe_gps_service_sport : R.string.permission_describe_gps_permission_sport);
            } else if (viewType == VIEW_TYPE_DEVICE) {
                view = inflater.inflate(R.layout.dialog_permission, container, false);
                TextView tvDialogTitle = view.findViewById(R.id.tv_dialog_permission_title);
                TextView tvDialogContent = view.findViewById(R.id.tv_dialog_permission_content);
                Button btNoRequire = view.findViewById(R.id.btn_chose_no_require);
                Button btRequire = view.findViewById(R.id.btn_chose_require);
                btNoRequire.setOnClickListener(v -> dismiss());
                btRequire.setOnClickListener(v -> onRequirePermission());
                if (isLocationService) {
                    tvDialogTitle.setText(R.string.open_gps_services);
                    tvDialogContent.setText(R.string.open_gps_service_tip);
                    btNoRequire.setText(R.string.cancel);
                    btRequire.setText(R.string.allow);
                } else {
                    tvDialogContent.setText(request != null ? getString(R.string.permission_describe_location, "")
                            : getString(R.string.permission_describe_location, getString(R.string.permission_system_set)));
                }
            }
            return view;
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void setOnGPSChooseListener(OnGPSChooseListener listener) {
        mListener = listener;
    }

    public void setLocationService(boolean locationService) {//请求类型是不是位置信息
        isLocationService = locationService;
    }

    void onRequirePermission() {
        if (!isLocationService) {
            if (request != null) {
                request.proceed();
            } else {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        } else {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        dismiss();
    }

    void onStartSport() {
        if (mListener != null) {
            mListener.onStartSport();
        }
    }

    public interface OnGPSChooseListener {
        void onStartSport();
    }
}
