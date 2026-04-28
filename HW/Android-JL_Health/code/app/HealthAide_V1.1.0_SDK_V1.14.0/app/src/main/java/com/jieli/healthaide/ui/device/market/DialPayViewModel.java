package com.jieli.healthaide.ui.device.market;

import android.annotation.SuppressLint;
import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.alipay.sdk.app.PayTask;
import com.jieli.healthaide.tool.watch.WatchServerCacheHelper;
import com.jieli.healthaide.ui.device.market.bean.ALiPayResult;
import com.jieli.healthaide.ui.device.market.bean.PayResult;
import com.jieli.healthaide.ui.device.market.bean.PayStateResult;
import com.jieli.healthaide.ui.device.market.bean.PayWayEntity;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_health_http.HttpConstant;
import com.jieli.jl_health_http.model.WatchFileMsg;
import com.jieli.jl_health_http.model.watch.ALiPayMsg;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.Map;


/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 表盘支付逻辑
 * @since 2022/6/20
 */
public class DialPayViewModel extends ViewModel {
    private final String TAG = DialPayViewModel.class.getSimpleName();
    private final WatchServerCacheHelper mWatchServerCacheHelper = WatchServerCacheHelper.getInstance();
    @SuppressLint("StaticFieldLeak")
    private final Activity mActivity;

    public final MutableLiveData<PayResult> payResultMLD = new MutableLiveData<>();
    public final MutableLiveData<PayStateResult> payStateResultMLD = new MutableLiveData<>();
    private ALiPayMsg mAliPayMsg;
    private volatile boolean isPaying;

    public DialPayViewModel(Activity activity) {
        mActivity = activity;
    }

    public void payment(int way, WatchFileMsg watchFileMsg) {
        if (null == watchFileMsg) return;
        JL_Log.d(TAG, "payment", "way : " + way);
        if (way == PayWayEntity.PAY_WAY_ALI) {
            paymentByAli(watchFileMsg.getId());
        } else {
            PayStateResult result = new PayStateResult();
            result.setPayWay(way);
            result.setResult(false);
            result.setCode(WatchServerCacheHelper.ERR_NOT_SUPPORT_WAY);
            result.setMessage("暂不支持");
            payStateResultMLD.postValue(result);
        }
    }

    public void checkPaymentState(int way) {
        if (way == PayWayEntity.PAY_WAY_ALI) {
            if (null == mAliPayMsg) {
                PayStateResult result = new PayStateResult();
                result.setPayWay(way);
                result.setResult(false);
                result.setCode(WatchServerCacheHelper.ERR_INVALID_PARAMETER);
                result.setMessage("mAliPayMsg is null");
                payStateResultMLD.postValue(result);
                return;
            }
            mWatchServerCacheHelper.checkPaymentStateByAliPay(mAliPayMsg.getOutTradeNo(), new WatchServerCacheHelper.IWatchHttpCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    JL_Log.d(TAG, "checkPaymentState", "" + result);
                    if (result) mAliPayMsg = null;
                    PayStateResult payStateResult = new PayStateResult();
                    payStateResult.setPayWay(way);
                    payStateResult.setResult(result);
                    payStateResultMLD.postValue(payStateResult);
                }

                @Override
                public void onFailed(int code, String message) {
                    JL_Log.w(TAG, "checkPaymentState", "onFailed ---> " + code + ", " + message);
                    PayStateResult result = new PayStateResult();
                    result.setPayWay(way);
                    result.setResult(false);
                    result.setCode(code);
                    result.setMessage(message);
                    payStateResultMLD.postValue(result);
                }
            });
        } else {
            PayStateResult result = new PayStateResult();
            result.setPayWay(way);
            result.setResult(false);
            result.setCode(WatchServerCacheHelper.ERR_NOT_SUPPORT_WAY);
            result.setMessage("暂不支持");
            payStateResultMLD.postValue(result);
        }
    }

    private void paymentByAli(String id) {
        if (isPaying) {
            JL_Log.i(TAG, "paymentByAli", "正在支付账单 : " + id);
            return;
        }
        isPaying = true;
        mWatchServerCacheHelper.dialPaymentByAliPay(id, WatchServerCacheHelper.IS_SAND_BOX,
                new WatchServerCacheHelper.IWatchHttpCallback<ALiPayMsg>() {
                    @Override
                    public void onSuccess(ALiPayMsg aLiPayMsg) {
                        mAliPayMsg = aLiPayMsg;
                        JL_Log.d(TAG, "paymentByAli", "dialPaymentByAliPay ---> " + aLiPayMsg);
                        new Thread(() -> {
                            JL_Log.d(TAG, "paymentByAli", "PayTask : start");
                            PayTask alipay = new PayTask(mActivity);
                            Map<String, String> result = alipay.payV2(aLiPayMsg.getAlipay(), true);
                            JL_Log.d(TAG, "paymentByAli", "PayTask : " + result);
                            payResultMLD.postValue(new ALiPayResult(result));
                            isPaying = false;
                        }, "PaymentThread").start();
                    }

                    @Override
                    public void onFailed(int code, String message) {
                        JL_Log.w(TAG, "paymentByAli", "onFailed --->  code : " + code + ", " + message);
                        mAliPayMsg = null;
                        ALiPayResult result = new ALiPayResult(null);
                        if (HealthUtil.getHttpErrorCode(message) == HttpConstant.ERROR_REPEAT_BUY_DIAL) {
                            result.setCode(WatchServerCacheHelper.ERR_DIAL_PAYMENT);
                        } else {
                            result.setCode(code);
                        }
                        result.setMessage(message);
                        payResultMLD.postValue(result);
                        isPaying = false;
                    }
                });
    }

    public static class DialPayViewModelFactory implements ViewModelProvider.Factory {
        private final Activity mActivity;

        public DialPayViewModelFactory(Activity activity) {
            mActivity = activity;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new DialPayViewModel(mActivity);
        }
    }
}
