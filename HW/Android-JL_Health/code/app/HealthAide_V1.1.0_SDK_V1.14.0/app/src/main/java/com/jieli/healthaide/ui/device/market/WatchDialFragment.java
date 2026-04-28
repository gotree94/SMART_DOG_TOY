package com.jieli.healthaide.ui.device.market;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentWatchDialBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.util.HealthConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * 手表表盘界面
 */
public class WatchDialFragment extends BaseFragment {
    private FragmentWatchDialBinding mBinding;
    private DialShopViewModel mViewModel;

    private RemovePaymentReceiver mReceiver;

    public static final int REQUEST_CODE_DIAL_OP = 0x6666;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentWatchDialBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this, new DialShopViewModel.DialShopViewModelFactory(requireContext())).get(DialShopViewModel.class);
        registerPaymentReceiver();
        initUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterPaymentReceiver();
        mBinding.vp2DialContainer.unregisterOnPageChangeCallback(mOnPageChangeCallback);
        mViewModel.release();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DIAL_OP) {
            mViewModel.listWatchList();
        }
    }

    private void initUI() {
        //update top bar
        mBinding.clDialTopbar.tvTopbarTitle.setText(getString(R.string.watch_dial));
        mBinding.clDialTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().finish());
        mBinding.clDialTopbar.tvTopbarRight.setVisibility(View.VISIBLE);
        mBinding.clDialTopbar.tvTopbarRight.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_pay_record_black, 0);
        mBinding.clDialTopbar.tvTopbarRight.setOnClickListener(v -> toDialListFragment(HealthConstant.DIAL_TYPE_RECORD));
        //update header
        mBinding.tbDialHeader.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mBinding.vp2DialContainer.setCurrentItem(tab.getPosition(), false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        //update vp2 container
        mBinding.vp2DialContainer.setUserInputEnabled(false);
        mBinding.vp2DialContainer.setAdapter(new CustomFragmentStateAdapter(WatchDialFragment.this, mViewModel));
        if (null != mBinding.vp2DialContainer.getAdapter()) {
            mBinding.vp2DialContainer.setOffscreenPageLimit(mBinding.vp2DialContainer.getAdapter().getItemCount());
        }
        mBinding.vp2DialContainer.registerOnPageChangeCallback(mOnPageChangeCallback);
    }

    private final ViewPager2.OnPageChangeCallback mOnPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
//            mBinding.tbDialHeader.setScrollPosition(position, positionOffset, positionOffset > 0.7);
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            mBinding.tbDialHeader.selectTab(mBinding.tbDialHeader.getTabAt(position));
        }
    };

    private void toDialListFragment(int dialType) {
        Bundle bundle = new Bundle();
        bundle.putInt(DialListFragment.EXTRA_DIAL_TYPE, dialType);
        ContentActivity.startContentActivityForResult(WatchDialFragment.this, DialListFragment.class.getCanonicalName(), bundle, REQUEST_CODE_DIAL_OP);
    }

    @SuppressLint("WrongConstant")
    private void registerPaymentReceiver() {
        if (null == mReceiver) {
            mReceiver = new RemovePaymentReceiver(mViewModel);
            final IntentFilter intentFilter = new IntentFilter(RemovePaymentReceiver.ACTION_REMOVE_PAYMENT);
            ContextCompat.registerReceiver(requireContext(), mReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED);
        }
    }

    private void unregisterPaymentReceiver() {
        if (null != mReceiver) {
            requireContext().unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    private static class CustomFragmentStateAdapter extends FragmentStateAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();

        public CustomFragmentStateAdapter(@NonNull Fragment fragment, @NonNull DialShopViewModel viewModel) {
            super(fragment);
            fragmentList.add(DialShopFragment.newInstance(viewModel));
            fragmentList.add(MyDialsFragment.newInstance(viewModel));
            fragmentList.add(CustomDialFragment.newInstance(filePath -> viewModel.listWatchList()));

        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getItemCount() {
            return fragmentList.size();
        }
    }

    public final static class RemovePaymentReceiver extends BroadcastReceiver {
        public static final String ACTION_REMOVE_PAYMENT = "com.jieli.healthaide.watch.remove_payment";

        private final DialShopViewModel mViewModel;

        public RemovePaymentReceiver(DialShopViewModel viewModel) {
            mViewModel = viewModel;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent) return;
            String action = intent.getAction();
            if (null == action) return;
            if (ACTION_REMOVE_PAYMENT.equals(action)) {
                mViewModel.cleanCache();
                mViewModel.listWatchList();
            }
        }
    }
}