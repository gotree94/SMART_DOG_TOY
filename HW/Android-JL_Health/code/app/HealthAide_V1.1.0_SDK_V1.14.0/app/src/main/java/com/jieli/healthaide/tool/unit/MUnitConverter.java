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
public class MUnitConverter extends BaseUnitConverter {

    public MUnitConverter() {
        super(null, 0, null);
    }

    public MUnitConverter(Activity activity, double value, IUnitListener unit) {
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
            return HealthApplication.getAppViewModel().getApplication().getString(R.string.unit_m);
        }

        @Override
        public double origin(double value) {
            return value;
        }
    }

    private static class ImperialConverter implements Converter {

        @Override
        public double value(double origin) {
            return 3.281 * origin;
        }

        @Override
        public String unit() {
            return HealthApplication.getAppViewModel().getApplication().getString(R.string.unit_ft);
        }

        @Override
        public double origin(double value) {
            return value / 3.281;
        }
    }


}
