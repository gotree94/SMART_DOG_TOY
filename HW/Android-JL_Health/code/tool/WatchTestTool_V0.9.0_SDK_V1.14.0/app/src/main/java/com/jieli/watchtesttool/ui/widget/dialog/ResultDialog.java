package com.jieli.watchtesttool.ui.widget.dialog;

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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.ui.base.BaseDialogFragment;

import java.io.Serializable;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 结果弹窗
 * @since 2021/3/17
 */
public class ResultDialog extends BaseDialogFragment {
    private ImageView ivImg;
    private TextView tvResult;
    private TextView tvBtn;

    private Builder mBuilder;
    private OnResultListener mOnResultListener;

    public final static String KEY_DIALOG_PARAM = "dialog_param";

    private ResultDialog(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_result, container, false);
        ivImg = view.findViewById(R.id.iv_result_img);
        tvResult = view.findViewById(R.id.tv_result);
        tvBtn = view.findViewById(R.id.tv_result_btn);

        tvBtn.setOnClickListener(v -> {
            if (mOnResultListener != null) mOnResultListener.onResult(mBuilder.isOk());
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Window window = requireDialog().getWindow();
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

    @SuppressLint("UseCompatLoadingForColorStateLists")
    public void updateView(Builder builder) {
        if (builder == null || isDetached() || !isAdded()) return;
        setCancelable(builder.isCancel());
        if (builder.getImgId() != 0) {
            ivImg.setImageResource(builder.getImgId());
        }
        tvResult.setText(builder.getResult());
        if (builder.getResultColor() != 0)
            tvResult.setTextColor(getResources().getColor(builder.getResultColor()));
        tvBtn.setText(builder.getBtnText());
        if (builder.getBtnTextColor() != 0)
            tvBtn.setTextColor(getResources().getColorStateList(builder.getBtnTextColor()));
    }

    public void setOnResultListener(OnResultListener listener) {
        this.mOnResultListener = listener;
    }

    public interface OnResultListener{

        void onResult(boolean isOk);
    }

    public static class Builder implements Serializable {
        private boolean cancel;

        private boolean isOk;
        private int imgId;
        private String result;
        private int resultColor;
        private String btnText;
        private int btnTextColor;

        public ResultDialog create() {
            ResultDialog dialog = new ResultDialog();
            dialog.mBuilder = this;
            return dialog;
        }

        public boolean isCancel() {
            return cancel;
        }

        public Builder setCancel(boolean cancel) {
            this.cancel = cancel;
            return this;
        }

        public boolean isOk() {
            return isOk;
        }

        public Builder setOk(boolean ok) {
            isOk = ok;
            return this;
        }

        public int getImgId() {
            return imgId;
        }

        public Builder setImgId(int imgId) {
            this.imgId = imgId;
            return this;
        }

        public String getResult() {
            return result;
        }

        public Builder setResult(String result) {
            this.result = result;
            return this;
        }

        public int getResultColor() {
            return resultColor;
        }

        public Builder setResultColor(int resultColor) {
            this.resultColor = resultColor;
            return this;
        }

        public String getBtnText() {
            return btnText;
        }

        public Builder setBtnText(String btnText) {
            this.btnText = btnText;
            return this;
        }

        public int getBtnTextColor() {
            return btnTextColor;
        }

        public Builder setBtnTextColor(int btnTextColor) {
            this.btnTextColor = btnTextColor;
            return this;
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "cancel=" + cancel +
                    ", isOk=" + isOk +
                    ", imgId=" + imgId +
                    ", result='" + result + '\'' +
                    ", resultColor=" + resultColor +
                    ", btnText='" + btnText + '\'' +
                    ", btnTextColor=" + btnTextColor +
                    '}';
        }
    }

}
