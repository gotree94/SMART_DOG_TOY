package com.jieli.healthaide.ui.health.heartrate.adapter;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.health.heartrate.entity.HeartDescribeEntity;

import org.jetbrains.annotations.NotNull;

/**
 * @ClassName: HeartDescribeAdapter
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/3/8 8:52
 */
public class HeartDescribeAdapter extends BaseQuickAdapter<HeartDescribeEntity, BaseViewHolder> {
    public HeartDescribeAdapter() {
        super(R.layout.item_heart_rate_analysis);
    }

    private int selectedItemPosition = 0;

    public void setSelectedItem(int selectedItemPosition) {
        this.selectedItemPosition = selectedItemPosition;
        notifyDataSetChanged();
    }

    public int getSelectedItemPosition() {
        return selectedItemPosition;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, HeartDescribeEntity heartDescribeEntity) {
        CardView bgCardView = baseViewHolder.findView(R.id.cv_bg);
        if (getItemPosition(heartDescribeEntity) == selectedItemPosition) {
            bgCardView.setCardElevation(ValueUtil.dp2px(getContext(), 5));
        } else {
            bgCardView.setCardElevation(0);
        }
        ImageView ivIconDescribe = baseViewHolder.findView(R.id.iv_icon_describe);
        ivIconDescribe.setImageResource(heartDescribeEntity.getIconSrc());
        TextView tvDescribeType = baseViewHolder.findView(R.id.tv_heart_rate_describe_type);
        tvDescribeType.setText(heartDescribeEntity.getEntityTypeStringSrc());
        TextView tvDescribeValue = baseViewHolder.findView(R.id.tv_heart_rate_describe_value);
        tvDescribeValue.setText(heartDescribeEntity.getValueString());
    }
}
