package com.jieli.healthaide.tool.aiui.handle;

import android.content.Context;

import com.jieli.healthaide.tool.watch.WatchManager;

/**
 * @ClassName: BaseAITask
 * @Description: AI任务基类
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/9/18 15:15
 */
public abstract class BaseAIHandler {
    protected final String TAG = this.getClass().getSimpleName();
    protected WatchManager mRcspOp;
    protected Context mContext;
    public BaseAIHandler(WatchManager rcspOp, Context context) {
        mRcspOp = rcspOp;
        mContext = context;
    }

    public void release() {
    }

    protected boolean isConnected() {
        return mRcspOp.isConnected();
    }
}
