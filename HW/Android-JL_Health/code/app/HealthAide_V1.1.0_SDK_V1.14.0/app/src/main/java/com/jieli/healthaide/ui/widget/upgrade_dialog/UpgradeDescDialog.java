package com.jieli.healthaide.ui.widget.upgrade_dialog;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.base.BaseDialogFragment;

import java.io.Serializable;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 升级更新内容弹窗
 * @since 2021/3/17
 */
public class UpgradeDescDialog extends BaseDialogFragment {
    private TextView tvTitle;
    private TextView tvContent;
    private TextView tvLeft;
    private TextView tvRight;

    private OnUpgradeDescListener mOnUpgradeDescListener;
    private Builder mBuilder;

    public final static String KEY_DIALOG_PARAM = "dialog_param";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_upgrade_desc, container, false);
        tvTitle = view.findViewById(R.id.tv_upgrade_desc_title);
        tvContent = view.findViewById(R.id.tv_upgrade_desc_content);
        tvLeft = view.findViewById(R.id.tv_upgrade_desc_left);
        tvRight = view.findViewById(R.id.tv_upgrade_Desc_right);

        tvLeft.setOnClickListener(v -> {
            if (mOnUpgradeDescListener != null) mOnUpgradeDescListener.onLeftClick();
        });
        tvRight.setOnClickListener(v -> {
            if (mOnUpgradeDescListener != null) mOnUpgradeDescListener.onRightClick();
        });
        if (getDialog() != null) getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getDialog() == null) return;
        Window window = getDialog().getWindow();
        if (window == null) return;
        WindowManager.LayoutParams mLayoutParams = window.getAttributes();
        mLayoutParams.gravity = Gravity.BOTTOM;
        mLayoutParams.dimAmount = 0.5f;
        mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;

        mLayoutParams.width = getScreenWidth();
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        window.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.text_transparent)));
        window.getDecorView().getRootView().setBackgroundColor(Color.TRANSPARENT);
        window.setAttributes(mLayoutParams);

        setCancelable(false);

        if (getArguments() != null) {
            Builder temp = (Builder) getArguments().getSerializable(KEY_DIALOG_PARAM);
            if (temp != null) {
                mBuilder = temp;
            }
        }
        updateView(mBuilder);
    }

    public void setOnUpgradeDescListener(OnUpgradeDescListener listener) {
        mOnUpgradeDescListener = listener;
    }

    public Builder getBuilder() {
        return mBuilder;
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    public void updateView(Builder builder) {
        if (builder == null || isDetached() || !isAdded()) return;
        tvTitle.setText(builder.getTitle());
        tvContent.setText(builder.getContent());
        tvLeft.setText(builder.getLeftText());
        if (builder.getLeftTextColor() != 0) {
            tvLeft.setTextColor(getResources().getColorStateList(builder.getLeftTextColor()));
        }
        tvRight.setText(builder.getRightText());
        if (builder.getRightTextColor() != 0) {
            tvRight.setTextColor(getResources().getColorStateList(builder.getRightTextColor()));
        }
    }


    public interface OnUpgradeDescListener {

        void onLeftClick();

        void onRightClick();
    }

    public static class Builder implements Serializable {

        private String title;
        private String content;
        private String leftText;
        private int leftTextColor;
        private String rightText;
        private int rightTextColor;

        public UpgradeDescDialog create() {
            UpgradeDescDialog dialog = new UpgradeDescDialog();
            dialog.mBuilder = this;
            return dialog;
        }

        public String getTitle() {
            return title;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getContent() {
            return content;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public String getLeftText() {
            return leftText;
        }

        public Builder setLeftText(String leftText) {
            this.leftText = leftText;
            return this;
        }

        public int getLeftTextColor() {
            return leftTextColor;
        }

        public Builder setLeftTextColor(int leftTextColor) {
            this.leftTextColor = leftTextColor;
            return this;
        }

        public String getRightText() {
            return rightText;
        }

        public Builder setRightText(String rightText) {
            this.rightText = rightText;
            return this;
        }

        public int getRightTextColor() {
            return rightTextColor;
        }

        public Builder setRightTextColor(int rightTextColor) {
            this.rightTextColor = rightTextColor;
            return this;
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "title='" + title + '\'' +
                    ", content='" + content + '\'' +
                    ", leftText='" + leftText + '\'' +
                    ", leftTextColor=" + leftTextColor +
                    ", rightText='" + rightText + '\'' +
                    ", rightTextColor=" + rightTextColor +
                    '}';
        }
    }

}
