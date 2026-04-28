package com.jieli.healthaide.tool.aiui.model;

/**
 * StateResult
 *
 * @author zhongzhuocheng
 * email: zhongzhuocheng@zh-jieli.com
 * create: 2025/11/11
 * note: 状态结果
 */
public class StateResult<T> extends OpResult<T> {
    /**
     * 状态码
     */
    private int state;
    /**
     * 进度
     */
    private float progress;

    public StateResult() {
        this(0, -1);
    }

    public StateResult(int op, int code) {
        super(op, code);
    }

    public int getState() {
        return state;
    }

    public StateResult<T> setState(int state) {
        this.state = state;
        return this;
    }

    public float getProgress() {
        return progress;
    }

    public StateResult<T> setProgress(float progress) {
        this.progress = progress;
        return this;
    }

    @Override
    public StateResult<T> setOp(int op) {
        return (StateResult<T>) super.setOp(op);
    }

    @Override
    public StateResult<T> setCode(int code) {
        return (StateResult<T>) super.setCode(code);
    }

    @Override
    public StateResult<T> setMessage(String message) {
        return (StateResult<T>) super.setMessage(message);
    }

    @Override
    public StateResult<T> setResult(T result) {
        return (StateResult<T>) super.setResult(result);
    }

    @Override
    public String toString() {
        return "StateResult{" +
                "state=" + state +
                ", progress=" + progress +
                ", op=" + getOp() +
                ", code=" + getCode() +
                ", message='" + getMessage() + '\'' +
                ", result=" + getResult() +
                '}';
    }
}
