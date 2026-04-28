package com.jieli.healthaide.tool.customdial;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * @ClassName: CustomDialInfo
 * @Description: 自定义表盘信息
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/11 15:27
 */
public class CustomDialInfo implements Parcelable {

    /**
     * 用户Id
     */
    public String uid;

    /**
     * 设备地址
     */
    public String mac;

    /**
     * id（唯一）
     */
    public long id;

    /**
     * 原图的路径
     */
    public String srcImagePath;

    /**
     * 裁剪图的路径
     */
    public String cutImagePath;

    /**
     * 表盘(bmp)文件路径
     */
    public String bmpPath;
    /**
     * 上一次使用时间
     */
    public long updateTime;

    public CustomDialInfo() {
    }

    protected CustomDialInfo(Parcel in) {
        id = in.readLong();
        srcImagePath = in.readString();
        cutImagePath = in.readString();
        uid = in.readString();
        mac = in.readString();
//        bmpPath = in.readString();
        updateTime = in.readLong();
    }

    public static final Creator<CustomDialInfo> CREATOR = new Creator<CustomDialInfo>() {
        @Override
        public CustomDialInfo createFromParcel(Parcel in) {
            return new CustomDialInfo(in);
        }

        @Override
        public CustomDialInfo[] newArray(int size) {
            return new CustomDialInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(srcImagePath);
        dest.writeString(cutImagePath);
        dest.writeString(uid);
        dest.writeString(mac);
//        dest.writeString(bmpPath);
        dest.writeLong(updateTime);
    }
}
