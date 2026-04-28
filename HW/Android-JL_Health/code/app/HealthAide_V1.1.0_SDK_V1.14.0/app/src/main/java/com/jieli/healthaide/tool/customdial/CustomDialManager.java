package com.jieli.healthaide.tool.customdial;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jieli.bmp_convert.BmpConvert;
import com.jieli.bmp_convert.ConvertResult;
import com.jieli.bmp_convert.OnConvertListener;
import com.jieli.component.utils.FileUtil;
import com.jieli.component.utils.PreferencesHelper;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.healthaide.util.AppUtil;
import com.jieli.healthaide.util.BitmapUtil;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_fatfs.FatFsErrCode;
import com.jieli.jl_fatfs.interfaces.OnFatFileProgressListener;
import com.jieli.jl_fatfs.model.FatFile;
import com.jieli.jl_fatfs.utils.FatUtil;
import com.jieli.jl_rcsp.constant.JLChipFlag;
import com.jieli.jl_rcsp.constant.WatchConstant;
import com.jieli.jl_rcsp.constant.WatchError;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.model.device.settings.v0.DialExpandInfo;
import com.jieli.jl_rcsp.model.response.ExternalFlashMsgResponse;
import com.jieli.jl_rcsp.util.JL_Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: CustomDialManager
 * @Description: 自定义表盘（按用户+设备mac保存）
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/11 15:23
 */
public class CustomDialManager {
    public static final String WATCH_DIRECTORY = "watch";
    private final String CUSTOM_DIRECTORY_PREFIX = "custom_";
    private final String CUSTOM_DIRECTORY_DEFAULT = "custom_default";
    public static final String IMAGE_SRC_TEMP_DEFAULT = "image_cache";
    private final String IMAGE_CUT_TEMP_DEFAULT = "cut_cache";
    private final String KEY_CUSTOM_DIAL_LIST = "key_custom_dial_list";
    private final String KEY_CUSTOM_DIAL_USE_IDS = "key_custom_dial_use_ids";
    private final static String WATCH_PREFIX = "WATCH";
    private final static String CUSTOM_BG_PREFIX = "bgp_w";
    public static final String JPG_FORMAT = ".jpg";
    public static final String PNG_FORMAT = ".png";
    public MutableLiveData<Long> usingDialInfoIdLiveData = new MutableLiveData<>();
    public MutableLiveData<List<CustomDialInfo>> dialInfoListLiveData = new MutableLiveData<>(new ArrayList<>());
    private volatile static CustomDialManager instance;
    private List<CustomDialInfo> mDialInfoList = new ArrayList<>();

    private Map<String, Long> usingIdMap = new HashMap<>();//当前使用的自定义表盘。如果当前表盘信息不是之前用的那个表盘
    private Context mContext;
    protected String tag = getClass().getSimpleName();

    public static CustomDialManager getInstance() {
        return instance;
    }

    private BmpConvert mBmpConvert;


    public static CustomDialManager init(Context context) {
        if (null == instance) {
            synchronized (CustomDialManager.class) {
                if (null == instance) {
                    instance = new CustomDialManager(context);
                }
            }
        }
        return instance;
    }

    public CustomDialManager(Context context) {
        mContext = context;
        mBmpConvert = new BmpConvert();
        SharedPreferences sharedPreferences = PreferencesHelper.getSharedPreferences(HealthApplication.getAppViewModel().getApplication());
        String listString = sharedPreferences.getString(KEY_CUSTOM_DIAL_LIST, "");
        if (!TextUtils.isEmpty(listString)) {
            mDialInfoList = new Gson().fromJson(listString, new TypeToken<List<CustomDialInfo>>() {
            }.getType());
        }
        String usingListStr = sharedPreferences.getString(KEY_CUSTOM_DIAL_USE_IDS, "");
        if (!TextUtils.isEmpty(usingListStr)) {
            usingIdMap = new Gson().fromJson(usingListStr, new TypeToken<Map<String, Long>>() {
            }.getType());
        }
        getWatchManager().registerOnWatchCallback(new OnWatchCallback() {
            @Override
            public void onWatchSystemInit(int code) {
                super.onWatchSystemInit(code);
                if (code == 0) {
                    refreshCurrentUsingId();
                    refreshDialInfoList();
                }
            }
        });
    }

    public boolean isCallWorkState() {
        DeviceInfo deviceInfo = getWatchManager().getDeviceInfo(getWatchManager().getConnectedDevice());
        return deviceInfo != null && deviceInfo.getPhoneStatus() == WatchConstant.DEVICE_PHONE_STATUS_CALLING;
    }

    public int getWatchWidth() {
        if (getWatchManager().getConnectedDevice() == null) {
            return 0;
        }
        int width = 240;
        ExternalFlashMsgResponse flashMsg = getWatchManager().getExternalFlashMsg(getWatchManager().getConnectedDevice());
        if (flashMsg != null && flashMsg.getScreenWidth() > 0) {
            width = flashMsg.getScreenWidth();
        }
        return width;
    }

    public int getWatchHeight() {
        if (getWatchManager().getConnectedDevice() == null) {
            return 0;
        }
        int height = 280;
        ExternalFlashMsgResponse flashMsg = getWatchManager().getExternalFlashMsg(getWatchManager().getConnectedDevice());
        if (flashMsg != null && flashMsg.getScreenHeight() > 0) {
            height = flashMsg.getScreenHeight();
        }
        return height;
    }

    private WatchManager getWatchManager() {
        return WatchManager.getInstance();
    }

    /**
     * 传输自定义表盘
     *
     * @param info                       当前使用的表盘信息
     * @param path                       图像文件路径(选取范围裁剪后的图片)
     * @param isAutoGenerateCustomBgName 是否自动生成表盘背景名。true:按照当前表盘信息自动生成背景名，false:根据文件路径来生成背景名
     * @param customDialInfo             是否已有自定义表盘信息()
     * @param srcPath                    原图文件路径(有customDialInfo时不需要)
     */
    public void enableCustomBg(WatchInfo info, String path, boolean isAutoGenerateCustomBgName, int targetWidth, int targetHeight, CustomDialInfo customDialInfo, String srcPath, CustomWatchBgTransferCallback callback) {
        if (null == info || targetWidth == 0 || targetHeight == 0) {
            if (callback != null) {
                callback.onFailed(new BaseError(FatFsErrCode.RES_ERR_PARAM, FatUtil.getFatFsErrorCodeMsg(FatFsErrCode.RES_ERR_PARAM)));
            }
            return;
        }
        String targetPath = HealthUtil.saveScaleBitmap(path, targetWidth, targetHeight, 100);
        JL_Log.i(tag, "enableCustomBg", "targetPath = " + targetPath + ", path : " + path + ", \n" + info);
        if (null == targetPath) {
            if (callback != null) {
                callback.onFailed(new BaseError(FatFsErrCode.RES_NO_PATH, FatUtil.getFatFsErrorCodeMsg(FatFsErrCode.RES_NO_PATH)));
            }
            return;
        }
        if (isCallWorkState()) {
            if (callback != null) {
                callback.onFailed(new BaseError(FatFsErrCode.RES_ERR_BEGIN, HealthApplication.getAppViewModel().getApplication().getString(R.string.call_phone_error_tips)));
            }
            return;
        }
        String outPath;
        if (!isAutoGenerateCustomBgName) {
            outPath = getOutPath(targetPath);
        } else {
            String bgName = getCustomBgName(info.getName());
            JL_Log.d(tag, "enableCustomBg", "bgName : " + bgName);
            outPath = FileUtil.createFilePath(mContext, WATCH_DIRECTORY) + File.separator + bgName;
        }
        JL_Log.i(tag, "enableCustomBg", "outPath = " + outPath);
        DeviceInfo deviceInfo = getWatchManager().getDeviceInfo(getWatchManager().getConnectedDevice());
        if (deviceInfo == null) {
            if (callback != null) {
                callback.onFailed(new BaseError(FatFsErrCode.RES_NOT_READY, "can not read device sdk type"));
            }
            return;
        }
        int type;
        WatchConfigure watchConfigure = getWatchManager().getWatchConfigure(getWatchManager().getConnectedDevice());
        switch (deviceInfo.getSdkType()) {
            case JLChipFlag.JL_CHIP_FLAG_701X_WATCH:
                type = BmpConvert.TYPE_701N_RGB;
                if (watchConfigure != null && watchConfigure.getFunctionOption().isSupportDialExpandInfo()) {
                    type = BmpConvert.TYPE_701N_ARGB;
                }
                break;
            case JLChipFlag.JL_CHIP_FLAG_707N_WATCH:
                type = BmpConvert.TYPE_707N_RGB;
                if (watchConfigure != null && watchConfigure.getFunctionOption().isSupportDialExpandInfo()) {
                    type = BmpConvert.TYPE_707N_ARGB;
                }
                break;
            default:
                type = BmpConvert.TYPE_695N_RBG;
                break;
        }
        Bitmap srcBmp = BitmapFactory.decodeFile(path);
        String tempPath = FileUtil.createFilePath(mContext, WATCH_DIRECTORY) + File.separator + "cut_crop.png";
        Bitmap cropBitmap = getCropBitmap(srcBmp);//裁剪图片
        BitmapUtil.bitmapToFile(cropBitmap, tempPath, 100);
        Bitmap fillBitmap = getFillBitmap(cropBitmap);//填充图片
        String fillBitmapPath = FileUtil.createFilePath(mContext, WATCH_DIRECTORY) + File.separator + "cut_fill.png";
        BitmapUtil.bitmapToFile(fillBitmap, fillBitmapPath, 100);
        cropBitmap.recycle();
        fillBitmap.recycle();
        mBmpConvert.bitmapConvert(type, fillBitmapPath, outPath, new OnConvertListener() {
            @Override
            public void onStart(String path) {
                JL_Log.d(tag, "enableCustomBg", "bitmapConvert#onStart ---> path = " + path);
                if (callback != null) {
                    callback.onTransferCustomWatchBgStart(outPath);
                }
            }

            @Override
            public void onStop(boolean result, final String output) {
                JL_Log.w(tag, "enableCustomBg", "bitmapConvert#onStop ---> result = " + result + ", output = " + output);
                if (result) {
                    getWatchManager().addFatFile(output, true, new OnFatFileProgressListener() {
                        @Override
                        public void onStart(String filePath) {
                        }

                        @Override
                        public void onProgress(float progress) {
                            if (progress >= 100) {
                                progress = 99.9f;
                            }
                            if (callback != null) {
                                callback.onTransferCustomWatchBgProgress(progress);
                            }
                        }

                        @Override
                        public void onStop(int result) {
                            if (result == WatchError.ERR_NONE) {
                                if (customDialInfo != null) {//已有自定义表盘信息，更新
                                    String thumbPath = customDialInfo.cutImagePath;
                                    File tempFile = new File(thumbPath);
                                    if (tempFile.exists()) tempFile.delete();//删除旧的文件，否则glide缓存不会更新
                                    long time = Calendar.getInstance().getTimeInMillis();
                                    String cutFileType = ".png";
                                    customDialInfo.updateTime = time;
                                    customDialInfo.cutImagePath = getCustomDialDirectory() + File.separator + "cut" + time + cutFileType;
                                    AppUtil.copyFile(tempPath, customDialInfo.cutImagePath);
                                    updateCustomDial(customDialInfo);
                                }
                                getWatchManager().enableWatchCustomBg(FatUtil.getFatFilePath(output), new OnWatchOpCallback<FatFile>() {
                                    @Override
                                    public void onSuccess(FatFile result) {
                                        CustomDialInfo tempInfo;
                                        if (customDialInfo == null) {
                                            tempInfo = insertNewCustomDialInfo(srcPath, tempPath);
                                        } else {
                                            tempInfo = customDialInfo;
                                        }
                                        if (tempInfo != null) {
                                            setCurrentUsingId(tempInfo.id);
                                            refreshCurrentUsingId();
                                        }
                                        if (callback != null) {
                                            callback.onTransferCustomWatchBgFinish();
                                        }
                                        getWatchManager().getCurrentWatchMsg(new OnWatchOpCallback<WatchInfo>() {
                                            @Override
                                            public void onSuccess(WatchInfo result) {
                                                if (callback != null) {
                                                    callback.onCurrentWatchMsg(result);
                                                }
                                            }

                                            @Override
                                            public void onFailed(BaseError error) {

                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailed(BaseError error) {
                                        if (callback != null) {
                                            callback.onFailed(error);
                                        }
                                    }
                                });
                            } else {
                                if (callback != null) {
                                    callback.onFailed(new BaseError(result, FatUtil.getFatFsErrorCodeMsg(result)));
                                }
                            }
                            if (result == WatchError.ERR_IN_PROGRESS) return; //正在操作的错误码，不处理
                            FileUtil.deleteFile(new File(output));
                        }
                    });
                } else {
                    if (callback != null) {
                        callback.onFailed(new BaseError(FatFsErrCode.RES_NOT_READY, "Image conversion failed."));
                    }
                }
            }

            @Override
            public void onStop(ConvertResult convertResult, String s) {

            }
        });
    }

    public CustomDialInfo insertNewCustomDialInfo(String srcPath, String thumbImagePath) {
        if (srcPath == null || thumbImagePath == null) {
            return null;
        }
        CustomDialInfo customDialInfo = new CustomDialInfo();
        long time = Calendar.getInstance().getTimeInMillis();
        customDialInfo.uid = getUid();
        customDialInfo.mac = getMac();
        customDialInfo.id = time;
        customDialInfo.updateTime = time;
        String srcFileType = srcPath.endsWith(".png") || srcPath.endsWith(".PNG") ? ".png" : ".jpg";
        String cutFileType = thumbImagePath.endsWith(".png") || thumbImagePath.endsWith(".PNG") ? ".png" : ".jpg";
        customDialInfo.srcImagePath = getCustomDialDirectory() + File.separator + "image" + time + srcFileType;
        customDialInfo.cutImagePath = getCustomDialDirectory() + File.separator + "cut" + time + cutFileType;
        AppUtil.copyFile(srcPath, customDialInfo.srcImagePath);
        AppUtil.copyFile(thumbImagePath, customDialInfo.cutImagePath);
        addCustomDial(customDialInfo);
        return customDialInfo;
    }


    /**
     * 添加自定义表盘信息
     */
    public void addCustomDial(CustomDialInfo dialInfo) {
        boolean isContain = false;
        for (int i = 0; i < mDialInfoList.size(); i++) {
            CustomDialInfo temp = mDialInfoList.get(i);
            if (dialInfo.id == temp.id) {
                isContain = true;
                break;
            }
        }
        if (!isContain) {
            mDialInfoList.add(dialInfo);
            updateCache();
            refreshDialInfoList();
        }
    }

    public void updateCustomDial(CustomDialInfo dialInfo) {
        boolean isContain = false;
        for (int i = 0; i < mDialInfoList.size(); i++) {
            CustomDialInfo temp = mDialInfoList.get(i);
            if (dialInfo.id == temp.id) {
                isContain = true;
                mDialInfoList.set(i, dialInfo);
                break;
            }
        }
        if (isContain) {
            updateCache();
            refreshDialInfoList();
        }
    }

    /**
     * 删除自定义表盘信息
     */
    public void deleteCustomDial(CustomDialInfo dialInfo) {
        for (int i = 0; i < mDialInfoList.size(); i++) {
            CustomDialInfo temp = mDialInfoList.get(i);
            if (dialInfo.id == temp.id) {
                String[] paths = new String[]{dialInfo.srcImagePath, dialInfo.cutImagePath};
                for (String path : paths) {
                    if (!TextUtils.isEmpty(path)) {
                        File file = new File(path);
                        if (file.exists()) file.delete();
                    }
                }
                mDialInfoList.remove(temp);
                updateCache();
                refreshDialInfoList();
                break;
            }
        }
    }

    public List<CustomDialInfo> getDialInfoList() {
        return getDialInfoListByMacAndUid(getMac(), getUid());
    }

    private void refreshCurrentUsingId() {
        usingDialInfoIdLiveData.postValue(getCurrentUsingId());
    }

    private void refreshDialInfoList() {
        dialInfoListLiveData.postValue(getDialInfoList());
    }

    private List<CustomDialInfo> getDialInfoListByMacAndUid(String mac, String uid) {
        List<CustomDialInfo> tempList = new ArrayList<>();
        for (CustomDialInfo info : this.mDialInfoList) {
            if (TextUtils.equals(info.uid, uid) && TextUtils.equals(info.mac, mac)) {
                tempList.add(info);
            }
        }
        Collections.sort(tempList, (o1, o2) -> Long.compare(o2.updateTime, o1.updateTime));
//        tempList.sort();
        return tempList;
    }

    public long getCurrentUsingId() {
        String key = getKey();
        Long useId = this.usingIdMap.get(key);
        if (useId == null) return -1;
        return useId;
    }

    public void setCurrentUsingId(long currentUsingId) {
        this.usingIdMap.put(getKey(), currentUsingId);
        PreferencesHelper.putStringValue(HealthApplication.getAppViewModel().getApplication(), KEY_CUSTOM_DIAL_USE_IDS, new Gson().toJson(usingIdMap));
    }

    /**
     * 获取自定义背景文件名
     *
     * @return 自定义背景文件名
     */
    public String getCustomBgName(String dialName) {
        if (HealthConstant.IS_SINGLE_CUSTOM_DIAL_BG) {
            String seq = formatSeq(0);
            if (dialName != null && (dialName.startsWith(WATCH_PREFIX)
                    || dialName.startsWith(WATCH_PREFIX.toLowerCase()))) {
                int start = WATCH_PREFIX.length();
                int end = dialName.getBytes().length;
                if (end > start) {
                    seq = dialName.substring(start, end);
                }
                try {
                    seq = formatSeq(Integer.parseInt(seq));
                } catch (Exception e) {
                    e.printStackTrace();
                    seq = formatSeq(0);
                }
            }
            return CUSTOM_BG_PREFIX + seq /*+ PNG_FORMAT*/;
        }
        List<FatFile> fatFileList = getWatchManager().devFatFileList;
        if (fatFileList == null || fatFileList.isEmpty())
            return CUSTOM_BG_PREFIX + formatSeq(0) /*+ PNG_FORMAT*/;
        int seq = 0;
        for (FatFile fatFile : fatFileList) {
            if (null == fatFile) continue;
            String filename = fatFile.getName();
            if (null == filename) continue;
            filename = filename.toLowerCase();
            if (filename.startsWith(CUSTOM_BG_PREFIX)) {
                String fileSeq = filename.replaceAll(CUSTOM_BG_PREFIX, "");
                try {
                    seq = Integer.parseInt(fileSeq);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                seq++;
            }
        }
        return CUSTOM_BG_PREFIX + formatSeq(seq) /*+ PNG_FORMAT*/;
    }

    private String formatSeq(int seq) {
        if (seq < 10) {
            return "00" + seq;
        } else if (seq < 100) {
            return "0" + seq;
        } else {
            return String.valueOf(seq);
        }
    }

    /**
     * 删除设备的自定义表盘记录
     */
    public void deleteAllCustomDialInfo(String uid, String mac) {
        for (CustomDialInfo info : this.mDialInfoList) {
            if (TextUtils.equals(info.uid, uid) && TextUtils.equals(info.mac, mac)) {
                this.mDialInfoList.remove(info);
            }
        }
        this.usingIdMap.remove(getKey());
        PreferencesHelper.putStringValue(HealthApplication.getAppViewModel().getApplication(), KEY_CUSTOM_DIAL_USE_IDS, new Gson().toJson(usingIdMap));
    }

    private void updateCache() {
        PreferencesHelper.putStringValue(HealthApplication.getAppViewModel().getApplication(), KEY_CUSTOM_DIAL_LIST, new Gson().toJson(mDialInfoList));
    }

    /**
     * 获取自定义表盘的的文件夹
     */
    private String getCustomDialDirectory() {
        String uid = getUid();
        String mac = getMac();
        String directoryName = CUSTOM_DIRECTORY_DEFAULT;
        if (!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(mac)) {
            directoryName = CUSTOM_DIRECTORY_PREFIX + getKey();
        }
        return FileUtil.createFilePath(mContext, WATCH_DIRECTORY, directoryName);
    }

    private void clearCacheTempImage() {

    }

    private String getKey() {
        return "uid_" + getUid() + "mac_" + getMac();
    }

    private String getMac() {
        final BluetoothDevice device = getWatchManager().getConnectedDevice();
        if (null == device) return "";
        return device.getAddress().replaceAll(":", "");
    }

    private String getUid() {
        return HealthApplication.getAppViewModel().getUid();
    }

    private boolean isImageJPG(Uri uri) {
        String mime = mContext.getContentResolver().getType(uri);
        if (mime != null && mime.equals("image/jpeg")) {
            return true; // JPEG 格式
        } else {
            return false;
        }
    }

    private String getOutPath(String path) {
        int index = path.lastIndexOf(".");
        if (index != -1) return path.substring(0, index);
        return path;
    }

    /**
     * 获取一个根据设备屏幕形状裁剪后的bitmap
     */
    private Bitmap getCropBitmap(Bitmap srcBmp) {
        final DialExpandInfo dialExpandInfo = getDialExpandInfo();
        if (dialExpandInfo != null) {
            final int shape = dialExpandInfo.getShape();
            if (shape > 0 && shape != DialExpandInfo.SHAPE_RECTANGLE) {
                if (null != srcBmp) {
                    Bitmap destBmp;
                    if (shape == DialExpandInfo.SHAPE_CIRCULAR) {
                        destBmp = BitmapUtil.clipCircleBitmap(srcBmp, true) /*BitmapUtil.clipCircleAndFillBitmap(srcBmp, 0xffffffff)*/;
                    } else {
                        destBmp = BitmapUtil.clipRoundBitmap(srcBmp, dialExpandInfo.getRadius(), true) /*BitmapUtil.clipRoundAndFillBitmap(srcBmp, dialExpandInfo.getRadius(), 0xffffffff)*/;
                    }
                    return destBmp;
                }
            }
        }
        return srcBmp;
    }

    private Bitmap getFillBitmap(Bitmap srcBmp) {
        final DialExpandInfo dialExpandInfo = getDialExpandInfo();
        if (dialExpandInfo != null) {
            final int shape = dialExpandInfo.getShape();
            if (shape > 0 && shape != DialExpandInfo.SHAPE_RECTANGLE) {
                if (null != srcBmp) {
                    Bitmap destBmp;
                    destBmp = BitmapUtil.fillBitmap(srcBmp, dialExpandInfo.getColor());
                    return destBmp;
                }
            }
        }
        return srcBmp;
    }

    private DialExpandInfo getDialExpandInfo() {
        final WatchManager watchManager = WatchManager.getInstance();
        final WatchConfigure watchConfigure = watchManager.getWatchConfigure(watchManager.getConnectedDevice());
        if (null == watchConfigure) return null;
        return watchConfigure.getDialExpandInfo();
    }
}
