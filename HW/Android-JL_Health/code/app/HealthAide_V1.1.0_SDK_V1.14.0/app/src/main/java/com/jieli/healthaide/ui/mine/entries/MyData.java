package com.jieli.healthaide.ui.mine.entries;

import androidx.annotation.NonNull;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/11/8
 * @desc :
 */
public class MyData {
    private int step;
    private float distance; //km
    private int kcal;


    private int heartRate;
    private float weight; //kg
    private int bloodOxygen; //0-100
    private int sleep;//min

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public int getKcal() {
        return kcal;
    }

    public void setKcal(int kcal) {
        this.kcal = kcal;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public int getBloodOxygen() {
        return bloodOxygen;
    }

    public void setBloodOxygen(int bloodOxygen) {
        this.bloodOxygen = bloodOxygen;
    }

    public int getSleep() {
        return sleep;
    }

    public void setSleep(int sleep) {
        this.sleep = sleep;
    }

    @NonNull
    @Override
    public String toString() {
        return "MyData{" +
                "step=" + step +
                ", distance=" + distance +
                ", kcal=" + kcal +
                ", heartRate=" + heartRate +
                ", weight=" + weight +
                ", bloodOxygen=" + bloodOxygen +
                ", sleep=" + sleep +
                '}';
    }
}
