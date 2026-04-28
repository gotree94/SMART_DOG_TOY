package com.jieli.healthaide.ui.test.log;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.tool.aiui.model.OpResult;
import com.jieli.healthaide.util.FileUtil;
import com.jieli.jl_rcsp.constant.RcspErrorCode;
import com.jieli.jl_rcsp.util.JL_Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 打印文件逻辑实现
 * @since 2024/11/6
 */
public class LogFileViewModel extends ViewModel {

    public static final int OP_DELETE_FILE = 1;
    public static final int OP_DELETE_FOLDER = 2;

    private final String tag = getClass().getSimpleName();

    public final MutableLiveData<List<File>> logFilesMLD = new MutableLiveData<>();
    public final MutableLiveData<OpResult<Boolean>> opResMLD = new MutableLiveData<>();

    public final String logFileDirPath = JL_Log.getSaveLogPath(getContext());

    public void readLogFiles() {
        File folder = new File(logFileDirPath);
        if (!folder.exists()) return;
        File[] files = folder.listFiles();
        if (null == files) return;
        List<File> list = new ArrayList<>();
        for (File file : files) {
            final String filename = file.getName();
            if (file.isFile() && file.length() >= 1024
                    && (filename.endsWith(".txt") || filename.endsWith(".TXT"))) {
                list.add(file);
            }
        }
        Collections.sort(list, (o1, o2) -> Long.compare(o2.lastModified(), o1.lastModified()));
        logFilesMLD.postValue(list);
    }

    public void deleteFile(String filePath) {
        if (FileUtil.deleteFile(new File(filePath))) {
            readLogFiles();
            return;
        }
        opResMLD.postValue(new OpResult<Boolean>()
                .setOp(OP_DELETE_FILE)
                .setCode(RcspErrorCode.ERR_IO_EXCEPTION)
                .setMessage("Failed to delete File.\n filePath : " + filePath)
                .setResult(false));
    }

    public void clearLog() {
        if (FileUtil.deleteFile(new File(logFileDirPath))) {
            logFilesMLD.postValue(new ArrayList<>());
            return;
        }
        opResMLD.postValue(new
                OpResult<Boolean>()
                .setOp(OP_DELETE_FOLDER)
                .setCode(RcspErrorCode.ERR_IO_EXCEPTION)
                .setMessage("Failed to delete folder.\n filePath : "+ logFileDirPath)
                .setResult(false));
    }

    private Context getContext() {
        return HealthApplication.getAppViewModel().getApplication();
    }
}