package com.jieli.healthaide.ui.device.contact;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/16/21 2:13 PM
 * @desc :
 */
public class Contact implements Cloneable {

    private static final int NAME_LIMIT = 20;

    private static final int PHONE_NUMBER_LIMIT = 16;

    /**
     * 格式化字符串
     *
     * @param string   String 字符串
     * @param maxBytes int 最大字节数(不包含)
     * @return String 截取字符串
     */
    public static String formatString(String string, int maxBytes) {
        if (null == string || maxBytes <= 0) return "";
        if (string.getBytes().length < maxBytes) return string;
        int byteLen = 0;
        StringBuilder sb = new StringBuilder();
        for (char c : string.toCharArray()) {
            int len = String.valueOf(c).getBytes(StandardCharsets.UTF_8).length;
            if (byteLen + len >= maxBytes) break;
            sb.append(c);
            byteLen += len;
        }
        return sb.toString();
    }


    private String typeName = "手机";
    private String name;
    private String number;
    private String pinyinName;
    public String letterName;
    private String phoneUri;
    private boolean select;
    private int fileId;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = formatString(name, NAME_LIMIT);
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = formatString(number, PHONE_NUMBER_LIMIT);
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public boolean isSelect() {
        return select;
    }


    public void setPinyinName(String pinyinName) {
        this.pinyinName = pinyinName;
    }

    public String getPinyinName() {
        return pinyinName;
    }

    public void setLetterName(String letterName) {
        this.letterName = letterName;
    }

    public String getLetterName() {
        return letterName;
    }

    public void setPhoneUri(String phoneUri) {
        this.phoneUri = phoneUri;
    }

    public String getPhoneUri() {
        return phoneUri;
    }


    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public int getFileId() {
        return fileId;
    }

    @NonNull
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "Contact{" +
                "typeName='" + typeName + '\'' +
                ", name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", pinyinName='" + pinyinName + '\'' +
                ", letterName='" + letterName + '\'' +
                ", phoneUrl='" + phoneUri + '\'' +
                ", select=" + select +
                '}';
    }
}
