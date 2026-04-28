package com.jieli.healthaide.ui.health.sleep.adapter;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.health.sleep.entity.StatisticsEntity;

import org.jetbrains.annotations.NotNull;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/4/21 11:50 AM
 * @desc :
 */
public class StatisticsAdapter extends BaseMultiItemQuickAdapter<StatisticsEntity, BaseViewHolder> {


    public StatisticsAdapter() {
        addItemType(StatisticsEntity.TYPE_TIME, R.layout.item_sleep_stataistic_data_time);
        addItemType(StatisticsEntity.TYPE_SCORE, R.layout.item_sleep_stataistic_data_score);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, StatisticsEntity statisticsEntity) {
        if (statisticsEntity.getItemType() == StatisticsEntity.TYPE_TIME) {
            baseViewHolder.setText(R.id.tv_sleep_time_hour, statisticsEntity.hour + "");
            baseViewHolder.setText(R.id.tv_sleep_time_min, statisticsEntity.min + "");
            baseViewHolder.setText(R.id.tv_sleep_status, statisticsEntity.type);
            baseViewHolder.setText(R.id.tv_sleep_result, statisticsEntity.result);
        } else {
            baseViewHolder.setText(R.id.tv_sleep_score, statisticsEntity.score + "");
            baseViewHolder.setText(R.id.tv_sleep_status, statisticsEntity.type);
            baseViewHolder.setText(R.id.tv_sleep_result, statisticsEntity.result);
            baseViewHolder.setTextColor(R.id.tv_sleep_result, getContext().getResources().getColor(statisticsEntity.typeColor));
        }
    }
}
