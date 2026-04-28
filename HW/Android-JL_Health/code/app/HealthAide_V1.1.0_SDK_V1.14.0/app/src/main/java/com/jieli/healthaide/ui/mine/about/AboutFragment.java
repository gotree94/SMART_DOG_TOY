package com.jieli.healthaide.ui.mine.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.component.utils.SystemUtil;
import com.jieli.healthaide.BuildConfig;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentAboutBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.test.TestConfigurationFragment;
import com.jieli.jl_rcsp.util.RcspUtil;

import java.util.Date;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/11/21 9:19 AM
 * @desc : 关于界面
 */
public class AboutFragment extends BaseFragment {

    /**
     * 是否显示反馈界面
     */
    private static final boolean SHOW_FEEDBACK = true;

    private final static long DOUBLE_CLICK_INTERVAL = 2000; //2 s

    private FragmentAboutBinding mBinding;

    private long lastClickTime = 0;


    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = FragmentAboutBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI();
    }

    private void initUI() {
        mBinding.viewTopbar.tvTopbarTitle.setText(R.string.mine_about);
        String version = getString(R.string.current_app_version, SystemUtil.getVersionName(requireContext()));
        if (BuildConfig.DEBUG) {
            version = RcspUtil.formatString("%s(%d)", version, SystemUtil.getVersion(requireContext()));
        }
        mBinding.tvAboutAppVersion.setText(version);
        mBinding.viewTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        mBinding.tvAboutPolicy.setOnClickListener(v -> WebFragment.start(requireContext(), getString(R.string.privacy_policy), getString(R.string.app_privacy_policy)));
        mBinding.tvAboutUserAgreement.setOnClickListener(v -> WebFragment.start(requireContext(), getString(R.string.user_agreement), getString(R.string.user_agreement_url)));
        mBinding.tvFeedback.setOnClickListener(v -> ContentActivity.startContentActivity(requireContext(), FeedBackFragment.class.getCanonicalName()));
        mBinding.tvIcpNumber.setOnClickListener(v -> WebFragment.start(requireContext(), getString(R.string.icp_number), "https://beian.miit.gov.cn/"));

        mBinding.groupFeedback.setVisibility(SHOW_FEEDBACK ? View.VISIBLE : View.GONE);
        mBinding.tvIcpNumber.setText(RcspUtil.formatString("%s : %s", getString(R.string.icp_info), getString(R.string.icp_number)));

        mBinding.tvAboutAppVersion.setOnClickListener(v -> {
            if (!isFastDoubleClick()) {
                showTips(getString(R.string.click_again_to_test_configuration));
                return;
            }
            ContentActivity.startContentActivity(requireContext(), TestConfigurationFragment.class.getCanonicalName());
        });
    }

    public boolean isFastDoubleClick() {
        return isFastDoubleClick(DOUBLE_CLICK_INTERVAL);
    }

    public boolean isFastDoubleClick(long interval) {
        boolean isDoubleClick = false;
        long currentTime = new Date().getTime();
        if (currentTime - lastClickTime <= interval) {
            isDoubleClick = true;
        }
        lastClickTime = currentTime;
        return isDoubleClick;
    }
}