package com.jieli.healthaide.ui.device.contact;

import androidx.lifecycle.MutableLiveData;

import com.jieli.component.utils.FileUtil;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.ui.device.watch.WatchViewModel;
import com.jieli.jl_rcsp.constant.WatchConstant;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.task.TaskListener;
import com.jieli.jl_rcsp.task.contacts.DeviceContacts;
import com.jieli.jl_rcsp.task.contacts.ReadContactsTask;
import com.jieli.jl_rcsp.task.contacts.UpdateContactsTask;
import com.jieli.jl_rcsp.util.JL_Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/16/21 11:42 AM
 * @desc :
 */
public class ContactViewModel extends WatchViewModel {
    private String tag = "ContactViewModel";
    public static final int STATE_READ_CONTACT = 0x00;
    public static final int STATE_READ_CONTACT_FINISH = 0x01;
    public static final int STATE_READ_CONTACT_ERROR = 0x02;
    public static final int STATE_READ_CONTACT_CANCEL = 0x03;
    public static final int START_ERROR_IN_CALLING = 0xF1;

    public static final int STATE_UPDATE_CONTACT = 0x80;
    public static final int STATE_UPDATE_CONTACT_FINISH = 0x81;
    public static final int STATE_UPDATE_CONTACT_ERROR = 0x82;
    public static final int STATE_UPDATE_CONTACT_CANCEL = 0x83;


    MutableLiveData<List<Contact>> contactLiveData = new MutableLiveData<>();
    MutableLiveData<Integer> stateLiveData = new MutableLiveData<>();

    public void readDeviceContacts() {
        if (isCallWorking()) {
            stateLiveData.postValue(START_ERROR_IN_CALLING);
            return;
        }
        String output = HealthApplication.getAppViewModel().getApplication().getExternalCacheDir() + File.separator + "read_call.txt";
        ReadContactsTask task = new ReadContactsTask(mWatchManager, output);
        stateLiveData.postValue(STATE_READ_CONTACT);

        task.setListener(new TaskListener() {
            @Override
            public void onBegin() {


            }

            @Override
            public void onFinish() {
                stateLiveData.postValue(STATE_READ_CONTACT_FINISH);
                List<DeviceContacts> data = task.getContacts();
                List<Contact> contacts = new ArrayList<>();
                for (DeviceContacts deviceContacts : data) {
                    Contact contact = new Contact();
                    contact.setName(deviceContacts.getName());
                    contact.setNumber(deviceContacts.getMobile());
                    contact.setFileId(deviceContacts.getFileId());
                    contacts.add(contact);
                }
                contactLiveData.postValue(contacts);
            }

            @Override
            public void onError(int code, String msg) {
                JL_Log.e(tag, "readDeviceContacts", "read contact failed code " + code + "\tmsg = " + msg);
                stateLiveData.postValue(STATE_READ_CONTACT_ERROR);

            }

            @Override
            public void onCancel(int reason) {
                JL_Log.w(tag, "readDeviceContacts", "read contact  cancel reason " + reason);
                stateLiveData.postValue(STATE_READ_CONTACT_CANCEL);

            }

            @Override
            public void onProgress(int progress) {

            }
        });

        task.start();
    }


    public void updateContact(List<Contact> contacts) {
        if (isCallWorking()) {
            stateLiveData.postValue(START_ERROR_IN_CALLING);
            return;
        }
        for (Contact contact : contacts) {
            contact.setName(new String(DeviceContacts.getTextData(contact.getName())));//限制联系人名字长度小于20Bytes
        }
        stateLiveData.postValue(STATE_UPDATE_CONTACT);
        byte[] data = ContactUtil.contactsToBytes(contacts);
        String path = HealthApplication.getAppViewModel().getApplication().getExternalCacheDir() + File.separator + "CALL.TXT";
        FileUtil.bytesToFile(data, path);
        List<DeviceContacts> list = new ArrayList<>();
        for (Contact contact : contacts) {
            list.add(new DeviceContacts((short) contact.getFileId(), contact.getName(), contact.getNumber()));
        }
        UpdateContactsTask task = new UpdateContactsTask(mWatchManager, HealthApplication.getAppViewModel().getApplication(), list);

        task.setListener(new TaskListener() {
            @Override
            public void onBegin() {

            }

            @Override
            public void onProgress(int progress) {
            }

            @Override
            public void onFinish() {
                JL_Log.d(tag, "updateContact", "update contact finish   ");
                stateLiveData.postValue(STATE_UPDATE_CONTACT_FINISH);
                contactLiveData.postValue(contacts);
            }

            @Override
            public void onError(int code, String msg) {
                JL_Log.e(tag, "updateContact", "update contact failed code " + code + "\tmsg = " + msg);
                stateLiveData.postValue(STATE_UPDATE_CONTACT_ERROR);
            }

            @Override
            public void onCancel(int reason) {
                JL_Log.w(tag, "updateContact", "update contact cancel   ");
                stateLiveData.postValue(STATE_UPDATE_CONTACT_CANCEL);
            }
        });
        task.start();

    }

    private boolean isCallWorking() {
        DeviceInfo deviceInfo = getDeviceInfo(getConnectedDevice());
        return deviceInfo != null && deviceInfo.getPhoneStatus() == WatchConstant.DEVICE_PHONE_STATUS_CALLING;
    }
}