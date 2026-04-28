package com.jieli.healthaide.ui.sports.model;

import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.model.command.watch.SportsInfoStatusSyncCmd;
import com.jieli.jl_rcsp.task.smallfile.QueryFileTask;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/10/27
 * @desc :
 */
public class SportsInfo {
    public static final int TYPE_OUTDOOR = SportsInfoStatusSyncCmd.SPORTS_TYPE_OUTDOOR;
    public static final int TYPE_INDOOR =  SportsInfoStatusSyncCmd.SPORTS_TYPE_INDOOR;
    public static final int STATUS_STOP = StateCode.SPORT_STATE_NONE;
    public static final int STATUS_BEGIN = StateCode.SPORT_STATE_RUNNING;
    public static final int STATUS_PAUSE = StateCode.SPORT_STATE_PAUSE;
    public static final int STATUS_RESUME = StateCode.SPORT_STATE_RESUME;
    public static final int STATUS_FAILED = 0x04;
    public int type;//运动模式
    public int id;//运动id
    public long startTime;//运动id
    public int status;//运动状态
    public boolean useMap;//app是否使用地图
    public int titleRes; //标题
    public int readRealDataInterval = 1000;//实时数据获取时间间隔
    public int heartRateMode;//心率模式

    public QueryFileTask.File file;//设备运动状态下有效


    @Override
    public String toString() {
        return "SportsInfo{" +
                "type=" + type +
                ", status=" + status +
                ", useMap=" + useMap +
                ", titleRes=" + titleRes +
                ", heartRateMode=" + heartRateMode +
                ", readRealDataInterval=" + readRealDataInterval +
                ", id=" + id +
                ", file=" + file +
                '}';
    }
}
