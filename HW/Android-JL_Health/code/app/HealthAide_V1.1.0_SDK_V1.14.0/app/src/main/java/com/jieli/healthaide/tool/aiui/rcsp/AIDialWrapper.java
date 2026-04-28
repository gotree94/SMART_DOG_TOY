package com.jieli.healthaide.tool.aiui.rcsp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.jieli.bmp_convert.BmpConvert;
import com.jieli.bmp_convert.ConvertResult;
import com.jieli.bmp_convert.OnConvertListener;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.tool.customdial.CustomDialManager;
import com.jieli.healthaide.tool.customdial.CustomWatchBgTransferCallback;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.tool.watch.synctask.WatchListSyncTask;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.healthaide.util.AppUtil;
import com.jieli.healthaide.util.BitmapUtil;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_fatfs.FatFsErrCode;
import com.jieli.jl_fatfs.interfaces.OnFatFileProgressListener;
import com.jieli.jl_rcsp.constant.AttrAndFunCode;
import com.jieli.jl_rcsp.constant.Command;
import com.jieli.jl_rcsp.constant.JLChipFlag;
import com.jieli.jl_rcsp.constant.RcspConstant;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.interfaces.data.OnDataEventCallback;
import com.jieli.jl_rcsp.interfaces.rcsp.RcspCommandCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.base.CommandBase;
import com.jieli.jl_rcsp.model.command.ai.AIOperateCmd;
import com.jieli.jl_rcsp.model.data.SendParams;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.model.device.settings.v0.DialExpandInfo;
import com.jieli.jl_rcsp.model.parameter.AIOperateParam;
import com.jieli.jl_rcsp.model.response.ExternalFlashMsgResponse;
import com.jieli.jl_rcsp.tool.callback.BaseCallbackManager;
import com.jieli.jl_rcsp.util.CommandBuilder;
import com.jieli.jl_rcsp.util.JL_Log;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @ClassName: AIDialWrapper
 * @Description: AI表盘
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/9/27 20:07
 */
public class AIDialWrapper extends AIDialListener {
    //    public static final String KEY_AI_DIAL_STYLE = "key_ai_dial_style";
    private final String TAG = this.getClass().getSimpleName();
    private final int AISupplier = 1;//ai供应商，0:杰理,1：科大讯飞
    private final CallbackManager mCallbackManager;
    private final String THUMB_PATH = "/AITHUMB";
    private final static String WATCH_PREFIX = "WATCH";
    private final static String CUSTOM_BG_PREFIX = "bgp_w";
    private final static String JPG_FORMAT = ".jpg";
    private BmpConvert mBmpConvert;
    private WatchInfo mWatchInfo;
    private String dialImagePath;//已裁剪的表盘背景图
    private String mPainStyle;
    private volatile boolean isSendData = false;
    private final WatchManager mWatchManager;
    private final LinkedBlockingQueue<SendTaskParam> mSendTaskQueue = new LinkedBlockingQueue<>();
    private final int MSG_SET_PAIN_STYLE = 101;
    private DialListBroadcastReceiver mDialListBroadcastReceiver;
    private int thumbWidth = 240;
    private int thumbHeight = 240;
    private final Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SET_PAIN_STYLE:
                    WatchConfigure configure = mWatchManager.getWatchConfigure(mWatchManager.getConnectedDevice());
                    boolean isSupportAIDial = configure == null || configure.getFunctionOption() != null && configure.getFunctionOption().isSupportAIDial();
                    if (isSupportAIDial) {
                        notifyDevPaintStyle(mPainStyle);
                    }
                    break;
            }
        }
    };
    private final OnWatchCallback mWatchCallback = new OnWatchCallback() {
        @Override
        public void onWatchSystemInit(int code) {
            super.onWatchSystemInit(code);
            mUIHandler.removeMessages(MSG_SET_PAIN_STYLE);
            mUIHandler.sendEmptyMessageDelayed(MSG_SET_PAIN_STYLE, 1000);
        }

        @Override
        public void onRcspCommand(BluetoothDevice device, CommandBase command) {
            super.onRcspCommand(device, command);
            if (command.getId() == Command.CMD_AI_OPERATE) {
                AIOperateCmd aiOperateCmd = (AIOperateCmd) command;
                AIOperateParam param = aiOperateCmd.getParam();
                if (param.getOp() == AttrAndFunCode.AI_OP_AI_DIAL) {
                    switch (param.getFlag()) {
                        case AttrAndFunCode.AI_DIAL_OP_UI://设备通知AppAI表盘界面变化
                            Integer state = param.getAiDialFunUIState();
                            Integer scaleZoomHeight = param.getScaleZoomHeight();
                            Integer scaleZoomWidth = param.getScaleZoomWidth();
                            if (state != null) {
                                onDevNotifyAIDialUIChange(state);
                            }
                            if (scaleZoomHeight != null) {
                                thumbHeight = scaleZoomHeight;
                            } else {
                                thumbHeight = 240;
                            }
                            if (scaleZoomWidth != null) {
                                thumbWidth = scaleZoomWidth;
                            } else {
                                thumbWidth = 240;
                            }
                            break;
                        case AttrAndFunCode.AI_DIAL_OP_GENERATE_DIAL://开始AI生成表盘
                            onGenerateDial();
                            break;
                        case AttrAndFunCode.AI_DIAL_OP_RECORDING_AGAIN://重新录音
                            onRecordingAgain();
                            break;
                        case AttrAndFunCode.AI_DIAL_OP_INSTALL_DIAL://开始安装表盘
                            onInstallDialStart();
                            transferCustomDial(dialImagePath);
                            break;
                        case AttrAndFunCode.AI_DIAL_OP_REGENERATE_DIAL://重新生成表盘
                            onReGenerateDial();
                            break;
                    }
                    aiOperateCmd.setParam(new AIOperateParam.AIOperateResultParam(0));
                    aiOperateCmd.setStatus(StateCode.STATUS_SUCCESS);
                    mWatchManager.sendCommandResponse(mWatchManager.getConnectedDevice(), aiOperateCmd, null);
                }
            }
        }

        @Override
        public void onCurrentWatchInfo(BluetoothDevice device, String fatFilePath) {
            super.onCurrentWatchInfo(device, fatFilePath);
            JL_Log.d(TAG, "onCurrentWatchInfo", "fatFilePath : " + fatFilePath);
            mWatchInfo = mWatchManager.getWatchInfoByPath(fatFilePath);
        }
    };

    @SuppressLint("WrongConstant")
    public AIDialWrapper(WatchManager watchManager) {
        mWatchManager = watchManager;
        mWatchManager.registerOnWatchCallback(mWatchCallback);
        mBmpConvert = new BmpConvert();
        mCallbackManager = new CallbackManager();
        mDialListBroadcastReceiver = new DialListBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WatchListSyncTask.INTENT_ACTION_WATCH_LIST);
        final Context context = HealthApplication.getAppViewModel().getApplication();
        ContextCompat.registerReceiver(context, mDialListBroadcastReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED);
    }

    public void release() {
        if (mWatchManager != null) {
            mWatchManager.unregisterOnWatchCallback(mWatchCallback);
        }
        mBmpConvert.release();
        mCallbackManager.release();
        HealthApplication.getAppViewModel().getApplication().unregisterReceiver(mDialListBroadcastReceiver);
    }

    public void registerListener(AIDialListener listener) {
        mCallbackManager.registerCallback(listener);
    }

    public void unregisterListener(AIDialListener listener) {
        mCallbackManager.unregisterCallback(listener);
    }

    public String getPainStyle() {
        return mPainStyle;
    }

    public void setPaintStyle(String paintStyle) {
        mPainStyle = paintStyle;
        notifyDevPaintStyle(paintStyle);
    }

    /**
     * 通知设备AI表盘当前绘画风格
     *
     * @param paintStyle 风格名称（例如：赛博朋克）
     */
    private void notifyDevPaintStyle(String paintStyle) {
        sendCmdToDev(CommandBuilder.buildNotifyAIPaintStyle(paintStyle), null);
    }

    /**
     * 通知设备缩略图传输完毕
     *
     * @param thumbPath 缩略图路径（目前缩略图路径默认使用:"/VIE_THUMB“）
     */
    public void notifyDevAIDialThumbSuccess(String thumbPath) {
        JL_Log.d(TAG, "notifyDevAIDialThumbSuccess", "通知设备缩略图传输完毕");
        sendCmdToDev(CommandBuilder.buildNotifyAIDialThumbSuccess(thumbPath), null);
    }

    /**
     * 获取自定义背景文件名
     *
     * @return 自定义背景文件名
     */
    public String getCustomBgName() {
        if (mWatchInfo == null) return CUSTOM_BG_PREFIX + formatSeq(0) + JPG_FORMAT;
        String watchName = mWatchInfo.getName().toUpperCase();
        if (!watchName.contains(WATCH_PREFIX)) {
            return CUSTOM_BG_PREFIX + formatSeq(0) + JPG_FORMAT;
        }
        int seq = 0;
        if (!watchName.equals(WATCH_PREFIX)) {
            watchName = watchName.replaceAll(WATCH_PREFIX, "");
            try {
                seq = Integer.parseInt(watchName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return CUSTOM_BG_PREFIX + formatSeq(seq) + JPG_FORMAT;
    }

    public String getThumbName() {
        return THUMB_PATH.replaceAll(File.separator, "") + JPG_FORMAT;
    }

    /**
     * ai对话拿到图片
     *
     * @param srcPath 图片资源
     */
    public void handleNlpImage(String srcPath, String tempThumbPath, String tempDialPath) {
        if (!mWatchManager.isConnected()) return;
        File tempThumbFile = new File(tempThumbPath);
        if (tempThumbFile.exists()) {
            tempThumbFile.delete();
        }
        File tempDialFile = new File(tempDialPath);
        if (tempDialFile.exists()) {
            tempDialFile.delete();
        }
        dialImagePath = tempDialPath;
        AppUtil.copyFile(srcPath, tempDialPath);
        AppUtil.copyFile(srcPath, tempThumbPath);
        Bitmap thumb = HealthUtil.createScaleBitmap(tempThumbPath, thumbWidth, thumbHeight);
        thumb = getCropBitmap(thumb);
        BitmapUtil.bitmapToFile(thumb, tempThumbPath, 100);
        thumb.recycle();
        transferThumb(tempThumbPath);
       /* //根据原图，先截取图片，后生成背景图
        File tempThumbFile = new File(tempThumbPath);
        if (tempThumbFile.exists()) {
            tempThumbFile.delete();
        }
        File tempDialFile = new File(tempDialPath);
        if (tempDialFile.exists()) {
            tempDialFile.delete();
        }
        int targetWidth = getWatchWidth();
        int targetHeight = getWatchHeight();
//        int thumbWidth = 240;
//        int thumbHeight = 240;
        float offsetX = 0;
        float offsetY = 0;
        Bitmap bmpSrc = BitmapFactory.decodeFile(srcPath);
        int srcWidth = bmpSrc.getWidth();
        int srcHeight = bmpSrc.getHeight();
        float widthScale = targetWidth * 1.0f / srcWidth;
        float heightScale = targetHeight * 1.0f / srcHeight;
        float scale = Math.min(widthScale, heightScale);
        float rangeWidth = srcWidth;
        float rangeHeight = srcHeight;
        Log.d(TAG, "handleNlpImage: widthScale " + widthScale + " heightScale : " + heightScale + " scale : " + scale);
        //缩小，放大
        if (scale == widthScale) {//图片的宽比例比较大，优先让高进行填满
            Log.d(TAG, "handleNlpImage: widthScale");
            rangeWidth = (targetHeight * 1.0f) / heightScale;
            offsetX = -(srcWidth - rangeWidth) / 2;//还可以优化一下，有轻微误差
        } else {
            Log.d(TAG, "handleNlpImage: heightScale");
            rangeHeight = (targetHeight * 1.0f) / widthScale;
            offsetY = -(srcHeight - rangeHeight) / 2;//还可以优化一下，有轻微误差
            Log.d(TAG, "handleNlpImage: heightScale  " + rangeHeight + "  offsetY : " + offsetY);
        }
        //todo 这里可能不需要去做这一步 缩放和放大图片
        //  裁剪图片
        Bitmap tempDial = createDialBitmap(bmpSrc, offsetX, offsetY, rangeWidth, rangeHeight, targetWidth, targetHeight);
        BitmapUtil.bitmapToFile(tempDial, tempDialPath, 100);
        tempDial.recycle();
//        AppUtil.copyFile(tempDialPath, thumbSavePath);
        dialImagePath = tempDialPath;
        //  生成缩略图,生成缩略图还要缓存到本地
        Bitmap thumb = createThumbnail(bmpSrc, offsetX, offsetY, rangeWidth, rangeHeight, thumbWidth, thumbHeight);
        thumb = getCropBitmap(thumb);
        BitmapUtil.bitmapToFile(thumb, tempThumbPath, 100);
        if (!thumb.isRecycled()) thumb.recycle();
        if (!bmpSrc.isRecycled()) bmpSrc.recycle();
        transferThumb(tempThumbPath);*/
    }

    /**
     * 传输缩略图
     */
    public void transferThumb(String thumbFilePath) {
        JL_Log.d(TAG, "transferThumb", "thumbFilePath : " + thumbFilePath);
        DeviceInfo deviceInfo = mWatchManager.getDeviceInfo(mWatchManager.getConnectedDevice());
        if (deviceInfo == null) {
            return;
        }
        onTransferThumbStart();
        int type;
        WatchConfigure watchConfigure = mWatchManager.getWatchConfigure(mWatchManager.getConnectedDevice());
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
        String outPath = getOutPath(thumbFilePath);
        mBmpConvert.bitmapConvert(type, thumbFilePath, outPath, new OnConvertListener() {
            @Override
            public void onStart(String s) {

            }

            @Override
            public void onStop(boolean result, String s) {
                JL_Log.d(TAG, "transferThumb", "onStop ---> " + result + ", " + s);
                if (result) {
                    mWatchManager.addFatFile(s, true, new OnFatFileProgressListener() {

                        @Override
                        public void onStart(String filePath) {

                        }

                        @Override
                        public void onProgress(float progress) {

                        }

                        @Override
                        public void onStop(int result) {
                            if (result == FatFsErrCode.RES_OK) {
                                notifyDevAIDialThumbSuccess(THUMB_PATH);
                                JL_Log.d(TAG, "transferThumb", "onStop ---> 发完缩略图");
                            } else {
                                JL_Log.e(TAG, "transferThumb", "onStop ---> 发送缩略图异常 ： " + result);
                            }
                            onTransferThumbFinish(result == FatFsErrCode.RES_OK);
                            File tempThumbFile = new File(thumbFilePath);
                            if (tempThumbFile.exists()) {
                                tempThumbFile.delete();
                            }
                            File tempDialFile = new File(s);
                            if (tempDialFile.exists()) {
                                tempDialFile.delete();
                            }
                        }
                    });
                }
            }

            @Override
            public void onStop(ConvertResult convertResult, String s) {

            }
        });
    }

    /**
     * 传输自定义表盘背景
     */
    public void transferCustomDial(String dialImagePath) {
        JL_Log.d(TAG, "transferCustomDial", "开始发送自定义背景");
        enableCustomBgModify(dialImagePath, getWatchWidth(), getWatchHeight());
    }

    /**
     * 取消之前的消息同步
     */
    public void cancelAsyncMessage() {
        for (Object param : mSendTaskQueue.toArray()) {
            SendTaskParam sendTaskParam = (SendTaskParam) param;
            sendTaskParam.setCancel(true);
        }
        mSendTaskQueue.clear();
    }

    /**
     * 消息同步-语音识别
     *
     * @param iatText 识别文本
     */
    public void asyncMessageIat(String iatText, OnDataEventCallback callback) {
        JL_Log.e(TAG, "asyncMessageIat", iatText);
        this.sendTextData(0, iatText, callback);
    }

    /**
     * 消息同步-AI错误提示
     *
     * @param aiErrorText AI错误提示
     */
    public void asyncMessageAIError(String aiErrorText, OnDataEventCallback callback) {
        JL_Log.e(TAG, "asyncMessageAIError", aiErrorText);
        this.sendTextData(2, aiErrorText, callback);
    }

    private void sendCmdToDev(CommandBase commandBase, RcspCommandCallback commandCallback) {
        if (mWatchManager != null) {
            BluetoothDevice connectedDevice = mWatchManager.getConnectedDevice();
            if (connectedDevice != null) {
                mWatchManager.sendRcspCommand(connectedDevice, commandBase, commandCallback);
            } else {
                JL_Log.e(TAG, "sendCmdToDev", "no connected device");
            }
        } else {
            JL_Log.e(TAG, "sendCmdToDev", "RcspOp is release");
        }
    }

    private String getOutPath(String path) {
        if (path.contains(".jpg")) {
            return path.substring(0, path.lastIndexOf(".jpg"));
        } else if (path.contains("\\.")) {
            return path.substring(0, path.lastIndexOf("."));
        } else {
            return path;
        }
    }

    private void enableCustomBgModify(String path, int targetWidth, int targetHeight) {
        CustomDialManager.getInstance().enableCustomBg(mWatchInfo, path, true, targetWidth, targetHeight, null, path, new CustomWatchBgTransferCallback() {
            @Override
            public void onFailed(BaseError error) {
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
            }

            @Override
            public void onTransferCustomWatchBgStart(String path) {

            }

            @Override
            public void onTransferCustomWatchBgProgress(float progress) {

            }

            @Override
            public void onTransferCustomWatchBgFinish() {
                onInstallDialFinish(true);
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
            }

            @Override
            public void onCurrentWatchMsg(WatchInfo watchInfo) {

            }
        });
    }

    private int getWatchWidth() {
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

    private int getWatchHeight() {
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

    @Override
    public void onDevNotifyAIDialUIChange(int state) {
        super.onDevNotifyAIDialUIChange(state);
        mCallbackManager.onDevNotifyAIDialUIChange(state);
    }

    @Override
    public void onGenerateDial() {
        super.onGenerateDial();
        mCallbackManager.onGenerateDial();
    }

    @Override
    public void onInstallDialStart() {
        super.onInstallDialStart();
        mCallbackManager.onInstallDialStart();
    }

    @Override
    public void onInstallDialFinish(boolean isSuccess) {
        super.onInstallDialFinish(isSuccess);
        mCallbackManager.onInstallDialFinish(isSuccess);
    }

    @Override
    public void onTransferThumbStart() {
        super.onTransferThumbStart();
        mCallbackManager.onTransferThumbStart();
    }

    @Override
    public void onTransferThumbFinish(boolean isSuccess) {
        super.onTransferThumbFinish(isSuccess);
        mCallbackManager.onTransferThumbFinish(isSuccess);
    }

    @Override
    public void onReGenerateDial() {
        super.onReGenerateDial();
        mCallbackManager.onReGenerateDial();
    }

    @Override
    public void onRecordingAgain() {
        super.onRecordingAgain();
        mCallbackManager.onRecordingAgain();
    }

    /**
     * 截取目标范围，放大到对应尺寸大小
     */
    private Bitmap createDialBitmap(Bitmap bmpSrc, float offsetX, float offsetY, float rangeWidth, float rangeHeight, int targetWidth, int targetHeight) {
        Bitmap result = null;
        if (bmpSrc != null) {
            int srcWidth = bmpSrc.getWidth();
            int srcHeight = bmpSrc.getHeight();
            boolean isSame = (offsetX == 0 && offsetY == 0) && (srcWidth == targetWidth && srcHeight == targetHeight) && (srcWidth == rangeWidth && srcHeight == rangeHeight);
            if (isSame) {
                result = bmpSrc;
            } else {
                float widthScale = targetWidth * 1.0f / rangeWidth;
                float heightScale = targetHeight * 1.0f / rangeHeight;
                Matrix matrix = new Matrix();
                matrix.postTranslate(offsetX, offsetY);
                matrix.postScale(widthScale, heightScale);
                result = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(result);
                // 如需要可自行设置 Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG 等等
                Paint paint = new Paint();
                paint.setFlags(Paint.ANTI_ALIAS_FLAG);
                canvas.drawBitmap(bmpSrc, matrix, paint);
            }
        }
        return result;
    }

    /**
     * 生成自定义表盘的缩略图
     *
     * @param offsetX     偏移x轴
     * @param offsetY     偏移Y轴
     * @param rangeWidth  选择的范围
     * @param rangeHeight 选择的范围
     * @param thumbWidth  缩略图的宽
     * @param thumbHeight 缩略图的高
     * @return
     */
    private Bitmap createThumbnail(Bitmap bmpSrc, float offsetX, float offsetY, float rangeWidth, float rangeHeight, int thumbWidth, int thumbHeight) {
        return createDialBitmap(bmpSrc, offsetX, offsetY, rangeWidth, rangeHeight, thumbWidth, thumbHeight);
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
     * @param textType //文本类型，0:语音识别，1：Ai应答文本
     */
    private void sendTextData(int textType, String text, OnDataEventCallback callback) {
        byte[] textData = text.getBytes();
        byte[] data = new byte[4 + textData.length];
        int version = 0;//数据格式解析的版号本
        data[0] = (byte) ((version & 0x0f) + ((textType & 0x0f) << 4));
        data[1] = (byte) this.AISupplier;
        data[2] = (byte) (textData.length >> 8 & 255);
        data[3] = (byte) (textData.length & 255);
        System.arraycopy(textData, 0, data, 4, textData.length);
        SendParams param = new SendParams(RcspConstant.TYPE_AI_CLOUD_DATA, RcspConstant.DATA_TRANSFER_VERSION,
                4 * 1024, 4 * 1024, data);
        try {
            mSendTaskQueue.put(new SendTaskParam(param, callback, text));
            JL_Log.d(TAG, "sendTextData", "put task in queue...");
            startSendTask();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startSendTask() {
        if (isSendData) {
            JL_Log.d(TAG, "startSendTask", "Task is running");
            return;
        }
        final SendTaskParam param = mSendTaskQueue.peek();
        if (null == param) {
            JL_Log.d(TAG, "startSendTask", "SendTaskParam is null");
            return;
        }
        isSendData = true;
        JL_Log.d(TAG, "startSendTask", "sendLargeData >>> " + param);
        WatchManager.getInstance().sendLargeData(param.getParam(), new OnDataEventCallback() {
            @Override
            public void onBegin(int way) {
                JL_Log.d(TAG, "startSendTask", "onBegin ---> way : " + way);
                isSendData = true;
                if (param.getCallback() != null && !param.isCancel()) {
                    param.getCallback().onBegin(way);
                }
            }

            @Override
            public void onProgress(float progress) {
                JL_Log.d(TAG, "startSendTask", "onProgress ---> " + progress);
                if (param.getCallback() != null && !param.isCancel()) {
                    param.getCallback().onProgress(progress);
                }
            }

            @Override
            public void onStop(int type, byte[] data) {
                JL_Log.i(TAG, "startSendTask", "onFinish ---> " + param.getText() + ", isCancel: " + param.isCancel());
                isSendData = false;
                mSendTaskQueue.poll();
                if (param.getCallback() != null && !param.isCancel()) {
                    param.getCallback().onStop(type, data);
                }
                startSendTask();
            }

            @Override
            public void onError(BaseError error) {
                JL_Log.e(TAG, "startSendTask", "onError ---> " + error);
                isSendData = false;
                mSendTaskQueue.clear();
                if (param.getCallback() != null && !param.isCancel()) {
                    param.getCallback().onError(error);
                }
            }
        });
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
                        destBmp = BitmapUtil.clipCircleAndFillBitmap(srcBmp, dialExpandInfo.getColor());
                    } else {
                        destBmp = BitmapUtil.clipRoundAndFillBitmap(srcBmp, dialExpandInfo.getRadius(), dialExpandInfo.getColor());
                    }
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

    private class DialListBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), WatchListSyncTask.INTENT_ACTION_WATCH_LIST)) {
                mWatchManager.getCurrentWatchMsg(new OnWatchOpCallback<WatchInfo>() {
                    @Override
                    public void onSuccess(WatchInfo result) {
                        mWatchInfo = result;
                        JL_Log.d(TAG, "getCurrentWatchMsg", "onSuccess: " + result);
                    }

                    @Override
                    public void onFailed(BaseError error) {
                        JL_Log.d(TAG, "getCurrentWatchMsg", "onFailed: " + error);

                    }
                });
            }
        }
    }

    private static class CallbackManager extends BaseCallbackManager<AIDialListener> {

        public void onDevNotifyAIDialUIChange(int state) {
            callbackEvent(callback -> callback.onDevNotifyAIDialUIChange(state));
        }

        public void onGenerateDial() {
            callbackEvent(AIDialListener::onGenerateDial);
        }

        public void onRecordingAgain() {
            callbackEvent(AIDialListener::onRecordingAgain);
        }

        public void onTransferThumbStart() {
            callbackEvent(AIDialListener::onTransferThumbStart);
        }

        public void onTransferThumbFinish(boolean isSuccess) {
            callbackEvent(callback -> callback.onTransferThumbFinish(isSuccess));
        }

        public void onInstallDialStart() {
            callbackEvent(AIDialListener::onInstallDialStart);
        }

        public void onInstallDialFinish(boolean isSuccess) {
            callbackEvent(callback -> callback.onInstallDialFinish(isSuccess));
        }

        public void onReGenerateDial() {
            callbackEvent(AIDialListener::onReGenerateDial);
        }
    }

    private static class SendTaskParam {
        private final SendParams mParam;              //发送参数
        private final OnDataEventCallback mCallback;  //结果回调
        private boolean isCancel = false;//是否取消
        private String text;

        public SendTaskParam(@NonNull SendParams param, OnDataEventCallback callback, String text) {
            mParam = param;
            mCallback = callback;
            this.text = text;
        }

        @NonNull
        public SendParams getParam() {
            return mParam;
        }

        public OnDataEventCallback getCallback() {
            return mCallback;
        }

        public String getText() {
            return text;
        }

        public boolean isCancel() {
            return isCancel;
        }

        public void setCancel(boolean cancel) {
            isCancel = cancel;
        }

        @Override
        public String toString() {
            return "SendTaskParam{" +
                    "mParam=" + mParam +
                    ", mCallback=" + mCallback +
                    '}';
        }
    }
}
