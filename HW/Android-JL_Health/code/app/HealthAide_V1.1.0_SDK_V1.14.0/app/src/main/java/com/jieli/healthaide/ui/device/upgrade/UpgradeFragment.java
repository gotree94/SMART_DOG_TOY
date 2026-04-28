package com.jieli.healthaide.ui.device.upgrade;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentUpgradeBinding;
import com.jieli.healthaide.ui.base.BaseActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.widget.ResultDialog;
import com.jieli.healthaide.ui.widget.upgrade_dialog.UpdateResourceDialog;
import com.jieli.healthaide.ui.widget.upgrade_dialog.UpgradeDescDialog;
import com.jieli.healthaide.ui.widget.upgrade_dialog.UpgradeProgressDialog;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_bt_ota.constant.ErrorCode;
import com.jieli.jl_bt_ota.model.ErrorMsg;
import com.jieli.jl_dialog.Jl_Dialog;
import com.jieli.jl_health_http.model.OtaFileMsg;
import com.jieli.jl_rcsp.constant.WatchConstant;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.util.JL_Log;


public class UpgradeFragment extends BaseFragment {

    private FragmentUpgradeBinding mUpgradeBinding;
    private UpgradeViewModel mViewModel;
    private BaseActivity mActivity;

    private UpgradeDescDialog mUpgradeDescDialog;
    private UpdateResourceDialog mUpdateResourceDialog;
    private UpgradeProgressDialog mUpgradeProgressDialog;
    private ResultDialog mResultDialog;
    //    private WaitingDialog mWaitingDialog;
    private Jl_Dialog mWarningTipsDialog;

    private String otaFilePath;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public final static String KEY_OTA_FLAG = "ota_flag";
    public final static String KEY_OTA_FILE_PATH = "ota_file_path";

    public final static int OTA_FLAG_NORMAL = 0;
    public final static int OTA_FLAG_FIRMWARE = 1;
    public final static int OTA_FLAG_RESOURCE = 2;

    public final static int OTA_FLAG_NETWORK = 3;

    public static UpgradeFragment newInstance() {
        return new UpgradeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mUpgradeBinding = FragmentUpgradeBinding.inflate(inflater, container, false);
        return mUpgradeBinding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity) {
            mActivity = (BaseActivity) context;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mActivity == null && requireActivity() instanceof BaseActivity) {
            mActivity = (BaseActivity) requireActivity();
        }
        mUpgradeBinding.clUpgradeTopbar.tvTopbarTitle.setText(R.string.firmware_upgrade);
        mUpgradeBinding.clUpgradeTopbar.tvTopbarLeft.setOnClickListener(v -> {
            if (mActivity != null) mActivity.onBackPressed();
        });
        mUpgradeBinding.tvUpgradeBtn.setOnClickListener(v -> {
            final OtaState otaState = mViewModel.getOtaState();
            if (otaState.getState() != OtaState.OTA_STATE_PREPARE) {
                mViewModel.otaPrepare();
            } else {
                showUpgradeDescDialog(otaState.getMessage());
            }
        });

        mViewModel = new ViewModelProvider(this).get(UpgradeViewModel.class);
        if (getArguments() != null) {
            mViewModel.setOtaFlag(getArguments().getInt(KEY_OTA_FLAG, OTA_FLAG_NORMAL)); //读取标志位
            final int otaFlag = mViewModel.getOtaFlag();
            mViewModel.setSkip4gOta(otaFlag != OTA_FLAG_NORMAL && otaFlag != OTA_FLAG_NETWORK);
            otaFilePath = getArguments().getString(KEY_OTA_FILE_PATH, null);
        }
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED && !mViewModel.isDevOta()
                    && mViewModel.getOtaState().getState() < OtaState.OTA_STATE_UPGRADE) {
                requireActivity().finish();
            }
        });
        mViewModel.mOtaStateMLD.observe(getViewLifecycleOwner(), otaState -> mHandler.post(() -> updateOtaState(otaState)));
        mViewModel.mOtaInitMLD.observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                int otaFlag = mViewModel.getOtaFlag();
                JL_Log.e(tag, "OtaInitMLD", "mOtaFlag : " + otaFlag + ", otaFilePath = " + otaFilePath);
                if (otaFlag != OTA_FLAG_NORMAL) {
//                    mViewModel.setOtaFlag(OTA_FLAG_NORMAL);
                    mViewModel.otaPrepare();
                    /*if (!FileUtil.checkFileExist(otaFilePath)) {
                        JL_Log.w(tag, "file not exist, enter otaPrepare");
                        mViewModel.otaPrepare();
                    } else {
                        switch (otaFlag) {
                            case OTA_FLAG_FIRMWARE:
                                mViewModel.otaFirmware(otaFilePath);
                                break;
                            case OTA_FLAG_RESOURCE:
                                mViewModel.otaResource(otaFilePath);
                                break;
                        }
                    }*/
                }
            }
        });

        if (mActivity != null) {
            mActivity.setOnBackPressIntercept(() -> {
                DeviceInfo deviceInfo = mViewModel.getDeviceInfo();
                if (deviceInfo != null && (deviceInfo.isMandatoryUpgrade()
                        || deviceInfo.getExpandMode() == WatchConstant.EXPAND_MODE_RES_OTA
                        || deviceInfo.getExpandMode() == WatchConstant.EXPAND_MODE_ONLY_UPDATE_RESOURCE)) {
                    int otaFlag = deviceInfo.isMandatoryUpgrade() ? OTA_FLAG_FIRMWARE : OTA_FLAG_RESOURCE;
                    showExitMandatoryUpgradeDialog(otaFlag);
                    return true;
                }
                return false;
            });
        }
        DeviceInfo deviceInfo = mViewModel.getDeviceInfo();
        if (null == deviceInfo) {
            if (mActivity != null) {
                mActivity.onBackPressed();
            }
            return;
        }
        mUpgradeBinding.tvUpgradeDevVersion.setText(deviceInfo.getVersionName());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        dismissWaitingDialog();
        dismissResultDialog();
        dismissUpgradeDescDialog();
        dismissUpdateResourceDialog();
        dismissUpgradeProgressDialog();
        mViewModel.release();
        mUpgradeBinding = null;
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    private void updateOtaState(OtaState otaState) {
        if (null == otaState || !isFragmentValid()) return;
//        JL_Log.i("UpgradeViewModel", "-updateOtaState- otaState >>>>>> " + otaState);
        switch (otaState.getState()) {
            case OtaState.OTA_STATE_IDLE:
                if (otaState.getStopResult() == OtaState.OTA_RES_SUCCESS) {
                    showTips(R.string.latest_version);
                }
                mUpgradeBinding.tvUpgradeBtn.setEnabled(true);
                mUpgradeBinding.tvUpgradeBtn.setText(R.string.check_update);
                mUpgradeBinding.tvUpgradeBtn.setTextColor(getResources().getColorStateList(R.color.text_white_2_purple_selector));
                mUpgradeBinding.tvUpgradeBtn.setBackgroundResource(R.drawable.bg_purple_2_gray_selector);
                break;
            case OtaState.OTA_STATE_PREPARE:
                mUpgradeBinding.tvUpgradeBtn.setEnabled(true);
                mUpgradeBinding.tvUpgradeBtn.setText(R.string.upgrade);
                mUpgradeBinding.tvUpgradeBtn.setTextColor(getResources().getColorStateList(R.color.text_white_2_purple_selector));
                mUpgradeBinding.tvUpgradeBtn.setBackgroundResource(R.drawable.bg_purple_2_gray_selector);
                showUpgradeDescDialog(otaState.getMessage());
                break;
            case OtaState.OTA_STATE_DOWNLOAD:
                showUpgradeProgressDialog(getString(R.string.ota_state_ready), Math.round(otaState.getOtaProgress()));
                mUpgradeBinding.tvUpgradeBtn.setEnabled(false);
                mUpgradeBinding.tvUpgradeBtn.setText(R.string.ota_state_ready);
                mUpgradeBinding.tvUpgradeBtn.setTextColor(getResources().getColor(R.color.text_secondary_disable_color));
                mUpgradeBinding.tvUpgradeBtn.setBackgroundColor(getResources().getColor(R.color.text_transparent));
                break;
            case OtaState.OTA_STATE_UPGRADE:
                dismissUpgradeDescDialog();
                showUpgradeProgressDialog(getString(R.string.ota_state_ready), 99);
                mUpgradeBinding.tvUpgradeBtn.setEnabled(false);
                mUpgradeBinding.tvUpgradeBtn.setText(R.string.ota_state_ready);
                mUpgradeBinding.tvUpgradeBtn.setTextColor(getResources().getColor(R.color.text_secondary_disable_color));
                mUpgradeBinding.tvUpgradeBtn.setBackgroundColor(getResources().getColor(R.color.text_transparent));
                break;
            case OtaState.OTA_STATE_START:
                dismissUpgradeDescDialog();
                if (requireActivity().getWindow() != null) {
                    requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
//                dismissWaitingDialog();
                mUpgradeBinding.tvUpgradeBtn.setText(R.string.ota_state_check_file);
                mUpgradeBinding.tvUpgradeBtn.setTextColor(getResources().getColor(R.color.text_secondary_disable_color));
                String txt = getString(R.string.ota_state_check_file);
                if (otaState.getOtaType() == OtaState.OTA_TYPE_OTA_UPDATE_RESOURCE) {
                    txt = getString(R.string.update_resource_tips, otaState.getOtaIndex(), otaState.getOtaTotal());
                    dismissUpgradeProgressDialog();
                    showUpdateResourceDialog(txt, otaState.getOtaFileInfo(), 0);
                } else {
                    showUpgradeProgressDialog(txt, 0);
                }
                break;
            case OtaState.OTA_STATE_WORKING:
                mUpgradeBinding.tvUpgradeBtn.setText(R.string.ota_state_updating);
                mUpgradeBinding.tvUpgradeBtn.setTextColor(getResources().getColor(R.color.text_secondary_disable_color));
                String text;
                if (otaState.getOtaType() == OtaState.OTA_TYPE_OTA_UPDATE_RESOURCE) {
                    text = getString(R.string.update_resource_tips, otaState.getOtaIndex(), otaState.getOtaTotal());
                    showUpdateResourceDialog(text, otaState.getOtaFileInfo(), Math.round(otaState.getOtaProgress()));
                } else {
                    text = otaState.getOtaType() == OtaState.OTA_TYPE_OTA_READY ? getString(R.string.ota_state_check_file) : getString(R.string.ota_state_updating);
                    showUpgradeProgressDialog(text, Math.round(otaState.getOtaProgress()));
                }
                break;
            case OtaState.OTA_STATE_STOP:
                mUpgradeBinding.tvUpgradeBtn.setText(R.string.ota_state_finish);
                mUpgradeBinding.tvUpgradeBtn.setTextColor(getResources().getColor(R.color.text_secondary_disable_color));
                dismissUpdateResourceDialog();
                dismissUpgradeProgressDialog();
                int resId = R.drawable.ic_fail_yellow;
                String message = getString(R.string.ota_result_failed);
                switch (otaState.getStopResult()) {
                    case OtaState.OTA_RES_SUCCESS:
                        resId = R.drawable.ic_success_green;
                        message = getString(R.string.ota_result_success);
                        break;
                    case OtaState.OTA_RES_CANCEL:
                        message = getString(R.string.ota_result_cancel);
                        break;
                    case OtaState.OTA_RES_FAILED:
                        if (otaState.getError() != null && otaState.getError().getMessage() != null) {
                            int errorCode = otaState.getError().getSubCode();
                            String errMsg = otaState.getError().getMessage();
                            message = HealthUtil.getOTAErrDesc(requireContext(), otaState.getError().getSubCode());
                            switch (errorCode) {
                                case ErrorCode.SUB_ERR_DEVICE_LOW_VOLTAGE:
                                    int lowPowerLimit = 30;
                                    if (mViewModel.getDeviceInfo() != null) {
                                        int limit = mViewModel.getDeviceInfo().getLowPowerLimit();
                                        if (limit != lowPowerLimit) lowPowerLimit = limit;
                                    }
                                    message = getString(R.string.ota_err_low_power, CalendarUtil.formatString("%d%%", lowPowerLimit));
                                    break;
                                case ErrorCode.SUB_ERR_RESPONSE_BAD_STATUS:
                                case ErrorCode.SUB_ERR_RESPONSE_BAD_RESULT:
                                case ErrorCode.SUB_ERR_UPGRADE_UNKNOWN:
                                    int subCode = getErrorSubCode(errMsg);
                                    if (subCode != -255) {
                                        message = CalendarUtil.formatString("%s:%d", message, subCode);
                                    }
                                    break;
                            }
                            if (TextUtils.isEmpty(message)) {
                                message = otaState.getError().getMessage();
                            }
                        }
                        break;
                }
                if (requireActivity().getWindow() != null) {
                    requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
                int resultCode = otaState.getStopResult();
                int errorCode = 0;
                if (resultCode == OtaState.OTA_RES_FAILED && otaState.getError() != null) {
                    errorCode = otaState.getError().getSubCode();
                }
                mViewModel.setOtaFlag(OTA_FLAG_NORMAL);
                final int otaType = otaState.getOtaType();
                if (resultCode == OtaState.OTA_RES_SUCCESS) { //升级成功
                    //因为V2.2.0版本的手表，重启会关闭EDR，所以需要APP主动回连设备
                    //发送广播回连设备
                    requireActivity().sendBroadcast(new Intent(HealthConstant.ACTION_RECONNECT_DEVICE));
                    if (mViewModel.isConnected() && (otaType == OtaState.OTA_TYPE_OTA_UPDATE_RESOURCE)) {
                        Intent intent = new Intent(HealthConstant.ACTION_UPDATE_RESOURCE_SUCCESS);
                        intent.putExtra(HealthConstant.KEY_DEVICE_ADDRESS, mViewModel.getConnectedDevice().getAddress());
                        requireActivity().sendBroadcast(intent);
                    }
                }
                showResultDialog(resultCode, resId, message, errorCode, otaType);
                break;
        }
    }

    private void showUpgradeDescDialog(OtaFileMsg otaFileMsg) {
        if (!isFragmentValid() || null == otaFileMsg) return;
        String title = getString(R.string.new_ota_version, otaFileMsg.getVersion());
        String content = otaFileMsg.getContent();
        if (TextUtils.isEmpty(content)) {
            content = otaFileMsg.getExplain();
        }
        if (mUpgradeDescDialog == null) {
            mUpgradeDescDialog = new UpgradeDescDialog.Builder()
                    .setTitle(title)
                    .setContent(content)
                    .setLeftText(getString(R.string.cancel))
                    .setRightText(getString(R.string.upgrade))
                    .create();
            mUpgradeDescDialog.setOnUpgradeDescListener(new UpgradeDescDialog.OnUpgradeDescListener() {
                @Override
                public void onLeftClick() {
                    dismissUpgradeDescDialog();
                }

                @Override
                public void onRightClick() {
                    dismissUpgradeDescDialog();
                    mUpgradeBinding.tvUpgradeBtn.setEnabled(false);
                    mUpgradeBinding.tvUpgradeBtn.setText(R.string.ota_state_ready);
                    mUpgradeBinding.tvUpgradeBtn.setTextColor(getResources().getColor(R.color.text_secondary_disable_color));
                    mUpgradeBinding.tvUpgradeBtn.setBackgroundColor(getResources().getColor(R.color.text_transparent));
                    mViewModel.otaPrepare();
                    showUpgradeProgressDialog(getString(R.string.ota_state_ready), 0);
//                    showWaitingDialog();
                }
            });
        } else {
            mUpgradeDescDialog.updateView(mUpgradeDescDialog.getBuilder()
                    .setTitle(title)
                    .setContent(content));
        }
        if (!mUpgradeDescDialog.isShow()) {
            mUpgradeDescDialog.show(getChildFragmentManager(), UpgradeDescDialog.class.getSimpleName());
        }
    }

    private void dismissUpgradeDescDialog() {
        if (!isFragmentValid()) return;
        if (mUpgradeDescDialog != null) {
            if (mUpgradeDescDialog.isShow()) {
                mUpgradeDescDialog.dismiss();
            }
            mUpgradeDescDialog = null;
        }
    }

    private void showUpgradeProgressDialog(String progressText, int progress) {
        if (!isFragmentValid()) return;
        if (mUpgradeProgressDialog == null) {
            mUpgradeProgressDialog = new UpgradeProgressDialog.Builder()
                    .setWidth(1f)
                    .setProgressText(progressText)
                    .setProgress(progress)
                    .setTips(getString(R.string.upgrade_warning))
                    .create();
        }
        mUpgradeProgressDialog.updateView(mUpgradeProgressDialog.getBuilder()
                .setProgressText(progressText)
                .setProgress(progress));
        dismissExitMandatoryUpgradeDialog();
        if (!mUpgradeProgressDialog.isShow()) {
            mUpgradeProgressDialog.show(getChildFragmentManager(), UpgradeProgressDialog.class.getSimpleName());
        }
    }

    private void dismissUpgradeProgressDialog() {
        if (!isFragmentValid()) return;
        if (mUpgradeProgressDialog != null) {
            if (mUpgradeProgressDialog.isShow()) {
                mUpgradeProgressDialog.dismiss();
            }
            mUpgradeProgressDialog = null;
        }
    }

    private void showResultDialog(int resultCode, int resId, String text, final int errorCode, int otaType) {
        if (!isFragmentValid()) return;
        if (mResultDialog == null) {
            mResultDialog = new ResultDialog.Builder()
                    .setCancel(false)
                    .setResultCode(resultCode)
                    .setImgId(resId)
                    .setResult(text)
                    .setBtnText(getString(R.string.sure))
                    .create();
            mResultDialog.setOnResultListener(code -> {
                dismissResultDialog();
                //因为固件升级完4G模块会重启设备，所以统一处理升级结果
                /*if (otaType != OtaState.OTA_TYPE_NETWORK_MODULE)*/
                {
                    //判断是否固件升级错误，如果是，断开设备
                    if (code != OtaState.OTA_RES_SUCCESS && errorCode > 0 && mViewModel.checkNeedDisconnect(errorCode)) {
                        mViewModel.disconnectDevice(mViewModel.getConnectedDevice());
                    }
                    requireActivity().finish();
                }/* else {
                    mViewModel.otaPrepare();
                }*/
            });
        }
        if (!mResultDialog.isShow()) {
            mResultDialog.show(getChildFragmentManager(), ResultDialog.class.getSimpleName());
        }
    }

    private void dismissResultDialog() {
        if (!isFragmentValid()) return;
        if (mResultDialog != null) {
            if (mResultDialog.isShow()) {
                mResultDialog.dismiss();
            }
            mResultDialog = null;
        }
    }

    private void showUpdateResourceDialog(String title, String name, int progress) {
        if (!isFragmentValid()) return;
        if (mUpdateResourceDialog == null) {
            mUpdateResourceDialog = new UpdateResourceDialog.Builder()
                    .setTitle(title)
                    .setName(name)
                    .setProgress(progress)
                    .create();
        } else {
            mUpdateResourceDialog.updateView(mUpdateResourceDialog.getBuilder().setTitle(title)
                    .setName(name)
                    .setProgress(progress));
        }
        dismissExitMandatoryUpgradeDialog();
        if (!mUpdateResourceDialog.isShow()) {
            mUpdateResourceDialog.show(getChildFragmentManager(), UpdateResourceDialog.class.getSimpleName());
        }
    }

    private void dismissUpdateResourceDialog() {
        if (!isFragmentValid()) return;
        if (mUpdateResourceDialog != null) {
            if (mUpdateResourceDialog.isShow()) {
                mUpdateResourceDialog.dismiss();
            }
            mUpdateResourceDialog = null;
        }
    }

    private void showExitMandatoryUpgradeDialog(int otaType) {
        if (!isFragmentValid()) return;
        String content = otaType == OTA_FLAG_FIRMWARE ? getString(R.string.firmware_mandatory_upgrade) : getString(R.string.resource_unfinished_tips);
        mWarningTipsDialog = Jl_Dialog.builder()
                .title(getString(R.string.upgrade_warning_tips))
                .content(content)
                .cancel(false)
                .left(getString(R.string.exit_and_disconnect))
                .leftColor(getResources().getColor(R.color.gray_B3B3B3))
                .leftClickListener((view, dialogFragment) -> {
                    dialogFragment.dismiss();
                    mViewModel.disconnectDevice(mViewModel.getConnectedDevice());
                    mWarningTipsDialog = null;
                    requireActivity().finish();
                })
                .right(getString(R.string.continue_upgrade))
                .rightColor(getResources().getColor(R.color.red_D25454))
                .rightClickListener(((view, dialogFragment) -> {
                    dialogFragment.dismiss();
                    mWarningTipsDialog = null;
                }))
                .build();
        mWarningTipsDialog.show(getChildFragmentManager(), "tips_dialog");
    }

    private void dismissExitMandatoryUpgradeDialog() {
        if (!isFragmentValid()) return;
        if (mWarningTipsDialog != null) {
            if (mWarningTipsDialog.isShow()) {
                mWarningTipsDialog.dismiss();
            }
            mWarningTipsDialog = null;
        }
    }

    private int getErrorSubCode(String msg) {
        if (TextUtils.isEmpty(msg)) return -255;
        int subCode = -255;
        ErrorMsg errorMsg = ErrorMsg.parseJson(msg);
        if (errorMsg != null) {
            subCode = errorMsg.getSubCode();
            if (subCode == -1) {
                String message = errorMsg.getSubMessage();
                if (!TextUtils.isEmpty(message)) {
                    message = message.replaceAll(" ", "");
                    int index = message.indexOf("=");
                    if (index != -1) {
                        message = message.substring(index + 1);
                        JL_Log.w(tag, "getErrorSubCode", message);
                        if (!TextUtils.isEmpty(message) && TextUtils.isDigitsOnly(message)) {
                            subCode = Integer.parseInt(message);
                        }
                    }
                }
            }
        }
        if (subCode == -255 && !TextUtils.isEmpty(msg) && TextUtils.isDigitsOnly(msg)) {
            try {
                subCode = Integer.parseInt(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return subCode;
    }

    /*private void showWaitingDialog() {
        if (isDetached() || !isAdded()) return;
        if (mWaitingDialog == null) {
            mWaitingDialog = new WaitingDialog();
        }
        if (!mWaitingDialog.isShow()) {
            mWaitingDialog.show(getChildFragmentManager(), WaitingDialog.class.getSimpleName());
        }
    }

    private void dismissWaitingDialog() {
        if (isDetached() || !isAdded()) return;
        if (mWaitingDialog != null) {
            if (mWaitingDialog.isShow()) {
                mWaitingDialog.dismiss();
            }
            mWaitingDialog = null;
        }
    }*/
}