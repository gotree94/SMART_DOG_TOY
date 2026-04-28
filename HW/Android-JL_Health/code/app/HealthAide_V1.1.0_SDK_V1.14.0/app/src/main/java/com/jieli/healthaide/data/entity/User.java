package com.jieli.healthaide.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

/**
 * 用户信息
 *
 * @author zqjasonZhong
 * @since 2021/3/4
 */
@Entity
public class User {
    @PrimaryKey
    @NonNull
    private String uuid = "null";
    private String uid;
    @ColumnInfo(name = "access_token")
    private String accessToken;
    @ColumnInfo(name = "phone_number")
    private String phoneNumber;
    private String password;
    private String nickname;
    private String sex;
    private String birthday;
    private int stature;
    private int weight;
    private String avatar;
    @ColumnInfo(name = "sport_target")
    private int sportTarget;

    public User() {

    }

    @NotNull
    public String getUuid() {
        return uuid;
    }

    public void setUuid(@NotNull String uuid) {
        this.uuid = uuid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public int getStature() {
        return stature;
    }

    public void setStature(int stature) {
        this.stature = stature;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getSportTarget() {
        return sportTarget;
    }

    public void setSportTarget(int sportTarget) {
        this.sportTarget = sportTarget;
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "uuid='" + uuid + '\'' +
                ", uid='" + uid + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", password='" + password + '\'' +
                ", nickname='" + nickname + '\'' +
                ", sex='" + sex + '\'' +
                ", birthday='" + birthday + '\'' +
                ", stature=" + stature +
                ", weight=" + weight +
                ", avatar='" + avatar + '\'' +
                ", sportTarget=" + sportTarget +
                '}';
    }
}
