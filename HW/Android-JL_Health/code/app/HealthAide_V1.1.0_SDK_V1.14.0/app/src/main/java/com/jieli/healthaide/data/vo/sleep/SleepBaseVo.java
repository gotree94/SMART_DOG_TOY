package com.jieli.healthaide.data.vo.sleep;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.BaseParseVo;
import com.jieli.healthaide.data.vo.parse.ParseEntity;
import com.jieli.healthaide.data.vo.parse.SleepParseImpl;

import java.util.Arrays;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 6/1/21
 * @desc :
 */
public abstract class SleepBaseVo extends BaseParseVo {
    public int highLightIndex;
    public float max = 0f;//最大值
    protected String tag = getClass().getSimpleName();
    protected List<Nap> napList;
    public int score;
    //    public boolean hasDarkSleep = false;
//    public double totalSleepTime;//如果没有夜间睡眠，只有零星小睡的话，totalSleepTime 为0；
    public long darkSleepTime;
    public long deepSleepTime;
    public long lightSleepTime;
    public long remSleepTime;
    public long awakeTime;
    public long napSleepTime;
    public int awakeNum;

    public SleepParseImpl.Analysis analysis;

    public List<Nap> getNapList() {
        return napList;
    }

    @Override
    public byte getType() {
        return HealthEntity.DATA_TYPE_SLEEP;
    }

    public static class Nap {
        public long startTimeStamp;
        public long endTimeStamp;

        public Nap(long startTimeStamp, long endTimeStamp) {
            this.startTimeStamp = startTimeStamp;
            this.endTimeStamp = endTimeStamp;
        }
    }

    public static class SleepBarCharData extends ParseEntity {
        public float[] data = new float[4];
        public int index;

        @Override
        public String toString() {
            return super.toString() + "\nSleepBarCharData{" +
                    "data=" + Arrays.toString(data) +
                    ", index=" + index +
                    '}';
        }
    }
}
