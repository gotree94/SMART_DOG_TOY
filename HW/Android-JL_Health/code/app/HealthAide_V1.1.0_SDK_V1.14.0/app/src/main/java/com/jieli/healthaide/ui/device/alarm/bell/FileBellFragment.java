package com.jieli.healthaide.ui.device.alarm.bell;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.gson.Gson;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.device.file.FileListAdapter;
import com.jieli.healthaide.ui.device.file.FilesFragment;
import com.jieli.jl_filebrowse.bean.FileStruct;
import com.jieli.jl_filebrowse.bean.SDCardBean;


/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2020/9/2 5:41 PM
 * @desc :
 */
public class FileBellFragment extends FilesFragment {

    private int initCluster = -1;
    private int initDev = -1;
    private BellViewModel mViewModel;

    public static FileBellFragment newInstance(SDCardBean sdCardBean, int dev, int cluster) {
        Bundle args = new Bundle();
        FileBellFragment fragment = new FileBellFragment();
        fragment.setSdCardBean(sdCardBean);
        fragment.setArguments(args);
        fragment.initCluster = cluster;
        fragment.initDev = dev;
        return fragment;
    }

    @Override
    protected void handleFileClick(FileStruct fileStruct) {
        Intent intent = new Intent();
        String bellName = fileStruct.getName();
        if (!TextUtils.isEmpty(bellName)) {
            //去掉后缀
            int lastDotIndex = bellName.lastIndexOf(".");
            bellName = bellName.substring(0, lastDotIndex);
            byte[] data = bellName.getBytes();
            if (data.length > 32) {
                for (int i = 9; i < bellName.length() - 1; i++) {
                    if (bellName.substring(0, i + 1).getBytes().length > 32) {
                        bellName = bellName.substring(0, i);
                        break;
                    }
                }
            }
        }
        BellInfo info = new BellInfo(fileStruct.getCluster(), bellName, false);
        info.setDev(fileStruct.getDevIndex());
        info.setType((byte) 1);
        intent.putExtra(AlarmBellContainerFragment.KEY_BELL_INFO, new Gson().toJson(info));
        requireActivity().setResult(Activity.RESULT_OK, intent);
        getFileListAdapter().setSelected(fileStruct.getDevIndex(), fileStruct.getCluster(),false);
        mViewModel.startBellAudition((byte) 1, fileStruct.getDevIndex(), fileStruct.getCluster());
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //忽律音乐模式的状态回调
//        BluetoothHelper.getInstance().unregisterBTEventCallback(mBTEventCallback);
        requireView().findViewById(R.id.view_topbar).setVisibility(View.GONE);
        mViewModel = new ViewModelProvider(requireActivity()).get(BellViewModel.class);
        getFileListAdapter().setSelected((byte) initDev, initCluster,false);
    }

    @Override
    protected FileListAdapter createFileAdapter() {
        return new BellFileListAdapter();
    }


    private static class BellFileListAdapter extends FileListAdapter {
        public BellFileListAdapter() {
            super(R.layout.item_alarm_bell);
        }

        @Override
        protected void convert(BaseViewHolder holder, FileStruct item) {
            boolean isSelected = isSelected(item);
            holder.setText(R.id.tv_bell_name, item.getName());
            holder.getView(R.id.iv_bell_state).setSelected(isSelected);
            holder.setImageResource(R.id.iv_bell_type, item.isFile() ? R.drawable.ic_device_file_file : R.drawable.ic_device_file_floder);
            holder.setVisible(R.id.view_bell_line, getItemPosition(item) < getData().size() - 1);
        }
    }

}
