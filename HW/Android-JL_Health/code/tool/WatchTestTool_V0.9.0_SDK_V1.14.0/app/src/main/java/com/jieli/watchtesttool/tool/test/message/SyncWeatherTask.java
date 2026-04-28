package com.jieli.watchtesttool.tool.test.message;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.jieli.jl_rcsp.impl.WatchOpImpl;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.command.watch.PushInfoDataToDeviceCmd;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.WatchApplication;
import com.jieli.watchtesttool.tool.test.AbstractTestTask;
import com.jieli.watchtesttool.tool.test.TestError;

import java.util.Locale;
import java.util.Random;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 同步天气任务
 * @since 2022/8/23
 */
public class SyncWeatherTask extends AbstractTestTask {

    private static final int DELAY_TIME = 2000;

    private final WatchOpImpl mWatchOp;
    private final int mTestCount;
    private final PushInfoDataToDeviceCmd.Weather[] mWeatherList;

    private final Handler mUIHandler = new Handler(Looper.getMainLooper());
    private boolean isTestRunning;
    private int count;
    private int lastIndex = -1;

    public SyncWeatherTask(WatchOpImpl watchOp, PushInfoDataToDeviceCmd.Weather weather) {
        this(watchOp, 1, weather);
    }

    public SyncWeatherTask(WatchOpImpl watchOp, int testCount, PushInfoDataToDeviceCmd.Weather... weatherList) {
        mWatchOp = watchOp;
        if (testCount < 1) testCount = 1;
        mTestCount = testCount;
        mWeatherList = weatherList;
    }

    @Override
    public void startTest() {
        if (!mWatchOp.isWatchSystemOk()) {
            callbackError(TestError.ERR_DEVICE_NOT_CONNECT);
            return;
        }
        isTestRunning = true;
        count = 0;
        onTestLog("准备开始同步天气测试, 总次数: " + mTestCount);
        startTestSyncWeather();
    }

    @Override
    public void stopTest() {
        if (isTestRunning) {
            callbackError(TestError.ERR_SUCCESS);
        }
//        onTestLog("停止测试");
    }

    @Override
    public String getName() {
        return WatchApplication.getWatchApplication().getString(R.string.func_weather_sync);
    }

    private int getRandom(int bound) {
        return new Random().nextInt(bound) % bound;
    }

    @Nullable
    private PushInfoDataToDeviceCmd.Weather getRandomWeather() {
        if (null == mWeatherList || mWeatherList.length == 0) return null;
        if (mWeatherList.length == 1) return mWeatherList[0];
        int random = getRandom(mWeatherList.length);
        while (random == lastIndex) {
            random = getRandom(mWeatherList.length);
        }
        lastIndex = random;
        return mWeatherList[random];
    }

    private void startTestSyncWeather() {
        if (!isTestRunning) return;
        PushInfoDataToDeviceCmd.Weather weather = getRandomWeather();
        if (null == weather) {
            callbackError(TestError.ERR_INVALID_PARAM);
            return;
        }
        onTestLog("开始同步天气:" + weather + ",\n 测试次数: " + (count + 1));
        mWatchOp.syncWeatherInfo(weather, new OnWatchOpCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                count++;
                if (count >= mTestCount) {
                    callbackError(TestError.ERR_SUCCESS);
                } else {
                    mUIHandler.postDelayed(() -> startTestSyncWeather(), DELAY_TIME);
                }
            }

            @Override
            public void onFailed(BaseError error) {
                String text = String.format(Locale.getDefault(), "code:%d, %s", error.getCode(), error.getMessage());
                callbackError(TestError.ERR_FAILED, text);
            }
        });
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
        mUIHandler.removeCallbacksAndMessages(null);
    }
}
