package com.jieli.healthaide.ui.health.binder_adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.chad.library.adapter.base.binder.BaseItemBinder;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.health.entity.BloodOxygenEntity;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

import org.jetbrains.annotations.NotNull;

/**
 * @ClassName: BloodOxygenBinder
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/2/19 16:37
 */
public class BloodOxygenBinder extends BaseItemBinder<BloodOxygenEntity, BaseViewHolder> {

    @Override
    public void convert(@NotNull BaseViewHolder baseViewHolder, BloodOxygenEntity bloodOxygenEntity) {
        if (bloodOxygenEntity.getBloodOxygen() != 0) {
            baseViewHolder.setText(R.id.tv_health_empty, CalendarUtil.formatString("%d%%", bloodOxygenEntity.getBloodOxygen()));
            baseViewHolder.setText(R.id.tv_health_date, CustomTimeFormatUtil.getTimeInterval(bloodOxygenEntity.getLeftTime(), 1, CalenderSelectorView.TYPE_WEEK));
        } else {
            baseViewHolder.setText(R.id.tv_health_date, getContext().getString(R.string.empty_date));
        }
    }

    @NotNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int i) {
        return new BaseViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_health_blood_oxygen, viewGroup, false));
    }
}
