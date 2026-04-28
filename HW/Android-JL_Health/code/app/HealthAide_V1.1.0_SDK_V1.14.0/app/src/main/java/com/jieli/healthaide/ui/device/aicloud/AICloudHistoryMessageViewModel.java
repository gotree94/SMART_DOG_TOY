package com.jieli.healthaide.ui.device.aicloud;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.data.dao.AICloudMessageDao;
import com.jieli.healthaide.data.db.HealthDatabase;
import com.jieli.healthaide.data.entity.AICloudMessageEntity;
import com.jieli.healthaide.tool.aiui.AIManager;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @ClassName: AICloudHistoryMessageViewModel
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/7/17 14:06
 */
public class AICloudHistoryMessageViewModel extends ViewModel {
    private final String tag = getClass().getSimpleName();
    public MutableLiveData<List<AICloudMessage>> loadMoreMessageListMLD = new MutableLiveData<>(new ArrayList<>());
    private List<AICloudMessage> mHistoryMessageList = new ArrayList<>();
    private long loadOffset = 0;
    private long validTime = 0;//有效时间 //todo 在外面卡着录音再进入界面会出现bug

    public AICloudHistoryMessageViewModel() {

    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public void firstLoad() {
        validTime = AIManager.getInstance().getAICloudServe().getStartRecordTime();
        //避免录音插入的数据导致加载更多异常
        if (validTime == 0) {//说明进入页面之前没有录音
            validTime = Calendar.getInstance().getTimeInMillis();
        } else {
            validTime--;
        }
        loadMore();
    }

    /**
     * 加载更多
     */
    public void loadMore() {
        queryHistoryMessage(loadOffset);
    }

    /**
     * 查询消息记录
     */
    private void queryHistoryMessage(long offset) {
        //下拉加载历史记录
        String uid = HealthApplication.getAppViewModel().getUid();
        AICloudMessageDao dao = HealthDatabase.buildHealthDb(HealthApplication.getAppViewModel().getApplication()).AICloudMessageDao();
        List<AICloudMessageEntity> entities = dao.getAICloudMessageByLimit(uid, offset, validTime);
        AICloudMessage lastMessage = null;
        if (!mHistoryMessageList.isEmpty()) {
            lastMessage = mHistoryMessageList.get(0);
        }
        List<AICloudMessage> tempList = new ArrayList<>();
        if (entities != null && !entities.isEmpty()) {
            for (int i = 0; i < entities.size(); i++) {
                AICloudMessageEntity entity = entities.get(i);
                boolean isFirst = false;
//                Log.e("TAG", "queryHistoryMessage: time" + entity.getTime());
                if (null != lastMessage) {
                    isFirst = isFirst(entity, lastMessage.getEntity());
                    if (!isFirst) {
                        lastMessage.setFirstMessage(false);
                    }
                }
                JL_Log.d(tag, "queryHistoryMessage", "lastMessage : " + lastMessage + ", isFirst : " + isFirst);
                AICloudMessage message = new AICloudMessage();
                message.setFirstMessage(true);
                message.setEntity(entity);
                tempList.add(0, message);
                lastMessage = message;
                mHistoryMessageList.add(0, message);
            }
            loadOffset += entities.size();
        }
        loadMoreMessageListMLD.postValue(tempList);
    }

    /**
     * 清空消息记录
     */
    public void cleanHistoryMessage() {
        AICloudMessageDao dao = HealthDatabase.buildHealthDb(HealthApplication.getAppViewModel().getApplication()).AICloudMessageDao();
        dao.clean();
        mHistoryMessageList = new ArrayList<>();
    }

    /**
     * 删除消息记录
     */
    public void deleteHistoryMessage(List<AICloudMessage> messages) {
        AICloudMessageDao dao = HealthDatabase.buildHealthDb(HealthApplication.getAppViewModel().getApplication()).AICloudMessageDao();
        if (messages != null && !messages.isEmpty()) {
            List<AICloudMessageEntity> tempList = new ArrayList<>();
            for (AICloudMessage message : messages) {
                tempList.add(message.getEntity());
            }
            dao.deleteEntities(tempList);
            mHistoryMessageList.removeAll(messages);
        }
    }

    public boolean isFirst(AICloudMessageEntity entity, AICloudMessageEntity lastEntity) {
        return isFirst(entity.getTime(), lastEntity.getTime());
    }

    public boolean isFirst(long time, AICloudMessageEntity lastEntity) {
        return isFirst(time, lastEntity.getTime());
    }

    public boolean isFirst(long time, long lastTime) {
        JL_Log.d(tag, "isFirst", "time : " + time + " , " + lastTime + " , " + (time - lastTime));
        return Math.abs((time - lastTime)) > (10 * 60 * 1000);
    }
}
