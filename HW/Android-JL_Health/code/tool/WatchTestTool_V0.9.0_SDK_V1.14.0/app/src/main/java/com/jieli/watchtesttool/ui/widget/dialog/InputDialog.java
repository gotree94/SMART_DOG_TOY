package com.jieli.watchtesttool.ui.widget.dialog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.component.utils.ToastUtil;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.databinding.DialogInputBinding;

import java.util.Locale;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 输入对话框
 * @since 2023/10/12
 */
public class InputDialog extends CommonDialog {
    private DialogInputBinding mBinding;

    private InputDialog(Builder builder) {
        super(builder);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Builder builder = (Builder) mBuilder;
        mBinding.etContent.setHint(null == builder.getHint() ? "" : builder.getHint());
        mBinding.etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                final int textSize = text.getBytes().length;
                if (textSize >= getBuilder().limitSize) {
                    mBinding.etContent.setText(text.substring(0, getBuilder().limitSize));
                    mBinding.tvDesc.setText(String.format(Locale.ENGLISH, "%s[%d]",  getString(R.string.input_over_limit), getBuilder().limitSize));
                } else {
                    mBinding.tvDesc.setText(String.format(Locale.ENGLISH, "%s[%d]", getString(R.string.left_input_count), (getBuilder().limitSize - textSize)));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        updateUI();
    }

    @Override
    public View createView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DialogInputBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    public void updateUI() {
        final Builder builder = getBuilder();
        mBinding.tvTitle.setText(builder.title);
        mBinding.btnCancel.setOnClickListener(v -> {
            if (builder.cancelClick != null) {
                builder.cancelClick.onClick(InputDialog.this, null);
            }
            dismiss();
        });
        if (null != builder.confirmClick) {
            mBinding.btnConfirm.setOnClickListener(v -> {
                final String content = mBinding.etContent.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    ToastUtil.showToastShort(getString(R.string.input_content));
                    return;
                }
                builder.confirmClick.onClick(InputDialog.this, content);
                dismiss();
            });
        }
    }

    @Override
    public Builder getBuilder() {
        return (Builder) super.getBuilder();
    }

    public static class Builder extends CommonDialog.Builder {
        private String title;
        private String hint;
        private int limitSize;
        private OnButtonClick cancelClick;
        private OnButtonClick confirmClick;

        public String getTitle() {
            return title;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getHint() {
            return hint;
        }

        public Builder setHint(String hint) {
            this.hint = hint;
            return this;
        }

        public int getLimitSize() {
            return limitSize;
        }

        public Builder setLimitSize(int limitSize) {
            this.limitSize = limitSize;
            return this;
        }

        public OnButtonClick getCancelClick() {
            return cancelClick;
        }

        public Builder setCancelClick(OnButtonClick cancelClick) {
            this.cancelClick = cancelClick;
            return this;
        }

        public OnButtonClick getConfirmClick() {
            return confirmClick;
        }

        public Builder setConfirmClick(OnButtonClick confirmClick) {
            this.confirmClick = confirmClick;
            return this;
        }

        @Override
        public InputDialog build() {
            return new InputDialog(this);
        }
    }
}
