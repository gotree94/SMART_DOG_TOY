package com.jieli.healthaide.ui.device.watch;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.jieli.bmp_convert.BmpConvert;
import com.jieli.component.utils.FileUtil;
import com.jieli.healthaide.tool.customdial.CustomDialInfo;
import com.jieli.healthaide.tool.customdial.CustomDialManager;
import com.jieli.healthaide.tool.customdial.CustomWatchBgTransferCallback;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.healthaide.ui.device.bean.WatchOpData;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.jl_fatfs.FatFsErrCode;
import com.jieli.jl_fatfs.interfaces.OnFatFileProgressListener;
import com.jieli.jl_fatfs.model.FatFile;
import com.jieli.jl_fatfs.utils.FatUtil;
import com.jieli.jl_rcsp.constant.RcspErrorCode;
import com.jieli.jl_rcsp.constant.WatchConstant;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.response.ExternalFlashMsgResponse;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.jl_rcsp.util.RcspUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomWatchBgViewModel extends WatchViewModel {
    private final static String TAG = CustomWatchBgViewModel.class.getSimpleName();
    public WatchInfo mWatchInfo;
    private final BmpConvert mBmpConvert;

    public File mPhotoSavePath;
    public Uri mCameraUri;

    public final MutableLiveData<CustomBgStatus> mCustomBgStatusMLD = new MutableLiveData<>();
    public final MutableLiveData<WatchInfo> mCurrentWatchMLD = new MutableLiveData<>();
    public final MutableLiveData<WatchOpData> mWatchOpDataMLD = new MutableLiveData<>();
    public final MutableLiveData<Boolean> mChangeWatchMLD = new MutableLiveData<>();

    private final static String WATCH_PREFIX = "WATCH";
    private final static String CUSTOM_BG_PREFIX = "bgp_w";
    private final static String JPG_FORMAT = ".jpg";
    private final static String PNG_FORMAT = ".png";

    private final Observer<ArrayList<WatchInfo>> dialListObserver = watchInfos -> {
        for (int i = 0; i < watchInfos.size(); i++) {
            WatchInfo watchInfo = watchInfos.get(i);
            if (watchInfo.getStatus() == WatchInfo.WATCH_STATUS_USING) {
                postCurrentWatchInfo(watchInfo);
                break;
            }
        }
    };

    public CustomWatchBgViewModel() {
        mBmpConvert = new BmpConvert();
        mWatchListMLD.observeForever(dialListObserver);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mWatchListMLD.removeObserver(dialListObserver);
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
            return CUSTOM_BG_PREFIX + seq + PNG_FORMAT;
        }
        List<FatFile> fatFileList = mWatchManager.devFatFileList;
        if (fatFileList == null || fatFileList.isEmpty())
            return CUSTOM_BG_PREFIX + formatSeq(0) + PNG_FORMAT;
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
        return CUSTOM_BG_PREFIX + formatSeq(seq) + PNG_FORMAT;
    }

    /**
     * 使能自定义背景
     *
     * <p>
     * 实现流程<br>
     * Step0. 缩放图像到指定尺寸
     * Step1. 压缩图像成为表盘背景格式
     * Step2. 添加自定义背景文件
     * Step3. 绑定自定义背景
     * </p>
     *
     * @param path         图像文件路径
     * @param targetWidth  指定宽度
     * @param targetHeight 指定高度
     */
    public void enableCustomBg(String path, String srcPath, int targetWidth, int targetHeight) {
        enableCustomBg(path, targetWidth, targetHeight, null, srcPath);
    }

    public void enableCustomBg(String path, int targetWidth, int targetHeight, CustomDialInfo customDialInfo, String srcPath) {
        final CustomBgStatus status = mCustomBgStatusMLD.getValue();
        if (status != null && status.getStatus() != CustomBgStatus.STATUS_END) { //正在操作中
            JL_Log.i(TAG, "enableCustomBg", "In operation.");
            return;
        }
        postCustomBgStart(path); //提前回调开始状态，避免重复操作
        CustomDialManager.getInstance().enableCustomBg(mWatchInfo, path, true,
                targetWidth, targetHeight, customDialInfo, srcPath, new CustomWatchBgTransferCallback() {
                    @Override
                    public void onFailed(BaseError error) {
                        postCustomBgFail(path, error.getSubCode(), error.getMessage());
                    }

                    @Override
                    public void onTransferCustomWatchBgStart(String path) {

                    }

                    @Override
                    public void onTransferCustomWatchBgProgress(float progress) {
                        postCustomBgProgress(progress);
                    }

                    @Override
                    public void onTransferCustomWatchBgFinish() {
                        postCustomBgFinish(path);
                    }

                    @Override
                    public void onCurrentWatchMsg(WatchInfo watchInfo) {
                        postCurrentWatchInfo(watchInfo);
                    }
                });
    }

    public void restoreCustomBg() {
        final WatchInfo info = mWatchInfo;
        if (null == info) return;
        if (!info.hasCustomBgFatPath()) {
            postDeleteCustomBgEnd("restoreCustomBg", FatFsErrCode.RES_OK);
            return;
        }
        deleteCustomBg(info.getCustomBgFatPath());
    }

    public int getWatchWidth() {
        if (mWatchManager.getConnectedDevice() == null) {
            return 0;
        }
        int width = 240;
        ExternalFlashMsgResponse flashMsg = mWatchManager.getExternalFlashMsg(mWatchManager.getConnectedDevice());
        if (flashMsg != null && flashMsg.getScreenWidth() > 0) {
            width = flashMsg.getScreenWidth();
        }
        return width;
    }

    public int getWatchHeight() {
        if (mWatchManager.getConnectedDevice() == null) {
            return 0;
        }
        int height = 280;
        ExternalFlashMsgResponse flashMsg = mWatchManager.getExternalFlashMsg(mWatchManager.getConnectedDevice());
        if (flashMsg != null && flashMsg.getScreenHeight() > 0) {
            height = flashMsg.getScreenHeight();
        }
        return height;
    }

    private void deleteCustomBg(String fatFilePath) {
        mWatchManager.enableWatchCustomBg(WatchConstant.DEFAULT_DIAL_BACKGROUND_NAME, new OnWatchOpCallback<FatFile>() {
            @Override
            public void onSuccess(FatFile result) {
                mWatchManager.deleteWatchFile(fatFilePath, new OnFatFileProgressListener() {
                    @Override
                    public void onStart(String filePath) {
                        postDeleteCustomBgStart(filePath);
                    }

                    @Override
                    public void onProgress(float progress) {
                        postDeleteCustomBgProgress(progress);
                    }

                    @Override
                    public void onStop(int result) {
                        if (result == FatFsErrCode.RES_OK) {
                            mWatchManager.updateWatchFileListByDevice(new OnWatchOpCallback<ArrayList<WatchInfo>>() {
                                @Override
                                public void onSuccess(ArrayList<WatchInfo> result) {
                                    final WatchInfo cacheInfo = mWatchInfo;
                                    if (null != cacheInfo) {
                                        postCurrentWatchInfo(mWatchManager.getWatchInfoByFatFile(cacheInfo.getWatchFile()));
                                    }
                                    postDeleteCustomBgEnd("deleteCustomBg", FatFsErrCode.RES_OK);
                                }

                                @Override
                                public void onFailed(BaseError error) {
                                    postDeleteCustomBgEnd("deleteCustomBg#updateWatchFileListByDevice", error.getSubCode());
                                }
                            });
                        } else {
                            postDeleteCustomBgEnd("deleteCustomBg#deleteWatchFile", result);
                        }
                    }
                });
            }

            @Override
            public void onFailed(BaseError error) {
                postDeleteCustomBgEnd("deleteCustomBg#enableWatchCustomBg", error.getSubCode());
            }
        });
    }

    public void destroy() {
        mBmpConvert.release();
    }

    private boolean isSameWatchInfo(WatchInfo info) {
        if (null == info) return false;
        final WatchInfo watchInfo = mWatchInfo;
        if (null == watchInfo) return false;
        return info.getWatchFile() != null && watchInfo.getWatchFile() != null
                && info.getWatchFile().getPath().equals(watchInfo.getWatchFile().getPath());
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

    private String getOutPath(String path) {
        int index = path.lastIndexOf(".");
        if (index != -1) return path.substring(0, index);
        return path;
    }

    private CustomBgStatus getCustomBgStatus() {
        CustomBgStatus status = mCustomBgStatusMLD.getValue();
        if (status == null) {
            status = new CustomBgStatus();
        }
        return status;
    }

    private void postCustomBgStart(String filePath) {
        mCustomBgStatusMLD.setValue(getCustomBgStatus()
                .setStatus(CustomBgStatus.STATUS_START)
                .setFilePath(FatUtil.getFatFilePath(filePath)));
    }

    private void postCustomBgProgress(float progress) {
        mCustomBgStatusMLD.setValue(getCustomBgStatus()
                .setStatus(CustomBgStatus.STATUS_PROGRESS)
                .setProgress(progress));
    }

    private void postCustomBgFinish(String filePath) {
        if (null != filePath && !filePath.isEmpty()) {
            FileUtil.deleteFile(new File(filePath));
        }
        mCustomBgStatusMLD.setValue(getCustomBgStatus()
                .setStatus(CustomBgStatus.STATUS_END)
                .setResult(true));
    }

    private void postCustomBgFail(String filePath, int code, String message) {
        JL_Log.w(TAG, "postCustomBgFail", RcspUtil.formatString("filePath : %s, \ncode :%s, %s.",
                filePath, code, message));
        if ((null != filePath && !filePath.isEmpty())
                && code != RcspErrorCode.ERR_REMOTE_NOT_CONNECT
        ) {
            FileUtil.deleteFile(new File(filePath));
        }
        mCustomBgStatusMLD.setValue(getCustomBgStatus()
                .setStatus(CustomBgStatus.STATUS_END)
                .setResult(false)
                .setCode(code)
                .setMessage(message));
    }

    @NonNull
    private WatchOpData getWatchOpData() {
        WatchOpData opData = mWatchOpDataMLD.getValue();
        if (opData == null) {
            opData = new WatchOpData();
        }
        return opData;
    }

    private void postDeleteCustomBgStart(String filePath) {
        mWatchOpDataMLD.setValue(getWatchOpData()
                .setOp(WatchOpData.OP_DELETE_FILE)
                .setState(WatchOpData.STATE_START)
                .setFilePath(filePath));
    }

    private void postDeleteCustomBgProgress(float progress) {
        mWatchOpDataMLD.setValue(getWatchOpData().setOp(WatchOpData.OP_DELETE_FILE)
                .setState(WatchOpData.STATE_PROGRESS)
                .setProgress(progress));
    }

    private void postDeleteCustomBgEnd(String method, int result) {
        JL_Log.w(TAG, "postDeleteCustomBgEnd", RcspUtil.formatString("(%s) ---> result : %d(0x%X).",
                method, result, result));
        mWatchOpDataMLD.setValue(getWatchOpData().setOp(WatchOpData.OP_DELETE_FILE)
                .setState(WatchOpData.STATE_END)
                .setResult(result));
    }

    private void postCurrentWatchInfo(WatchInfo info) {
        if (null == info) return;
        JL_Log.d(TAG, "postCurrentWatchInfo", "" + info);
        mWatchInfo = info;
        mCurrentWatchMLD.postValue(info);
        if (!isSameWatchInfo(info)) {
            mChangeWatchMLD.postValue(true);
        }
    }

    public static class CustomBgStatus {
        private int status;
        private float progress;
        private boolean result;
        private int code;
        private String message;
        private String filePath;

        public final static int STATUS_END = 0;
        public final static int STATUS_START = 1;
        public final static int STATUS_PROGRESS = 2;

        public int getStatus() {
            return status;
        }

        public CustomBgStatus setStatus(int status) {
            this.status = status;
            return this;
        }

        public float getProgress() {
            return progress;
        }

        public CustomBgStatus setProgress(float progress) {
            this.progress = progress;
            return this;
        }

        public boolean isResult() {
            return result;
        }

        public CustomBgStatus setResult(boolean result) {
            this.result = result;
            return this;
        }

        public int getCode() {
            return code;
        }

        public CustomBgStatus setCode(int code) {
            this.code = code;
            return this;
        }

        public String getMessage() {
            return message;
        }

        public CustomBgStatus setMessage(String message) {
            this.message = message;
            return this;
        }

        public String getFilePath() {
            return filePath;
        }

        public CustomBgStatus setFilePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        @NonNull
        @Override
        public String toString() {
            return "CustomBgStatus{" +
                    "status=" + status +
                    ", progress=" + progress +
                    ", result=" + result +
                    ", code=" + code +
                    ", message='" + message + '\'' +
                    ", filePath='" + filePath + '\'' +
                    '}';
        }
    }
}