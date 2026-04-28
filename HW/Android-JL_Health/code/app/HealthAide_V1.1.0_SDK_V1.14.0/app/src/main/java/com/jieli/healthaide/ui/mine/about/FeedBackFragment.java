package com.jieli.healthaide.ui.mine.about;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentFeedBackBinding;
import com.jieli.healthaide.ui.base.BaseFragment;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * FeedBackFragment
 *
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 用户反馈界面
 * @since 2024/4/2 14:00
 */
public class FeedBackFragment extends BaseFragment {

    private final static int LIMIT_TEXT_SIZE = 200;
    private FragmentFeedBackBinding mBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentFeedBackBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI();
    }

    private void initUI() {
        mBinding.viewTopBar.tvTopbarTitle.setText(getString(R.string.feedback));
        mBinding.viewTopBar.tvTopbarLeft.setOnClickListener(v -> requireActivity().finish());

        mBinding.etFeedback.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (null == s) return;
                String text = s.toString().trim();
                int size = text.length();
                if (size > LIMIT_TEXT_SIZE) {
                    mBinding.etFeedback.setText(text.substring(0, LIMIT_TEXT_SIZE));
                    mBinding.etFeedback.setSelection(LIMIT_TEXT_SIZE - 1);
                    size = LIMIT_TEXT_SIZE;
                }
                mBinding.tvTextCounter.setText(String.format(Locale.ENGLISH, "%d/%d", size, LIMIT_TEXT_SIZE));
            }
        });
        mBinding.btnCommit.setOnClickListener(v -> tryToSubmitFeedback());
    }

    private void tryToSubmitFeedback() {
        String feedbackContent = mBinding.etFeedback.getText().toString();
        String feedbackPhone = mBinding.etPhoneNumber.getText().toString();
        if (TextUtils.isEmpty(feedbackContent)) {
            showTips(getString(R.string.feedback_empty_tips));
            return;
        }
        if (TextUtils.isEmpty(feedbackPhone)) {
            showTips(getString(R.string.phone_empty_tips));
            return;
        }
        if (feedbackPhone.contains(" ") || feedbackPhone.length() != 11) {
            showTips(getString(R.string.phone_format_tips));
            return;
        }
        postFeedbackRequest(feedbackContent, feedbackPhone);
    }

    private void postFeedbackRequest(@NonNull String feedback, @NonNull String phone) {
        OkHttpClient okhttpClient = new OkHttpClient();
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("feedbackContent", feedback);
        formBody.add("feedbackPhone", phone);
        Request request = new Request.Builder()
                .url("https://www.zh-jieli.com/")
                .post(formBody.build())
                .build();
        Call call2 = okhttpClient.newCall(request);
        call2.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> showTips(getString(R.string.network_exception_tips)));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                requireActivity().runOnUiThread(() -> {
                    showTips(getString(R.string.feedback_success));
                    requireActivity().finish();
                });
            }
        });
    }
}