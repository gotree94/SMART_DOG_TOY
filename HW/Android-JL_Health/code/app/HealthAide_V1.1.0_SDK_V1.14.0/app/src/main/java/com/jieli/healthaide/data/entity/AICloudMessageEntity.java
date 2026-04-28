package com.jieli.healthaide.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * @ClassName: AICloudMessageEntity
 * @Description: AI云消息记录
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/7/17 15:57
 */
@Entity
public class AICloudMessageEntity {
    public static final int ROLE_USER = 0;//用户
    public static final int ROLE_AI = 1;//AI

    public static final byte AI_STATE_IDLE = 0;//默认状态
    public static final byte AI_STATE_RECORDING = 1;//录音中
    public static final byte AI_STATE_IAT_ING = 2;//语音识别转换中
    public static final byte AI_STATE_IAT_END = 3;//语音识别结束
    public static final byte AI_STATE_NLP_ING = 4;//语义识别转换中
    public static final byte AI_STATE_NLP_END = 5;//语义识别结束
    @PrimaryKey(autoGenerate = true)
    private long id; //唯一id
    @NonNull
    private String uid;//用户唯一id
    @NonNull
    private String devMac;//设备mac
    private int role = ROLE_USER;//角色类型
    private long time; //时间
    private long revId;//回复id
    private int aiCloudState = AI_STATE_IDLE;//语音识别状态
    private String text;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    @NonNull
    public String getDevMac() {
        return devMac;
    }

    public void setDevMac(@NonNull String devMac) {
        this.devMac = devMac;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getRevId() {
        return revId;
    }

    public void setRevId(long revId) {
        this.revId = revId;
    }

    public int getAiCloudState() {
        return aiCloudState;
    }

    public void setAiCloudState(int aiCloudState) {
        this.aiCloudState = aiCloudState;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
