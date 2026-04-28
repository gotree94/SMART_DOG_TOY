package com.jieli.healthaide.ui.device.contact;

import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;

import com.jieli.component.thread.ThreadManager;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.ui.device.watch.WatchViewModel;
import com.jieli.jl_rcsp.task.contacts.DeviceContacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/16/21 11:42 AM
 * @desc :
 */
public class ContactChoseViewModel extends WatchViewModel {

    MutableLiveData<List<IndexContactData>> contactLiveData = new MutableLiveData<>();
    private final List<Contact> contacts = new ArrayList<>();
    private final HealthApplication mApplication;

    public ContactChoseViewModel() {
        super();
        mApplication = HealthApplication.getAppViewModel().getApplication();
    }


    public void loadContacts(List<Contact> filters) {
        ThreadManager.getInstance().postRunnable(() -> {
            contacts.clear();
            List<Contact> temp = ContactUtil.queryContacts(mApplication, null, null);
            temp = distinct(temp);//去重
            if (filters != null && !filters.isEmpty()) {
                //过滤
                for (Contact contact : temp) {
                    boolean has = false;
                    for (Contact f : filters) {
                        String name = new String(DeviceContacts.getTextData(contact.getName()));
                        if (name.equals(f.getName()) && contact.getNumber().equals(f.getNumber())) {
                            has = true;
                            break;
                        }
                    }
                    if (!has) {
                        contacts.add(contact);
                    }
                }
            } else {
                contacts.clear();
                contacts.addAll(temp);
            }

            Collections.sort(contacts, (o1, o2) -> {
                byte[] bytes1 = o1.getPinyinName().getBytes();
                byte[] bytes2 = o2.getPinyinName().getBytes();
                if (bytes1.length < 1 || bytes2.length < 1) {
                    return (bytes1.length < 1) ? -1 : 1;
                } else {
                    return Byte.compare(bytes1[0], bytes2[0]);
                }

            });
            searchByText("");
        });
    }


    public void searchByText(String text) {
        List<Contact> tmp = new ArrayList<>();
        try {
            for (Contact contact : contacts) {
                if (TextUtils.isEmpty(text)
                        || contact.getPinyinName().startsWith(text.toLowerCase())
                        || contact.getName().startsWith(text.toLowerCase())
                        || contact.getLetterName().startsWith(text.toLowerCase())
                        || contact.getNumber().startsWith(text)
                ) {
                    tmp.add((Contact) contact.clone());
                }
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }


        List<IndexContactData> list = convert(tmp);
        contactLiveData.postValue(list);
    }


    private List<IndexContactData> getNotAlphaContact(List<Contact> contacts) {
        List<IndexContactData> data = new ArrayList<>();
        for (Contact contact : contacts) {

            char start = 0;
            if (!TextUtils.isEmpty(contact.getPinyinName())) {
                start = contact.getPinyinName().toUpperCase().charAt(0);
            }
            if (start < 65 || start > 90) {
                IndexContactData contactData = new IndexContactData();
                contactData.type = 1;
                contactData.contact = contact;
                data.add(contactData);
            }
        }

        if (!data.isEmpty()) {
            IndexContactData indexContactData = new IndexContactData();
            indexContactData.index = "#";
            data.add(0, indexContactData);
        }
        return data;
    }


    private List<IndexContactData> convert(List<Contact> contacts) {
        List<IndexContactData> data = getNotAlphaContact(contacts);
        int j = 0;
        for (int i = 0; i < 26; i++) {
            char index = (char) (65 + i);
            int count = 0;
            IndexContactData indexContactData = new IndexContactData();
            indexContactData.index = String.valueOf(index);
            data.add(indexContactData);
            for (; j < contacts.size(); j++) {
                char start = 0;
                if (!TextUtils.isEmpty(contacts.get(j).getPinyinName())) {
                    start = contacts.get(j).getPinyinName().toUpperCase().charAt(0);
                }
                if (start < 65 || start > 90) continue;
                if (index != start) {
                    break;
                }
                IndexContactData contactData = new IndexContactData();
                contactData.type = 1;
                contactData.contact = contacts.get(j);
                data.add(contactData);
                count++;
            }
            if (count == 0) {
                data.remove(indexContactData);
            }
        }
        return data;

    }

    private static List<Contact> distinct(List<Contact> list) {
        if (list == null || list.isEmpty()) return list;
        List<Contact> result = new ArrayList<>();
        int i = 0;
        int size = list.size();
        for (; i < size; i++) {
            Contact contact = list.get(i);
            boolean conflict = false;
            for (int j = 0; j < result.size(); j++) {
                Contact compareContact = result.get(j);
                if (contact.getName().equals(compareContact.getName()) && contact.getNumber().equals(compareContact.getNumber())) {
                    conflict = true;
                    break;
                }
            }
            if (!conflict) {
                result.add(contact);
            }
        }
        return result;
    }
}

