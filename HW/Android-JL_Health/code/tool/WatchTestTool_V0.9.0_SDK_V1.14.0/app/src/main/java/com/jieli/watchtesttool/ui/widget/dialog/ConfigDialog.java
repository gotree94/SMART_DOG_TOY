package com.jieli.watchtesttool.ui.widget.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.jl_bt_ota.constant.BluetoothConstant;
import com.jieli.watchtesttool.BuildConfig;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.WatchApplication;
import com.jieli.watchtesttool.databinding.DialogConfigBinding;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothHelper;
import com.jieli.watchtesttool.tool.config.ConfigHelper;
import com.jieli.watchtesttool.ui.base.BaseDialogFragment;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 配置弹窗
 * @since 2022/5/9
 */
public class ConfigDialog extends BaseDialogFragment {
    private DialogConfigBinding mBinding;
    private final ConfigHelper mConfigHelper = ConfigHelper.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requireDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = requireDialog().getWindow();
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
        mBinding = DialogConfigBinding.bind(inflater.inflate(R.layout.dialog_config, container));
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setCancelable(true);
        initUI();
    }

    private void initUI() {
        mBinding.btnConfigCancel.setOnClickListener(v -> dismiss());
        mBinding.btnConfigSure.setOnClickListener(v -> saveConfig());
        mBinding.switchSppConnectWay.setOnCheckedChangeListener((buttonView, isChecked) -> updateBleMtu(isChecked));
        if (BuildConfig.DEBUG) {
            String logDir = WatchApplication.getWatchApplication().getLogFileDir();
            mBinding.tvLogFilePath.setText(String.format(Locale.getDefault(), "%s : %s", getString(R.string.log_save_path), logDir));
        }
        mBinding.switchTestFileTransfer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(!isChecked){
                mBinding.switchUseOtherEncode.setChecked(false);
            }
            mBinding.switchUseOtherEncode.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        mBinding.switchSppConnectWay.setChecked(mConfigHelper.isSPPConnectWay());
        mBinding.switchUseDeviceAuth.setChecked(mConfigHelper.isUseDeviceAuth());
        mBinding.switchBanAutoTest.setChecked(mConfigHelper.isBanAutoTest());
        mBinding.switchTestFileTransfer.setChecked(mConfigHelper.isTestFileTransfer());
        mBinding.switchTestFileBrowse.setChecked(mConfigHelper.isTestFileBrowse());
        mBinding.switchTestSmallFileTransfer.setChecked(mConfigHelper.isTestSmallFileTransfer());
        mBinding.switchTestWatchOp.setChecked(mConfigHelper.isTestWatchOp());
        mBinding.switchTestOta.setChecked(mConfigHelper.isTestOTA());
        mBinding.switchFilterDevice.setChecked(mConfigHelper.isFilterDevice());
        updateBleMtu(mConfigHelper.isSPPConnectWay());
        mBinding.switchTestMessageSync.setChecked(mConfigHelper.isTestMessageSync());
        mBinding.switchUseOtherEncode.setChecked(mConfigHelper.isUseOtherEncode());
        mBinding.switchTestRecord.setChecked(mConfigHelper.isTestRecord());
    }

    private void saveConfig() {
        boolean isChangeConfig = false;
        boolean isSppConnectWay = mConfigHelper.isSPPConnectWay();
        boolean isUseDeviceAuth = mConfigHelper.isUseDeviceAuth();
        boolean isBanAutoTest = mConfigHelper.isBanAutoTest();
        boolean isTestFileTransfer = mConfigHelper.isTestFileTransfer();
        boolean isTestFileBrowse = mConfigHelper.isTestFileBrowse();
        boolean isTestSmallFileTransfer = mConfigHelper.isTestSmallFileTransfer();
        boolean isTestWatchOp = mConfigHelper.isTestWatchOp();
        boolean isTestOta = mConfigHelper.isTestOTA();
        boolean isFilterDevice = mConfigHelper.isFilterDevice();
        int bleMtu = mConfigHelper.getBleMtu();
        boolean isTestMessageSync = mConfigHelper.isTestMessageSync();
        boolean isUseOtherEncode = mConfigHelper.isUseOtherEncode();
        boolean isTestRecord = mConfigHelper.isTestRecord();

        boolean newSppConnectWay = mBinding.switchSppConnectWay.isChecked();
        boolean newUseDeviceAuth = mBinding.switchUseDeviceAuth.isChecked();
        boolean newBanAutoTest = mBinding.switchBanAutoTest.isChecked();
        boolean newIsTestFileTransfer = mBinding.switchTestFileTransfer.isChecked();
        boolean newIsTestFileBrowse = mBinding.switchTestFileBrowse.isChecked();
        boolean newIsTestSmallFileTransfer = mBinding.switchTestSmallFileTransfer.isChecked();
        boolean newIsTestWatchOp = mBinding.switchTestWatchOp.isChecked();
        boolean newIsTestOta = mBinding.switchTestOta.isChecked();
        boolean newIsFilterDevice = mBinding.switchFilterDevice.isChecked();
        boolean newIsTestMessageSync = mBinding.switchTestMessageSync.isChecked();
        boolean newIsUseOtherEncode = mBinding.switchUseOtherEncode.isChecked();
        boolean newIsTestRecord = mBinding.switchTestRecord.isChecked();
        int newBleMtu = bleMtu;
        String bleMtuText = mBinding.etBleMtu.getText().toString();
        try {
            if (!TextUtils.isEmpty(bleMtuText) && TextUtils.isDigitsOnly(bleMtuText)) {
                int temp = Integer.parseInt(bleMtuText);
                if (temp >= BluetoothConstant.BLE_MTU_MIN && temp <= BluetoothConstant.BLE_MTU_MAX) {
                    newBleMtu = temp;
                } else {
                    mBinding.etBleMtu.setError(String.format(Locale.getDefault(), "%s: [%d, %d]", getString(R.string.input_value_err),
                            BluetoothConstant.BLE_MTU_MIN, BluetoothConstant.BLE_MTU_MAX));
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isSppConnectWay != newSppConnectWay) {
            mConfigHelper.setSppConnectWay(newSppConnectWay);
            BluetoothHelper.getInstance().getBluetoothOp().clearHistoryRecords();
            isChangeConfig = true;
        }
        if (isUseDeviceAuth != newUseDeviceAuth) {
            mConfigHelper.setUseDeviceAuth(newUseDeviceAuth);
            isChangeConfig = true;
        }
        if (isBanAutoTest != newBanAutoTest) {
            mConfigHelper.setBanAutoTest(newBanAutoTest);
            isChangeConfig = true;
        }
        if (isTestFileTransfer != newIsTestFileTransfer) {
            mConfigHelper.setTestFileTransfer(newIsTestFileTransfer);
            isChangeConfig = true;
        }
        if (isTestFileBrowse != newIsTestFileBrowse) {
            mConfigHelper.setTestFileBrowse(newIsTestFileBrowse);
            isChangeConfig = true;
        }
        if (isTestSmallFileTransfer != newIsTestSmallFileTransfer) {
            mConfigHelper.setTestSmallFileTransfer(newIsTestSmallFileTransfer);
            isChangeConfig = true;
        }
        if (isTestWatchOp != newIsTestWatchOp) {
            mConfigHelper.setTestWatchOp(newIsTestWatchOp);
            isChangeConfig = true;
        }
        if (isTestOta != newIsTestOta) {
            mConfigHelper.setTestOTA(newIsTestOta);
            isChangeConfig = true;
        }
        if (isFilterDevice != newIsFilterDevice) {
            mConfigHelper.setFilterDevice(newIsFilterDevice);
            isChangeConfig = true;
        }
        if (bleMtu != newBleMtu) {
            mConfigHelper.setBleMtu(newBleMtu);
            isChangeConfig = true;
        }
        if (isTestMessageSync != newIsTestMessageSync) {
            mConfigHelper.setTestMessageSync(newIsTestMessageSync);
            isChangeConfig = true;
        }
        if (isUseOtherEncode != newIsUseOtherEncode) {
            mConfigHelper.setFileNameEncode(newIsUseOtherEncode ? StandardCharsets.UTF_16LE.displayName() : "");
            isChangeConfig = true;
        }
        if (isTestRecord != newIsTestRecord) {
            mConfigHelper.setTestRecord(newIsTestRecord);
            isChangeConfig = true;
        }
        if (isChangeConfig) {
            Toast.makeText(requireContext().getApplicationContext(), getString(R.string.change_configuration_success_tips), Toast.LENGTH_LONG).show();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    System.exit(-1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 1500);
        } else {
            Toast.makeText(requireContext().getApplicationContext(), getString(R.string.configuration_not_change), Toast.LENGTH_LONG).show();
            dismiss();
        }
    }

    private void updateBleMtu(boolean isSPPConnectWay) {
        mBinding.groupAdjustBleMtu.setVisibility(isSPPConnectWay ? View.GONE : View.VISIBLE);
        if (!isSPPConnectWay) {
            mBinding.etBleMtu.setText(String.valueOf(mConfigHelper.getBleMtu()));
        }
    }
}
