package com.jieli.healthaide.ui.device.alarm;

import android.content.Context;

import com.jieli.healthaide.R;
import com.jieli.jl_rcsp.model.device.AlarmBean;

public class Util {

    /*
     * 获取
     *
     * @param context
     * @param alarmBean
     * @return
     */
    public static String getRepeatDesc(Context context, AlarmBean alarmBean) {
        StringBuilder sb = new StringBuilder();
        String[] dayOfWeek = new String[]{context.getString(R.string.alarm_repeat_simple_mon),
                context.getString(R.string.alarm_repeat_simple_tue),
                context.getString(R.string.alarm_repeat_simple_wed),
                context.getString(R.string.alarm_repeat_simple_thu),
                context.getString(R.string.alarm_repeat_simple_fri),
                context.getString(R.string.alarm_repeat_simple_sat),
                context.getString(R.string.alarm_repeat_simple_sun)};
        int mode = alarmBean.getRepeatMode() & 0xff;
        if (mode == 0x00) {
            sb.append(context.getString(R.string.alarm_repeat_single));
        } else if ((mode & 0x01) == 0x01) {
            sb.append(context.getString(R.string.alarm_repeat_every_day));
        } else if (mode == 0xfe) {
            sb.append(context.getString(R.string.alarm_repeat_on_workday));
        } else {
            sb.append(context.getString(R.string.alarm_repeat_week));
            for (int i = 1; i < 8; i++) {
                int temp = mode;
                temp = temp >> i;
                temp = temp & 0x01;
                if (temp == 0x01) {
                    sb.append(dayOfWeek[i - 1]);
                    sb.append(" ");
                }
            }
        }
        return sb.toString().trim().replace(" ", "、");
    }

    /*
     * 获取闹钟信息的重复模式描述
     *
     * @param context   上下文
     * @param alarmBean 闹钟信息
     * @return 重复模式描述
     */
    public static String getRepeatDescModify(Context context, AlarmBean alarmBean) {
        StringBuilder sb = new StringBuilder();
        String result = null;
        int mode = alarmBean.getRepeatMode() & 0xff;
        if (mode == 0x00) {
            result = context.getString(R.string.alarm_repeat_single);
        } else if ((mode & 0x01) == 0x01) {
            result = context.getString(R.string.alarm_repeat_every_day);
        } else if (mode == 0x3e) {
            String[] dayOfWeek = context.getResources().getStringArray(R.array.alarm_weeks_workday);
            for (int i = 0; i < dayOfWeek.length; i++) {
                int temp = mode;
                temp = temp >> i + 1;
                temp = temp & 0x01;
                if (temp == 0x01) {
                    sb.append(dayOfWeek[i]);
                    sb.append(" ");
                }
            }
            result = sb.toString().trim().replace(" ", "，");
        } else {
            String[] dayOfWeek = context.getResources().getStringArray(R.array.alarm_weeks_simple);
            for (int i = 0; i < dayOfWeek.length; i++) {
                int temp = mode;
                temp = temp >> i + 1;
                temp = temp & 0x01;
                if (temp == 0x01) {
                    sb.append(dayOfWeek[i]);
                    sb.append(" ");
                }
            }
            result = sb.toString().trim().replace(" ", "，");
        }
        return result;
    }
}
