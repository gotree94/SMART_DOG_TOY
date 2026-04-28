package com.jieli.healthaide.tool.aiui.model;

/**
 * OpResult
 *
 * @author zhongzhuocheng
 * email: zhongzhuocheng@zh-jieli.com
 * create: 2025/11/11
 * note: 操作结果
 */
public class OpResult<T> {

    public static final int ERR_NONE = 0;

    public static final int ERR_FAIL = 1;

    /**
     * 操作码
     */
    private int op;
    /**
     * 结果码
     */
    private int code;
    /**
     * 描述信息
     */
    private String message;
    /**
     * 结果
     */
    private T result;

    public OpResult() {
        this(0, -1);
    }

    public OpResult(int op, int code) {
        this.op = op;
        this.code = code;
    }

    public int getOp() {
        return op;
    }

    public OpResult<T> setOp(int op) {
        this.op = op;
        return this;
    }

    public int getCode() {
        return code;
    }

    public OpResult<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public OpResult<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public T getResult() {
        return result;
    }

    public OpResult<T> setResult(T result) {
        this.result = result;
        return this;
    }

    public boolean isSuccess() {
        return code == ERR_NONE;
    }

    @Override
    public String toString() {
        return "OpResult{" +
                "op=" + op +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", result=" + result +
                '}';
    }
}
