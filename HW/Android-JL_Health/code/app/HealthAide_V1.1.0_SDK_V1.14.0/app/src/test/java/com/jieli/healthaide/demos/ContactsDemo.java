package com.jieli.healthaide.demos;

import android.content.Context;

import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.task.SimpleTaskListener;
import com.jieli.jl_rcsp.task.contacts.DeviceContacts;
import com.jieli.jl_rcsp.task.contacts.ReadContactsTask;
import com.jieli.jl_rcsp.task.contacts.UpdateContactsTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/12/1
 * @desc : 联系人管理demo
 */
public class ContactsDemo {


    void readContacts(Context context) {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        String output = HealthApplication.getAppViewModel().getApplication().getExternalCacheDir() + File.separator + "read_call.txt";
        ReadContactsTask task = new ReadContactsTask(watchManager, output);
        task.setListener(new SimpleTaskListener() {
            @Override
            public void onBegin() {
                //开始传输
            }

            @Override
            public void onError(int code, String msg) {
                //异常
            }

            @Override
            public void onFinish() {
                //完成
                List<DeviceContacts> contacts = task.getContacts();
            }
        });
    }

    void updateContacts(Context context) {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        List<DeviceContacts> contacts = new ArrayList<>();
//        List<DeviceContacts> contacts = "修改后的联系人数据";
        UpdateContactsTask task = new UpdateContactsTask(watchManager, context, contacts);
        task.setListener(new SimpleTaskListener() {
            @Override
            public void onBegin() {
                //开始传输
            }

            @Override
            public void onError(int code, String msg) {
                //异常
            }

            @Override
            public void onFinish() {
                //完成
            }
        });
    }


}
