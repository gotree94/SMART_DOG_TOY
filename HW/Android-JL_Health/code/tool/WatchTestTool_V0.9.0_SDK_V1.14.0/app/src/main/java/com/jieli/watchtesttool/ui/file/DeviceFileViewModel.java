package com.jieli.watchtesttool.ui.file;

import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jieli.component.utils.ToastUtil;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothEventListener;
import com.jieli.watchtesttool.tool.watch.WatchManager;

import com.jieli.jl_filebrowse.FileBrowseConstant;
import com.jieli.jl_filebrowse.FileBrowseManager;
import com.jieli.jl_filebrowse.bean.FileStruct;
import com.jieli.jl_filebrowse.bean.Folder;
import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.jl_filebrowse.interfaces.DeleteCallback;
import com.jieli.jl_filebrowse.interfaces.FileObserver;
import com.jieli.jl_rcsp.constant.AttrAndFunCode;
import com.jieli.jl_rcsp.constant.Command;
import com.jieli.jl_rcsp.constant.RcspConstant;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.interfaces.rcsp.RcspCommandCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.base.CommandBase;
import com.jieli.jl_rcsp.model.command.sys.GetSysInfoCmd;
import com.jieli.jl_rcsp.model.response.SysInfoResponse;
import com.jieli.jl_rcsp.util.CommandBuilder;
import com.jieli.watchtesttool.ui.file.model.MusicNameInfo;
import com.jieli.watchtesttool.ui.file.model.MusicPlayInfo;
import com.jieli.watchtesttool.ui.file.model.MusicStatusInfo;
import com.jieli.watchtesttool.ui.file.model.PlayModeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/15/21 5:42 PM
 * @desc :
 */
public class DeviceFileViewModel extends ViewModel implements FileObserver {
    static final int STATE_START = 0;
    static final int STATE_END = 1;//单次读取完成
    static final int STATE_FAILED = 2;
    static final int STATE_FINISH = 3;//改目录读取完成

    public SDCardBean mSDCardBean;

    MutableLiveData<List<SDCardBean>> SDCardsMutableLiveData = new MutableLiveData<>();
    MutableLiveData<Folder> currentFolderMutableLiveData = new MutableLiveData<>();
    MutableLiveData<List<FileStruct>> filesMutableLiveData = new MutableLiveData<>();
    MutableLiveData<Integer> readStateMutableLiveData = new MutableLiveData<>();
    MutableLiveData<MusicPlayInfo> musicPlayInfoLiveData = new MutableLiveData<>(new MusicPlayInfo());


    public DeviceFileViewModel() {
        FileBrowseManager.getInstance().addFileObserver(this);
        SDCardsMutableLiveData.postValue(FileBrowseManager.getInstance().getOnlineDev());
        WatchManager.getInstance().registerOnRcspCallback(musicInfoHandler);
    }


    public void getCurrentInfo() {
        Folder folder = FileBrowseManager.getInstance().getCurrentReadFile(mSDCardBean);
        if (folder == null) {
            JL_Log.e("sen", "The current directory is null.");
            return;
        }
        currentFolderMutableLiveData.postValue(folder);
        filesMutableLiveData.setValue(folder.getChildFileStructs());
        if (!folder.isLoadFinished(false) && folder.getChildFileStructs().isEmpty()) {
            loadMore();
        } else if (folder.isLoadFinished(false)) {
            readStateMutableLiveData.postValue(STATE_FINISH);
        }
    }

    public void loadSpecificFolder(String dirName) {
        Folder folder = FileBrowseManager.getInstance().getCurrentReadFile(mSDCardBean);
        if (null == folder) {
            JL_Log.d("sen", "The current folder is null.");
            return;
        }

        //获取当前目录的缓存文件列表
        List<FileStruct> files = folder.getChildFileStructs();
        if (files != null && !files.isEmpty()) {
            for (FileStruct file : files) {
                if (!file.isFile() && file.getName().equals(dirName)) { //找到目标目录
                    append(file); //访问目标目录
                    return;
                }
            }
        }
        //当前目录是否加载完成
        boolean isLoadFinish = folder.isLoadFinished(false);
        if (!isLoadFinish) { //未加载完数据，重新加载完数据在开始查找
            loadMore();
        }
    }


    public void append(FileStruct fileStruct) {
        int ret = FileBrowseManager.getInstance().appenBrowse(fileStruct, mSDCardBean);
        if (ret == FileBrowseConstant.ERR_READING) {
            ToastUtil.showToastShort(R.string.msg_read_file_err_reading);
        } else if (ret == FileBrowseConstant.ERR_OFFLINE) {
            ToastUtil.showToastShort(R.string.msg_read_file_err_offline);
        } else if (ret == FileBrowseConstant.ERR_BEYOND_MAX_DEPTH) {
            ToastUtil.showToastShort(R.string.msg_read_file_err_beyond_max_depth);
        } else if (ret == FileBrowseConstant.SUCCESS) {
            filesMutableLiveData.setValue(new ArrayList<>());
            currentFolderMutableLiveData.postValue(FileBrowseManager.getInstance().getCurrentReadFile(mSDCardBean));
        }
    }


    public void back(FileStruct fileStruct) {
        SDCardBean sdCardBean = mSDCardBean;
        Folder current = FileBrowseManager.getInstance().getCurrentReadFile(sdCardBean);
        if (current == null) {
            return;
        }
        //当前文件返回
        if (current.getFileStruct().getCluster() == fileStruct.getCluster()) {
            return;
        }
        //遍历父文件夹，直到父文件夹是选中文件夹。
        while (current.getParent() != null && current.getParent().getFileStruct().getCluster() != fileStruct.getCluster()) {
            if(FileBrowseManager.getInstance().backBrowse(sdCardBean, false) != 0){
                break;
            }
            current = FileBrowseManager.getInstance().getCurrentReadFile(sdCardBean);
        }

        //清空文件列表
        filesMutableLiveData.setValue(new ArrayList<>());
        FileBrowseManager.getInstance().backBrowse(sdCardBean, true);
        currentFolderMutableLiveData.postValue(FileBrowseManager.getInstance().getCurrentReadFile(sdCardBean));
    }

    public void play(FileStruct fileStruct) {
        if (FileBrowseManager.getInstance().isReading()) {
            ToastUtil.showToastShort(R.string.msg_read_file_err_reading);
            return;
        }
        FileBrowseManager.getInstance().playFile(fileStruct, mSDCardBean);

    }

    public void delete(FileStruct fileStruct) {
        if (FileBrowseManager.getInstance().isReading()) {
            ToastUtil.showToastShort(R.string.msg_read_file_err_reading);
            return;
        }
        List<FileStruct> list = new ArrayList<>();
        list.add(fileStruct);

        FileBrowseManager.getInstance().deleteFile(mSDCardBean, list, mSDCardBean.getType() > SDCardBean.USB, new DeleteCallback() {
            @Override
            public void onSuccess(FileStruct fileStruct) {
                ToastUtil.showToastShort(R.string.delete_file_success);
                filesMutableLiveData.postValue(FileBrowseManager.getInstance().getCurrentFileStructs(mSDCardBean));
            }

            @Override
            public void onError(int code, FileStruct fileStruct) {
                ToastUtil.showToastShort(R.string.delete_file_failure);
            }

            @Override
            public void onFinish() {

            }
        });

    }


    public void loadMore() {
        int ret = FileBrowseManager.getInstance().loadMore(mSDCardBean);
        if (ret == FileBrowseConstant.ERR_LOAD_FINISHED) {
            readStateMutableLiveData.postValue(STATE_FINISH);
        } else if (ret == FileBrowseConstant.ERR_READING) {
            readStateMutableLiveData.postValue(STATE_END);
        } else if (ret != FileBrowseConstant.SUCCESS) {
            readStateMutableLiveData.postValue(STATE_FAILED);
        }
    }

    public void getMusicInfo() {
        WatchManager.getInstance().sendRcspCommand(WatchManager.getInstance().getConnectedDevice(), CommandBuilder.buildGetMusicSysInfoCmd(),
                RcspConstant.DEFAULT_SEND_CMD_TIMEOUT, new RcspCommandCallback() {
                    @Override
                    public void onCommandResponse(BluetoothDevice device, CommandBase cmd) {
                        if (cmd.getId() != Command.CMD_GET_SYS_INFO) return;
                        if (cmd.getStatus() != StateCode.STATUS_SUCCESS) {
                            onErrCode(null, null);
                            return;
                        }
                        GetSysInfoCmd sysInfoCmd = (GetSysInfoCmd) cmd;
                        SysInfoResponse sysInfoResponse = sysInfoCmd.getResponse();
                        if (sysInfoResponse.getFunction() != AttrAndFunCode.SYS_INFO_FUNCTION_MUSIC)
                            return;
                        musicInfoHandler.parseMusicData(device, sysInfoResponse.getAttrs());
                    }

                    @Override
                    public void onErrCode(BluetoothDevice device, BaseError error) {
                        ToastUtil.showToastShort(R.string.get_music_info_failed);
                    }
                });
    }


    @Override
    protected void onCleared() {
        FileBrowseManager.getInstance().removeFileObserver(this);
        WatchManager.getInstance().unregisterOnRcspCallback(musicInfoHandler);
        super.onCleared();
    }

    @Override
    public void onFileReceiver(List<FileStruct> fileStructs) {
//        if (filesMutableLiveData.getValue() != null) {
//            filesMutableLiveData.getValue().addAll(fileStructs);
//        }
//        filesMutableLiveData.setValue(filesMutableLiveData.getValue());
    }

    @Override
    public void onFileReadStop(boolean isEnd) {
        JL_Log.e("sen", "onFileReadStop--->" + isEnd);
        filesMutableLiveData.postValue(FileBrowseManager.getInstance().getCurrentFileStructs(mSDCardBean));
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

    private final BluetoothEventListener listener = new BluetoothEventListener() {
        @Override
        public void onConnection(BluetoothDevice device, int status) {
            super.onConnection(device, status);
            if (status != StateCode.CONNECTION_OK && status != StateCode.CONNECTION_CONNECTED) {
                SDCardsMutableLiveData.postValue(new ArrayList<>());
            }
        }
    };

    private final MusicInfoHandler musicInfoHandler = new MusicInfoHandler() {
        @Override
        protected void onMusicNameChange(MusicNameInfo musicNameInfo) {
            super.onMusicNameChange(musicNameInfo);
            MusicPlayInfo info = musicPlayInfoLiveData.getValue();
            if (null == info) return;
            info.setMusicNameInfo(musicNameInfo);
            musicPlayInfoLiveData.postValue(info);

        }

        @Override
        protected void onPlayModeChange(PlayModeInfo playModeInfo) {
            super.onPlayModeChange(playModeInfo);
            MusicPlayInfo info = musicPlayInfoLiveData.getValue();
            if (null == info) return;
            info.setPlayModeInfo(playModeInfo);
            musicPlayInfoLiveData.postValue(info);

        }

        @Override
        protected void onMusicStatusChange(MusicStatusInfo musicStatusInfo) {
            super.onMusicStatusChange(musicStatusInfo);
            MusicPlayInfo info = musicPlayInfoLiveData.getValue();
            if (null == info) return;
            info.setMusicStatusInfo(musicStatusInfo);
            musicPlayInfoLiveData.postValue(info);
        }

        @Override
        protected void onDeviceModeChange(int mode) {
            super.onDeviceModeChange(mode);
            MusicPlayInfo info = musicPlayInfoLiveData.getValue();
            if (null == info) return;
            info.setDeviceMode(mode);
            musicPlayInfoLiveData.postValue(info);
        }
    };
}
