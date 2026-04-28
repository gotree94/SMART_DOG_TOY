package com.jieli.healthaide.ui.health.sleep.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.health.sleep.entity.NapEntity;

import org.jetbrains.annotations.NotNull;

/**
 * @ClassName: NapAdapter
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/11/3 11:29
 */
public class NapAdapter extends BaseQuickAdapter<NapEntity, BaseViewHolder> {
    public NapAdapter() {
        super(R.layout.item_sleep_nap);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, NapEntity napEntity) {
        baseViewHolder.setImageResource(R.id.iv_nap_type, napEntity.drawableSrc);
        baseViewHolder.setText(R.id.tv_nap_type, napEntity.type);
        baseViewHolder.setText(R.id.tv_nap_stamp, napEntity.timeSlot);
        baseViewHolder.setText(R.id.tv_nap_duration, napEntity.duration);
    }
}
