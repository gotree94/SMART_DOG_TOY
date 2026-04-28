package com.jieli.healthaide.ui.device.market.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseSectionQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.healthaide.ui.device.market.bean.DialShopItem;
import com.jieli.healthaide.ui.device.market.bean.HeadEntity;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.model.device.settings.v0.DialExpandInfo;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 表盘商城适配器
 * @since 2022/6/16
 */
public class DialShopAdapter extends BaseSectionQuickAdapter<DialShopItem, BaseViewHolder> {
    private final String tag = DialShopAdapter.class.getSimpleName();

    public DialShopAdapter() {
        super(R.layout.item_dial_shop_head);
        setNormalLayout(R.layout.item_watch_market);

        addChildClickViewIds(R.id.tv_head_value, R.id.iv_item_watch_delete, R.id.tv_item_watch_btn);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, DialShopItem dialShopItem) {
        if (dialShopItem.isHeader()) return;
        if (!(dialShopItem.getObject() instanceof WatchInfo)) return;
        WatchInfo item = (WatchInfo) dialShopItem.getObject();
        //update image
        ImageView ivWatch = viewHolder.getView(R.id.iv_item_watch_img);
        String uri = item.getBitmapUri();
        HealthUtil.updateWatchImg(getContext(), ivWatch, uri);

        //update name
        viewHolder.setText(R.id.tv_item_watch_name, getWatchName(item));

        //update status
        TextView tvWatchStatus = viewHolder.getView(R.id.tv_item_watch_btn);
        updateWatchStatus(getContext(), tvWatchStatus, item.getStatus(), item);

        viewHolder.setVisible(R.id.iv_item_watch_delete, false);
        viewHolder.setVisible(R.id.tv_item_watch_edit, false);
        {
            TextView tvWatchBgEdit = viewHolder.getView(R.id.tv_item_watch_bg);
            DialExpandInfo dialExpandInfo = getDialExpandInfo();
            if (dialExpandInfo != null) {
                tvWatchBgEdit.setBackgroundResource(getDialExpandInfo().isCircular() ? R.drawable.ic_watch_bg : R.drawable.ic_watch_bg_rect);
            }
        }
    }

    @Override
    protected void convertHeader(@NonNull BaseViewHolder viewHolder, @NonNull DialShopItem dialShopItem) {
        if (!dialShopItem.isHeader()) return;
        if (!(dialShopItem.getObject() instanceof HeadEntity)) return;
        HeadEntity entity = (HeadEntity) dialShopItem.getObject();
        viewHolder.setText(R.id.tv_head_title, entity.getTitle());
        viewHolder.setVisible(R.id.tv_head_value, entity.isHasValue());
        if (entity.isHasValue()) {
            TextView tvValue = viewHolder.getView(R.id.tv_head_value);
            tvValue.setText(entity.getValue());
            if (entity.getValueIcon() > 0) {
                tvValue.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, entity.getValueIcon(), 0);
            }
        }
    }

    public boolean hasItem(WatchInfo info) {
        if (null == info || getData().isEmpty()) return false;
        for (DialShopItem item : getData()) {
            if (item.isHeader() || !(item.getObject() instanceof WatchInfo)) continue;
            WatchInfo data = (WatchInfo) item.getObject();
            if (info.equals(data)) {
                return true;
            }
        }
        return false;
    }

    private DialExpandInfo getDialExpandInfo() {
        final WatchManager watchManager = WatchManager.getInstance();
        final WatchConfigure watchConfigure = watchManager.getWatchConfigure(watchManager.getConnectedDevice());
        if (null == watchConfigure) return null;
        return watchConfigure.getDialExpandInfo();
    }

    private String getWatchName(WatchInfo info) {
        return info.getName();
    }

    @SuppressLint({"ResourceType", "UseCompatLoadingForColorStateLists"})
    private void updateWatchStatus(Context context, TextView textView, int status, WatchInfo watchInfo) {
        if (textView == null) return;
        String text = context.getString(R.string.download_watch);
        int colorId = R.color.btn_blue_2_gray_selector;
        int bgId = R.drawable.bg_watch_white_2_gray_selector;
        switch (status) {
            case WatchInfo.WATCH_STATUS_NOT_PAYMENT:
                if (watchInfo.getServerFile() != null && watchInfo.getServerFile().getPrice() > 0) {
                    text = CalendarUtil.formatString("¥ %s", HealthUtil.getPriceFormat(watchInfo.getServerFile().getPrice()));
                }
                break;
            case WatchInfo.WATCH_STATUS_EXIST:
                text = context.getString(R.string.use_watch);
                if (watchInfo.hasUpdate()) {
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

    public void updateWatchInfo(WatchInfo watchInfo) {
        if (null == watchInfo) return;
        int index = -1;
        for (int i = 0; i < getData().size(); i++) {
            DialShopItem item = getData().get(i);
            if (item.isHeader() || !(item.getObject() instanceof WatchInfo)) continue;
            WatchInfo cacheInfo = (WatchInfo) item.getObject();
            boolean isMatch = HealthUtil.isMatchInfo(cacheInfo, watchInfo);
            JL_Log.i(tag, "updateWatchInfo", "isMatch = " + isMatch + ", i = " + i);
            if (isMatch) {
                index = i;
                JL_Log.i(tag, "updateWatchInfo", "index = " + index + ", cacheInfo = " + cacheInfo + ", watchInfo = " + watchInfo);
                cacheInfo.setStatus(watchInfo.getStatus())
                        .setVersion(watchInfo.getVersion())
                        .setCustomBgFatPath(watchInfo.getCustomBgFatPath())
                        .setWatchFile(watchInfo.getWatchFile())
                        .setServerFile(watchInfo.getServerFile())
                        .setUpdateUUID(watchInfo.getUpdateUUID())
                        .setUpdateFile(watchInfo.getUpdateFile())
                        .setCircleDial(watchInfo.isCircleDial());
                break;
            }
        }
        JL_Log.i(tag, "updateWatchInfo", "index = " + index);
        if (index != -1) {
            notifyItemChanged(index);
        }
    }


}
