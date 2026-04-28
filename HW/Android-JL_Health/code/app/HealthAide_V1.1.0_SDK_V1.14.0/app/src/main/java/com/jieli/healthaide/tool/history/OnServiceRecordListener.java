package com.jieli.healthaide.tool.history;

import com.jieli.healthaide.ui.device.bean.DeviceHistoryRecord;
import com.jieli.jl_health_http.model.device.DevMessage;

import java.util.List;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 服务器缓存历史列表
 * @since 2021/7/23
 */
public interface OnServiceRecordListener {

    void onServiceRecord(List<DeviceHistoryRecord> recordList);

    void onRecordChange(int op, DevMessage devMessage);
}
