package com.jieli.watchtesttool.tool.test.filetask;

import android.os.Build;

import com.jieli.component.utils.FileUtil;
import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.WatchApplication;
import com.jieli.watchtesttool.tool.test.AbstractTestTask;
import com.jieli.watchtesttool.tool.test.ITaskFactory;
import com.jieli.watchtesttool.tool.test.ITestTask;
import com.jieli.watchtesttool.tool.test.OnTestLogCallback;
import com.jieli.watchtesttool.tool.test.TestError;
import com.jieli.watchtesttool.tool.test.contacts.SaveCallTask;
import com.jieli.watchtesttool.tool.test.model.Contact;
import com.jieli.watchtesttool.tool.test.util.ContactUtil;
import com.jieli.watchtesttool.tool.watch.WatchManager;
import com.jieli.watchtesttool.util.AppUtil;
import com.jieli.watchtesttool.util.WLog;
import com.jieli.watchtesttool.util.WatchTestConstant;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/25/21
 * @desc :
 */
public class ContactTestTask extends AbstractTestTask {
    private final SaveCallTask saveCallTask;
    private final ReadFileByNameTask getCallTask;
    private final String tag = "ContactTestTask";

    private static byte[] lastData = null; //保存上一次传输的联系人数据，

    public ContactTestTask(WatchManager watchManager, SDCardBean sdCardBean, String path) throws Exception {
        this.saveCallTask = (SaveCallTask) new SaveCallTask.Factory(sdCardBean).create();
        this.getCallTask = (ReadFileByNameTask) new ReadFileByNameTask.Factory(sdCardBean, "CALL.TXT",false).create();
        this.getCallTask.setINextTask(error -> {
            if (error.code == 0) {
                if (checkLastResult(path)) {
                    changeAndSaveContact(path);
                } else {
                    next(new TestError(-1, "联系人比对错误"));
                }
            }else {
                next(new TestError(error.code, error.msg));
            }

        });
        this.saveCallTask.setINextTask(this);
    }

    @Override
    public void next(TestError error) {
        if (error.code != 0) {
            lastData = null;
        }
        super.next(error);
    }


    @Override
    public void startTest() {
        if (lastData == null) {
            WLog.e(tag, "lastData is null");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ContactUtil.byteToContacts(lastData).forEach(contact -> {
                WLog.e(tag, "last contact -->" + contact);
            });
        }
        getCallTask.startTest();
    }

    @Override
    public void stopTest() {
        getCallTask.stopTest();
        saveCallTask.stopTest();
    }


    @Override
    public void setOnTestLogCallback(OnTestLogCallback callback) {
        super.setOnTestLogCallback(callback);
        if (callback != null) {
            getCallTask.setOnTestLogCallback(callback);
            saveCallTask.setOnTestLogCallback(callback);
        }
    }

    private void changeAndSaveContact(String path) {
        byte[] dataStr = FileUtil.getBytes(path);
        List<Contact> contacts = ContactUtil.byteToContacts(dataStr);
        WLog.d(tag, "contacts -->" + new String(dataStr));
        StringBuilder sb = new StringBuilder();
        for (Contact c : contacts) {
            sb.append(c).append("\n");
        }
        WLog.w(tag, " read contacts -->" + sb.toString());
        if (contacts.size() < 1) {
            insert(contacts);
        } else if (contacts.size() > 9) {
            delete(contacts);
        } else {
            int random = (int) (Math.random() * contacts.size());
            if (random % 2 == 0) {
                insert(contacts);
            } else {
                delete(contacts);
            }
        }
        byte[] data = ContactUtil.contactsToBytes(contacts);
        if (data == null || data.length < 20) {
            data = new byte[20];
        }
        FileUtil.bytesToFile(data, path);
        lastData = data;//保存上次的数据
        saveCallTask.startTest();
    }


    private void insert(List<Contact> list) {
        List<Contact> contacts = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            String name = "联系人" + i;
            String number = "12345678" + String.format(Locale.getDefault(), "%03d", i);
            Contact contact = new Contact();
            contact.setName(name);
            contact.setNumber(number);
            contacts.add(contact);
        }
        int random = (int) (Math.random() * contacts.size());
        WLog.i(tag, "-----插入联系人 -->" + random);
        list.add(contacts.get(Math.min(random, contacts.size() - 1)));
    }


    private void delete(List<Contact> list) {
        if (list.size() == 0) return;
        int random = (int) (Math.random() * list.size());
        WLog.i(tag, "-----删除 -->" + random);
        list.remove(Math.min(random, list.size() - 1));
    }


    private boolean checkLastResult(String path) {
        if (lastData == null) {
            return true;
        }
        byte[] dataStr = FileUtil.getBytes(path);

        return Arrays.equals(lastData, dataStr);
    }

    @Override
    public String getName() {
        return WatchApplication.getWatchApplication().getString(R.string.func_contacts);
    }

    public static class Factory implements ITaskFactory {
        private final SDCardBean sdCardBean;

        public Factory(SDCardBean sdCardBean) {
            this.sdCardBean = sdCardBean;
        }

        @Override
        public ITestTask create() throws Exception {
            String path = AppUtil.createFilePath(WatchApplication.getWatchApplication(), WatchTestConstant.DIR_CONTACTS) + File.separator + "CALL.TXT";
            return new ContactTestTask(WatchManager.getInstance(), sdCardBean, path);
        }
    }


}
