package com.jieli.healthaide.ui.mine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.dao.HealthDao;
import com.jieli.healthaide.data.db.HealthDatabase;
import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.parse.ParseEntity;
import com.jieli.healthaide.data.vo.parse.WeightParserImpl;
import com.jieli.healthaide.databinding.FragmentMyTargetBinding;
import com.jieli.healthaide.tool.unit.BaseUnitConverter;
import com.jieli.healthaide.tool.unit.Converter;
import com.jieli.healthaide.tool.unit.KGUnitConverter;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.widget.rulerview.RulerView;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_health_http.model.UserInfo;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.List;

import static com.jieli.healthaide.tool.unit.BaseUnitConverter.TYPE_METRIC;

/**
 * 目标界面
 */
public class MyTargetFragment extends BaseFragment {
    private FragmentMyTargetBinding fragmentMyTargetBinding;
    private int targetStep = 0;
    private float targetWeight = 0;//公制的值 kg
    private UserInfoViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        fragmentMyTargetBinding = FragmentMyTargetBinding.inflate(inflater, container, false);
        fragmentMyTargetBinding.layoutTopbar.tvTopbarTitle.setText(R.string.target);
        fragmentMyTargetBinding.layoutTopbar.tvTopbarRight.setVisibility(View.VISIBLE);
        fragmentMyTargetBinding.layoutTopbar.tvTopbarRight.setTextColor(getResources().getColor(R.color.black_242424));
        fragmentMyTargetBinding.layoutTopbar.tvTopbarRight.setTextSize(16);
        fragmentMyTargetBinding.layoutTopbar.tvTopbarRight.setText(R.string.save);
        fragmentMyTargetBinding.layoutTopbar.tvTopbarRight.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        fragmentMyTargetBinding.layoutTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        fragmentMyTargetBinding.layoutTopbar.tvTopbarRight.setOnClickListener(view -> save());
        Converter converter = new KGUnitConverter().getConverter(BaseUnitConverter.getType());
        fragmentMyTargetBinding.scaleViewWeight.setMinValueAndMaxValue((int) converter.value(10) * 10, (int) converter.value(250) * 10);
        fragmentMyTargetBinding.tvTargetWeightUnit.setText(converter.unit());
        fragmentMyTargetBinding.tvTargetWeightValue.setText(CalendarUtil.formatString("%.1f", converter.value(10)));
        fragmentMyTargetBinding.scaleViewStep.setOnValueChangeListener(new RulerView.OnValueChangeListener() {
            @Override
            public void onChange(int value) {
                refreshTargetStepUI(value / 10);
            }

            @Override
            public void onActionUp(int value) {
                refreshTargetStepUI(value / 10);
                targetStep = value / 10;
            }

            @Override
            public void onFling(int value, boolean isFlingEnd) {
                refreshTargetStepUI(value / 10);
                targetStep = value / 10;
            }

            @Override
            public void onCanNotSlide() {

            }
        });
        fragmentMyTargetBinding.scaleViewWeight.setOnValueChangeListener(new RulerView.OnValueChangeListener() {
            @Override
            public void onChange(int value) {
                float weight = ((float) value) / ((float) 10);
                fragmentMyTargetBinding.tvTargetWeightValue.setText(CalendarUtil.formatString("%.1f", weight));
            }

            @Override
            public void onActionUp(int value) {
                float weight = ((float) value) / ((float) 10);
                fragmentMyTargetBinding.tvTargetWeightValue.setText(CalendarUtil.formatString("%.1f", weight));
                targetWeight = (float) converter.origin(weight);
            }

            @Override
            public void onFling(int value, boolean isFlingEnd) {
                float weight = ((float) value) / ((float) 10);
                fragmentMyTargetBinding.tvTargetWeightValue.setText(CalendarUtil.formatString("%.1f", weight));
                targetWeight = (float) converter.origin(weight);
            }

            @Override
            public void onCanNotSlide() {

            }
        });
        return fragmentMyTargetBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UserInfoViewModel.class);
        viewModel.userInfoLiveData.observe(getViewLifecycleOwner(), this::updateUserInfoView);
        viewModel.httpStateLiveData.observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case UserInfoViewModel.HTTP_STATE_REQUESTING:
                case UserInfoViewModel.HTTP_STATE_UPDATING:
                    showWaitDialog();
                    break;
                case UserInfoViewModel.HTTP_STATE_UPDATED_FINISH:
                    dismissWaitDialog();
                    requireActivity().onBackPressed();
                    break;
                case UserInfoViewModel.HTTP_STATE_UPDATED_ERROR:
                    dismissWaitDialog();
                    showTips(R.string.network_error_tip);
                    break;
                default:
                    dismissWaitDialog();
                    break;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Converter converter = new KGUnitConverter().getConverter(BaseUnitConverter.getType());
        fragmentMyTargetBinding.scaleViewWeight.setCurrentPosition((int) converter.value(10) * 10);
        viewModel.getUserInfo();
    }

    private void save() {
        UserInfo userInfo = viewModel.copyUserInfo();
        if (userInfo == null) userInfo = new UserInfo();
        if (targetStep != 0 && targetStep != userInfo.getStep()) {
            userInfo.setStep(targetStep);
        }
        if (targetWeight != 0 && targetWeight != userInfo.getWeightTarget()) {
            userInfo.setWeightTarget(targetWeight);
        }
        //todo 获取数据库中体重的最新值
        float startWeight = 0f;
        HealthDao healthDao = HealthDatabase.buildHealthDb(HealthApplication.getAppViewModel().getApplication()).HealthDao();
        String uid = HealthApplication.getAppViewModel().getUid();
        HealthEntity lastEntity = healthDao.getHealthLiveDataLast(HealthEntity.DATA_TYPE_WEIGHT, uid);
        if (lastEntity != null) {
            WeightParserImpl parser = new WeightParserImpl();
            List<HealthEntity> list = new ArrayList<>();
            list.add(lastEntity);
            List<ParseEntity> parseEntities = parser.parse(list);
            if (!parseEntities.isEmpty()) {
                startWeight = (float) parseEntities.get(0).getValue();
            }
        }
        if (startWeight == 0f) {
            startWeight = userInfo.getWeight();
        }
        userInfo.setWeightStart(startWeight);
        viewModel.updateUserInfo(userInfo);
    }

    private void refreshTargetStepUI(int step) {
        UserInfo userInfo = viewModel.userInfoLiveData.getValue();
        if (userInfo == null) return;
        float height = userInfo.getHeight() == 0 ? 170 : userInfo.getHeight();//cm
        float weight = userInfo.getWeight() == 0 ? 60 : userInfo.getWeight();//kg
        int stepRate = 90;//步/min
        int duration = step / stepRate;//min
        int consume = (int) (0.43 * height + 0.57 * weight + 0.26 * stepRate + 0.92 * duration - 108.44);
        fragmentMyTargetBinding.tvConsume.setText(getString(R.string.target_consume, consume));
        fragmentMyTargetBinding.tvTargetStepValue.setText(String.valueOf(step));
    }

    private void updateUserInfoView(UserInfo userInfo) {
        JL_Log.d(tag, "updateUserInfoView", "userInfo : " + userInfo);
        Converter converter = new KGUnitConverter().getConverter(BaseUnitConverter.getType());
        if (userInfo.getStep() > 0) {
            targetStep = userInfo.getStep();
            fragmentMyTargetBinding.scaleViewStep.setCurrentPosition(targetStep * 10);
        } else {
            fragmentMyTargetBinding.scaleViewStep.setCurrentPosition(2000 * 10);
        }
        if (userInfo.getWeightTarget() > 0) {
            targetWeight = userInfo.getWeightTarget();
            double converterValue = converter.value(targetWeight);
            converterValue = BaseUnitConverter.getType() == TYPE_METRIC ? formatKg(converterValue) : formatPound(converterValue);
            converterValue = ((double) Math.round(converterValue * 10)) / 10;
            fragmentMyTargetBinding.tvTargetWeightValue.setText(CalendarUtil.formatString("%.1f", converterValue));
            fragmentMyTargetBinding.scaleViewWeight.setCurrentPosition((int) (converterValue * 10));
        }
    }

    private double formatKg(double kg) {
        int minKG = 10;
        int maxKG = 250;
        kg = Math.max(minKG, kg);
        kg = Math.min(kg, maxKG);
        return kg;
    }

    private double formatPound(double pound) {
        int minPound = 22;
        int maxPound = 551;
        pound = Math.max(minPound, pound);
        pound = Math.min(pound, maxPound);
        return pound;
    }
}