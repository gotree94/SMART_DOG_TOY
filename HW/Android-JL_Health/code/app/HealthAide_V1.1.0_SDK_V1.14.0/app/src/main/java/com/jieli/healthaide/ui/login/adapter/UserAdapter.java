package com.jieli.healthaide.ui.login.adapter;

import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.login.bean.UserItem;

import org.jetbrains.annotations.NotNull;

/**
 * 用户属性设置适配器
 *
 * @author zqjasonZhong
 * @since 2021/3/5
 */
public class UserAdapter extends BaseQuickAdapter<UserItem, BaseViewHolder> {

    public UserAdapter() {
        super(R.layout.item_user_msg);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder viewHolder, UserItem userItem) {
        if (userItem == null) return;
        viewHolder.setText(R.id.tv_item_user_msg_name, userItem.getName());
        TextView tvValue = viewHolder.getView(R.id.tv_item_user_msg_value);
        tvValue.setText(userItem.getValue());
        if (userItem.isHideIcon()) {
            tvValue.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            tvValue.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_right_arrow, 0);
        }
        ImageView ivImage = viewHolder.getView(R.id.iv_item_user_msg_img);
        if (null != userItem.getImgPath()) {
            ivImage.setImageBitmap(BitmapFactory.decodeFile(userItem.getImgPath()));
        }
    }
}
