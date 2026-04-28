package com.jieli.healthaide.ui.dialog;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.DialogMusicDownloadBinding;
import com.jieli.healthaide.ui.base.BaseDialogFragment;
import com.jieli.healthaide.ui.device.music.MusicDownloadEvent;
import com.jieli.healthaide.ui.device.music.MusicManagerViewModel;

/**
 * 音乐下载界面
 *
 * @author zqjasonZhong
 * @since 2021/3/5
 */
public class MusicDownloadDialog extends BaseDialogFragment {

    private DialogMusicDownloadBinding binding;
    private MusicDownloadEvent event;

    public void setEvent(MusicDownloadEvent event) {
        this.event = event;
    }

    private MusicManagerViewModel mViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
//            Window window = getDialog().getWindow();
//            if (window != null) {
//                //去掉dialog默认的padding
//
//                WindowManager.LayoutParams lp = window.getAttributes();
//                lp.width = Math.round(1f * getScreenWidth());
//                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//                lp.gravity = Gravity.BOTTOM;
//                window.setAttributes(lp);
//                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//
//                window.getDecorView().setPadding(0, 0, 0, ValueUtil.dp2px(getContext(), 17));
//              }
        }
        View view = inflater.inflate(R.layout.dialog_music_download, container, false);
        binding = DialogMusicDownloadBinding.bind(view);
        binding.btnCancelMusicDownload.setOnClickListener(v -> dismiss());
        binding.tvMusicDownloadTransferCount.setText(getString(R.string.transfer_info, 0, 0));
        updateTaskInfo(event);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        if (window != null) {
            //去掉dialog默认的padding
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = Math.round(  getScreenWidth()-ValueUtil.dp2px(getContext(), 24));
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.BOTTOM;
            window.setAttributes(lp);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.getDecorView().setPadding(0, 0, 0, ValueUtil.dp2px(getContext(), 17));
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(MusicManagerViewModel.class);
        binding.btnCancelMusicDownload.setOnClickListener(v -> mViewModel.cancelTransfer());
    }


    @SuppressLint("SetTextI18n")
    public void updateTaskInfo(MusicDownloadEvent event) {
        if (event == null || binding == null || getContext() == null) return;
        binding.tvMusicDownloadTransferMusicTitle.setText(event.getName());
        binding.tvMusicDownloadTransferCount.setText(getString(R.string.transfer_info, event.getIndex(), event.getTotal()));
        binding.tvMusicDownloadTransferPercent.setText(event.getPercent() + "%");
        binding.pbMusicDownload.setProgress(event.getPercent());
        binding.tvMusicDownloadFinishTip.setVisibility(event.getType() == MusicDownloadEvent.TYPE_FINISH ? View.VISIBLE : View.INVISIBLE);
        binding.btnCancelMusicDownload.setText(event.getType() == MusicDownloadEvent.TYPE_FINISH ? R.string.sure : R.string.cancel);
        binding.btnCancelMusicDownload.setTextColor(getResources().getColor(event.getType() == MusicDownloadEvent.TYPE_FINISH ? R.color.auxiliary_widget : R.color.text_important_color));
        if (event.getType() == MusicDownloadEvent.TYPE_FINISH) {
            binding.btnCancelMusicDownload.setOnClickListener(v -> dismiss());
            binding.tvMusicDownloadFinishTip.setText(getString(R.string.tip_download_finished,event.getTotal()));
        }

    }


}
