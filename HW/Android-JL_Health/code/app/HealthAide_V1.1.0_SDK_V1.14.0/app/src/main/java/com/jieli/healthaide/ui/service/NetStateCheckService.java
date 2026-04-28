package com.jieli.healthaide.ui.service;

import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import com.jieli.component.ActivityManager;
import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.net.NetWorkStateModel;
import com.jieli.healthaide.tool.net.NetworkStateHelper;
import com.jieli.jl_dialog.Jl_Dialog;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/31/21
 * @desc :
 */
public class NetStateCheckService implements NetworkStateHelper.Listener {

    private Jl_Dialog jl_dialog;

    public NetStateCheckService() {
        NetworkStateHelper.getInstance().registerListener(this);
    }

    @Override
    public void onNetworkStateChange(NetWorkStateModel model) {
        if (model.isAvailable()) {
            dismissDialog();
        } else {
            showTipDialog();
        }
    }

    private void showTipDialog() {
        if (jl_dialog != null) return;
        FragmentActivity activity = (FragmentActivity) ActivityManager.getInstance().getTopActivity();
        if (activity == null || activity.isDestroyed()) return;
        TextView textView = new TextView(activity);
        textView.setText(R.string.tip_check_net);
        textView.setTextSize(16);
        textView.setTextColor(ResourcesCompat.getColor(activity.getResources(), R.color.text_important_color, activity.getTheme()));
        textView.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        textView.setGravity(Gravity.CENTER);
        int paddingTopAndBottom = ValueUtil.dp2px(activity, 32);
        int paddingLeftAndRight = ValueUtil.dp2px(activity, 16);
        textView.setPadding(paddingLeftAndRight, paddingTopAndBottom, paddingLeftAndRight, paddingTopAndBottom);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(lp);
        jl_dialog = Jl_Dialog.builder()
                .contentLayoutView(textView)
                .left(activity.getString(R.string.sure))
                .leftColor(ResourcesCompat.getColor(activity.getResources(), R.color.auxiliary_widget, activity.getTheme()))
                .leftClickListener((view, dialogFragment) -> dismissDialog())
                .build();
        jl_dialog.show(activity.getSupportFragmentManager(), getClass().getCanonicalName());
    }


    private void dismissDialog() {
        if (jl_dialog != null) {
            jl_dialog.dismiss();
        }
    }
}