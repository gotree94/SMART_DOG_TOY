package com.jieli.watchtesttool.data.db.message;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.jieli.jl_rcsp.model.NotificationMsg;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 信息数据
 * @since 2023/1/13
 */
@Entity
public class MessageEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String mac;
    private String packageName;
    private String title;
    private String content;
    private long updateTime;
    private int flag;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public NotificationMsg convertData() {
        return convertData(NotificationMsg.OP_PUSH);
    }
    public NotificationMsg convertData(int op) {
        return new NotificationMsg()
                .setAppName(packageName)
                .setTitle(title)
                .setContent(content)
                .setTime(updateTime)
                .setFlag(flag)
                .setOp(op);
    }
}
