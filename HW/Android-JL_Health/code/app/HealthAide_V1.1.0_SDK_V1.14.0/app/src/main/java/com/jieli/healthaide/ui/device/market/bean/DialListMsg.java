package com.jieli.healthaide.ui.device.market.bean;

import androidx.annotation.NonNull;

import com.jieli.healthaide.ui.device.bean.WatchInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 表盘列表信息
 * @since 2022/6/24
 */
public class DialListMsg {
    private int currentPage;
    private int totalPage = -1;
    private int size;
    private final List<WatchInfo> list;

    public DialListMsg() {
        list = new ArrayList<>();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @NonNull
    public List<WatchInfo> getList() {
        return list;
    }

    public boolean isLoadFinish() {
        return (totalPage > 0 && currentPage == totalPage) || (list.size() > 0 && size == list.size());
    }

    @Override
    public String toString() {
        return "DialListMsg{" +
                "currentPage=" + currentPage +
                ", totalPage=" + totalPage +
                ", size=" + size +
                ", list=" + list +
                '}';
    }
}
