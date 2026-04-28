package com.jieli.healthaide.ui.device.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.device.bean.FuncItem;

import org.jetbrains.annotations.NotNull;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 手表功能适配器
 * @since 2021/3/11
 */
public class WatchFuncAdapter extends BaseQuickAdapter<FuncItem, BaseViewHolder> {

    public WatchFuncAdapter() {
        super(R.layout.item_watch_func);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder viewHolder, FuncItem funcItem) {
        viewHolder.setText(R.id.tv_item_watch_func, funcItem.getName());
        viewHolder.setImageResource(R.id.iv_item_watch_func_icon, funcItem.getResId());
    }
}
