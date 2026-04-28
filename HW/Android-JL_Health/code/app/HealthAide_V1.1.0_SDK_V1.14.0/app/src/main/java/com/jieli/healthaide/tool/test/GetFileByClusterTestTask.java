package com.jieli.healthaide.tool.test;

import com.jieli.component.thread.ThreadManager;
import com.jieli.healthaide.HealthApplication;
import com.jieli.jl_rcsp.impl.RcspOpImpl;
import com.jieli.jl_rcsp.task.ITask;
import com.jieli.jl_rcsp.task.TaskListener;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 5/8/21
 * @desc :
 */
public class GetFileByClusterTestTask extends Thread implements ITask {
    private String outPath;
    private TaskListener listener;

    public GetFileByClusterTestTask(RcspOpImpl mRcspOp, String outPath, int cluster) {
        this.outPath = outPath;
    }

    @Override
    public synchronized void start() {
        //super.start();
        ThreadManager.getInstance().postRunnable(this);
    }

    @Override
    public void run() {
        try {
            listener.onBegin();
            OutputStream os = new FileOutputStream(outPath);
            InputStream is = HealthApplication.getAppViewModel().getApplication().getAssets()
                    .open("5678.SPT");
            byte[] buf = new byte[is.available()];
            is.read(buf);
            os.write(buf);
            os.close();
            listener.onFinish();
        } catch (Exception e) {
            e.printStackTrace();
            listener.onError(-1, e.getMessage());
        }

    }

    @Override
    public void cancel(byte reason) {

    }

    @Override
    public boolean isRun() {
        return isAlive();
    }

    @Override
    public void setListener(TaskListener listener) {
        this.listener = listener;

    }
}
