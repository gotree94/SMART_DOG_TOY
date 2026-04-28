package com.jieli.watchtesttool.tool.test.message;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.jieli.jl_bt_ota.util.FileUtil;
import com.jieli.jl_rcsp.impl.RcspOpImpl;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.task.TaskListener;
import com.jieli.jl_rcsp.task.logcat.ReadLogcatTask;
import com.jieli.watchtesttool.WatchApplication;
import com.jieli.watchtesttool.tool.test.AbstractTestTask;
import com.jieli.watchtesttool.tool.test.TestError;
import com.jieli.watchtesttool.util.AppUtil;
import com.jieli.watchtesttool.util.WatchTestConstant;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * SyncDeviceLogcatTask
 *
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 同步异常信息
 * @since 2024/6/5
 */
public class SyncDeviceLogcatTask extends AbstractTestTask {

    @NonNull
    private final RcspOpImpl mRcspOp;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
    private volatile boolean isRunning;

    private ReadLogcatTask task;
    private String filePath;

    public SyncDeviceLogcatTask(@NonNull RcspOpImpl rcspOp) {
        mRcspOp = rcspOp;
    }

    public String getOutputFilePath() {
        return filePath;
    }

    @Override
    public void startTest() {
        if (isRunning) {
            next(new TestError(TestError.ERR_TEST_IN_PROGRESS));
            return;
        }
        filePath = null;
        isRunning = true;
        //创建任务
        task = new ReadLogcatTask(mRcspOp);
        //设置监听器
        task.setListener(new TaskListener() {
            @Override
            public void onBegin() {
                //回调任务开始
            }

            @Override
            public void onProgress(int progress) {
                //回调进度
            }

            @SuppressLint("MissingPermission")
            @Override
            public void onFinish() {
                //回调任务完成
                byte[] logcatData = task.getResult(); //日志数据
                if (null != logcatData && logcatData.length > 0) {
                    final BluetoothDevice device = mRcspOp.getConnectedDevice();
                    final DeviceInfo deviceInfo = mRcspOp.getDeviceInfo(device);
                    filePath = AppUtil.createFilePath(WatchApplication.getWatchApplication(), WatchTestConstant.DIR_MESSAGE, device.getAddress()) + File.separator
                            + String.format(Locale.ENGLISH, "dev_log_%d_%d_%d_%d_%s.txt", deviceInfo.getSdkType(),
                            deviceInfo.getVid(), deviceInfo.getUid(), deviceInfo.getPid(), dateFormat.format(Calendar.getInstance()));
                    FileUtil.bytesToFile(logcatData, filePath);
                }
                next(TestError.SUCCESS);
            }

            @Override
            public void onError(int code, String msg) {
                //回调任务异常
                next(new TestError(code, msg));
            }

            @Override
            public void onCancel(int reason) {
                //回调任务被取消, 该任务暂不支持取消操作
                next(new TestError(TestError.ERR_USER_STOP));
            }
        });
        //执行任务
        task.start();
    }

    @Override
    public void stopTest() {
        if (!isRunning || null == task) return;
        task.cancel((byte) 0);
    }

    @Override
    public String getName() {
        return "同步设备异常信息";
    }

}
