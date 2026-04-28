package com.jieli.healthaide;

import com.jieli.healthaide.util.CalendarUtil;

import org.junit.Test;

import java.util.Calendar;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 5/27/21
 * @desc :
 */
public class CalenderTest {

    @Test
    public void testRemoveTime(){

        Calendar calendar = Calendar.getInstance();
        System.out.println("current time :"+calendar.getTime());
        int time = 123439;
        double timed = time;
        float timef = time;
        float timef1 = (float) timed;

        long time1 = (long) timef;

        System.out.println("time = "+time);
        System.out.printf("timef = %.2f%n",timef);
        System.out.printf("timed = %.2f%n",timed);
        System.out.printf("timef1 = %.2f%n",timef1);
        System.out.println("time1 = "+time1);


        long removeTime = CalendarUtil.removeTime(calendar.getTimeInMillis());
        calendar.setTimeInMillis(removeTime);
        System.out.print("after remove  time:"+calendar.getTime());
    }
}
