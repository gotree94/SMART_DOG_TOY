package com.jieli.watchtesttool.tool.test.message;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.jieli.jl_rcsp.impl.WatchOpImpl;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.NotificationMsg;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.WatchApplication;
import com.jieli.watchtesttool.tool.test.AbstractTestTask;
import com.jieli.watchtesttool.tool.test.TestError;

import java.util.Locale;
import java.util.Random;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 同步通知信息任务
 * @since 2023/1/12
 */
public class SyncNotifyMessageTask extends AbstractTestTask {
    private static final int DELAY_TIME = 2000;
    private final WatchOpImpl mWatchOp;
    private final int mTestCount;
    private final NotificationMsg[] mMessages;

    private boolean isTestRunning;
    private int count;
    private int lastIndex = -1;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public SyncNotifyMessageTask(WatchOpImpl watchOp, NotificationMsg message) {
        this(watchOp, 1, message);
    }

    public SyncNotifyMessageTask(WatchOpImpl watchOp, int testCount, NotificationMsg... messages) {
        mWatchOp = watchOp;
        if (testCount < 1) testCount = 1;
        mTestCount = testCount;
        mMessages = messages;
    }

    @Override
    public void startTest() {
        if (!mWatchOp.isWatchSystemOk()) {
            callbackError(TestError.ERR_DEVICE_NOT_CONNECT);
            return;
        }
        isTestRunning = true;
        count = 0;
        onTestLog("准备开始同步通知信息测试, 总次数: " + mTestCount);
        startTestSyncMessage();
    }

    @Override
    public void stopTest() {
        if (isTestRunning) {
            callbackError(TestError.ERR_SUCCESS);
        }
    }

    @Override
    public String getName() {
        return WatchApplication.getWatchApplication().getString(R.string.func_message_sync);
    }

    private int getRandom(int bound) {
        return new Random().nextInt(bound) % bound;
    }

    @Nullable
    private NotificationMsg getRandomMessage() {
        if (null == mMessages || mMessages.length == 0) return null;
        if (mMessages.length == 1) return mMessages[0];
//        int random = getRandom(mMessages.length);
//        while (random == lastIndex) {
//            random = getRandom(mMessages.length);
//        }
//        lastIndex = random;
        if (lastIndex == mMessages.length - 1) {
            lastIndex = -1;
        }
        return mMessages[++lastIndex];
    }

    private void startTestSyncMessage() {
        if (!isTestRunning) return;
        NotificationMsg message = getRandomMessage();
        if (null == message) {
            callbackError(TestError.ERR_INVALID_PARAM);
            return;
        }
        onTestLog("开始同步通知信息:" + message + ",\n 测试次数: " + (count + 1));
        if (message.getOp() == NotificationMsg.OP_REMOVE) {
            mWatchOp.removeMessageInfo(message, mWatchOpCallback);
        } else {
            mWatchOp.pushMessageInfo(message, mWatchOpCallback);
        }
    }

    private void callbackError(int code) {
        callbackError(code, "");
    }

    private void callbackError(int code, String message) {
        if (isTestRunning) isTestRunning = false;
        String msg = TestError.getTestMsg(code);
        if (!TextUtils.isEmpty(message)) {
            msg = msg + "\n" + message;
        }
        next(new TestError(code, msg));
        mHandler.removeCallbacksAndMessages(null);
    }

    private final OnWatchOpCallback<Boolean> mWatchOpCallback = new OnWatchOpCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean result) {
            count++;
            JL_Log.d(tag, "OnWatchOpCallback >> " + count);
            if (count >= mTestCount) {
                callbackError(TestError.ERR_SUCCESS);
            } else {
//                    startTestSyncMessage();
                mHandler.postDelayed(() -> startTestSyncMessage(), DELAY_TIME);
            }
        }

        @Override
        public void onFailed(BaseError error) {
            String text = String.format(Locale.getDefault(), "code:%d, %s", error.getCode(), error.getMessage());
            callbackError(TestError.ERR_FAILED, text);
        }
    };
}
