package com.jieli.healthaide.ui.test.func;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.bluetooth_connect.util.ConnectUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentCustomCmdTestBinding;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.util.UIHelper;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.jl_rcsp.util.RcspUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 自定义命令测试界面
 * @since 2026/01/12
 */
public class CustomCmdTestFragment extends BaseFragment {

    @SuppressLint("ConstantLocale")
    private static final SimpleDateFormat yyyyMMdd_HHmmssSSS = new SimpleDateFormat(
            "yyyy/MM/dd HH:mm:ss.SSS", Locale.ENGLISH);

    private FragmentCustomCmdTestBinding binding;
    private CustomCmdTestViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCustomCmdTestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CustomCmdTestViewModel.class);
        initUI();
        addObserver();
        if (!viewModel.isDeviceConnected()) {
            finish();
        }
    }

    private void initUI() {
        binding.viewTopBar.tvTopbarTitle.setText(getString(R.string.test_custom_cmd));
        binding.viewTopBar.tvTopbarLeft.setOnClickListener(v -> finish());
        binding.tvLogContainer.setMovementMethod(ScrollingMovementMethod.getInstance());
        binding.btnClearLog.setOnClickListener(v -> addLog("", false));
        binding.btnSendCustomCmd.setOnClickListener(v -> tryToSendCustomCmd());
    }

    private void addObserver() {
        viewModel.deviceConnectionMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            if (deviceConnectionData.getStatus() != StateCode.CONNECTION_OK) {
                addLog(getString(R.string.device_is_disconnected));
                finish();
            }
        });
        viewModel.customCmdMLD.observe(getViewLifecycleOwner(), opResult -> {
            if (!opResult.isOk()) {
                addLog(ConnectUtil.formatString(
                        "%s.\n%s: %d(0x%X), %s.",
                        getString(R.string.operation_failed, getString(R.string.send_custom_cmd)),
                        getString(R.string.error_code),
                        opResult.getCode(), opResult.getCode(),
                        opResult.getMessage()));
                return;
            }
            String operation = opResult.getOp() == CustomCmdTestViewModel.OP_RECEIVER_CUSTOM_CMD ?
                    "Receive Custom Command" : getString(R.string.send_custom_cmd);
            String dataText = CHexConver.byte2HexStr(opResult.getResult());
            String content = RcspUtil.formatString("%s Successfully.\nCustom Data : [%s]", operation, dataText);
            addLog(content);
        });
    }

    private String getTimeFormat() {
        return yyyyMMdd_HHmmssSSS.format(Calendar.getInstance().getTime());
    }

    private void addLog(String text) {
        addLog(text, true);
    }

    private void addLog(String text, boolean isAppend) {
        if (!isFragmentValid() || null == text) return;
        JL_Log.d(tag, "addLog", "isAppend : " + isAppend + ", " + text);
        final TextView textView = binding.tvLogContainer;
        if (!isAppend) {
            textView.setText(text);
            textView.scrollTo(0, 0);
        } else {
            String content = RcspUtil.formatString("%s\t%s", getTimeFormat(), text);
            textView.append(content);
            textView.append("\n");
            int offset = UIHelper.getTextViewHeight(textView);
            if (offset > textView.getHeight()) {
                textView.scrollTo(0, offset - textView.getHeight());
            }
        }
    }

    private void tryToSendCustomCmd() {
        String hexText = binding.etCustomData.getText().toString().trim();
        final byte[] customData = CHexConver.hexStr2Bytes(hexText);
        if (null == customData || customData.length == 0) {
            showTips(getString(R.string.hint_input_hex_data));
            return;
        }
        boolean isNeedReply = binding.swNeedReply.isChecked();
        addLog("isNeedReply : " + isNeedReply + ", Send Custom Data : " + CHexConver.byte2HexStr(customData));
        viewModel.sendCustomCmd(customData, isNeedReply);
    }
}