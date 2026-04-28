package com.jieli.watchtesttool.tool.test;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/29/21
 * @desc :
 */
public interface OnTaskChangeCallback extends  OnTestLogCallback{

    void onTaskChange(ITestTask task,int index);


}
