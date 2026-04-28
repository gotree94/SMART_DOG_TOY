package com.jieli.watchtesttool.data.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.jieli.jl_fatfs.model.FatFile;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 表盘信息
 * @since 2021/3/11
 */
public class WatchInfo implements Parcelable {
    public final static int WATCH_STATUS_NONE_EXIST = 0;
    public final static int WATCH_STATUS_EXIST = 1;
    public final static int WATCH_STATUS_USING = 2;

    private String name; //表盘名
    private int status; //表盘状态
    private String bitmapUri; //表盘缩略图链接
    private String uuid; //表盘服务器唯一标识
    private String version; //表盘版本
    private long size; //表盘文件大小
    private String fileUrl; //文件链接
    private FatFile mFatFile; //表盘文件信息
    private String updateUUID;  //更新的UUID
    private String updateVersion; //更新版本
    private String updateUrl; //更新下载链接
    private String customBgFatPath; //自定义背景路径

    public WatchInfo() {

    }

    protected WatchInfo(Parcel in) {
        name = in.readString();
        status = in.readInt();
        bitmapUri = in.readString();
        uuid = in.readString();
        version = in.readString();
        size = in.readLong();
        fileUrl = in.readString();
        mFatFile = in.readParcelable(FatFile.class.getClassLoader());
        updateUUID = in.readString();
        updateVersion = in.readString();
        updateUrl = in.readString();
        customBgFatPath = in.readString();
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

    public String getName() {
        return name;
    }

    public WatchInfo setName(String name) {
        this.name = name;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public WatchInfo setStatus(int status) {
        this.status = status;
        return this;
    }

    public String getBitmapUri() {
        return bitmapUri;
    }

    public WatchInfo setBitmapUri(String bitmapUri) {
        this.bitmapUri = bitmapUri;
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

    public long getSize() {
        return size;
    }

    public WatchInfo setSize(long size) {
        this.size = size;
        return this;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public WatchInfo setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
        return this;
    }

    public FatFile getFatFile() {
        return mFatFile;
    }

    public WatchInfo setFatFile(FatFile fatFile) {
        mFatFile = fatFile;
        return this;
    }

    public String getUpdateUUID() {
        return updateUUID;
    }

    public WatchInfo setUpdateUUID(String updateUUID) {
        this.updateUUID = updateUUID;
        return this;
    }

    public String getUpdateVersion() {
        return updateVersion;
    }

    public WatchInfo setUpdateVersion(String updateVersion) {
        this.updateVersion = updateVersion;
        return this;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public WatchInfo setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
        return this;
    }

    public boolean hasUpdate() {
        return updateUUID != null && updateVersion != null && version != null && updateVersion.compareTo(version) > 0 && updateUrl != null;
    }

    public String getCustomBgFatPath() {
        return customBgFatPath;
    }

    public WatchInfo setCustomBgFatPath(String customBgFatPath) {
        this.customBgFatPath = customBgFatPath;
        return this;
    }

    public boolean hasCustomBgFatPath() {
        return customBgFatPath != null && !customBgFatPath.equals("null") && customBgFatPath.contains("/BGP");
    }

    @Override
    public String toString() {
        return "WatchInfo{" +
                "name='" + name + '\'' +
                ", status=" + status +
                ", bitmapUri='" + bitmapUri + '\'' +
                ", uuid='" + uuid + '\'' +
                ", version='" + version + '\'' +
                ", size=" + size +
                ", fileUrl='" + fileUrl + '\'' +
                ", mFatFile=" + mFatFile +
                ", updateUUID='" + updateUUID + '\'' +
                ", updateVersion='" + updateVersion + '\'' +
                ", updateUrl='" + updateUrl + '\'' +
                ", customBgFatPath='" + customBgFatPath + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(status);
        dest.writeString(bitmapUri);
        dest.writeString(uuid);
        dest.writeString(version);
        dest.writeLong(size);
        dest.writeString(fileUrl);
        dest.writeParcelable(mFatFile, flags);
        dest.writeString(updateUUID);
        dest.writeString(updateVersion);
        dest.writeString(updateUrl);
        dest.writeString(customBgFatPath);
    }
}
