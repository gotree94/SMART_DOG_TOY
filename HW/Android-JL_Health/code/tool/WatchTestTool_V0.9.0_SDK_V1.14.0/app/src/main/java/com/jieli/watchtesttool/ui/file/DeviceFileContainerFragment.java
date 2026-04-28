package com.jieli.watchtesttool.ui.file;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.jieli.jl_filebrowse.FileBrowseManager;
import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.databinding.FragmentDeviceFileContainerBinding;
import com.jieli.watchtesttool.ui.base.BaseFragment;

import java.util.List;

/**
 * 设备文件列表界面
 */
public class DeviceFileContainerFragment extends BaseFragment implements TabLayout.OnTabSelectedListener {
    public static final String KEY_TYPE = "type";
    public static final String KEY_DEVICE_INDEX = "index";

    private DeviceFileViewModel mViewModel;
    private FragmentDeviceFileContainerBinding binding;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDeviceFileContainerBinding.inflate(inflater, container, false);
        binding.vp2Device.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return (Fragment) binding.tlDevice.getTabAt(position).getTag();
            }

            @Override
            public int getItemCount() {
                return binding.tlDevice.getTabCount();
            }
        });
        binding.vp2Device.registerOnPageChangeCallback(pageChangeCallback);
        binding.tlDevice.addOnTabSelectedListener(this);
        binding.ibDeviceBack.setOnClickListener(v -> requireActivity().onBackPressed());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider((requireActivity())).get(DeviceFileViewModel.class);
        mViewModel.SDCardsMutableLiveData.observe(getViewLifecycleOwner(), sdCardBeans -> {
            if (requireArguments().getInt(KEY_TYPE, -1) == -1) {
                throw new RuntimeException("没有传入设备类型");
            }
            refreshView(requireArguments().getInt(KEY_TYPE, 0));
        });
    }

    @Override
    public void onDestroyView() {
        binding.vp2Device.unregisterOnPageChangeCallback(pageChangeCallback);
        binding.tlDevice.removeOnTabSelectedListener(this);
        super.onDestroyView();
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        binding.vp2Device.setCurrentItem(tab.getPosition(), false);
    }


    private void refreshView(int type) {
        binding.tlDevice.removeAllTabs();
        //过滤type不同的设备
        int i = 0;
        List<SDCardBean> list = FileBrowseManager.getInstance().getOnlineDev();
        for (SDCardBean sdCardBean : list) {
            if (type == sdCardBean.getType()) {
                binding.tlDevice.addTab(createTab(sdCardBean), i++ == 0);
            }
        }
        //在线设备为空时退出activity
        if (binding.tlDevice.getTabCount() < 1) {
            requireActivity().finish();
        }
        binding.vp2Device.setUserInputEnabled(binding.tlDevice.getTabCount() > 1);
        binding.tlDevice.setSelectedTabIndicator(binding.tlDevice.getTabCount() > 1 ? R.drawable.tab_indicator_device_music : R.drawable.tab_indicator_device_music_none);
        if (binding.vp2Device.getAdapter() != null) {
            binding.vp2Device.getAdapter().notifyDataSetChanged();
        }
    }


    private TabLayout.Tab createTab(SDCardBean bean) {
        TabLayout.Tab tab = binding.tlDevice.newTab();
        FilesFragment filesFragment = new  FilesFragment();

        tab.setTag(filesFragment);
        TextView tvTab = new TextView(requireContext());
        ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        tvTab.setLayoutParams(mlp);
        tvTab.setGravity(Gravity.CENTER);
        tvTab.setTextColor(getResources().getColorStateList(R.color.color_device_file_tab));
        tvTab.setTextSize(18);
        tvTab.setText(bean.getName());
        tab.setCustomView(tvTab);
        return tab;
    }


    private final ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            binding.tlDevice.selectTab(binding.tlDevice.getTabAt(position));
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            binding.tlDevice.setScrollPosition(position, positionOffset, positionOffset > 0.7);
        }
    };


    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

}
