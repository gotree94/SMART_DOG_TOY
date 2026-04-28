package com.jieli.healthaide.ui.health.sleep.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.health.sleep.entity.AnalysisEntity;

import org.jetbrains.annotations.NotNull;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/4/21 11:49 AM
 * @desc :
 */
public class AnalysisAdapter extends BaseQuickAdapter<AnalysisEntity, BaseViewHolder> {

    public AnalysisAdapter() {
        super(R.layout.item_sleep_analysis_data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, AnalysisEntity analysisEntity) {
        baseViewHolder.setText(R.id.tv_sleep_analysis_title, analysisEntity.title);
        baseViewHolder.setText(R.id.tv_sleep_analysis_reference, analysisEntity.reference);
        baseViewHolder.setText(R.id.tv_sleep_analysis_result, analysisEntity.level);
        baseViewHolder.setTextColor(R.id.tv_sleep_analysis_result, getContext().getResources().getColor(analysisEntity.levelColor));
    }
}
