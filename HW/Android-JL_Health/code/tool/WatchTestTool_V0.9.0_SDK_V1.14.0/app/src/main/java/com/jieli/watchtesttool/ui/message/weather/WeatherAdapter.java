package com.jieli.watchtesttool.ui.message.weather;

import android.annotation.SuppressLint;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.data.db.weather.WeatherEntity;
import com.jieli.watchtesttool.util.WeatherUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 天气适配器
 * @since 2023/1/13
 */
class WeatherAdapter extends BaseQuickAdapter<WeatherEntity, BaseViewHolder> {
    private final OnEventListener mListener;

    public WeatherAdapter(OnEventListener listener) {
        super(R.layout.item_sync_weather);
        mListener = listener;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, WeatherEntity weather) {
        if (null == weather) return;
        viewHolder.setText(R.id.tv_temperature_value, String.format(Locale.getDefault(), "%d℃", weather.getTemperature()));
        viewHolder.setText(R.id.tv_humidity_value, String.format(Locale.getDefault(), "%d%%", weather.getHumidity()));
        viewHolder.setText(R.id.tv_wind_direction, WeatherUtil.getWindDesc(getContext(), weather.getWindDirection()));
        viewHolder.setVisible(R.id.tv_wind_power, weather.getWindPower() > 0);
        viewHolder.setText(R.id.tv_wind_power, String.format(Locale.getDefault(), "%d%s", weather.getWindPower(), getContext().getString(R.string.wind_power_level)));
        viewHolder.setText(R.id.tv_weather, WeatherUtil.getWeatherDesc(getContext(), weather.getWeather()));
        viewHolder.setText(R.id.tv_province, weather.getProvince());
        viewHolder.setText(R.id.tv_city, weather.getCity());
        viewHolder.setText(R.id.tv_update_time, obtainTimeFormat(weather.getTime()));

        int position = getItemPosition(weather);
        ImageView ivSendBtn = viewHolder.getView(R.id.iv_send_btn);
        ivSendBtn.setTag(position);
        ivSendBtn.setOnClickListener(v -> {
            if (v.getTag() instanceof Integer) {
                int pos = (int) v.getTag();
                WeatherEntity entity = getItem(pos);
                if (null != mListener) {
                    mListener.onSend(pos, entity);
                }
            }
        });
        ImageView ivDeleteBtn = viewHolder.getView(R.id.iv_delete_btn);
        ivDeleteBtn.setTag(position);
        ivDeleteBtn.setOnClickListener(v -> {
            if (v.getTag() instanceof Integer) {
                int pos = (int) v.getTag();
                WeatherEntity entity = getItem(pos);
                if (null != mListener) {
                    mListener.onDelete(pos, entity);
                }
            }
        });
    }

    @NonNull
    private String obtainTimeFormat(long time) {
        @SuppressLint("SimpleDateFormat") final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return format.format(calendar.getTime());
    }

    public interface OnEventListener {

        void onSend(int position, WeatherEntity weather);

        void onDelete(int position, WeatherEntity weather);
    }
}
