package com.jieli.healthaide.ui.sports.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayout;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentSportsBinding;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.watch.WatchViewModel;
import com.jieli.jl_rcsp.model.WatchConfigure;

/**
 * 运动界面
 */
public class SportsFragment extends BaseFragment {
    private WatchViewModel mViewModel;
    private FragmentSportsBinding mSportsBinding;

    private final static boolean USE_VIEW_PAGER = false;

    public static SportsFragment newInstance() {
        return new SportsFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSportsBinding = FragmentSportsBinding.inflate(inflater, container, false);
        return mSportsBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(WatchViewModel.class);
        initUI();
        observeCallback();
        updateUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSportsBinding = null;
    }


    private void initUI() {
        /*mSportsBinding.vp2Sport.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                TabLayout.Tab tab = null;
                if (position >= 0 && position < mSportsBinding.tlSport.getTabCount()) {
                    tab = mSportsBinding.tlSport.getTabAt(position);
                }
                if (null == tab || !(tab.getTag() instanceof Fragment)) {
                    return HomeOutdoorRunningFragment.newInstance();
                }
                return (Fragment) tab.getTag();
            }

            @Override
            public int getItemCount() {
                return mSportsBinding.tlSport.getTabCount();
            }
        });
        mSportsBinding.vp2Sport.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
//                mSportsBinding.tlSport.setScrollPosition(position, positionOffset, positionOffset > 0.7);
            }

            @Override
            public void onPageSelected(int position) {
                mSportsBinding.tlSport.selectTab(mSportsBinding.tlSport.getTabAt(position));
            }
        });*/
        mSportsBinding.tlSport.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
//                mSportsBinding.vp2Sport.setCurrentItem(tab.getPosition());
                replaceFragment(R.id.fl_sport_container, (String) tab.getTag(), null);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void observeCallback() {
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> requireActivity().runOnUiThread(this::updateUI));
        mViewModel.mDeviceConfigureMLD.observe(getViewLifecycleOwner(), device -> updateUI());
    }

    private TabLayout.Tab createTabItem(String title, String classPath) {
        TabLayout.Tab tabItem = mSportsBinding.tlSport.newTab();
        tabItem.setText(title);
        tabItem.setTag(classPath);
        return tabItem;
    }

    private TabLayout.Tab createTabItem(String title, Fragment fragment) {
        TabLayout.Tab tabItem = mSportsBinding.tlSport.newTab();
        tabItem.setText(title);
        tabItem.setTag(fragment);
        return tabItem;
    }

    private void updateUI() {
        mSportsBinding.tlSport.removeAllTabs();
        if (mViewModel.isWatchSystemInit(mViewModel.getConnectedDevice())) {
            WatchConfigure configure = mViewModel.getWatchConfigure(mViewModel.getConnectedDevice());
            boolean isSupportOutdoor = configure == null || (configure.getSportHealthConfigure() != null
                    && configure.getSportHealthConfigure().getSportModeFunc() != null
                    && configure.getSportHealthConfigure().getSportModeFunc().isSupportOutDoor());
            boolean isSupportIndoor = configure == null || (configure.getSportHealthConfigure() != null
                    && configure.getSportHealthConfigure().getSportModeFunc() != null
                    && configure.getSportHealthConfigure().getSportModeFunc().isSupportInDoor());
            if (isSupportOutdoor || !isSupportIndoor) {
                if (USE_VIEW_PAGER) {
                    mSportsBinding.tlSport.addTab(createTabItem(getString(R.string.sport_outdoor_running), HomeOutdoorRunningFragment.newInstance()));
                } else {
                    mSportsBinding.tlSport.addTab(createTabItem(getString(R.string.sport_outdoor_running), HomeOutdoorRunningFragment.class.getCanonicalName()));
                }
            }
            if (isSupportIndoor) {
                if (USE_VIEW_PAGER) {
                    mSportsBinding.tlSport.addTab(createTabItem(getString(R.string.sport_indoor_running), HomeIndoorRunningFragment.newInstance()));
                } else {
                    mSportsBinding.tlSport.addTab(createTabItem(getString(R.string.sport_indoor_running), HomeIndoorRunningFragment.class.getCanonicalName()));
                }
            }
            return;
        }
        if (USE_VIEW_PAGER) {
            mSportsBinding.tlSport.addTab(createTabItem(getString(R.string.sport_outdoor_running), HomeOutdoorRunningFragment.newInstance()));
            mSportsBinding.tlSport.addTab(createTabItem(getString(R.string.sport_indoor_running), HomeIndoorRunningFragment.newInstance()));
            return;
        }
        mSportsBinding.tlSport.addTab(createTabItem(getString(R.string.sport_outdoor_running), HomeOutdoorRunningFragment.class.getCanonicalName()));
        mSportsBinding.tlSport.addTab(createTabItem(getString(R.string.sport_indoor_running), HomeIndoorRunningFragment.class.getCanonicalName()));

    }
}