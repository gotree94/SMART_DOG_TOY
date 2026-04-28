package com.jieli.watchtesttool.ui.ota;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;

import com.google.gson.GsonBuilder;
import com.jieli.component.utils.FileUtil;
import com.jieli.jl_bt_ota.constant.ErrorCode;
import com.jieli.jl_bt_ota.util.JL_Log;
import com.jieli.jl_fatfs.utils.ZipUtil;
import com.jieli.jl_rcsp.constant.WatchError;
import com.jieli.jl_rcsp.impl.NetworkOpImpl;
import com.jieli.jl_rcsp.interfaces.network.OnNetworkListener;
import com.jieli.jl_rcsp.interfaces.network.OnNetworkOTACallback;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.model.device.settings.v0.NetworkInfo;
import com.jieli.jl_rcsp.model.device.settings.v0.NetworkOTAState;
import com.jieli.jl_rcsp.model.network.OTAParam;
import com.jieli.watchtesttool.WatchApplication;
import com.jieli.watchtesttool.data.bean.SdkMapInfo;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothViewModel;
import com.jieli.watchtesttool.tool.upgrade.OTAManager;
import com.jieli.watchtesttool.util.AppUtil;
import com.jieli.watchtesttool.util.WatchTestConstant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 网络模块升级逻辑实现
 * @since 2023/12/19
 */
public class NetworkOtaViewModel extends BluetoothViewModel {
    private final NetworkOpImpl mNetworkOp;

    public final MutableLiveData<NetworkInfo> mNetworkInfoMLD = new MutableLiveData<>();
    public final MutableLiveData<List<File>> mOtaFileMLD = new MutableLiveData<>();
    public final MutableLiveData<OtaStatus> mOtaStatusMLD = new MutableLiveData<>();

    private String otaDirPath;

    public NetworkOtaViewModel() {
        mNetworkOp = NetworkOpImpl.instance(mWatchManager);
        mNetworkOp.addOnNetworkListener(mOnNetworkListener);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        destroy();
    }

    @Override
    public void destroy() {
        mNetworkOp.removeOnNetworkListener(mOnNetworkListener);
        mNetworkOp.destroy();
        super.destroy();
    }

    public String getOTADirPath() {
        return AppUtil.createFilePath(WatchApplication.getWatchApplication(), WatchTestConstant.DIR_NETWORK);
    }

    public void listOTAFile() {
        File dir = new File(getOTADirPath());
        File[] files = dir.listFiles();
        if (null == files || files.length == 0) {
            mOtaFileMLD.postValue(new ArrayList<>());
            return;
        }
        List<File> result = new ArrayList<>();
        for (File file : files) {
            if (file.isFile() && file.length() > 0) {
                result.add(file);
            }
        }
        mOtaFileMLD.postValue(result);
    }

    public void queryNetworkInfo() {
        mNetworkOp.queryNetworkInfo(getConnectedDevice(), null);
    }

    public boolean isNetworkOTA() {
        return mNetworkOp.isNetworkOTA();
    }

    public void startOTA(String filePath) {
        if (isNetworkOTA() || (mOtaStatusMLD.getValue() != null && (mOtaStatusMLD.getValue().getState() != OtaStatus.STATE_IDLE
                || mOtaStatusMLD.getValue().getState() != OtaStatus.STATE_STOP))) return;
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            mOtaStatusMLD.postValue(new OtaStop().setCode(ErrorCode.SUB_ERR_FILE_NOT_FOUND).setMessage("Ota File not found." + filePath));
            return;
        }
        String otaFilePath = null;
        if (file.getName().endsWith(OTAManager.OTA_ZIP_SUFFIX)) {
            final DeviceInfo deviceInfo = mWatchManager.getDeviceInfo();
            NetworkInfo networkInfo = deviceInfo.getNetworkInfo();
            try {
                otaDirPath = AppUtil.getDirPath(filePath);
                ZipUtil.unZipFolder(filePath, otaDirPath, "tmp");
                otaDirPath = otaDirPath + File.separator + "tmp";
                String jsonFilePath = AppUtil.obtainUpdateFilePath(otaDirPath, ".json");
                File json = new File(jsonFilePath);
                JL_Log.d(tag, "[startOTA] >>> jsonFilePath = " + jsonFilePath);
                if (json.exists() && json.isFile()) {
                    String content = AppUtil.readJsonByPath(json);
                    SdkMapInfo sdkMapInfo = new GsonBuilder().create().fromJson(content, SdkMapInfo.class);
                    JL_Log.d(tag, "[startOTA] >>> " + sdkMapInfo);
                    final List<SdkMapInfo.MapDTO> mapList = sdkMapInfo.getMap();
                    if (null != mapList) {
                        for (SdkMapInfo.MapDTO map : mapList) {
                            long mapVersion = convertVersionCode(networkInfo.getVid(), map.getVersion());
                            long firmwareVersion = convertVersionCode(networkInfo.getVid(), networkInfo.getVersion());
                            if (mapVersion == firmwareVersion) {
                                otaFilePath = AppUtil.obtainUpdateFilePath(otaDirPath, map.getPakage());
                                break;
                            }
                        }
                    }
                }
//                FileUtil.deleteFile(new File(filePath));
                JL_Log.d(tag, "[startOTA] >>> otaFilePath = " + otaFilePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            otaFilePath = file.getPath();
        }
        if (TextUtils.isEmpty(otaFilePath)) {
            mOtaStatusMLD.postValue(new OtaStop().setCode(ErrorCode.SUB_ERR_FILE_NOT_FOUND).setMessage("Ota File not found." + filePath));
            return;
        }
        mNetworkOp.startNetworkOTA(getConnectedDevice(), new OTAParam(otaFilePath), new OnNetworkOTACallback() {
            @Override
            public void onStart() {
                mOtaStatusMLD.postValue(new OtaStatus(OtaStatus.STATE_START));
            }

            @Override
            public void onProgress(int progress) {
                mOtaStatusMLD.postValue(new OtaWorking().setProgress(progress));
            }

            @Override
            public void onCancel() {
                if (null != otaDirPath) {
                    FileUtil.deleteFile(new File(otaDirPath));
                    otaDirPath = null;
                }
                mOtaStatusMLD.postValue(new OtaStop().setCode(WatchError.ERR_CANCEL_OP)
                        .setMessage(WatchError.getErrorDesc(WatchError.ERR_CANCEL_OP)));
            }

            @Override
            public void onStop() {
                if (null != otaDirPath) {
                    FileUtil.deleteFile(new File(otaDirPath));
                    otaDirPath = null;
                }
                mOtaStatusMLD.postValue(new OtaStop().setCode(0).setMessage(""));
            }

            @Override
            public void onError(int code, String message) {
                if (null != otaDirPath) {
                    FileUtil.deleteFile(new File(otaDirPath));
                    otaDirPath = null;
                }
                mOtaStatusMLD.postValue(new OtaStop().setCode(code).setMessage(message));
            }
        });
    }

    public void cancelOTA() {
        mNetworkOp.cancelNetworkOTA(getConnectedDevice(), null);
    }

    private long convertVersionCode(int vid, String version) {
        if (null == version || version.length() == 0) return -1;
        Pattern pattern = Pattern.compile("\\d+"); // 定义正则表达式模式，匹配连续的数字
        Matcher matcher = pattern.matcher(version);
        StringBuilder value = new StringBuilder();
        while (matcher.find()) {
            value.append(matcher.group());
        }
        String result = value.toString();
        if (TextUtils.isDigitsOnly(result)) {
            try {
                return Long.parseLong(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    private final OnNetworkListener mOnNetworkListener = new OnNetworkListener() {
        @Override
        public void onNetworkInfo(BluetoothDevice device, NetworkInfo info) {
            mNetworkInfoMLD.postValue(info);
        }

        @Override
        public void onNetworkOTAState(BluetoothDevice device, NetworkOTAState state) {

        }
    };
}
