package com.jieli.healthaide.ui.device.bean;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 手表操作数据
 * @since 2021/3/11
 */
public class WatchOpData {
    public final static int STATE_START = 1;
    public final static int STATE_PROGRESS = 2;
    public final static int STATE_END = 3;

    public final static int OP_CREATE_FILE = 1;
    public final static int OP_DELETE_FILE = 2;
    public final static int OP_REPLACE_FILE = 3;
    public final static int OP_RESTORE_SYS = 255;

    private int state; //阶段
    private int op;
    private String filePath; //路径
    private float progress;  //进度
    private int result; //结果
    private String message; //信息

    public int getState() {
        return state;
    }

    public WatchOpData setState(int state) {
        this.state = state;
        return this;
    }

    public int getOp() {
        return op;
    }

    public WatchOpData setOp(int op) {
        this.op = op;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public WatchOpData setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public float getProgress() {
        return progress;
    }

    public WatchOpData setProgress(float progress) {
        this.progress = progress;
        return this;
    }

    public int getResult() {
        return result;
    }

    public WatchOpData setResult(int result) {
        this.result = result;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public WatchOpData setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public String toString() {
        return "WatchOpData{" +
                "state=" + state +
                ", op=" + op +
                ", filePath='" + filePath + '\'' +
                ", progress=" + progress +
                ", result=" + result +
                ", message='" + message + '\'' +
                '}';
    }
}
