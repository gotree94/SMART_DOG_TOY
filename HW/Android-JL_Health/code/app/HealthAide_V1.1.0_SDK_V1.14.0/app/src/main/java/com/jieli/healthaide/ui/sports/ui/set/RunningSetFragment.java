package com.jieli.healthaide.ui.sports.ui.set;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentRunningSetBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;

import java.util.Objects;

/**
 * @ClassName: RunningSetFragment
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/11/4 16:03
 */
public class RunningSetFragment extends BaseFragment {

    public RunningSetFragment() {
        // Required empty public constructor
    }


    public static RunningSetFragment newInstance() {
        return new RunningSetFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentRunningSetBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_running_set, container, false);
        binding.layoutTopbar.tvTopbarTitle.setText(R.string.running_set);
        binding.layoutTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().finish());
        binding.layoutSportPermission.tvSettingTarget2.setText(R.string.permission_sport);
        binding.layoutSportPermission.getRoot().setOnClickListener(v -> ContentActivity.startContentActivity(requireContext(), SportPermissionFragment.class.getCanonicalName()));
        if (Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")) {
            binding.clLockScreenDisplay.setVisibility(View.VISIBLE);
            binding.clLockScreenDisplay.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ComponentName componentName = ComponentName.unflattenFromString("com.miui.securitycenter/com.miui.appmanager.ApplicationsDetailsActivity");
                intent.setComponent(componentName);
                intent.putExtra("package_name", Objects.requireNonNull(requireContext()).getPackageName());
                intent.putExtra("package_label", Objects.requireNonNull(requireContext()).getString(R.string.app_name));
                Objects.requireNonNull(requireContext()).startActivity(intent);
            });
        }
        return binding.getRoot();
    }
}
