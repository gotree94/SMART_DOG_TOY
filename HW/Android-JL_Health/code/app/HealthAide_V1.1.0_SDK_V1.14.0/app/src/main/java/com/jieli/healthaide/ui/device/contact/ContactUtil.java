package com.jieli.healthaide.ui.device.contact;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.jieli.component.Logcat;
import com.jieli.jl_rcsp.util.JL_Log;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/16/21 5:26 PM
 * @desc :
 */
public class ContactUtil {
    private static final String TAG = ContactUtil.class.getSimpleName();

    public static List<Contact> queryContacts(Context context, String selection, String[] selectionArgs) {
        String[] projection = {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
                ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
                ContactsContract.CommonDataKinds.Phone._ID
        };

        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection,
                selection, selectionArgs, ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY + " ASC");
        if (cursor == null || cursor.getCount() < 1) {
            return new ArrayList<>();
        }
        cursor.moveToFirst();
        List<Contact> list = new ArrayList<>();
        try {
            do {
                Contact contacts = new Contact();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                contacts.setName(name);
                contacts.setNumber(cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                long photoId = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_ID));
                String uri = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                JL_Log.d(TAG, "queryContacts", "photoId = " + photoId + "\turi" + uri);
                if (photoId > 0) {
                    contacts.setPhoneUri(uri);
                }
//            if (!TextUtils.isEmpty(contacts.getName())) {
//                contacts.setName(StringUtil.removeAllMark(contacts.getName()));
//            }
                PinyinBean pinyinBean = getPinYin(name);
                contacts.setPinyinName(pinyinBean.getPinyinName());
                contacts.setLetterName(pinyinBean.getLetterName());
                list.add(contacts);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        cursor.close();
        return list;
    }


    public static List<Contact> searchContactByName(Context context, String name) {
        String selection = "";
        selection = selection + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like ?";
        return queryContacts(context, selection, new String[]{"%" + name + "%"});
    }

    public static List<Contact> searchContactByNumber(Context context, String number) {
        String selection = "";
        selection = selection + ContactsContract.CommonDataKinds.Phone.NUMBER + " like ?";
        return queryContacts(context, selection, new String[]{"%" + number + "%"});
    }


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

    public static PinyinBean getPinYin(String chines) {
        StringBuilder sb = new StringBuilder();
        String pinyin1 = "";
        String pinyin2 = "";

        try {
            char[] nameChar = chines.toLowerCase(Locale.CHINA).toCharArray();
            HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
            defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            //defaultFormat.setToneType(HanyuPinyinToneType.WITH_TONE_NUMBER);
            defaultFormat.setVCharType(HanyuPinyinVCharType.WITH_V);

            for (int i = 0; i < nameChar.length; i++) {
                if (nameChar[i] > 128) {
                    try {
                        String pinyin = PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat)[0];
                        pinyin1 = pinyin1 + pinyin;
                        pinyin = pinyin.replace("ang", "an");
                        pinyin = pinyin.replace("eng", "en");
                        pinyin = pinyin.replace("ing", "in");
                        pinyin = pinyin.replace("ch", "c");
                        pinyin = pinyin.replace("sh", "s");
                        pinyin = pinyin.replace("zh", "z");
                        pinyin = pinyin.replace("hu", "wu");
                        pinyin = pinyin.replace("long", "rong");
                        pinyin = pinyin.replace("yun", "yue");
                        pinyin2 = pinyin2 + pinyin;
                        sb.append(pinyin.charAt(0));
                    } catch (Exception e) {
                        Logcat.w(TAG, e.getMessage());
                    }
                } else {
                    sb.append(nameChar[i]);
                    pinyin1 = pinyin1 + nameChar[i];
                    pinyin2 = pinyin2 + nameChar[i];
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        PinyinBean pinyinBean = new PinyinBean();
        pinyinBean.setPinyinName(pinyin1);
        pinyinBean.setPinyinNameWithNoMatch(pinyin2);
        pinyinBean.setLetterName(sb.toString());
        return pinyinBean;
    }

}
