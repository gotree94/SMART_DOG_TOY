package com.jieli.healthaide.tool.notification;

import com.jieli.component.bean.AppInfo;

import java.util.List;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc
 * @since 2021/4/29
 */
public interface OnReadAppListListener {

    void onResult(List<AppInfo> list);
}
