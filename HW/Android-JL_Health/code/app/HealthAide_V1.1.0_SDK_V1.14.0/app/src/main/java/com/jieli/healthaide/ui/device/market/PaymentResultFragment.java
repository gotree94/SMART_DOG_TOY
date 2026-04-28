package com.jieli.healthaide.ui.device.market;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentPaymentResultBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.healthaide.util.HealthUtil;

/**
 * 付款结果界面
 */
public class PaymentResultFragment extends BaseFragment {
    private FragmentPaymentResultBinding mBinding;

    private WatchInfo watchInfo;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentPaymentResultBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (null == getArguments()) {
            requireActivity().finish();
            return;
        }
        watchInfo = getArguments().getParcelable(HealthConstant.EXTRA_WATCH_INFO);
        if (null == watchInfo || null == watchInfo.getServerFile()) {
            requireActivity().finish();
            return;
        }
        initUI();
    }

    private void initUI() {
        mBinding.viewPayResultTopbar.tvTopbarTitle.setText(watchInfo.getName());
        mBinding.viewPayResultTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().finish());

        int price = watchInfo.getServerFile().getPrice();
        String priceText = price == 0 ? getString(R.string.free_dial)
                : CalendarUtil.formatString("¥ %s", HealthUtil.getPriceFormat(price));
        mBinding.tvProductPrice.setText(priceText);

        mBinding.tvUserProduct.setOnClickListener(v -> toDialDetailFragment());
    }

    private void toDialDetailFragment() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(HealthConstant.EXTRA_WATCH_INFO, watchInfo);
        bundle.putBoolean(DialDetailFragment.EXTRA_AUTO_EXECUTE, true);
        ContentActivity.startContentActivity(requireContext(), DialDetailFragment.class.getCanonicalName(), bundle);
        requireActivity().finish();
    }
}