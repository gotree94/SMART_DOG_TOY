package com.jieli.healthaide.ui.device.aidial;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.aiui.iflytek.IflytekAIDialStyleHelper;

/**
 * @ClassName: AIDialStyleAdapter
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/9 14:23
 */
public class AIDialStyleAdapter extends BaseQuickAdapter<IflytekAIDialStyleHelper.IflytekAIDialStyle, BaseViewHolder> {
    public AIDialStyleAdapter() {
        super(R.layout.item_ai_dial_style);
    }

    private int selectPosition = -1;

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, IflytekAIDialStyleHelper.IflytekAIDialStyle style) {
        baseViewHolder.setImageResource(R.id.iv_ai_dial_style_bg, style.imageSrcId);
        baseViewHolder.setText(R.id.tv_ai_dial_style_name, style.textSrcId);
        boolean isSelect = getItemPosition(style) == selectPosition;
        baseViewHolder.setVisible(R.id.iv_ai_dial_style_select, isSelect);
        baseViewHolder.setVisible(R.id.view_ai_dial_style_select, isSelect);
    }

    public int getSelectPosition() {
        return selectPosition;
    }

    public void setSelectPosition(int selectPosition) {
        this.selectPosition = selectPosition;
        notifyDataSetChanged();
    }

    public static class AIDialStyle {
        public AIDialStyle(int nameSrc, int imgSrc) {
            this.nameSrc = nameSrc;
            this.imgSrc = imgSrc;
        }

        public int nameSrc;
        public int imgSrc;
    }
}
