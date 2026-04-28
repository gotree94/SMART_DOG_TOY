package com.jieli.healthaide.tool.phone;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 电话状态辅助类
 * @since 2021/11/26
 */
@Deprecated
public class PhoneHelper {
   /* private final String tag = PhoneHelper.class.getSimpleName();
    private static volatile PhoneHelper instance;

    private final TelephonyManager mTelephonyManager;
    private OnCallStateListener mListener;

    private PhoneHelper() throws RuntimeException {
        final Context context = HealthApplication.getAppViewModel().getApplication();
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (mTelephonyManager == null) {
            throw new RuntimeException("TelephonyManager is null.");
        }
        if (!PermissionUtil.isHasPermission(context, Manifest.permission.READ_PHONE_STATE)) {
            throw new RuntimeException("No phone permission.");
        }
        mTelephonyManager.listen(new CustomPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }

    public static PhoneHelper getInstance() throws Exception {
        if (instance == null) {
            synchronized (PhoneHelper.class) {
                if (instance == null) {
                    instance = new PhoneHelper();
                }
            }
        }
        return instance;
    }

    public void destroy() {
        mListener = null;
        instance = null;
    }

    public void setOnCallStateListener(OnCallStateListener onCallStateListener) {
        mListener = onCallStateListener;
    }

    *//**
     * 获取来电状态
     *
     * @return 来电状态
     *//*
    public int getCallState() {
        if (mTelephonyManager == null) return -1;
        return mTelephonyManager.getCallState();
    }

    *//**
     * 是否处于响铃状态
     *
     * @return 结果
     *//*
    public boolean isRinging() {
        return getCallState() == TelephonyManager.CALL_STATE_RINGING;
    }

    *//**
     * 是否处于电话工作状态
     *
     * @return 结果
     *//*
    public boolean isCallWorking() {
        return getCallState() != TelephonyManager.CALL_STATE_IDLE;
    }

    public class CustomPhoneStateListener extends PhoneStateListener {


        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            int callState = getCallState();
            JL_Log.i(tag, "phone state : " + state + ", phoneNumber = " + phoneNumber + ", callState = " + callState);
            if (mListener != null) {
                mListener.onCallState(callState);
            }
        }
    }

    public interface OnCallStateListener {

        void onCallState(int state);
    }*/
}
