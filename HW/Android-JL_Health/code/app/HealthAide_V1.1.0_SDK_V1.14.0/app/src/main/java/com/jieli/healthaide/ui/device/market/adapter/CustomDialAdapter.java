package com.jieli.healthaide.ui.device.market.adapter;

import android.annotation.SuppressLint;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.customdial.CustomDialInfo;
import com.jieli.jl_rcsp.model.device.settings.v0.DialExpandInfo;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @ClassName: CustomDialAdapter
 * @Description: 自定义表盘适配器
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/11 11:22
 */
public class CustomDialAdapter extends BaseMultiItemQuickAdapter<CustomDialAdapter.Data, BaseViewHolder> {
    private boolean isEditMode;
    private long usingId;
    private DialExpandInfo mDialExpandInfo;

    public CustomDialAdapter() {
        addItemType(0, R.layout.item_watch_add);
        addItemType(1, R.layout.item_watch_market);
//        super(R.layout.item_watch_market);
        addChildClickViewIds(R.id.iv_item_watch_delete, R.id.tv_item_watch_btn, R.id.tv_item_watch_edit);
    }

    public void setUsingId(long usingId) {
        this.usingId = usingId;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setEditMode(boolean editMode) {
        if (isEditMode != editMode) {
            isEditMode = editMode;
            notifyDataSetChanged();
        }
    }

    public void setDialExpandInfo(DialExpandInfo dialExpandInfo) {
        mDialExpandInfo = dialExpandInfo;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, CustomDialAdapter.Data data) {
        switch (data.getItemType()) {
            case 0://add
                baseViewHolder.setImageResource(R.id.iv_item_watch_img, R.drawable.ic_add_custom_dial);
                baseViewHolder.setText(R.id.tv_item_watch_btn, R.string.add);
                baseViewHolder.setGone(R.id.tv_item_watch_name, true);
                break;
            case 1:
                int operateBtnText = R.string.use_watch;
                boolean isShowEditView = false;
                boolean isShowDeleteView = false;
                if (isEditMode) {
                    isShowDeleteView = true;
                }
                String imgPath = data.customDialInfo.cutImagePath;
                ImageView imageView = baseViewHolder.findView(R.id.iv_item_watch_img);
                updateImageView(imgPath, imageView);
                boolean isUsing = false/*usingId == data.customDialInfo.id*/;
                if (isUsing) {
                    if (!isEditMode) {
                        isShowEditView = true;
                    }
                    operateBtnText = R.string.using_watch;
                }
                if (isShowEditView) {
                    TextView tvWatchBgEdit = baseViewHolder.getView(R.id.tv_item_watch_bg);
                    boolean isCircular = mDialExpandInfo != null && mDialExpandInfo.isCircular();
                    tvWatchBgEdit.setBackgroundResource(isCircular ? R.drawable.ic_watch_bg : R.drawable.ic_watch_bg_rect);
                }
                baseViewHolder.setVisible(R.id.tv_item_watch_bg, isShowEditView);
                baseViewHolder.setVisible(R.id.tv_item_watch_edit, isShowEditView);
                baseViewHolder.setText(R.id.tv_item_watch_btn, operateBtnText);
                baseViewHolder.setVisible(R.id.iv_item_watch_delete, isShowDeleteView);
                baseViewHolder.setGone(R.id.tv_item_watch_name, true);
                break;
        }
    }

    private void updateImageView(String filePath, ImageView view) {
        JL_Log.d(getClass().getSimpleName(), "updateImageView", "" + this.mDialExpandInfo);
        Glide.with(getContext())
                .load(filePath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view);
    }

    public static class Data implements MultiItemEntity {

        /**
         * 类型：0：添加view，1：表盘数据
         */
        public int type = 0;

        public CustomDialInfo customDialInfo;

        @Override
        public int getItemType() {
            return type;
        }
    }

}
