package com.jieli.healthaide.ui.mine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.jieli.component.ActivityManager;
import com.jieli.component.utils.PreferencesHelper;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentLanguageSetBinding;
import com.jieli.healthaide.ui.home.HomeActivity;
import com.jieli.healthaide.util.MultiLanguageUtils;

import static com.jieli.healthaide.util.MultiLanguageUtils.LANGUAGE_AUTO;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LanguageSetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LanguageSetFragment extends Fragment {

    private FragmentLanguageSetBinding binding;
    private String selectLanguage = null;
    private String setLanguage = null;//已设置的语言
    private boolean isSaved = false;

    public static LanguageSetFragment newInstance() {
        return new LanguageSetFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_language_set, container, false);
        binding.viewTopbar.tvTopbarTitle.setText(getString(R.string.set_language));
        binding.viewTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.viewTopbar.tvTopbarRight.setVisibility(View.VISIBLE);
        binding.viewTopbar.tvTopbarRight.setTextColor(getResources().getColor(R.color.gray_D8D8D8));
        binding.viewTopbar.tvTopbarRight.setText(getString(R.string.sure));
        binding.viewTopbar.tvTopbarRight.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        binding.viewTopbar.tvTopbarRight.setOnClickListener(v -> saveChange());
        binding.tvFollowSystem.setTag(LANGUAGE_AUTO);
        binding.tvSimplifiedChinese.setTag(MultiLanguageUtils.LANGUAGE_ZH);
        binding.tvEnglish.setTag(MultiLanguageUtils.LANGUAGE_EN);
        binding.tvJanapese.setTag(MultiLanguageUtils.LANGUAGE_JA);
        binding.tvKorean.setTag(MultiLanguageUtils.LANGUAGE_KO);
        binding.tvFrench.setTag(MultiLanguageUtils.LANGUAGE_FR);
        binding.tvGerman.setTag(MultiLanguageUtils.LANGUAGE_DE);
        binding.tvItalian.setTag(MultiLanguageUtils.LANGUAGE_IT);
        binding.tvPortuguese.setTag(MultiLanguageUtils.LANGUAGE_PT);
        binding.tvSpanish.setTag(MultiLanguageUtils.LANGUAGE_ES);
        binding.tvSwedish.setTag(MultiLanguageUtils.LANGUAGE_SV);
        binding.tvPolish.setTag(MultiLanguageUtils.LANGUAGE_PL);
        binding.tvRussian.setTag(MultiLanguageUtils.LANGUAGE_RU);
        binding.tvTurkish.setTag(MultiLanguageUtils.LANGUAGE_TR);
        binding.tvHebrew.setTag(MultiLanguageUtils.LANGUAGE_IW);
        binding.tvThai.setTag(MultiLanguageUtils.LANGUAGE_TH);
        binding.tvArabic.setTag(MultiLanguageUtils.LANGUAGE_AR);
        binding.tvVietnamese.setTag(MultiLanguageUtils.LANGUAGE_VI);
        binding.tvIndonesian.setTag(MultiLanguageUtils.LANGUAGE_IN);
        binding.tvMalay.setTag(MultiLanguageUtils.LANGUAGE_MS);
        binding.tvPersian.setTag(MultiLanguageUtils.LANGUAGE_FA);

        initClick();
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setLanguage = PreferencesHelper.getSharedPreferences(HealthApplication.getAppViewModel().getApplication()).getString(MultiLanguageUtils.SP_LANGUAGE, MultiLanguageUtils.LANGUAGE_AUTO);
        selectLanguage = setLanguage;
        updateSelectView();
        updateConfirmButton();
    }

    private void initClick() {
        ConstraintLayout parent = binding.constraintLayout;
        int count = parent.getChildCount();
        for (int i = 1; i < count; i++) {
            if (!(parent.getChildAt(i) instanceof TextView)) continue;
            TextView textView = (TextView) parent.getChildAt(i);
            textView.setOnClickListener(v -> {
                if (v.getTag() != null) {
                    selectLanguage = (String) v.getTag();
                    updateSelectView();
                    updateConfirmButton();
                }
            });
        }
    }

    private void updateConfirmButton() {
        binding.viewTopbar.tvTopbarRight.setTextColor(getResources().getColor(!isSetLanguage(selectLanguage) ? R.color.blue_558CFF : R.color.gray_D8D8D8));
        binding.viewTopbar.tvTopbarRight.setEnabled(!isSetLanguage(selectLanguage));
    }

    private void updateSelectView() {
        ConstraintLayout parent = binding.constraintLayout;
        int count = parent.getChildCount();
        for (int i = 1; i < count; i++) {
            if (!(parent.getChildAt(i) instanceof TextView)) continue;
            TextView textView = (TextView) parent.getChildAt(i);
            String tag = (String) textView.getTag();
            if (selectLanguage != null && tag.equals(selectLanguage)) {
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_choose_blue, 0);
            } else {
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
            }
        }
    }

    private boolean isSetLanguage(String language) {
        return TextUtils.equals(setLanguage, language);
    }

    private void saveChange() {
        if (isSaved) return;
        isSaved = true;
        Context context = HealthApplication.getAppViewModel().getApplication();
        switch (selectLanguage) {
            case LANGUAGE_AUTO://切换到 跟随系统
                MultiLanguageUtils.changeLanguageAndArea(context, LANGUAGE_AUTO, MultiLanguageUtils.AREA_AUTO);
                break;
            case MultiLanguageUtils.LANGUAGE_ZH:
                MultiLanguageUtils.changeLanguageAndArea(context, MultiLanguageUtils.LANGUAGE_ZH, MultiLanguageUtils.AREA_ZH);
                break;
            case MultiLanguageUtils.LANGUAGE_EN:
                MultiLanguageUtils.changeLanguageAndArea(context, MultiLanguageUtils.LANGUAGE_EN, MultiLanguageUtils.AREA_EN);
                break;
            case MultiLanguageUtils.LANGUAGE_JA:
                MultiLanguageUtils.changeLanguage(context, MultiLanguageUtils.LANGUAGE_JA);
                break;
            case MultiLanguageUtils.LANGUAGE_KO:
                MultiLanguageUtils.changeLanguage(context, MultiLanguageUtils.LANGUAGE_KO);
                break;
            case MultiLanguageUtils.LANGUAGE_FR:
                MultiLanguageUtils.changeLanguage(context, MultiLanguageUtils.LANGUAGE_FR);
                break;
            case MultiLanguageUtils.LANGUAGE_DE:
                MultiLanguageUtils.changeLanguage(context, MultiLanguageUtils.LANGUAGE_DE);
                break;
            case MultiLanguageUtils.LANGUAGE_IT:
                MultiLanguageUtils.changeLanguage(context, MultiLanguageUtils.LANGUAGE_IT);
                break;
            case MultiLanguageUtils.LANGUAGE_PT:
                MultiLanguageUtils.changeLanguage(context, MultiLanguageUtils.LANGUAGE_PT);
                break;
            case MultiLanguageUtils.LANGUAGE_ES:
                MultiLanguageUtils.changeLanguage(context, MultiLanguageUtils.LANGUAGE_ES);
                break;
            case MultiLanguageUtils.LANGUAGE_SV:
                MultiLanguageUtils.changeLanguage(context, MultiLanguageUtils.LANGUAGE_SV);
                break;
            case MultiLanguageUtils.LANGUAGE_PL:
                MultiLanguageUtils.changeLanguage(context, MultiLanguageUtils.LANGUAGE_PL);
                break;
            case MultiLanguageUtils.LANGUAGE_RU:
                MultiLanguageUtils.changeLanguage(context, MultiLanguageUtils.LANGUAGE_RU);
                break;
            case MultiLanguageUtils.LANGUAGE_TR:
                MultiLanguageUtils.changeLanguage(context, MultiLanguageUtils.LANGUAGE_TR);
                break;
            case MultiLanguageUtils.LANGUAGE_IW:
                MultiLanguageUtils.changeLanguage(context, MultiLanguageUtils.LANGUAGE_IW);
                break;
            case MultiLanguageUtils.LANGUAGE_TH:
                MultiLanguageUtils.changeLanguage(context, MultiLanguageUtils.LANGUAGE_TH);
                break;
            case MultiLanguageUtils.LANGUAGE_AR:
                MultiLanguageUtils.changeLanguage(context, MultiLanguageUtils.LANGUAGE_AR);
                break;
            case MultiLanguageUtils.LANGUAGE_VI:
                MultiLanguageUtils.changeLanguage(context, MultiLanguageUtils.LANGUAGE_VI);
                break;
            case MultiLanguageUtils.LANGUAGE_IN:
                MultiLanguageUtils.changeLanguage(context, MultiLanguageUtils.LANGUAGE_IN);
                break;
            case MultiLanguageUtils.LANGUAGE_MS:
                MultiLanguageUtils.changeLanguage(context, MultiLanguageUtils.LANGUAGE_MS);
                break;
            case MultiLanguageUtils.LANGUAGE_FA:
                MultiLanguageUtils.changeLanguage(context, MultiLanguageUtils.LANGUAGE_FA);
                break;
        }
        //关闭应用所有Activity

        while (!(ActivityManager.getInstance().getTopActivity() instanceof HomeActivity)) {
            Activity activity = ActivityManager.getInstance().getTopActivity();
            if (null == activity) break;
            activity.finish();
            ActivityManager.getInstance().popActivity(activity);
        }
        Intent intent = new Intent(HomeActivity.HOME_ACTIVITY_RELOAD);
        requireActivity().sendBroadcast(intent);
    }
}