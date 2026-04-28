package com.jieli.watchtesttool.util;

import android.content.Context;

import com.jieli.watchtesttool.R;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 天气工具类
 * @since 2023/1/29
 */
public class WeatherUtil {
    /**
     * 晴朗
     */
    public static final int WEATHER_SUNNY = 0;
    /**
     * 少云/局部多云
     */
    public static final int WEATHER_PARTLY_CLOUDY = 1;
    /**
     * 晴转多云
     */
    public static final int WEATHER_CLEAR_TO_OVERCAST = 2;
    /**
     * 多云
     */
    public static final int WEATHER_CLOUDY = 3;
    /**
     * 阴天
     */
    public static final int WEATHER_OVERCAST = 4;
    /**
     * 和风/清风/微风
     */
    public static final int WEATHER_BREEZE = 5;
    /**
     * 平静天气
     */
    public static final int WEATHER_CALM = 6;
    /**
     * 大风/疾风
     */
    public static final int WEATHER_HIGH_WIND = 7;
    /**
     * 飓风
     */
    public static final int WEATHER_HURRICANE = 8;
    /**
     * 风暴
     */
    public static final int WEATHER_WINDSTORM = 9;
    /**
     * 雾霾
     */
    public static final int WEATHER_HAZE = 10;
    /**
     * 阵雨
     */
    public static final int WEATHER_SHOWER = 11;
    /**
     * 雷阵雨
     */
    public static final int WEATHER_THUNDERSHOWER = 12;
    /**
     * 雷阵雨并伴有冰雹
     */
    public static final int WEATHER_THUNDERSTORM_AND_HAIL = 13;
    /**
     * 小雨
     */
    public static final int WEATHER_LIGHT_RAIN = 14;
    /**
     * 中雨
     */
    public static final int WEATHER_MODERATE_RAIN = 15;
    /**
     * 大雨
     */
    public static final int WEATHER_HEAVY_RAIN = 16;
    /**
     * 暴雨
     */
    public static final int WEATHER_INTENSE_FALL = 17;
    /**
     * 大暴雨
     */
    public static final int WEATHER_RAINSTORM = 18;
    /**
     * 特大暴雨
     */
    public static final int WEATHER_HEAVY_DOWNPOUR = 19;
    /**
     * 强降雨
     */
    public static final int WEATHER_HEAVY_RAINFALL = 20;
    /**
     * 强雷阵雨
     */
    public static final int WEATHER_STRONG_THUNDERSHOWER = 21;
    /**
     * 极端降雨
     */
    public static final int WEATHER_EXTREME_RAINFALL = 22;
    /**
     * 雨夹雪
     */
    public static final int WEATHER_SLEET = 23;
    /**
     * 雪
     */
    public static final int WEATHER_SNOW = 24;
    /**
     * 阵雪
     */
    public static final int WEATHER_SNOW_SHOWER = 25;
    /**
     * 小雪
     */
    public static final int WEATHER_LIGHT_SNOW = 26;
    /**
     * 中雪
     */
    public static final int WEATHER_MODERATE_SNOW = 27;
    /**
     * 大雪
     */
    public static final int WEATHER_HEAVY_SNOW = 28;
    /**
     * 暴雪
     */
    public static final int WEATHER_SNOWSTORM = 29;
    /**
     * 浮尘
     */
    public static final int WEATHER_FLOATING_DUST = 30;
    /**
     * 扬尘
     */
    public static final int WEATHER_RAISE_DUST = 31;
    /**
     * 沙尘暴
     */
    public static final int WEATHER_SANDSTORM = 32;
    /**
     * 强沙尘暴
     */
    public static final int WEATHER_STRONG_SANDSTORM = 33;
    /**
     * 龙卷风
     */
    public static final int WEATHER_TORNADO = 34;
    /**
     * 浓雾
     */
    public static final int WEATHER_SMOG = 35;
    /**
     * 热
     */
    public static final int WEATHER_HOT = 36;
    /**
     * 冷
     */
    public static final int WEATHER_COLD = 37;

    /**
     * 无风向
     */
    public static final int WIND_NONE = 0;
    /**
     * 东风
     */
    public static final int WIND_EAST = 1;
    /**
     * 南风
     */
    public static final int WIND_SOUTH = 2;
    /**
     * 西风
     */
    public static final int WIND_WEST = 3;
    /**
     * 北风
     */
    public static final int WIND_NORTH = 4;
    /**
     * 东南风
     */
    public static final int WIND_SOUTHEAST = 5;
    /**
     * 东北风
     */
    public static final int WIND_NORTHEAST = 6;
    /**
     * 西北风
     */
    public static final int WIND_NORTHWEST = 7;
    /**
     * 西南风
     */
    public static final int WIND_SOUTHWEST = 8;
    /**
     * 旋转不定风
     */
    public static final int WIND_BAFFLING = 9;

    /**
     * 获得天气描述
     *
     * @param context 上下文
     * @param weather 天气编码
     * @return 天气描述
     */
    public static String getWeatherDesc(Context context, int weather) {
        if (null == context) return "";
        String desc = "";
        switch (weather) {
            case WEATHER_SUNNY:
                desc = context.getString(R.string.weather_sunny);
                break;
            case WEATHER_PARTLY_CLOUDY:
                desc = context.getString(R.string.weather_partly_cloudy);
                break;
            case WEATHER_CLEAR_TO_OVERCAST:
                desc = context.getString(R.string.weather_clear_to_overcast);
                break;
            case WEATHER_CLOUDY:
                desc = context.getString(R.string.weather_cloudy);
                break;
            case WEATHER_OVERCAST:
                desc = context.getString(R.string.weather_overcast);
                break;
            case WEATHER_BREEZE:
                desc = context.getString(R.string.weather_breeze);
                break;
            case WEATHER_CALM:
                desc = context.getString(R.string.weather_calm);
                break;
            case WEATHER_HIGH_WIND:
                desc = context.getString(R.string.weather_high_wind);
                break;
            case WEATHER_HURRICANE:
                desc = context.getString(R.string.weather_hurricane);
                break;
            case WEATHER_WINDSTORM:
                desc = context.getString(R.string.weather_windstorm);
                break;
            case WEATHER_HAZE:
                desc = context.getString(R.string.weather_haze);
                break;
            case WEATHER_SHOWER:
                desc = context.getString(R.string.weather_shower);
                break;
            case WEATHER_THUNDERSHOWER:
                desc = context.getString(R.string.weather_thundershower);
                break;
            case WEATHER_THUNDERSTORM_AND_HAIL:
                desc = context.getString(R.string.weather_thundershower_and_hail);
                break;
            case WEATHER_LIGHT_RAIN:
                desc = context.getString(R.string.weather_light_rain);
                break;
            case WEATHER_MODERATE_RAIN:
                desc = context.getString(R.string.weather_moderate_rain);
                break;
            case WEATHER_HEAVY_RAIN:
                desc = context.getString(R.string.weather_heavy_rain);
                break;
            case WEATHER_INTENSE_FALL:
                desc = context.getString(R.string.weather_intense_fall);
                break;
            case WEATHER_RAINSTORM:
                desc = context.getString(R.string.weather_rainstorm);
                break;
            case WEATHER_HEAVY_DOWNPOUR:
                desc = context.getString(R.string.weather_heavy_downpour);
                break;
            case WEATHER_HEAVY_RAINFALL:
                desc = context.getString(R.string.weather_heavy_rainfall);
                break;
            case WEATHER_STRONG_THUNDERSHOWER:
                desc = context.getString(R.string.weather_strong_thundershower);
                break;
            case WEATHER_EXTREME_RAINFALL:
                desc = context.getString(R.string.weather_extreme_rainfall);
                break;
            case WEATHER_SLEET:
                desc = context.getString(R.string.weather_sleet);
                break;
            case WEATHER_SNOW:
                desc = context.getString(R.string.weather_snow);
                break;
            case WEATHER_SNOW_SHOWER:
                desc = context.getString(R.string.weather_snow_shower);
                break;
            case WEATHER_LIGHT_SNOW:
                desc = context.getString(R.string.weather_light_snow);
                break;
            case WEATHER_MODERATE_SNOW:
                desc = context.getString(R.string.weather_moderate_snow);
                break;
            case WEATHER_HEAVY_SNOW:
                desc = context.getString(R.string.weather_heavy_snow);
                break;
            case WEATHER_SNOWSTORM:
                desc = context.getString(R.string.weather_snowstorm);
                break;
            case WEATHER_FLOATING_DUST:
                desc = context.getString(R.string.weather_floating_dust);
                break;
            case WEATHER_RAISE_DUST:
                desc = context.getString(R.string.weather_raise_dust);
                break;
            case WEATHER_SANDSTORM:
                desc = context.getString(R.string.weather_sandstorm);
                break;
            case WEATHER_STRONG_SANDSTORM:
                desc = context.getString(R.string.weather_strong_sandstorm);
                break;
            case WEATHER_TORNADO:
                desc = context.getString(R.string.weather_tornado);
                break;
            case WEATHER_SMOG:
                desc = context.getString(R.string.weather_smog);
                break;
            case WEATHER_HOT:
                desc = context.getString(R.string.weather_hot);
                break;
            case WEATHER_COLD:
                desc = context.getString(R.string.weather_cold);
                break;
        }
        return desc;
    }

    /**
     * 获取风向描述
     *
     * @param context 上下文
     * @param wind    风向编码
     * @return 风向描述
     */
    public static String getWindDesc(Context context, int wind) {
        if (null == context) return "";
        String desc = "";
        switch (wind) {
            case WIND_NONE:
                desc = context.getString(R.string.calm);
                break;
            case WIND_EAST:
                desc = context.getString(R.string.east_wind);
                break;
            case WIND_SOUTH:
                desc = context.getString(R.string.south_wind);
                break;
            case WIND_WEST:
                desc = context.getString(R.string.west_wind);
                break;
            case WIND_NORTH:
                desc = context.getString(R.string.north_wind);
                break;
            case WIND_SOUTHEAST:
                desc = context.getString(R.string.southeast_wind);
                break;
            case WIND_NORTHEAST:
                desc = context.getString(R.string.northeast_wind);
                break;
            case WIND_NORTHWEST:
                desc = context.getString(R.string.northwest_wind);
                break;
            case WIND_SOUTHWEST:
                desc = context.getString(R.string.southwest_wind);
                break;
            case WIND_BAFFLING:
                desc = context.getString(R.string.baffling_wind);
                break;
        }
        return desc;
    }
}
