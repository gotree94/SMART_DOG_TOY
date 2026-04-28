package com.jieli.healthaide.ui.device.health;

import android.text.TextUtils;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.ItemHealthOptionBinding;

import org.jetbrains.annotations.NotNull;

/**
 * @ClassName: HealthOptionAdapter
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/7/13 11:26
 */
public class HealthOptionAdapter extends BaseQuickAdapter<HealthOptionItem, BaseDataBindingHolder<ItemHealthOptionBinding>> {
    public HealthOptionAdapter() {
        super(R.layout.item_health_option);
    }

    @Override
    protected void convert(@NotNull BaseDataBindingHolder<ItemHealthOptionBinding> holder, HealthOptionItem healthOptionItem) {
        ItemHealthOptionBinding binding = holder.getDataBinding();
        binding.tvHealthItemName.setText(healthOptionItem.getTitle());
        binding.ivHealthItemLeft.setVisibility(healthOptionItem.getLeftImg() <= 0 ? View.GONE : View.VISIBLE);
        binding.ivHealthItemLeft.setImageResource(healthOptionItem.getLeftImg());
        binding.ivHealthItemRight.setVisibility(healthOptionItem.getRightImg() <= 0 ? View.GONE : View.VISIBLE);
        binding.ivHealthItemRight.setImageResource(healthOptionItem.getRightImg());
        binding.tvHealthItemTail.setVisibility(TextUtils.isEmpty(healthOptionItem.getTailString()) ? View.GONE : View.VISIBLE);
        binding.tvHealthItemTail.setText(healthOptionItem.getTailString());
        binding.swHealthCommon.setVisibility(!healthOptionItem.isShowSw() ? View.GONE : View.VISIBLE);
        binding.swHealthCommon.setOnCheckedChangeListener(healthOptionItem.getSwCheckListener());
        binding.swHealthCommon.setCheckedImmediatelyNoEvent(healthOptionItem.isSwChecked());
        binding.ivHealthItemNext.setVisibility(!healthOptionItem.isShowNext() ? View.GONE : View.VISIBLE);
        binding.tvHealthHint.setVisibility(TextUtils.isEmpty(healthOptionItem.getHintText()) ? View.GONE : View.VISIBLE);
        binding.tvHealthHint.setText(healthOptionItem.getHintText());
    }
}
