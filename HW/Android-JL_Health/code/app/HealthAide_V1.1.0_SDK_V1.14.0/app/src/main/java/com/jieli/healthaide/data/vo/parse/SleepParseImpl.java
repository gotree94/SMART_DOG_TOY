package com.jieli.healthaide.data.vo.parse;


import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 5/31/21
 * @desc :
 */
public class SleepParseImpl implements IParser<ParseEntity> {
    private final static String TAG = SleepParseImpl.class.getSimpleName();
    public Analysis analysis;

    @Override
    public List<ParseEntity> parse(HealthEntity entity) {
        analysis = new Analysis();
        if (entity.getVersion() == 0) {
            return new SleepParseImpl.ParserV0().parse(entity);
        }
        return new ArrayList<>();
    }

    public static class Analysis {
        public int analysisSleepGrade;//睡眠得分
        public int analysisDeepSleepRatio;//深睡比例
        public int analysisLightSleepRatio;//浅睡比例
        public int analysisREMRatio;//rem比例
        public int analysisAllDurationAppraise;//总时长评价
        public int analysisDeepSleepDurationAppraise;//深睡评价
        public int analysisLightSleepDurationAppraise;//浅睡评价
        public int analysisREMDurationAppraise;//rem评价
        public int analysisDeepSleepGrade;//深睡连续性得分
        public int analysisAwakeTime;//夜间醒来次数

    }

    private class ParserV0 implements IParser<ParseEntity> {
        private final Map<Integer, Integer> typeValueMap = new HashMap<Integer, Integer>() {{
            this.put(255, 3);
            this.put(1, 1);
            this.put(2, 0);
            this.put(3, 2);
            this.put(4, 4);
        }};

        @Override

        public List<ParseEntity> parse(HealthEntity entity) {
            List<ParseEntity> entities = new ArrayList<>();
            byte[] origin = entity.getData();
            if (origin != null && origin.length > 15) {
                if (origin[0] == (byte) 0x05) {
                    JL_Log.d(TAG, "ParserV0", "parse ---> " + CHexConver.byte2HexStr(origin));
                    Calendar calendar = Calendar.getInstance();
                    int year = CHexConver.bytesToInt(origin[1], origin[2]);
                    int month = 0xff & origin[3];
                    int day = 0xff & origin[4];
                    int offset = 11;
                    while (offset + 4 <= origin.length) {
                        byte[] tempArray = new byte[4];
                        System.arraycopy(origin, offset, tempArray, 0, tempArray.length);
                        int hour = 0xff & tempArray[0];
                        int minute = 0xff & tempArray[1];
                        int dataLen = CHexConver.bytesToInt(tempArray[2], tempArray[3]);
                        if (hour <= 24 && minute <= 60) {
                            if (offset + dataLen > origin.length) {
                                break;
                            }
                            calendar.set(year, month - 1, day, hour, minute);
                            byte[] data = new byte[dataLen];
                            System.arraycopy(origin, offset + 4, data, 0, dataLen);
                            //先计算duration
                            int duration = 0;
                            for (int i = 0; i + 2 <= data.length; i += 2) {
                                int min = data[i + 1] & 0xFF;
                                duration += min;
                            }
                            calendar.add(Calendar.MINUTE, duration);
                            calendar.set(year, month - 1, day);
                            Date endDate = calendar.getTime();
                            calendar.add(Calendar.MINUTE, -duration);
                            Date startDate = calendar.getTime();
                            long time = calendar.getTimeInMillis();//算得真实的起始时间

                            if (inDarkSleepRange(startDate, endDate, duration)) {//夜间睡眠
                                ParseEntity last = null;
                                for (int i = 0; i + 2 <= data.length; i += 2) {
                                    Integer type = typeValueMap.get(data[i] & 0xFF);
                                    if (type == null) continue;
                                    int min = data[i + 1] & 0xFF;
                                    min = Math.max(min, 0);
                                    long endTime = time + min * 60000;
                                    if (last != null && ((int) last.getValue()) == type) {
                                        last.setEndTime(endTime);
                                    } else {
                                        ParseEntity current = new ParseEntity(time, endTime, type);
                                        entities.add(current);
                                        last = current;
                                    }
                                    time = endTime;
                                }
                            } else {//零星小睡
                                entities.add(new ParseEntity(time, time + duration * 60000, 0x04));
                            }

                        } else if (tempArray[0] == (byte) 0xff && tempArray[1] == (byte) 0xff) {//睡眠的分析数据
                            if (dataLen >= 7 && dataLen + offset + 4 <= origin.length) {
                                byte[] analysisDataArray = new byte[dataLen];
                                System.arraycopy(origin, offset + 4, analysisDataArray, 0, analysisDataArray.length);
                                analysis.analysisSleepGrade = ValueUtil.byteToInt(analysisDataArray[0]);
                                analysis.analysisDeepSleepRatio = ValueUtil.byteToInt(analysisDataArray[1]);
                                analysis.analysisLightSleepRatio = ValueUtil.byteToInt(analysisDataArray[2]);
                                analysis.analysisREMRatio = ValueUtil.byteToInt(analysisDataArray[3]);
                                analysis.analysisAllDurationAppraise = analysisDataArray[4] & 0xC0;
                                analysis.analysisDeepSleepDurationAppraise = analysisDataArray[4] & 0x30;
                                analysis.analysisLightSleepDurationAppraise = analysisDataArray[4] & 0x0C;
                                analysis.analysisREMDurationAppraise = analysisDataArray[4] & 0x03;
                                analysis.analysisDeepSleepGrade = ValueUtil.byteToInt(analysisDataArray[5]);
                                analysis.analysisAwakeTime = ValueUtil.byteToInt(analysisDataArray[6]);
                            }
                        }
                        offset += (dataLen + 4);
                    }
                }
            }
            return entities;
        }

        //掉在白天的睡眠为零星小睡
        private boolean inDarkSleepRange(Date startDate, Date endDate, long time) {//起始或结束时间在零点到六点，或者持续时间大于两小时
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDate);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            Date rangeStartDate = calendar.getTime();
            calendar.set(Calendar.HOUR_OF_DAY, 6);
            Date rangeEndDate = calendar.getTime();
            boolean isContain = startDate.before(rangeStartDate) && endDate.after(rangeEndDate);//睡眠时间跨度超过了0点到6点
            boolean ret = isContain || (time >= 120 && ((startDate.after(rangeStartDate) && startDate.before(rangeEndDate)) || (endDate.after(rangeStartDate) && endDate.before(rangeEndDate))));//起始或结束时间在零点到六点且持续时间大于两小时
           /* {
                Log.d("ZHM", "inDarkSleepRange: " + (time >= 120) + time);
                Log.d("ZHM", "inDarkSleepRange: " + (startDate.after(rangeStartDate) && startDate.before(rangeEndDate)));
                Log.d("ZHM", "inDarkSleepRange: " + (endDate.after(rangeStartDate) && endDate.before(rangeEndDate)));
                Log.d("ZHM", "inDarkSleepRange: " + ret);

            }*/
            return ret;
        }
    }
}