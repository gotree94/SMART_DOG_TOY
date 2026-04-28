package com.jieli.healthaide.ui.health.binder_adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.chad.library.adapter.base.binder.BaseItemBinder;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.health.entity.PressureEntity;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

import org.jetbrains.annotations.NotNull;

/**
 * @ClassName: BloodOxygenBinder
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/2/19 16:37
 */
public class PressureBinder extends BaseItemBinder<PressureEntity, BaseViewHolder> {
    @Override
    public void convert(@NotNull BaseViewHolder baseViewHolder, PressureEntity pressureEntity) {
        if (pressureEntity.getPressure() != 0) {
            baseViewHolder.setText(R.id.tv_health_empty, String.valueOf(pressureEntity.getPressure()));
            baseViewHolder.setText(R.id.tv_unit, pressureEntity.getPressureStateSrc());
            baseViewHolder.setText(R.id.tv_health_date, CustomTimeFormatUtil.getTimeInterval(pressureEntity.getLeftTime(), 1, CalenderSelectorView.TYPE_WEEK));
        }else {
            baseViewHolder.setText(R.id.tv_health_date, getContext().getString(R.string.empty_date));
        }
    }

    @NotNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int i) {
        return new BaseViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_health_pressure, viewGroup, false));
    }
}
