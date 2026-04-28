package com.jieli.watchtesttool.tool.test.fattask;

import android.text.TextUtils;

import com.jieli.jl_fatfs.FatFsErrCode;
import com.jieli.jl_fatfs.interfaces.OnFatFileProgressListener;
import com.jieli.jl_fatfs.model.FatFile;
import com.jieli.jl_fatfs.utils.FatUtil;
import com.jieli.jl_rcsp.constant.JLChipFlag;
import com.jieli.jl_rcsp.constant.WatchError;
import com.jieli.jl_rcsp.impl.WatchOpImpl;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.model.response.ExternalFlashMsgResponse;
import com.jieli.jl_rcsp.util.RcspUtil;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.WatchApplication;
import com.jieli.watchtesttool.tool.test.AbstractTestTask;
import com.jieli.watchtesttool.tool.test.ITaskFactory;
import com.jieli.watchtesttool.tool.test.ITestTask;
import com.jieli.watchtesttool.tool.test.TestError;
import com.jieli.watchtesttool.tool.watch.WatchManager;
import com.jieli.watchtesttool.util.AppUtil;
import com.jieli.watchtesttool.util.WLog;
import com.jieli.watchtesttool.util.WatchTestConstant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 表盘操作测试
 * @since 2021/4/23
 */
public class FatFileTestTask extends AbstractTestTask {

    private final WatchOpImpl mWatchOp;
    private final String testResDir;
    private final boolean isNoNeedCheck; //true : 自定义背景  false :  表盘

    private boolean isStopTest;
    private boolean tryToInsert;

    public FatFileTestTask(WatchOpImpl mWatchOp, String testResDir, boolean isNoNeedCheck) {
        this.mWatchOp = mWatchOp;
        this.testResDir = testResDir;
        this.isNoNeedCheck = isNoNeedCheck;
    }

    @Override
    public void startTest() {
        File file = getRandomFile(testResDir);
        if (file == null) {
            next(new TestError(FatFsErrCode.RES_NO_PATH, String.format(Locale.getDefault(),
                    "----开始测试失败----\n原因：%s,\t文件路径：%s",
                    FatUtil.getFatFsErrorCodeMsg(FatFsErrCode.RES_NO_PATH), testResDir)));
            return;
        }
        isStopTest = false;
        WLog.d("fat_test", "startTest : " + file);
        insertFile(file.getPath());
    }


    @Override
    public void stopTest() {
        isStopTest = true;
    }

    @Override
    public void onTestLog(String msg) {
        WLog.d("fat_test", "thread : " + Thread.currentThread().getName() + " onTestLog : " + msg);
        super.onTestLog(msg);
    }

    @Override
    public void next(TestError error) {
        if (isStopTest) isStopTest = false;
        super.next(error);

    }


    /**
     * 插入文件
     *
     * @param filePath 文件路径
     */
    private void insertFile(final String filePath) {
        if (stopTestCallback()) return;
        mWatchOp.createWatchFile(filePath, isNoNeedCheck, new OnFatFileProgressListener() {
            @Override
            public void onStart(String s) {
                String msg = String.format(Locale.getDefault(), "----插入%s开始----\n路径: %s", isNoNeedCheck ? "自定义背景" : "表盘", FatUtil.getFatFilePath(s));
                onTestLog(msg);
            }

            @Override
            public void onProgress(float v) {
                int progress = Math.round(v);
                if (progress > 100) {
                    progress = 99;
                }
                String msg = String.format(Locale.getDefault(), "----插入%s进度---- %d", isNoNeedCheck ? "自定义背景" : "表盘", progress);
                onTestLog(msg);
            }

            @Override
            public void onStop(int i) {
                String msg = String.format(Locale.getDefault(), "----插入%s结束----\n结果：%s", isNoNeedCheck ? "自定义背景" : "表盘",
                        RcspUtil.formatInt(i));

                if (i == FatFsErrCode.RES_OK) {
                    String fatFilePath = FatUtil.getFatFilePath(filePath);
                    msg += "成功，路径：" + fatFilePath;
                    onTestLog(msg);
                    if (tryToInsert) {
                        tryToInsert = false;
                    }
                    enableFatFile(fatFilePath);
                } else if (i == FatFsErrCode.RES_TOO_BIG || i == WatchError.ERR_SPACE_TO_UPDATE
                        || i == WatchError.ERR_FAT_TOO_BIG) {
                    msg += "失败， 空间不足";
                    onTestLog(msg);
                    listAndDelFatFiles(filePath);
                } else {
                    msg += "失败， " + i + " " + FatUtil.getFatFsErrorCodeMsg(i);
                    next(new TestError(i, msg));
                }
            }
        });
    }


    /**
     * 随机删除文件
     */
    public void listAndDelFatFiles(final String path) {
        if (stopTestCallback()) return;
        mWatchOp.listWatchList(new OnWatchOpCallback<ArrayList<FatFile>>() {
            @Override
            public void onSuccess(ArrayList<FatFile> fatFiles) {
                if (fatFiles == null) {
                    String msg = "----查询手表文件列表----\n结果：失败，数据异常";
                    next(new TestError(FatFsErrCode.RES_RCSP_SEND, msg));
                    return;
                }
                if (fatFiles.size() <= 1) {
                    next(new TestError(FatFsErrCode.RES_TOO_BIG, "失败， 空间不足"));
                    return;
                }
                if (stopTestCallback()) return;
                //delete
                for (FatFile fatFile : fatFiles) {
                    WLog.v("fat_test", "fatFile = " + fatFile);
                }
                int random = getRandom(fatFiles.size());
                FatFile fatFile = fatFiles.get(random);
                /*if (fatFile.getName().equalsIgnoreCase("SIDEBAR")
                        || fatFile.getName().equalsIgnoreCase("font")
                        || fatFile.getName().equalsIgnoreCase("jl")) {

                    WLog.w("fat_test", "delete fat file error , path = " + fatFile.getPath());
                    if (random == fatFiles.size() - 1) {
                        next(new TestError(FatFsErrCode.RES_ERR_EMPTY_FILE, "异常情况"));
                        return;
                    }
                    if (fatFiles.size() > 1) {
                        onSuccess(fatFiles);
                    }
                    return;
                }*/
                WLog.i("fat_test", "delete fat file, path = " + fatFile.getPath());
                mWatchOp.deleteWatchFile(fatFile.getPath(), new OnFatFileProgressListener() {
                    @Override
                    public void onStart(String s) {
                        String msg = String.format(Locale.getDefault(), "----删除%s开始----\n路径: %s", isNoNeedCheck ? "自定义背景" : "表盘", s);
                        onTestLog(msg);
                    }

                    @Override
                    public void onProgress(float v) {
                        int progress = Math.round(v);
                        if (progress > 100) {
                            progress = 99;
                        }
                        String msg = String.format(Locale.getDefault(), "----删除%s进度----%d", isNoNeedCheck ? "自定义背景" : "表盘", progress);
                        onTestLog(msg);
                    }

                    @Override
                    public void onStop(int i) {
                        if (i == FatFsErrCode.RES_OK) {
                            //删除成功，继续插入操作
                            insertFile(path);
                            return;
                        }
                        String msg = String.format(Locale.getDefault(), "----删除%s结束----\n结果: 失败，%s", isNoNeedCheck ? "自定义背景" : "表盘", FatUtil.getFatFsErrorCodeMsg(i));
                        next(new TestError(i, msg));
                    }
                });
            }

            @Override
            public void onFailed(BaseError baseError) {
                String msg = "----查询手表文件列表----\n结果：失败，发送异常， " + baseError.getMessage();
                next(new TestError(baseError.getSubCode(), msg));
            }
        });

    }

    /**
     * 使能插入文件
     *
     * @param fatFilePath fat文件路径
     */
    public void enableFatFile(final String fatFilePath) {
//        if (stopTestCallback()) return;
        if (!isNoNeedCheck) {
            mWatchOp.setCurrentWatchInfo(fatFilePath, new OnWatchOpCallback<FatFile>() {
                @Override
                public void onSuccess(FatFile fatFile) {
                    String msg = String.format(Locale.getDefault(), "----设置当前表盘----\n结果: 成功，路径：%s", (null == fatFile ? fatFilePath : fatFile.getPath()));
                    next(new TestError(FatFsErrCode.RES_OK, msg));
                }

                @Override
                public void onFailed(BaseError baseError) {
                    String msg = String.format(Locale.getDefault(), "----设置当前表盘----\n结果: 失败，发送异常，%s", baseError.getMessage());
                    next(new TestError(baseError.getSubCode(), msg));
                }
            });
        } else {
            mWatchOp.enableCustomWatchBg(fatFilePath, new OnWatchOpCallback<FatFile>() {
                @Override
                public void onSuccess(FatFile fatFile) {
                    String msg = String.format(Locale.getDefault(), "----激活自定义表盘----\n结果: 成功，路径：%s", fatFile.getPath());
                    next(new TestError(FatFsErrCode.RES_OK, msg));
                }

                @Override
                public void onFailed(BaseError baseError) {
                    String msg = String.format(Locale.getDefault(), "----激活自定义表盘----\n结果: 失败，发送异常，%s", baseError.getMessage());
                    next(new TestError(baseError.getSubCode(), msg));
                }
            });
        }
    }

    private int getRandom(int bound) {
        return new Random().nextInt(bound) % bound;
    }

    private File getRandomFile(String dirPath) {
        File resDir = new File(dirPath);
        File[] files = resDir.listFiles();
        List<File> watchList = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (!isNoNeedCheck && file.getName().toLowerCase().startsWith("watch")) {
                    watchList.add(file);
                } else if (isNoNeedCheck && file.getName().toLowerCase().startsWith("bgp_")) {
                    watchList.add(file);
                }
            }
        }
        if (watchList.isEmpty()) {
            return null;
        }
        int random = getRandom(watchList.size());
        return watchList.get(random);
    }

    private boolean stopTestCallback() {
        if (isStopTest) {
            next(new TestError(FatFsErrCode.RES_EXIST, "----用户停止测试----"));
            return true;
        }
        return false;
    }


    @Override
    public String getName() {
        return isNoNeedCheck ? WatchApplication.getWatchApplication().getString(R.string.func_insert_watch_bg)
                : WatchApplication.getWatchApplication().getString(R.string.func_insert_watch);
    }

    public static class Factory implements ITaskFactory {
        private final boolean isNoNeedCheck;

        public Factory(boolean isNoNeedCheck) {
            this.isNoNeedCheck = isNoNeedCheck;
        }

        @Override
        public ITestTask create() throws Exception {
            WatchManager watchManager = WatchManager.getInstance();
            DeviceInfo deviceInfo = watchManager.getDeviceInfo();
            if (null == deviceInfo) throw new RuntimeException("Device is not connected.");
            String subDir;
            switch (deviceInfo.getSdkType()) {
                case JLChipFlag.JL_CHIP_FLAG_701X_WATCH:
                    subDir = WatchTestConstant.DIR_BR28;
                    break;
                case JLChipFlag.JL_CHIP_FLAG_707N_WATCH:
                    subDir = WatchTestConstant.DIR_BR35;
                    break;
                default:
                    subDir = WatchTestConstant.DIR_BR23;
                    break;
            }
            String versionDir = null;
            if (!isNoNeedCheck && WatchTestConstant.DIR_BR28.equals(subDir)) {
                versionDir = WatchTestConstant.VERSION_W001;
                ExternalFlashMsgResponse flashMsg = watchManager.getExternalFlashMsg(watchManager.getConnectedDevice());
                if (null != flashMsg) {
                    String[] matchVersions = flashMsg.getMatchVersions();
                    if (matchVersions != null) {
                        for (String matchVersion : matchVersions) {
                            if (WatchTestConstant.VERSION_W002.equalsIgnoreCase(matchVersion)) {
                                versionDir = WatchTestConstant.VERSION_W002;
                                break;
                            }
                        }
                    }
                }
            }
            String path;
            if (TextUtils.isEmpty(versionDir)) {
                path = AppUtil.createFilePath(WatchApplication.getWatchApplication(), isNoNeedCheck ? WatchTestConstant.DIR_WATCH_BG : WatchTestConstant.DIR_WATCH, subDir);
            } else {
                path = AppUtil.createFilePath(WatchApplication.getWatchApplication(), isNoNeedCheck ? WatchTestConstant.DIR_WATCH_BG : WatchTestConstant.DIR_WATCH, subDir, versionDir);
            }
            return new FatFileTestTask(watchManager, path, isNoNeedCheck);
        }
    }


}
