package com.jieli.watchtesttool.tool.upgrade.auto;

import androidx.annotation.NonNull;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 更新参数
 * @since 2022/8/2
 */
public class UpdateParam {
    private final String filePath;
    private final OnUpdateListener listener;
    private boolean isShowTime;
    private boolean isShowSpeed;

    public UpdateParam(@NonNull String filePath, @NonNull OnUpdateListener listener) {
        this.filePath = filePath;
        this.listener = listener;
    }

    public String getFilePath() {
        return filePath;
    }

    public OnUpdateListener getListener() {
        return listener;
    }

    public boolean isShowTime() {
        return isShowTime;
    }

    public void setShowTime(boolean showTime) {
        isShowTime = showTime;
    }

    public boolean isShowSpeed() {
        return isShowSpeed;
    }

    public void setShowSpeed(boolean showSpeed) {
        isShowSpeed = showSpeed;
    }

    @Override
    public String toString() {
        return "UpdateParam{" +
                "filePath='" + filePath + '\'' +
                ", listener=" + listener +
                ", isShowTime=" + isShowTime +
                ", isShowSpeed=" + isShowSpeed +
                '}';
    }
}
