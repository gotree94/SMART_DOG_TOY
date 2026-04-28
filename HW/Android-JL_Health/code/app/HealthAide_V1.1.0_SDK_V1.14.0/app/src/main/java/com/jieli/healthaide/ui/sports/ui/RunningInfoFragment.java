package com.jieli.healthaide.ui.sports.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentRunningInfoBinding;
import com.jieli.healthaide.tool.unit.BaseUnitConverter;
import com.jieli.healthaide.tool.unit.Converter;
import com.jieli.healthaide.tool.unit.KMUnitConverter;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.sports.model.RunningRealData;
import com.jieli.healthaide.ui.sports.model.SportsInfo;
import com.jieli.healthaide.ui.sports.ui.set.RunningSetFragment;
import com.jieli.healthaide.ui.sports.viewmodel.SportsViewModel;
import com.jieli.healthaide.ui.sports.widget.SportsControlView;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.FormatUtil;
import com.jieli.jl_dialog.Jl_Dialog;


/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/6/21
 * @desc :
 */
public class RunningInfoFragment extends BaseFragment {
    //private final static int ANIMATOR_TIME = 200;
    private FragmentRunningInfoBinding mBinding;
    private SportsViewModel mViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentRunningInfoBinding.inflate(inflater, container, false);
        mBinding.viewTopbar.tvTopbarRight.setVisibility(View.VISIBLE);
        mBinding.viewTopbar.tvTopbarRight.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,R.drawable.run_icon_settle_nol,  0);
        mBinding.viewTopbar.tvTopbarLeft.setVisibility(View.GONE);
        mBinding.viewTopbar.tvTopbarRight.setOnClickListener(v -> ContentActivity.startContentActivity(requireContext(), RunningSetFragment.class.getCanonicalName()));


        mBinding.clSportsControl.setOnEventListener(new SportsControlView.OnEventListener() {
            @Override
            public void onResume() {
                mViewModel.resume();

            }

            @Override
            public void onStop() {
                showStopRequestDialog();
            }

            @Override
            public void onPause() {
                mViewModel.pause();

            }

            @Override
            public void onLock(boolean lock) {

            }

            @Override
            public void onMap() {
                BaseFragment baseFragment = (BaseFragment) getParentFragment();
                assert baseFragment != null;
                baseFragment.replaceFragment(R.id.fl_fragment_content, RunningWithMapFragment.class.getCanonicalName(), null);
            }
        });


        //运动信息标题设置
        initSportsInfoTitle();
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.requireActivity().getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED/*|               //这个在锁屏状态下
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON*/);
        mViewModel = new ViewModelProvider(requireActivity(),
                new SportsViewModel.ViewModelFactory(requireActivity().getApplication(), 0))
                .get(SportsViewModel.class);

        LiveData<RunningRealData> livedata =
                mViewModel.getRealDataLiveData();


        int[] realModes = new int[]{
                R.string.real_sports_status_0, R.string.real_sports_status_1,
                R.string.real_sports_status_2,
                R.string.real_sports_status_3,
                R.string.real_sports_status_4, R.string.real_sports_status_5
        };

        int[] saveModes = new int[]{
                R.string.save_sports_status_0, R.string.save_sports_status_1,
                R.string.save_sports_status_2,
                R.string.save_sports_status_3,
                R.string.save_sports_status_4, R.string.save_sports_status_5
        };

        int[] modeIcons = new int[]{
                R.drawable.dot_sports_status_dot_0,
                R.drawable.dot_sports_status_dot_1,
                R.drawable.dot_sports_status_dot_2,
                R.drawable.dot_sports_status_dot_3,
                R.drawable.dot_sports_status_dot_4,
                R.drawable.dot_sports_status_dot_5,
        };


        final int[] heartRateMode = {0x00};//心率模式
        livedata.observe(getViewLifecycleOwner(), data -> {
            Converter unitConverter = new KMUnitConverter().getConverter(BaseUnitConverter.getType());
            mBinding.tvDistance.setText(CalendarUtil.formatString("%.2f", unitConverter.value(data.distance)));
            mBinding.tvDistanceUint.setText(unitConverter.unit());

            mBinding.layoutSportsRealDataContainer.layoutSportsRealDataPace.tvSportsRealDataValue.setText(data.pace > 0 ? FormatUtil.paceFormat((long) data.pace) : "--");
            mBinding.layoutSportsRealDataContainer.layoutSportsRealDataDuration.tvSportsRealDataValue.setText(CalendarUtil.formatSeconds(data.duration));
            mBinding.layoutSportsRealDataContainer.layoutSportsRealDataKcal.tvSportsRealDataValue.setText(CalendarUtil.formatString("%.2f", data.kcal));
            mBinding.layoutSportsRealDataContainer.layoutSportsRealDataHeartRate.tvSportsRealDataValue.setText(data.heartRate > 0 ? CalendarUtil.formatString("%d", data.heartRate) : "--");
            mBinding.layoutSportsRealDataContainer.layoutSportsRealDataStep.tvSportsRealDataValue.setText(CalendarUtil.formatString("%d", data.step));
            mBinding.layoutSportsRealDataContainer.layoutSportsRealDataStride.tvSportsRealDataValue.setText(data.stepFreq > 0 ? CalendarUtil.formatString("%d", data.stepFreq) : "--");

            int modeIndex = Math.max(Math.min(data.sportsStatus, 5), 0);
            int modeName = heartRateMode[0] == 0x01 ? saveModes[modeIndex] : realModes[modeIndex];
            mBinding.layoutSportsRealDataContainer.tvSportsRealDataMode.setCompoundDrawablesWithIntrinsicBounds(modeIcons[modeIndex], 0, 0, 0);
            mBinding.layoutSportsRealDataContainer.tvSportsRealDataMode.setText(modeName);
//            mBinding.tvSpeed.setText(CalendarUtil.formatSeconds((long) data.pace));
//            mBinding.tvKcal.setText(CalendarUtil.formatString("%.2f", data.kcal));
//            mBinding.tvTime.setText(CalendarUtil.formatSeconds(data.duration));
        });


        mViewModel.getSportInfoLiveData().observe(getViewLifecycleOwner(), sportsInfo -> {
            mBinding.clSportsControl.showMapBtn(sportsInfo.useMap);
            mBinding.gpsView.setVisibility(sportsInfo.useMap ? View.VISIBLE : View.GONE);
            mBinding.viewTopbar.tvTopbarTitle.setText(sportsInfo.titleRes);
            heartRateMode[0] = sportsInfo.heartRateMode;
            if (sportsInfo.status == SportsInfo.STATUS_RESUME) {
                mBinding.clSportsControl.resume();
            } else if (sportsInfo.status == SportsInfo.STATUS_PAUSE) {
                mBinding.clSportsControl.pause();
            }
        });

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mViewModel.stop();
    }

    private void showStopRequestDialog() {
        Jl_Dialog.builder()
                .content(getString(R.string.tip_finished_exercise))
                .left(getString(R.string.keep_moving))
                .right(getString(R.string.terminate_sport))
                .leftColor(ResourcesCompat.getColor(getResources(), R.color.text_secondary_color, requireActivity().getTheme()))
                .rightColor(ResourcesCompat.getColor(getResources(), R.color.auxiliary_error, requireActivity().getTheme()))
                .leftClickListener((view, dialogFragment) -> {
                    dialogFragment.dismiss();
                    mBinding.clSportsControl.resume();
                    mViewModel.resume();
                })
                .rightClickListener((view, dialogFragment) -> {
                    dialogFragment.dismiss();
                    mViewModel.stop();
                })
                .build()
                .show(getChildFragmentManager(), "stop_running");
    }

    private void initSportsInfoTitle() {
        mBinding.layoutSportsRealDataContainer.layoutSportsRealDataPace.tvSportsRealDataTitle.setText(R.string.pace);
        mBinding.layoutSportsRealDataContainer.layoutSportsRealDataPace.ivSportsRealDataIcon.setImageResource(R.drawable.run_icon_speed_nol);
        mBinding.layoutSportsRealDataContainer.layoutSportsRealDataDuration.tvSportsRealDataTitle.setText(R.string.exercise_time);
        mBinding.layoutSportsRealDataContainer.layoutSportsRealDataDuration.ivSportsRealDataIcon.setImageResource(R.drawable.run_icon_time_nol);
        mBinding.layoutSportsRealDataContainer.layoutSportsRealDataKcal.tvSportsRealDataTitle.setText(R.string.kcal);
        mBinding.layoutSportsRealDataContainer.layoutSportsRealDataKcal.ivSportsRealDataIcon.setImageResource(R.drawable.run_icon_kcal_nol);

        mBinding.layoutSportsRealDataContainer.layoutSportsRealDataHeartRate.tvSportsRealDataTitle.setText(R.string.heart_rate_and_unit);
        mBinding.layoutSportsRealDataContainer.layoutSportsRealDataHeartRate.ivSportsRealDataIcon.setImageResource(R.drawable.run_icon_heart_nol);
        mBinding.layoutSportsRealDataContainer.layoutSportsRealDataStep.tvSportsRealDataTitle.setText(R.string.step_number_and_unit);
        mBinding.layoutSportsRealDataContainer.layoutSportsRealDataStep.ivSportsRealDataIcon.setImageResource(R.drawable.run_icon_step_nol);
        mBinding.layoutSportsRealDataContainer.layoutSportsRealDataStride.tvSportsRealDataTitle.setText(R.string.step_freq_info_title);
        mBinding.layoutSportsRealDataContainer.layoutSportsRealDataStride.ivSportsRealDataIcon.setImageResource(R.drawable.run_icon_step_frequency_nol);
    }


}
