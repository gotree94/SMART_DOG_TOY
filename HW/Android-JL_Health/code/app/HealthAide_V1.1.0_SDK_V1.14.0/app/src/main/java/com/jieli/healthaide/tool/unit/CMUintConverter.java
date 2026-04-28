package com.jieli.healthaide.tool.unit;

import android.app.Activity;

import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 5/26/21
 * @desc :
 */
public class CMUintConverter extends BaseUnitConverter {

    public CMUintConverter(){
        super(null,0,null);
    }

    public CMUintConverter(Activity activity, double value, IUnitListener unit) {
        super(activity, value, unit);
    }



    @Override
    public Converter getConverter(int type) {
        return type == TYPE_METRIC ? new MetricConverter() : new ImperialConverter();
    }

    private static class MetricConverter implements Converter {

        @Override
        public double value(double origin) {
            return origin;
        }

        @Override
        public String unit() {
            return HealthApplication.getAppViewModel().getApplication().getString(R.string.unit_cm);
        }

        @Override
        public double origin(double value) {
            return value;
        }
    }

    private static class ImperialConverter implements Converter {

        @Override
        public double value(double origin) {
            return 0.394 * origin;
        }

        @Override
        public String unit() {
            return HealthApplication.getAppViewModel().getApplication().getString(R.string.unit_in);
        }

        @Override
        public double origin(double value) {
            return value/0.394;
        }
    }


}
