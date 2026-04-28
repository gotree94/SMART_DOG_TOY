package com.jieli.healthaide.data.vo.heart_rate;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.BaseParseVo;
import com.jieli.healthaide.data.vo.parse.ParseEntity;

/**
 * @ClassName: HeartRateBaseVo
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/6/7 19:59
 */
public abstract class HeartRateBaseVo extends BaseParseVo {
    protected int VALUE_MAX = 220;
    protected int VALUE_MIN = 0;
    public float max;
    public float min;
    public float restingAvg;//静息心率的平均值
    public int highLightIndex;

    @Override
    public byte getType() {
        return HealthEntity.DATA_TYPE_HEART_RATE;
    }

    public static class HeartRateCharData extends ParseEntity {
        public HeartRateCharData(int index, float max, float min) {
            this.index = index;
            this.max = max;
            this.min = min;
        }

        public float max;//心率范围的最大值
        public float min;//心率范围的最小值
        public int index;
        public float restingRate;//静息心率
    }
}
