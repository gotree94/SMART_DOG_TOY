package com.jieli.watchtesttool.ui.widget.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.watchtesttool.databinding.DialogPermissionTipsBinding;

/**
 * PermissionTipsDialog
 *
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 权限申请说明弹窗
 * @since 2024/11/25
 */
public class PermissionTipsDialog extends CommonDialog {
    private DialogPermissionTipsBinding mBinding;

    private PermissionTipsDialog(Builder builder) {
        super(builder);
    }

    @Override
    public View createView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DialogPermissionTipsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI();
    }

    private void initUI() {
        if (!(mBuilder instanceof Builder)) return;
        Builder builder = (Builder) mBuilder;
        final TextStyle tipsStyle = builder.mTipsStyle;
        if (null == tipsStyle) {
            mBinding.tvTips.setText("");
            return;
        }
        mBinding.tvTips.setText(tipsStyle.text);
    }

    public static class Builder extends CommonDialog.Builder {
        public TextStyle mTipsStyle;

        public Builder() {
            setGravity(Gravity.TOP);
        }

        public Builder tips(String text) {
            if (null == mTipsStyle) {
                mTipsStyle = new TextStyle();
            }
            mTipsStyle.text = text;
            return this;
        }

        @Override
        public PermissionTipsDialog build() {
            return new PermissionTipsDialog(this);
        }
    }
}
