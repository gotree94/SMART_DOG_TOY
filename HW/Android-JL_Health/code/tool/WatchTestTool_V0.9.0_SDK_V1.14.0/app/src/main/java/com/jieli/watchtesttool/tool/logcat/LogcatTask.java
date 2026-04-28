package com.jieli.watchtesttool.tool.logcat;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 6/4/21
 * @desc :
 */
public class LogcatTask extends Thread {
    private final String tag = getClass().getSimpleName();
    private final Context context;

    public LogcatTask(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        int pid = android.os.Process.myPid();
        String parentPath = context.getExternalCacheDir() + File.separator + "log";
        File file = new File(parentPath);
        if (!file.exists()) {
            file.mkdir();
        }
        String path = parentPath + File.separator + "app_watch_test.log";
        String cmd = new LogcatBuilder()
                .pid(pid)
                .fileSize(1024 * 50)//50M
                .count(5)//10个文件循环
                .outPath(path)
                .toString();
        Log.e(tag, "logcat adb cmd: " + cmd);
        try {
            processCmd(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processCmd(String cmd) throws IOException {
        Process process = Runtime.getRuntime().exec(cmd);
        InputStream is = process.getInputStream();
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader bis = new BufferedReader(reader);
        String line = "";
        while ((line = bis.readLine()) != null) {
            Log.e(tag, "logcat adb cmd -->: " + line);
        }

    }
}
