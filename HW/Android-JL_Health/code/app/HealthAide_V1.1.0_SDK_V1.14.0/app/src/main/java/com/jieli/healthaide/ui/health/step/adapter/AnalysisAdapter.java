package com.jieli.healthaide.ui.health.step.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.health.step.entity.AnalysisEntity;

import org.jetbrains.annotations.NotNull;

/**
 * @ClassName: AnalysisAdapter
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/3/8 17:06
 */
public class AnalysisAdapter extends BaseQuickAdapter<AnalysisEntity, BaseViewHolder> {
    public AnalysisAdapter() {
        super(R.layout.item_movement_analysis);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, AnalysisEntity analysisEntity) {
        baseViewHolder.setText(R.id.tv_all_step_value, analysisEntity.getFirstAnalysisValue());
        baseViewHolder.setText(R.id.tv_all_step_describe, analysisEntity.getFirstAnalysisDescribe());
        baseViewHolder.setText(R.id.tv_all_step_unit, analysisEntity.getFirstAnalysisUnit());
        baseViewHolder.setText(R.id.tv_average_step_value, analysisEntity.getSecondAnalysisValue());
        baseViewHolder.setText(R.id.tv_average_step_describe, analysisEntity.getSecondAnalysisDescribe());
        baseViewHolder.setText(R.id.tv_average_step_unit, analysisEntity.getSecondAnalysisUnit());
    }
}
