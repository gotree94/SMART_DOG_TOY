package com.jieli.healthaide.ui.base;

import android.annotation.SuppressLint;

import com.jieli.component.ActivityManager;
import com.jieli.healthaide.R;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/22/21 5:21 PM
 * @desc :
 */
public class DoubleClickBackExitActivity extends BaseActivity {
    private long mLastClickTime = 0;

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        if (mOnBackPressIntercept != null && mOnBackPressIntercept.intercept()) return;
        long currentClickTime = System.currentTimeMillis();
        if (currentClickTime - mLastClickTime > 1000) {
            showTips(getString(R.string.double_tap_to_exit));
            mLastClickTime = currentClickTime;
        } else {
            ActivityManager.getInstance().popAllActivity();
//            System.exit(0);
        }
    }
}
