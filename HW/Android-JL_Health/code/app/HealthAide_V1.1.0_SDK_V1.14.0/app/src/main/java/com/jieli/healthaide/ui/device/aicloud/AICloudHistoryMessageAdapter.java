package com.jieli.healthaide.ui.device.aicloud;


import android.annotation.SuppressLint;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.module.UpFetchModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.jl_rcsp.util.JL_Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @ClassName: AICloudHistoryMessageAdapter
 * @Description: AI云 消息历史记录
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/7/17 17:31
 */
public class AICloudHistoryMessageAdapter extends BaseMultiItemQuickAdapter<AICloudMessage, BaseViewHolder> implements UpFetchModule {
    private List<AICloudMessage> mSelectedList = new ArrayList<>();

    /**
     * 模式:0:正常，1：删除
     */
    private int mode = 0;

    public AICloudHistoryMessageAdapter() {
        addItemType(0, R.layout.item_aicloud_history_message_user);
        addItemType(1, R.layout.item_aicloud_history_message_ai);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, AICloudMessage aiCloudMessage) {
        if (aiCloudMessage.isFirstMessage()) {
            baseViewHolder.setGone(R.id.tv_aicloud_msg_user_time, false);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formater = new SimpleDateFormat("MM-dd HH:mm", Locale.ENGLISH);
            baseViewHolder.setText(R.id.tv_aicloud_msg_user_time, formater.format(new Date(aiCloudMessage.getEntity().getTime())));
        } else {
            baseViewHolder.setGone(R.id.tv_aicloud_msg_user_time, true);
        }
        switch (aiCloudMessage.getItemType()) {
            case 0://user
                boolean isDeleteMode = isDeleteMode();
                baseViewHolder.setVisible(R.id.img_aicloud_select_user, isDeleteMode);
                if (isDeleteMode) {
                    boolean isSelected = isSelected(aiCloudMessage);
                    baseViewHolder.setImageResource(R.id.img_aicloud_select_user, isSelected ? R.drawable.ic_choose2_sel : R.drawable.ic_choose2_nol);
                }
                int state = aiCloudMessage.getEntity().getAiCloudState();
                baseViewHolder.setGone(R.id.ll_aicloud_msg_content, state <= 0);
                baseViewHolder.setGone(R.id.iv_aicloud_recording, state != 1);
                baseViewHolder.setGone(R.id.tv_aicloud_msg_user_transferring, state != 2);
                boolean isHasUserText = state == 3 && !TextUtils.isEmpty(aiCloudMessage.getEntity().getText());
                baseViewHolder.setGone(R.id.tv_aicloud_msg_user_content, !isHasUserText);
                if (isHasUserText) {//语音识别结束，且有文本
                    baseViewHolder.setText(R.id.tv_aicloud_msg_user_content, aiCloudMessage.getEntity().getText());
                }
                break;
            case 1://ai
                baseViewHolder.setGone(R.id.img_aicloud_logo_ai, false);
                boolean isHasAIText = !TextUtils.isEmpty(aiCloudMessage.getEntity().getText());
                baseViewHolder.setGone(R.id.ll_aicloud_msg_content, !isHasAIText);
                if (isHasAIText) {
                    baseViewHolder.setText(R.id.tv_aicloud_msg_ai_content, aiCloudMessage.getEntity().getText());
                }
                boolean isSelected = isSelected(aiCloudMessage);
                baseViewHolder.setBackgroundResource(R.id.fl_aicloud_msg_content, (isDeleteMode() && isSelected) ? R.drawable.bg_aicloud_message_ai_gray_shape_sel : R.drawable.bg_aicloud_message_ai_gray_shape_nol);
                break;
        }
    }

    public boolean isSelected(AICloudMessage item) {
        return mSelectedList.contains(item);
    }

    public void onSelectedItemClick(AICloudMessage item) {
        int index = mSelectedList.indexOf(item);
        int position = getItemPosition(item);
        AICloudMessage pairMessage = null;
        int pairPosition = -1;
        if (position != -1) {
            int prev = position - 1;
            int next = position + 1;
            if (prev >= 0) {
                AICloudMessage prevMessage = getItem(prev);
                boolean isPair = prevMessage.getEntity().getId() == item.getEntity().getRevId();
                JL_Log.d("TAG", "onSelectedItemClick1", "RevId : " + prevMessage.getEntity().getRevId() + " " + item.getEntity().getId());
                JL_Log.d("TAG", "onSelectedItemClick1", "Id : " + prevMessage.getEntity().getId() + " " + item.getEntity().getRevId());
                if (isPair) {
                    pairMessage = prevMessage;
                    pairPosition = prev;
                }
            }
            if (next < getItemCount()) {
                AICloudMessage nextMessage = getItem(next);
                boolean isPair = nextMessage.getEntity().getRevId() == item.getEntity().getId();
                JL_Log.d("TAG", "onSelectedItemClick2", "Id : " + nextMessage.getEntity().getId() + " " + item.getEntity().getRevId());
                JL_Log.d("TAG", "onSelectedItemClick2", "RevId : " + nextMessage.getEntity().getRevId() + " " + item.getEntity().getId());

                if (isPair) {
                    pairMessage = nextMessage;
                    pairPosition = next;
                }
            }
            if (index != -1) {
                mSelectedList.remove(item);
                if (pairMessage != null) {
                    mSelectedList.remove(pairMessage);
                }
            } else {
                mSelectedList.add(item);
                if (pairMessage != null) {
                    mSelectedList.add(pairMessage);
                }
            }
            notifyItemChanged(position);
            if (pairPosition >= 0) {
                notifyItemChanged(pairPosition);
            }
        }
    }

    public List<AICloudMessage> getSelectedList() {
        return mSelectedList;
    }

    public void cleanSelectedList() {
        mSelectedList.clear();
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    private boolean isDeleteMode() {
        return mode == 1;
    }


}
