package com.jieli.healthaide.ui.device.market.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.device.market.bean.PayWayEntity;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 付款方式适配器
 * @since 2022/6/20
 */
public class PayWayAdapter extends BaseQuickAdapter<PayWayEntity, BaseViewHolder> {
    private int selectedIndex = -1;

    public PayWayAdapter() {
        super(R.layout.item_pay_way);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, PayWayEntity payWayEntity) {
        if (null == payWayEntity) return;
        TextView tvPayWay = viewHolder.getView(R.id.tv_pay_way);
        tvPayWay.setText(getPayWayName(getContext(), payWayEntity.getWay()));
        tvPayWay.setCompoundDrawablesRelativeWithIntrinsicBounds(getPayWayIcon(payWayEntity.getWay()), 0, 0, 0);
        ImageView ivSelect = viewHolder.getView(R.id.iv_pay_way_select);
        int position = getItemPosition(payWayEntity);
        ivSelect.setSelected(isSelectIndex(position));
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectedIndex(int selectedIndex) {
        if (selectedIndex != this.selectedIndex) {
            this.selectedIndex = selectedIndex;
            notifyDataSetChanged();
        }
    }

    public int getPayWay() {
        if (selectedIndex == -1) return -1;
        PayWayEntity entity = getItemOrNull(selectedIndex);
        if (null == entity) return -1;
        return entity.getWay();
    }

    public boolean isSelectIndex(int position) {
        return selectedIndex == position;
    }

    public String getPayWayName(Context context, int way) {
        String name = "";
        switch (way) {
            case PayWayEntity.PAY_WAY_ALI:
                name = context.getString(R.string.ali_pay);
                break;
            case PayWayEntity.PAY_WAY_WEIXIN:
                name = context.getString(R.string.weixin_pay);
                break;
        }
        return name;
    }

    private int getPayWayIcon(int way) {
        int res = 0;
        switch (way) {
            case PayWayEntity.PAY_WAY_ALI:
                res = R.drawable.ic_ali_pay;
                break;
            case PayWayEntity.PAY_WAY_WEIXIN:
                res = R.drawable.ic_weixin_pay;
                break;
        }
        return res;
    }
}
