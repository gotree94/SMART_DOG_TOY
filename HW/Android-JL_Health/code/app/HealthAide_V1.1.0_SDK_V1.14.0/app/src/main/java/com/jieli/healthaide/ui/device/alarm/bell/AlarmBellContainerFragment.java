package com.jieli.healthaide.ui.device.alarm.bell;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentAlarmBellContainerBinding;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.alarm.AlarmSettingFragment;
import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.jl_rcsp.model.device.AlarmBean;

import java.util.List;

/**
 * 闹钟铃声选择界面
 */
public class AlarmBellContainerFragment extends BaseFragment implements TabLayout.OnTabSelectedListener {
    public static final String KEY_BELL_INFO = "bell_info";
    private FragmentAlarmBellContainerBinding mBinding;
    private BellViewModel mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mBinding = FragmentAlarmBellContainerBinding.inflate(inflater, container, false);
        mBinding.viewTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        mBinding.viewTopbar.tvTopbarTitle.setText(R.string.bell);

        mBinding.vp2Device.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return (Fragment) mBinding.tlDevice.getTabAt(position).getTag();
            }

            @Override
            public int getItemCount() {
                return mBinding.tlDevice.getTabCount();
            }
        });
        mBinding.vp2Device.registerOnPageChangeCallback(pageChangeCallback);
        mBinding.tlDevice.addOnTabSelectedListener(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(BellViewModel.class);
        mViewModel.SDCardsMutableLiveData.observe(getViewLifecycleOwner(), this::refreshView);
        mViewModel.finishLiveData.observe(getViewLifecycleOwner(), finish -> {
                    if (finish) {
                        requireActivity().finish();
                    }
                }
        );
    }

    @Override
    public void onDestroyView() {
        mBinding.vp2Device.unregisterOnPageChangeCallback(pageChangeCallback);
        mBinding.tlDevice.removeOnTabSelectedListener(this);
        super.onDestroyView();
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mBinding.vp2Device.setCurrentItem(tab.getPosition(), false);
    }


    private void refreshView(List<SDCardBean> list) {
        mBinding.tlDevice.removeAllTabs();
        AlarmBean alarmBean = getAlarmBean();
        //默认铃声tab
        mBinding.tlDevice.addTab(createDefaultTab(), alarmBean == null || alarmBean.getBellType() == 0);
        //设备铃声tab
        for (SDCardBean sdCardBean : list) {
            if(sdCardBean == null) break;
            if (sdCardBean.getType() < 3) {
                boolean selected = alarmBean != null && alarmBean.getBellType() == 1
                        && alarmBean.getDevIndex() == sdCardBean.getIndex();
                mBinding.tlDevice.addTab(createFileTab(sdCardBean), selected);
            }
        }
        mBinding.vp2Device.setUserInputEnabled(mBinding.tlDevice.getTabCount() > 1);
        mBinding.tlDevice.setVisibility(mBinding.tlDevice.getTabCount() > 1 ? View.VISIBLE : View.GONE);
        if (mBinding.vp2Device.getAdapter() != null) {
            mBinding.vp2Device.getAdapter().notifyDataSetChanged();
        }

    }

    //设备类型铃声选择tab
    private TabLayout.Tab createFileTab(SDCardBean bean) {
        TabLayout.Tab tab = mBinding.tlDevice.newTab();
        int cluster = -1;
        int dev = -1;
        //设置选中文件
        AlarmBean alarmBean = getAlarmBean();
        if (alarmBean != null && alarmBean.getBellType() == 1 && alarmBean.getDevIndex() == bean.getIndex()) {
            cluster = alarmBean.getBellCluster();
            dev = alarmBean.getDevIndex();
        }
        FileBellFragment filesFragment = FileBellFragment.newInstance(bean, dev, cluster);
        tab.setTag(filesFragment);
        tab.setText(R.string.music);
        return tab;
    }


    private AlarmBean getAlarmBean() {
        if (getArguments() == null) {
            return null;
        }
        Bundle bundle = requireArguments();
        String text = bundle.getString(AlarmSettingFragment.KEY_ALARM_EDIT);
        return new Gson().fromJson(text, AlarmBean.class);
    }


    //默认类型铃声选择tab
    private TabLayout.Tab createDefaultTab() {
        TabLayout.Tab tab = mBinding.tlDevice.newTab();
        DefaultBellFragment bellFragment = new DefaultBellFragment();
        AlarmBean alarmBean = getAlarmBean();
        if (alarmBean != null && alarmBean.getBellType() == 0) {
            bellFragment.setInitIndex(alarmBean.getBellCluster());
        }
        tab.setTag(bellFragment);
        tab.setText(getString(R.string.alarm_default_bell));
        return tab;
    }


    private final ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            mBinding.tlDevice.setScrollPosition(position, positionOffset, positionOffset > 0.7);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            super.onPageScrollStateChanged(state);
            if (state == ViewPager2.SCROLL_STATE_IDLE) {
                mBinding.tlDevice.selectTab(mBinding.tlDevice.getTabAt(mBinding.vp2Device.getCurrentItem()));
            }
        }
    };


    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }


}
