package com.jieli.healthaide.ui.mine;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.data.vo.BaseVo;
import com.jieli.healthaide.data.vo.livedatas.HealthLiveData;
import com.jieli.healthaide.data.vo.step.StepBaseVo;
import com.jieli.healthaide.data.vo.step.StepWeekVo;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_health_http.model.UserInfo;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/11/9
 * @desc :
 */
public class SportsWeeklyViewModel extends ViewModel {

    private static final String TAG = SportsWeeklyViewModel.class.getSimpleName();

    private final static long WEEK_TIME_MILL = 1000 * 60 * 60 * 24 * 7;
    private final HealthLiveData<StepWeekVo> thisWeek = new HealthLiveData<>(new StepWeekVo());

    private final HealthLiveData<StepWeekVo> lastWeek1 = new HealthLiveData<>(new StepWeekVo());
    private final HealthLiveData<StepWeekVo> lastWeek2 = new HealthLiveData<>(new StepWeekVo());
    private final HealthLiveData<StepWeekVo> lastWeek3 = new HealthLiveData<>(new StepWeekVo());

    private UserInfo userInfo;

    private final MediatorLiveData<Info> infoLiveData = new MediatorLiveData<>();


    public LiveData<Info> getLiveData() {
        return infoLiveData;
    }

    public SportsWeeklyViewModel() {
        infoLiveData.addSource(thisWeek, stepWeekVo -> {
            if (userInfo == null) return;
            JL_Log.d(TAG, "infoLiveData", "thisWeek  " + CalendarUtil.serverDateFormat().format(stepWeekVo.getStartTime()) + "-" + CalendarUtil.serverDateFormat().format(stepWeekVo.getEndTime()));
            Info info = getCacheInfo();
            info.totalDistance = stepWeekVo.getTotalDistance();
            info.totalStep = stepWeekVo.getTotalStep();
            info.totalKcal = stepWeekVo.getTotalKcal();
            info.allWeekString[3] = formatTime(stepWeekVo);
            info.allWeekValue[3] = stepWeekVo.getTotalStep();
            info.weekData = stepWeekVo.getEntities() == null ? new ArrayList<>() : stepWeekVo.getEntities();
            info.target = userInfo.getStep();
            int count = 0;
            for (StepBaseVo.StepChartData data : info.weekData) {
                if (data.value >= info.target && data.value > 0 && info.target > 0) {
                    count++;
                }
            }
            info.reachTarget = count;
            infoLiveData.setValue(info);
            refreshWeekData(lastWeek1, stepWeekVo.getStartTime() - WEEK_TIME_MILL, stepWeekVo.getEndTime() - WEEK_TIME_MILL);
        });
        infoLiveData.addSource(lastWeek1, stepWeekVo -> {
            if (userInfo == null) return;
            JL_Log.d(TAG, "infoLiveData", "lastWeek1  " + CalendarUtil.serverDateFormat().format(stepWeekVo.getStartTime()) + "-" + CalendarUtil.serverDateFormat().format(stepWeekVo.getEndTime()));
            Info info = getCacheInfo();
            info.allWeekString[2] = formatTime(stepWeekVo);
            info.allWeekValue[2] = stepWeekVo.getTotalStep();

            float totalDistance = stepWeekVo.getTotalDistance();
            int totalStep = stepWeekVo.getTotalStep();
            int totalKcal = stepWeekVo.getTotalKcal();

            int reachTarget = 0;
            List<StepBaseVo.StepChartData> list = stepWeekVo.getEntities() == null ? new ArrayList<>() : stepWeekVo.getEntities();
            for (StepBaseVo.StepChartData data : list) {
                if (data.value >= info.target && data.value > 0) {
                    reachTarget++;
                }
            }
            info.distanceUp = info.totalDistance - totalDistance;
            info.stepUp = info.totalStep - totalStep;
            info.kcalUp = info.totalKcal - totalKcal;

            info.targetUp = info.reachTarget - reachTarget;
            infoLiveData.setValue(info);
            refreshWeekData(lastWeek2, stepWeekVo.getStartTime() - WEEK_TIME_MILL, stepWeekVo.getEndTime() - WEEK_TIME_MILL);
        });
        infoLiveData.addSource(lastWeek2, stepWeekVo -> {
            if (userInfo == null) return;
            JL_Log.d(TAG, "infoLiveData", "lastWeek2  " + CalendarUtil.serverDateFormat().format(stepWeekVo.getStartTime()) + "-" + CalendarUtil.serverDateFormat().format(stepWeekVo.getEndTime()));
            Info info = getCacheInfo();
            info.allWeekString[1] = formatTime(stepWeekVo);
            info.allWeekValue[1] = stepWeekVo.getTotalStep();
            infoLiveData.setValue(info);
            refreshWeekData(lastWeek3, stepWeekVo.getStartTime() - WEEK_TIME_MILL, stepWeekVo.getEndTime() - WEEK_TIME_MILL);

        });
        infoLiveData.addSource(lastWeek3, stepWeekVo -> {
            if (userInfo == null) return;
            JL_Log.d(TAG, "infoLiveData", "lastWeek3  " + CalendarUtil.serverDateFormat().format(stepWeekVo.getStartTime()) + "-" + CalendarUtil.serverDateFormat().format(stepWeekVo.getEndTime()));
            Info info = getCacheInfo();
            info.allWeekString[0] = formatTime(stepWeekVo);
            info.allWeekValue[0] = stepWeekVo.getTotalStep();
            infoLiveData.setValue(info);
        });
    }

    @NonNull
    private Info getCacheInfo() {
        Info info = infoLiveData.getValue();
        if (info == null) {
            info = new Info();
        }
        return info;
    }


    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    //获取最后第一周得开始时间
    public long getStartTime() {
        long time = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        c.add(Calendar.DAY_OF_MONTH, -2);
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return c.getTimeInMillis();
    }

    //获取最后一周得结束时间
    public long getEndTime() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(getStartTime());
        c.add(Calendar.DAY_OF_MONTH, 6);
        return c.getTimeInMillis();
    }

    public void refreshWeekData(long startTime, long endTime) {
        refreshWeekData(thisWeek, startTime, endTime);
    }

    private void refreshWeekData(HealthLiveData<StepWeekVo> healthLiveData, long startTime, long endTime) {
        if (userInfo == null) return;
        String uid = HealthApplication.getAppViewModel().getUid();
        healthLiveData.refresh(uid, startTime, endTime);
    }


    private String formatTime(BaseVo baseVo) {
        long startTime = baseVo.getStartTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);


        int startMonth = calendar.get(Calendar.MONTH) + 1;
        int startDay = calendar.get(Calendar.DAY_OF_MONTH);
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(baseVo.getEndTime());
        int endMonth = calendar.get(Calendar.MONTH) + 1;
        int endDay = calendar.get(Calendar.DAY_OF_MONTH);

        return CalendarUtil.formatString("%02d/%02d-%02d/%02d", startMonth, startDay, endMonth, endDay);
    }

    static class Info {
        float totalDistance;
        int totalStep;
        int totalKcal;

        //月分星期统计
        String[] allWeekString = new String[]{"", "", "", ""};
        int[] allWeekValue = new int[4];

        //一星期的数据
        List<StepBaseVo.StepChartData> weekData = new ArrayList<>();

        //达标
        int reachTarget;
        int target = 0;

        //和上周的对比
        int stepUp;
        int kcalUp;
        float distanceUp;
        int targetUp;


        @NonNull
        @Override
        public String toString() {
            return "Info{" +
                    "totalDistance=" + totalDistance +
                    ", totalStep=" + totalStep +
                    ", totalKcal=" + totalKcal +
                    ", allWeekString=" + Arrays.toString(allWeekString) +
                    ", allWeekValue=" + Arrays.toString(allWeekValue) +
                    ", weekData=" + weekData +
                    ", reachTarget=" + reachTarget +
                    ", target=" + target +
                    ", stepUp=" + stepUp +
                    ", kcalUp=" + kcalUp +
                    ", distanceUp=" + distanceUp +
                    ", targetUp=" + targetUp +
                    '}';
        }
    }

}
