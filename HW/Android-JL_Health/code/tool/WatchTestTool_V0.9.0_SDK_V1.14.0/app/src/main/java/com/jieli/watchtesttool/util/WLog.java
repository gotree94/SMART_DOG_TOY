package com.jieli.watchtesttool.util;


import android.content.Context;

import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 统一打印类
 * @since 2021/4/25
 */
public class WLog extends JL_Log {

    public static void configureLog(Context context, boolean log, boolean isSaveFile) {
        JL_Log.setTagPrefix("watch_test");
        JL_Log.configureLog(context, log, isSaveFile);
        com.jieli.bluetooth_connect.util.JL_Log.setLog(log);
        com.jieli.jl_bt_ota.util.JL_Log.setLog(log);
        if (isSaveFile) {
            com.jieli.bluetooth_connect.util.JL_Log.setLogOutput(JL_Log::addLogOutput);
            com.jieli.jl_bt_ota.util.JL_Log.setLogOutput(JL_Log::addLogOutput);
        } else {
            com.jieli.bluetooth_connect.util.JL_Log.setLogOutput(null);
            com.jieli.jl_bt_ota.util.JL_Log.setLogOutput(null);
        }
    }
}
