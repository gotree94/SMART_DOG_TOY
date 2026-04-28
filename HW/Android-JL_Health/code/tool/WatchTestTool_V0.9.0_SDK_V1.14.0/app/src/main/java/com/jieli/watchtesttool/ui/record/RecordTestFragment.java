package com.jieli.watchtesttool.ui.record;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.model.RecordParam;
import com.jieli.jl_rcsp.model.RecordState;
import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.jl_rcsp.util.RcspUtil;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.databinding.FragmentRecordTestBinding;
import com.jieli.watchtesttool.ui.base.BaseFragment;
import com.jieli.watchtesttool.util.AppUtil;

/**
 * 录音测试
 */
public class RecordTestFragment extends BaseFragment {

    /**
     * 日志最大长度
     */
    private static final long LOG_MAX_SIZE = 2 * 1024 * 1024; //2Mb
    /**
     * 更新间隔
     */
    private static final long UPDATE_INTERVAL = 1000L;
    /**
     * 更新日志消息
     */
    private static final int MSG_UPDATE_LOG = 0x5467;

    private FragmentRecordTestBinding mBinding;
    private RecordViewModel mViewModel;

    private final StringBuilder logBuilder = new StringBuilder();

    private final Handler uiHandler = new Handler(Looper.getMainLooper(), msg -> {
        if (MSG_UPDATE_LOG == msg.what) {
            String context = logBuilder.toString();
            logBuilder.setLength(0);
            addLog(context);
        }
        return true;
    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentRecordTestBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(RecordViewModel.class);
        initUI();
        observerCallback();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHandler.removeCallbacksAndMessages(null);
    }

    private void initUI() {
        mBinding.clRecordTopbar.tvTopbarTitle.setText(getString(R.string.func_record_test));
        mBinding.clRecordTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().finish());

        mBinding.btnRecord.setOnClickListener(v -> {
            if (mViewModel.isRecording()) {
                mViewModel.stopRecord(RecordState.REASON_NORMAL);
                return;
            }
            int selected = mBinding.rgAudioType.getCheckedRadioButtonId();
            int audioType = selected == R.id.rbtn_pcm ? RecordParam.VOICE_TYPE_PCM
                    : selected == R.id.rbtn_speex ? RecordParam.VOICE_TYPE_SPEEX : RecordParam.VOICE_TYPE_OPUS;
            mViewModel.startRecord(audioType);
        });
        mBinding.ivRecordClearLog.setOnClickListener(v -> clearLog());
        mBinding.switchDecodeAudio.setOnCheckedChangeListener((buttonView, isChecked) -> mViewModel.setDecodeAudioData(isChecked));
        mBinding.switchSaveFile.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mViewModel.setSaveRawAudioFile(isChecked);
            updateSaveFile(isChecked);
        });

        mBinding.tvRecordLogcat.setMovementMethod(ScrollingMovementMethod.getInstance());
        mBinding.tvRecordLogcat.setLongClickable(false);

        mBinding.rbtnSpeex.setVisibility(mViewModel.is707NWatch() ? View.VISIBLE : View.GONE);

        handleBtnEnable(!mViewModel.isRecording());
        updateRecordBtn(mViewModel.isRecording());

        updateDecodeUI(mViewModel.isDecodeAudioData());
        updateSaveFile(mViewModel.isSaveRawAudioFile());
    }

    private void observerCallback() {
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), connection -> {
            if (connection.getStatus() != StateCode.CONNECTION_OK) {
                requireActivity().finish();
            }
        });
        mViewModel.mRecordStateMLD.observe(getViewLifecycleOwner(), this::handleRecordEvent);
        mViewModel.mLogMLD.observe(getViewLifecycleOwner(), this::updateLog);
    }

    private void updateRecordBtn(boolean isRecording) {
        mBinding.btnRecord.setText(isRecording ? getString(R.string.stop_record) : getString(R.string.start_record));
    }

    private void updateDecodeUI(boolean isDecode) {
        mBinding.switchDecodeAudio.setChecked(isDecode);
    }

    private void updateSaveFile(boolean isSave) {
        mBinding.switchSaveFile.setChecked(isSave);
        mBinding.tvSaveFilePath.setVisibility(isSave ? View.VISIBLE : View.GONE);
        if (isSave) {
            mBinding.tvSaveFilePath.setText(RcspUtil.formatString("%s : %s", getString(R.string.save_file_path), mViewModel.getOutputFilePath()));
        }
    }

    private void clearLog() {
        JL_Log.d(tag, "clearLog", "");
        logBuilder.setLength(0);
        uiHandler.removeMessages(MSG_UPDATE_LOG);
        addLog("", false);
    }

    private void addLog(String text) {
        addLog(text, true);
    }

    private void addLog(String text, boolean isAppend) {
        if (!isAppend) {
            mBinding.tvRecordLogcat.setText(text);
            mBinding.tvRecordLogcat.scrollTo(0, 0);
        } else {
            String log = mBinding.tvRecordLogcat.getText().toString().trim();
            if (log.length() >= LOG_MAX_SIZE) {
                JL_Log.d(tag, "addLog", "log len over limit. clear log");
                mBinding.tvRecordLogcat.setText("");
            }
            mBinding.tvRecordLogcat.append(text);
            mBinding.tvRecordLogcat.append("\n");
            int offset = AppUtil.getTextViewHeight(mBinding.tvRecordLogcat);
            if (offset > mBinding.tvRecordLogcat.getHeight()) {
                mBinding.tvRecordLogcat.scrollTo(0, offset - mBinding.tvRecordLogcat.getHeight());
            }
        }
    }

    private void handleRecordEvent(RecordState state) {
        switch (state.getState()) {
            case RecordState.RECORD_STATE_START: {
                handleBtnEnable(false);
                RecordParam param = state.getRecordParam();
                String message = RcspUtil.formatString(
                        "=========================>>>\n" +
                                "||%s!\n" +
                                "=========================>>>\n" +
                                "||%s : %s,\n" +
                                "||%s : %s,\n" +
                                "||%s : %s\n" +
                                "=========================>>>\n",
                        getString(R.string.record_start),
                        getString(R.string.voice_type),
                        (param.getVoiceType() == RecordParam.VOICE_TYPE_PCM ? "PCM" :
                                (param.getVoiceType() == RecordParam.VOICE_TYPE_SPEEX ? "SPEEX" : "OPUS")),
                        getString(R.string.sample_rate),
                        (param.getSampleRate() == RecordParam.SAMPLE_RATE_8K ? "8K" : "16K"),
                        getString(R.string.vad_way),
                        (param.getVadWay() == RecordParam.VAD_WAY_SDK ? getString(R.string.vad_sdk) : getString(R.string.vad_device)));
                updateLog(message);
                updateRecordBtn(true);
                break;
            }
            case RecordState.RECORD_STATE_WORKING: {
                byte[] block = state.getVoiceDataBlock();
                RecordParam param = state.getRecordParam();
                String message;
                if (param != null) {
                    message = RcspUtil.formatString(
                            "=========================>>>\n" +
                                    "||%s!\n" +
                                    "=========================>>>\n" +
                                    "||%s : %s,\n" +
                                    "||%s : %s,\n" +
                                    "||%s : %s\n" +
                                    "=========================>>>\n" +
                                    "||[%s]\n" +
                                    "=========================>>>\n",
                            getString(R.string.record_data_callback),
                            getString(R.string.voice_type),
                            (param.getVoiceType() == RecordParam.VOICE_TYPE_PCM ? "PCM" :
                                    (param.getVoiceType() == RecordParam.VOICE_TYPE_SPEEX ? "SPEEX" : "OPUS")),
                            getString(R.string.sample_rate),
                            (param.getSampleRate() == RecordParam.SAMPLE_RATE_8K ? "8K" : "16K"),
                            getString(R.string.vad_way),
                            (param.getVadWay() == RecordParam.VAD_WAY_SDK ? getString(R.string.vad_sdk) : getString(R.string.vad_device)),
                            CHexConver.byte2HexStr(block));
                } else {
                    message = RcspUtil.formatString(
                            "=========================>>>\n" +
                                    "||%s!\n" +
                                    "=========================>>>\n" +
                                    "||[%s]\n" +
                                    "=========================>>>\n", getString(R.string.record_data_callback), CHexConver.byte2HexStr(block));
                }
                updateLog(message);
                break;
            }
            case RecordState.RECORD_STATE_IDLE: {
                int reason = state.getReason();
                String content = reason == RecordState.REASON_NORMAL ?
                        getString(R.string.record_success) : (reason == RecordState.REASON_STOP ?
                        getString(R.string.record_cancel) : state.getMessage());
                String message = RcspUtil.formatString(
                        "=========================>>>\n" +
                                "||%s!\n" +
                                "=========================>>>\n" +
                                "||%s : %d,\n" +
                                "||%s : %s\n" +
                                "=========================>>>\n",
                        getString(R.string.record_finish), getString(R.string.finish_code), reason,
                        getString(R.string.finish_desc), content);
                updateLog(message);
                handleBtnEnable(true);
                updateRecordBtn(false);
                    /*if (reason == RecordState.REASON_NORMAL) {
                        byte[] allData = state.getVoiceData(); //录音采集的数据

                    }*/
                break;
            }
        }
    }

    private void handleBtnEnable(boolean enable) {
        mBinding.switchDecodeAudio.setEnabled(enable);
        mBinding.switchSaveFile.setEnabled(enable);
        mBinding.rbtnPcm.setEnabled(enable);
        mBinding.rbtnSpeex.setEnabled(enable);
        mBinding.rbtnOpus.setEnabled(enable);
    }

    private void updateLog(String text) {
        logBuilder.append(text).append("\n");
        if (!uiHandler.hasMessages(MSG_UPDATE_LOG)) {
            uiHandler.sendEmptyMessageDelayed(MSG_UPDATE_LOG, UPDATE_INTERVAL);
        }
    }
}