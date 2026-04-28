package com.jieli.watchtesttool.tool.test.util;

import com.jieli.watchtesttool.tool.test.model.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/16/21 5:26 PM
 * @desc :
 */
public class ContactUtil {


    /**
     * 获取通讯录编码后数据
     *
     * @param list 通讯列表
     * @return 编码后数据
     */
    public static byte[] contactsToBytes(List<Contact> list) {
        if (list == null || list.isEmpty()) return new byte[20];
        int dataSize = 20;//每个字段的大小，包含一个空格
        byte[] data = new byte[dataSize * 2 * list.size()];
        int index = 0;
        for (Contact contact : list) {
            String name = removeMoreString(contact.getName());
            String number = removeMoreString(contact.getNumber());
            byte[] nameData = name.getBytes();
            byte[] numberData = number.getBytes();
            System.arraycopy(nameData, 0, data, index, Math.min(nameData.length, dataSize - 1));
            index += dataSize;
            System.arraycopy(numberData, 0, data, index, Math.min(numberData.length, dataSize - 1));
            index += dataSize;
        }
        return data;
    }

    /**
     * 将字节数组转化为联系人列表
     *
     * @param data
     * @return
     */
    public static List<Contact> byteToContacts(byte[] data) {
        List<Contact> contacts = new ArrayList<>();
        if (data == null || data.length < 40) {
            return contacts;
        }
        for (int i = 0; i <= data.length - 40; i += 40) {
            byte[] nameData = new byte[20];
            byte[] numberData = new byte[20];
            System.arraycopy(data, i, nameData, 0, nameData.length);
            System.arraycopy(data, i + 20, numberData, 0, numberData.length);
            String name = new String(nameData).trim();
            String number = new String(numberData).trim();
            Contact contact = new Contact();
            contact.setName(name);
            contact.setNumber(number);
            contacts.add(contact);
        }

        return contacts;
    }

    private static String removeMoreString(String text) {
        while (text.getBytes().length > 19) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

}
