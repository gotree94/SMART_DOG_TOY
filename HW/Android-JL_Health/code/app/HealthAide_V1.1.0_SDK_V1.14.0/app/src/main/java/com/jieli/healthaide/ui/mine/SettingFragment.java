package com.jieli.healthaide.ui.mine;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;

import com.jieli.component.utils.PreferencesHelper;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentSettingBinding;
import com.jieli.healthaide.tool.bluetooth.BluetoothHelper;
import com.jieli.healthaide.tool.config.ConfigHelper;
import com.jieli.healthaide.tool.notification.NotificationHelper;
import com.jieli.healthaide.tool.unit.BaseUnitConverter;
import com.jieli.healthaide.tool.watch.WatchServerCacheHelper;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.more.MessageSyncFragment;
import com.jieli.healthaide.ui.dialog.SingleChooseDialog;
import com.jieli.healthaide.ui.mine.entries.CommonItem;
import com.jieli.healthaide.util.MultiLanguageUtils;
import com.jieli.jl_dialog.Jl_Dialog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/10/21 9:44 AM
 * @desc :
 */
public class SettingFragment extends BaseFragment {

    FragmentSettingBinding settingBinding;

    private Jl_Dialog mEnableNotificationListenerDialog;

    private final static int TYPE_MESSAGE_SYNC = 1;
    private final static int TYPE_WEATHER_PUSH = 2;
    private final static int TYPE_UNIT_SETTINGS = 3;
    private final static int TYPE_LANGUAGE_SETTINGS = 4;
//    private ESimWrapper mESimWrapper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       /* mESimWrapper = new ESimWrapper(WatchManager.getInstance());
        mESimWrapper.addESimWrapperListener(new ESimWrapper.ESimWrapperListener() {
            @Override
            public void onReceiveESimData(byte[] eSimData) {
                settingBinding.tvTestEsimRev.setText("收到eSim数据：" + CHexConver.byte2HexStr(eSimData));
            }
        });
        WatchManager.getInstance().registerOnRcspEventListener(new OnRcspEventListener() {
            @Override
            public void onPlatformInterfaceInfoChange(BluetoothDevice device, Map<Integer, List<LtvBean>> platformInterfaceInfoMap) {
                super.onPlatformInterfaceInfoChange(device, platformInterfaceInfoMap);
                String text = "收到eSim数据：" + "\n";
                for (Map.Entry<Integer, List<LtvBean>> entry : platformInterfaceInfoMap.entrySet()) {
                    System.out.println(entry.getKey() + ":" + entry.getValue());
                    text = text + "接口" + entry.getKey() + "\n";
                    for (LtvBean ltvBean : entry.getValue()) {
                        text = text + "类型  " + ltvBean.getType()+" ,数据  " + new String(ltvBean.getData())+ "\n";
                    }
                }
                settingBinding.tvTestTip.setText(text);
            }
        });*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        mESimWrapper.release();
//        mESimWrapper = null;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        settingBinding = FragmentSettingBinding.inflate(inflater, container, false);
        settingBinding.layoutTopbar.tvTopbarTitle.setText(R.string.mine_setting);
        settingBinding.layoutAccountSecurity.tvSettingTarget2.setText(R.string.account_security);
        settingBinding.layoutAccountSecurity.getRoot().setOnClickListener(view -> ContentActivity.startContentActivity(requireContext(), AccountSecurityFragment.class.getCanonicalName()));
        settingBinding.layoutTarget.tvSettingTarget2.setText(getString(R.string.target));
        settingBinding.layoutTarget.getRoot().setOnClickListener(view -> ContentActivity.startContentActivity(requireContext(), MyTargetFragment.class.getCanonicalName()));
        settingBinding.layoutCleanCache.tvSettingTarget2.setText(getString(R.string.clean_cache));
        settingBinding.layoutCleanCache.getRoot().setOnClickListener(view -> cleanCache());
        CommonAdapter commonAdapter = new CommonAdapter();
        commonAdapter.setList(getCommonItemData());
        commonAdapter.setOnItemClickListener((adapter, view, position) -> {
            CommonItem item = ((CommonAdapter) adapter).getItem(position);
            if (null == item) return;
            switch (item.getType()) {
                case TYPE_MESSAGE_SYNC:
                    if (!NotificationHelper.isNotificationServiceEnabled(requireContext())) {
                        showEnableNotificationListenerDialog();
                    } else {
                        if (BluetoothHelper.getInstance().isConnectedDevice()) {
                            ContentActivity.startContentActivity(requireContext(), MessageSyncFragment.class.getCanonicalName());
                        } else {
                            showTips(R.string.bt_disconnect_tips);
                        }
                    }
                    break;
                case TYPE_WEATHER_PUSH:
                    break;
                case TYPE_UNIT_SETTINGS:
                    List<String> data = new ArrayList<>();
                    data.add(getString(R.string.unit_metric));
                    data.add(getString(R.string.unit_imperial));
                    String select = commonAdapter.getItem(position).getTailString().toString();
                    SingleChooseDialog<String> chooseDialog = new SingleChooseDialog<>(getString(R.string.unit), data, select, (dialog, value) -> {
                        commonAdapter.getItem(position).setTailString(value);
                        commonAdapter.notifyItemChanged(position);
                        BaseUnitConverter.setType(value.equals(getString(R.string.unit_metric)) ? BaseUnitConverter.TYPE_METRIC : BaseUnitConverter.TYPE_IMPERIAL);
                        ConfigHelper.getInstance().setUnitType(BaseUnitConverter.getType());
                        dialog.dismiss();
                    });
                    chooseDialog.show(getChildFragmentManager(), "select_unit_type");
                    break;
                case TYPE_LANGUAGE_SETTINGS: //多语言设置
                    ContentActivity.startContentActivity(requireContext(), LanguageSetFragment.class.getCanonicalName());
                    break;
            }
        });
        settingBinding.rvSetting.setAdapter(commonAdapter);
        settingBinding.rvSetting.setLayoutManager(new LinearLayoutManager(requireContext()));
        DividerItemDecoration decoration = new DividerItemDecoration(requireContext(), OrientationHelper.VERTICAL);
        decoration.setDrawable(getResources().getDrawable(R.drawable.line_gray_1dp));
        settingBinding.rvSetting.addItemDecoration(decoration);
        settingBinding.layoutTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());

        settingBinding.layoutCleanCache.getRoot().setOnClickListener(v -> showCleanCacheDialog());

/*        settingBinding.tvGoBtSharePage.setOnClickListener(v -> {
//            jumpToHotSpotApPage(getContext());
            jumpToBluetoothSharePage(getContext());
        });
        settingBinding.tvTest.setOnClickListener(v -> {
            DataParams dataParams = new DataParams(RcspConstant.WAY_READ_DATA, RcspConstant.TYPE_PLATFORM_INTERFACE_DATA, RcspConstant.DATA_TRANSFER_VERSION,
                    4 * 1024, 4 * 1024);
            OnDataEventCallback mOnDataEventCallback = new OnDataEventCallback() {
                @Override
                public void onBegin(int way) {
                    settingBinding.tvTestTip.setText("开始读取数据,way:  " + way);
                }

                @Override
                public void onProgress(float progress) {
                    settingBinding.tvTestTip.setText("读取数据...,progress:  " + progress);
                }

                @Override
                public void onStop(int type, byte[] data) {
                    settingBinding.tvTestTip.setText("读取数据结束...,type:  " + type + "\n" + CHexConver.byte2HexStr(data));
                }

                @Override
                public void onError(BaseError error) {
                    settingBinding.tvTestTip.setText("读取数据失败...,error:  " + error.toString());
                }
            };
            WatchManager.getInstance().readLargeData(dataParams, mOnDataEventCallback);
        });
        settingBinding.tvTestEsim.setOnClickListener(v -> {
            OnDataEventCallback onDataEventCallback = new OnDataEventCallback() {
                @Override
                public void onBegin(int way) {
                    // TODO: 2023/11/28 开始传输
                }

                @Override
                public void onProgress(float progress) {
                    // TODO: 2023/11/28 传输进度
                }

                @Override
                public void onStop(int type, byte[] data) {
                    // TODO: 2023/11/28 传输结束-结果返回数据
                }

                @Override
                public void onError(BaseError error) {
                    // TODO: 2023/11/28 传输失败
                }
            };
            byte[] bytes = new byte[]{(byte) 0xFF, (byte) 0x00};
            mESimWrapper.sendESimData(bytes, onDataEventCallback);
        });*/
        return settingBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
//        testPan();
    }

/*    private AtomicReference<Object> mBluetoothPan = new AtomicReference<>();
    private BluetoothProfile.ServiceListener mProfileServiceListener =
            new BluetoothProfile.ServiceListener() {
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    //获取成功
                    mBluetoothPan.set(proxy);
                    Log.d("TAG", "onServiceConnected: ");
                    updateIsTetheringOn();
                }

                public void onServiceDisconnected(int profile) {
                    Log.d("TAG", "onServiceDisconnected: ");
                    //获取失败
                    mBluetoothPan.set(null);
                }
            };

    private void updateIsTetheringOn() {
        boolean isTetheringOn = getIsTetheringOn();
        settingBinding.tvOpenBtShare.setText(isTetheringOn ? "关闭蓝牙共享" : "打开蓝牙共享");
        settingBinding.tvOpenBtShare.setOnClickListener(v -> {
            setBluetoothTethering(!isTetheringOn);
        });
        settingBinding.tvOpenBtShareStatus.setText("当前蓝牙共享状态：" + (isTetheringOn ? "打开" : "关闭"));
    }

    private void testPan() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            //获取pan代理对象
            adapter.getProfileProxy(getActivity().getApplicationContext(),
                    mProfileServiceListener,
                    5);
        }
    }

    private boolean getIsTetheringOn() {
        try {
            Object bluetoothPan = mBluetoothPan.get();
            Class bluetoothPanClass = Class.forName("android.bluetooth.BluetoothPan");
            if (bluetoothPan != null) {
                Method methodIsTetheringOn = bluetoothPanClass.getMethod("isTetheringOn");
                Log.i("TAG", "isTetheringOn: " + methodIsTetheringOn.invoke(bluetoothPan));
                return (boolean) methodIsTetheringOn.invoke(bluetoothPan);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setBluetoothTethering(boolean isOpen) {
        try {
            Object bluetoothPan = mBluetoothPan.get();
            Class bluetoothPanClass = Class.forName("android.bluetooth.BluetoothPan");
            if (bluetoothPan != null) {
                Method methodSetBluetoothTethering = bluetoothPanClass.getMethod("setBluetoothTethering", boolean.class);
                methodSetBluetoothTethering.invoke(bluetoothPan, isOpen);
                updateIsTetheringOn();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showToastShort("设置失败");
        }
    }

    public void jumpToBluetoothSharePage(Context context) {
        // TODO: 2023/11/17 兼容部分机型先跳转
        try {
            Intent intent = new Intent();
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setAction("android.intent.action.MAIN");
            ComponentName cn = new ComponentName("com.android.settings",
                    "com.android.settings.Settings$TetherSettingsActivity");
            intent.setComponent(cn);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            jumpToBluetoothSharePageCompat(context);
        }
    }

    public void jumpToBluetoothSharePageCompat(Context context) {

    }*/

//    //跳转到热点页面
//    public static void jumpToHotSpotApPage(Context context) {
//        Log.d("TAG", "jumpToHotSpotApPage: "+Build.BRAND);
//        if (!Build.BRAND.equalsIgnoreCase("huawei") && !Build.BRAND.equalsIgnoreCase("honor")) {
//            jumpToHotSpotApPageCompat(context);
//            Log.d("TAG", "jumpToHotSpotApPageCompat: ");
//            return;
//        }//end of if
//        try {
//            Intent intent = new Intent();
//            intent.addCategory(Intent.CATEGORY_DEFAULT);
//            intent.setAction("android.intent.action.MAIN");
//            intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$WirelessSettingsActivity"));
//            context.startActivity(intent);
//        } catch (Exception e) {
//            e.printStackTrace();
//            jumpToHotSpotApPageCompat(context);
//        }
//    }
//
//    //跳转到热点页面兼容特殊机型的特殊处理
//    public static void jumpToHotSpotApPageCompat(Context context) {
//        try {
//            Intent intent = new Intent();
//            intent.addCategory(Intent.CATEGORY_DEFAULT);
//            intent.setAction("android.intent.action.MAIN");
//            intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$TetherSettingsActivity"));
//            context.startActivity(intent);
//        } catch (Exception e) {
//            e.printStackTrace();
//            try {
//                Intent intent = new Intent();
//                intent.addCategory(Intent.CATEGORY_DEFAULT);
//                intent.setAction("android.intent.action.MAIN");
//                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.TetherSettings"));
//                context.startActivity(intent);
//            } catch (Exception e2) {
//                e2.printStackTrace();
//                ToastUtil.showToastShort("跳转蓝牙共享界面失败,请手动去打开");
////                Utils.Companion.toastMsg("跳转热点界面失败,请手动去打开", true, null);
//            }
//        }
//    }

    private List<CommonItem> getCommonItemData() {
        List<CommonItem> list = new ArrayList<>();

//        CommonItem notify = new CommonItem();
//        notify.setType(TYPE_MESSAGE_SYNC);
//        notify.setTitle(getString(R.string.alert));
//        notify.setShowNext(true);
//        list.add(notify);

//        CommonItem weatherPush = new CommonItem();
//        weatherPush.setType(TYPE_WEATHER_PUSH);
//        weatherPush.setTitle(getString(R.string.weather_push));
//        weatherPush.setShowSw(true);
//        list.add(weatherPush);

        CommonItem unitSetting = new CommonItem();
        unitSetting.setType(TYPE_UNIT_SETTINGS);
        unitSetting.setTitle(getString(R.string.unit_settings));
        unitSetting.setTailString(getString(BaseUnitConverter.getType() == BaseUnitConverter.TYPE_METRIC ? R.string.unit_metric : R.string.unit_imperial));
        unitSetting.setShowNext(true);
        list.add(unitSetting);

        String languageTailString = getString(R.string.follow_system);
        String language = PreferencesHelper.getSharedPreferences(HealthApplication.getAppViewModel().getApplication()).getString(MultiLanguageUtils.SP_LANGUAGE, MultiLanguageUtils.LANGUAGE_AUTO);
        if (TextUtils.equals(language, MultiLanguageUtils.LANGUAGE_ZH)) {
            languageTailString = getString(R.string.simplified_chinese);
        } else if (TextUtils.equals(language, MultiLanguageUtils.LANGUAGE_EN)) {
            languageTailString = getString(R.string.english);
        } else if (TextUtils.equals(language, MultiLanguageUtils.LANGUAGE_JA)) {
            languageTailString = getString(R.string.japanese);
        } else if (TextUtils.equals(language, MultiLanguageUtils.LANGUAGE_KO)) {
            languageTailString = getString(R.string.korean);
        } else if (TextUtils.equals(language, MultiLanguageUtils.LANGUAGE_FR)) {
            languageTailString = getString(R.string.french);
        } else if (TextUtils.equals(language, MultiLanguageUtils.LANGUAGE_DE)) {
            languageTailString = getString(R.string.german);
        } else if (TextUtils.equals(language, MultiLanguageUtils.LANGUAGE_IT)) {
            languageTailString = getString(R.string.italian);
        } else if (TextUtils.equals(language, MultiLanguageUtils.LANGUAGE_PT)) {
            languageTailString = getString(R.string.portuguese);
        } else if (TextUtils.equals(language, MultiLanguageUtils.LANGUAGE_ES)) {
            languageTailString = getString(R.string.spanish);
        } else if (TextUtils.equals(language, MultiLanguageUtils.LANGUAGE_SV)) {
            languageTailString = getString(R.string.swedish);
        } else if (TextUtils.equals(language, MultiLanguageUtils.LANGUAGE_PL)) {
            languageTailString = getString(R.string.polish);
        } else if (TextUtils.equals(language, MultiLanguageUtils.LANGUAGE_RU)) {
            languageTailString = getString(R.string.russian);
        } else if (TextUtils.equals(language, MultiLanguageUtils.LANGUAGE_TR)) {
            languageTailString = getString(R.string.turkish);
        } else if (TextUtils.equals(language, MultiLanguageUtils.LANGUAGE_IW)) {
            languageTailString = getString(R.string.hebrew);
        } else if (TextUtils.equals(language, MultiLanguageUtils.LANGUAGE_TH)) {
            languageTailString = getString(R.string.thai);
        } else if (TextUtils.equals(language, MultiLanguageUtils.LANGUAGE_AR)) {
            languageTailString = getString(R.string.arabic);
        } else if (TextUtils.equals(language, MultiLanguageUtils.LANGUAGE_VI)) {
            languageTailString = getString(R.string.vietnamese);
        } else if (TextUtils.equals(language, MultiLanguageUtils.LANGUAGE_IN)) {
            languageTailString = getString(R.string.indonesian);
        } else if (TextUtils.equals(language, MultiLanguageUtils.LANGUAGE_MS)) {
            languageTailString = getString(R.string.malay);
        } else if (TextUtils.equals(language, MultiLanguageUtils.LANGUAGE_FA)) {
            languageTailString = getString(R.string.persian);
        }
        CommonItem languageSetting = new CommonItem();
        languageSetting.setType(TYPE_LANGUAGE_SETTINGS);
        languageSetting.setTitle(getString(R.string.multilingual));
        languageSetting.setTailString(languageTailString);
        languageSetting.setShowNext(true);
        list.add(languageSetting);
        return list;
    }

    private void showEnableNotificationListenerDialog() {
        if (isDetached() || !isAdded()) return;
        if (null == mEnableNotificationListenerDialog) {
            mEnableNotificationListenerDialog = Jl_Dialog.builder()
                    .width(0.8f)
                    .cancel(true)
                    .content(getString(R.string.enable_notification_listener_service_tips))
                    .left(getString(R.string.cancel))
                    .leftColor(getResources().getColor(R.color.black))
                    .leftClickListener((view, dialogFragment) -> dismissEnableNotificationListenerDialog())
                    .right(getString(R.string.sure))
                    .rightColor(getResources().getColor(R.color.red_D25454))
                    .rightClickListener((view, dialogFragment) -> {
                        dismissEnableNotificationListenerDialog();
                        startActivity(new Intent(NotificationHelper.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    })
                    .build();
        }
        if (!mEnableNotificationListenerDialog.isShow()) {
            mEnableNotificationListenerDialog.show(getChildFragmentManager(), "notification_listener_service");
        }
    }


    private void showCleanCacheDialog() {
        String tag = "clean_cache_dialog";
        Fragment fragment = getChildFragmentManager().findFragmentByTag(tag);
        if (fragment != null) return;

        Jl_Dialog dialog = Jl_Dialog.builder()
                .width(0.8f)
                .cancel(true)
                .content(getString(R.string.tip_clean_cache))
                .left(getString(R.string.cancel))
                .leftColor(getResources().getColor(R.color.black))
                .leftClickListener((view, dialogFragment) -> dialogFragment.dismiss())
                .right(getString(R.string.sure))
                .rightColor(getResources().getColor(R.color.red_D25454))
                .rightClickListener((view, dialogFragment) -> {
                    dialogFragment.dismiss();
                    //todo 内存清理
                    HealthApplication.getAppViewModel().cleanCache();
                    WatchServerCacheHelper.getInstance().clearCache();
                })
                .build();
        dialog.show(getChildFragmentManager(), tag);

    }


    private void dismissEnableNotificationListenerDialog() {
        if (isDetached() || !isAdded()) return;
        if (mEnableNotificationListenerDialog != null) {
            if (mEnableNotificationListenerDialog.isShow()) {
                mEnableNotificationListenerDialog.dismiss();
            }
            mEnableNotificationListenerDialog = null;
        }
    }

    private void cleanCache() {
        //todo 清除缓存
    }
}