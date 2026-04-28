package com.jieli.healthaide.ui.device.aicloud;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.jieli.healthaide.data.entity.AICloudMessageEntity;

/**
 * @ClassName: AICloudMessage
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/7/19 10:14
 */
public class AICloudMessage implements MultiItemEntity {
    private boolean isFirstMessage = false;
    private AICloudMessageEntity mEntity;

    public AICloudMessageEntity getEntity() {
        return mEntity;
    }

    public void setEntity(AICloudMessageEntity entity) {
        mEntity = entity;
    }

    public boolean isFirstMessage() {
        return isFirstMessage;
    }

    public void setFirstMessage(boolean firstMessage) {
        isFirstMessage = firstMessage;
    }

    @Override
    public int getItemType() {
        return mEntity.getRole();
    }
}
