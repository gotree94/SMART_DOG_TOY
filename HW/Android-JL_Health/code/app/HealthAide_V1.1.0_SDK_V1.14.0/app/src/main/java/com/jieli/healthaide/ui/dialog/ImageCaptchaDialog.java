package com.jieli.healthaide.ui.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.DialogImageVerifyBinding;
import com.jieli.healthaide.tool.http.base.BaseHttpResultHandler;
import com.jieli.healthaide.ui.base.BaseDialogFragment;
import com.jieli.jl_health_http.HttpClient;
import com.jieli.jl_health_http.model.ImageInfo;
import com.jieli.jl_health_http.tool.OnResultCallback;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * ImageCaptchaDialog
 *
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 图像验证码弹窗
 * @since 2024/7/26
 */
public class ImageCaptchaDialog extends BaseDialogFragment {
    private DialogImageVerifyBinding binding;
    /**
     * 结果回调
     */
    @NonNull
    private final OnResultCallback<ImageInfo> callback;

    public ImageCaptchaDialog(@NonNull OnResultCallback<ImageInfo> callback) {
        this.callback = callback;
    }

    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private ImageInfo mImageInfo;

    @Override
    public void onStart() {
        super.onStart();
        Window window = requireDialog().getWindow();
        if (window != null) {
            //去掉dialog默认的padding
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = Math.round(getScreenWidth() * 0.9f);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.CENTER;
            window.setAttributes(lp);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requireDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = DialogImageVerifyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI();
        requestImageCode();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        uiHandler.removeCallbacksAndMessages(null);
    }

    private void initUI() {
        binding.tvRefreshCode.setOnClickListener(v -> requestImageCode());
        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnConfirm.setOnClickListener(v -> {
            String code = binding.etInputCode.getText().toString().trim();
            if (TextUtils.isEmpty(code)) {
                showTips(getString(R.string.input_captcha_tips));
                return;
            }
            checkImageVerificationCode(mImageInfo, code, new OnResultCallback<Boolean>() {
                @Override
                public void onResult(Boolean result) {
                    if(!isAdded()) return;
                    mImageInfo.setValue(code);
                    callback.onResult(mImageInfo);
                    dismiss();
                }

                @Override
                public void onError(int code, String message) {
                    JL_Log.w(TAG, "checkImageVerificationCode", "onError ---> code : " + code + ", " + message);
                    if(!isAdded()) return;
                    callback.onError(code, message);
                    dismiss();
                }
            });
        });
    }

    private void requestImageCode() {
        requestImageVerificationCode(new OnResultCallback<ImageInfo>() {
            @Override
            public void onResult(ImageInfo result) {
                if(!isAdded()) return;
                mImageInfo = result;
                uiHandler.post(() -> {
                    if ("gif".equalsIgnoreCase(mImageInfo.getType())) {
                        Glide.with(HealthApplication.getAppViewModel().getApplication())
                                .asGif()
                                .load("data:image/gif;base64," + mImageInfo.getBase64())
                                .error(new ColorDrawable(ContextCompat.getColor(requireContext(), R.color.gray_9E9E9E)))
                                .into(binding.ivImageVerificationCode);
                    } else {
                        Glide.with(HealthApplication.getAppViewModel().getApplication())
                                .asBitmap()
                                .load("data:image/gif;base64," + mImageInfo.getBase64())
                                .error(new ColorDrawable(ContextCompat.getColor(requireContext(), R.color.gray_9E9E9E)))
                                .into(binding.ivImageVerificationCode);
                    }
                });
            }

            @Override
            public void onError(int code, String message) {
                JL_Log.w(TAG, "requestImageCode", "onError ---> code : " + code + ", " + message);
                if(!isAdded()) return;
                showTips(getString(R.string.load_code_failed_tips));
            }
        });
    }

    private void requestImageVerificationCode(OnResultCallback<ImageInfo> callback) {
        HttpClient.createUserApi().getImageVerificationCode().enqueue(new BaseHttpResultHandler<>(callback));
    }

    private void checkImageVerificationCode(ImageInfo info, String value, OnResultCallback<Boolean> callback) {
        HttpClient.createUserApi().checkImageVerificationCode(info.getCode(), value).enqueue(new BaseHttpResultHandler<>(callback));
    }
}
