package com.jieli.healthaide.ui.sports.map;

import android.graphics.Color;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/6/21
 * @desc :
 */
public class MapUtil {
    public static void commonSingleMapStyle(AMap aMap) {
        commonMapStyle(aMap, MyLocationStyle.LOCATION_TYPE_LOCATE);
    }

    public static void commonFollowMapStyle(AMap aMap) {
        commonMapStyle(aMap, MyLocationStyle.LOCATION_TYPE_FOLLOW);
    }

    public static void commonMapStyle(AMap aMap, int style) {
        MyLocationStyle locationStyle = new MyLocationStyle();
        locationStyle.strokeColor(Color.TRANSPARENT);
        locationStyle.interval(5000);
        locationStyle.radiusFillColor(Color.TRANSPARENT);
        locationStyle.myLocationType(style);
        aMap.setMyLocationStyle(locationStyle);
        aMap.setMyLocationEnabled(true);
        aMap.getUiSettings().setMyLocationButtonEnabled(false);
        aMap.getUiSettings().setZoomControlsEnabled(false);
        CameraPosition position = aMap.getCameraPosition();
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position.target, 18f));
    }


    //计算距离
    public static float getDistance(List<JLLocation> list) {
        float distance = 0;
        if (list == null || list.size() == 0) {
            return distance;
        }
        for (int i = 0; i < list.size() - 1; i++) {
            LatLng firstLatLng = new LatLng(list.get(i).getLatitude(), list.get(i).getLongitude());
            LatLng secondLatLng = new LatLng(list.get(i + 1).getLatitude(), list.get(i + 1).getLongitude());
            double betweenDis = AMapUtils.calculateLineDistance(firstLatLng,
                    secondLatLng);
            distance = (float) (distance + betweenDis);
        }
        return distance;
    }


    /**
     * 描述: 计算卡路里
     * ---------计算公式：体重（kg）* 距离（km）* 运动系数（k）
     * ---------运动系数：健走：k=0.8214；跑步：k=1.036；自行车：k=0.6142；轮滑、溜冰：k=0.518室外滑雪：k=0.888
     *
     * @param weight   体重
     * @param distance 距离
     */
    public static double calculationCalorie(double weight, double distance) {
        return weight * distance * 1.036;
    }

    /**
     * 转换为配速字符串
     *
     * @param pace
     * @return
     */
    public static String toPaceString(double pace) {
        int min = (int) pace;
        int sec = (int) (((pace * 100) % 100) * 0.6f);
        return min + "'" + sec + "\"";
    }


    /**
     * 将坐标点转化为每公里配速列表
     *
     * @param locations
     * @return
     */
    public static List<LatLng> toLatlngList(List<JLLocation> locations) {
        List<LatLng> latlngs = new ArrayList<>();
        for (JLLocation location : locations) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            latlngs.add(latLng);
        }
        return latlngs;

    }

}
