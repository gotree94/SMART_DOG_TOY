package com.jieli.healthaide.ui.device.music;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;

import androidx.lifecycle.MutableLiveData;

import com.jieli.component.thread.ThreadManager;
import com.jieli.component.utils.ToastUtil;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.device.file.Util;
import com.jieli.healthaide.ui.device.watch.WatchViewModel;
import com.jieli.jl_filebrowse.FileBrowseManager;
import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.jl_filebrowse.util.DeviceChoseUtil;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.constant.WatchConstant;
import com.jieli.jl_rcsp.interfaces.IHandleResult;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.command.BatchCmd;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.task.ITask;
import com.jieli.jl_rcsp.task.TaskListener;
import com.jieli.jl_rcsp.task.TransferTask;
import com.jieli.jl_rcsp.task.UriTransferTask;
import com.jieli.jl_rcsp.tool.CustomRcspActionCallback;
import com.jieli.jl_rcsp.util.CHexConver;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/12/21 5:23 PM
 * @desc :
 */
public class MusicManagerViewModel extends WatchViewModel {
    static final int MODE_LIST = 0;
    static final int MODE_SELECT = 1;
    static final int MODE_DOWNLOAD = 2;

    MutableLiveData<List<Music>> musicsMutableLiveData = new MutableLiveData<>();
    MutableLiveData<Integer> modeLiveData = new MutableLiveData<>(MODE_SELECT);

    private ITask task;

    MutableLiveData<MusicDownloadEvent> downloadEventMutableLiveData = new MutableLiveData<>();

    public MusicManagerViewModel() {
        super();
    }


    @Override
    public void release() {
        super.release();
        cancelTransfer();
    }

    public void getMusicList(Context context) {
        ThreadManager.getInstance().postRunnable(() -> {
            JL_LocalMusicLoader loader = new JL_LocalMusicLoader(context.getContentResolver());
            musicsMutableLiveData.postValue(loader.loadAll());
        });
    }


    public void toNextMode() {
        if (FileBrowseManager.getInstance().getOnlineDev() == null || FileBrowseManager.getInstance().getOnlineDev().size() < 1) {
            ToastUtil.showToastShort(R.string.no_sdcard_device);
            return;
        }

        int mode = Objects.requireNonNull(modeLiveData.getValue()) + 1;
        if (mode > MODE_DOWNLOAD) {
            mode = 1;
        }
        modeLiveData.postValue(mode);
        if (mode == MODE_DOWNLOAD) {
            addToDownloadList();
        }
    }

    public void cancelSelect() {
        modeLiveData.postValue(MODE_SELECT);
    }

    private boolean isCallWorking() {
        DeviceInfo deviceInfo = getDeviceInfo(getConnectedDevice());
        return deviceInfo != null && deviceInfo.getPhoneStatus() == WatchConstant.DEVICE_PHONE_STATUS_CALLING;
    }

    private void addToDownloadList() {
        if (musicsMutableLiveData == null) {
            return;
        }
        if (isCallWorking()) {
            downloadEventMutableLiveData.postValue(new MusicDownloadEvent(MusicDownloadEvent.TYPE_ERROR, 0, 0, 0,
                    HealthApplication.getAppViewModel().getApplication().getString(R.string.call_phone_error_tips)));
            return;
        }

        List<Music> tmp = new ArrayList<>();
        for (Music music : Objects.requireNonNull(musicsMutableLiveData.getValue())) {
            if (music.isSelected()) {
                tmp.add(music);
            }
        }
        if (tmp.size() < 1) {
            modeLiveData.postValue(MODE_SELECT);
            return;
        }

        ITask lastTask = null;
        int size = tmp.size();
        SDCardBean sdCardBean = DeviceChoseUtil.getTargetDev();
        for (int i = size - 1; i >= 0; i--) {
            Music music = tmp.get(i);
            TransferTask.Param p = new TransferTask.Param();
            if (sdCardBean == null) {
                downloadEventMutableLiveData.postValue(new MusicDownloadEvent(MusicDownloadEvent.TYPE_ERROR, i + 1, 0, 0, music.getTitle()));
                return;
            }
            p.devHandler = sdCardBean.getDevHandler();
            ITask task;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                task = new UriTransferTask(HealthApplication.getAppViewModel().getApplication(), mWatchManager, music.getUri(), music.getTitle(), p);
            } else {
                task = new TransferTask(mWatchManager, music.getUrl(), p);
            }
            //通过listener形成一个调用链
            task.setListener(new AutoLastListener(lastTask, i + 1, size, music.getTitle()));
            lastTask = task;
        }
        task = lastTask;
        if (size > 1) { //通知设备开始多文件传输流程
            final String musicName = tmp.get(0).getTitle();
            sendBatchCmd(BatchCmd.OP_START, new OnOperationCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    task.start();
                }

                @Override
                public void onFailed(BaseError error) {
                    modeLiveData.postValue(MODE_SELECT);
                    downloadEventMutableLiveData.postValue(new MusicDownloadEvent(MusicDownloadEvent.TYPE_ERROR, 0, size, 0, musicName));
                }
            });
        } else {
            task.start();
        }
        Util.cleanDownloadDir(sdCardBean);
    }


    public void cancelTransfer() {
        if (task != null) {
            task.cancel((byte) 0x00);
        }
    }

    private void sendBatchCmd(int op, OnOperationCallback<Boolean> callback) {
        BatchCmd startBatchCmd = new BatchCmd(new BatchCmd.Param(CHexConver.intToByte(op), new byte[]{(byte) 0x02}));
        mWatchManager.sendRcspCommand(mWatchManager.getConnectedDevice(), startBatchCmd, new CustomRcspActionCallback<>("FormatBatchCmd", callback,
                new IHandleResult<Boolean, BatchCmd>() {
                    @Override
                    public int hasResult(BluetoothDevice device, BatchCmd cmd) {
                        if (null == cmd) return StateCode.STATUS_UNKNOWN;
                        BatchCmd.Response response = cmd.getResponse();
                        if (null == response) return StateCode.STATUS_UNKNOWN;
                        return CHexConver.byteToInt(response.getRet());
                    }

                    @Override
                    public Boolean handleResult(BluetoothDevice device, BatchCmd cmd) {
                        return true;
                    }
                }));
    }


    private class AutoLastListener implements TaskListener {

        private final int index;
        private final int total;
        private final String name;
        private final ITask last;

        public AutoLastListener(ITask last, int index, int total, String name) {
            this.index = index;
            this.total = total;
            this.name = name;
            this.last = last;
        }

        @Override
        public void onBegin() {
            downloadEventMutableLiveData.postValue(new MusicDownloadEvent(MusicDownloadEvent.TYPE_DOWNLOAD, index, total, 0, name));
        }


        @Override
        public void onProgress(int progress) {
            downloadEventMutableLiveData.postValue(new MusicDownloadEvent(MusicDownloadEvent.TYPE_DOWNLOAD, index, total, progress, name));

        }

        @Override
        public void onFinish() {
            task = last;
            if (last != null) {
                last.start();
            } else {
                sendBatchCmd(BatchCmd.OP_STOP, new OnOperationCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        modeLiveData.postValue(MODE_SELECT);
                        MusicDownloadEvent musicDownloadEvent = downloadEventMutableLiveData.getValue();
                        Objects.requireNonNull(musicDownloadEvent).setType(MusicDownloadEvent.TYPE_FINISH);
                        downloadEventMutableLiveData.postValue(musicDownloadEvent);
                    }

                    @Override
                    public void onFailed(BaseError error) {
                        dealWithFailedEvent(MusicDownloadEvent.TYPE_ERROR, error.getSubCode(), false);
                    }
                });
            }
        }

        @Override
        public void onError(int code, String msg) {
            dealWithFailedEvent(MusicDownloadEvent.TYPE_ERROR, code);
        }

        @Override
        public void onCancel(int reason) {
            dealWithFailedEvent(MusicDownloadEvent.TYPE_CANCEL, reason);
        }

        private void dealWithFailedEvent(int errType, int code) {
            dealWithFailedEvent(errType, code, true);
        }

        private void dealWithFailedEvent(int errType, int code, boolean isSendStopCmd) {
            if (isSendStopCmd) sendBatchCmd(BatchCmd.OP_STOP, null);
            modeLiveData.postValue(MODE_SELECT);
            downloadEventMutableLiveData.postValue(new MusicDownloadEvent(errType, index, total, 0, name));
        }
    }
}