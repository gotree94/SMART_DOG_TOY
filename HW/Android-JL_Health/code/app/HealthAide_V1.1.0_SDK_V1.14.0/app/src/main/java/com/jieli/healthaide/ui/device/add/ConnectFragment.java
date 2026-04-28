package com.jieli.healthaide.ui.device.add;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.bluetooth_connect.bean.ble.BleScanMessage;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentConnectBinding;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.util.JL_Log;

import org.jetbrains.annotations.NotNull;

/**
 * 连接界面
 */
public class ConnectFragment extends BaseFragment {
    public final static String KEY_CONNECT_DEV = "connect_dev";
    public final static String KEY_CONNECT_BLE_MSG = "connect_ble_msg";

    private FragmentConnectBinding mConnectBinding;
    private DeviceConnectViewModel mViewModel;
    private BluetoothDevice device;


    public static ConnectFragment newInstance() {
        return new ConnectFragment();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mConnectBinding = FragmentConnectBinding.inflate(inflater, container, false);
        return mConnectBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Bundle bundle = getArguments();
        device = null == bundle ? null : bundle.getParcelable(KEY_CONNECT_DEV);
        if (device == null) {
            finish();
            return;
        }
        BleScanMessage scanMessage = bundle.getParcelable(KEY_CONNECT_BLE_MSG);
        mViewModel = new ViewModelProvider(requireActivity()).get(DeviceConnectViewModel.class);

        initUI();
        addObserver();
        mViewModel.connectDevice(device, scanMessage);
    }

    private void initUI() {
        mConnectBinding.clConnectTopbar.tvTopbarLeft.setVisibility(View.VISIBLE);
        mConnectBinding.clConnectTopbar.tvTopbarLeft.setOnClickListener(v -> finish());
        mConnectBinding.clConnectTopbar.tvTopbarTitle.setText(R.string.connect_device);

        mConnectBinding.tvConnectDeviceMsg.setText(getString(R.string.cur_dev_msg, HealthUtil.getDeviceName(device)));
    }

    private void addObserver() {
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            JL_Log.i(tag, "mConnectionDataMLD", "device : " + device + ", " + deviceConnectionData);
            if (!mViewModel.isMatchDevice(device, deviceConnectionData.getDevice())) return;
            if (deviceConnectionData.getStatus() == StateCode.CONNECTION_DISCONNECT
                    && mViewModel.isSkipDevice(deviceConnectionData.getDevice()))
                return;
            updateStateUI(deviceConnectionData.getStatus());
        });
    }

    private void updateStateUI(int status) {
        if (!isFragmentValid()) return;
        JL_Log.d(tag, "updateStateUI", "status : " + status);
        if (status == StateCode.CONNECTION_CONNECTING) {
            mConnectBinding.tvConnectStatus.setText(R.string.dev_connecting);
            mConnectBinding.tvConnectDesc.setText(R.string.dev_connecting_desc);
            mConnectBinding.ivConnectImg.setImageResource(R.drawable.ic_device_pairing);
        } else {
            switch (status) {
                case StateCode.CONNECTION_OK:
                    mConnectBinding.tvConnectStatus.setText(R.string.dev_connect_success);
                    mConnectBinding.tvConnectDesc.setText("");
                    mConnectBinding.ivConnectImg.setImageResource(R.drawable.ic_device_connect_success);
                    finish(2000, null);
                    break;
                case StateCode.CONNECTION_DISCONNECT:
                case StateCode.CONNECTION_FAILED:
                    mConnectBinding.tvConnectStatus.setText(R.string.dev_connect_fail);
                    mConnectBinding.tvConnectDesc.setText(R.string.dev_connect_fail_desc);
                    mConnectBinding.ivConnectImg.setImageResource(R.drawable.ic_device_connect_fail);
                    break;
            }
        }
    }
}