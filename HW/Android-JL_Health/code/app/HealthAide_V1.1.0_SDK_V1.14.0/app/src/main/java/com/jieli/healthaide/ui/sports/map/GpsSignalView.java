package com.jieli.healthaide.ui.sports.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.LevelListDrawable;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;

import com.jieli.healthaide.R;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/8/21
 * @desc :
 */
public class GpsSignalView extends AppCompatTextView implements GpsStatus.Listener {
    private final static String TAG = GpsSignalView.class.getSimpleName();
    private LocationManager locationManager;
    private GnssStatus.Callback callback;
    private boolean isListenerGps;

    public GpsSignalView(@NonNull Context context) {
        super(context);
    }

    public GpsSignalView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GpsSignalView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private LevelListDrawable drawable;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        JL_Log.e(TAG, "onAttachedToWindow", "");
        drawable = (LevelListDrawable) getResources().getDrawable(R.drawable.gps_signal_level_imgs);
        drawable.setLevel(0);
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) return;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            listenerGps();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, mLocationListener);
        } else {
            JL_Log.e(TAG, "onAttachedToWindow", "not permission");
        }
        setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unListenerGps();
        locationManager.removeUpdates(mLocationListener);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onGpsStatusChanged(int event) {
        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            GpsStatus gpsStatus = locationManager.getGpsStatus(null);
            int count = 0;
            int size = 0;
            for (GpsSatellite satellite : gpsStatus.getSatellites()) {
                if (satellite.usedInFix()) {
                    count++;
                }
                size++;
            }
            onStatlliteCountChange(count, size);
        }

    }

    private void onStatlliteCountChange(int size, int fixSize) {
        int level = fixSize / 3;
        level = Math.min(level, 3);
//        setText("f"+fixSize+"s="+size);
        drawable.setLevel(level);
    }

    @SuppressLint("MissingPermission")
    private void listenerGps() {
        if (!isListenerGps) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                callback = new GnssStatus.Callback() {
                    @Override
                    public void onStopped() {
                        super.onStopped();
                        JL_Log.w(TAG, "onStopped", "gps is stopped.");
                    }

                    @Override
                    public void onStarted() {
                        super.onStarted();
                        JL_Log.i(TAG, "onStarted", "gps is started.");
                    }

                    @Override
                    public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
                        super.onSatelliteStatusChanged(status);
//                        JL_Log.d(TAG, "onSatelliteStatusChanged", "status : " + status);
                        int count = status.getSatelliteCount();
                        StringBuilder sb = new StringBuilder();
                        sb.append("count = ").append(count).append("\t");
                        int fc = 0;
                        for (int i = 0; i < count; i++) {
                            sb.append("index = ").append(i).append("  usefix = ").append(status.usedInFix(i));
                            if (status.usedInFix(i)) {
                                fc++;
                            }
                        }
//                        JL_Log.d("sen", sb.toString());
                        onStatlliteCountChange(count, fc);

                    }
                };
                if (locationManager != null) {
                    locationManager.registerGnssStatusCallback(callback, new Handler(Looper.getMainLooper()));
                }

            } else {
                locationManager.addGpsStatusListener(this);
            }
            isListenerGps = true;
        }
    }

    private void unListenerGps() {
        if (isListenerGps) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locationManager.unregisterGnssStatusCallback(callback);
            } else {
                locationManager.removeGpsStatusListener(this);
            }
            isListenerGps = false;
        }
    }


    private final LocationListener mLocationListener = new LocationListener() {
        private boolean isDisabled = false;

        @Override
        public void onLocationChanged(@NonNull Location location) {
            JL_Log.d(TAG, "onLocationChanged", "" + location);
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            JL_Log.i(TAG, "onProviderEnabled", provider);
            if (isDisabled) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, mLocationListener);
                }
                isDisabled = false;
            }
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            JL_Log.w(TAG, "onProviderDisabled", "provider : " + provider);
            isDisabled = true;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            JL_Log.w(TAG, "onStatusChanged", "provider : " + provider + ", status = " + status);
        }
    };
}
