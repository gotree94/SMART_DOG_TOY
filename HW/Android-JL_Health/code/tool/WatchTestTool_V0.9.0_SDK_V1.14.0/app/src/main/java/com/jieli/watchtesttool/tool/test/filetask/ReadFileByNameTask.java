package com.jieli.watchtesttool.tool.test.filetask;

import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.jl_rcsp.task.GetFileByNameTask;
import com.jieli.jl_rcsp.task.GetFileTask;
import com.jieli.jl_rcsp.task.TaskListener;
import com.jieli.watchtesttool.tool.test.AbstractTestTask;
import com.jieli.watchtesttool.tool.test.ITaskFactory;
import com.jieli.watchtesttool.tool.test.ITestTask;
import com.jieli.watchtesttool.tool.test.TestError;
import com.jieli.watchtesttool.tool.watch.WatchManager;

import java.io.File;
import java.util.Locale;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/23/21
 * @desc :
 */
public class ReadFileByNameTask extends AbstractTestTask implements TaskListener {
    private final GetFileTask getFileTask;

    private final String name;
    private final boolean unicode = false;

    public ReadFileByNameTask(WatchManager watchManager, SDCardBean sdCardBean, String outPath, String name, boolean unicode) {
        GetFileByNameTask.Param param = new GetFileByNameTask.Param(sdCardBean.getDevHandler(), name, outPath, unicode);
        getFileTask = new GetFileByNameTask(watchManager, param);
        getFileTask.setListener(this);
        this.name = name;
    }

    @Override
    public void stopTest() {
        if (getFileTask != null) {
            getFileTask.cancel((byte) 1);
        }
    }

    @Override
    public void startTest() {
        getFileTask.start();
    }

    @Override
    public void onBegin() {
        onTestLog("----获取" + name + "开始----");
    }

    @Override
    public void onFinish() {
        String msg = "获取" + name + "成功 ";
        next(new TestError(0, msg));
    }

    @Override
    public void onError(int i, String s) {
        String msg = "获取文件异常:" + s;
        next(new TestError(i, msg));
    }

    @Override
    public void onCancel(int i) {
        next(new TestError(-1, "取消获取文件" + name));
    }

    @Override
    public void onProgress(int i) {
        onTestLog(String.format(Locale.getDefault(), "获取文件进度progress %d \nfilename = %s", i, name));

    }


    @Override
    public String getName() {
        return "读取固件文件(通过名字)";
    }

    public static class Factory implements ITaskFactory {

        private final String name;
        private final SDCardBean sdCardBean;
        private boolean unicode;

        public Factory(SDCardBean sdCardBean, String name, boolean unicode) {
            this.name = name;
            this.sdCardBean = sdCardBean;
            this.unicode = unicode;
        }

        @Override
        public ITestTask create() throws Exception {
            File dir = new File(ReadFileByClusterTask.READ_FILE_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
//            String date = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
            String path = dir.getPath() + File.separator + name;
            return new ReadFileByNameTask(WatchManager.getInstance(), sdCardBean, path, name, unicode);
        }
    }
}
