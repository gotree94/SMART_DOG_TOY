package com.jieli.healthaide.ui.health.binder_adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.binder.BaseItemBinder;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.health.entity.WeightEntity;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

import org.jetbrains.annotations.NotNull;

import static com.jieli.healthaide.tool.unit.BaseUnitConverter.TYPE_METRIC;

/**
 * @ClassName: BloodOxygenBinder
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/2/19 16:37
 */
public class WeightBinder extends BaseItemBinder<WeightEntity, BaseViewHolder> {
    @Override
    public void convert(@NotNull BaseViewHolder baseViewHolder, WeightEntity weightEntity) {
        baseViewHolder.setText(R.id.tv_unit, weightEntity.getUnitType() == TYPE_METRIC ? R.string.unit_kg : R.string.unit_lb);
        if (weightEntity.getWeight() != 0) {
            baseViewHolder.setText(R.id.tv_health_empty, CalendarUtil.formatString("%.1f", weightEntity.getWeight()));
            baseViewHolder.setText(R.id.tv_health_date, CustomTimeFormatUtil.getTimeInterval(weightEntity.getLeftTime(), 1, CalenderSelectorView.TYPE_WEEK));
        }else {
            baseViewHolder.setText(R.id.tv_health_date, getContext().getString(R.string.empty_date));
        }
    }

    @NotNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_health_weight, viewGroup, false);
        return new BaseViewHolder(view);
    }
}
