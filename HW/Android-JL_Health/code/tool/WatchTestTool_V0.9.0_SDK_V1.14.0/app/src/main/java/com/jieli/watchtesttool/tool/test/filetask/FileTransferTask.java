package com.jieli.watchtesttool.tool.test.filetask;

import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.jl_rcsp.task.TaskListener;
import com.jieli.jl_rcsp.task.TransferTask;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.WatchApplication;
import com.jieli.watchtesttool.tool.config.ConfigHelper;
import com.jieli.watchtesttool.tool.test.AbstractTestTask;
import com.jieli.watchtesttool.tool.test.ITaskFactory;
import com.jieli.watchtesttool.tool.test.ITestTask;
import com.jieli.watchtesttool.tool.test.TestError;
import com.jieli.watchtesttool.tool.watch.WatchManager;
import com.jieli.watchtesttool.util.AppUtil;
import com.jieli.watchtesttool.util.WatchTestConstant;

import java.io.File;
import java.util.Locale;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/23/21
 * @desc : 文件传输
 */
public class FileTransferTask extends AbstractTestTask implements TaskListener {
    private final TransferTask transferTask;
    private String sizeString;
    private long currentTime = 0;
    private final SDCardBean sdCardBean;

    public FileTransferTask(WatchManager watchManager, SDCardBean sdCardBean, String path) {
        transferTask = createTask(watchManager, sdCardBean);
        transferTask.setListener(this);
        transferTask.setPath(path);
        this.sdCardBean = sdCardBean;
    }

    protected TransferTask createTask(WatchManager watchManager, SDCardBean sdCardBean) {
        TransferTask.Param param = new TransferTask.Param();
        param.isOtherEncode = ConfigHelper.getInstance().isUseOtherEncode(); //设置是否使用其他编码方式 -- true
        if (param.isOtherEncode) {
            param.encodeType = ConfigHelper.getInstance().getEncodeName();  //设置编码方式 - Unicode编码方式(StandardCharsets.UTF_16LE.displayName();)
        }
        param.devHandler = sdCardBean.getDevHandler();
        param.useFlash = sdCardBean.getType() == SDCardBean.FLASH;
        return new TransferTask(watchManager, "", param);
    }

    @Override
    public void startTest() {
        //NOTE: 如果Flash支持传输长文件名文件，注释下面的判断条件
        //如果是flash设备，则不能传输长文件名文件
        /*if (sdCardBean.getType() == SDCardBean.FLASH) {
            String path = transferTask.getPath();
            File file = new File(path);
            String name = file.getName();
            try {
                byte[] nameData = file.getName().getBytes("gbk");
                if (nameData.length > 12) {
                    new Handler(Looper.getMainLooper())
                            .postDelayed(() -> next(new TestError(-1, "flash设备不能传输长文件名文件.\nname = " + name)), 1000);
                    return;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }*/
        transferTask.start();
    }

    @Override
    public void stopTest() {
        if (transferTask != null) {
            transferTask.cancel((byte) 0x00);
        }
    }

    @Override
    public void onBegin() {
        float size = new File(transferTask.getPath()).length();
        size = size / 1024.0f / 1024.0f;
        currentTime = System.currentTimeMillis();
        sizeString = String.format(Locale.getDefault(), "%.2fM", size);
        onTestLog("----文件传输开始----路径: " + transferTask.getPath());
//        String name = "a{|}b<c()\"\"df~jk.#$+/efg.1::23hijhlm-^]op\\[qr?@s>=<;:w.-。，rr.txt";

    }

    @Override
    public void onProgress(int i) {
        int space = (int) (System.currentTimeMillis() - currentTime) / 1000;
        onTestLog("----传输进度----" + i + "\nsize = " + sizeString + "\t\ttime = " + space + "s"
                + "\nname = " + new File(transferTask.getPath()).getName());
    }

    @Override
    public void onFinish() {
        int space = (int) (System.currentTimeMillis() - currentTime) / 1000;
        next(new TestError(0, "文件传输结束: size = " + sizeString + "\ttime = " + space + "s"));

    }

    @Override
    public void onError(int i, String s) {
        String msg = String.format(Locale.getDefault(), ":文件传输异常:%d, 描述:%s", i, s);
        next(new TestError(i, msg));
    }

    @Override
    public void onCancel(int reason) {
        next(new TestError(-1, "取消文件传输"));
    }


    public static class RandomMusicFactory implements ITaskFactory {
        private final SDCardBean sdCardBean;

        public RandomMusicFactory(SDCardBean sdCardBean) {
            this.sdCardBean = sdCardBean;
        }

        @Override
        public ITestTask create() throws Exception {
            String path = AppUtil.createFilePath(WatchApplication.getWatchApplication(), WatchTestConstant.DIR_MUSIC);
            File file = new File(path);
            if(!file.exists()) throw new RuntimeException("Not found folder. file path : " + path);
            File[] files = file.listFiles();
            if (null == files) throw new RuntimeException("Not found music. file path : " + path);
            int index = (int) (Math.random() * files.length);
//            index =0;
            return new FileTransferTask(WatchManager.getInstance(), sdCardBean, files[index].getPath());
        }
    }

    @Override
    public String getName() {
        return WatchApplication.getWatchApplication().getString(R.string.func_music_transfer);
    }

    public static class Factory implements ITaskFactory {
        private final String path;
        private final SDCardBean sdCardBean;

        public Factory(SDCardBean sdCardBean, String path) {
            this.path = path;
            this.sdCardBean = sdCardBean;
        }

        @Override
        public ITestTask create() {
            return new FileTransferTask(WatchManager.getInstance(), sdCardBean, path);
        }
    }
}
