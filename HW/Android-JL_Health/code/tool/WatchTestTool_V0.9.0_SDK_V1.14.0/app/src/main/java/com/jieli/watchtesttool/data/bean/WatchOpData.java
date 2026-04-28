package com.jieli.watchtesttool.data.bean;

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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getOp() {
        return op;
    }

    public void setOp(int op) {
        this.op = op;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "WatchOpData{" +
                "state=" + state +
                ", op=" + op +
                ", filePath='" + filePath + '\'' +
                ", progress=" + progress +
                ", result=" + result +
                '}';
    }
}
