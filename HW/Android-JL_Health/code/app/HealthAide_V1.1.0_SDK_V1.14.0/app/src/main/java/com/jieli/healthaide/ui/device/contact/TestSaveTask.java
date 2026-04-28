package com.jieli.healthaide.ui.device.contact;

import android.text.TextUtils;

import com.jieli.component.utils.FileUtil;
import com.jieli.healthaide.HealthApplication;
import com.jieli.jl_rcsp.impl.RcspOpImpl;
import com.jieli.jl_rcsp.task.CallTransferTask;
import com.jieli.jl_rcsp.util.JL_Log;

import java.io.File;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/18/21 1:41 PM
 * @desc :
 */
public class TestSaveTask extends CallTransferTask {


    static final String CACHE_PATH = HealthApplication.getAppViewModel().getApplication().getExternalCacheDir() + File.separator + "test_call.txt";
    private String path;

    public TestSaveTask(RcspOpImpl rcspOp) {
        super(rcspOp, "", new Param());
    }

    public TestSaveTask(RcspOpImpl rcspOp, String path) {
        super(rcspOp, "", new Param());
        this.path = path;
    }


    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public void start() {

        new Thread() {
            @Override
            public void run() {
                try {
                    listener.onBegin();
                    Thread.sleep(1000);
                    if (TextUtils.isEmpty(path)) {
                        listener.onError(CallTransferTask.ERR_PARAM, "文件错误");
                        return;
                    }
                    Thread.sleep(1000);
                    byte[] data = FileUtil.getBytes(path);
                    FileUtil.bytesToFile(data, CACHE_PATH);
                    File file = new File(CACHE_PATH);

                    JL_Log.i("TestSaveTask", "TestSaveTask", "save contact file size --->" + file.length() + "\t data length = " + data.length);

                    listener.onFinish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }.start();


    }


}
