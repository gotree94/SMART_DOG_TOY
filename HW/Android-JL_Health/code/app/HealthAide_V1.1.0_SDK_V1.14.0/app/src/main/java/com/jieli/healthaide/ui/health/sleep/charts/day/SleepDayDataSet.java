package com.jieli.healthaide.ui.health.sleep.charts.day;

import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.LineScatterCandleRadarDataSet;

import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/3/21 3:03 PM
 * @desc :
 */

public class SleepDayDataSet extends LineScatterCandleRadarDataSet<SleepDayEntry> implements  ISleepDayDataSet{


    public SleepDayDataSet(List<SleepDayEntry> yVals, String label) {
        super(yVals, label);
    }

    @Override
    public DataSet<SleepDayEntry> copy() {
        return null;
    }

    protected void copy( SleepDayDataSet sleepDayDataSet) {
        super.copy(sleepDayDataSet);
    }


}


