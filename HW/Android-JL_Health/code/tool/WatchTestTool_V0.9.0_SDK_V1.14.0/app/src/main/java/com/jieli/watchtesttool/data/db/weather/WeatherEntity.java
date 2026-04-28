package com.jieli.watchtesttool.data.db.weather;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.jieli.jl_rcsp.model.command.watch.PushInfoDataToDeviceCmd;
import com.jieli.jl_rcsp.util.CHexConver;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 天气数据
 * @since 2023/1/13
 */
@Entity
public class WeatherEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String mac;
    private String province;
    private String city;
    private int weather;
    private int temperature;
    private int humidity;
    private int windDirection;
    private int windPower;
    private long time;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getWeather() {
        return weather;
    }

    public void setWeather(int weather) {
        this.weather = weather;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(int windDirection) {
        this.windDirection = windDirection;
    }

    public int getWindPower() {
        return windPower;
    }

    public void setWindPower(int windPower) {
        this.windPower = windPower;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Ignore
    public PushInfoDataToDeviceCmd.Weather convertData() {
        return new PushInfoDataToDeviceCmd.Weather(province, city,
                CHexConver.intToByte(weather), CHexConver.intToByte(temperature),
                CHexConver.intToByte(humidity), CHexConver.intToByte(windDirection),
                CHexConver.intToByte(windPower), time);
    }

    @Override
    public String toString() {
        return "WeatherEntity{" +
                "id=" + id +
                ", mac='" + mac + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", weather=" + weather +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", windDirection=" + windDirection +
                ", windPower=" + windPower +
                ", time=" + time +
                '}';
    }
}
