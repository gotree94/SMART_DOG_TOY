package com.jieli.healthaide.ui.test;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentTestConfigurationBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.test.func.CustomCmdTestFragment;
import com.jieli.healthaide.ui.test.log.LogFileFragment;
import com.jieli.healthaide.ui.test.model.TestConfiguration;
import com.jieli.healthaide.util.UIHelper;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 自定义命令测试界面
 * @since 2026/01/12
 */
public class TestConfigurationFragment extends BaseFragment {

    private FragmentTestConfigurationBinding binding;
    private TestConfigurationViewModel viewModel;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTestConfigurationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TestConfigurationViewModel.class);
        initUI();
        addObserver();
    }

    private void initUI() {
        binding.viewTopBar.tvTopbarTitle.setText(getString(R.string.test_configuration));
        binding.viewTopBar.tvTopbarLeft.setOnClickListener(v -> finish());
        UIHelper.show(binding.viewTopBar.tvTopbarRight);
        binding.viewTopBar.tvTopbarRight.setText(getString(R.string.save_configuration));
        binding.viewTopBar.tvTopbarRight.setOnClickListener(v -> tryToSaveTestConfiguration());

        UIHelper.updateItemSettingsSwitchUI(binding.viewSaveLog, getString(R.string.enable_log_function), 0, false,
                (buttonView, isChecked) -> {
                    viewModel.syncTestConfiguration(getTestConfigurationByUI());
                    updateSaveLogUI(isChecked);
                });
        UIHelper.updateItemSettingsSwitchUI(binding.viewUseDeviceAuth, getString(R.string.use_device_auth), 0, false,
                (buttonView, isChecked) -> viewModel.syncTestConfiguration(getTestConfigurationByUI()));
        UIHelper.updateItemSettingsSwitchUI(binding.viewLocalOta, getString(R.string.support_local_ota), 0, false,
                (buttonView, isChecked) -> {
                    viewModel.syncTestConfiguration(getTestConfigurationByUI());
                    updateLocalOTAUI(isChecked);
                });

        binding.viewCustomCommand.getRoot().setBackgroundResource(R.drawable.bg_card_white_15_shape);
        binding.viewCustomCommand.tvItemSettingsName.setTextSize(15);
        binding.viewCustomCommand.tvItemSettingsName.setTypeface(Typeface.DEFAULT_BOLD);
        UIHelper.updateItemSettingsTextUI(binding.viewCustomCommand, getString(R.string.test_custom_cmd),
                null, true, false, v -> goToTestCustomCmdActivity());

        updateSaveConfigurationUI(viewModel.isChangeCfg());
        updateTestConfiguration(viewModel.getTestConfiguration());
    }

    private void addObserver() {
        viewModel.cfgChangeMLD.observe(getViewLifecycleOwner(), this::updateSaveConfigurationUI);
    }

    private void updateSaveConfigurationUI(boolean isChange) {
        binding.viewTopBar.tvTopbarRight.setEnabled(isChange);
        binding.viewTopBar.tvTopbarRight.setClickable(isChange);
        binding.viewTopBar.tvTopbarRight.setTextColor(ContextCompat.getColor(requireContext(),
                isChange ? R.color.blue_448eff : R.color.gray_838383));
    }

    private void updateTestConfiguration(TestConfiguration cfg) {
        if (!isFragmentValid() || null == cfg) return;
        UIHelper.updateItemSettingsSwitchUI(binding.viewSaveLog, null, 0, cfg.isEnableLogFunc(), null);
        updateSaveLogUI(cfg.isEnableLogFunc());
        UIHelper.updateItemSettingsSwitchUI(binding.viewUseDeviceAuth, null, 0, cfg.isEnableDeviceAuth(), null);
        UIHelper.updateItemSettingsSwitchUI(binding.viewLocalOta, null, 0, cfg.isEnableLocalOTATest(), null);
        updateLocalOTAUI(cfg.isEnableLocalOTATest());
    }

    private void updateSaveLogUI(boolean isShow) {
        if (!isShow) {
            UIHelper.gone(binding.viewLogPath.getRoot());
            return;
        }
        UIHelper.show(binding.viewLogPath.getRoot());
        String logPath = viewModel.getSaveLogFilePath().replaceAll("/storage/emulated/0", "");
        UIHelper.updateItemSettingsTextUI(binding.viewLogPath, getString(R.string.log_file), logPath, true, false, v -> goToLogFileFragment());
    }

    private void updateLocalOTAUI(boolean isShow) {
        if (!isShow) {
            UIHelper.gone(binding.tvLocalOtaTips);
            return;
        }
        UIHelper.show(binding.tvLocalOtaTips);
        String packageName = requireContext().getApplicationContext().getPackageName();
        String otaTips = getString(R.string.local_ota_desc, packageName);
        binding.tvLocalOtaTips.setText(otaTips);
    }

    private void goToLogFileFragment() {
        ContentActivity.startContentActivity(requireContext(), LogFileFragment.class.getCanonicalName());
    }

    private void goToTestCustomCmdActivity() {
        if (!viewModel.isDeviceConnected()) {
            showTips(getString(R.string.device_is_disconnected));
            return;
        }
        ContentActivity.startContentActivity(requireContext(), CustomCmdTestFragment.class.getCanonicalName());
        finish();
    }

    private TestConfiguration getTestConfigurationByUI() {
        return new TestConfiguration(binding.viewSaveLog.switchBtn.isChecked(), binding.viewUseDeviceAuth.switchBtn.isChecked(),
                binding.viewLocalOta.switchBtn.isChecked());
    }

    private void tryToSaveTestConfiguration() {
        if (!viewModel.isChangeCfg()) return;
        viewModel.saveTestConfiguration();
//        showTips(getString(R.string.save_configuration_success));
        finish();
    }

}