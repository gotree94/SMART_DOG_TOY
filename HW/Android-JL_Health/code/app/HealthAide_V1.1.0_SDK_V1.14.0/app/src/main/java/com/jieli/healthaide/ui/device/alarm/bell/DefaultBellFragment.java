package com.jieli.healthaide.ui.device.alarm.bell;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.gson.Gson;
import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.base.BaseFragment;

import java.util.List;


/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2020/9/2 9:41 AM
 * @desc :
 */
public class DefaultBellFragment extends BaseFragment {
    private AlarmDefaultBellAdapter mBellAdapter;
    private BellViewModel mViewModel;
    private int initIndex = -1;

    public void setInitIndex(int initIndex) {
        this.initIndex = initIndex;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(getContext().getResources().getColor(R.color.bg_color));
        linearLayout.setPadding(0, ValueUtil.dp2px(getContext(), 8), 0, 0);

        RecyclerView recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mBellAdapter = new AlarmDefaultBellAdapter();
        mBellAdapter.setOnItemClickListener((adapter, view, position) -> {
            //重复点选中，则重新试听
            if (mBellAdapter.getData().get(position).isSelected()) {
                audition(mBellAdapter.getData().get(position));
                return;
            }
            //将其他项状态修改为false
            for (int i = 0; i < mBellAdapter.getData().size(); i++) {
                if (mBellAdapter.getData().get(i).isSelected()) {
                    mBellAdapter.getData().get(i).setSelected(false);
                    mBellAdapter.notifyItemChanged(i);
                }
            }
            mBellAdapter.getData().get(position).setSelected(true);
            mBellAdapter.notifyItemChanged(position);
            audition(mBellAdapter.getData().get(position));
        });
        recyclerView.setAdapter(mBellAdapter);
        linearLayout.addView(recyclerView);
        return linearLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(BellViewModel.class);
        mViewModel.bellsMutableLiveData.observe(getViewLifecycleOwner(), new Observer<List<BellInfo>>() {
            @Override
            public void onChanged(List<BellInfo> defaultAlarmBells) {
                setNewData(defaultAlarmBells);
            }
        });
        mViewModel.readAlarmBell();
    }


    private void setNewData(List<BellInfo> list) {
        for (BellInfo bell : list) {
            bell.setSelected(bell.getCluster() == initIndex);
        }
        mBellAdapter.setNewInstance(list);
    }

    private void audition(BellInfo info) {
        Intent intent = new Intent();
        intent.putExtra(AlarmBellContainerFragment.KEY_BELL_INFO, new Gson().toJson(info));
        requireActivity().setResult(Activity.RESULT_OK, intent);
        mViewModel.startBellAudition((byte) 0, (byte) 0, info.getCluster());
    }


    private static class AlarmDefaultBellAdapter extends BaseQuickAdapter<BellInfo, BaseViewHolder> {
        public AlarmDefaultBellAdapter() {
            super(R.layout.item_alarm_bell);
        }
        @Override
        protected void convert(BaseViewHolder holder, final BellInfo ring) {
            if (holder != null) {
                ((TextView) holder.getView(R.id.tv_bell_name)).setText(TextUtils.isEmpty(ring.getName()) ? getContext().getString(R.string.unnamed) : ring.getName());
                holder.getView(R.id.tv_bell_name).setSelected(ring.isSelected());
                holder.getView(R.id.iv_bell_state).setSelected(ring.isSelected());
                holder.setVisible(R.id.view_bell_line, getItemPosition(ring) < getData().size() - 1);
            }
        }
    }


}
