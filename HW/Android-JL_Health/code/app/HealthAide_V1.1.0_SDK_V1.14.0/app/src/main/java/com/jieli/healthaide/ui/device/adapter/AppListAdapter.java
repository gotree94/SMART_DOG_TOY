package com.jieli.healthaide.ui.device.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.component.bean.AppInfo;
import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.notification.NotificationHelper;
import com.kyleduo.switchbutton.SwitchButton;

import org.jetbrains.annotations.NotNull;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc APP列表适配器
 * @since 2021/4/29
 */
public class AppListAdapter extends BaseQuickAdapter<AppInfo, BaseViewHolder> implements LoadMoreModule {
    private final NotificationHelper mNotificationHelper = NotificationHelper.getInstance();

    public AppListAdapter() {
        super(R.layout.item_app_info);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder viewHolder, AppInfo appInfo) {
        viewHolder.setImageDrawable(R.id.iv_app_info_logo, appInfo.getLogo());
        viewHolder.setText(R.id.tv_app_info_name, appInfo.getName());
        viewHolder.setText(R.id.tv_app_info_package_name, appInfo.getPackageName());
        SwitchButton switchButton = viewHolder.getView(R.id.sbtn_app_info_choose_status);
        switchButton.setCheckedImmediatelyNoEvent(isSelectedApp(appInfo.getPackageName()));
        addChildClickViewIds(R.id.sbtn_app_info_choose_status);
        bindViewClickListener(viewHolder, R.id.sbtn_app_info_choose_status);
    }

    public void handleClickItem(AppInfo appInfo) {
        if (null == appInfo) return;
        String packageName = appInfo.getPackageName();
        if (null == packageName) return;
        if (isSelectedApp(packageName)) {
            mNotificationHelper.removePackageName(packageName);
        } else {
            mNotificationHelper.addPackageName(packageName);
        }
        notifyDataSetChanged();
    }

    public boolean isSelectedApp(String packageName) {
        if (null == packageName) return false;
        return mNotificationHelper.getPackageObserverList().contains(packageName);
    }
}
