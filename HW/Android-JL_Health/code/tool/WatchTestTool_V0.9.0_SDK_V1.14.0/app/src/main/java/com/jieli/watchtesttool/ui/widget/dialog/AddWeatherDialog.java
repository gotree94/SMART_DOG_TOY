package com.jieli.watchtesttool.ui.widget.dialog;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.data.bean.SettingItem;
import com.jieli.watchtesttool.data.db.weather.WeatherEntity;
import com.jieli.watchtesttool.databinding.DialogAddWeatherBinding;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothViewModel;
import com.jieli.watchtesttool.ui.base.BaseDialogFragment;
import com.jieli.watchtesttool.util.AppUtil;
import com.jieli.watchtesttool.util.WeatherUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 添加天气信息对话框
 * @since 2023/1/29
 */
public class AddWeatherDialog extends BaseDialogFragment {
    private final OnResultListener mListener;
    private DialogAddWeatherBinding mBinding;
    private BluetoothViewModel mViewModel;

    public AddWeatherDialog(OnResultListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requireDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = requireDialog().getWindow();
        if (window != null) {
            //去掉dialog默认的padding
            window.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = Math.round(0.9f * getScreenWidth());
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.CENTER;
            //设置dialog的动画
//                lp.windowAnimations = R.style.BottomToTopAnim;
            window.setAttributes(lp);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        mBinding = DialogAddWeatherBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(BluetoothViewModel.class);
        setCancelable(false);
        if (!mViewModel.isConnected()) {
            dismiss();
            return;
        }
        initUI();
        addObserver();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewModel.destroy();
    }

    private void initUI() {
        List<SettingItem> list = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            list.add(new SettingItem(i, WeatherUtil.getWindDesc(requireContext(), i)));
        }
        SettingAdapter windAdapter = new SettingAdapter(requireContext(), list);
        mBinding.spWindDirection.setAdapter(windAdapter);
        mBinding.spWindDirection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SettingAdapter adapter = (SettingAdapter) parent.getAdapter();
                SettingItem item = adapter.getItem(position);
                if (null == item) return;
                adapter.updateSelectedId(item.getId());
                mBinding.spWindDirection.setSelection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        list = new ArrayList<>();
        for (int i = 0; i < 38; i++) {
            list.add(new SettingItem(i, WeatherUtil.getWeatherDesc(requireContext(), i)));
        }
        SettingAdapter weatherAdapter = new SettingAdapter(requireContext(), list);
        mBinding.spWeather.setAdapter(weatherAdapter);
        mBinding.spWeather.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SettingAdapter adapter = (SettingAdapter) parent.getAdapter();
                SettingItem item = adapter.getItem(position);
                if (null == item) return;
                adapter.updateSelectedId(item.getId());
                mBinding.spWeather.setSelection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mBinding.btnCancel.setOnClickListener(v -> dismiss());
        mBinding.btnConfirm.setOnClickListener(v -> confirmResult());
        mBinding.etUpdateTime.setHint(String.format(Locale.getDefault(), "%s: 2022-12-14", getString(R.string.hint_time_format)));
    }

    private void addObserver() {
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), connection -> {
            if (connection.getStatus() != StateCode.CONNECTION_OK) {
                dismiss();
            }
        });
    }

    private void confirmResult() {
        if (null == mViewModel.getConnectedDevice()) return;
        int temperature = AppUtil.getTextValue(mBinding.etTemperatureValue);
        if (temperature < -125 || temperature > 125) {
            mBinding.etTemperatureValue.setError(String.format(Locale.getDefault(), "%s [%d, %d]",
                    getString(R.string.input_value_err), -125, 125));
            return;
        }
        int humidity = AppUtil.getTextValue(mBinding.etHumidityValue);
        if (humidity < 0 || humidity > 100) {
            mBinding.etHumidityValue.setError(String.format(Locale.getDefault(), "%s [%d, %d]",
                    getString(R.string.input_value_err), 0, 100));
            return;
        }
        int windPower = AppUtil.getTextValue(mBinding.etWindPower);
        if (windPower < 0 || windPower > 16) {
            mBinding.etWindPower.setError(String.format(Locale.getDefault(), "%s [%d, %d]",
                    getString(R.string.input_value_err), 0, 16));
            return;
        }
        String timeText = mBinding.etUpdateTime.getText().toString().trim();
        long time = convertToTime(timeText);
        if (time == 0) {
            mBinding.etUpdateTime.setText("");
            mBinding.etUpdateTime.setError(getString(R.string.input_correct_format));
            return;
        }
        String province = mBinding.etProvince.getText().toString().trim();
        if (TextUtils.isEmpty(province)) {
            mBinding.etProvince.setError(getString(R.string.input_data_err));
            return;
        }
        String city = mBinding.etCity.getText().toString().trim();
        if (TextUtils.isEmpty(city)) {
            mBinding.etCity.setError(getString(R.string.input_data_err));
            return;
        }
        int weatherCode = ((SettingItem) mBinding.spWeather.getSelectedItem()).getId();
        int windDirection = ((SettingItem) mBinding.spWindDirection.getSelectedItem()).getId();
        WeatherEntity weather = new WeatherEntity();
        weather.setMac(mViewModel.getConnectedDevice().getAddress());
        weather.setProvince(province);
        weather.setCity(city);
        weather.setWeather(weatherCode);
        weather.setWindDirection(windDirection);
        weather.setWindPower(windPower);
        weather.setTemperature(temperature);
        weather.setHumidity(humidity);
        weather.setTime(time);
        if (mListener != null) {
            mListener.onResult(this, weather);
        }
    }

    private long convertToTime(String text) {
        if (TextUtils.isEmpty(text)) return Calendar.getInstance().getTimeInMillis();
        long time = 0;
        @SuppressLint("SimpleDateFormat") final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = dateFormat.parse(text);
            if (null != date) time = date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }

    public interface OnResultListener {

        void onResult(DialogFragment dialog, WeatherEntity weather);
    }
}
