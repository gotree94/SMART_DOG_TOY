package com.jieli.healthaide.data.vo.weight;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.BaseParseVo;
import com.jieli.healthaide.data.vo.parse.ParseEntity;

/**
 * @ClassName: WeightBaseVo
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/8/20 14:35
 */
public abstract class WeightBaseVo extends BaseParseVo {
    protected final double VALUE_MIN = 10;
    protected final double VALUE_MAX = 250;
    public int highLightIndex;
    /**
     * 时间段最大值(体重)
     */
    public double maxVal;
    /**
     * 时间段最小值(体重)
     */
    public double minVal;
    /**
     * 变化范围(体重)
     * todo 华为健康的变化是指最后两次数据的差值
     * todo 我认为应该是一周的起始数据和结束数据的变化范围
     */
    public double changeRange;
    /**
     * 平均值(体重)
     */
    public double averageVal;

    @Override
    public byte getType() {
        return HealthEntity.DATA_TYPE_WEIGHT;
    }

    public static class WeightBarCharData extends ParseEntity {
        public int index;
        public double value;
    }
}
