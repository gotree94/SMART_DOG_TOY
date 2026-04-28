package com.jieli.healthaide.tool.history;

import com.jieli.bluetooth_connect.bean.history.HistoryRecord;

import java.util.Objects;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 连接记录任务
 * @since 2021/7/21
 */
public class HistoryTask {
    private int op;
    private String id;
    private HistoryRecord record;

    public final static int OP_ADD = 0;
    public final static int OP_REMOVE = 1;

    public HistoryTask(int op, String id, HistoryRecord record) {
        setOp(op);
        setId(id);
        setRecord(record);
    }

    public int getOp() {
        return op;
    }

    public void setOp(int op) {
        this.op = op;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HistoryRecord getRecord() {
        return record;
    }

    public void setRecord(HistoryRecord record) {
        this.record = record;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoryTask task = (HistoryTask) o;
        return op == task.op &&
                Objects.equals(id, task.id) &&
                Objects.equals(record, task.record);
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, id, record);
    }

    @Override
    public String toString() {
        return "HistoryTask{" +
                "op=" + op +
                ", id='" + id + '\'' +
                ", record=" + record +
                '}';
    }
}
