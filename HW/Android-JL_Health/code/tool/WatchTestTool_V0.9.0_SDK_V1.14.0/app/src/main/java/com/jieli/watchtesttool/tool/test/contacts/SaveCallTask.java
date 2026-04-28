package com.jieli.watchtesttool.tool.test.contacts;

import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.jl_rcsp.task.CallTransferTask;
import com.jieli.jl_rcsp.task.TransferTask;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.WatchApplication;
import com.jieli.watchtesttool.tool.test.ITaskFactory;
import com.jieli.watchtesttool.tool.test.ITestTask;
import com.jieli.watchtesttool.tool.test.filetask.FileTransferTask;
import com.jieli.watchtesttool.tool.watch.WatchManager;
import com.jieli.watchtesttool.util.AppUtil;
import com.jieli.watchtesttool.util.WatchTestConstant;

import java.io.File;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/23/21
 * @desc :保存联系人任务
 */
public class SaveCallTask extends FileTransferTask {


    public SaveCallTask(WatchManager watchManager, SDCardBean sdCardBean, String path) {
        super(watchManager, sdCardBean, path);
    }


    @Override
    protected TransferTask createTask(WatchManager watchManager, SDCardBean sdCardBean) {
        TransferTask.Param param = new TransferTask.Param();
        param.devHandler = sdCardBean.getDevHandler();
        param.useFlash = sdCardBean.getType() == SDCardBean.FLASH;
        return new CallTransferTask(watchManager, "", param);
    }

    @Override
    public String getName() {
        return WatchApplication.getWatchApplication().getString(R.string.func_contacts);
    }


    public static class Factory implements ITaskFactory {
        private final SDCardBean sdCardBean;

        public Factory(SDCardBean sdCardBean) {
            this.sdCardBean = sdCardBean;
        }

        @Override
        public ITestTask create() throws Exception {
            String path = AppUtil.createFilePath(WatchApplication.getWatchApplication(),
                    WatchTestConstant.DIR_CONTACTS) + File.separator + "CALL.TXT";
            return new SaveCallTask(WatchManager.getInstance(), sdCardBean, path);
        }
    }
}
