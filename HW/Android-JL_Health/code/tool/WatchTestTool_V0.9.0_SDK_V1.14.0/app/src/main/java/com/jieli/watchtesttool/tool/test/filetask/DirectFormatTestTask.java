package com.jieli.watchtesttool.tool.test.filetask;

import com.jieli.jl_filebrowse.FileBrowseManager;
import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.jl_filebrowse.interfaces.OperatCallback;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.WatchApplication;
import com.jieli.watchtesttool.tool.test.AbstractTestTask;
import com.jieli.watchtesttool.tool.test.ITaskFactory;
import com.jieli.watchtesttool.tool.test.ITestTask;
import com.jieli.watchtesttool.tool.test.TestError;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/25/21
 * @desc :
 */
public class DirectFormatTestTask extends AbstractTestTask {

    private final String tag = DirectFormatTestTask.class.getSimpleName();
    private final SDCardBean sdCardBean;
    public boolean run;


    public DirectFormatTestTask(SDCardBean sdCardBean) {
        super();
        this.sdCardBean = sdCardBean;
    }


    @Override
    public void stopTest() {
        onTestLog("改任务不能取消，请等待任务结束");
    }

    @Override
    public String getName() {
        return WatchApplication.getWatchApplication().getString(R.string.func_fast_formatting);
    }


    @Override
    public void startTest() {
        run = true;
        int ret = FileBrowseManager.getInstance().formatDevice(sdCardBean, new OperatCallback() {
            @Override
            public void onSuccess() {
                next(new TestError(0, "直接格式化成功"));
                run = false;
            }

            @Override
            public void onError(int i) {
                next(new TestError(i, "直接格式化失败 code = " + i));
                run = false;
            }
        });

        if (ret != 0) {
            run = false;
            next(new TestError(ret, "直接格式化失败 code = " + ret));
        }

    }


    public static class Factory implements ITaskFactory {
        private final SDCardBean sdCardBean;

        public Factory(SDCardBean sdCardBean) {
            this.sdCardBean = sdCardBean;
        }

        @Override
        public ITestTask create() throws Exception {
            return new DirectFormatTestTask(sdCardBean);
        }
    }


}
