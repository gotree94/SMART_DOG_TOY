package com.jieli.healthaide.demos;

import android.content.Context;

import com.jieli.healthaide.tool.aiui.AIManager;
import com.jieli.healthaide.tool.watch.WatchManager;

import org.junit.Test;

/**
 * @ClassName: AIDialDemo
 * @Description: AI表盘功能示例
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/12/28 11:39
 */
public class AIDialDemo {
    private AIManager mAIManager;
    /**
     * 初始化AIManager
     */
    @Test
    public void init(Context context){
        //Step1. 初始化
        AIManager.init(context, WatchManager.getInstance());
        mAIManager = AIManager.getInstance();
    }

    /**
     * 设置AI表盘风格
     */
    @Test
    public void setStyle(String style){
        mAIManager.getAIDial().setCurrentAIDialStyle(style);
    }
}
