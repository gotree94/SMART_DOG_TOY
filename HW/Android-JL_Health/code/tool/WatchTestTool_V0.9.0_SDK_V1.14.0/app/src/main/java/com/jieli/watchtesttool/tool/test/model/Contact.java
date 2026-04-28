package com.jieli.watchtesttool.tool.test.model;

import androidx.annotation.NonNull;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/16/21 2:13 PM
 * @desc :
 */
public class Contact implements Cloneable{
    private String typeName ="手机";
    private String name;
    private String number;

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
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
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

                '}';
    }
}
