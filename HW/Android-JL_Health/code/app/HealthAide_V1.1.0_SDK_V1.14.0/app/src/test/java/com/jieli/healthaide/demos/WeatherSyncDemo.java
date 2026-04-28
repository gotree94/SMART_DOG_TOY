package com.jieli.healthaide.demos;

import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.command.watch.PushInfoDataToDeviceCmd;

import org.junit.Test;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/12/1
 * @desc : 同步天气
 */
public class WeatherSyncDemo {
    @Test
    public void syncWeatherInformation() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        String province = "省份";
        String city = "城市";
        byte weatherCode = 0;//天气编码，参考天气编码表
        byte temperature = 0;//温度
        byte humidity = 0; //湿度
        byte windPower = 0;//风力等级
        byte windDirectionCode = 0; //方向编码，参考方向编码表
        long time = System.currentTimeMillis(); //时间
        //构建天气信息
        PushInfoDataToDeviceCmd.Weather weather = new PushInfoDataToDeviceCmd.Weather(province, city,
                weatherCode, temperature,
                humidity, windPower, windDirectionCode,
                time
        );
        //执行同步天气信息功能并等待结果回调
        watchManager.syncWeatherInfo(weather, new OnWatchOpCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //天气同步成功
            }

            @Override
            public void onFailed(BaseError error) {
                //天气同步失败
                //error - 错误信息
            }
        });
    }
}
