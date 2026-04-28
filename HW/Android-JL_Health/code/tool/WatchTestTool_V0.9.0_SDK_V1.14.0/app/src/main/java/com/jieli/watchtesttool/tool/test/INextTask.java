package com.jieli.watchtesttool.tool.test;


/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/23/21
 * @desc :任务间通过INextTask接口可以组成一个任务链或者任务队列;
 */
public interface INextTask {

    /**
     *
     * @param error 上一个任务的执行结果
     */
    void next(TestError error);

 }
