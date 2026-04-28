package com.jieli.healthaide.ui.device;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.util.BluetoothUtil;
import com.jieli.bluetooth_connect.util.ConnectUtil;
import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentDeviceBinding;
import com.jieli.healthaide.tool.notification.NotificationHelper;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.adapter.DeviceHistoryAdapter;
import com.jieli.healthaide.ui.device.adapter.WatchAdapter;
import com.jieli.healthaide.ui.device.adapter.WatchFuncAdapter;
import com.jieli.healthaide.ui.device.add.AddDeviceFragment;
import com.jieli.healthaide.ui.device.aicloud.AICloudHistoryMessageFragment;
import com.jieli.healthaide.ui.device.aidial.AIDialStyleFragment;
import com.jieli.healthaide.ui.device.alarm.AlarmListFragment;
import com.jieli.healthaide.ui.device.bean.DeviceConnectionData;
import com.jieli.healthaide.ui.device.bean.DeviceHistoryRecord;
import com.jieli.healthaide.ui.device.bean.FuncItem;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.healthaide.ui.device.bean.WatchOpData;
import com.jieli.healthaide.ui.device.contact.ContactFragment;
import com.jieli.healthaide.ui.device.file.DeviceFileContainerFragment;
import com.jieli.healthaide.ui.device.file.FilesFragment;
import com.jieli.healthaide.ui.device.health.HealthOptionFragment;
import com.jieli.healthaide.ui.device.history.HistoryRecordFragment;
import com.jieli.healthaide.ui.device.market.WatchDialFragment;
import com.jieli.healthaide.ui.device.market.WatchMarketFragment;
import com.jieli.healthaide.ui.device.more.MoreFragment;
import com.jieli.healthaide.ui.device.nfc.NFCActivity;
import com.jieli.healthaide.ui.device.nfc.fragment.NfcMsgFragment;
import com.jieli.healthaide.ui.device.upgrade.UpgradeFragment;
import com.jieli.healthaide.ui.device.watch.CustomWatchBgFragment;
import com.jieli.healthaide.ui.device.watch.WatchViewModel;
import com.jieli.healthaide.ui.dialog.RequireGPSDialog;
import com.jieli.healthaide.ui.widget.CommonDecoration;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.healthaide.util.PermissionUtil;
import com.jieli.jl_dialog.Jl_Dialog;
import com.jieli.jl_fatfs.FatFsErrCode;
import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.jl_filebrowse.util.DeviceChoseUtil;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.model.device.BatteryInfo;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.model.response.TargetInfoResponse;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * 设备管理界面
 */
@RuntimePermissions
public class DeviceFragment extends BaseFragment {
    private FragmentDeviceBinding mDeviceBinding;

    private DeviceHistoryAdapter mHistoryAdapter;
    private WatchAdapter mWatchAdapter;
    private WatchFuncAdapter mWatchFuncAdapter;

    private WatchViewModel mWatchViewModel;

    private StateBroadcastReceiver receiver;


    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<Intent> refreshLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    JL_Log.d(tag, "refreshLauncher", "result code : " + result.getResultCode());
                    mWatchViewModel.listWatchList();
                }
            });

    public static DeviceFragment newInstance() {
        return new DeviceFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDeviceBinding = FragmentDeviceBinding.inflate(inflater, container, false);
        return mDeviceBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWatchViewModel = new ViewModelProvider(this).get(WatchViewModel.class);
        initView();
        observeCallback();
        registerStateReceiver();

        updateWatchUI(mWatchViewModel.isConnected(), mWatchViewModel.getConnectedDevice());
        mWatchViewModel.syncHistoryRecordList();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DeviceFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeObserver();
        unregisterStateReceiver();
        mWatchViewModel.release();
        mDeviceBinding = null;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        //init history list
        mHistoryAdapter = new DeviceHistoryAdapter();
        View emptyView = LayoutInflater.from(requireActivity()).inflate(R.layout.view_none_device, null);
        TextView tvAddDevice = emptyView.findViewById(R.id.tv_none_device_add_device);
        tvAddDevice.setOnClickListener(v -> ContentActivity.startContentActivity(requireActivity(), AddDeviceFragment.class.getCanonicalName()));
        mHistoryAdapter.setEmptyView(emptyView);
        mHistoryAdapter.setOnItemClickListener((adapter, view, position) -> {
            DeviceHistoryRecord historyRecord = mHistoryAdapter.getItem(position);
            if (null == historyRecord) return;
            if (historyRecord.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTING) { //设备未连接 或者 设备已连接
                Bundle bundle = new Bundle();
                bundle.putParcelable(HistoryRecordFragment.KEY_HISTORY_RECORD, historyRecord);
                ContentActivity.startContentActivity(requireContext(), HistoryRecordFragment.class.getCanonicalName(), bundle);
            }
        });
        mHistoryAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            DeviceHistoryRecord historyRecord = mHistoryAdapter.getItem(position);
            if (null == historyRecord) return;
            if (historyRecord.getStatus() == BluetoothConstant.CONNECT_STATE_DISCONNECT) { //回连设备
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !ConnectUtil.isHasConnectPermission(requireContext())) {
                    showPermissionDialog(Manifest.permission.BLUETOOTH_CONNECT, ((permission) ->
                            DeviceFragmentPermissionsDispatcher.requestBtPermissionWithPermissionCheck(this, historyRecord)));
                    return;
                }
                reconnectHistory(historyRecord);
            }
        });
        mHistoryAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                List<DeviceHistoryRecord> data = mHistoryAdapter.getData();
                if (data.isEmpty()) {
                    int padding16dp = ValueUtil.dp2px(requireContext(), 16);
                    updateDevicesListViewPager2Margin(padding16dp, padding16dp);
                } else if (data.size() == 1) {
                    int padding8dp = ValueUtil.dp2px(requireContext(), 8);
                    updateDevicesListViewPager2Margin(padding8dp, padding8dp);
                } else {
                    if (mDeviceBinding.rvDeviceList.getCurrentItem() == 0) {
                        int padding8dp = ValueUtil.dp2px(requireContext(), 8);
                        int padding26dp = ValueUtil.dp2px(requireContext(), 26);
                        updateDevicesListViewPager2Margin(padding8dp, padding26dp);
                    }
                }
            }
        });
        mDeviceBinding.rvDeviceList.setOffscreenPageLimit(3);
        mDeviceBinding.rvDeviceList.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            float lastOffset = 0;
            float lastPosition = 0;

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDeviceScrollUI(mHistoryAdapter.getData().size(), position);
                lastOffset = 0;
                lastPosition = 0;
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (lastPosition != position) {
                    lastOffset = 0;
                    lastPosition = position;
                }
                if (Math.abs(lastOffset - positionOffset) < 0.1f) {
                    return;
                }
                lastOffset = positionOffset;
                boolean isScrollToFirst = false;
                boolean isScrollToLast = false;
                int finallyPosition = mHistoryAdapter.getData().size() - 1;
                if (position == 0 && positionOffset < 0.45f) {
                    isScrollToFirst = true;
                } else if (position == finallyPosition || (position == finallyPosition - 1 && (positionOffset > 0.55f))) {
                    isScrollToLast = true;
                }
                if (isScrollToFirst) {
                    updateViewPager2(0);
                } else if (isScrollToLast) {
                    updateViewPager2(finallyPosition);
                } else {
                    updateViewPager2(1);
                }
            }
        });
        mDeviceBinding.rvDeviceList.setAdapter(mHistoryAdapter);

        //init local watch
        mDeviceBinding.tvDeviceWatchMarketAll.setOnClickListener(v -> {
            boolean isSupportDialPayment = mWatchViewModel.isSupportDialPayment();
            JL_Log.d(tag, "tvDeviceWatchMarketAll", "isSupportDialPayment : " + isSupportDialPayment);
            String fragmentCanonicalName = isSupportDialPayment ? WatchDialFragment.class.getCanonicalName() : WatchMarketFragment.class.getCanonicalName();
            ContentActivity.startContentActivityForResult(this, fragmentCanonicalName, null, refreshLauncher);
        });
        mDeviceBinding.rvDeviceWatchList.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false));
        mWatchAdapter = new WatchAdapter();
        mWatchAdapter.setBanUpdate(true);
        mWatchAdapter.setGoneDeleteIcon(true);
        mDeviceBinding.rvDeviceWatchList.addItemDecoration(new CommonDecoration(getContext(), RecyclerView.HORIZONTAL, getResources().getColor(R.color.half_transparent), ValueUtil.dp2px(getContext(), 12)));
        mWatchAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            WatchInfo item = mWatchAdapter.getItem(position);
            if (null == item || null == item.getWatchFile()) return;
            if (view.getId() == R.id.tv_item_watch_btn) {
                if (item.getStatus() == WatchInfo.WATCH_STATUS_EXIST) {
                    mWatchViewModel.enableCurrentWatch(item.getWatchFile().getPath());
                }
            } else if (view.getId() == R.id.iv_item_watch_delete) {
                if (mWatchAdapter.getData().size() > 2) {
                    mWatchViewModel.deleteWatch(item);
                } else {
                    showTips(R.string.delete_watch_tip);
                }
            } else if (view.getId() == R.id.tv_item_watch_edit) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(HealthConstant.EXTRA_WATCH_INFO, item);
                ContentActivity.startContentActivityForResult(DeviceFragment.this, CustomWatchBgFragment.class.getCanonicalName(), bundle, refreshLauncher);
            }
        });
        mWatchAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (mWatchAdapter != null && mWatchAdapter.isEditMode()) {
                mWatchAdapter.setEditMode(false);
            }
        });
        mDeviceBinding.rvDeviceWatchList.setAdapter(mWatchAdapter);
        mDeviceBinding.clDeviceWatchMarket.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mWatchAdapter != null && mWatchAdapter.isEditMode()) {
                    mWatchAdapter.setEditMode(false);
                }
            }
            return false;
        });

        //init func list
        mDeviceBinding.rvDeviceFuncList.setLayoutManager(new LinearLayoutManager(requireActivity()));
        mWatchFuncAdapter = new WatchFuncAdapter();
        mWatchFuncAdapter.setOnItemClickListener((adapter, view, position) -> {
            FuncItem funcItem = mWatchFuncAdapter.getItem(position);
            if (null == funcItem) return;
            switch (funcItem.getFunc()) {
                case FuncItem.FUNC_HEALTH:
                    ContentActivity.startContentActivity(requireContext(), HealthOptionFragment.class.getCanonicalName());
                    break;
                case FuncItem.FUNC_MUSIC:
                    ContentActivity.startContentActivity(requireContext(), FilesFragment.class.getCanonicalName());
                    break;
                case FuncItem.FUNC_ALARM:
                    ContentActivity.startContentActivity(requireContext(), AlarmListFragment.class.getCanonicalName());
                    break;
                case FuncItem.FUNC_CONTACTS:
                    ContentActivity.startContentActivity(requireContext(), ContactFragment.class.getCanonicalName());
                    break;
                case FuncItem.FUNC_FILE:
                    Bundle args = new Bundle();
                    args.putInt(DeviceFileContainerFragment.KEY_TYPE, SDCardBean.SD);
                    ContentActivity.startContentActivity(requireContext(), DeviceFileContainerFragment.class.getCanonicalName(), args);
                    break;
                case FuncItem.FUNC_OTA:
                    ContentActivity.startContentActivity(requireContext(), UpgradeFragment.class.getCanonicalName());
                    break;
                case FuncItem.FUNC_NFC:
                    if (HealthConstant.TEST_DEVICE_FUNCTION) {
                        ContentActivity.startContentActivity(requireContext(), NfcMsgFragment.class.getCanonicalName());
                    } else {
                        startActivity(new Intent(getContext(), NFCActivity.class));
                    }
                    break;
                case FuncItem.FUNC_AI_CLOUD:
                    ContentActivity.startContentActivity(requireContext(), AICloudHistoryMessageFragment.class.getCanonicalName());
                    break;
                case FuncItem.FUNC_MORE:
                    ContentActivity.startContentActivity(requireContext(), MoreFragment.class.getCanonicalName());
                    break;
                case FuncItem.FUNC_AI_DIAL:
                    ContentActivity.startContentActivity(requireContext(), AIDialStyleFragment.class.getCanonicalName());
                    break;
            }
        });
        mDeviceBinding.rvDeviceFuncList.setAdapter(mWatchFuncAdapter);
    }

    private void observeCallback() {
        mWatchViewModel.mConnectionDataMLD.observeForever(mConnectionDataObserver);
        mWatchViewModel.mWatchStatusMLD.observe(getViewLifecycleOwner(), watchStatus -> {
            JL_Log.d(tag, "WatchStatusMLD", "device : " + watchStatus.getDevice() + ", " + watchStatus.getException());
            boolean isShowWatchList = false;
            if (mWatchViewModel.isUsingDevice(watchStatus.getDevice())) {
                if (watchStatus.getException() != 0) {
                    String text = "手表系统初始化异常：" + watchStatus.getException();
                    JL_Log.e(tag, "WatchStatusMLD", text);
                } else { //手表系统初始化正常
                    isShowWatchList = true;
                }
            } else {
                JL_Log.w(tag, "WatchStatusMLD", "当前设备与表盘系统不一致， device : " + HealthUtil.printBtDeviceInfo(watchStatus.getDevice()) + ", connectDevice = " + HealthUtil.printBtDeviceInfo(mWatchViewModel.getConnectedDevice()));
            }
            if (isShowWatchList) {
                boolean isSkip = mWatchViewModel.isBleChangeSpp();
                JL_Log.i(tag, "WatchStatusMLD", "init watch system ok...." + isSkip);
                if (isSkip) return;
                updateWatchUI(true, watchStatus.getDevice());
            }
        });
        mWatchViewModel.mWatchListMLD.observe(getViewLifecycleOwner(), this::updateWatchList);
        mWatchViewModel.mWatchOpDataMLD.observe(getViewLifecycleOwner(), this::updateWatchOpUI);
        mWatchViewModel.mDevPowerMsgMLD.observe(getViewLifecycleOwner(), devPowerMsg -> updateHistoryBattery(devPowerMsg.getDevice(), devPowerMsg.getBattery()));
        mWatchViewModel.mHistoryRecordListMLD.observe(getViewLifecycleOwner(), deviceHistoryRecords -> requireActivity().runOnUiThread(() -> {
            if (!isFragmentValid() || mHistoryAdapter == null) return;
            JL_Log.i(tag, "HistoryRecordListMLD", "list : " + deviceHistoryRecords.getList());
            mHistoryAdapter.setList(deviceHistoryRecords.getList());
            mDeviceBinding.rvDeviceList.setCurrentItem(deviceHistoryRecords.getUsingIndex());
            updateDeviceScrollUI(deviceHistoryRecords.getList().size(), deviceHistoryRecords.getUsingIndex());
        }));
        mWatchViewModel.mHistoryRecordChangeMLD.observe(getViewLifecycleOwner(), integer -> mWatchViewModel.syncHistoryRecordList());
        mWatchViewModel.mHistoryConnectStatusMLD.observe(getViewLifecycleOwner(), historyConnectStatus -> {
            if (historyConnectStatus.getConnectStatus() == StateCode.CONNECTION_CONNECTING) {
                showWaitDialog(true);
            } else {
                dismissWaitDialog();
                if (historyConnectStatus.getConnectStatus() == StateCode.CONNECTION_OK) {
                    showTips(R.string.history_connect_ok);
                } else {
                    showTips(R.string.history_connect_failed);
                    mWatchViewModel.syncHistoryRecordList();
                }
            }
        });
        mWatchViewModel.mDeviceConfigureMLD.observe(getViewLifecycleOwner(), this::updateWatchFuncList);
    }

    private void removeObserver() {
        mWatchViewModel.mConnectionDataMLD.removeObserver(mConnectionDataObserver);
    }

    private void updateViewPager2(int position) {
        List<DeviceHistoryRecord> data = mHistoryAdapter.getData();
        if (data.isEmpty()) return;
        int padding8dp = ValueUtil.dp2px(requireContext(), 8);
        int padding26dp = ValueUtil.dp2px(requireContext(), 26);
        if (position < 1) {
            if (data.size() == 1) {
                updateDevicesListViewPager2Margin(padding8dp, padding8dp);
            } else {
                updateDevicesListViewPager2Margin(padding8dp, padding26dp);
            }
        } else if (position >= mHistoryAdapter.getData().size() - 1) {
            updateDevicesListViewPager2Margin(padding26dp, padding8dp);
        } else {
            updateDevicesListViewPager2Margin(padding26dp, padding26dp);
        }
    }

    private void updateDevicesListViewPager2Margin(int marginStart, int marginEnd) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mDeviceBinding.rvDeviceList.getLayoutParams();
        layoutParams.setMarginStart(marginStart);
        layoutParams.setMarginEnd(marginEnd);
        mDeviceBinding.rvDeviceList.setLayoutParams(layoutParams);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateHistoryDeviceMsg(BluetoothDevice device, int status) {
        if (mHistoryAdapter == null || isDetached() || !isAdded()) return;
        if (status == BluetoothConstant.CONNECT_STATE_CONNECTED) {
            //历史适配器为空 或者 检测连接设备的历史记录是否发生变化
            if (mHistoryAdapter.getData().isEmpty() || mHistoryAdapter.checkHistoryDataIsChange(device, mWatchViewModel.getConnectedDeviceConnectWay(device))) {
                mWatchViewModel.syncHistoryRecordList();
                return;
            }
        }
        DeviceHistoryRecord historyRecord = mHistoryAdapter.getItemByDevice(device);
        JL_Log.i(tag, "updateHistoryDeviceMsg", "historyRecord : " + historyRecord + ", " + status);
        if (historyRecord == null) return;
        historyRecord.setStatus(status);
        if (status == BluetoothConstant.CONNECT_STATE_CONNECTED) {
            historyRecord.setConnectedDev(device);
            TargetInfoResponse deviceInfo = mWatchViewModel.getDeviceInfo(device);
            if (deviceInfo != null) {
                int battery = deviceInfo.getQuantity();
                historyRecord.setBattery(battery);
            }
            mHistoryAdapter.getData().remove(historyRecord);
            mHistoryAdapter.getData().add(0, historyRecord);
            mDeviceBinding.rvDeviceList.setCurrentItem(0);
        } else {
            historyRecord.setBattery(0);
        }
        mHistoryAdapter.notifyItemChanged(mHistoryAdapter.getItemPosition(historyRecord));
    }

    private void updateDeviceScrollUI(int dataSize, int pos) {
        if (isDetached() || !isAdded()) return;
        JL_Log.d(tag, "updateDeviceScrollUI", "dataSize = " + dataSize + ", pos = " + pos);
       /* if (dataSize <= 1) {
            mDeviceBinding.ivDeviceHead.setVisibility(View.GONE);
            mDeviceBinding.ivDeviceEnd.setVisibility(View.GONE);
        } else {
            mDeviceBinding.ivDeviceHead.setVisibility(pos == 0 ? View.GONE : View.VISIBLE);
            mDeviceBinding.ivDeviceEnd.setVisibility(pos == dataSize - 1 ? View.GONE : View.VISIBLE);
        }*/

        if (pos < dataSize) {
            DeviceHistoryRecord record = mHistoryAdapter.getItem(pos);
            updateWatchUI(record.getStatus() == BluetoothConstant.CONNECT_STATE_CONNECTED, record.getConnectedDev());
        } else if (dataSize == 0) {
            updateWatchUI(false, null);
        }
    }

    private void updateWatchList(ArrayList<WatchInfo> watchList) {
        if (mWatchAdapter == null || isDetached() || !isAdded()) return;
        if (!mWatchViewModel.isConnected()) watchList = new ArrayList<>();
        mDeviceBinding.clDeviceWatchMarket.setVisibility(watchList != null && !watchList.isEmpty() ? View.VISIBLE : View.GONE);
        if (watchList == null) return;
        mWatchAdapter.setList(watchList);
    }

    private void updateWatchFuncList(BluetoothDevice device) {
        if (mWatchFuncAdapter == null || isDetached() || !isAdded()) return;
        DeviceInfo deviceInfo = mWatchViewModel.getDeviceInfo(device);
        mDeviceBinding.clDeviceFunc.setVisibility(deviceInfo != null ? View.VISIBLE : View.GONE);
        if (deviceInfo == null) {
            mWatchFuncAdapter.setList(new ArrayList<>());
            return;
        }
        WatchConfigure configure = mWatchViewModel.getWatchConfigure(mWatchViewModel.getConnectedDevice());
        List<FuncItem> list = new ArrayList<>();
        FuncItem item;
        boolean isShowHealth = !deviceInfo.isMandatoryUpgrade() && (configure == null || configure.getSportHealthConfigure() != null && configure.getSportHealthConfigure().getCombineFunc() != null && configure.getSportHealthConfigure().getCombineFunc().isSupportHealthMonitoring());
        if (isShowHealth) {
            item = new FuncItem(FuncItem.FUNC_HEALTH);
            item.setName(getString(R.string.health_record));
            item.setResId(R.drawable.ic_health_green);
            list.add(item);
        }
        boolean isShowMusicManager = !deviceInfo.isMandatoryUpgrade() && DeviceChoseUtil.getTargetDev() != null && (configure == null || configure.getFunctionOption() != null && configure.getFunctionOption().isSupportMusicTransfer());
        if (isShowMusicManager) {
            item = new FuncItem(FuncItem.FUNC_MUSIC);
            item.setName(getString(R.string.music_manager));
            item.setResId(R.drawable.ic_music_orange);
            list.add(item);
        }
        boolean isSupportRTC = !deviceInfo.isMandatoryUpgrade() && deviceInfo.isRTCEnable() && (configure == null || configure.getFunctionOption() != null && configure.getFunctionOption().isSupportAlarmSetting());
        if (isSupportRTC) {
            item = new FuncItem(FuncItem.FUNC_ALARM);
            item.setName(getString(R.string.alarm));
            item.setResId(R.drawable.ic_alarm_purple);
            list.add(item);
        }
        //当flash2存在的是否也支持联系人传输
        boolean isShowContract = !deviceInfo.isMandatoryUpgrade() && (DeviceChoseUtil.getTargetDevFlash2First() != null || deviceInfo.isContactsTransferBySmallFile()) && (configure == null || configure.getFunctionOption() != null && configure.getFunctionOption().isSupportContacts());
        if (isShowContract) {
            item = new FuncItem(FuncItem.FUNC_CONTACTS);
            item.setName(getString(R.string.contacts));
            item.setResId(R.drawable.ic_contacts_orange);
            list.add(item);
        }

        if (HealthConstant.TEST_NFC_FUNCTION) {
            item = new FuncItem(FuncItem.FUNC_NFC);
            item.setName(getString(R.string.card_bag));
            item.setResId(R.drawable.ic_card_bag_purple);
            list.add(item);
        }
        boolean isShowAIDial = configure == null || configure.getFunctionOption() != null && configure.getFunctionOption().isSupportAIDial();
        if (isShowAIDial) {
            item = new FuncItem(FuncItem.FUNC_AI_DIAL);
            item.setName(getString(R.string.ai_dial));
            item.setResId(R.drawable.ic_aidial);
            list.add(item);
        }
        boolean isShowAICloud = configure == null || configure.getFunctionOption() != null && configure.getFunctionOption().isSupportAICloud();
        if (isShowAICloud) {
            item = new FuncItem(FuncItem.FUNC_AI_CLOUD);
            item.setName(getString(R.string.ai_cloud_serve));
            item.setResId(R.drawable.ic_ai);
            list.add(item);
        }
        boolean isShowOTA = configure == null || configure.getNecessaryFunc() != null && configure.getNecessaryFunc().isSupportOTA();
        if (isShowOTA) {
            item = new FuncItem(FuncItem.FUNC_OTA);
            item.setName(getString(R.string.firmware_upgrade));
            item.setResId(R.drawable.ic_firmware_upgrade_blue);
            list.add(item);
        }
        if (!deviceInfo.isMandatoryUpgrade()) {
            item = new FuncItem(FuncItem.FUNC_MORE);
            item.setName(getString(R.string.more));
            item.setResId(R.drawable.ic_more_green);
            list.add(item);
        }
        mWatchFuncAdapter.setList(list);
    }

    private void updateWatchOpUI(final WatchOpData data) {
        mHandler.post(() -> {
            if (data.getOp() != WatchOpData.OP_DELETE_FILE) return;
            switch (data.getState()) {
                case WatchOpData.STATE_START:
                    showWaitDialog(true);
                    break;
                case WatchOpData.STATE_PROGRESS:
                    break;
                case WatchOpData.STATE_END:
                    JL_Log.e(tag, "updateWatchOpUI", "watchOpData >>> " + data);
                    dismissWaitDialog();
                    if (data.getResult() == FatFsErrCode.RES_OK) {
                        mWatchViewModel.listWatchList();
                    }
                    break;
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateHistoryBattery(BluetoothDevice device, BatteryInfo batteryInfo) {
        if (!isFragmentValid() || mHistoryAdapter == null || device == null || batteryInfo == null)
            return;
        DeviceHistoryRecord record = mHistoryAdapter.getItemByDevice(device);
        if (record == null) return;
        record.setBattery(batteryInfo.getBattery());
        mHistoryAdapter.notifyDataSetChanged();
    }

    public void updateWatchUI(boolean isConnected, BluetoothDevice device) {
        JL_Log.i(tag, "updateWatchUI", "isConnected : " + isConnected + ", device = " + device);
        DeviceInfo deviceInfo = mWatchViewModel.getDeviceInfo(device);
        if (isConnected && mWatchViewModel.isWatchSystemInit(device)) {
            mWatchViewModel.listWatchList();
            checkNotificationEnable(requireContext());
            updateWatchFuncList(device);
        } else if (isConnected && (deviceInfo != null && deviceInfo.isMandatoryUpgrade())) {
            updateWatchFuncList(device);
        } else {
            updateWatchList(null);
            updateWatchFuncList(null);
        }
    }

    @NeedsPermission({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,})
    public void reconnectHistory(DeviceHistoryRecord record) {
        if (!PermissionUtil.checkGpsProviderEnable(getContext())) {
            showOpenGPSDialog();
        } else {
            if (BluetoothUtil.isBluetoothEnable()) {
                record.setStatus(BluetoothConstant.CONNECT_STATE_CONNECTING);
                mHistoryAdapter.notifyItemChanged(mHistoryAdapter.getItemPosition(record));
                mWatchViewModel.reconnectHistory(record);
            } else {
                BluetoothUtil.enableBluetooth(requireContext());
            }
        }
    }

    @OnShowRationale({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,})
    public void showRelationForLocationPermission(PermissionRequest request) {
        showRequireGPSPermissionDialog(request);
    }

    @OnPermissionDenied({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,})
    public void onLocationDenied() {
        showTips(getString(R.string.user_denies_permissions, getString(R.string.app_name)));
    }

    @OnNeverAskAgain({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,})
    public void onLocationNeverAskAgain() {
        showTips(getString(R.string.user_denies_permissions, getString(R.string.app_name)));
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @NeedsPermission({Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN,})
    public void requestBtPermission(DeviceHistoryRecord record) {
        tryToReconnectHistory(record);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @OnShowRationale({Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN,})
    public void showRelationForBtPermission(PermissionRequest request) {
        request.proceed();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @OnPermissionDenied({Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN,})
    public void onBtDenied() {
        showTips(CalendarUtil.formatString("%s%s%s", getString(R.string.permissions_tips_02), getString(R.string.permission_bluetooth), getString(R.string.permission)));
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @OnNeverAskAgain({Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN,})
    public void onBtNeverAskAgain() {
        showTips(CalendarUtil.formatString("%s%s%s", getString(R.string.permissions_tips_02), getString(R.string.permission_bluetooth), getString(R.string.permission)));
    }

    private void showOpenGPSDialog() {
        showGPSDialog(null, true);
    }

    private void showRequireGPSPermissionDialog(PermissionRequest request) {
        showGPSDialog(request, false);
    }

    private void showGPSDialog(PermissionRequest request, boolean isLocationService) {
        RequireGPSDialog requireGPSDialog = new RequireGPSDialog(RequireGPSDialog.VIEW_TYPE_DEVICE, request);
        requireGPSDialog.setLocationService(isLocationService);
        requireGPSDialog.setCancelable(true);
        requireGPSDialog.show(getChildFragmentManager(), RequireGPSDialog.class.getCanonicalName());
    }

    private void checkNotificationEnable(@NonNull Context context) {
        if (NotificationHelper.getInstance().isEnableNotification() && NotificationHelper.isNotificationServiceEnabled(context)) {
            if (!NotificationHelper.isNotificationEnable(context)) {
                showEnableNotificationListenerDialog();
            }
        }
    }

    private void showEnableNotificationListenerDialog() {
        if (!isFragmentValid()) return;
        Jl_Dialog.builder().width(0.8f).cancel(true).content(getString(R.string.enable_notification_listener_service_tips)).left(getString(R.string.cancel)).leftColor(getResources().getColor(R.color.black)).leftClickListener((view, dialogFragment) -> dialogFragment.dismiss()).right(getString(R.string.sure)).rightColor(getResources().getColor(R.color.red_D25454)).rightClickListener((view, dialogFragment) -> {
            dialogFragment.dismiss();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", requireContext().getApplicationContext().getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }).build().show(getChildFragmentManager(), "notification_listener_service");
    }

    private void tryToReconnectHistory(DeviceHistoryRecord record) {
        if (null == record) return;
        showPermissionDialog(Manifest.permission.ACCESS_COARSE_LOCATION, ((permission) ->
                DeviceFragmentPermissionsDispatcher.reconnectHistoryWithPermissionCheck(this, record)));
    }


    @SuppressLint("WrongConstant")
    private void registerStateReceiver() {
        if (null != receiver) return;
        receiver = new StateBroadcastReceiver(mWatchViewModel);
        IntentFilter filter = new IntentFilter(HealthConstant.ACTION_UPDATE_RESOURCE_SUCCESS);
        ContextCompat.registerReceiver(requireActivity(), receiver, filter, ContextCompat.RECEIVER_EXPORTED);
    }


    private void unregisterStateReceiver() {
        if (null == receiver) return;
        requireContext().unregisterReceiver(receiver);
        receiver = null;
    }

    private final Observer<DeviceConnectionData> mConnectionDataObserver = deviceConnectionData -> {
        JL_Log.i(tag, "ConnectionDataObserver", "deviceConnectionData = " + deviceConnectionData);
        if (!isAdded()) return;
        updateHistoryDeviceMsg(deviceConnectionData.getDevice(), deviceConnectionData.getStatus());
        if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
            updateWatchUI(false, null);
        }
        if (deviceConnectionData.getStatus() == BluetoothConstant.CONNECT_STATE_CONNECTING) {
            showWaitDialog(true);
        } else {
            dismissWaitDialog();
        }
    };

    private static class StateBroadcastReceiver extends BroadcastReceiver {

        @NonNull
        private final WatchViewModel viewModel;

        public StateBroadcastReceiver(@NonNull WatchViewModel viewModel) {
            this.viewModel = viewModel;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent) return;
            String action = intent.getAction();
            if (null == action) return;
            if (HealthConstant.ACTION_UPDATE_RESOURCE_SUCCESS.equals(action)) {
                BluetoothDevice device = viewModel.getConnectedDevice();
                if (null == device) return;
                String address = intent.getStringExtra(HealthConstant.KEY_DEVICE_ADDRESS);
                if (TextUtils.equals(device.getAddress(), address)) {
                    JL_Log.d("StateBroadcastReceiver", "ACTION_UPDATE_RESOURCE_SUCCESS", "syncWatchList");
                    viewModel.syncWatchList();
                }
            }
        }
    }
}