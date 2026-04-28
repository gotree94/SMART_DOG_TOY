package com.jieli.healthaide.ui.health.blood_oxygen.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.health.blood_oxygen.entity.AnalysisEntity;

import org.jetbrains.annotations.NotNull;

/**
 * @ClassName: AnalysisAdapter
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/3/14 14:50
 */
/*public class AnalysisAdapter extends BaseQuickAdapter<AnalysisEntity, BaseViewHolder> {
    public AnalysisAdapter() {
        super(R.layout.item_blood_oxygen_analysis);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, AnalysisEntity analysisEntity) {
        boolean newestViewVisible = TextUtils.isEmpty(analysisEntity.getFirstAnalysisDescribe());
        TextView tvNewestTime = baseViewHolder.findView(R.id.tv_newest_time);
        tvNewestTime.setVisibility(newestViewVisible ? View.GONE : View.VISIBLE);
        TextView tvNewestValue = baseViewHolder.findView(R.id.tv_newest_value);
        tvNewestValue.setVisibility(newestViewVisible ? View.GONE : View.VISIBLE);
        baseViewHolder.setText(R.id.tv_newest_time, analysisEntity.getFirstAnalysisDescribe());
        baseViewHolder.setText(R.id.tv_newest_value, analysisEntity.getFirstAnalysisValue());
        baseViewHolder.setText(R.id.tv_min_max, analysisEntity.getSecondAnalysisDescribe());
        baseViewHolder.setText(R.id.tv_min_max_value, analysisEntity.getSecondAnalysisValue());
    }
}*/

public class AnalysisAdapter extends BaseMultiItemQuickAdapter<AnalysisEntity, BaseViewHolder> {
    public AnalysisAdapter() {
        addItemType(0, R.layout.item_blood_oxygen_analysis);
        addItemType(1, R.layout.item_blood_oxygen_analysis2);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, AnalysisEntity analysisEntity) {
        boolean newestViewVisible = TextUtils.isEmpty(analysisEntity.getFirstAnalysisDescribe());
        TextView tvNewestTime = baseViewHolder.getView(R.id.tv_newest_time);
        tvNewestTime.setVisibility(newestViewVisible ? View.GONE : View.VISIBLE);
        TextView tvNewestValue = baseViewHolder.getView(R.id.tv_newest_value);
        tvNewestValue.setVisibility(newestViewVisible ? View.GONE : View.VISIBLE);
        baseViewHolder.setText(R.id.tv_newest_time, analysisEntity.getFirstAnalysisDescribe());
        baseViewHolder.setText(R.id.tv_newest_value, analysisEntity.getFirstAnalysisValue());
        baseViewHolder.setText(R.id.tv_min_max, analysisEntity.getSecondAnalysisDescribe());
        baseViewHolder.setText(R.id.tv_min_max_value, analysisEntity.getSecondAnalysisValue());
    }
}