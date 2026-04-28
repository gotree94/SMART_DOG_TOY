package com.jieli.healthaide.ui.device.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.jieli.jl_fatfs.model.FatFile;
import com.jieli.jl_health_http.model.WatchFileMsg;

import java.util.Objects;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 表盘信息
 * @since 2021/3/11
 */
public class WatchInfo implements Parcelable {
    public final static int WATCH_STATUS_NOT_PAYMENT = 0;  //未付款状态
    public final static int WATCH_STATUS_NONE_EXIST = 1;   //已付款未下载状态
    public final static int WATCH_STATUS_EXIST = 2;        //已存在设备状态
    public final static int WATCH_STATUS_USING = 3;        //正在使用状态

    //基本信息
    private int status;              //表盘状态
    private String version;          //表盘版本
    private String uuid;             //表盘服务器唯一标识
    private String updateUUID;       //更新的UUID
    private String customBgFatPath;  //自定义背景路径

    //联系信息
    private FatFile watchFile;      //设备文件信息
    private WatchFileMsg serverFile; //服务器文件信息
    private WatchFileMsg updateFile; //服务器更新文件信息

    private boolean isCircleDial = true;    //是否圆形表盘


    public WatchInfo() {

    }

    protected WatchInfo(Parcel in) {
        status = in.readInt();
        version = in.readString();
        uuid = in.readString();
        updateUUID = in.readString();
        customBgFatPath = in.readString();

        watchFile = in.readParcelable(FatFile.class.getClassLoader());
        serverFile = in.readParcelable(WatchFileMsg.class.getClassLoader());
        updateFile = in.readParcelable(WatchFileMsg.class.getClassLoader());

        isCircleDial = in.readInt() == 1;
    }

    public static final Creator<WatchInfo> CREATOR = new Creator<WatchInfo>() {
        @Override
        public WatchInfo createFromParcel(Parcel in) {
            return new WatchInfo(in);
        }

        @Override
        public WatchInfo[] newArray(int size) {
            return new WatchInfo[size];
        }
    };

    public int getStatus() {
        return status;
    }

    public WatchInfo setStatus(int status) {
        this.status = status;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public WatchInfo setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public WatchInfo setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getUpdateUUID() {
        return updateUUID;
    }

    public WatchInfo setUpdateUUID(String updateUUID) {
        this.updateUUID = updateUUID;
        return this;
    }

    public String getCustomBgFatPath() {
        return customBgFatPath;
    }

    public WatchInfo setCustomBgFatPath(String customBgFatPath) {
        this.customBgFatPath = customBgFatPath;
        return this;
    }

    public FatFile getWatchFile() {
        return watchFile;
    }

    public WatchInfo setWatchFile(FatFile fatFile) {
        watchFile = fatFile;
        return this;
    }

    public WatchFileMsg getServerFile() {
        return serverFile;
    }

    public WatchInfo setServerFile(WatchFileMsg serverFile) {
        this.serverFile = serverFile;
        return this;
    }

    public WatchFileMsg getUpdateFile() {
        return updateFile;
    }

    public WatchInfo setUpdateFile(WatchFileMsg updateFile) {
        this.updateFile = updateFile;
        return this;
    }

    public boolean isCircleDial() {
        return isCircleDial;
    }

    public WatchInfo setCircleDial(boolean circleDial) {
        isCircleDial = circleDial;
        return this;
    }

    public String getName() {
        String name = null;
        if (watchFile != null) {
            name = watchFile.getName();
        }
        if (null == name && serverFile != null) {
            name = serverFile.getName();
        }
        return name;
    }

    public String getBitmapUri() {
        if (null == serverFile) return null;
        return serverFile.getIcon();
    }

    public long getSize() {
        long size = 0;
        if (null != watchFile) {
            size = watchFile.getSize();
        }
        return size;
    }

    public boolean hasUpdate() {
        return updateUUID != null && updateFile != null;
    }


    public boolean hasCustomBgFatPath() {
        return customBgFatPath != null && !customBgFatPath.equalsIgnoreCase("null");
    }

    public WatchInfo clone() {
        return new WatchInfo()
                .setStatus(status)
                .setUuid(uuid)
                .setVersion(version)
                .setUpdateUUID(updateUUID)
                .setCustomBgFatPath(customBgFatPath)
                .setWatchFile(watchFile)
                .setServerFile(serverFile)
                .setUpdateFile(updateFile)
                .setCircleDial(isCircleDial);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(status);
        dest.writeString(version);
        dest.writeString(uuid);
        dest.writeString(updateUUID);
        dest.writeString(customBgFatPath);

        dest.writeParcelable(watchFile, flags);
        dest.writeParcelable(serverFile, flags);
        dest.writeParcelable(updateFile, flags);

        dest.writeInt(isCircleDial ? 1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WatchInfo watchInfo = (WatchInfo) o;
        return version.equals(watchInfo.version) && uuid.equals(watchInfo.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, uuid);
    }

    @Override
    public String toString() {
        return "WatchInfo{" +
                "status=" + status +
                ", version='" + version + '\'' +
                ", uuid='" + uuid + '\'' +
                ", updateUUID='" + updateUUID + '\'' +
                ", customBgFatPath='" + customBgFatPath + '\'' +
                ", watchFile=" + watchFile +
                ", serverFile=" + serverFile +
                ", updateFile=" + updateFile +
                ", isCircleDial=" + isCircleDial +
                '}';
    }
}
