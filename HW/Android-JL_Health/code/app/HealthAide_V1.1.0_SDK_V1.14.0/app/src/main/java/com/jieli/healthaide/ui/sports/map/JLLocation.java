package com.jieli.healthaide.ui.sports.map;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/8/21
 * @desc :
 */
public class JLLocation implements Parcelable {
    public static final Creator<JLLocation> CREATOR = new Creator<JLLocation>() {
        @Override
        public JLLocation createFromParcel(Parcel source) {
            double latitude = source.readDouble();
            double longitude = source.readDouble();
            double speed = source.readDouble();
            double pace = source.readDouble();
            double distance = source.readDouble();
            JLLocation location = new JLLocation(latitude, longitude, speed);
            location.setPace(pace);
            location.setDistance(distance);
            return location;
        }

        @Override
        public JLLocation[] newArray(int size) {
            return new JLLocation[size];
        }
    };
    private double latitude;
    private double longitude;
    private double speed;
    private double pace;
    private long time;
    private double distance;



    public JLLocation(double latitude, double longitude, double speed) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
    }

    public JLLocation() {
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setPace(double pace) {
        this.pace = pace;
    }

    public double getPace() {
        return pace;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeDouble(this.speed);
        dest.writeDouble(this.pace);
        dest.writeDouble(this.distance);
    }

    @Override
    public String toString() {
        return "JLLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", speed=" + speed +
                ", pace=" + pace +
                '}';
    }
}
