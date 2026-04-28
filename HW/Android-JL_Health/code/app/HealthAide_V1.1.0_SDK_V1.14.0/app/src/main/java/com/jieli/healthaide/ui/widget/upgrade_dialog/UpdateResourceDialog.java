package com.jieli.healthaide.ui.widget.upgrade_dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.base.BaseDialogFragment;
import com.jieli.healthaide.util.CalendarUtil;

import java.io.Serializable;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 升级资源进度弹窗
 * @since 2021/3/18
 */
public class UpdateResourceDialog extends BaseDialogFragment {

    private TextView tvTitle;
    private TextView tvName;
    private TextView tvProgress;
    private ProgressBar pbProgress;
    private TextView tvTips;

    private Builder mBuilder;

    public final static String KEY_DIALOG_PARAM = "dialog_param";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_update_resource, container, false);
        tvTitle = view.findViewById(R.id.tv_update_res_title);
        tvName = view.findViewById(R.id.tv_update_res_name);
        tvProgress = view.findViewById(R.id.tv_update_res_progress);
        pbProgress = view.findViewById(R.id.pb_update_res_progress);
        tvTips = view.findViewById(R.id.tv_update_res_tips);
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

    public Builder getBuilder() {
        return mBuilder;
    }

    public void updateView(Builder builder) {
        if (builder == null || isDetached() || !isAdded()) return;
        if (builder.getTitle() != null) tvTitle.setText(builder.getTitle());
        if (builder.getTitleTextColor() != 0)
            tvTitle.setTextColor(ContextCompat.getColor(requireContext(), builder.getTitleTextColor()));
        if(builder.getName() != null) tvName.setText(builder.getName());
        if(builder.getNameTextColor() != 0) tvName.setTextColor(ContextCompat.getColor(requireContext(), builder.getNameTextColor()));
        if (builder.getProgress() >= 0) {
            tvProgress.setText(CalendarUtil.formatString("%d%%", builder.getProgress()));
            pbProgress.setProgress(builder.getProgress());
        }
        if (builder.getProgressTextColor() != 0)
            tvProgress.setTextColor(ContextCompat.getColor(requireContext(), builder.getProgressTextColor()));
        if (builder.getTips() != null) tvTips.setText(builder.getTips());
        if (builder.getTipsTextColor() != 0)
            tvTips.setTextColor(ContextCompat.getColor(requireContext(), builder.getTipsTextColor()));
    }

    public static class Builder implements Serializable {
        private String title;
        private int titleTextColor;
        private String name;
        private int nameTextColor;
        private int progress;
        private int progressTextColor;
        private String tips;
        private int tipsTextColor;

        public UpdateResourceDialog create() {
            UpdateResourceDialog dialog = new UpdateResourceDialog();
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

        public int getTitleTextColor() {
            return titleTextColor;
        }

        public Builder setTitleTextColor(int titleTextColor) {
            this.titleTextColor = titleTextColor;
            return this;
        }

        public String getName() {
            return name;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public int getNameTextColor() {
            return nameTextColor;
        }

        public Builder setNameTextColor(int nameTextColor) {
            this.nameTextColor = nameTextColor;
            return this;
        }

        public int getProgress() {
            return progress;
        }

        public Builder setProgress(int progress) {
            this.progress = progress;
            return this;
        }

        public int getProgressTextColor() {
            return progressTextColor;
        }

        public Builder setProgressTextColor(int progressTextColor) {
            this.progressTextColor = progressTextColor;
            return this;
        }

        public String getTips() {
            return tips;
        }

        public Builder setTips(String tips) {
            this.tips = tips;
            return this;
        }

        public int getTipsTextColor() {
            return tipsTextColor;
        }

        public Builder setTipsTextColor(int tipsTextColor) {
            this.tipsTextColor = tipsTextColor;
            return this;
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "title='" + title + '\'' +
                    ", titleTextColor=" + titleTextColor +
                    ", name='" + name + '\'' +
                    ", nameTextColor=" + nameTextColor +
                    ", progress=" + progress +
                    ", progressTextColor=" + progressTextColor +
                    ", tips='" + tips + '\'' +
                    ", tipsTextColor=" + tipsTextColor +
                    '}';
        }
    }
}
