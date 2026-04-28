package com.jieli.healthaide.ui.health.sleep.charts.day;

import com.github.mikephil.charting.interfaces.dataprovider.BarLineScatterCandleBubbleDataProvider;

public interface SleepDayDataProvider extends BarLineScatterCandleBubbleDataProvider {

    SleepDayData getSleepData();

}
