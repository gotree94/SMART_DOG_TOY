package com.jieli.watchtesttool.tool.test.fattask;

import com.jieli.jl_fatfs.FatFsErrCode;
import com.jieli.jl_fatfs.interfaces.OnFatFileProgressListener;
import com.jieli.jl_fatfs.model.FatFile;
import com.jieli.jl_fatfs.utils.FatUtil;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.watchtesttool.tool.test.AbstractTestTask;
import com.jieli.watchtesttool.tool.test.TestError;
import com.jieli.watchtesttool.tool.watch.WatchManager;

import java.util.Locale;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 6/10/21
 * @desc :
 */
public class FatInsertWatchTask extends AbstractTestTask implements OnFatFileProgressListener {
    private final WatchManager watchManager;
    private final String mPath;
    public boolean run;

    public FatInsertWatchTask(WatchManager watchManager, String path) {
        this.watchManager = watchManager;
        this.mPath = path;
    }

    @Override
    public void startTest() {
        if (run) return;
        run = true;
        watchManager.createWatchFile(mPath, false, this);
    }

    @Override
    public void stopTest() {
        if (run) {
            run = false;
        }

    }

    @Override
    public String getName() {
        return "表盘测试";
    }

    @Override
    public void onStart(String s) {
        if (!run) return;
        String msg = String.format(Locale.getDefault(), "----插入%s开始----\n路径: %s", "表盘", FatUtil.getFatFilePath(s));
        onTestLog(msg);
    }

    @Override
    public void onProgress(float v) {
        if (!run) return;
        int progress = Math.round(v);
        if (progress > 100) {
            progress = 99;
        }
        String msg = String.format(Locale.getDefault(), "----插入%s进度---- %d", "表盘", progress);
        onTestLog(msg);
    }

    @Override
    public void onStop(int i) {
        if (!run) return;
        String msg = String.format(Locale.getDefault(), "----插入%s结束----\n结果：", "表盘");
        if (i == FatFsErrCode.RES_OK) {
            String fatFilePath = FatUtil.getFatFilePath(mPath);
            msg += "成功，路径：" + fatFilePath;
            onTestLog(msg);
            enableFatFile(fatFilePath);
        } else if (i == FatFsErrCode.RES_TOO_BIG) {
            msg += "失败， 空间不足";
            onTestLog(msg);
            next(new TestError(i, "空间不够"));
        } else if (i == FatFsErrCode.RES_DEVICE_IS_BUSY) {
            msg += "失败， 设备繁忙，处于通话状态";
            onTestLog(msg);
            next(new TestError(i, "设备繁忙"));
        } else {
            msg += "失败， " + i + " " + FatUtil.getFatFsErrorCodeMsg(i);
            next(new TestError(i, msg));
        }
    }


    /**
     * 使能插入文件
     *
     * @param fatFilePath fat文件路径
     */
    public void enableFatFile(final String fatFilePath) {
        if (!run) return;
        watchManager.setCurrentWatchInfo(fatFilePath, new OnWatchOpCallback<FatFile>() {
            @Override
            public void onSuccess(FatFile fatFile) {
//                Thread.dumpStack();
                String msg = String.format(Locale.getDefault(), "----设置当前表盘----\n结果: 成功，路径：%s", fatFile.getPath());
                next(new TestError(FatFsErrCode.RES_OK, msg));
            }

            @Override
            public void onFailed(BaseError baseError) {
                String msg = String.format(Locale.getDefault(), "----设置当前表盘----\n结果: 失败，发送异常，%s", baseError.getMessage());
                next(new TestError(baseError.getSubCode(), msg));
            }
        });
    }

}
