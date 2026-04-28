package com.jieli.healthaide.ui.device.music;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentMusicManagerBinding;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.dialog.MusicDownloadDialog;
import com.jieli.healthaide.util.CalendarUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/12/21 5:23 PM
 * @desc :
 */
public class MusicManagerFragment extends BaseFragment {

    private MusicManagerViewModel mViewModel;
    private FragmentMusicManagerBinding binding;
    private MusicAdapter musicAdapter;
    private MusicDownloadDialog musicDownloadDialog;

    public static MusicManagerFragment newInstance() {
        return new MusicManagerFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMusicManagerBinding.inflate(inflater, container, false);
        binding.viewTopbar.tvTopbarTitle.setText(getString(R.string.local_music));
        binding.viewTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.viewTopbar.tvTopbarRight.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_choose3_nol, 0, 0, 0);
        binding.viewTopbar.tvTopbarRight.setTextColor(getResources().getColor(R.color.blue_558CFF));
        binding.viewTopbar.tvTopbarRight.setVisibility(View.VISIBLE);
        binding.viewTopbar.tvTopbarRight.setOnClickListener(v -> {
            boolean isSelect = !binding.viewTopbar.tvTopbarRight.isSelected();
            for (Music m : musicAdapter.getData()) {
                m.setSelected(isSelect);
            }
            musicAdapter.notifyDataSetChanged();
            updateSelectCount();
            binding.viewTopbar.tvTopbarRight.setSelected(isSelect);
            binding.viewTopbar.tvTopbarRight.setCompoundDrawablesWithIntrinsicBounds(isSelect ? R.drawable.icon_choose3_sel : R.drawable.icon_choose3_nol, 0, 0, 0);
        });

        musicAdapter = new MusicAdapter();
        binding.rvMusicManager.setAdapter(musicAdapter);
        binding.rvMusicManager.setLayoutManager(new LinearLayoutManager(requireContext()));
        musicAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!musicAdapter.selectMode) return;
            Music music = musicAdapter.getItem(position);
            music.setSelected(!music.isSelected());
            musicAdapter.notifyItemChanged(position);
            updateSelectCount();
            binding.viewTopbar.tvTopbarRight.setSelected(false);
            binding.viewTopbar.tvTopbarRight.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_choose3_nol,0,0,0);
        });
        binding.btnMusicManager.setOnClickListener(v -> mViewModel.toNextMode());

        View footer = new View(requireContext());
        footer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ValueUtil.dp2px(requireContext(),94)));
        musicAdapter.addFooterView(footer);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(MusicManagerViewModel.class);
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
                requireActivity().finish();
            }
        });
        mViewModel.musicsMutableLiveData.observe(getViewLifecycleOwner(), musics -> {
            if (musics == null) return;
            musicAdapter.setList(musics);
            updateSelectCount();
        });

        mViewModel.modeLiveData.observe(getViewLifecycleOwner(), mode -> {
            switch (mode) {
                case MusicManagerViewModel.MODE_LIST:
                    binding.viewTopbar.tvTopbarRight.setVisibility(View.INVISIBLE);
                    binding.btnMusicManager.setText(getString(R.string.add_music));
                    musicAdapter.setSelectMode(false);
                    break;
                case MusicManagerViewModel.MODE_SELECT:
                    binding.viewTopbar.tvTopbarRight.setVisibility(View.VISIBLE);
                    updateSelectCount();
                    musicAdapter.setSelectMode(true);
                    updateSelectCount();
                    break;
                case MusicManagerViewModel.MODE_DOWNLOAD:
                    binding.viewTopbar.tvTopbarRight.setVisibility(View.INVISIBLE);
                    break;
            }

        });
        mViewModel.downloadEventMutableLiveData.observe(getViewLifecycleOwner(), event -> {
            switch (event.getType()) {
                case MusicDownloadEvent.TYPE_DOWNLOAD:
                    requireActivity().setResult(Activity.RESULT_OK);
                    if (musicDownloadDialog == null) {
                        musicDownloadDialog = new MusicDownloadDialog();
                        musicDownloadDialog.setCancelable(false);
                    }
                    if (!musicDownloadDialog.isShow()) {
                        musicDownloadDialog.setEvent(event);
                        musicDownloadDialog.show(getChildFragmentManager(), MusicDownloadDialog.class.getCanonicalName());
                    }
                    musicDownloadDialog.updateTaskInfo(event);
                    break;
                case MusicDownloadEvent.TYPE_FINISH:
                    musicDownloadDialog.updateTaskInfo(event);
//                        showTips(R.string.tip_transfer_finish);

//                        if (musicDownloadDialog != null) {
//                            musicDownloadDialog.dismiss();
//                        }
                    break;
                case MusicDownloadEvent.TYPE_CANCEL:
                    showTips(R.string.tip_transfer_canceled);
                    if (musicDownloadDialog != null) {
                        musicDownloadDialog.dismiss();
                    }
                    break;
                case MusicDownloadEvent.TYPE_ERROR:
                    showTips(R.string.tip_transfer_error);
                    if (musicDownloadDialog != null) {
                        musicDownloadDialog.dismiss();
                    }
                    break;
            }
        });
        mViewModel.getMusicList(requireContext());
    }


    //更新选中数量
    private void updateSelectCount() {
        List<Music> musics = musicAdapter.getData();
        int selectCount = 0;
        for (Music music : musics) {
            if (music.isSelected()) {
                selectCount++;
            }
        }
        binding.btnMusicManager.setText(getString(R.string.download_music, selectCount, musics.size()));
    }

    private static class MusicAdapter extends BaseQuickAdapter<Music, BaseViewHolder> {
        private boolean selectMode = false;

        public void setSelectMode(boolean selectMode) {
            this.selectMode = selectMode;
            List<Music> musics = getData();
            for (Music music : musics) {
                music.setSelected(false);
            }
            notifyDataSetChanged();
        }

        public MusicAdapter() {
            super(R.layout.item_music);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, Music music) {
            holder.setText(R.id.tv_music_index, String.valueOf(getItemPosition(music) + 1));
            holder.setText(R.id.tv_music_name, music.getTitle());
            holder.setText(R.id.tv_music_artist, music.getArtist());
            holder.setVisible(R.id.iv_music_select, selectMode);
            holder.setVisible(R.id.tv_music_index, !selectMode);
            holder.getView(R.id.iv_music_select).setSelected(music.isSelected());
            holder.setVisible(R.id.tv_music_size, true);
            float size = music.getSize() / 1024f / 1024f;
            holder.setText(R.id.tv_music_size, CalendarUtil.formatString("%.2fMB", size));
        }
    }


}