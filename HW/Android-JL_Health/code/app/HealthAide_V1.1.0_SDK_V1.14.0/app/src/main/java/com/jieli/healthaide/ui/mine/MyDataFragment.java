package com.jieli.healthaide.ui.mine;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;

import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentMyDataBinding;
import com.jieli.healthaide.tool.unit.BaseUnitConverter;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.health.BloodOxygenFragment;
import com.jieli.healthaide.ui.health.HealthPreviewViewModel;
import com.jieli.healthaide.ui.health.HeartRateFragment;
import com.jieli.healthaide.ui.health.SleepFragment;
import com.jieli.healthaide.ui.health.StepFragment;
import com.jieli.healthaide.ui.health.WeightFragment;
import com.jieli.healthaide.ui.mine.entries.CommonItem;
import com.jieli.healthaide.util.CalendarUtil;

import java.util.ArrayList;
import java.util.List;

import static com.jieli.healthaide.tool.unit.BaseUnitConverter.TYPE_METRIC;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/10/21 9:44 AM
 * @desc :
 */
public class MyDataFragment extends CommonFragment {

    private CommonAdapter healthAdapter;
    private CommonAdapter activityStatisticsAdapter;


    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        com.jieli.healthaide.databinding.FragmentMyDataBinding binding = FragmentMyDataBinding.inflate(inflater, container, false);

        binding.layoutTopbar.tvTopbarTitle.setText(R.string.mine_my_data);
        activityStatisticsAdapter = new CommonAdapter();
        binding.rvActivityStatistics.setAdapter(activityStatisticsAdapter);
        binding.rvActivityStatistics.setLayoutManager(new LinearLayoutManager(requireContext()));
        DividerItemDecoration decoration = new DividerItemDecoration(requireContext(), OrientationHelper.VERTICAL);

        decoration.setDrawable(getResources().getDrawable(R.drawable.line_gray_1dp));
        binding.rvActivityStatistics.addItemDecoration(decoration);

        healthAdapter = new CommonAdapter();
        binding.rvHealth.setAdapter(healthAdapter);
        binding.rvHealth.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.layoutTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());


        OnItemClickListener listener = (adapter, view, position) -> {
            CommonItem commonItem = (CommonItem) adapter.getItem(position);
            if (TextUtils.isEmpty(commonItem.getNextFragmentName())) return;
            Activity activity = requireActivity();
            ContentActivity.startContentActivity(activity, commonItem.getNextFragmentName());
        };
        healthAdapter.setOnItemClickListener(listener);
        activityStatisticsAdapter.setOnItemClickListener(listener);
        healthAdapter.setList(getHealthData());
        activityStatisticsAdapter.setList(getActivityStatisticsData());
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        HealthPreviewViewModel mViewModel = new ViewModelProvider(this, new HealthPreviewViewModel.HealthPreviewViewModelFactory(requireActivity())).get(HealthPreviewViewModel.class);
        mViewModel.getStepEntityMutableLiveData().observe(getViewLifecycleOwner(), stepEntity -> {//方便后期增加 爬楼，所以不使用setList()
            List<CommonItem> list = activityStatisticsAdapter.getData();
            CommonItem commonItem1 = list.get(0);
            commonItem1.setTailString(Html.fromHtml(toHtmlString(stepEntity.getSteps(), R.string.step)));
            CommonItem commonItem2 = list.get(1);
            commonItem2.setTailString(Html.fromHtml(toHtmlString(stepEntity.getDistance(),BaseUnitConverter.getType() == TYPE_METRIC ? R.string.km : R.string.distance_mile)));
            CommonItem commonItem3 = list.get(2);
            commonItem3.setTailString(Html.fromHtml(toHtmlString(stepEntity.getHeatQuantity(), R.string.kcal)));
            activityStatisticsAdapter.setData(0, commonItem1);
            activityStatisticsAdapter.setData(1, commonItem2);
            activityStatisticsAdapter.setData(2, commonItem3);
        });
        mViewModel.getHeartRateEntityMutableLiveData().observe(getViewLifecycleOwner(), heartRateEntity -> {
            List<CommonItem> list = healthAdapter.getData();
            CommonItem commonItem = list.get(0);
            commonItem.setTailString(Html.fromHtml(toHtmlString(heartRateEntity.getLastHeartBeat(), R.string.times_per_minute)));
            healthAdapter.setData(0, commonItem);
        });
        mViewModel.getWeightEntityMutableLiveData().observe(getViewLifecycleOwner(), weightEntity -> {
            List<CommonItem> list = healthAdapter.getData();
            CommonItem commonItem = list.get(1);
            commonItem.setTailString(Html.fromHtml(toHtmlString(weightEntity.getWeight(), BaseUnitConverter.getType() == TYPE_METRIC ? R.string.unit_kg : R.string.unit_lb)));
            healthAdapter.setData(1, commonItem);
        });
        mViewModel.getBloodOxygenEntityMutableLiveData().observe(getViewLifecycleOwner(), bloodOxygenEntity -> {
            List<CommonItem> list = healthAdapter.getData();
            CommonItem commonItem = list.get(2);
            commonItem.setTailString(Html.fromHtml(toHtmlString(bloodOxygenEntity.getBloodOxygen(), R.string.percent)));
            healthAdapter.setData(2, commonItem);
        });

        mViewModel.getSleepEntityMutableLiveData().observe(getViewLifecycleOwner(), sleepEntity -> {
            List<CommonItem> list = healthAdapter.getData();
            CommonItem commonItem = list.get(3);
            commonItem.setTailString(Html.fromHtml(toHtmlString(sleepEntity.getHour(), R.string.hour) + toHtmlString(sleepEntity.getMin(), R.string.minute)));
            healthAdapter.setData(3, commonItem);
        });
        mViewModel.readMyData();
    }

    private List<CommonItem> getActivityStatisticsData() {
        List<CommonItem> list = new ArrayList<>();

        CommonItem step = new CommonItem();
        step.setTitle(getString(R.string.step_number));
        step.setLeftImg(R.drawable.ic_my_data_step_nol);
        step.setTailString(Html.fromHtml(toHtmlString(0, R.string.step)));
        step.setNextFragmentName(StepFragment.class.getCanonicalName());
        step.setShowNext(true);
        list.add(step);

        CommonItem distance = new CommonItem();
        distance.setTitle(getString(R.string.distance));
        distance.setLeftImg(R.drawable.ic_my_data_distance_nol);
        distance.setTailString(Html.fromHtml(toHtmlString(0.0f, BaseUnitConverter.getType() == TYPE_METRIC ? R.string.km : R.string.distance_mile)));
        distance.setNextFragmentName(StepFragment.class.getCanonicalName());
        distance.setShowNext(true);
        list.add(distance);

//        CommonItem climb = new CommonItem();
//        climb.setTitle(getString(R.string.climb));
//        climb.setLeftImg(R.drawable.ic_my_data_height_nol);
//        climb.setTailString(toHtml(12, R.string.distance_m));
//        climb.setShowNext(true);
//        list.add(climb);

        CommonItem heat = new CommonItem();
        heat.setTitle(getString(R.string.heat));
        heat.setLeftImg(R.drawable.ic_my_data_energy_nol);
        heat.setTailString(Html.fromHtml(toHtmlString(0, R.string.kcal)));
        heat.setShowNext(true);
        heat.setNextFragmentName(StepFragment.class.getCanonicalName());
        list.add(heat);
        return list;
    }

    @SuppressLint("DefaultLocale")
    private List<CommonItem> getHealthData() {
        List<CommonItem> list = new ArrayList<>();
        CommonItem heartRate = new CommonItem();
        heartRate.setTitle(getString(R.string.heart_rate));
        heartRate.setLeftImg(R.drawable.ic_my_data_heart_nol);
        heartRate.setTailString(Html.fromHtml(toHtmlString(0, R.string.times_per_minute)));
        heartRate.setNextFragmentName(HeartRateFragment.class.getCanonicalName());
        heartRate.setShowNext(true);
        list.add(heartRate);

        CommonItem weight = new CommonItem();
        weight.setTitle(getString(R.string.weight));
        weight.setLeftImg(R.drawable.ic_my_data_weight_nol);
        weight.setTailString(Html.fromHtml(toHtmlString(0.0f, BaseUnitConverter.getType() == TYPE_METRIC ? R.string.unit_kg : R.string.unit_lb)));
        weight.setNextFragmentName(WeightFragment.class.getCanonicalName());
        weight.setShowNext(true);
        list.add(weight);

//        CommonItem press = new CommonItem();
//        press.setTitle(getString(R.string.pressure));
//        press.setLeftImg(R.drawable.ic_my_data_press_nol);
//        press.setTailString(toHtml(12, R.string.relax));
//        press.setShowNext(true);
//        list.add(press);


        CommonItem oxy = new CommonItem();
        oxy.setTitle(getString(R.string.blood_oxygen_saturation));
        oxy.setLeftImg(R.drawable.ic_my_data_spo_nol);
        oxy.setNextFragmentName(BloodOxygenFragment.class.getCanonicalName());
        oxy.setTailString(Html.fromHtml(toHtmlString(0, R.string.percent)));
        oxy.setShowNext(true);
        list.add(oxy);


//        String sleepString = CalendarUtil.formatString("<font color='#242424'><big>%d</big></font>   <font color='#919191'>小时</font> <font color='#242424'><big>%d</big></font>  <font color='#919191'>分钟</font>", 12, 34);

        CommonItem sleep = new CommonItem();
        sleep.setTitle(getString(R.string.sleep));
        sleep.setLeftImg(R.drawable.ic_my_data_sleep_nol);
        sleep.setNextFragmentName(SleepFragment.class.getCanonicalName());
        sleep.setTailString(Html.fromHtml(toHtmlString(0, R.string.hour) + toHtmlString(0, R.string.minute)));
        sleep.setShowNext(true);
        list.add(sleep);

        return list;
    }



    private String toHtmlString(float value, int unitRes) {
        return CalendarUtil.formatString("<font color='#242424'><big><big>%.1f</big></big></font>  <font color='#919191'>%s</font> ", value, getString(unitRes));
    }

    private String toHtmlString(int value, int unitRes) {
        return CalendarUtil.formatString("<font  color='#242424'><big><big>%d</big></big></font>  <font color='#919191'>%s</font> ", value, getString(unitRes));
    }


}
