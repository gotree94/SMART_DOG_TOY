package com.jieli.healthaide.ui.device.aicloud;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.widget.FocusTextView;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc
 * @since 2021/4/2
 */
public class MessageMorePopWindow {
    private final static String TAG = MessageMorePopWindow.class.getSimpleName();
    private PopupWindow mPopupWindow;

    private OnPopWindowListener mListener;

    public MessageMorePopWindow(Context context) {
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
            int viewHeight = mPopupWindow.getContentView().getMeasuredHeight();
            int screenWidth = getScreenWidth(parent.getContext());
            int offsetX1 = (parent.getWidth() / 2) - (viewWidth / 2);
            int offsetY = -(viewHeight + parent.getHeight());
            if ((offsetX1 + location[0] + viewWidth) > screenWidth) {//超出屏幕范围
                offsetX1 = (screenWidth-viewWidth-location[0]);
            }
            mPopupWindow.showAsDropDown(parent, offsetX1, offsetY);
            mPopupWindow.update();
        }
    }

    public void dismissPopupWindow() {
        if (isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    public void setPopWindowListener(OnPopWindowListener listener) {
        mListener = listener;
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_pop_ai_cloud_message_more, null);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        FocusTextView tvCopy = view.findViewById(R.id.tv_pop_menu_copy);
        FocusTextView tvMulti = view.findViewById(R.id.tv_pop_menu_multi);
        tvCopy.setOnClickListener(v -> {
            if (mListener != null) mListener.onItemClick(0);
            dismissPopupWindow();
        });

        tvMulti.setOnClickListener(v -> {
            if (mListener != null) mListener.onItemClick(1);
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

    public interface OnPopWindowListener {

        void onItemClick(int item);
    }

}
