package com.jieli.healthaide.ui.health;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.jieli.component.utils.PreferencesHelper;
import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentHealthBinding;
import com.jieli.healthaide.databinding.LayoutHealthHeaderBinding;
import com.jieli.healthaide.tool.watch.synctask.SyncTaskManager;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.health.entity.BloodOxygenEntity;
import com.jieli.healthaide.ui.health.entity.HealthMultipleEntity;
import com.jieli.healthaide.ui.health.entity.HeartRateEntity;
import com.jieli.healthaide.ui.health.entity.MovementRecordEntity;
import com.jieli.healthaide.ui.health.entity.PressureEntity;
import com.jieli.healthaide.ui.health.entity.SleepEntity;
import com.jieli.healthaide.ui.health.entity.StepEntity;
import com.jieli.healthaide.ui.health.entity.WeightEntity;
import com.jieli.healthaide.ui.mine.UserInfoViewModel;
import com.jieli.healthaide.ui.service.HealthService;
import com.jieli.healthaide.ui.sports.ui.SportsRecordFragment;
import com.jieli.healthaide.ui.widget.CommonDecoration;
import com.jieli.healthaide.util.CalendarUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.jieli.healthaide.tool.unit.BaseUnitConverter.TYPE_METRIC;
import static com.jieli.healthaide.ui.health.entity.HealthMultipleEntity.TYPE_BLOOD_OXYGEN;
import static com.jieli.healthaide.ui.health.entity.HealthMultipleEntity.TYPE_HEART_RATE;
import static com.jieli.healthaide.ui.health.entity.HealthMultipleEntity.TYPE_MOVEMENT_RECORD;
import static com.jieli.healthaide.ui.health.entity.HealthMultipleEntity.TYPE_PRESSURE;
import static com.jieli.healthaide.ui.health.entity.HealthMultipleEntity.TYPE_SLEEP;
import static com.jieli.healthaide.ui.health.entity.HealthMultipleEntity.TYPE_WEIGHT;
import static com.jieli.healthaide.util.HealthConstant.KEY_WEATHER_PUSH;

/**
 * 健康界面
 */
public class HealthFragment extends Fragment {
    private FragmentHealthBinding mHealthBinding;
    private LayoutHealthHeaderBinding layoutHealthHeaderBinding;
    private HealthBinderAdapter healthBinderAdapter;
    private HealthPreviewViewModel healthPreviewViewModel;
    private UserInfoViewModel userInfoViewModel;
    private final ArrayList<Object> healthMultipleEntities = new ArrayList(Arrays.asList(
            new MovementRecordEntity(),
            new HeartRateEntity(),
            new SleepEntity(),
            new WeightEntity(),
            /*    new PressureEntity(),*/
            new BloodOxygenEntity()));

    public HealthFragment() {
        // Required empty public constructor
    }


    public static HealthFragment newInstance() {
        return new HealthFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mHealthBinding = FragmentHealthBinding.inflate(inflater, container, false);
        return mHealthBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        userInfoViewModel = new ViewModelProvider(this).get(UserInfoViewModel.class);
        healthPreviewViewModel = new ViewModelProvider(requireActivity(), new HealthPreviewViewModel.HealthPreviewViewModelFactory(requireActivity()))
                .get(HealthPreviewViewModel.class);
        userInfoViewModel.userInfoLiveData.observe(getViewLifecycleOwner(), userInfo -> {
            if (null != userInfo && userInfo.getStep() != 0) {
                layoutHealthHeaderBinding.rvSteps.setValue(userInfo.getStep(), layoutHealthHeaderBinding.rvSteps.getValue());
            }
        });
        HealthApplication.getAppViewModel().userLoginInfoLiveData.observe(getViewLifecycleOwner(), userLoginInfo -> {
            if (userLoginInfo != null) {
                observerHeartPreviewLiveData();
            }
        });
        healthPreviewViewModel.readMyData();
        SyncTaskManager.getInstance().isSyncingLiveData.observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                mHealthBinding.smartRefreshHealth.autoRefreshAnimationOnly();
            } else {
                mHealthBinding.smartRefreshHealth.finishRefresh();
            }
        });
        boolean isSupportSync = PreferencesHelper.getSharedPreferences(requireContext()).getBoolean(KEY_WEATHER_PUSH, false);
        SyncTaskManager.getInstance().setSupportSyncWeather(isSupportSync);
    }

    @Override
    public void onResume() {
        super.onResume();
        userInfoViewModel.getUserInfo();
//        healthPreviewViewModel.queryLastDayWeightData();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHealthBinding.smartRefreshHealth.setOnRefreshListener(refreshLayout -> {
            SyncTaskManager syncTaskManager = SyncTaskManager.getInstance();
            if (syncTaskManager.isSyncingLiveData != null && syncTaskManager.isSyncingLiveData.getValue() == Boolean.TRUE)
                return;
            syncTaskManager.refreshTask();
        });
        healthBinderAdapter = new HealthBinderAdapter();
        layoutHealthHeaderBinding = LayoutHealthHeaderBinding.inflate(getLayoutInflater(), mHealthBinding.rvHealthContent, false);
        healthBinderAdapter.addHeaderView(layoutHealthHeaderBinding.getRoot());
        mHealthBinding.rvHealthContent.setAdapter(healthBinderAdapter);
        mHealthBinding.rvHealthContent.addItemDecoration(new CommonDecoration(getContext(), RecyclerView.VERTICAL, getResources().getColor(R.color.half_transparent), ValueUtil.dp2px(getContext(), 12)));
        healthBinderAdapter.setOnItemClickListener((adapter1, view1, position) -> {
            String fragmentCanonicalName;
            List<HealthMultipleEntity> dataArray = (List<HealthMultipleEntity>) adapter1.getData();
            HealthMultipleEntity healthMultipleEntity = dataArray.get(position);
            switch (healthMultipleEntity.getItemType()) {
                default:
                case TYPE_MOVEMENT_RECORD:
                    fragmentCanonicalName = SportsRecordFragment.class.getCanonicalName();
                    break;
                case TYPE_HEART_RATE:
                    fragmentCanonicalName = HeartRateFragment.class.getCanonicalName();
                    break;
                case TYPE_SLEEP:
                    fragmentCanonicalName = SleepFragment.class.getCanonicalName();
                    break;
                case TYPE_WEIGHT:
                    fragmentCanonicalName = WeightFragment.class.getCanonicalName();
                    break;
                case TYPE_PRESSURE:
                    fragmentCanonicalName = PressureFragment.class.getCanonicalName();
                    break;
                case TYPE_BLOOD_OXYGEN:
                    fragmentCanonicalName = BloodOxygenFragment.class.getCanonicalName();
                    break;
            }
            Intent intent = new Intent(getContext(), ContentActivity.class);
            intent.putExtra(ContentActivity.FRAGMENT_TAG, fragmentCanonicalName);
            startActivity(intent);
        });
        healthBinderAdapter.setNewInstance(healthMultipleEntities);
        initView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHealthBinding = null;
    }

    private void initView() {
        layoutHealthHeaderBinding.rvSteps.setOnClickListener((View.OnClickListener) view -> {
            //todo 跳转步数界面
            Intent intent = new Intent(getContext(), ContentActivity.class);
            intent.putExtra(ContentActivity.FRAGMENT_TAG, StepFragment.class.getCanonicalName());
            startActivity(intent);
        });
        layoutHealthHeaderBinding.tvDistanceUnit.setOnClickListener(view -> {
            //todo 跳转 距离界面
            Intent intent = new Intent(getContext(), ContentActivity.class);
            intent.putExtra(ContentActivity.FRAGMENT_TAG, StepFragment.class.getCanonicalName());
            startActivity(intent);
        });
        layoutHealthHeaderBinding.tvDistanceValue.setOnClickListener(view -> {
            //todo 跳转 距离界面
            Intent intent = new Intent(getContext(), ContentActivity.class);
            intent.putExtra(ContentActivity.FRAGMENT_TAG, StepFragment.class.getCanonicalName());
            startActivity(intent);
        });
        layoutHealthHeaderBinding.tvHeatQuantityUnit.setOnClickListener(view -> {
            //todo 跳转 热量界面
            Intent intent = new Intent(getContext(), ContentActivity.class);
            intent.putExtra(ContentActivity.FRAGMENT_TAG, StepFragment.class.getCanonicalName());
            startActivity(intent);
        });
        layoutHealthHeaderBinding.tvHeatQuantityValue.setOnClickListener(view -> {
            //todo 跳转 热量界面
            Intent intent = new Intent(getContext(), ContentActivity.class);
            intent.putExtra(ContentActivity.FRAGMENT_TAG, StepFragment.class.getCanonicalName());
            startActivity(intent);
        });
        layoutHealthHeaderBinding.tvClimbStairsUnit.setOnClickListener(view -> {
            //todo 跳转 爬楼界面
            Intent intent = new Intent(getContext(), ContentActivity.class);
            intent.putExtra(ContentActivity.FRAGMENT_TAG, StepFragment.class.getCanonicalName());
            startActivity(intent);
        });
        layoutHealthHeaderBinding.tvClimbStairsValue.setOnClickListener(view -> {
            //todo 跳转 爬楼界面
            Intent intent = new Intent(getContext(), ContentActivity.class);
            intent.putExtra(ContentActivity.FRAGMENT_TAG, StepFragment.class.getCanonicalName());
            startActivity(intent);
        });
    }

    private void updateLayoutHealthHeader(StepEntity stepEntity) {
        layoutHealthHeaderBinding.tvTodayStepValue.setText(CalendarUtil.formatString("%d", stepEntity.getSteps()));
        layoutHealthHeaderBinding.tvDistanceValue.setText(CalendarUtil.formatString("%.2f", stepEntity.getDistance()));
        layoutHealthHeaderBinding.tvHeatQuantityValue.setText(CalendarUtil.formatString("%d", stepEntity.getHeatQuantity()));
        layoutHealthHeaderBinding.tvClimbStairsValue.setText(CalendarUtil.formatString("%.1f", stepEntity.getHeight()));
        layoutHealthHeaderBinding.rvSteps.setValue(layoutHealthHeaderBinding.rvSteps.getTargetValue(), stepEntity.getSteps());
        layoutHealthHeaderBinding.tvDistanceUnit.setText(stepEntity.getUnitType() == TYPE_METRIC ? R.string.distance_km : R.string.distance_mile);
        layoutHealthHeaderBinding.tvHeatQuantityUnit.setText(stepEntity.getUnitType() == TYPE_METRIC ? R.string.climbing_stairs_m : R.string.climbing_stairs_foot);
    }

    private void observerHeartPreviewLiveData() {
        healthPreviewViewModel.getStepEntityMutableLiveData().observe(getViewLifecycleOwner(), stepEntity -> {
            updateLayoutHealthHeader(stepEntity);
            Intent intent = new Intent(requireActivity().getApplicationContext(), HealthService.class);
            intent.setAction(HealthService.ACTION_HEALTH_RECORD);
            intent.putExtra(HealthService.EXTRA_STEP, stepEntity.getSteps());
            intent.putExtra(HealthService.EXTRA_KCAL, stepEntity.getHeatQuantity());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireActivity().getApplicationContext().startForegroundService(intent);
            } else {
                requireActivity().getApplicationContext().getApplicationContext().startService(intent);
            }
        });
        healthPreviewViewModel.getMovementRecordEntityMutableLiveData().observe(getViewLifecycleOwner(), movementRecordEntity ->
                updateHealthMultipleEntity(MovementRecordEntity.class, movementRecordEntity));
        healthPreviewViewModel.getHeartRateEntityMutableLiveData().observe(getViewLifecycleOwner(), heartRateEntity ->
                updateHealthMultipleEntity(HeartRateEntity.class, heartRateEntity));
        healthPreviewViewModel.getSleepEntityMutableLiveData().observe(getViewLifecycleOwner(), sleepEntity ->
                updateHealthMultipleEntity(SleepEntity.class, sleepEntity));
        healthPreviewViewModel.getWeightEntityMutableLiveData().observe(getViewLifecycleOwner(), weightEntity ->
                updateHealthMultipleEntity(WeightEntity.class, weightEntity));
        healthPreviewViewModel.getPressureEntityMutableLiveData().observe(getViewLifecycleOwner(), pressureEntity ->
                updateHealthMultipleEntity(PressureEntity.class, pressureEntity));
        healthPreviewViewModel.getBloodOxygenEntityMutableLiveData().observe(getViewLifecycleOwner(), bloodOxygenEntity ->
                updateHealthMultipleEntity(BloodOxygenEntity.class, bloodOxygenEntity));
    }

    private void updateHealthMultipleEntity(Class clazz, HealthMultipleEntity updateEntity) {
        List dataArray = healthBinderAdapter.getData();
        for (int i = 0; i < dataArray.size(); i++) {
            Object data = dataArray.get(i);
            if (clazz.isInstance(data)) {
                healthBinderAdapter.setData(i, updateEntity);
            }
        }
    }
}