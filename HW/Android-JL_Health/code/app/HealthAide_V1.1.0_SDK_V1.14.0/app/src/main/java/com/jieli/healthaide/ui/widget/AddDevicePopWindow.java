package com.jieli.healthaide.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.R;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc
 * @since 2021/4/2
 */
public class AddDevicePopWindow {
    private final static String TAG = AddDevicePopWindow.class.getSimpleName();
    private PopupWindow mPopupWindow;

    private OnAddDevicePopWindowListener mListener;

    public final static int SCAN_WAY_QR = 1;
    public final static int SCAN_WAY_DEVICE = 2;

    public AddDevicePopWindow(Context context) {
        if (context == null) {
            throw new NullPointerException("context can not be null.");
        }
        init(context);
    }

    public boolean isShowing() {
        return mPopupWindow != null && mPopupWindow.isShowing();
    }

    public void showPopupWindow(View parent) {
        if (mPopupWindow != null && !isShowing() && parent != null) {
            int[] location = new int[2];
            parent.getLocationOnScreen(location);
            mPopupWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int viewWidth = mPopupWindow.getContentView().getMeasuredWidth();
            int x = location[0] - viewWidth + (parent.getWidth() - parent.getPaddingStart() - parent.getPaddingEnd()) / 2;
            int y = parent.getHeight() + ValueUtil.dp2px(parent.getContext(), 10);
            int offsetX = -(viewWidth - (parent.getWidth() - parent.getPaddingStart() - parent.getPaddingEnd()) / 2 - ValueUtil.dp2px(parent.getContext(), 10));
            JL_Log.i(TAG, "showPopupWindow", "x = " + x + ", y = " + y + ", mPopupWindow = " + viewWidth + ", " + parent.getWidth()
                    + ",  location 0 :" + location[0] + ",  location 1 :" + location[1] + ",  " + offsetX);
            mPopupWindow.showAsDropDown(parent, offsetX, -ValueUtil.dp2px(parent.getContext(), 20));
//            mPopupWindow.showAtLocation(parent, Gravity.NO_GRAVITY, x, y);
            mPopupWindow.update();
        }
    }

    public void dismissPopupWindow() {
        if (isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    public void setOnAddDevicePopWindowListener(OnAddDevicePopWindowListener listener) {
        mListener = listener;
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_pop_menu, null);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        TextView tvScanQr = view.findViewById(R.id.tv_pop_menu_scan_qr);
        TextView tvScanDevice = view.findViewById(R.id.tv_pop_menu_add_device);
        tvScanQr.setOnClickListener(v -> {
            if (mListener != null) mListener.onItemClick(SCAN_WAY_QR);
            dismissPopupWindow();
        });

        tvScanDevice.setOnClickListener(v -> {
            if (mListener != null) mListener.onItemClick(SCAN_WAY_DEVICE);
            dismissPopupWindow();
        });

        if (mPopupWindow == null) {
            mPopupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mPopupWindow.setClippingEnabled(false);
            mPopupWindow.setFocusable(true);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mPopupWindow.setTouchable(true);
        }
    }

    public int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = getDisplayMetrics(context);
        return displayMetrics == null ? 0 : displayMetrics.widthPixels;
    }

    public int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = getDisplayMetrics(context);
        return displayMetrics == null ? 0 : displayMetrics.heightPixels;
    }

    private DisplayMetrics getDisplayMetrics(Context context) {
        if (context == null) return null;
        if (context.getResources() == null) return null;
        return context.getResources().getDisplayMetrics();
    }

    public interface OnAddDevicePopWindowListener {

        void onItemClick(int scanWay);
    }

}
