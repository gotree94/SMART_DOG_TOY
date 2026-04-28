package com.jieli.healthaide.ui.mine;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.mine.entries.CommonItem;

import org.jetbrains.annotations.NotNull;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/10/21 9:19 AM
 * @desc :
 */
public class CommonAdapter extends BaseQuickAdapter<CommonItem, BaseViewHolder> {
    public CommonAdapter() {
        super(R.layout.item_mine_common);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, CommonItem commonItem) {


        holder.setText(R.id.tv_mine_item_name, commonItem.getTitle());

        holder.setGone(R.id.iv_mine_item_left, commonItem.getLeftImg() <= 0);
        holder.setImageResource(R.id.iv_mine_item_left, commonItem.getLeftImg());

        holder.setGone(R.id.iv_mine_item_right, commonItem.getRightImg() <= 0);
        holder.setImageResource(R.id.iv_mine_item_right, commonItem.getRightImg());

        holder.setText(R.id.tv_mine_item_tail, commonItem.getTailString());
        holder.setGone(R.id.tv_mine_item_tail, TextUtils.isEmpty(commonItem.getTailString()));


        holder.setGone(R.id.sw_mine_common, !commonItem.isShowSw());

        holder.setGone(R.id.iv_mine_item_next, !commonItem.isShowNext());


    }
}
