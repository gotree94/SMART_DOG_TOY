package com.jieli.healthaide.ui.sports.ui;

import static com.jieli.healthaide.ui.sports.ui.RunningDetailFragment.KEY_RECORD_START_TIME;

import android.app.Application;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.component.utils.HandlerManager;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.entity.SportRecord;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.sports.model.RequestRecordState;
import com.jieli.healthaide.ui.sports.model.SportsInfo;
import com.jieli.healthaide.ui.sports.viewmodel.SportsViewModel;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/6/21
 * @desc :
 */
public class RunningParentFragment extends BaseFragment {
    public static final String KEY_RUNNING_TYPE = "RUNNING_TYPE";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_running_parent, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int sportsType = SportRecord.TYPE_OUTDOOR;
        if (getArguments() != null) {
            sportsType = getArguments().getInt(KEY_RUNNING_TYPE, -1);
        }

        Application application = requireActivity().getApplication();
        SportsViewModel mViewModel = new ViewModelProvider(requireActivity(), new SportsViewModel.ViewModelFactory(application, sportsType)).get(SportsViewModel.class);
        replaceFragment(R.id.fl_fragment_content, RunningInfoFragment.class.getCanonicalName(), null);
        mViewModel.start();
        BaseActivity activity = (BaseActivity) requireActivity();
        activity.setOnBackPressIntercept(() -> {
            showTips(R.string.please_click_finish_btn);
            return true;
        });

        mViewModel.getSportInfoLiveData().observe(getViewLifecycleOwner(), sportsInfo -> {
            if (sportsInfo.status == SportsInfo.STATUS_FAILED) {
                JL_Log.e(tag, "SportInfoLiveData", "运动失败，关闭运动页面-->");
                finish();
            }
        });

        mViewModel.getRequestRecordLiveData().observe(getViewLifecycleOwner(), status -> {
            switch (status.status) {
                case RequestRecordState.REQUEST_RECORD_STATE_SUCCESS:
                    Bundle args = new Bundle();
                    args.putLong(KEY_RECORD_START_TIME, status.startTime);
                    HandlerManager.getInstance().getMainHandler().postDelayed(() -> {
                        String targetName = status.type == SportsInfo.TYPE_OUTDOOR ? RunningDetailWithMapFragment.class.getCanonicalName() : RunningDetailFragment.class.getCanonicalName();
                        ContentActivity.startContentActivity(requireActivity(), targetName, args);
                        requireActivity().finish();
                    }, 1000);
                    break;
                case RequestRecordState.REQUEST_RECORD_STATE_FAILED:
                    finish();
                    break;
                default:
                    break;
            }

        });
    }
}
