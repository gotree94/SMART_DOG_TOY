package com.jieli.healthaide.ui.sports.ui.set;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentSportPermissionBinding;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.util.SportPermissionUtil;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public class SportPermissionFragment extends BaseFragment {
    private SportPermissionAdapter permissionAdapter;
    private final int MSG_REFRESH_STATUS = 1;
    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_REFRESH_STATUS) {
                JL_Log.d("ZHM", "MSG_REFRESH_STATUS", "");
                List<SportPermission> permissionList = new ArrayList<>();
                permissionList.add(getSuspensionPermission());
                permissionList.add(getBatteryOptimizedPermission());
                permissionList.add(getBackgroundPermission());
                permissionList.add(getPowerSavePermission());
                permissionAdapter.setList(permissionList);
                handler.removeMessages(MSG_REFRESH_STATUS);
                handler.sendEmptyMessageDelayed(MSG_REFRESH_STATUS, 500);
            }
        }
    };


    public SportPermissionFragment() {
        // Required empty public constructor
    }

    public static SportPermissionFragment newInstance() {
        return new SportPermissionFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentSportPermissionBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sport_permission, container, false);
        binding.tvPermissionTip.setText(getString(R.string.permission_sport_tips, getString(R.string.app_name)));
        permissionAdapter = new SportPermissionAdapter();
        binding.rvSportPermission.setAdapter(permissionAdapter);
        binding.layoutTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().finish());
        binding.layoutTopbar.tvTopbarTitle.setText(R.string.permission_sport);
        handler.removeMessages(MSG_REFRESH_STATUS);
        handler.sendEmptyMessageDelayed(MSG_REFRESH_STATUS, 0);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeMessages(MSG_REFRESH_STATUS);
    }

    //悬浮窗权限
    private SportPermission getSuspensionPermission() {
        return new SportPermission() {

            @Override
            void init() {
                String appName = getString(R.string.app_name);
                super.permissionTitle = getString(R.string.permission_sport_suspension_title);
                super.permissionDescribe = getString(R.string.permission_sport_suspension_describe, appName);
                super.permissionOperate = getString(R.string.permission_sport_suspension_operate);
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            void operate() {
                //todo 跳转悬浮窗设置
                int REQUEST_CODE = 11;
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" +
                        Objects.requireNonNull(requireActivity()).getPackageName())), REQUEST_CODE);

            }
        };
    }

    //电池优化权限(加入白名单)
    private SportPermission getBatteryOptimizedPermission() {
        return new SportPermission() {

            @Override
            void init() {
                String appName = getString(R.string.app_name);
                super.permissionTitle = getString(R.string.permission_sport_battery_optimized_title);
                super.permissionDescribe = getString(R.string.permission_sport_battery_optimized_describe, appName);
                int operateSrc = R.string.permission_sport_battery_optimized_operate_2;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    operateSrc = SportPermissionUtil.isIgnoringBatteryOptimizations(requireContext()) ? R.string.permission_sport_battery_optimized_operate_2 : R.string.permission_sport_battery_optimized_operate_1;
                }
                super.permissionOperate = getString(operateSrc);
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            void operate() {
                //todo 关闭电池优化
                boolean isClosed = true;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    isClosed = SportPermissionUtil.isIgnoringBatteryOptimizations(requireActivity().getApplicationContext());
                }
                if (isClosed) {
                    showTips(R.string.permission_sport_battery_optimized_operate_2);
                } else {
                    SportPermissionUtil.requestIgnoreBatteryOptimizations(getContext());
                }
            }
        };
    }

    //后台自启动权限
    private SportPermission getBackgroundPermission() {
        return new SportPermission() {

            @Override
            void init() {
                String appName = getString(R.string.app_name);
                super.permissionTitle = getString(R.string.permission_sport_background_title);
                String guide = SportPermissionUtil.getAutoStartSettingGuide(requireContext());
                super.permissionDescribe = getString(R.string.permission_sport_background_describe, appName) + guide;
                super.permissionOperate = getString(R.string.permission_sport_background_operate);
            }

            @Override
            void operate() {
                //todo 打开后台保护设置
                SportPermissionUtil.startToAutoStartSetting(getContext());
            }
        };
    }

    //电池优化无限制白名单
    private SportPermission getPowerSavePermission() {
        return new SportPermission() {

            @Override
            void init() {
                String appName = getString(R.string.app_name);
                super.permissionTitle = getString(R.string.permission_sport_powersave_title);
                String guide = SportPermissionUtil.getSleepPreventionGuide(requireContext());
                super.permissionDescribe = getString(R.string.permission_sport_powersave_describe, appName) + guide;
                super.permissionOperate = getString(R.string.permission_sport_powersave_oprate);
            }

            @Override
            void operate() {
                //todo 打开防睡眠系统设置
                SportPermissionUtil.startToSleepPreventionSetting(getContext());
            }
        };
    }
}