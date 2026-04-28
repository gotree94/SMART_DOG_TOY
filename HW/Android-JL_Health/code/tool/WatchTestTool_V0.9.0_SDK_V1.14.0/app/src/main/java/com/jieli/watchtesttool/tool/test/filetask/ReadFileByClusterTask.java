package com.jieli.watchtesttool.tool.test.filetask;

import com.jieli.jl_filebrowse.bean.FileStruct;
import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.jl_rcsp.impl.RcspOpImpl;
import com.jieli.jl_rcsp.task.GetFileByClusterTask;
import com.jieli.jl_rcsp.task.TaskListener;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.watchtesttool.WatchApplication;
import com.jieli.watchtesttool.tool.test.AbstractTestTask;
import com.jieli.watchtesttool.tool.test.ITaskFactory;
import com.jieli.watchtesttool.tool.test.ITestTask;
import com.jieli.watchtesttool.tool.test.TestError;

import java.io.File;
import java.util.Locale;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 6/10/21
 * @desc :
 */
public class ReadFileByClusterTask extends AbstractTestTask implements TaskListener {

    public static final String READ_FILE_DIR = WatchApplication.getWatchApplication().getExternalCacheDir() + File.separator + "clusters";
    private final String tag = getClass().getSimpleName();
    private final GetFileByClusterTask task;

    private final SDCardBean sdCardBean;
    private final FileStruct fileStruct;
    private final RcspOpImpl rcspOp;

    public ReadFileByClusterTask(RcspOpImpl rcspOp, SDCardBean sdCardBean, FileStruct fileStruct) {
        this.sdCardBean = sdCardBean;
        this.fileStruct = fileStruct;
        this.rcspOp = rcspOp;

        File file = new File(READ_FILE_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(READ_FILE_DIR + File.separator + fileStruct.getName());
        GetFileByClusterTask.Param param = new GetFileByClusterTask.Param(sdCardBean.getDevHandler(), 0, fileStruct.getCluster(), file.getPath());
        task = new GetFileByClusterTask(rcspOp, param);


    }

    @Override
    public void startTest() {
        task.setListener(this);
        task.start();
    }

    @Override
    public void stopTest() {
        task.cancel((byte) 1);
    }

    @Override
    public String getName() {
        return "通过簇号获取文件";
    }

    @Override
    public void onBegin() {
        onTestLog(String.format("----获取文件 (%s) 开始----", fileStruct.getName()));
    }

    @Override
    public void onProgress(int i) {
        onTestLog(String.format(Locale.getDefault(), "获取文件进度progress %d \nfilename = %s", i, fileStruct.getName()));
    }

    @Override
    public void onFinish() {
        next(new TestError(0, fileStruct.getName() + "\t获取成功"));
    }

    @Override
    public void onError(int i, String s) {
        JL_Log.e(tag, String.format(Locale.getDefault(),  "----获取%s 失败----code =  %d  msg = %s", fileStruct.toString(), i, s));
        next(new TestError(i, String.format(Locale.getDefault(),  "获取 %s 失败code =  %d  msg = %s", fileStruct.getName(), i, s)));
    }

    @Override
    public void onCancel(int i) {
        next(new TestError(-1, "取消文件获取"));
    }


    public static class Factory implements ITaskFactory {
        private final SDCardBean sdCardBean;
        private final FileStruct fileStruct;
        private final RcspOpImpl rcspOp;

        public Factory(RcspOpImpl rcspOp, SDCardBean sdCardBean, FileStruct fileStruct) {
            this.sdCardBean = sdCardBean;
            this.fileStruct = fileStruct;
            this.rcspOp = rcspOp;
        }

        @Override
        public ITestTask create() throws Exception {
            return new ReadFileByClusterTask(rcspOp, sdCardBean, fileStruct);
        }
    }
}
