package com.jieli.healthaide.ui.device.market;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentDialPayBinding;
import com.jieli.healthaide.tool.watch.WatchServerCacheHelper;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.healthaide.ui.device.market.adapter.PayWayAdapter;
import com.jieli.healthaide.ui.device.market.bean.ALiPayResult;
import com.jieli.healthaide.ui.device.market.bean.PayWayEntity;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_dialog.Jl_Dialog;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 表盘付款界面
 */
public class DialPayFragment extends BaseFragment {
    private FragmentDialPayBinding mBinding;
    private DialPayViewModel mViewModel;
    private PayWayAdapter mAdapter;
    private WatchInfo watchInfo;

    private Jl_Dialog payTipsDialog;
    private Jl_Dialog payErrorDialog;
    private int checkPaymentResultCount;

    private static final int CHECK_COUNT = 5;
    private static final int DELAY_TIME = 300;

    private static final int MSG_CHECK_PAYMENT_RESULT = 0x9862;
    private final Handler mUIHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (MSG_CHECK_PAYMENT_RESULT == msg.what) {
                mViewModel.checkPaymentState(msg.arg1);
            }
            return true;
        }
    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentDialPayBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this, new DialPayViewModel.DialPayViewModelFactory(requireActivity())).get(DialPayViewModel.class);
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
        observeCallback();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        payTipsDialog = null;
        payErrorDialog = null;
    }

    private void initUI() {
        mBinding.viewDialPayTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().finish());
        mBinding.viewDialPayTopbar.tvTopbarTitle.setText(watchInfo.getName());
        mBinding.tvProductNameValue.setText(watchInfo.getName());
        int price = watchInfo.getServerFile().getPrice();
        String priceValue = CalendarUtil.formatString("¥ %s", HealthUtil.getPriceFormat(price));
        mBinding.tvProductPriceValue.setText(priceValue);
        mBinding.btnDialPay.setOnClickListener(v -> {
            int payWay = mAdapter.getPayWay();
            if (payWay < 0) {
                showTips(getString(R.string.pay_way_tips));
                return;
            }
            mViewModel.payment(payWay, watchInfo.getServerFile());
        });

        mBinding.rvDialPayWay.setLayoutManager(new LinearLayoutManager(requireContext()));
        mAdapter = new PayWayAdapter();
        List<PayWayEntity> list = new ArrayList<>();
        list.add(new PayWayEntity(PayWayEntity.PAY_WAY_ALI));
//        list.add(new PayWayEntity(PayWayEntity.PAY_WAY_WEIXIN));
        mAdapter.setNewInstance(list);
        mAdapter.setSelectedIndex(0);
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            mAdapter.setSelectedIndex(position);
            updateBtnUI();
        });
        mBinding.rvDialPayWay.setAdapter(mAdapter);

        updateBtnUI();
    }

    private void observeCallback() {
        mViewModel.payResultMLD.observe(getViewLifecycleOwner(), payResult -> requireActivity().runOnUiThread(() -> {
            boolean isPayOK = payResult.getCode() == 0; //支付成功
            if (isPayOK) { //支付成功
//                toPaymentSuccess();
                startCheckPayResult(payResult.getPayWay());
            } else { //支付失败
                if (payResult.getCode() == WatchServerCacheHelper.ERR_DIAL_PAYMENT) {
                    handlePaySuccess();
                    return;
                }
                if (payResult instanceof ALiPayResult) {
                    ALiPayResult aLiPayResult = (ALiPayResult) payResult;
                    if (TextUtils.equals(ALiPayResult.PAY_PROCESSING, aLiPayResult.getResultStatus())
                            || TextUtils.equals(ALiPayResult.PAY_UNKNOWN_STATUS, aLiPayResult.getResultStatus())) {
                        showTips(getString(R.string.payment_processing));
                        showPayTipsDialog(payResult.getPayWay(), getString(R.string.whether_payment_finish));
                    } else { //支付异常处理
                        String error;
                        if (TextUtils.equals(ALiPayResult.PAY_FAILED, aLiPayResult.getResultStatus())) {  //支付失败
                            error = getString(R.string.payment_error);
                        } else if (TextUtils.equals(ALiPayResult.PAY_DUPLEX, aLiPayResult.getResultStatus())) { //重复支付
                            error = getString(R.string.payment_duplex);
                        } else if (TextUtils.equals(ALiPayResult.PAY_USE_CANCEL, aLiPayResult.getResultStatus())) { //用户取消支付
                            error = getString(R.string.payment_user_cancel);
                        } else if (TextUtils.equals(ALiPayResult.PAY_NETWORK_EXCEPTION, aLiPayResult.getResultStatus())) { //网络问题
                            error = getString(R.string.payment_network);
                        } else {
                            error = getString(R.string.payment_unknown);
                        }
                        String message = CalendarUtil.formatString("%s\n%s", error, aLiPayResult.getResult());
                        showPayErrorDialog(message);
                    }
                    return;
                }
                String message = CalendarUtil.formatString("%s\n%s", getString(R.string.payment_unknown), payResult.getMessage());
                showPayErrorDialog(message);
//                showPayTipsDialog(payResult.getPayWay(), getString(R.string.whether_payment_finish));
            }
        }));
        mViewModel.payStateResultMLD.observe(getViewLifecycleOwner(), payStateResult -> {
            if (payStateResult.isOk() && payStateResult.getResult()) {
                handlePaySuccess();
            } else {
                if (checkPaymentResultCount < CHECK_COUNT) {
                    checkPaymentResultCount++;
                    mUIHandler.removeMessages(MSG_CHECK_PAYMENT_RESULT);
                    mUIHandler.sendMessageDelayed(mUIHandler.obtainMessage(MSG_CHECK_PAYMENT_RESULT, payStateResult.getPayWay(), 0), DELAY_TIME);
                    return;
                } else {
                    checkPaymentResultCount = 0;
                }
                showTips(getString(R.string.payment_failed));
            }
        });
    }

    private void updateBtnUI() {
        int payWay = mAdapter.getPayWay();
        if (payWay < 0) return;
        String value = CalendarUtil.formatString("%s %s", mAdapter.getPayWayName(requireContext(), payWay),
                mBinding.tvProductPriceValue.getText().toString());
        mBinding.btnDialPay.setText(value);
    }

    private void toPaymentSuccess() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(HealthConstant.EXTRA_WATCH_INFO, watchInfo);
        ContentActivity.startContentActivity(requireContext(), PaymentResultFragment.class.getCanonicalName(), bundle);
        requireActivity().finish();
    }

    private void handlePaySuccess() {
        checkPaymentResultCount = 0;
        //更新表盘状态
        watchInfo.getServerFile().setStatus(true);
        watchInfo.getServerFile().setPay(true);
        watchInfo.setStatus(WatchInfo.WATCH_STATUS_NONE_EXIST);
        //广播付款成功
        notifyPaymentSuccess();
        //跳转到付款成功界面
        toPaymentSuccess();
    }

    private void showPayTipsDialog(int payWay, String tips) {
        if (!isFragmentValid()) return;
        if (payTipsDialog == null) {
            payTipsDialog = Jl_Dialog.builder()
                    .content(tips)
                    .contentColor(getResources().getColor(R.color.black_242424))
                    .cancel(false)
                    .left(getString(R.string.unfinished))
                    .leftColor(getResources().getColor(R.color.blue_558CFF))
                    .leftClickListener((view, dialogFragment) -> {
                        dialogFragment.dismiss();
                        payTipsDialog = null;
                    })
                    .right(getString(R.string.finished))
                    .rightColor(getResources().getColor(R.color.blue_558CFF))
                    .rightClickListener((view, dialogFragment) -> {
                        dialogFragment.dismiss();
                        payTipsDialog = null;
                        startCheckPayResult(payWay);
                    })
                    .build();
        }
        if (!payTipsDialog.isShow()) {
            payTipsDialog.show(getChildFragmentManager(), "Payment tips");
        }
    }

    private void showPayErrorDialog(String message) {
        if (!isFragmentValid()) return;
        if (payErrorDialog == null) {
            payErrorDialog = Jl_Dialog.builder()
                    .content(message)
                    .contentColor(getResources().getColor(R.color.black_242424))
                    .cancel(false)
                    .left(getString(R.string.sure))
                    .leftColor(getResources().getColor(R.color.blue_558CFF))
                    .leftClickListener((view, dialogFragment) -> {
                        dialogFragment.dismiss();
                        payErrorDialog = null;
                        requireActivity().finish();
                    })
                    .build();
        }
        if (!payErrorDialog.isShow()) {
            payErrorDialog.show(getChildFragmentManager(), "Payment error");
        }
    }

    private void notifyPaymentSuccess() {
        Intent intent = new Intent();
        intent.setAction(HealthConstant.ACTION_PAYMENT_SUCCESS);
        intent.putExtra(HealthConstant.EXTRA_WATCH_INFO, watchInfo);
        JL_Log.i(tag, "notifyPaymentSuccess", "action : " + intent.getAction() + ",\n" + watchInfo);
        requireContext().sendBroadcast(intent);
    }

    private void startCheckPayResult(int payWay) {
        checkPaymentResultCount = 0;
        mUIHandler.removeMessages(MSG_CHECK_PAYMENT_RESULT);
        mUIHandler.sendMessageDelayed(mUIHandler.obtainMessage(MSG_CHECK_PAYMENT_RESULT, payWay, 0), DELAY_TIME);
    }
}