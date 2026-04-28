package com.jieli.healthaide.util;

import android.text.Layout;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.ItemSettingsBinding;
import com.jieli.healthaide.databinding.ItemSettingsSwitchBinding;

/**
 * UIHelper
 *
 * @author zhongzhuocheng
 * email: zhongzhuocheng@zh-jieli.com
 * create: 2026/1/12
 * note:  UI辅助类
 */
public class UIHelper {

    public static void setVisibility(View view, int visibility) {
        if (null == view) return;
        final int oldValue = view.getVisibility();
        if (oldValue != visibility) {
            view.setVisibility(visibility);
        }
    }

    public static void show(View view) {
        setVisibility(view, View.VISIBLE);
    }

    public static void gone(View view) {
        setVisibility(view, View.GONE);
    }

    public static void hide(View view) {
        setVisibility(view, View.INVISIBLE);
    }

    public static int getTextViewHeight(@NonNull TextView textView) {
        Layout layout = textView.getLayout();
        if (null == layout) return 0;
        int desired = layout.getLineTop(textView.getLineCount());
        int padding = textView.getCompoundPaddingTop() + textView.getCompoundPaddingBottom();
        return desired + padding;
    }

    /**
     * 更新设置文本布局UI
     *
     * @param binding     ItemSettingsBinding 布局控制器
     * @param title       String 标题
     * @param value       String 内容
     * @param isShowArrow boolean 是否显示箭头
     * @param isShowLine  boolean 是否显示下划线
     * @param callback    View.OnClickListener 点击事件回调
     */
    public static void updateItemSettingsTextUI(@NonNull ItemSettingsBinding binding, String title, String value,
                                                boolean isShowArrow, boolean isShowLine, View.OnClickListener callback) {
        if (null != title) {
            binding.tvItemSettingsName.setText(title);
        }
        if (null != value) {
            binding.tvItemSettingsValue.setText(value);
        }
        binding.tvItemSettingsValue.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, isShowArrow ? R.drawable.ic_right_arrow_small : 0, 0);
        if (isShowLine) {
            show(binding.viewItemSettingsLine);
        } else {
            gone(binding.viewItemSettingsLine);
        }
        if (null != callback) {
            binding.getRoot().setOnClickListener(callback);
        }
    }

    /**
     * 更新设置带开关布局UI
     *
     * @param binding  ItemSettingsSwitchBinding 布局控制器
     * @param title    String 标题
     * @param iconId   int 图标资源ID
     * @param isOn     boolean 是否开启
     * @param listener CompoundButton.OnCheckedChangeListener 开关监听器
     */
    public static void updateItemSettingsSwitchUI(@NonNull ItemSettingsSwitchBinding binding, String title, int iconId, boolean isOn,
                                                  CompoundButton.OnCheckedChangeListener listener) {
        if (null != title) {
            binding.tvTitle.setText(title);
        }
        if (iconId != Integer.MIN_VALUE) {
            if (iconId == 0) {
                gone(binding.ivImage);
            } else {
                show(binding.ivImage);
                binding.ivImage.setImageResource(iconId);
            }
        }
        binding.switchBtn.setCheckedNoEvent(isOn);
        if (null != listener) {
            binding.switchBtn.setOnCheckedChangeListener(listener);
        }
    }
}
