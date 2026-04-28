package com.jieli.healthaide.ui.device.more;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.component.utils.PreferencesHelper;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentMoreBinding;
import com.jieli.healthaide.tool.notification.NotificationHelper;
import com.jieli.healthaide.tool.watch.synctask.SyncTaskManager;
import com.jieli.healthaide.tool.watch.synctask.WeatherSyncTask;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.device.health.BaseHealthSettingFragment;
import com.jieli.jl_dialog.Jl_Dialog;
import com.jieli.jl_rcsp.constant.AttrAndFunCode;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.model.device.health.DisconnectReminder;
import com.jieli.jl_rcsp.model.device.health.LiftWristDetection;

import org.jetbrains.annotations.NotNull;

import static com.jieli.healthaide.util.HealthConstant.KEY_WEATHER_PUSH;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/7/26
 * @desc :
 */
public class MoreFragment extends BaseHealthSettingFragment {

    FragmentMoreBinding binding;

    static final int REQUEST_CODE_NOTIFICATION = 6514;
    static final int FLAG_NOTIFICATION_LISTENER_PERMISSION = 1;
    static final int FLAG_NOTIFICATION_ENABLE = 2;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_more, container, false);
        binding = FragmentMoreBinding.bind(root);

        binding.viewTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.viewTopbar.tvTopbarTitle.setText(R.string.more);


        binding.tvAlert.setOnClickListener(v -> checkNotificationServerEnable(requireContext()));
        boolean isSupportSync = PreferencesHelper.getSharedPreferences(requireContext()).getBoolean(KEY_WEATHER_PUSH, false);
        binding.swWeatherPush.setChecked(isSupportSync);
        binding.swWeatherPush.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PreferencesHelper.putBooleanValue(requireContext(), KEY_WEATHER_PUSH, isChecked);
            if (isChecked) {
                SyncTaskManager.getInstance().addTask(new WeatherSyncTask(SyncTaskManager.getInstance(), SyncTaskManager.getInstance()));
            }
            SyncTaskManager.getInstance().setSupportSyncWeather(isChecked);
        });

        binding.twBtDisconnectTip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (null == viewModel.getHealthSettingInfo()) return;
            DisconnectReminder disconnectReminder = viewModel.getHealthSettingInfo().getDisconnectReminder();
            disconnectReminder.setEnable(isChecked);
            viewModel.sendSettingCmd(disconnectReminder);

        });

        binding.clBrightScreen.setOnClickListener(v -> ContentActivity.startContentActivity(requireContext(), LiftWristDetectionFragment.class.getCanonicalName()));


        binding.tvSensorSetting.setOnClickListener(v -> ContentActivity.startContentActivity(requireContext(), SensorSettingFragment.class.getCanonicalName()));


        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel.requestHealthSettingInfo(0x01 << AttrAndFunCode.HEALTH_SETTING_TYPE_LIFT_WRIST_DETECTION
                | 0x01 << AttrAndFunCode.HEALTH_SETTING_TYPE_DISCONNECT_REMINDER);
        viewModel.healthSettingInfoLiveData().observe(getViewLifecycleOwner(), healthSettingInfo -> {
            LiftWristDetection liftWristDetection = healthSettingInfo.getLiftWristDetection();
//            binding.twBrightScreen.setCheckedNoEvent(liftWristDetection.isEnable());
            DisconnectReminder disconnectReminder = healthSettingInfo.getDisconnectReminder();
            binding.twBtDisconnectTip.setCheckedNoEvent(disconnectReminder.isEnable());
            String[] statusString = getResources().getStringArray(R.array.sw_status_string_array);
            binding.tvLiftWristDetectionValue.setText(statusString[liftWristDetection.getStatus()]);
        });
        viewModel.mDeviceConfigureMLD.observe(getViewLifecycleOwner(), device -> updateUI());
        updateUI();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_NOTIFICATION && NotificationHelper.isNotificationServiceEnabled(requireContext())
                && NotificationHelper.isNotificationEnable(requireContext())) {
            ContentActivity.startContentActivity(requireContext(), MessageSyncFragment.class.getCanonicalName());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateUI() {
        WatchConfigure configure = viewModel.getWatchConfigure();
        if (null == configure) return;
        boolean isShowMessageSync = configure.getFunctionOption() != null && configure.getFunctionOption().isSupportMessageSync();
        binding.tvAlert.setVisibility(isShowMessageSync ? View.VISIBLE : View.GONE);
        boolean isShowDisconnect = configure.getSystemSetup() != null && configure.getSystemSetup().isSupportBtDisconnectSetting();
        binding.rlBtDisconnect.setVisibility(isShowDisconnect ? View.VISIBLE : View.GONE);
        boolean isShowScreenSettings = configure.getSystemSetup() != null && configure.getSystemSetup().isSupportScreenSetting();
        binding.clBrightScreen.setVisibility(isShowScreenSettings ? View.VISIBLE : View.GONE);
        boolean isSensorSettings = configure.getSportHealthConfigure() != null && configure.getSportHealthConfigure().getCombineFunc() != null
                && configure.getSportHealthConfigure().getCombineFunc().isSupportSensorSettings();
        binding.tvSensorSetting.setVisibility(isSensorSettings ? View.VISIBLE : View.GONE);
    }

    private void checkNotificationServerEnable(Context context) {
        if (!NotificationHelper.isNotificationServiceEnabled(context)) {
            showEnableNotificationListenerDialog(FLAG_NOTIFICATION_LISTENER_PERMISSION);
        } else if (!NotificationHelper.isNotificationEnable(context)) {
            showEnableNotificationListenerDialog(FLAG_NOTIFICATION_ENABLE);
        } else {
            ContentActivity.startContentActivity(context, MessageSyncFragment.class.getCanonicalName());
        }
    }

    private void showEnableNotificationListenerDialog(int flag) {
        if (!isFragmentValid()) return;
        Jl_Dialog.builder()
                .width(0.8f)
                .cancel(true)
                .content(getString(R.string.enable_notification_listener_service_tips))
                .left(getString(R.string.cancel))
                .leftColor(getResources().getColor(R.color.black))
                .leftClickListener((view, dialogFragment) -> dialogFragment.dismiss())
                .right(getString(R.string.sure))
                .rightColor(getResources().getColor(R.color.red_D25454))
                .rightClickListener((view, dialogFragment) -> {
                    dialogFragment.dismiss();
                    if (flag == FLAG_NOTIFICATION_LISTENER_PERMISSION) {
                        startActivityForResult(new Intent(NotificationHelper.ACTION_NOTIFICATION_LISTENER_SETTINGS), REQUEST_CODE_NOTIFICATION);
                    } else {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", requireContext().getApplicationContext().getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_CODE_NOTIFICATION);
                    }
                })
                .build()
                .show(getChildFragmentManager(), "notification_listener_service");
    }
}
