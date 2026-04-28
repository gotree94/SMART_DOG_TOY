package com.jieli.watchtesttool.ui.record;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;

import com.jieli.jl_audio_decode.callback.OnDecodeStreamCallback;
import com.jieli.jl_audio_decode.exceptions.OpusException;
import com.jieli.jl_audio_decode.exceptions.SpeexException;
import com.jieli.jl_audio_decode.opus.OpusManager;
import com.jieli.jl_audio_decode.speex.SpeexManager;
import com.jieli.jl_rcsp.constant.JLChipFlag;
import com.jieli.jl_rcsp.impl.RecordOpImpl;
import com.jieli.jl_rcsp.interfaces.record.OnRecordStateCallback;
import com.jieli.jl_rcsp.model.RecordParam;
import com.jieli.jl_rcsp.model.RecordState;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothViewModel;
import com.jieli.watchtesttool.tool.config.ConfigHelper;
import com.jieli.watchtesttool.util.AppUtil;
import com.jieli.watchtesttool.util.WatchTestConstant;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 录音逻辑处理
 * @since 2022/9/28
 */
public class RecordViewModel extends BluetoothViewModel {
    private final static int MSG_STOP_PLAY_AUDIO = 0x3245;

    private final static long STOP_PLAY_TIMEOUT = 2000L;

    private final ConfigHelper mConfigHelper = ConfigHelper.getInstance();
    private final RecordOpImpl mRecordOp;

    private OpusManager mOpusManager;
    private SpeexManager mSpeexManager;
    private AudioTrack mAudioTrack;
    private FileOutputStream mOutputStream;
    private int minBufferSize = 0;

    private int audioType = RecordParam.VOICE_TYPE_OPUS;
    private final ByteArrayOutputStream cacheBuf = new ByteArrayOutputStream();
    private final ExecutorService threadPool = Executors.newSingleThreadExecutor();

    private final String outputFilePath;
    private final Handler uiHandler = new Handler(Looper.getMainLooper(), msg -> {
        if (MSG_STOP_PLAY_AUDIO == msg.what) {
            stopAudio();
        }
        return true;
    });

    public final MutableLiveData<RecordState> mRecordStateMLD = new MutableLiveData<>();
    public final MutableLiveData<String> mLogMLD = new MutableLiveData<>();

    public RecordViewModel() {
        try {
            mOpusManager = new OpusManager();
        } catch (OpusException e) {
            JL_Log.w(tag, "init", "OpusManager init error : " + e.getMessage());
        }
        try {
            mSpeexManager = new SpeexManager();
        } catch (SpeexException e) {
            JL_Log.w(tag, "init", "SpeexManager init error : " + e.getMessage());
        }
        outputFilePath = AppUtil.createFilePath(getContext(), WatchTestConstant.DIR_RECORD) + "/test.opus";
        mRecordOp = new RecordOpImpl(mWatchManager);
        mRecordOp.addOnRecordStateCallback(mWatchStateCallback);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        destroy();
    }

    public boolean is707NWatch() {
        DeviceInfo deviceInfo = mWatchManager.getDeviceInfo();
        if (null == deviceInfo) return false;
        return deviceInfo.getSdkType() == JLChipFlag.JL_CHIP_FLAG_707N_WATCH;
    }

    public boolean isRecording() {
        return mRecordOp != null && mRecordOp.getRecordState().getState() != RecordState.RECORD_STATE_IDLE;
    }

    public void startRecord(int audioType) {
        this.audioType = audioType;
        //录音参数: OPUS格式音频或者PCM音频, 采样率: 16K， VAD方式: SDK端判断
        RecordParam recordParam = new RecordParam(audioType, RecordParam.SAMPLE_RATE_16K, RecordParam.VAD_WAY_SDK);
        mRecordOp.startRecord(mRecordOp.getConnectedDevice(), recordParam, null);
    }

    public void stopRecord(int reason) {
        mRecordOp.stopRecord(mRecordOp.getConnectedDevice(), reason, false, false, false, null);
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public boolean isDecodeAudioData() {
        return mConfigHelper.isDecodeAudioData();
    }

    public void setDecodeAudioData(boolean isDecode) {
        mConfigHelper.setDecodeAudioData(isDecode);
    }

    public boolean isSaveRawAudioFile() {
        return mConfigHelper.isSaveRawAudioFile();
    }

    public void setSaveRawAudioFile(boolean isSave) {
        mConfigHelper.setSaveRawAudioFile(isSave);
    }

    public void destroy() {
        super.destroy();
        uiHandler.removeCallbacksAndMessages(null);
        mRecordOp.removeOnRecordStateCallback(mWatchStateCallback);
        mRecordOp.release();
        cacheBuf.reset();
        closeFileStream();
        stopAudio();
        threadPool.shutdownNow();
        if (mAudioTrack != null) {
            mAudioTrack.release();
            mAudioTrack = null;
        }
        if (mOpusManager != null) {
            mOpusManager.release();
            mOpusManager = null;
        }
        if (mSpeexManager != null) {
            mSpeexManager.release();
            mSpeexManager = null;
        }
    }

    private void log(String format, Object... args) {
        log(String.format(format, args));
    }

    private void log(String content) {
        JL_Log.d(tag, "log", content);
        mLogMLD.postValue(content);
    }

    private void handleRecordEvent(RecordState state) {
        if (null == state) return;
        switch (state.getState()) {
            case RecordState.RECORD_STATE_START:  //录音开始
                saveFilePrepare();
                if (!isDecodeAudioData()) break;
                playPrepare(state.getRecordParam());
                switch (audioType) {
                    case RecordParam.VOICE_TYPE_OPUS:
                        startOpusDecode();
                        break;
                    case RecordParam.VOICE_TYPE_PCM:
                        playAudio();
                        break;
                    case RecordParam.VOICE_TYPE_SPEEX:
                        startSpeexDecode();
                        break;
                }
                break;
            case RecordState.RECORD_STATE_WORKING: //录音数据回传
                final byte[] voiceData = state.getVoiceDataBlock();
                saveData(voiceData);
                if (!isDecodeAudioData()) break;
                switch (audioType) {
                    case RecordParam.VOICE_TYPE_OPUS:
                        writeOpusData(voiceData);
                        break;
                    case RecordParam.VOICE_TYPE_PCM:
                        writeAudioData(voiceData);
                        break;
                    case RecordParam.VOICE_TYPE_SPEEX:
                        writeSpeexData(voiceData);
                        break;
                }
                break;
            case RecordState.RECORD_STATE_IDLE:   //录音结束
                closeFileStream();
                if (!isDecodeAudioData()) break;
                switch (audioType) {
                    case RecordParam.VOICE_TYPE_OPUS:
                        stopOpusDecode();
                        break;
                    case RecordParam.VOICE_TYPE_PCM:
                        stopAudio();
                        break;
                    case RecordParam.VOICE_TYPE_SPEEX:
                        stopSpeexDecode();
                        break;
                }
                break;
        }

    }

    private boolean isPlayerInit() {
        return mAudioTrack != null && mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED;
    }

    private boolean isAudioPlaying() {
        return isPlayerInit() && mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
    }

    /*
     * 播放准备
     */
    private void playPrepare(RecordParam param) {
        if (mAudioTrack != null) {
            if (isAudioPlaying()) {
                mAudioTrack.stop();
            }
            mAudioTrack.release();
            mAudioTrack = null;
        }
        int sampleRate = 16 * 1000;
        minBufferSize = AudioTrack.getMinBufferSize(sampleRate,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        AudioFormat format = new AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setSampleRate(sampleRate)
                .build();
        JL_Log.w(tag, "playPrepare", "minBufferSize = " + minBufferSize);
        mAudioTrack = new AudioTrack(attributes, format, minBufferSize, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);
    }

    private void playAudio() {
        if (!isPlayerInit()) return;
        if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            mAudioTrack.stop();
        }
        mAudioTrack.play();
    }

    private void writeAudioData(final byte[] pcmData) {
        if (null == pcmData || pcmData.length == 0) return;
        if (!isPlayerInit() || threadPool.isShutdown()) return;
        threadPool.submit(() ->{
            try {
                cacheBuf.write(pcmData);
                if (cacheBuf.size() >= minBufferSize) {
                    byte[] data = cacheBuf.toByteArray();
                    byte[] buf = new byte[minBufferSize];
                    byte[] left = new byte[data.length - minBufferSize];
                    System.arraycopy(data, 0, buf, 0, buf.length);
                    if (left.length > 0) {
                        System.arraycopy(data, buf.length, left, 0, left.length);
                    }
                    cacheBuf.reset();
                    if (left.length > 0) {
                        try {
                            cacheBuf.write(left);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    JL_Log.d(tag, "writeAudioData", "voice data : " + buf.length);
                    mAudioTrack.write(buf, 0, buf.length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void stopAudio() {
        if (isAudioPlaying()) {
            mAudioTrack.stop();
        }
    }

    private void startOpusDecode() {
        if (null == mOpusManager) return;
        mOpusManager.startDecodeStream(decodeStreamCallback);
    }

    private void stopOpusDecode() {
        if (null == mOpusManager || !mOpusManager.isDecodeStream()) return;
        mOpusManager.stopDecodeStream();
    }

    private void writeOpusData(byte[] data) {
        if (null == data || data.length == 0) return;
        if (null == mOpusManager || !mOpusManager.isDecodeStream()) return;
        mOpusManager.writeAudioStream(data);
    }

    private void startSpeexDecode() {
        if (null == mSpeexManager) return;
        mSpeexManager.startDecodeStream(decodeStreamCallback);
    }

    private void stopSpeexDecode() {
        if (null == mSpeexManager || !mSpeexManager.isDecodeStream()) return;
        mSpeexManager.stopDecodeStream();
    }

    private void writeSpeexData(byte[] data) {
        if (null == data || data.length == 0) return;
        if (null == mSpeexManager || !mSpeexManager.isDecodeStream()) return;
        mSpeexManager.writeAudioStream(data);
    }

    private void saveFilePrepare() {
        if (!isSaveRawAudioFile()) return;
        closeFileStream();
        try {
            mOutputStream = new FileOutputStream(outputFilePath);
            log("%s: %s", getContext().getString(R.string.save_audio_file_success), outputFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveData(byte[] data) {
        if (null == mOutputStream || !isSaveRawAudioFile()) return;
        try {
            mOutputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeFileStream() {
        if (null == mOutputStream) return;
        try {
            mOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mOutputStream = null;
    }

    private final OnRecordStateCallback mWatchStateCallback = (bluetoothDevice, recordState) -> {
        handleRecordEvent(recordState);
        mRecordStateMLD.setValue(recordState);
    };

    private final OnDecodeStreamCallback decodeStreamCallback = new OnDecodeStreamCallback() {
        @Override
        public void onDecodeStream(byte[] bytes) {
            log("(%s) ---> %s: data Length = %d", getMethod(), getContext().getString(R.string.decode_data_size), bytes.length);
            writeAudioData(bytes);
            uiHandler.removeMessages(MSG_STOP_PLAY_AUDIO);
            uiHandler.sendEmptyMessageDelayed(MSG_STOP_PLAY_AUDIO, STOP_PLAY_TIMEOUT);
        }

        @Override
        public void onStart() {
            log("(%s) ---> %s", getMethod(), getContext().getString(R.string.start_decode));
            playAudio();
        }

        @Override
        public void onComplete(String s) {
            log("(%s) ---> %s", getMethod(), getContext().getString(R.string.stop_decode));
        }

        @Override
        public void onError(int i, String s) {
            log("(%s) ---> %s: code = %d, %s", getMethod(), getContext().getString(R.string.decode_exception), i, s);
            uiHandler.removeMessages(MSG_STOP_PLAY_AUDIO);
            stopAudio();
        }

        private String getMethod() {
            return audioType == RecordParam.VOICE_TYPE_SPEEX ? "SPEEX" : "OPUS";
        }
    };
}
