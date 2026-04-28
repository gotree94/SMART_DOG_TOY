package com.jieli.healthaide.tool.watch.synctask;

import android.annotation.SuppressLint;
import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.tool.net.NetWorkStateModel;
import com.jieli.healthaide.tool.net.NetworkStateHelper;
import com.jieli.healthaide.util.CustomTimeFormatUtil;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.command.watch.PushInfoDataToDeviceCmd;
import com.jieli.jl_rcsp.util.JL_Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 5/11/21
 * @desc :
 */
public class WeatherSyncTask extends DeviceSyncTask {
    private final String tag = getClass().getSimpleName();
    private final SyncTaskManager syncTaskManager;

    public WeatherSyncTask(SyncTaskManager manager, SyncTaskFinishListener finishListener) {
        super(finishListener);
        this.syncTaskManager = manager;
    }

    @Override
    public int getType() {
        return TASK_TYPE_SYNC_DEVICE_WEATHER;
    }

    @Override
    public void start() {
        if (mWatchManager.isFirmwareOTA()) {
            JL_Log.w(tag, "start", "device's ota is in progress.");
            if (finishListener != null) finishListener.onFinish();
            return;
        }
        if (checkEnv()) {
            refreshCity(HealthApplication.getAppViewModel().getApplication());
        } else {
            onFinish(false);
        }
    }

    private boolean checkEnv() {
        if (!mWatchManager.isConnected()) return false;
        NetWorkStateModel netWorkStateModel = NetworkStateHelper.getInstance().getNetWorkStateModel();
        //网络状态
        return netWorkStateModel != null && netWorkStateModel.isAvailable();
    }

    private void refreshCity(Context context) {
        AMapLocationClient locationClient = null;
        try {
            locationClient = new AMapLocationClient(context);
            locationClient.setLocationListener(mAMapLocationListener);
            AMapLocationClientOption option = new AMapLocationClientOption();
            option.setOnceLocation(true);
            option.setOnceLocationLatest(true);
            option.setHttpTimeOut(20000);
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            locationClient.setLocationOption(option);
            //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
            locationClient.stopLocation();
            locationClient.startLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshCityByIP() {
        String url = "http://restapi.amap.com/v3/ip?key=2824c5044e6cd020c3af5e5d923cb883";
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                JL_Log.w(tag, "refreshCityByIP", "onFailure: " + e);
                refreshWeather("北京市");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful() || null == response.body()) return;
                String res = response.body().string();
                JL_Log.d(tag, "refreshCityByIP", "onResponse: " + res);
                String city = null;
                try {
                    Gson gson = new GsonBuilder().create();
                    City cityObj = gson.fromJson(res, City.class);
                    if (cityObj != null) {
                        city = cityObj.getCity();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                refreshWeather(city != null ? city : "北京市");
            }
        });
    }

    private void refreshWeather(String city) {
        try {
            WeatherSearchQuery query = new WeatherSearchQuery(city, WeatherSearchQuery.WEATHER_TYPE_LIVE);
            WeatherSearch search = new WeatherSearch(HealthApplication.getAppViewModel().getApplication());
            search.setQuery(query);
            search.setOnWeatherSearchListener(new WeatherSearch.OnWeatherSearchListener() {
                @Override
                public void onWeatherLiveSearched(LocalWeatherLiveResult localWeatherLiveResult, int code) {
                    if (code != 1000) {
                        JL_Log.w(tag, "refreshWeather", "获取天气失败");
                        onFinish(false);
                        return;
                    }
                    if (localWeatherLiveResult == null || localWeatherLiveResult.getLiveResult() == null) {
                        JL_Log.w(tag, "refreshWeather", "没有天气数据");
                        onFinish(false);
                        return;
                    }
                    LocalWeatherLive result = localWeatherLiveResult.getLiveResult();

                    String sb = "实时天气数据:" +
                            "\n" +
                            "省份:" + result.getProvince() + "\t\t" +
                            "城市:" + result.getCity() + "\t\t" +
                            "城市编码:" + result.getAdCode() + "\n" +
                            "天气:" + result.getWeather() + "\t\t" +
                            "温度:" + result.getTemperature() + "\t\t" +
                            "湿度:" + result.getHumidity() + "\n" +
                            "方向:" + result.getWindDirection() + "\t\t" +
                            "风力:" + result.getWindPower() + "\n" +
                            "时间:" + result.getReportTime() +
                            "\n\n";
                    JL_Log.d(tag, "refreshWeather", sb);
                    syncWeatherToDevice(result);
                }

                @Override
                public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int code) {
                    onFinish(false);
                }
            });
            search.searchWeatherAsyn();
        } catch (AMapException e) {
            e.printStackTrace();
        }
    }

    private void syncWeatherToDevice(LocalWeatherLive result) {
        byte weatherCode = getWeatherCode(result.getWeather());
        byte windDirectionCode = getWindDirectionCode(result.getWindDirection());
        byte temperature = Byte.parseByte(result.getTemperature());
        byte humidity = Byte.parseByte(result.getHumidity());
        byte windPower = (result.getWindPower().equals("≤3")) ? 3 : Byte.parseByte(result.getWindPower());
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = CustomTimeFormatUtil.dateFormat("yyyy-MM-dd hh:mm:ss");
        long time = 0;
        try {
            Date date = format.parse(result.getReportTime());
            if (date != null) {
                time = date.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        PushInfoDataToDeviceCmd.Weather weather = new PushInfoDataToDeviceCmd.Weather(
                result.getProvince(), result.getCity(),
                weatherCode, temperature,
                humidity, windPower, windDirectionCode,
                time
        );
        mWatchManager.syncWeatherInfo(weather, new OnWatchOpCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                onFinish(true);
            }

            @Override
            public void onFailed(BaseError error) {
                JL_Log.w(tag, "syncWeatherToDevice", "天气同步失败, " + error);
                onFinish(false);
            }
        });

    }

    private void onFinish(boolean success) {
        int min = 1000 * 60;
        syncTaskManager.addWeatherTask(success ? 15 * min : 2 * min);//同步成功，间隔15分钟，同步失败，间隔2分钟
        finishListener.onFinish();
    }


    private byte getWeatherCode(String weather) {
        List<String> weathers = new ArrayList<>();
        weathers.add("晴");//0
        weathers.add("少云");//1
        weathers.add("晴间多云");//2
        weathers.add("多云");//3
        weathers.add("阴");//4
        weathers.add("有风/和风/清风/微风");//5
        weathers.add("平静");//6
        weathers.add("大风/强风/劲风/疾风");//7
        weathers.add("飓风/狂爆风");//8
        weathers.add("热带风暴/风暴");//9
        weathers.add("霾/中度霾/重度霾/严重霾");//10
        weathers.add("阵雨");//11
        weathers.add("雷阵雨");//12
        weathers.add("雷阵雨并伴有冰雹");//13
        weathers.add("雨/小雨/毛毛雨/细雨/小雨-中雨");//14
        weathers.add("中雨/中雨-大雨");//15
        weathers.add("大雨/大雨-暴雨");//16
        weathers.add("暴雨/暴雨-大暴雨");//17
        weathers.add("大暴雨/大暴雨-特大暴雨");//18
        weathers.add("特大暴雨");//19
        weathers.add("强阵雨");//20
        weathers.add("强雷阵雨");//21
        weathers.add("极端降雨");//22
        weathers.add("雨夹雪/阵雨夹雪/冻雨/雨雪天气");//23
        weathers.add("雪");//24
        weathers.add("阵雪");//25
        weathers.add("小雪/小雪-中雪");//26
        weathers.add("中雪/中雪-大雪");//27
        weathers.add("大雪/大雪-暴雪");//28
        weathers.add("暴雪");//29
        weathers.add("浮尘");//30
        weathers.add("扬沙");//31
        weathers.add("沙尘暴");//32
        weathers.add("强沙尘暴");//33
        weathers.add("龙卷风");//34
        weathers.add("雾/轻雾/浓雾/强浓雾/特强浓雾");//35
        weathers.add("热");//36
        weathers.add("冷");//37
        byte code = 38;
        for (int i = 0; i < weathers.size(); i++) {
            String tmp = weathers.get(i);
            if (weather.equals(tmp) || tmp.contains("/" + weather) || tmp.contains(weather + "/")) {
                code = (byte) i;
                break;
            }
        }

        return code;
    }

    private byte getWindDirectionCode(String windDirection) {
        List<String> windDirections = new ArrayList<>();
        windDirections.add("无风向");//0
        windDirections.add("东");//1
        windDirections.add("南");//2
        windDirections.add("西");//3
        windDirections.add("北");//4
        windDirections.add("东南");//5
        windDirections.add("东北");//6
        windDirections.add("西北");//7
        windDirections.add("西南");//8
        windDirections.add("旋转不定");//9
        byte code = 0;
        for (int i = 0; i < windDirections.size(); i++) {
            String tmp = windDirections.get(i);
            if (windDirection.equals(tmp)) {
                code = (byte) i;
                break;
            }
        }
        return code;
    }

    private final AMapLocationListener mAMapLocationListener = aMapLocation -> {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                refreshWeather(aMapLocation.getCity());
            } else {
                if (aMapLocation.getErrorCode() == AMapLocation.ERROR_CODE_FAILURE_LOCATION_PERMISSION) {
                    refreshCityByIP();
                } else {
                    onFinish(false);
                }
                JL_Log.d(tag, "onLocationChanged", "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    };

    private static class City {
        String status;
        String info;
        String infocode;
        String province;
        String city;
        String adcode;
        String rectangle;

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        public String getInfocode() {
            return infocode;
        }

        public void setInfocode(String infocode) {
            this.infocode = infocode;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getAdcode() {
            return adcode;
        }

        public void setAdcode(String adcode) {
            this.adcode = adcode;
        }

        public String getRectangle() {
            return rectangle;
        }

        public void setRectangle(String rectangle) {
            this.rectangle = rectangle;
        }
    }
}
