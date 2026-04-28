package com.jieli.healthaide.tool.watch.synctask;

import com.jieli.jl_rcsp.task.SimpleTaskListener;
import com.jieli.jl_rcsp.task.smallfile.QueryFileTask;
import com.jieli.jl_rcsp.task.smallfile.ReadFileTask;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.Collections;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/11/1
 * @desc :
 */
public abstract class SmallFileSyncTask extends DeviceSyncTask {
    final byte type;


    public SmallFileSyncTask(byte type, SyncTaskFinishListener finishListener) {
        super(finishListener);
        this.type = type;
    }

    @Override
    public void start() {
        if (mWatchManager.isFirmwareOTA()) {
            JL_Log.w(tag, "start", "device's ota is in progress.");
            if (finishListener != null) finishListener.onFinish();
            return;
        }
        listFile();
    }

    private void listFile() {
        QueryFileTask task = new QueryFileTask(mWatchManager, new QueryFileTask.Param(type));
        task.setListener(new SimpleTaskListener() {
            @Override
            public void onFinish() {
                super.onFinish();
                JL_Log.i(tag, "listFile", "获取文件列表成功。 clazz : " + getClass().getSimpleName());
                List<QueryFileTask.File> list = task.getFiles();
                Collections.reverse(list);//倒序，从后面往回读
                readFileInfoRecursion(list);
            }

            @Override
            public void onError(int code, String msg) {
                super.onError(code, msg);
                JL_Log.w(tag, "listFile", "获取运动记录列表失败。code : " + code);
                finishListener.onFinish();
            }
        });
        task.start();
    }


    private void readFileInfoRecursion(List<QueryFileTask.File> list) {
        if (mWatchManager.isFirmwareOTA()) {
            JL_Log.w(tag, "readFileInfoRecursion", "device's ota is in progress.");
            if (finishListener != null) finishListener.onFinish();
            return;
        }
        if (list.isEmpty()) {
            finishListener.onFinish();//结束
            return;
        }
        readFileHeader(list.get(0), new SimpleTaskListener() {
            @Override
            public void onFinish() {
                list.remove(0);
                readFileInfoRecursion(list);
            }

            @Override
            public void onError(int code, String msg) {
                JL_Log.w(tag, "readFileInfoRecursion", "获取运动记录失败。 code = " + code + "\tmsg = " + msg);
                finishListener.onFinish();
            }
        });
    }


    private void readFileHeader(QueryFileTask.File file, final SimpleTaskListener simpleTaskListener) {
        int size = 30;
        ReadFileTask readFileTask = new ReadFileTask(mWatchManager, new ReadFileTask.Param(file.type, file.id, size, 0));
        readFileTask.setListener(new SimpleTaskListener() {
            @Override
            public void onError(int code, String msg) {
                super.onError(code, msg);
                JL_Log.w(tag, "readFileHeader", "获取文件头失败。 code = " + code + "\tmsg = " + msg);
                simpleTaskListener.onError(code, msg);

            }

            @Override
            public void onFinish() {
                //todo 和本地数据库比对
                byte[] data = readFileTask.getReadData();
                if (isInLocal(data)) {
                    simpleTaskListener.onFinish();//如果本地数据库存在，则直接退出
                    JL_Log.w(tag, "readFileHeader", "本地数据库存在。file id = " + file.id);
                } else {
                    readFileData(file, simpleTaskListener);
                }

            }
        });
        readFileTask.start();
    }


    private void readFileData(QueryFileTask.File file, SimpleTaskListener simpleTaskListener) {
        ReadFileTask readFileTask = new ReadFileTask(mWatchManager, new ReadFileTask.Param(file.type, file.id, file.size, 0));
        readFileTask.setListener(new SimpleTaskListener() {
            @Override
            public void onError(int code, String msg) {
                JL_Log.w(tag, "readFileData", "获取文件内容失败。code = " + code + "\tmsg = " + msg);
                simpleTaskListener.onError(code, msg);
            }

            @Override
            public void onFinish() {
                JL_Log.w(tag, "readFileData", "获取文件内容成功");

                byte[] data = readFileTask.getReadData();
                //  todo 保存到数据库
                saveToDb(data);
                simpleTaskListener.onFinish();
            }
        });

        readFileTask.start();
    }

    /**
     * 和本地数据库比对
     */
    protected abstract boolean isInLocal(byte[] data);


    /**
     * 保存到数据
     */
    protected abstract void saveToDb(byte[] data);


}
