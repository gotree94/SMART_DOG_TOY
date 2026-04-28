package com.jieli.watchtesttool.tool.test;

import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/23/21
 * @desc 通用task处理抽象类：责任链实现和log处理
 */
public abstract class AbstractTestTask implements ITestTask {
    protected String tag = getClass().getSimpleName();

    private INextTask nextTask;
    private OnTestLogCallback mLogCallback;

    @Override
    public void onTestLog(String msg) {
        JL_Log.d(tag,"onTestLog", "thread : " + Thread.currentThread().getName() + " " + msg);
        if (mLogCallback != null) {
            mLogCallback.onLog(msg);
        }
    }

    @Override
    public void setOnTestLogCallback(OnTestLogCallback callback) {
        mLogCallback = callback;
    }


    @Override
    public void next(TestError error) {
        onTestLog(error.msg);
        if (nextTask != null) nextTask.next(error);
    }

    @Override
    public void setINextTask(INextTask nextTask) {
        this.nextTask = nextTask;
    }
}
