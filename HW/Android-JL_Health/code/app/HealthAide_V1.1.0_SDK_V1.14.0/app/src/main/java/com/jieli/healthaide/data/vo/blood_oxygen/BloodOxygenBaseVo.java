package com.jieli.healthaide.data.vo.blood_oxygen;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.BaseParseVo;
import com.jieli.healthaide.data.vo.parse.ParseEntity;

/**
 * @ClassName: BloodOxygenBaseVo
 * @Description: 血氧的基础解析Vo 属性：血氧范围（max min）和高亮位置
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/6/7 19:59
 */
public abstract class BloodOxygenBaseVo extends BaseParseVo {
    protected int VALUE_MAX = 100;
    protected int VALUE_MIN = 0;
    public float max;
    public float min;
    public int highLightIndex;

    @Override
    public byte getType() {
        return HealthEntity.DATA_TYPE_BLOOD_OXYGEN;
    }

    public static class BloodOxygenBarCharData extends ParseEntity {
        /**
         * @param index 在chart x轴的数值
         * @param max   血氧的最大值（范围
         * @param min   血氧的最小值（范围
         */
        public BloodOxygenBarCharData(int index, float max, float min) {
            this.index = index;
            this.max = max;
            this.min = min;
        }

        public float max;
        public float min;
        public int index;
    }
}
