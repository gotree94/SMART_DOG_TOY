package com.jieli.healthaide.ui.device.nfc.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.ItemBannerBinding;

import org.jetbrains.annotations.NotNull;

/**
 * @ClassName: BannerAdapter
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/7/26 19:44
 */
public class BannerAdapter extends BaseQuickAdapter<Integer, BaseDataBindingHolder<ItemBannerBinding>> {

    public BannerAdapter() {
        super(R.layout.item_banner);
    }

    //重写此方法，使recyclerView可以一直滑动
    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Integer getItem(int position) {
        return super.getItem(position % getData().size());
    }

    @Override
    public int getItemViewType(int position) {
        int count = getHeaderLayoutCount() + getData().size();
        //刚开始进入包含该类的activity时,count为0。就会出现0%0的情况，这会抛出异常，所以我们要在下面做一下判断
        if (count <= 0) {
            count = 1;
        }
        int newPosition = position % count;
        return super.getItemViewType(newPosition);
    }

    @Override
    protected void convert(@NotNull BaseDataBindingHolder<ItemBannerBinding> itemBannerBindingBaseDataBindingHolder, Integer integer) {
        itemBannerBindingBaseDataBindingHolder.getDataBinding().imageView.setImageResource(integer);
    }
}
