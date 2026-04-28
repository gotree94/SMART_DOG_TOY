package com.jieli.healthaide.ui.sports.ui;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.jieli.component.utils.PreferencesHelper;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentRunningMapBinding;
import com.jieli.healthaide.tool.unit.BaseUnitConverter;
import com.jieli.healthaide.tool.unit.Converter;
import com.jieli.healthaide.tool.unit.KMUnitConverter;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.sports.map.MapUtil;
import com.jieli.healthaide.ui.sports.model.LocationRealData;
import com.jieli.healthaide.ui.sports.model.RunningRealData;
import com.jieli.healthaide.ui.sports.viewmodel.SportsViewModel;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.FormatUtil;
import com.jieli.healthaide.util.MultiLanguageUtils;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.Locale;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/6/21
 * @desc :
 */
public class RunningWithMapFragment extends BaseFragment implements AMap.OnMyLocationChangeListener {
    private FragmentRunningMapBinding mBinding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentRunningMapBinding.inflate(inflater, container, false);
        mBinding.ivBack.setOnClickListener(v -> {
            assert getParentFragment() != null;
            ((BaseFragment) (getParentFragment())).replaceFragment(R.id.fl_fragment_content, RunningInfoFragment.class.getCanonicalName(), null);
        });
        mBinding.ivCurrentPosition.setOnClickListener(v -> MapUtil.commonSingleMapStyle(mBinding.outdoorMap.getMap()));
        return mBinding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SportsViewModel mViewModel = new ViewModelProvider(requireActivity(), new SportsViewModel.ViewModelFactory(requireActivity().getApplication(), 0)).get(SportsViewModel.class);
        mBinding.outdoorMap.onCreate(savedInstanceState);
        AMap aMap = mBinding.outdoorMap.getMap();
        String setLanguage = PreferencesHelper.getSharedPreferences(requireContext()).getString(MultiLanguageUtils.SP_LANGUAGE, MultiLanguageUtils.LANGUAGE_AUTO);
        JL_Log.d(tag, "onActivityCreated", "setLanguage >>  " + setLanguage);
        if (MultiLanguageUtils.LANGUAGE_ZH.equals(setLanguage)) {
            aMap.setMapLanguage(AMap.CHINESE);
        } else if (MultiLanguageUtils.LANGUAGE_AUTO.equals(setLanguage)) {
            Locale locale = MultiLanguageUtils.getSystemLanguage().get(0);
            String language = null == locale ? "" : locale.getLanguage();
            if (Locale.CHINA.getLanguage().equals(language)) {
                aMap.setMapLanguage(AMap.CHINESE);
            } else {
                aMap.setMapLanguage(AMap.ENGLISH);
            }
        } else {
            aMap.setMapLanguage(AMap.ENGLISH);
        }
        MapUtil.commonFollowMapStyle(mBinding.outdoorMap.getMap());
        mBinding.outdoorMap.getMap().setOnMyLocationChangeListener(this);
        LiveData<RunningRealData> runningRealData = mViewModel.getRealDataLiveData();

        runningRealData.observe(getViewLifecycleOwner(), runningRecord -> {
            Converter unitConverter = new KMUnitConverter().getConverter(BaseUnitConverter.getType());
            mBinding.tvDistance.setText(CalendarUtil.formatString("%.2f", unitConverter.value(runningRecord.distance / 1000f)));
            mBinding.tvDistanceUint.setText(unitConverter.unit());


            mBinding.tvSpeed.setText(runningRecord.pace > 0 ? FormatUtil.paceFormat((long) runningRecord.pace) : "--");
            mBinding.tvKcal.setText(CalendarUtil.formatString("%.2f", runningRecord.kcal));
            mBinding.tvTime.setText(CalendarUtil.formatSeconds(runningRecord.duration));

        });

        LiveData<LocationRealData> locationRealDataLiveData = mViewModel.getLocationRealDataData();
        locationRealDataLiveData.observe(getViewLifecycleOwner(), new Observer<LocationRealData>() {
            private AMapLocation lastLocation;

            @Override
            public void onChanged(LocationRealData locationRealData) {
                AMapLocation aMapLocation = locationRealData.aMapLocation;
                if (lastLocation == null) {
                    lastLocation = aMapLocation;
                    return;
                }
                AMap aMap = mBinding.outdoorMap.getMap();
                LatLng latLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                LatLng lastLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                aMap.addPolyline(new PolylineOptions().add(lastLatLng, latLng).width(10).color(Color.RED));
                aMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mBinding.outdoorMap.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        mBinding.outdoorMap.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
        mBinding.outdoorMap.onResume();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.outdoorMap.onDestroy();

    }

    @Override
    public void onMyLocationChange(Location location) {
        if (location != null) {
            JL_Log.d(tag, "onMyLocationChange", "定位成功， lat: " + location.getLatitude() + " lon: " + location.getLongitude());
            Bundle bundle = location.getExtras();
            if (bundle != null) {
                int errorCode = bundle.getInt(MyLocationStyle.ERROR_CODE);
                String errorInfo = bundle.getString(MyLocationStyle.ERROR_INFO);
                // 定位类型，可能为GPS WIFI等，具体可以参考官网的定位SDK介绍
                int locationType = bundle.getInt(MyLocationStyle.LOCATION_TYPE);
                JL_Log.d(tag, "onMyLocationChange", "定位信息， code: " + errorCode + " errorInfo: " + errorInfo + " locationType: " + locationType);
            } else {
                JL_Log.e(tag, "onMyLocationChange", "定位信息， bundle is null ");
            }
        } else {
            JL_Log.e(tag, "onMyLocationChange", "定位失败");
        }
    }
}
