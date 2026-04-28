package com.jieli.healthaide.demos;

import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.task.SimpleTaskListener;
import com.jieli.jl_rcsp.task.smallfile.QueryFileTask;
import com.jieli.jl_rcsp.task.smallfile.ReadFileTask;

import java.util.Collections;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/11/1
 * @desc :
 */
public class HealthFileSyncDemo {
    final byte type;
    final WatchManager mWatchManager;


    /**
     * @param type 文件类型:
     *             QueryFileTask.TYPE_HEART_RATE;
     *             QueryFileTask.TYPE_BLOOD_OXYGEN;
     *             QueryFileTask.TYPE_SLEEP;
     *             QueryFileTask.TYPE_STEP;
     */
    public HealthFileSyncDemo(byte type) {

        this.type = type;
        mWatchManager = WatchManager.getInstance();
    }

    public void start() {
        //获取小文件列表
        QueryFileTask task = new QueryFileTask(mWatchManager, new QueryFileTask.Param(type));
        task.setListener(new SimpleTaskListener() {
            @Override
            public void onFinish() {
                super.onFinish();
                List<QueryFileTask.File> list = task.getFiles();
                Collections.reverse(list);//倒序，从后面往回读
                readFileInfoRecursion(list);
            }

            @Override
            public void onError(int code, String msg) {
                super.onError(code, msg);
                //获取文件列表失败 结束
            }
        });
        task.start();
    }

    /**
     * 递归读取文小文件
     *
     * @param list
     */
    private void readFileInfoRecursion(List<QueryFileTask.File> list) {
        if (list.size() < 1) {
            //没有文件，结束
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
                //获取文件内容失败,不应该继续读取，结束
            }
        });
    }

    /**
     * 读取小文件头内容
     */
    private void readFileHeader(QueryFileTask.File file, final SimpleTaskListener simpleTaskListener) {
        int size = 30;
        ReadFileTask readFileTask = new ReadFileTask(mWatchManager, new ReadFileTask.Param(file.type, file.id, size, 0));
        readFileTask.setListener(new SimpleTaskListener() {
            @Override
            public void onError(int code, String msg) {
                super.onError(code, msg);
                //获取文件头失败
                simpleTaskListener.onError(code, msg);
            }

            @Override
            public void onFinish() {
                //todo 和本地数据库比对
                byte[] data = readFileTask.getReadData();
                if (isInLocal(data)) {
                    //一个文件读取结束
                    simpleTaskListener.onFinish();
                } else {
                    readFileData(file, simpleTaskListener);
                }

            }
        });
        readFileTask.start();
    }


    /**
     * 读取小文件内容
     */
    private void readFileData(QueryFileTask.File file, SimpleTaskListener simpleTaskListener) {
        ReadFileTask readFileTask = new ReadFileTask(mWatchManager, new ReadFileTask.Param(file.type, file.id, file.size, 0));
        readFileTask.setListener(new SimpleTaskListener() {
            @Override
            public void onError(int code, String msg) {
                simpleTaskListener.onError(code, msg);
            }

            @Override
            public void onFinish() {
                byte[] data = readFileTask.getReadData();
                //  todo 保存到数据库
                saveToDb(data);
                simpleTaskListener.onFinish();//一个文件读取结束

            }
        });

        readFileTask.start();
    }

    /**
     * 和本地数据库比对
     */
    protected boolean isInLocal(byte[] data) {
        //todo 判断文件是否已经读取过
        //1.获取data 内的文件crc
        //2. 在本地查找是否有和该文件一样的crc文件，如有则读取过
        return false;
    }


    /**
     * 保存到数据
     */
    protected void saveToDb(byte[] data) {
        //todo 保存文件到本地
    }


}