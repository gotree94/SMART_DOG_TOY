package com.jieli.healthaide.ui.sports.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.jieli.component.thread.ThreadManager;
import com.jieli.component.utils.PreferencesHelper;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.db.HealthDataDbHelper;
import com.jieli.healthaide.data.entity.LocationEntity;
import com.jieli.healthaide.util.MultiLanguageUtils;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/8/21
 * @desc :
 */
public class RunningDetailWithMapFragment extends RunningDetailFragment {


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.viewTopbar.tvTopbarTitle.setText(R.string.sport_outdoor_running);
        mBinding.mapRunningDetail.setVisibility(View.VISIBLE);
        mBinding.mapRunningDetail.onCreate(savedInstanceState);
        AMap aMap = mBinding.mapRunningDetail.getMap();
        String setLanguage = PreferencesHelper.getSharedPreferences(requireContext()).getString(MultiLanguageUtils.SP_LANGUAGE, MultiLanguageUtils.LANGUAGE_AUTO);
        JL_Log.d(tag, "onActivityCreated", "setLanguage >>  " + setLanguage);
        if (MultiLanguageUtils.LANGUAGE_EN.equals(setLanguage)) {
            aMap.setMapLanguage(AMap.ENGLISH);
        } else if (MultiLanguageUtils.LANGUAGE_ZH.equals(setLanguage)) {
            aMap.setMapLanguage(AMap.CHINESE);
        } else {
            aMap.setMapLanguage(AMap.CUSTOM);
        }
        aMap.getUiSettings().setZoomControlsEnabled(false);
        ThreadManager.getInstance().postRunnable(this::queryAndParse);
    }

    @Override
    public void onResume() {
        super.onResume();
        mBinding.mapRunningDetail.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mBinding.mapRunningDetail.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.mapRunningDetail.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mBinding.mapRunningDetail.onSaveInstanceState(outState);
    }


    private void queryAndParse() {
        String uid = HealthApplication.getAppViewModel().getUid();
        long startTime = requireArguments().getLong(KEY_RECORD_START_TIME);
        LocationEntity locationEntity = HealthDataDbHelper.getInstance().getLocationDao().findByStartTime(uid, startTime);
        if (locationEntity == null) {
            JL_Log.d(tag, "queryAndParse", "该运动记录没有位置信息--->" + startTime);
            return;
        }
        List<List<LatLng>> latLngs = locationEntity.toTrackData();
        mBinding.getRoot().post(() -> initMap(latLngs));
    }

    private void initMap(List<List<LatLng>> latLngs) {


//        List<List<LatLng>> latLngs = createTestData();


        if (latLngs == null || latLngs.isEmpty() || latLngs.get(0).isEmpty()) {
            JL_Log.w(tag, "initMap", "没有位置数据");
            return;
        }
        if (latLngs.get(0).isEmpty()) {
            JL_Log.w(tag, "initMap", "位置数据未空");
            return;
        }

        LatLng startLatlng = latLngs.get(0).get(0); //这里 崩
        List<LatLng> lastLatLngList = latLngs.get(latLngs.size() - 1);
        if (lastLatLngList == null || lastLatLngList.isEmpty()) {
            JL_Log.w(tag, "initMap", "没有找到未位置");
            return;
        }
        LatLng endLatLng = lastLatLngList.get(lastLatLngList.size() - 1);

        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (List<LatLng> list : latLngs) {
            PolylineOptions options = new PolylineOptions()
                    .addAll(list)
                    .width(10)
                    .color(getResources().getColor(R.color.main_color));
            mBinding.mapRunningDetail.getMap().addPolyline(options);
            for (LatLng latLng : list) {
                builder.include(latLng);
            }
        }
        mBinding.mapRunningDetail.getMap().addMarker(new MarkerOptions()
                .position(startLatlng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.run_icon_origin)));
        mBinding.mapRunningDetail.getMap().addMarker(new MarkerOptions()
                .position(endLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.run_icon_finish)));
//        mBinding.mapRunningDetail.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(startLatlng, 18f));

        mBinding.mapRunningDetail.getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));
        mBinding.mapRunningDetail.getMap().getUiSettings().setZoomControlsEnabled(false);

    }

//        mBinding.mapRunningDetail.getMap().moveCamera(CameraUpdateFactory.newLatLng(startLatlng));


//    private List<List<LatLng>> createTestData() {
//        List<List<LatLng>> list = new ArrayList<>();
//        double lat = 22.234512;
//        for (int i = 0; i < 3; i++) {
//            List<LatLng> data = new ArrayList<>();
//            for (int j = 0; j < 100; j++) {
//                LatLng latLng = new LatLng(lat += 0.0001, 113.23 + Math.random() / 1000 * (Math.random() > 0.4 ? 1 : -1));
//                data.add(latLng);
//            }
//            lat += 0.002;
//            list.add(data);
//        }
//        return list;
//    }
}
