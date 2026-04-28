package com.jieli.healthaide.ui.sports.ui;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.jieli.component.utils.PreferencesHelper;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentHomeOutdoorRunningBinding;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.dialog.ChooseSexDialog;
import com.jieli.healthaide.ui.dialog.RequireGPSDialog;
import com.jieli.healthaide.ui.sports.map.MapUtil;
import com.jieli.healthaide.util.MultiLanguageUtils;
import com.jieli.healthaide.util.PermissionUtil;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.Locale;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/2/21
 * @desc :
 */
@RuntimePermissions
public class HomeOutdoorRunningFragment extends BaseFragment implements AMap.OnMyLocationChangeListener, LocationSource, AMapLocationListener {

    private FragmentHomeOutdoorRunningBinding mBinding;
    private boolean isUserNeverAskAgain = false;
    private boolean isNeedMoveCamera = true;

    private AMapLocationClient mLocationClient;
    private OnLocationChangedListener mListener;

    private final static int MAP_DISPLAY_ZOOM_LEVEL = 17;


    public static HomeOutdoorRunningFragment newInstance() {
        return new HomeOutdoorRunningFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentHomeOutdoorRunningBinding.inflate(inflater, container, false);
        mBinding.btnStartOutdoorSport.setOnClickListener(v -> {
            WatchManager watchManager = WatchManager.getInstance();
            if (!watchManager.isWatchSystemOk()) {
                showTips(R.string.bt_disconnect_tips);
//                ContentActivity.startContentActivity(requireActivity(),RunningDetailWithMapFragment.class.getCanonicalName());
                return;
            }
            WatchConfigure configure = watchManager.getWatchConfigure(watchManager.getConnectedDevice());
            if (configure != null && configure.getSportHealthConfigure() != null
                    && (configure.getSportHealthConfigure().getSportModeFunc() == null
                    || !configure.getSportHealthConfigure().getSportModeFunc().isSupportOutDoor())) {
                showTips(getString(R.string.device_not_support));
                return;
            }
            if (!PermissionUtil.checkGpsProviderEnable(getContext())) {
                showOpenGPSDialog();
            } else {
                showPermissionDialog(Manifest.permission.ACCESS_COARSE_LOCATION, (permission ->
                        HomeOutdoorRunningFragmentPermissionsDispatcher.startRunWithPermissionCheck(this)));
            }
        });
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.mapHomeSport.onCreate(savedInstanceState);

        AMap aMap = mBinding.mapHomeSport.getMap();
        String setLanguage = PreferencesHelper.getSharedPreferences(requireContext()).getString(MultiLanguageUtils.SP_LANGUAGE, MultiLanguageUtils.LANGUAGE_AUTO);
        JL_Log.d(tag, "onActivityCreated", "setLanguage >>  " + setLanguage);
        if (MultiLanguageUtils.LANGUAGE_ZH.equals(setLanguage)) {
            aMap.setMapLanguage(AMap.CHINESE);
        } else if (MultiLanguageUtils.LANGUAGE_AUTO.equals(setLanguage)) {
            if (Locale.CHINA.getLanguage().equals(MultiLanguageUtils.getSystemLanguage().get(0).getLanguage())) {
                aMap.setMapLanguage(AMap.CHINESE);
            } else {
                aMap.setMapLanguage(AMap.ENGLISH);
            }
        } else {
            aMap.setMapLanguage(AMap.ENGLISH);
        }
        aMap.setLocationSource(this);
        MapUtil.commonSingleMapStyle(aMap);
        aMap.getUiSettings().setAllGesturesEnabled(false);//禁止手势操作
        aMap.setOnMyLocationChangeListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        HomeOutdoorRunningFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


    @Override
    public void onResume() {
        super.onResume();
        mBinding.mapHomeSport.onResume();
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.startLocation();//启动定位
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mBinding.mapHomeSport.onPause();

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mBinding.mapHomeSport.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.mapHomeSport.onDestroy();
        if (null != mLocationClient) {
            mLocationClient.onDestroy();
        }
    }

    @Override
    public void onMyLocationChange(Location location) {
        if (location != null) {
            JL_Log.d(tag, "onMyLocationChange", "定位成功， lat: " + location.getLatitude() + " lon: " + location.getLongitude() + "\t" + location.getClass().getCanonicalName());
            Bundle bundle = location.getExtras();
            if (bundle != null) {
                int errorCode = bundle.getInt(MyLocationStyle.ERROR_CODE);
                String errorInfo = bundle.getString(MyLocationStyle.ERROR_INFO);
                // 定位类型，可能为GPS WIFI等，具体可以参考官网的定位SDK介绍
                int locationType = bundle.getInt(MyLocationStyle.LOCATION_TYPE);
                JL_Log.d(tag, "onMyLocationChange", "定位信息， code: " + errorCode + " errorInfo: " + errorInfo + " locationType: " + locationType + ", isNeedMoveCamera = " + isNeedMoveCamera);
                if (isNeedMoveCamera) {
                    isNeedMoveCamera = false;
                    String setLanguage = PreferencesHelper.getSharedPreferences(requireContext()).getString(MultiLanguageUtils.SP_LANGUAGE, MultiLanguageUtils.LANGUAGE_AUTO);
                    int zoom = MultiLanguageUtils.LANGUAGE_EN.equals(setLanguage) ? MAP_DISPLAY_ZOOM_LEVEL - 1 : MAP_DISPLAY_ZOOM_LEVEL;
                    mBinding.mapHomeSport.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoom));
                }
            } else {
                JL_Log.w(tag, "onMyLocationChange", "定位信息， bundle is null ");
            }

        } else {
            JL_Log.w(tag, "onMyLocationChange", "定位失败");
        }
    }

    @NeedsPermission({
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    })
    public void startRun() {
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.startLocation();//启动定位
        }
        SportsCountdownFragment.startByOutdoorRunning(requireContext());
    }

    @OnShowRationale({
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    })
    public void showRelationForLocationPermission(PermissionRequest request) {
        showRequireGPSPermissionDialog(request);
        isUserNeverAskAgain = true;
    }

    @OnNeverAskAgain({
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    })
    public void onLocationNeverAskAgain() {
        if (isUserNeverAskAgain) {
            isUserNeverAskAgain = false;
        } else {
            showRequireGPSPermissionDialog(null);
        }
    }


    private void showOpenGPSDialog() {
        showGPSDialog(null, true);
    }

    private void showRequireGPSPermissionDialog(PermissionRequest request) {
        showGPSDialog(request, false);
    }

    private void showGPSDialog(PermissionRequest request, boolean isLocationService) {
        RequireGPSDialog requireGPSDialog = new RequireGPSDialog(RequireGPSDialog.VIEW_TYPE_SPORT, request);
        requireGPSDialog.setLocationService(isLocationService);
        requireGPSDialog.setCancelable(true);
        requireGPSDialog.setOnGPSChooseListener(() -> {
            requireGPSDialog.dismiss();
            SportsCountdownFragment.startByOutdoorRunning(requireContext());
        });
        requireGPSDialog.show(getChildFragmentManager(), ChooseSexDialog.class.getCanonicalName());
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient == null) {
            //初始化定位
            try {
                mLocationClient = new AMapLocationClient(requireContext());
                //初始化定位参数
                AMapLocationClientOption option = new AMapLocationClientOption();
                option.setOnceLocation(true);
                option.setHttpTimeOut(20000);
                //设置定位回调监听
                mLocationClient.setLocationListener(this);
                //设置为高精度定位模式
                option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                //设置定位参数
                mLocationClient.setLocationOption(option);
                mLocationClient.stopLocation();
                mLocationClient.startLocation();//启动定位
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation.getErrorCode() == AMapLocation.LOCATION_SUCCESS) {
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
                JL_Log.e(tag, "onLocationChanged", "AmapErr : " + errText);
            }
        }
    }
}
