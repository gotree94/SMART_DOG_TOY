package com.jieli.healthaide;

import com.jieli.healthaide.util.CustomTimeFormatUtil;
import com.jieli.jl_rcsp.constant.AttrAndFunCode;
import com.jieli.jl_rcsp.exception.ParseDataException;
import com.jieli.jl_rcsp.model.command.watch.ReceiveHealthDataCmd;
import com.jieli.jl_rcsp.model.device.health.HealthData;
import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.jl_rcsp.util.HealthDataUtil;
import com.jieli.jl_rcsp.util.RcspUtil;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/7/28
 * @desc :
 */

public class ReceiveHealthDataCmdTest {

    @Test
    public void testRecombineHealthDataBuffer() {
        String text = "bad code : -10034, message : 设备不存在";
        boolean ret = text.contains(String.valueOf(-10034));

    }

    private ReceiveHealthDataCmd createCmd(byte count, byte id) throws ParseDataException {
        byte[] data = new byte[20];
        data[0] = 0x00;
        data[1] = count;
        data[2] = id;
        for (int i = 3; i < data.length; i++) {
            data[i] = (byte) (id * (data.length - 3) + i);
        }
        return new ReceiveHealthDataCmd(new ReceiveHealthDataCmd.Param(data));
    }

    @Test
    public void  testTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,2021);
        calendar.set(Calendar.MONTH,7);
        calendar.set(Calendar.DAY_OF_MONTH,2);
        calendar.set(Calendar.HOUR_OF_DAY,10);
        calendar.set(Calendar.MINUTE,10);
        calendar.set(Calendar.SECOND,10);
        int intTime = RcspUtil.time2Int(calendar.getTimeInMillis());

        System.out.println( "inTime ="+CHexConver.intToHexString(intTime));

        byte [] data = CHexConver.hexStr2Bytes("2E04A28A");
        long time = RcspUtil.intToTime(CHexConver.bytesToInt(data));
        SimpleDateFormat dateFormat = CustomTimeFormatUtil.dateFormat("yyyy-MM-dd hh:mm:ss");
        System.out.println(dateFormat.format(new Date(time)));
    }

    @Test
    public void testHealthDataParse() {
        HealthData data = new HealthData(AttrAndFunCode.HEALTH_DATA_TYPE_HEART_RATE, (byte) 0x07, new byte[]{0x01, 0x02, 0x03}, 0);
        System.out.println("------------------heart rate -----------");
        HealthDataUtil.parseHeartRate(data, real -> {
            System.out.println("real:" + real);
        }, resting -> {
            System.out.println("resting:" + resting);
        }, max -> {
            System.out.println("max:" + max);
        });


        data = new HealthData(AttrAndFunCode.HEALTH_DATA_TYPE_AIR_PRESSURE, (byte) 0x07, new byte[]{0x00, 0x01, 0x00, 0x02, 0x00, 0x03}, 0);
        System.out.println("------------------air pressure   -----------");
        HealthDataUtil.parseAirPressure(data, real -> {
            System.out.println("real:" + real);
        }, min -> {
            System.out.println("min:" + min);
        }, max -> {
            System.out.println("max:" + max);
        });

        data = new HealthData(AttrAndFunCode.HEALTH_DATA_TYPE_ALTITUDE, (byte) 0x07, new byte[]{0x00, 0x01, 0x00, 0x01, 0x00, 0x02, 0x00, 0x02, 0x00, 0x03, 0x00, 0x03}, 0);
        System.out.println("------------------  altitude   -----------");
        HealthDataUtil.parseAltitude(data, real -> {
            System.out.println("real:" + real);
        }, min -> {
            System.out.println("min:" + min);
        }, max -> {
            System.out.println("max:" + max);
        });


        System.out.println("------------------  step   -----------");
        data = new HealthData(AttrAndFunCode.HEALTH_DATA_TYPE_STEP, (byte) 0x07, new byte[]{0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x02}, 0);
        HealthDataUtil.parseStep(data, real -> {
            System.out.println("real:" + real);
        }, sport -> {
            System.out.println("sport:" + sport);
        });

        System.out.println("------------------  pressure   -----------");
        data = new HealthData(AttrAndFunCode.HEALTH_DATA_TYPE_PRESSURE, (byte) 0x07, new byte[]{0x01}, 0);
        HealthDataUtil.parsePressure(data, real -> {
            System.out.println("real:" + real);
        });

        System.out.println("------------------  blood oxygen   -----------");
        data = new HealthData(AttrAndFunCode.HEALTH_DATA_TYPE_BLOOD_OXYGEN, (byte) 0x07, new byte[]{0x01}, 0);
        HealthDataUtil.parseBloodOxygen(data, real -> {
            System.out.println("real:" + real);
        });
    }

}
