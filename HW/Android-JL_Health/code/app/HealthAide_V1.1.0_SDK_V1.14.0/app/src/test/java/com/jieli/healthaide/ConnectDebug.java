package com.jieli.healthaide;

import android.content.Context;

import com.jieli.bluetooth_connect.util.JL_Log;

import org.junit.Test;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 连接调试
 * @since 2022/3/30
 */
public class ConnectDebug {

    @Test
    public void debugConfig(Context context) {
        //是否开启Log, 建议是开发时打开，发布时关闭
        JL_Log.setLog(BuildConfig.DEBUG);
        //是否保存Log文件, 建议是开发时打开，发布时关闭
        JL_Log.setIsSaveLogFile(BuildConfig.DEBUG, context);
    }

    @Test
    public void addLogMsg() {
        //开启杰理OTA库打印
        com.jieli.jl_bt_ota.util.JL_Log.setLog(BuildConfig.DEBUG);
        if (BuildConfig.DEBUG) { //如果开启，把杰理OTA库的打印输出到杰理连接库的输出文件中
            com.jieli.jl_bt_ota.util.JL_Log.setLogOutput(new com.jieli.jl_bt_ota.util.JL_Log.ILogOutput() {
                @Override
                public void output(String s) {
                    //此处回调杰理OTA库的打印信息
                    JL_Log.addLogOutput(s); //添加到杰理连接库的输出文件中
                }
            });
        }
    }
}
