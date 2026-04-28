package com.jieli.healthaide.ui.device.bean;

import java.util.List;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 历史记录列表
 * @since 2021/11/26
 */
public class DevRecordListBean {
    private final List<DeviceHistoryRecord> list;
    private int usingIndex;

    public DevRecordListBean(List<DeviceHistoryRecord> list) {
        this(list, 0);
    }

    public DevRecordListBean(List<DeviceHistoryRecord> list, int index) {
        this.list = list;
        setUsingIndex(index);
    }

    public List<DeviceHistoryRecord> getList() {
        return list;
    }

    public int getUsingIndex() {
        return usingIndex;
    }

    public void setUsingIndex(int usingIndex) {
        this.usingIndex = usingIndex;
    }
}
