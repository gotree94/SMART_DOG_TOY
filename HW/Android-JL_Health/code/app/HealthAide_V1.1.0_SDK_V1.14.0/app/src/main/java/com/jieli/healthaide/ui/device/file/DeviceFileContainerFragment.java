package com.jieli.healthaide.ui.device.file;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentDeviceFileContainerBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.music.MusicManagerFragment;
import com.jieli.healthaide.ui.dialog.PermissionDialog;
import com.jieli.jl_filebrowse.FileBrowseManager;
import com.jieli.jl_filebrowse.bean.SDCardBean;

import java.util.List;
import java.util.Objects;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * 设备文件列表界面
 */
@RuntimePermissions
public class DeviceFileContainerFragment extends BaseFragment implements TabLayout.OnTabSelectedListener {
    public static final String KEY_TYPE = "type";
    public static final String KEY_DEVICE_INDEX = "index";

    private DeviceFileViewModel mViewModel;
    private FragmentDeviceFileContainerBinding binding;
    private boolean isUserNeverAskAgain = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDeviceFileContainerBinding.inflate(inflater, container, false);
        binding.vp2Device.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return (Fragment) Objects.requireNonNull(Objects.requireNonNull(binding.tlDevice.getTabAt(position)).getTag());
            }

            @Override
            public int getItemCount() {
                return binding.tlDevice.getTabCount();
            }
        });
        binding.vp2Device.registerOnPageChangeCallback(pageChangeCallback);
        binding.tlDevice.addOnTabSelectedListener(this);
        binding.ibDeviceBack.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.btnMusicManager.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                showPermissionDialog(Manifest.permission.READ_MEDIA_AUDIO, (permission ->
                        DeviceFileContainerFragmentPermissionsDispatcher.toMusicManagerFragmentBy33WithPermissionCheck(DeviceFileContainerFragment.this)));
                return;
            }
            showPermissionDialog(Manifest.permission.READ_EXTERNAL_STORAGE, (permission ->
                    DeviceFileContainerFragmentPermissionsDispatcher.toMusicManagerFragmentWithPermissionCheck(DeviceFileContainerFragment.this)));
        });
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider((requireActivity())).get(DeviceFileViewModel.class);
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
                requireActivity().finish();
            }
        });
        mViewModel.SDCardsMutableLiveData.observe(getViewLifecycleOwner(), sdCardBeans -> {
            if (getArguments() != null && getArguments().getInt(KEY_TYPE, -1) == -1) {
                throw new RuntimeException("没有传入设备类型");
            }
            refreshView(getArguments().getInt(KEY_TYPE, 0));
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DeviceFileContainerFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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


    @SuppressLint("NotifyDataSetChanged")
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


    @SuppressLint("UseCompatLoadingForColorStateLists")
    private TabLayout.Tab createTab(SDCardBean bean) {
        TabLayout.Tab tab = binding.tlDevice.newTab();
        FilesFragment filesFragment = FilesFragment.newInstance(bean);
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

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @NeedsPermission({
            Manifest.permission.READ_MEDIA_AUDIO
    })
    public void toMusicManagerFragmentBy33() {
        ContentActivity.startContentActivity(requireContext(), MusicManagerFragment.class.getCanonicalName());
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @OnShowRationale({
            Manifest.permission.READ_MEDIA_AUDIO
    })
    public void showRelationForExternalStoragePermissionBy33(PermissionRequest request) {
        showExternalStorageDialog(Manifest.permission.READ_MEDIA_AUDIO, request);
        isUserNeverAskAgain = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @OnNeverAskAgain({
            Manifest.permission.READ_MEDIA_AUDIO
    })
    public void onExternalStorageNeverAskAgainBy33() {
        if (isUserNeverAskAgain) {
            isUserNeverAskAgain = false;
        } else {
            showExternalStorageDialog(Manifest.permission.READ_MEDIA_AUDIO, null);
        }
    }

    @NeedsPermission({
            Manifest.permission.READ_EXTERNAL_STORAGE
    })
    public void toMusicManagerFragment() {
        ContentActivity.startContentActivity(requireContext(), MusicManagerFragment.class.getCanonicalName());
    }

    @OnShowRationale({
            Manifest.permission.READ_EXTERNAL_STORAGE
    })
    public void showRelationForExternalStoragePermission(PermissionRequest request) {
        showExternalStorageDialog(Manifest.permission.READ_EXTERNAL_STORAGE, request);
        isUserNeverAskAgain = true;
    }

    @OnNeverAskAgain({
            Manifest.permission.READ_EXTERNAL_STORAGE
    })
    public void onExternalStorageNeverAskAgain() {
        if (isUserNeverAskAgain) {
            isUserNeverAskAgain = false;
        } else {
            showExternalStorageDialog(Manifest.permission.READ_EXTERNAL_STORAGE, null);
        }
    }

    private void showExternalStorageDialog(String permission, PermissionRequest request) {
        PermissionDialog permissionDialog = new PermissionDialog(permission, request);
        permissionDialog.setCancelable(true);
        permissionDialog.show(getChildFragmentManager(), PermissionDialog.class.getCanonicalName());
    }

}
