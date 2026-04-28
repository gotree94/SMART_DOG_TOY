package com.jieli.watchtesttool.tool.test.fattask;

import com.jieli.jl_fatfs.FatFsErrCode;
import com.jieli.jl_fatfs.interfaces.OnFatFileProgressListener;
import com.jieli.jl_fatfs.model.FatFile;
import com.jieli.jl_fatfs.utils.FatUtil;
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
public class FatDeleteWatchTask extends AbstractTestTask implements OnFatFileProgressListener {
    private final WatchManager watchManager;

    private boolean run;
    private final FatFile fatFile;

    public FatDeleteWatchTask(WatchManager watchManager, FatFile fatFile) {
        this.watchManager = watchManager;
        this.fatFile = fatFile;
    }

    @Override
    public void startTest() {
        run = true;
        watchManager.deleteWatchFile(fatFile.getPath(), this);
    }

    @Override
    public void stopTest() {
        if (run) {
            run = false;
        }
    }

    @Override
    public String getName() {
        return "表盘删除";
    }

    @Override
    public void onStart(String s) {
        String msg = String.format(Locale.getDefault(), "----删除%s开始----\n路径: %s", getName(), s);
        onTestLog(msg);
    }

    @Override
    public void onProgress(float v) {
        int progress = Math.round(v);
        if (progress > 100) {
            progress = 99;
        }
        String msg = String.format(Locale.getDefault(), "----删除%s进度----%d", getName(), progress);
        onTestLog(msg);
    }

    @Override
    public void onStop(int i) {
        if (i == FatFsErrCode.RES_OK) {
            //删除成功，继续插入操作
            next(new TestError(0, getName() + "成功"));
        } else {
            String msg = String.format(Locale.getDefault(), "----删除%s结束----\n结果: 失败，%s", getName(), FatUtil.getFatFsErrorCodeMsg(i));
            next(new TestError(i, msg));
        }
    }
}
