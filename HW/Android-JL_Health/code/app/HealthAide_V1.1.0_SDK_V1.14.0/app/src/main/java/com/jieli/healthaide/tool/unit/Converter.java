package com.jieli.healthaide.tool.unit;

public interface Converter {

        /**
         * 根据单位类型获取值
         *
         * @param origin
         * @return
         */
        double value(double origin);

        /**
         * 根据单位类型获取单位
         *
         * @return
         */
        String unit();

        /**
         * 获取公立单位的值
         * @param value
         * @return
         */
        double origin(double value);


    }

