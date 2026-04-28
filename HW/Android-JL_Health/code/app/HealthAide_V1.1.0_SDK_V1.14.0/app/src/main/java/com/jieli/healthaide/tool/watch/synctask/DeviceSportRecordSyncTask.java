package com.jieli.healthaide.tool.watch.synctask;

import android.text.TextUtils;

import com.jieli.component.utils.FileUtil;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.data.db.HealthDatabase;
import com.jieli.healthaide.data.entity.SportRecord;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_filebrowse.FileBrowseManager;
import com.jieli.jl_filebrowse.bean.FileStruct;
import com.jieli.jl_filebrowse.bean.Folder;
import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.jl_filebrowse.interfaces.FileObserver;
import com.jieli.jl_filebrowse.interfaces.SimpleFileObserver;
import com.jieli.jl_filebrowse.util.DeviceChoseUtil;
import com.jieli.jl_rcsp.task.GetFileByClusterTask;
import com.jieli.jl_rcsp.task.ITask;
import com.jieli.jl_rcsp.task.TaskListener;
import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.jl_rcsp.util.JL_Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 5/7/21
 * @desc : 同步固件的运动记录到手机
 * @deprecated 通过 {@link DeviceSportRecordSyncTaskModify }替代
 */
@Deprecated
public class DeviceSportRecordSyncTask extends DeviceSyncTask {
    private final String tag = getClass().getSimpleName();
    private final FileBrowseManager mFileBrowseManager = FileBrowseManager.getInstance();
    private String uid;

    public DeviceSportRecordSyncTask(SyncTaskFinishListener finishListener) {
        super(finishListener);
    }


    @Override
    public int getType() {
        return TASK_TYPE_SYNC_DEVICE_SPORT_FILE;
    }

    @Override
    public void start() {
        if (mWatchManager.isFirmwareOTA()) {
            JL_Log.w(tag, "start", "device's ota is in progress.");
            if (finishListener != null) finishListener.onFinish();
            return;
        }
        JL_Log.i(tag, "start", "开始同步设备的运动记录");
        uid = HealthApplication.getAppViewModel().getUid();

        if (TextUtils.isEmpty(uid)) {
            JL_Log.i(tag, "start", "uid is Empty");

            return;
        }
        if (checkEnv()) {
            readRecordFolder();
        } else {
            JL_Log.w(tag, "start", "设备环境不支持");
            finishListener.onFinish();
        }
    }


    @Override
    public void setFinishListener(SyncTaskFinishListener finishListener) {
        this.finishListener = finishListener;
    }

    /**
     * 检测环境是否正常
     *
     * @return true 设备环境正常  false 设备环境不支持。
     */
    private boolean checkEnv() {
        SDCardBean sdCardBean = DeviceChoseUtil.getTargetDev();
        return sdCardBean != null;
    }

    /**
     * 获取运动记录文件夹
     */
    private void readRecordFolder() {
        SDCardBean sdCardBean = DeviceChoseUtil.getTargetDev();
        JL_Log.i(tag, "readRecordFolder", "读取Record文件夹");
        Folder folder = mFileBrowseManager.getCurrentReadFile(sdCardBean);//获取当前的目录
        //返回根目录
        while (folder.getParent() != null) {
            mFileBrowseManager.backBrowse(sdCardBean, false);
            folder = mFileBrowseManager.getCurrentReadFile(sdCardBean);
        }
        List<FileStruct> files = folder.getChildFileStructs();

        FileStruct recordFileStruct = null;
        for (FileStruct fileStruct : files) {
            if (!fileStruct.isFile() && fileStruct.getName().equalsIgnoreCase("record")) {
                recordFileStruct = fileStruct;
                break;
            }
        }
        //没有读取到record文件夹并没有读取完毕
        if (recordFileStruct == null && !folder.isLoadFinished(false)) {
            mFileBrowseManager.addFileObserver(new AutoRemoveFileObserver() {
                @Override
                public void onFileReadStop(boolean isEnd) {
                    super.onFileReadStop(isEnd);
                    readRecordFolder();//继续读取record文件夹
                }

            });
            mFileBrowseManager.loadMore(sdCardBean);//读取根目录文件
        } else if (recordFileStruct != null) {
            readRecordList(recordFileStruct);
        } else {
            JL_Log.w(tag, "readRecordFolder", "没有找到record文件夹");
            finishListener.onFinish();
        }

    }

    /**
     * 读取运动记录文件列表
     */
    private void readRecordList(FileStruct fileStruct) {
        SDCardBean sdCardBean = DeviceChoseUtil.getTargetDev();
        JL_Log.i(tag, "readRecordList", "读取运动记录文件列表 ");
        Folder folder = mFileBrowseManager.getCurrentReadFile(sdCardBean);
        if (null == folder) return;
        JL_Log.i(tag, "readRecordList", "读取运动记录文件列表  folder " + folder.getFileStruct().toString());
        FileObserver fileObserver = new AutoRemoveFileObserver() {
            @Override
            public void onFileReadStop(boolean isEnd) {
                super.onFileReadStop(isEnd);
                readRecordList(fileStruct);//继续读取问价列表
            }
        };
        JL_Log.e(tag, "readRecordList", " record status   = " + folder.isLoadFinished(false));

        mFileBrowseManager.addFileObserver(fileObserver);
        if (folder.getFileStruct() != fileStruct) {
            //当前文件夹不是record文件夹
            mFileBrowseManager.appenBrowse(fileStruct, sdCardBean);
        } else if (!folder.isLoadFinished(false)) {
            //record文件夹没有读取完毕
            mFileBrowseManager.loadMore(sdCardBean);
        } else {
            mFileBrowseManager.removeFileObserver(fileObserver);
            compareWithLocalDb();
        }
    }


    /**
     * 和本地数据库比对
     */
    private void compareWithLocalDb() {
        SDCardBean sdCardBean = DeviceChoseUtil.getTargetDev();
        JL_Log.i(tag, "compareWithLocalDb", "和本地数据库比对");
        List<FileStruct> fileStructs = mFileBrowseManager.getCurrentFileStructs(sdCardBean);
        if (fileStructs == null || fileStructs.isEmpty()) {
            JL_Log.d(tag, "compareWithLocalDb", "无运动记录");
            finishListener.onFinish();
            return;
        }

        List<FileStruct> syncFiles = new ArrayList<>();
        for (FileStruct fileStruct : fileStructs) {
            JL_Log.i(tag, "compareWithLocalDb", fileStruct.toString());
            String name = fileStruct.getName();
            SportRecord sportRecord = HealthDatabase
                    .buildHealthDb(HealthApplication.getAppViewModel().getApplication())
                    .SportRecordDao().findByStartTime(uid, new Date().getTime());
            JL_Log.d(tag, "compareWithLocalDb", "findByName -->" + sportRecord);
            if (sportRecord == null) {
                syncFiles.add(fileStruct);
            }
        }
        getRecordFile(syncFiles);
    }


    /**
     * 读取运动记录文件到手机
     *
     * @param syncFiles
     */
    private void getRecordFile(final List<FileStruct> syncFiles) {
        SDCardBean sdCardBean = DeviceChoseUtil.getTargetDev();
        if (null == sdCardBean) return;
        if (!syncFiles.isEmpty()) {
            FileStruct fileStruct = syncFiles.remove(0);
            String outPath = HealthApplication.getAppViewModel().getApplication().getCacheDir().getPath()
                    + File.separator + "record_temp_file";
            GetFileByClusterTask.Param param = new GetFileByClusterTask.Param(sdCardBean.getDevHandler(), 0,
                    fileStruct.getCluster(), outPath);
            ITask task = new GetFileByClusterTask(WatchManager.getInstance(), param);
            task.setListener(new TaskListener() {
                @Override
                public void onBegin() {

                }

                @Override
                public void onProgress(int progress) {

                }

                @Override
                public void onFinish() {
                    String name = fileStruct.getName();
                    byte[] data = FileUtil.getBytes(outPath);
                    JL_Log.d(tag, "getRecordFile", "file data-->" + CHexConver.byte2HexStr(data));
                    saveDataToDb(name, data);
                    getRecordFile(syncFiles);
                }

                @Override
                public void onError(int code, String msg) {
                    JL_Log.e(tag, "getRecordFile", "获取文件失败");
                }

                @Override
                public void onCancel(int reason) {
                    JL_Log.e(tag, "getRecordFile", "取消文件传输");
                }
            });
            task.start();
        } else {
            finishListener.onFinish();
        }

    }

    private void saveDataToDb(String name, byte[] data) {
        SportRecord sportRecord = SportRecord.from(data);
        sportRecord.setUid(uid);
        HealthDatabase.getInstance().SportRecordDao().insert(sportRecord);
    }


    private static class AutoRemoveFileObserver extends SimpleFileObserver {

        @Override
        public void onFileReadStop(boolean isEnd) {
            FileBrowseManager.getInstance().removeFileObserver(this);
        }


        @Override
        public void onFileReadFailed(int reason) {
            FileBrowseManager.getInstance().removeFileObserver(this);
        }

        @Override
        public void onSdCardStatusChange(List<SDCardBean> onLineCards) {
            FileBrowseManager.getInstance().removeFileObserver(this);
        }

    }


}
