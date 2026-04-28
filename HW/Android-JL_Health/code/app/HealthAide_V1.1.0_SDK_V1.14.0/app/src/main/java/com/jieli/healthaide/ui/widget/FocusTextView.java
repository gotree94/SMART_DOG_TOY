package com.jieli.healthaide.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

/**
 * @ClassName: FocusTextView
 * @Description: 默认焦点地TextView
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/3/8 18:35
 */
public class FocusTextView extends androidx.appcompat.widget.AppCompatTextView {

    public FocusTextView(Context context) {
        super(context);
    }

    public FocusTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FocusTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
