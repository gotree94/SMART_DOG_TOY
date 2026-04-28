package com.jieli.watchtesttool.ui.message.msg;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.jieli.watchtesttool.data.db.SensorDbBase;
import com.jieli.watchtesttool.data.db.message.MessageDao;
import com.jieli.watchtesttool.data.db.message.MessageEntity;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothViewModel;
import com.jieli.watchtesttool.tool.watch.WatchManager;
import com.jieli.watchtesttool.util.MessageUtil;

import java.util.Calendar;
import java.util.List;

public class SyncMessageViewModel extends BluetoothViewModel {

    private final MessageDao mMessageDao;

    MutableLiveData<List<MessageEntity>> messageListMLD = new MutableLiveData<>();

    private boolean isFirst = true;

    public SyncMessageViewModel() {
        mMessageDao = SensorDbBase.getInstance().messageDao();
    }

    public WatchManager getWatchManager() {
        return mWatchManager;
    }

    public void queryMessageList() {
        if (!mWatchManager.isWatchSystemOk()) return;
        String mac = getConnectedDevice().getAddress();
        List<MessageEntity> messageList = mMessageDao.queryMessages(mac);
        /*if (messageList.isEmpty() && isFirst) {
            isFirst = false;
            MessageEntity entity = new MessageEntity();
            entity.setMac(mac);
            entity.setFlag(MessageUtil.FLAG_WECHAT);
            entity.setPackageName(MessageUtil.getPackageName(MessageUtil.FLAG_WECHAT));
            entity.setTitle("测试通知1");
            entity.setContent("测试内容:Hi,帅小伙！今晚有空去吃饭吗？");
            Calendar calendar = Calendar.getInstance();
            calendar.set(2022, Calendar.DECEMBER, 15, 12, 8, 36);
            entity.setUpdateTime(calendar.getTimeInMillis());
            mMessageDao.insert(entity);
            messageList.add(entity);

            entity= new MessageEntity();
            entity.setMac(mac);
            entity.setFlag(MessageUtil.FLAG_DING_DING);
            entity.setPackageName(MessageUtil.getPackageName(MessageUtil.FLAG_DING_DING));
            entity.setTitle("测试通知2");
            entity.setContent("测试内容:来一次说走就走的旅游吗？");
            calendar = Calendar.getInstance();
            calendar.set(2023, Calendar.JANUARY, 28, 14, 32, 20);
            entity.setUpdateTime(calendar.getTimeInMillis());
            mMessageDao.insert(entity);
            messageList.add(entity);
        }*/
        messageListMLD.setValue(messageList);
    }

    public boolean insertMessage(@NonNull MessageEntity message) {
        MessageEntity cache = mMessageDao.queryMessage(message.getMac(), message.getPackageName(), message.getUpdateTime());
        if (cache != null) return false;
        mMessageDao.insert(message);
        queryMessageList();
        return true;
    }

    public void deleteMessage(MessageEntity message) {
        if (null == message) return;
        mMessageDao.delete(message);
        queryMessageList();
    }

}