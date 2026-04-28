package com.jieli.healthaide.ui.device.file;


import android.annotation.SuppressLint;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.jl_filebrowse.bean.FileStruct;
import com.jieli.jl_rcsp.constant.AttrAndFunCode;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.util.ArrayList;


/**
 * Created by chensenhua on 2018/5/29.
 */

public class FileListAdapter extends BaseQuickAdapter<FileStruct, BaseViewHolder> implements LoadMoreModule {

    private int selectedCluster;
    private byte devIndex;
    private int deviceMode = -1;
    private boolean isPlay;


    public byte getDevIndex() {
        return devIndex;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelected(byte devIndex, int selectedCluster, boolean isPlay) {
        this.devIndex = devIndex;
        this.selectedCluster = selectedCluster;
        this.isPlay = isPlay;
        notifyDataSetChanged();
    }

    public int getSelectedCluster() {
        return selectedCluster;
    }

    public FileListAdapter(int layoutId) {
        super(layoutId, new ArrayList<FileStruct>());
    }

    public FileListAdapter() {
        this(R.layout.item_device_file);
    }


    @Override
    protected void convert(@NonNull BaseViewHolder holder, FileStruct item) {
        if (null == item) return;
        SwipeMenuLayout layout = holder.findView(R.id.swml);
        if (layout != null) {
            layout.setSwipeEnable(item.isFile());
        }
        boolean isSelected = isSelected(item);
        //清除原有的动画
        ImageView imageView = holder.getView(R.id.iv_device_type);
        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof AnimationDrawable) {
            ((AnimationDrawable) drawable).stop();
        }
        //当前播放的歌曲
        holder.setText(R.id.tv_device_file_name, item.getName());
        holder.getView(R.id.tv_device_file_name).setSelected(isSelected && isPlay);
        if (isSelected) {
            imageView.setImageResource(R.drawable.anim_song_playing);
            AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getDrawable();
            if (isMusicMode() && isPlay()) {
                animationDrawable.start();
            } else {
                animationDrawable.stop();
            }
        } else {
            holder.setImageResource(R.id.iv_device_type, item.isFile() ? R.drawable.ic_device_file_file : R.drawable.ic_device_file_floder);
        }
    }


    protected boolean isSelected(FileStruct fileStruct) {
        if (fileStruct == null) {
            return false;
        }
        return fileStruct.getCluster() == selectedCluster && fileStruct.getDevIndex() == devIndex;
    }


    @SuppressLint("NotifyDataSetChanged")
    public void setDeviceMode(int deviceMode) {
        if (this.deviceMode != deviceMode) {
            this.deviceMode = deviceMode;
            notifyDataSetChanged();
        }
    }

    protected boolean isMusicMode() {
        return deviceMode == AttrAndFunCode.SYS_INFO_FUNCTION_MUSIC;
    }


    public boolean isPlay() {
        return isPlay;
    }
}
