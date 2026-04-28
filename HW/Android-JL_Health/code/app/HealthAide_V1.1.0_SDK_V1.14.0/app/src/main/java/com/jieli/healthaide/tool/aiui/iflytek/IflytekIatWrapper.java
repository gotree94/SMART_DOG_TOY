package com.jieli.healthaide.tool.aiui.iflytek;

import android.content.Context;

import androidx.core.util.Consumer;

import com.iflytek.sparkchain.core.asr.ASR;
import com.iflytek.sparkchain.core.asr.AsrCallbacks;
import com.iflytek.sparkchain.utils.constants.ErrorCode;
import com.jieli.healthaide.tool.aiui.model.OpResult;
import com.jieli.healthaide.tool.aiui.model.StateResult;

/**
 * @ClassName: IflytekIatWrapper
 * @Description: 科大讯飞语音识别功能实现
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/16 19:35
 */
public class IflytekIatWrapper extends BasicWrapper<Void, ASR.ASRResult> {

    /**
     * 语言识别对象
     */
    private final ASR mAsr;

    public IflytekIatWrapper(Context context) throws RuntimeException {
        super(context);
        mAsr = new ASR();
        config();
        mAsr.registerCallbacks(new AsrCallbacks() {
            @Override
            public void onResult(ASR.ASRResult asrResult, Object userTag) {
                if (!isSameTag(userTag)) return;
                handleAsrResult(asrResult);
            }

            @Override
            public void onError(ASR.ASRError asrError, Object userTag) {
                if (!isSameTag(userTag)) return;
                handleAsrError(asrError);
            }
        });
    }

    @Override
    public void destroy() {
        cancel();
        mAsr.registerCallbacks(null);
        super.destroy();
    }

    @Override
    public int getType() {
        return FUNCTION_ASR;
    }

    @Override
    public boolean isRunning() {
        return mStatus == STATUS_WORKING;
    }

    @Override
    public void execute(Void input, Consumer<StateResult<ASR.ASRResult>> callback) {
        if (isRunning()) {
            cancel();
        }
        setCallback(callback);
        callbackStart();
        int ret = mAsr.start(autoIncUserTag());
        if (ret != 0) {
            callbackFinish("execute", ret, "Operation failed. code : " + ret, null);
        }
    }

    @Override
    public void stop() {
        if (!isRunning()) return;
        mAsr.stop(false);
    }

    @Override
    public void cancel() {
        if (!isRunning()) return;
        mAsr.stop(true);
        callbackFinish("cancel", OpResult.ERR_NONE, "User cancels operation.", null);
    }

    /**
     * 写入听写数据
     */
    public void writeAudio(byte[] buff) {
        if (null == buff || buff.length == 0 || !isRunning()) return;
        mAsr.write(buff);
    }

    private void config() {
        String language = "zh_cn";
        mAsr.language(language);//语种，zh_cn:中文，en_us:英文。其他语种参见集成文档
        mAsr.domain("iat");//应用领域,iat:日常用语。其他领域参见集成文档
        mAsr.accent("mandarin");//方言，mandarin:普通话。方言仅当language为中文时才会生效。其他方言参见集成文档。
        mAsr.vinfo(true);//返回子句结果对应的起始和结束的端点帧偏移值。
        /*if ("zh_cn".equals(language))*/
        {
            mAsr.dwa("wpgs");//动态修正
        }
        mAsr.ptt(false); //是否自动添加标点符号，默认开启。
    }

    private void handleAsrResult(ASR.ASRResult result) {
        if (!isRunning()) return;
        //以下信息需要开发者根据自身需求，如无必要，可不需要解析执行。
        int begin = result.getBegin();         //识别结果所处音频的起始点
        int end = result.getEnd();           //识别结果所处音频的结束点
        int status = result.getStatus();        //结果数据状态，0：识别的第一块结果,1：识别中间结果,2：识别最后一块结果
        String asrText = result.getBestMatchText(); //识别结果
        String sid = result.getSid();           //sid

        if (status == 2) {
            mAsr.stop(false);
            callbackFinish("handleAsrResult", OpResult.ERR_NONE, "", result);
        } else {
            callbackWorking("handleAsrResult", 0f, result);
        }
    }

    private void handleAsrError(ASR.ASRError error) {
        if (!isRunning()) return;
        int code = error.getCode();
        String msg = error.getErrMsg();
        String sid = error.getSid();

        callbackFinish("handleAsrError", code, msg, null);
    }
}
