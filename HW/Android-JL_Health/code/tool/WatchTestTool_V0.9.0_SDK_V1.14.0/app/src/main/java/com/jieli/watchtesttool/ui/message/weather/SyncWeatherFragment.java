package com.jieli.watchtesttool.ui.message.weather;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jieli.component.utils.ToastUtil;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.model.command.watch.PushInfoDataToDeviceCmd;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.data.db.weather.WeatherEntity;
import com.jieli.watchtesttool.databinding.FragmentSyncWeatherBinding;
import com.jieli.watchtesttool.tool.test.LogDialog;
import com.jieli.watchtesttool.tool.test.message.SyncWeatherTask;
import com.jieli.watchtesttool.ui.base.BaseFragment;
import com.jieli.watchtesttool.ui.widget.dialog.AddWeatherDialog;
import com.jieli.watchtesttool.util.AppUtil;

import java.util.List;
import java.util.Locale;

/**
 * 同步天气信息
 */
public class SyncWeatherFragment extends BaseFragment {
    private FragmentSyncWeatherBinding mBinding;
    private WeatherAdapter mAdapter;
    private SyncWeatherViewModel mViewModel;

    private final Handler mUIHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentSyncWeatherBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SyncWeatherViewModel.class);
        initUI();
        addObserver();
        mViewModel.queryWeatherMessages();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewModel.destroy();
    }

    private void initUI() {
        mBinding.viewWeatherTopbar.tvTopbarTitle.setText(getString(R.string.func_weather_sync));
        mBinding.viewWeatherTopbar.tvTopbarLeft.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_back_black, 0, 0, 0);
        mBinding.viewWeatherTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().finish());
        mBinding.btnWeatherAutoTest.setOnClickListener(v -> {
            int testCount = AppUtil.getTextValue(mBinding.etTestCount, 1);
            if (testCount <= 0 || testCount > 999) {
                mBinding.etTestCount.setError(String.format(Locale.getDefault(), "%s [%d, %d]",
                        getString(R.string.input_value_err), 1, 999));
                return;
            }
            List<WeatherEntity> entities = mAdapter.getData();
            if (entities.isEmpty()) {
                ToastUtil.showToastShort(getString(R.string.add_weather));
                return;
            }
            PushInfoDataToDeviceCmd.Weather[] weathers = new PushInfoDataToDeviceCmd.Weather[entities.size()];
            for (int i = 0; i < weathers.length; i++) {
                weathers[i] = entities.get(i).convertData();
            }
            SyncWeatherTask task = new SyncWeatherTask(mViewModel.getWatchManager(), testCount, weathers);
            startTaskWithDialog(task);
        });
        mBinding.rvWeatherInfo.setLayoutManager(new LinearLayoutManager(requireContext()));
        mAdapter = new WeatherAdapter(new WeatherAdapter.OnEventListener() {
            @Override
            public void onSend(int position, WeatherEntity weather) {
                SyncWeatherTask task = new SyncWeatherTask(mViewModel.getWatchManager(), weather.convertData());
                startTaskWithDialog(task);
            }

            @Override
            public void onDelete(int position, WeatherEntity weather) {
                mViewModel.deleteWeather(weather);
            }
        });
        mBinding.rvWeatherInfo.setAdapter(mAdapter);
        View footerView = LayoutInflater.from(requireContext()).inflate(R.layout.view_add_item, mBinding.rvWeatherInfo, false);
        footerView.setOnClickListener(v -> {
            AddWeatherDialog dialog = null;
            Fragment fragment = getChildFragmentManager().findFragmentByTag(AddWeatherDialog.class.getSimpleName());
            if (fragment instanceof AddWeatherDialog) {
                dialog = (AddWeatherDialog) fragment;
            }
            if (null == dialog) {
                dialog = new AddWeatherDialog((dialog1, weather) -> {
                    if (!mViewModel.insertWeather(weather)) {
                        ToastUtil.showToastShort(getString(R.string.repeat_info));
                    }
                    dialog1.dismiss();
                });
            }
            if (!dialog.isShow()) {
                dialog.show(getChildFragmentManager(), AddWeatherDialog.class.getSimpleName());
            }
        });
        mAdapter.addFooterView(footerView);
        mAdapter.setFooterWithEmptyEnable(true);
    }

    private void addObserver() {
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), connection -> {
            if (connection.getStatus() != StateCode.CONNECTION_OK) {
                requireActivity().finish();
            }
        });
        mViewModel.weathersMLD.observe(getViewLifecycleOwner(), weathers -> mAdapter.setList(weathers));
    }

    private void startTaskWithDialog(final SyncWeatherTask task) {
        if (null == task) return;
        final LogDialog dialog = new LogDialog(task, v -> task.stopTest());
        dialog.show(getChildFragmentManager(), LogDialog.class.getSimpleName());
        mUIHandler.postDelayed(task::startTest, 500);
    }
}