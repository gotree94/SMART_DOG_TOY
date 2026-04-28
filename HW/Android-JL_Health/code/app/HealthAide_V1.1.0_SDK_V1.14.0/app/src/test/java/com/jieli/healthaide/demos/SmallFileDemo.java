package com.jieli.healthaide.demos;

import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.task.SimpleTaskListener;
import com.jieli.jl_rcsp.task.smallfile.AddFileTask;
import com.jieli.jl_rcsp.task.smallfile.DeleteFileTask;
import com.jieli.jl_rcsp.task.smallfile.QueryFileTask;
import com.jieli.jl_rcsp.task.smallfile.ReadFileTask;
import com.jieli.jl_rcsp.task.smallfile.UpdateFileTask;

import org.junit.Test;

import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/12/1
 * @desc : 小文件传输使用demo
 */
public class SmallFileDemo {


    @Test
    public void querySmallFile() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //小文件类型：QueryFileTask.TYPE_BLOOD_OXYGEN、QueryFileTask.TYPE_SLEEP、QueryFileTask.TYPE_STEP等
        byte type = QueryFileTask.TYPE_HEART_RATE;
        QueryFileTask.Param param = new QueryFileTask.Param(type);
        QueryFileTask task = new QueryFileTask(watchManager, param);
        task.setListener(new SimpleTaskListener() {
            @Override
            public void onBegin() {
                //开始传输
            }

            @Override
            public void onFinish() {
                //查询成功
                List<QueryFileTask.File> list = task.getFiles();
            }

            @Override
            public void onError(int code, String msg) {
                //查询失败
            }
        });
        task.start();
    }

    @Test
    public void addSmallfile() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        byte type = QueryFileTask.TYPE_MESSAGE_SYNC;
        byte[] data = new byte[10];
        AddFileTask.Param param = new AddFileTask.Param(type, data);
        AddFileTask task = new AddFileTask(watchManager, param);
        task.setListener(new SimpleTaskListener() {

            @Override
            public void onBegin() {
                //开始传输
            }

            @Override
            public void onError(int code, String msg) {
                //异常
            }

            @Override
            public void onFinish() {
                //添加完成
            }
        });
        task.start();
    }


    @Test
    public void readSmallfile() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        QueryFileTask.File file = null; //通过查询文件列表获取
        byte type = file.type; //文件类型
        short id = file.id; //文件id
        int size = file.size; //要读取的内容大小
        int offset = 0;//开始读取的位置。和size结合可以实现部分读取
        ReadFileTask.Param param = new ReadFileTask.Param(type, id, size, offset);
        ReadFileTask task = new ReadFileTask(watchManager, param);
        task.setListener(new SimpleTaskListener() {

            @Override
            public void onBegin() {
                //开始传输
            }

            @Override
            public void onError(int code, String msg) {
                //异常
            }

            @Override
            public void onFinish() {
                //完成
               byte [] data =  task.getReadData(); //获取文件内容
            }
        });
        task.start();
    }

    @Test
    public void updateSmallfile() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        QueryFileTask.File file = null; //通过查询文件列表获取
        byte type = file.type; //文件类型
        short id = file.id; //文件id
//        byte [] data  = "更新的数据";
        byte[] data = new byte[100];
        UpdateFileTask.Param param = new UpdateFileTask.Param(type, id, data);
        UpdateFileTask task = new UpdateFileTask(watchManager, param);
        task.setListener(new SimpleTaskListener() {

            @Override
            public void onBegin() {
                //开始传输
            }

            @Override
            public void onError(int code, String msg) {
                //异常
            }


            @Override
            public void onFinish() {
                //完成
            }
        });
        task.start();
    }

    @Test
    public void deleteSmallfile() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        QueryFileTask.File file = null; //通过查询文件列表获取
        byte type = file.type; //文件类型
        short id = file.id; //文件id
        DeleteFileTask.Param param = new DeleteFileTask.Param(type, id);
        DeleteFileTask task = new DeleteFileTask(watchManager, param);
        task.setListener(new SimpleTaskListener() {

            @Override
            public void onBegin() {
                //开始
            }

            @Override
            public void onError(int code, String msg) {
                //异常
            }

            @Override
            public void onFinish() {
                //完成
            }
        });
        task.start();
    }
}
