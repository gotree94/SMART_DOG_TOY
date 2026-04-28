package com.jieli.watchtesttool.tool.test;

import android.os.Handler;
import android.os.Looper;

import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.watchtesttool.util.WLog;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/23/21
 * @desc :
 */
public class TestTaskQueue extends ArrayBlockingQueue<ITestTask> implements INextTask, ITestTask, OnTestLogCallback {

    private final static String tag = TestTaskQueue.class.getSimpleName();
    public int delayTask = 3000;

    private INextTask iNextTask;
    private OnTestLogCallback mOnTestLogCallback;
    private int taskSeq;

    private OnTaskChangeCallback mOnTaskChangeCallback;

    private final Handler mDelayHandler;


    private boolean hasDelayTask = false;


    public TestTaskQueue(int capacity) {
        super(capacity);
        taskSeq = -1;
        mDelayHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public boolean add(ITestTask iTestTask) {
        iTestTask.setINextTask(this);
        iTestTask.setOnTestLogCallback(this);
        return super.add(iTestTask);
    }

    @Override
    public void put(ITestTask iTestTask) throws InterruptedException {
        iTestTask.setINextTask(this);
        super.put(iTestTask);
    }

    /**
     * 开始调用下一个任务
     */
    @Override
    public void next(TestError error) {
        JL_Log.i(tag, "next", "" + error);
        poll();//上一次任务出队列
        if (error.code != 0) {
            //失败
            stopTest();
            if (iNextTask != null) iNextTask.next(error);
        } else if (peek() == null) {
            //所有任务都执行完了
            taskSeq = -1;
            onTestLog(TestError.getTestMsg(TestError.ERR_SUCCESS));
            if (iNextTask != null) iNextTask.next(new TestError(TestError.ERR_SUCCESS));
        } else {
            hasDelayTask = true;
            mDelayHandler.postDelayed(this::startTest, delayTask);
        }

    }

    @Override
    public void startTest() {
        hasDelayTask = false;
        ITestTask task = peek();
        if (task == null) {
            return;
        }
        taskSeq++;
        onTestLog("----开始第" + taskSeq + "回测试----");
        if (mOnTaskChangeCallback != null) {
            mOnTaskChangeCallback.onTaskChange(task, taskSeq);
        }
        task.startTest();
    }

    @Override
    public void stopTest() {
        taskSeq = -1;
        ITestTask task = peek();
        if (task != null) {
            task.stopTest();
            clear();
        }
        //如果在等待下一个任务时，停止任务队列时需要外抛错误
        mDelayHandler.removeCallbacks(this::startTest);
        if (hasDelayTask) {
            if (iNextTask != null) iNextTask.next(new TestError(-1, "取消任务"));
        }
        hasDelayTask = false;
    }

    @Override
    public void onTestLog(String msg) {
        WLog.i(tag, msg);
        if (mOnTestLogCallback != null) {
            mOnTestLogCallback.onLog(msg);
        }
    }


    @Override
    public void setINextTask(INextTask nextTask) {
        this.iNextTask = nextTask;
    }

    @Override
    public void setOnTestLogCallback(OnTestLogCallback callback) {
        mOnTestLogCallback = callback;
    }


    public void setOnTaskChangeCallback(OnTaskChangeCallback mOnTaskChangeCallback) {
        this.mOnTaskChangeCallback = mOnTaskChangeCallback;
    }

    @Override
    public String getName() {
        return "测试任务队列";
    }

    @Override
    public void onLog(String log) {
        if (mOnTestLogCallback != null) {
            mOnTestLogCallback.onLog(log);
        }
    }

    public boolean isTesting() {
        return taskSeq >= 0 && !isEmpty();
    }

    public static class Factory implements ITaskFactory {
        private final int count;
        private final ITaskFactory factory;

        public Factory(int count, ITaskFactory factory) {
            this.count = count;
            this.factory = factory;
        }

        @Override
        public ITestTask create() throws Exception {
            TestTaskQueue queue = new TestTaskQueue(count);
            for (int i = 0; i < count; i++) {
                ITestTask task = factory.create();
                queue.add(task);
            }
            return queue;
        }
    }

}
