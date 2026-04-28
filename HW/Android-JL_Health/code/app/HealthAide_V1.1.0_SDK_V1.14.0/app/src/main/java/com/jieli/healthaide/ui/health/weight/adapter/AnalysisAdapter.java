package com.jieli.healthaide.ui.health.weight.adapter;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.health.weight.entity.AnalysisDayEntity;
import com.jieli.healthaide.ui.health.weight.entity.AnalysisMultipleBaseEntity;
import com.jieli.healthaide.ui.health.weight.entity.AnalysisWeekEntity;

import org.jetbrains.annotations.NotNull;

/**
 * @ClassName: AnalysisAdapter
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/3/8 17:06
 */
public class AnalysisAdapter extends BaseMultiItemQuickAdapter<AnalysisMultipleBaseEntity, BaseViewHolder> {
    public AnalysisAdapter() {
        super();
        addItemType(AnalysisMultipleBaseEntity.TYPE_ONE, R.layout.item_weight_analysis_1);
        addItemType(AnalysisMultipleBaseEntity.TYPE_TWO, R.layout.item_weight_analysis_2);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, AnalysisMultipleBaseEntity analysisMultipleBaseEntity) {
        int viewType = getItemViewType(getItemPosition(analysisMultipleBaseEntity));
        switch (viewType) {
            case AnalysisMultipleBaseEntity.TYPE_ONE:
                AnalysisDayEntity analysisDayEntity = (AnalysisDayEntity) analysisMultipleBaseEntity;
                baseViewHolder.setText(R.id.tv_weight_analysis1_type, analysisDayEntity.getAnalysisDescribe());
                baseViewHolder.setText(R.id.tv_weight_analysis_value, analysisDayEntity.getAnalysisValue());
                baseViewHolder.setImageResource(R.id.iv_weight_setting_type, analysisDayEntity.getAnalysisIconSrc());
                break;
            case AnalysisMultipleBaseEntity.TYPE_TWO:
                AnalysisWeekEntity analysisWeekEntity = (AnalysisWeekEntity) analysisMultipleBaseEntity;
                baseViewHolder.setText(R.id.tv_weight_analysis_type, analysisWeekEntity.getAnalysisDescribe());
                baseViewHolder.setText(R.id.tv_weight_analysis_value, analysisWeekEntity.getAnalysisValue());
                baseViewHolder.setText(R.id.tv_weight_analysis_unit, analysisWeekEntity.getAnalysisUnit());
                break;
        }
    }
}
