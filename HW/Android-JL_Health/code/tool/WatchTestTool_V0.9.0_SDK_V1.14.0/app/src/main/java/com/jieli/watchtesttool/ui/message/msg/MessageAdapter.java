package com.jieli.watchtesttool.ui.message.msg;

import android.annotation.SuppressLint;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.data.db.message.MessageEntity;
import com.jieli.watchtesttool.util.MessageUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 信息适配器
 * @since 2023/1/31
 */
class MessageAdapter extends BaseQuickAdapter<MessageEntity, BaseViewHolder> {
    private final OnEventListener mListener;

    public MessageAdapter(OnEventListener listener) {
        super(R.layout.item_sync_message);
        mListener = listener;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, MessageEntity entity) {
        if (null == entity) return;
        viewHolder.setText(R.id.tv_message_app_value, String.format(Locale.getDefault(), "%s(%s)",
                MessageUtil.getAppName(getContext(), entity.getFlag()), MessageUtil.getPackageName(entity.getFlag())));
        viewHolder.setText(R.id.tv_message_title_value, entity.getTitle());
        viewHolder.setText(R.id.tv_message_time, timeFormat(entity.getUpdateTime()));
        viewHolder.setText(R.id.tv_message_content_value, entity.getContent());

        int position = getItemPosition(entity);
        ImageView ivSendBtn = viewHolder.getView(R.id.iv_send_btn);
        ivSendBtn.setTag(position);
        ivSendBtn.setOnClickListener(v -> {
            if (v.getTag() instanceof Integer) {
                int pos = (int) v.getTag();
                MessageEntity entity1 = getItem(pos);
                if (null != mListener) {
                    mListener.onSend(pos, entity1);
                }
            }
        });
        ImageView ivRetractBtn = viewHolder.getView(R.id.iv_retract_btn);
        ivRetractBtn.setTag(position);
        ivRetractBtn.setOnClickListener(v -> {
            if (v.getTag() instanceof Integer) {
                int pos = (int) v.getTag();
                MessageEntity entity1 = getItem(pos);
                if (null != mListener) {
                    mListener.onRetract(pos, entity1);
                }
            }
        });
        ImageView ivDeleteBtn = viewHolder.getView(R.id.iv_delete_btn);
        ivDeleteBtn.setTag(position);
        ivDeleteBtn.setOnClickListener(v -> {
            if (v.getTag() instanceof Integer) {
                int pos = (int) v.getTag();
                MessageEntity entity1 = getItem(pos);
                if (null != mListener) {
                    mListener.onDelete(pos, entity1);
                }
            }
        });
    }

    private String timeFormat(long time) {
        String text = "";
        @SuppressLint("SimpleDateFormat") final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        try {
            text = dateFormat.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }

    public interface OnEventListener {

        void onSend(int position, MessageEntity message);

        void onRetract(int position, MessageEntity message);

        void onDelete(int position, MessageEntity message);
    }
}
