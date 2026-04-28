package com.jieli.watchtesttool.ui.message.weather;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.jieli.watchtesttool.data.db.SensorDbBase;
import com.jieli.watchtesttool.data.db.weather.WeatherDao;
import com.jieli.watchtesttool.data.db.weather.WeatherEntity;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothViewModel;
import com.jieli.watchtesttool.tool.watch.WatchManager;
import com.jieli.watchtesttool.util.WeatherUtil;

import java.util.Calendar;
import java.util.List;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 同步天气逻辑处理
 * @since 2023/1/28
 */
public class SyncWeatherViewModel extends BluetoothViewModel {
    private final WeatherDao mWeatherDao;

    final MutableLiveData<List<WeatherEntity>> weathersMLD = new MutableLiveData<>();

    private boolean isFirst = true;

    public SyncWeatherViewModel() {
        mWeatherDao = SensorDbBase.getInstance().weatherDao();
    }

    public WatchManager getWatchManager() {
        return mWatchManager;
    }

    public void queryWeatherMessages() {
        if (!mWatchManager.isWatchSystemOk()) return;
        String mac = getConnectedDevice().getAddress();
        List<WeatherEntity> list = mWeatherDao.queryWeathers(mac);
        /*if (list.isEmpty() && isFirst) {
            isFirst = false;
            WeatherEntity weather = new WeatherEntity();
            weather.setMac(mac);
            weather.setTemperature(21);
            weather.setHumidity(64);
            weather.setWindDirection(WeatherUtil.WIND_SOUTHEAST);
            weather.setWindPower(6);
            weather.setWeather(WeatherUtil.WEATHER_CLEAR_TO_OVERCAST);
            weather.setProvince("广东省");
            weather.setCity("珠海市");
            Calendar calendar = Calendar.getInstance();
            calendar.set(2022, Calendar.DECEMBER, 25);
            weather.setTime(calendar.getTimeInMillis());
            mWeatherDao.insert(weather);
            list.add(weather);

            weather = new WeatherEntity();
            weather.setMac(mac);
            weather.setTemperature(23);
            weather.setHumidity(68);
            weather.setWindDirection(WeatherUtil.WIND_SOUTHEAST);
            weather.setWindPower(5);
            weather.setWeather(WeatherUtil.WEATHER_SUNNY);
            weather.setProvince("广东省");
            weather.setCity("珠海市");
            calendar = Calendar.getInstance();
            calendar.set(2023, Calendar.JANUARY, 13);
            weather.setTime(calendar.getTimeInMillis());
            mWeatherDao.insert(weather);
            list.add(weather);
        }*/
        weathersMLD.setValue(list);
    }

    public boolean insertWeather(@NonNull WeatherEntity weather) {
        WeatherEntity cache = mWeatherDao.queryWeather(weather.getMac(), weather.getProvince(), weather.getCity(), weather.getTime());
        if (null != cache) return false;
        mWeatherDao.insert(weather);
        queryWeatherMessages();
        return true;
    }

    public void deleteWeather(WeatherEntity weather) {
        if (null == weather) return;
        mWeatherDao.delete(weather);
        queryWeatherMessages();
    }

}
