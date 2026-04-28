package com.jieli.watchtesttool.tool.test.filetask;

import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.jl_rcsp.impl.WatchOpImpl;
import com.jieli.jl_rcsp.task.ITask;
import com.jieli.jl_rcsp.task.TaskListener;
import com.jieli.jl_rcsp.task.format.FormatTask;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.WatchApplication;
import com.jieli.watchtesttool.tool.test.AbstractTestTask;
import com.jieli.watchtesttool.tool.test.ITaskFactory;
import com.jieli.watchtesttool.tool.test.ITestTask;
import com.jieli.watchtesttool.tool.test.TestError;
import com.jieli.watchtesttool.tool.watch.WatchManager;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/25/21
 * @desc :
 */
public class FormatTestTask extends AbstractTestTask implements TaskListener {

    private String tag = FormatTestTask.class.getSimpleName();
    private SDCardBean sdCardBean;


    public FormatTestTask(WatchOpImpl mWatchOp, SDCardBean sdCardBean) {
        super();
        this.formatTask = new FormatTask(mWatchOp, WatchApplication.getWatchApplication(), sdCardBean);
        this.formatTask.setListener(this);
    }


    @Override
    public void stopTest() {
        onTestLog("该任务不能取消，请等待任务结束");
    }

    @Override
    public String getName() {
        return WatchApplication.getWatchApplication().getString(R.string.func_formatting);
    }


    private ITask formatTask;


    @Override
    public void startTest() {
        formatTask.start();
    }

    @Override
    public void onBegin() {

    }

    @Override
    public void onProgress(int i) {

    }

    @Override
    public void onFinish() {
       next(new TestError(0, getName() + "\tfinnish"));
    }

    @Override
    public void onError(int i, String s) {
        next(new TestError(i, getName() + "\t失败"));
    }

    @Override
    public void onCancel(int i) {

    }

    public static class Factory implements ITaskFactory {
        private SDCardBean sdCardBean;

        public Factory(SDCardBean sdCardBean) {
            this.sdCardBean = sdCardBean;
        }

        @Override
        public ITestTask create() throws Exception {
            return new FormatTestTask(WatchManager.getInstance(),sdCardBean);
        }
    }


}
