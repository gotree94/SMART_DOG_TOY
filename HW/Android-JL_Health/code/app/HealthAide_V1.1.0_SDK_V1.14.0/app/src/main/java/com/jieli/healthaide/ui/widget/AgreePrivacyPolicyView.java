package com.jieli.healthaide.ui.widget;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.ViewAgreePrivacyPolicyBinding;
import com.jieli.healthaide.ui.mine.about.WebFragment;


/**
 * AgreePrivacyPolicyView
 *
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 同意隐私政策控件
 * @since 2024/11/20
 */
public class AgreePrivacyPolicyView {

    /**
     * 上下文
     */
    @NonNull
    private final Context context;
    /**
     * 布局
     */
    @NonNull
    private final ViewAgreePrivacyPolicyBinding binding;
    /**
     * 结果回调
     */
    private final Consumer<Boolean> callback;
    /**
     * 是否同意隐私政策
     */
    private boolean isAgree;

    public AgreePrivacyPolicyView(@NonNull Context context, @NonNull ViewAgreePrivacyPolicyBinding binding) {
        this(context, binding, null);
    }

    public AgreePrivacyPolicyView(@NonNull Context context, @NonNull ViewAgreePrivacyPolicyBinding binding,
                                  Consumer<Boolean> callback) {
        this.binding = binding;
        this.context = context;
        this.callback = callback;
        initUI();
    }

    public boolean isAgree() {
        return isAgree;
    }

    public void setAgree(boolean agree) {
        isAgree = agree;
        if (null != callback) callback.accept(isAgree);
        binding.ivAgreeState.setImageResource(agree ? R.drawable.ic_choose_purple : R.drawable.ic_choose_gray);
    }

    private void initUI() {
        binding.ivAgreeState.setOnClickListener(v -> setAgree(!isAgree));

        String text = context.getString(R.string.agree_policy_tips);
        String userService = context.getString(R.string.user_agreement);
        String privacyPolicy = context.getString(R.string.privacy_policy);
        int startPos = text.indexOf("####");
        if (startPos == -1) return;
        int endPos = startPos + userService.length();
        text = text.replace("####", userService);
        int startPos1 = text.indexOf("****");
        if (startPos1 == -1) return;
        int endPos1 = startPos1 + privacyPolicy.length();
        text = text.replace("****", privacyPolicy);
        SpannableString span = new SpannableString(text);
        span.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                goToUserService();
            }
        }, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.main_color)), startPos, endPos,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        span.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                goToPrivacyPolicy();
            }
        }, startPos1, endPos1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.main_color)), startPos1, endPos1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.tvTips.append(span);
        binding.tvTips.setMovementMethod(LinkMovementMethod.getInstance());
        binding.tvTips.setLongClickable(false);

        setAgree(isAgree);
    }

    private void goToUserService() {
        WebFragment.start(context, context.getString(R.string.user_agreement), context.getString(R.string.user_agreement_url));
    }

    private void goToPrivacyPolicy() {
        WebFragment.start(context, context.getString(R.string.privacy_policy), context.getString(R.string.app_privacy_policy));
    }
}
