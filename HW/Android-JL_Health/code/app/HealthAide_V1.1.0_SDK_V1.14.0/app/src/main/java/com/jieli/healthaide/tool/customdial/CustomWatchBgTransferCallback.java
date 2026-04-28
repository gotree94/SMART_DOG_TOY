package com.jieli.healthaide.tool.customdial;

import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.jl_rcsp.model.base.BaseError;

/**
 * @ClassName: CustomWatchBgTransferCallback
 * @Description: 手表自定义表盘传输回调
 * @Author: ZhangHuanMing
 * @CreateDate: 2024/2/23 16:12
 */
public interface CustomWatchBgTransferCallback {
    /**
     * 失败回调
     *
     * @param error 失败信息
     */
    void onFailed(BaseError error);

    /**
     * 开始传输自定义表盘背景
     *
     * @param path 传输表盘的文件路径
     */
    void onTransferCustomWatchBgStart(String path);

    /**
     * 传输自定义表盘背景进度
     *
     * @param progress 进度
     */
    void onTransferCustomWatchBgProgress(float progress);

    /**
     * 传输自定义表盘背景结束
     */
    void onTransferCustomWatchBgFinish();

    /**
     * 当前自定义表盘信息
     */
    void onCurrentWatchMsg(WatchInfo watchInfo);
}
