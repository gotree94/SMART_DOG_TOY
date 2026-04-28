package com.jieli.healthaide.ui.device.nfc.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.jl_rcsp.model.device.NfcMsg;

import org.jetbrains.annotations.NotNull;

/**
 * @ClassName: NFCCardBagAdapter
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/7/19 17:45
 */
public class NfcCardBagAdapter extends BaseQuickAdapter<NfcMsg, BaseViewHolder> {
    private short defaultID = 0;

    public NfcCardBagAdapter() {
        super(R.layout.item_nfc_card);
    }

    public void setDefaultCart(short id) {
        if (defaultID != id) {
            defaultID = id;
            notifyDataSetChanged();
        }
    }

    @Override
    protected void convert(@NotNull BaseViewHolder viewHolder, NfcMsg nfcMsg) {
        viewHolder.setText(R.id.tv_nfc_card_nickname, nfcMsg.getNickname());
        viewHolder.setVisible(R.id.tv_nfc_card_default_flag, defaultID == nfcMsg.getId());
        if (HealthConstant.TEST_DEVICE_FUNCTION){
            addChildClickViewIds(R.id.iv_nfc_card_edit);
            bindViewClickListener(viewHolder, R.id.iv_nfc_card_edit);
        }
        viewHolder.setVisible(R.id.iv_nfc_card_edit, HealthConstant.TEST_DEVICE_FUNCTION);
    }
}
