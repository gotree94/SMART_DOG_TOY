package com.jieli.healthaide.tool.aiui.iflytek;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.text.TextUtils;

import androidx.core.util.Consumer;

import com.iflytek.sparkchain.core.tts.OnlineTTS;
import com.iflytek.sparkchain.core.tts.TTS;
import com.iflytek.sparkchain.core.tts.TTSCallbacks;
import com.iflytek.sparkchain.utils.constants.ErrorCode;
import com.jieli.healthaide.tool.aiui.model.OpResult;
import com.jieli.healthaide.tool.aiui.model.StateResult;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ClassName: IflytekTtsWrapper
 * @Description: 科大讯飞语音合成功能实现
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/16 19:35
 */
public class IflytekTtsWrapper extends BasicWrapper<String, TTS.TTSResult> {

    /**
     * 声道数 --- 单声道
     */
    public static final int CHANNEL_NUM = AudioFormat.CHANNEL_OUT_MONO;
    /**
     * 音频格式 --- PCM_16Bit
     */
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    /**
     * 采样率 --- 16kHz
     */
    public static final int SAMPLE_RATE = 16000;

    /**
     * 语言合成对象
     */
    private final OnlineTTS mOnlineTTS;
    /**
     * 音乐播放器
     */
    private final AudioTrack mAudioTrack;
    /**
     * 处理线程
     */
    private final ExecutorService mWorkThread;
    /**
     * 累计数据大小
     */
    private int mDataSize;

    public IflytekTtsWrapper(Context context) throws RuntimeException {
        super(context);
        mOnlineTTS = new OnlineTTS("xiaoyan");
        mAudioTrack = new AudioTrack(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
                new AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setEncoding(AUDIO_FORMAT)
                        .setChannelMask(CHANNEL_NUM)
                        .build(),
                AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_NUM, AUDIO_FORMAT), //缓冲区大小
                AudioTrack.MODE_STREAM, //流模式
                AudioManager.AUDIO_SESSION_ID_GENERATE); //自动生成会话ID
        mWorkThread = Executors.newSingleThreadExecutor();
        config();
        mOnlineTTS.registerCallbacks(new TTSCallbacks() {
            //合成结果回调
            @Override
            public void onResult(TTS.TTSResult ttsResult, Object userTag) {
                final String currentTag = String.valueOf(mUserTag);
                if (userTag instanceof String && TextUtils.equals(currentTag, (String) userTag)) {
                    handleTtsResult(ttsResult);
                }
            }

            //合成失败回调
            @Override
            public void onError(TTS.TTSError ttsError, Object userTag) {
                final String currentTag = String.valueOf(mUserTag);
                if (userTag instanceof String && TextUtils.equals(currentTag, (String) userTag)) {
                    handleTtsError(ttsError);
                }
            }
        });
    }

    @Override
    public void destroy() {
        stop();
        mOnlineTTS.registerCallbacks(null);
        mAudioTrack.release();
        if (!mWorkThread.isShutdown()) {
            mWorkThread.shutdownNow();
        }
        super.destroy();
    }

    @Override
    public int getType() {
        return FUNCTION_TTS;
    }

    @Override
    public boolean isRunning() {
        return mStatus == STATUS_WORKING || isPlaying();
    }

    @Override
    public void execute(String input, Consumer<StateResult<TTS.TTSResult>> callback) {
        if (mWorkThread.isShutdown() || mWorkThread.isTerminated()) {
            if (null != callback) {
                runInMainThread(() -> callback.accept(new StateResult<TTS.TTSResult>()
                        .setState(STATUS_FINISH)
                        .setCode(ErrorCode.ERROR_LOCAL_NO_INIT)
                        .setMessage("Wrapper is released.")));
            }
            return;
        }
        if (isRunning()) { //正在处理中
            stop(); //停止处理
        }
        setCallback(callback);
        callbackStart();
        int userTag = autoIncUserTag();
        JL_Log.d(tag, "execute", "Input : " + input + ", userTag : " + userTag);
        int ret = mOnlineTTS.aRun(input, String.valueOf(userTag));
        if (ret != 0) {
            callbackFinish("execute", ret, "Operation failed. code : " + ret, null);
            return;
        }
        if (!startPlay())
            stopPlayTts(ErrorCode.MSP_ERROR_FAIL, "Failed to open audio player.");
    }

    @Override
    public void stop() {
        stopPlayTts(OpResult.ERR_NONE, "Stop operation.");
    }

    @Override
    public void cancel() {
        stopPlayTts(OpResult.ERR_NONE, "User stopped playing TTS.");
    }

    private void config() {
        /********************
         * aue(必填):
         * 音频编码，可选值：raw：未压缩的pcm
         * lame：mp3 (当aue=lame时需传参sfl=1)
         * speex-org-wb;7： 标准开源speex（for speex_wideband，即16k）数字代表指定压缩等级（默认等级为8）
         * speex-org-nb;7： 标准开源speex（for speex_narrowband，即8k）数字代表指定压缩等级（默认等级为8）
         * speex;7：压缩格式，压缩等级1~10，默认为7（8k讯飞定制speex）
         * speex-wb;7：压缩格式，压缩等级1~10，默认为7（16k讯飞定制speex）
         * ****************************/
        mOnlineTTS.aue("raw");
        mOnlineTTS.speed(50);//语速：0对应默认语速的1/2，100对应默认语速的2倍。最⼩值:0, 最⼤值:100
        mOnlineTTS.pitch(50);//语调：0对应默认语速的1/2，100对应默认语速的2倍。最⼩值:0, 最⼤值:100
        mOnlineTTS.volume(50);//音量：0是静音，1对应默认音量1/2，100对应默认音量的2倍。最⼩值:0, 最⼤值:100
        mOnlineTTS.bgs(0);//合成音频的背景音 0:无背景音（默认值） 1:有背景音
    }

    private void stopPlayTts(int code, String message) {
        if (isPlaying()) {
            stopPlay();
        }
        if (mStatus == STATUS_WORKING) { //正在合成语音
            mOnlineTTS.stop();
            callbackFinish("stopPlayTts", code, message, null);
        }
        mDataSize = 0;
    }

    private void async(Runnable runnable) {
        if (mWorkThread.isShutdown() || mWorkThread.isTerminated() || null == runnable) return;
        mWorkThread.submit(runnable);
    }

    private boolean isPlaying() {
        return mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED &&
                mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
    }

    private boolean startPlay() {
        if (mAudioTrack.getState() == AudioTrack.STATE_UNINITIALIZED) {
            JL_Log.w(tag, "startPlay", "AudioTrack is released.");
            return false;
        }
        if (isPlaying()) {
            stopPlay();
        }
        mAudioTrack.play();
        return true;
    }

    private void writePcmData(byte[] pcmData) {
        if (!isPlaying() || null == pcmData || pcmData.length == 0) return;
        mAudioTrack.write(pcmData, 0, pcmData.length);
        mDataSize += pcmData.length;
    }

    private void stopPlay() {
        if (isPlaying()) {
            mAudioTrack.stop();
        }
    }

    private void handleTtsResult(TTS.TTSResult result) {
        if (!isRunning()) return;
        //解析获取的交互结果，示例展示所有结果获取，开发者可根据自身需要，选择获取。
        byte[] audio = result.getData();//音频数据
        int len = result.getLen();//音频数据长度
        int status = result.getStatus();//数据状态, 0：start，1：continue，2：end
        String ced = result.getCed();//进度
        String sid = result.getSid();//sid

        if (len > 0) {
            writePcmData(audio);
        }
        if (status == 2) { //音频合成完成
            async(() -> {
                while (isPlaying()) {
                    int position = mAudioTrack.getPlaybackHeadPosition();
                    int totalFrames = mDataSize / 2; //因为是16Bit 所以是除以2Byte
                    if (position >= totalFrames) {
                        //播放完成
                        JL_Log.d(tag, "handleTtsResult", "Play finish.");
                        break;
                    }
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                //播放结束
                callbackFinish("handleTtsResult", OpResult.ERR_NONE, "Play finish.", result);
            });
        }
    }

    private void handleTtsError(TTS.TTSError error) {
        if (!isRunning()) return;
        int errCode = error.getCode();//错误码
        String errMsg = error.getErrMsg();//错误信息
        String sid = error.getSid();//sid

        callbackFinish("handleTtsError", errCode, errMsg, null);
    }
}
