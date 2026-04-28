package com.jieli.healthaide.ui.device.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.HealthUtil;

import org.jetbrains.annotations.NotNull;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 表盘适配器
 * @since 2021/3/11
 */
public class WatchAdapter extends BaseQuickAdapter<WatchInfo, BaseViewHolder> implements LoadMoreModule {
    private boolean isEditMode;
    private boolean isBanUpdate;
    private boolean isBanEditCustomBg;
    private boolean isGoneDeleteIcon = false;//直接gone删除按钮避免占位置

    public WatchAdapter() {
        super(R.layout.item_watch);
        addChildClickViewIds(R.id.iv_item_watch_delete, R.id.tv_item_watch_btn, R.id.tv_item_watch_edit);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder viewHolder, WatchInfo watchInfo) {
        if (watchInfo == null) return;
        ImageView ivWatch = viewHolder.getView(R.id.iv_item_watch_img);
        String uri = watchInfo.getBitmapUri();
        HealthUtil.updateWatchImg(getContext(), ivWatch, uri);
        viewHolder.setText(R.id.tv_item_watch_name, watchInfo.getName());
        TextView tvWatchStatus = viewHolder.getView(R.id.tv_item_watch_btn);
        updateWatchStatus(getContext(), tvWatchStatus, watchInfo);
        tvWatchStatus.setEnabled(!isEditMode);
        boolean isShowDelete = isEditMode && watchInfo.getStatus() != WatchInfo.WATCH_STATUS_NONE_EXIST;
        ImageView ivDelete = viewHolder.getView(R.id.iv_item_watch_delete);
        if (isGoneDeleteIcon) {
            ivDelete.setVisibility(isShowDelete ? View.VISIBLE : View.GONE);
        } else {
            ivDelete.setVisibility(isShowDelete ? View.VISIBLE : View.INVISIBLE);
        }
        TextView tvWatchBgEdit = viewHolder.getView(R.id.tv_item_watch_bg);
        TextView tvWatchEdit = viewHolder.getView(R.id.tv_item_watch_edit);

        boolean isShowEditUI = !isBanEditCustomBg && !isEditMode && watchInfo.getStatus() == WatchInfo.WATCH_STATUS_USING;
        tvWatchBgEdit.setVisibility(isShowEditUI ? View.VISIBLE : View.GONE);
        tvWatchEdit.setVisibility(isShowEditUI ? View.VISIBLE : View.GONE);
        if (isShowEditUI) {
            tvWatchBgEdit.setBackgroundResource(watchInfo.isCircleDial() ? R.drawable.ic_watch_bg : R.drawable.ic_watch_bg_rect);
        }
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setGoneDeleteIcon(boolean goneDeleteIcon) {
        isGoneDeleteIcon = goneDeleteIcon;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setEditMode(boolean editMode) {
        if (isEditMode != editMode) {
            isEditMode = editMode;
            notifyDataSetChanged();
        }
    }

    public boolean isBanUpdate() {
        return isBanUpdate;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setBanUpdate(boolean banUpdate) {
        if (isBanUpdate != banUpdate) {
            isBanUpdate = banUpdate;
            notifyDataSetChanged();
        }
    }

    public boolean isBanEditCustomBg() {
        return isBanEditCustomBg;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setBanEditCustomBg(boolean banEditCustomBg) {
        if (isBanEditCustomBg != banEditCustomBg) {
            isBanEditCustomBg = banEditCustomBg;
            notifyDataSetChanged();
        }
    }

    @SuppressLint({"ResourceType", "UseCompatLoadingForColorStateLists"})
    private void updateWatchStatus(Context context, TextView textView, WatchInfo watchInfo) {
        if (context == null || textView == null) return;
        String text = context.getString(R.string.download_watch);
        int colorId = R.color.btn_blue_2_gray_selector;
        int bgId = R.drawable.bg_watch_white_2_gray_selector;
        switch (watchInfo.getStatus()) {
            case WatchInfo.WATCH_STATUS_NOT_PAYMENT:
                if (watchInfo.getServerFile() != null && watchInfo.getServerFile().getPrice() != null
                        && watchInfo.getServerFile().getPrice() > 0) {
                    text = CalendarUtil.formatString("¥ %s", HealthUtil.getPriceFormat(watchInfo.getServerFile().getPrice()));
                }
                break;
            case WatchInfo.WATCH_STATUS_EXIST:
                text = context.getString(R.string.use_watch);
                if (!isBanUpdate && watchInfo.hasUpdate()) {
                    text = context.getString(R.string.update_watch);
                    colorId = R.color.white;
                    bgId = R.drawable.bg_watch_green_2_gray_selecter;
                }
                break;
            case WatchInfo.WATCH_STATUS_USING:
                text = context.getString(R.string.using_watch);
                break;
        }
        textView.setText(text);
        textView.setTextColor(context.getResources().getColorStateList(colorId));
        textView.setBackgroundResource(bgId);
    }
}
