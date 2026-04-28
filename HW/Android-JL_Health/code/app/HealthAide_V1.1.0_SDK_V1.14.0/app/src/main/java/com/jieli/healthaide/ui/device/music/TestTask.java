package com.jieli.healthaide.ui.device.music;

import android.os.Handler;
import android.os.Looper;

import com.jieli.jl_rcsp.impl.RcspOpImpl;
import com.jieli.jl_rcsp.task.TransferTask;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/12/21 5:23 PM
 * @desc :
 */
class TestTask extends TransferTask {
    Handler handler = new Handler(Looper.getMainLooper());

    public TestTask(RcspOpImpl mRcspOp) {
        super(mRcspOp,"",new Param());
    }

    @Override
    public void start() {
        handler.post(() -> listener.onBegin());
        new Thread() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 100; i++) {
                        Thread.sleep(50);
                        int progress = i;
                        if (listener != null) {
                            handler.post(() -> listener.onProgress(progress));
                        } else {
                            return;
                        }

                    }

                    if (listener != null) handler.post(() -> listener.onFinish());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    handler.post(() -> {
                        if (listener != null)
                            listener.onError(-2, e.getMessage());
                    });
                }

            }
        }.start();

    }

    @Override
    public void cancel(byte reason) {
        if (listener != null) {
            listener.onCancel(reason);
            listener = null;
        }

    }


}
