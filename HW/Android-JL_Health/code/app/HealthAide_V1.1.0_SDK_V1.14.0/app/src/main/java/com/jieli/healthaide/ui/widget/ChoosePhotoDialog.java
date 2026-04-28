package com.jieli.healthaide.ui.widget;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.base.BaseDialogFragment;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc
 * @since 2021/4/13
 */
public class ChoosePhotoDialog extends BaseDialogFragment {
    private OnChoosePhotoListener mOnChoosePhotoListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null) {
            //设置dialog的基本样式参数
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            Window window = getDialog().getWindow();
            if (window != null) {
                //去掉dialog默认的padding
                window.getDecorView().setPadding(0, 0, 0, 0);
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                //设置dialog的位置在底部
                lp.gravity = Gravity.BOTTOM;
                //设置dialog的动画
                lp.windowAnimations = R.style.BottomToTopAnim;
                window.setAttributes(lp);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
        View view = inflater.inflate(R.layout.dialog_choose_photo, container, false);
        view.findViewById(R.id.tv_dialog_choose_photo_take_photo).setOnClickListener(v -> {
            if (mOnChoosePhotoListener != null) mOnChoosePhotoListener.onTakePhoto();
        });
        view.findViewById(R.id.tv_dialog_choose_photo_from_album).setOnClickListener(v -> {
            if (mOnChoosePhotoListener != null) mOnChoosePhotoListener.onSelectFromAlbum();
        });
        view.findViewById(R.id.tv_dialog_choose_photo_cancel).setOnClickListener(v -> {
            if (mOnChoosePhotoListener != null) mOnChoosePhotoListener.onCancel();
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void setOnChoosePhotoListener(OnChoosePhotoListener onChoosePhotoListener) {
        mOnChoosePhotoListener = onChoosePhotoListener;
    }

    public interface OnChoosePhotoListener {
        void onTakePhoto();

        void onSelectFromAlbum();

        void onCancel();
    }
}
