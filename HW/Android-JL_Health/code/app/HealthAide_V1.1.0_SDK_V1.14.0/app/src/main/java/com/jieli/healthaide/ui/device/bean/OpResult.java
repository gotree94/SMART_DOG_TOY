package com.jieli.healthaide.ui.device.bean;

import com.jieli.jl_rcsp.util.RcspUtil;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 操作结果
 * @since 2022/6/17
 */
public class OpResult<T> {
    private int op;
    private int code;
    private String message;
    private T result;

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

    public boolean isOk() {
        return code == 0;
    }

    @Override
    public String toString() {
        return "OpResult{" +
                "op=" + RcspUtil.formatInt(op) +
                ", code=" + RcspUtil.formatInt(code) +
                ", message='" + message + '\'' +
                ", result=" + result +
                '}';
    }
}
