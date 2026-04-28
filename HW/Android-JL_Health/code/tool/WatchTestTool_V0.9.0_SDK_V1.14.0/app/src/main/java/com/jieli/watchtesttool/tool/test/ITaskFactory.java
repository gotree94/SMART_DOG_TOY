package com.jieli.watchtesttool.tool.test;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/23/21
 * @desc : 任务工厂
 */
public interface ITaskFactory {
    ITestTask create() throws Exception;
}
