package com.jieli.healthaide.ui.device.file;

import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.MutableLiveData;

import com.jieli.component.utils.ToastUtil;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.device.file.model.MusicPlayInfo;
import com.jieli.healthaide.ui.device.watch.WatchViewModel;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_filebrowse.FileBrowseConstant;
import com.jieli.jl_filebrowse.FileBrowseManager;
import com.jieli.jl_filebrowse.bean.FileStruct;
import com.jieli.jl_filebrowse.bean.Folder;
import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.jl_filebrowse.interfaces.DeleteCallback;
import com.jieli.jl_filebrowse.interfaces.FileObserver;
import com.jieli.jl_filebrowse.interfaces.SimpleFileObserver;
import com.jieli.jl_rcsp.constant.AttrAndFunCode;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspEventListener;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.model.device.MusicNameInfo;
import com.jieli.jl_rcsp.model.device.MusicStatusInfo;
import com.jieli.jl_rcsp.model.device.PlayModeInfo;
import com.jieli.jl_rcsp.tool.BooleanRcspActionCallback;
import com.jieli.jl_rcsp.util.CommandBuilder;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/15/21 5:42 PM
 * @desc :
 */
public class DeviceFileViewModel extends WatchViewModel implements FileObserver {
    public static final String DOWNLOAD_DIR = "download";
    static final int STATE_START = 0;
    static final int STATE_END = 1;//单次读取完成
    static final int STATE_FAILED = 2;
    static final int STATE_FINISH = 3;//改目录读取完成

    private final FileBrowseManager mFileBrowseManager = FileBrowseManager.getInstance();

    MutableLiveData<List<SDCardBean>> SDCardsMutableLiveData = new MutableLiveData<>();
    MutableLiveData<Folder> currentFolderMutableLiveData = new MutableLiveData<>();
    MutableLiveData<List<FileStruct>> filesMutableLiveData = new MutableLiveData<>(new ArrayList<>());
    MutableLiveData<Integer> readStateMutableLiveData = new MutableLiveData<>();
    MutableLiveData<MusicPlayInfo> musicPlayInfoLiveData = new MutableLiveData<>();


    public DeviceFileViewModel() {
        super();
        mFileBrowseManager.addFileObserver(this);
        SDCardsMutableLiveData.postValue(mFileBrowseManager.getOnlineDev());
        mWatchManager.registerOnRcspEventListener(onRcspEventListener);
        DeviceInfo deviceInfo = mWatchManager.getDeviceInfo(mWatchManager.getConnectedDevice());
        MusicPlayInfo musicPlayInfo = new MusicPlayInfo();
        if (deviceInfo != null) {
            musicPlayInfo.setDeviceMode(deviceInfo.getCurFunction());
            musicPlayInfo.setMusicNameInfo(deviceInfo.getMusicNameInfo());
            musicPlayInfo.setMusicStatusInfo(deviceInfo.getMusicStatusInfo());
            musicPlayInfo.setPlayModeInfo(deviceInfo.getPlayModeInfo());
            //没有缓存信息则读取
            if (deviceInfo.getCurFunction() == AttrAndFunCode.SYS_INFO_FUNCTION_MUSIC && musicPlayInfo.getMusicNameInfo() == null) {
                getMusicInfo();
            }
        }
        //跳转到
        musicPlayInfoLiveData.postValue(musicPlayInfo);
    }


    //自动跳转到下载目录
    private void browToDownLoadDir(SDCardBean sdCardBean) {

        Folder current = mFileBrowseManager.getCurrentReadFile(sdCardBean);
        if (current == null) {
            return;
        }

        // 返回根目录
        //遍历父文件夹，直到父文件夹是选中文件夹。
        while (current.getParent() != null) {
            mFileBrowseManager.backBrowse(sdCardBean, false);
            current = mFileBrowseManager.getCurrentReadFile(sdCardBean);
        }

        List<FileStruct> childs = current.getChildFileStructs();
        FileStruct downloadDir = Util.getDownloadDir(childs);
        if (downloadDir != null) {
            append(sdCardBean, downloadDir);
        } else if (!current.isLoadFinished(false)) {
            SimpleFileObserver simpleFileObserver = new SimpleFileObserver() {
                @Override
                public void onSdCardStatusChange(List<SDCardBean> onLineCards) {
                    super.onSdCardStatusChange(onLineCards);
                    mFileBrowseManager.removeFileObserver(this);
                }

                @Override
                public void onFileReadStop(boolean isEnd) {
                    super.onFileReadStop(isEnd);
                    mFileBrowseManager.addFileObserver(DeviceFileViewModel.this);
                    mFileBrowseManager.removeFileObserver(this);
                    browToDownLoadDir(sdCardBean);
                }

                @Override
                public void onFileReadFailed(int reason) {
                    super.onFileReadFailed(reason);
                    mFileBrowseManager.addFileObserver(DeviceFileViewModel.this);
                    mFileBrowseManager.removeFileObserver(this);
                }
            };
            mFileBrowseManager.addFileObserver(simpleFileObserver);
            int ret = mFileBrowseManager.loadMore(sdCardBean);
            if (ret == FileBrowseConstant.SUCCESS) {
                mFileBrowseManager.removeFileObserver(this);
            } else {
                mFileBrowseManager.removeFileObserver(simpleFileObserver);
            }
        } else {
            readStateMutableLiveData.postValue(STATE_FINISH);
        }

    }


    public void getCurrentInfo(SDCardBean sdCardBean) {
        Folder folder = mFileBrowseManager.getCurrentReadFile(sdCardBean);
        if (folder == null) return;
        if (folder.getName().equalsIgnoreCase(DOWNLOAD_DIR) && folder.getLevel() == 1) {
            currentFolderMutableLiveData.postValue(folder);
            onFileReceiver(folder.getChildFileStructs());
            if (!folder.isLoadFinished(false) && folder.getChildFileStructs().size() < 1) {
                loadMore(sdCardBean);
            } else if (folder.isLoadFinished(false)) {
                readStateMutableLiveData.postValue(STATE_FINISH);
            }
            return;
        }
        loadMore(sdCardBean);
    }


    public void append(SDCardBean sdCardBean, FileStruct fileStruct) {
        //清除download目录
        int ret = mFileBrowseManager.appenBrowse(fileStruct, sdCardBean);
        if (ret == FileBrowseConstant.ERR_READING) {
            ToastUtil.showToastShort(R.string.msg_read_file_err_reading);
        } else if (ret == FileBrowseConstant.ERR_OFFLINE) {
            ToastUtil.showToastShort(R.string.msg_read_file_err_offline);
        } else if (ret == FileBrowseConstant.ERR_BEYOND_MAX_DEPTH) {
            ToastUtil.showToastShort(R.string.msg_read_file_err_beyond_max_depth);
        } else if (ret == FileBrowseConstant.SUCCESS) {
            filesMutableLiveData.setValue(new ArrayList<>());
            currentFolderMutableLiveData.setValue(mFileBrowseManager.getCurrentReadFile(sdCardBean));
        }
    }


    public void back(SDCardBean sdCardBean, FileStruct fileStruct) {
        Folder current = mFileBrowseManager.getCurrentReadFile(sdCardBean);
        if (current == null) {
            return;
        }
        //当前文件返回
        if (current.getFileStruct().getCluster() == fileStruct.getCluster()) {
            return;
        }
        //遍历父文件夹，直到父文件夹是选中文件夹。
        while (current.getParent() != null && current.getParent().getFileStruct().getCluster() != fileStruct.getCluster()) {
            mFileBrowseManager.backBrowse(sdCardBean, false);
            current = mFileBrowseManager.getCurrentReadFile(sdCardBean);
        }
        //清空文件列表
        filesMutableLiveData.setValue(new ArrayList<>());
        mFileBrowseManager.backBrowse(sdCardBean, true);
        currentFolderMutableLiveData.postValue(mFileBrowseManager.getCurrentReadFile(sdCardBean));
    }

    public void play(SDCardBean sdCardBean, FileStruct fileStruct) {
        if (mFileBrowseManager.isReading()) {
            ToastUtil.showToastShort(R.string.msg_read_file_err_reading);
            return;
        }
        mFileBrowseManager.playFile(fileStruct, sdCardBean);

    }

    public void delete(SDCardBean sdCardBean, FileStruct fileStruct) {
        if (mFileBrowseManager.isReading()) {
            ToastUtil.showToastShort(R.string.msg_read_file_err_reading);
            return;
        }
        List<FileStruct> list = new ArrayList<>();
        list.add(fileStruct);
        mFileBrowseManager.deleteFile(sdCardBean, list, false, new DeleteCallback() {
            @Override
            public void onSuccess(FileStruct fileStruct) {
                ToastUtil.showToastShort(HealthApplication.getAppViewModel().getApplication().getString(R.string.execution_succeeded));
                cleanDownloadCache(sdCardBean); //更新下载音乐缓存
            }

            @Override
            public void onError(int code, FileStruct fileStruct) {
                //ToastUtil.showToastShort("删除失败");
                ToastUtil.showToastShort(CalendarUtil.formatString("%s: %d", HealthApplication.getAppViewModel().getApplication().getString(R.string.failed_reason), code));
            }

            @Override
            public void onFinish() {

            }
        });

    }


    public void loadMore(SDCardBean sdCardBean) {
        Folder folder = mFileBrowseManager.getCurrentReadFile(sdCardBean);
        if (folder == null || !folder.getName().equalsIgnoreCase(DOWNLOAD_DIR) || folder.getLevel() != 1) {
            browToDownLoadDir(sdCardBean);
            return;
        }
        int ret = mFileBrowseManager.loadMore(sdCardBean);
        if (ret == FileBrowseConstant.ERR_LOAD_FINISHED) {
            if (filesMutableLiveData.getValue() != null && filesMutableLiveData.getValue().isEmpty()) {
                filesMutableLiveData.setValue(Util.filter(folder.getChildFileStructs()));
            }
            readStateMutableLiveData.postValue(STATE_FINISH);
        } else if (ret == FileBrowseConstant.ERR_READING) {
            readStateMutableLiveData.postValue(STATE_END);
        } else if (ret != FileBrowseConstant.SUCCESS) {
            readStateMutableLiveData.postValue(STATE_FAILED);
        }
    }


    public void cleanDownloadCache(SDCardBean sdCardBean) {
        filesMutableLiveData.setValue(new ArrayList<>());
        loadMore(sdCardBean);
    }

    public void getMusicInfo() {
        mWatchManager.sendRcspCommand(CommandBuilder.buildGetMusicSysInfoCmd(),
                new BooleanRcspActionCallback("getMusicInfo", new OnOperationCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {

                    }

                    @Override
                    public void onFailed(BaseError error) {
                        ToastUtil.showToastShort(R.string.get_music_info_failed);
                    }
                }));
    }

    @Override
    protected void onCleared() {
        mFileBrowseManager.removeFileObserver(this);
        mWatchManager.unregisterOnRcspEventListener(onRcspEventListener);
        super.onCleared();
    }

    @Override
    public void onFileReceiver(List<FileStruct> fileStructs) {
        JL_Log.d(tag, "onFileReceiver", "");
        List<FileStruct> list = filesMutableLiveData.getValue() == null ? new ArrayList<>() : filesMutableLiveData.getValue();
        list.addAll(Util.filter(fileStructs));
        filesMutableLiveData.postValue(list);
    }

    @Override
    public void onFileReadStop(boolean isEnd) {
        if (isEnd) {
            readStateMutableLiveData.postValue(STATE_FINISH);
        } else {
            readStateMutableLiveData.postValue(STATE_END);
        }
    }

    @Override
    public void onFileReadStart() {
        readStateMutableLiveData.postValue(STATE_START);
    }

    @Override
    public void onFileReadFailed(int reason) {
        readStateMutableLiveData.postValue(STATE_FAILED);

    }

    @Override
    public void onSdCardStatusChange(List<SDCardBean> onLineCards) {

    }


    @Override
    public void OnFlayCallback(boolean success) {
        getMusicInfo();
    }

    private MusicPlayInfo getMusicPlayInfo() {
        MusicPlayInfo info = musicPlayInfoLiveData.getValue();
        if (info == null) {
            info = new MusicPlayInfo();
        }
        return info;
    }

    private final OnRcspEventListener onRcspEventListener = new OnRcspEventListener() {
        @Override
        public void onMusicNameChange(BluetoothDevice device, MusicNameInfo nameInfo) {
            MusicPlayInfo info = getMusicPlayInfo();
            info.setMusicNameInfo(nameInfo);
            musicPlayInfoLiveData.postValue(info);
        }

        @Override
        public void onPlayModeChange(BluetoothDevice device, PlayModeInfo playModeInfo) {
            MusicPlayInfo info = getMusicPlayInfo();
            info.setPlayModeInfo(playModeInfo);
            musicPlayInfoLiveData.postValue(info);
        }

        @Override
        public void onMusicStatusChange(BluetoothDevice device, MusicStatusInfo statusInfo) {
            MusicPlayInfo info = getMusicPlayInfo();
            info.setMusicStatusInfo(statusInfo);
            musicPlayInfoLiveData.postValue(info);
        }

        @Override
        public void onDeviceModeChange(BluetoothDevice device, int mode) {
            MusicPlayInfo info = getMusicPlayInfo();
            info.setDeviceMode(mode);
            musicPlayInfoLiveData.postValue(info);
        }
    };

}
