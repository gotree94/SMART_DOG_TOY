package com.jieli.healthaide.ui.device.contact;

import android.text.TextUtils;

import com.jieli.component.utils.FileUtil;
import com.jieli.jl_rcsp.impl.RcspOpImpl;
import com.jieli.jl_rcsp.task.GetFileByNameTask;
import com.jieli.jl_rcsp.task.TaskListener;
import com.jieli.jl_rcsp.util.JL_Log;

import java.io.File;
import java.io.IOException;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/18/21 1:41 PM
 * @desc :
 */
public class TestReadTask extends GetFileByNameTask {


    static final String CACHE_PATH = TestSaveTask.CACHE_PATH;
    private final String path;


    public TestReadTask(RcspOpImpl mRcspOp, String outPath, String name) {
        super(mRcspOp, new Param(0, name, outPath, false));
        this.path = outPath;
    }


    @Override
    public void start() {
        new Thread() {
            @Override
            public void run() {
                TaskListener listener = TestReadTask.this.listener;
                try {
                    listener.onBegin();
                    Thread.sleep(1000);
                    if (TextUtils.isEmpty(path)) {
                        listener.onError(ERR_PARAM, "outPath is invalid.");
                        return;
                    }
                    Thread.sleep(1000);
                    if (!FileUtil.checkFileExist(CACHE_PATH)) {
                        try {
                            new File(CACHE_PATH).createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    File file = new File(CACHE_PATH);
                    byte[] data = FileUtil.getBytes(CACHE_PATH);
                    JL_Log.d("TestReadTask", "TestReadTask", "read contact file size --->" + file.length() + "\t data length = " + data.length);
                    FileUtil.bytesToFile(data, path);
                    listener.onFinish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
