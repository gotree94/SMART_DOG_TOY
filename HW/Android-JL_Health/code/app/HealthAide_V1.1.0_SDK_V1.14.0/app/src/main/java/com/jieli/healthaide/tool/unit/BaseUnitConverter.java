package com.jieli.healthaide.tool.unit;


import android.app.Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;


/**
 * 功能:以公制单位为基础，自动转换为其他类型的单位，目前支持两种：英制、公制
 */

public abstract class BaseUnitConverter implements LifecycleObserver {
    public final static int TYPE_METRIC = 0;
    public final static int TYPE_IMPERIAL = 1;


    private final IUnitListener iUnitListener;
    private double value;
    private static int sType = TYPE_METRIC;

    private final Activity activity;


    /**
     * @param activity      为null或者不为AppCompatActivity时不注册回显更新
     * @param value         公制值
     * @param iUnitListener 单位变化时监听器
     */
    public BaseUnitConverter(@Nullable Activity activity, double value, IUnitListener iUnitListener) {
        this.iUnitListener = iUnitListener;
        this.value = value;
        updateValue();
        if (activity instanceof AppCompatActivity) {
            AppCompatActivity compatActivity = (AppCompatActivity) activity;
            compatActivity.getLifecycle().addObserver(this);
        }
        this.activity = activity;
    }

    public void setValue(double value) {
        this.value = value;
        updateValue();
    }

    public static int getType() {
        return sType;
    }

    public static void setType(int type) {
        BaseUnitConverter.sType = type;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        updateValue();
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void release() {
        if (activity != null && activity instanceof AppCompatActivity) {
            AppCompatActivity compatActivity = (AppCompatActivity) activity;
            compatActivity.getLifecycle().removeObserver(this);
        }

    }

    private void updateValue() {
        Converter converter = getConverter(sType);
        if (iUnitListener != null) {
            iUnitListener.onChange(converter.value(value), converter.unit());
        }
    }

    public abstract Converter getConverter(int type);


}
