package com.jieli.watchtesttool.tool.test;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/23/21
 * @desc :
 */
public interface ITestTask extends INextTask{

    /**
     * 开始测试
     */
    void startTest();

    /**
     * 停止测试
     */
    void stopTest();

    /**
     * 用于任务关联
     *
     * @param nextTask 下一个任务
     */
    void setINextTask(INextTask nextTask);

    /**
     * 测试打印
     *
     * @param msg 信息
     */
    void onTestLog(String msg);

    /**
     * 打印回调
     *
     * @param callback 打印回调
     */
    void setOnTestLogCallback(OnTestLogCallback callback);


    String getName();

}
