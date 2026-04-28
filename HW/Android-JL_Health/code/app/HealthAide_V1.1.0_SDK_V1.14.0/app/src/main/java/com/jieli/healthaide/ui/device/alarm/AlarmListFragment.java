package com.jieli.healthaide.ui.device.alarm;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.gson.Gson;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentAlarmListBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_rcsp.model.device.AlarmBean;
import com.kyleduo.switchbutton.SwitchButton;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/15/21 9:23 PM
 * @desc :
 */
public class AlarmListFragment extends BaseFragment {

    private AlarmViewModel mViewModel;
    private FragmentAlarmListBinding binding;
    private AlarmAdapter alarmAdapter;

    public static AlarmListFragment newInstance() {
        return new AlarmListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAlarmListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AlarmViewModel.class);
        initUI();
        addObserver();
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.readAlarmList();
    }

    private void initUI(){
        binding.viewTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.viewTopbar.tvTopbarTitle.setText(R.string.alarm);
        binding.viewTopbar.tvTopbarRight.setVisibility(View.VISIBLE);
        binding.viewTopbar.tvTopbarRight.setText("");
        binding.viewTopbar.tvTopbarRight.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_add_device_black, 0);
        binding.viewTopbar.tvTopbarRight.setOnClickListener(v -> addAlarm());

        alarmAdapter = new AlarmAdapter();
        binding.rvAlarmList.setAdapter(alarmAdapter);
        binding.rvAlarmList.setLayoutManager(new LinearLayoutManager(requireContext()));
        DividerItemDecoration decoration = new DividerItemDecoration(requireContext(), OrientationHelper.VERTICAL);
        decoration.setDrawable(Objects.requireNonNull(ResourcesCompat.getDrawable(getResources(), R.drawable.line_gray_1dp, requireActivity().getTheme())));
        binding.rvAlarmList.addItemDecoration(decoration);
    }

    private void addObserver(){
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
                finish();
            }
        });
        mViewModel.alarmsMutableLiveData.observe(getViewLifecycleOwner(), alarmListInfo -> {
            List<AlarmBean> sorts = alarmListInfo.getAlarmBeans();
            Collections.sort(sorts, (o1, o2) -> Integer.compare(o1.getHour() * 60 + o1.getMin(), o2.getHour() * 60 + o2.getMin()));
            if(sorts.isEmpty()){
                binding.tvTips.setVisibility(View.GONE);
            }else {
                binding.tvTips.setVisibility(View.VISIBLE);
            }
            alarmAdapter.setList(sorts);
        });
    }

    private void addAlarm() {
        if (alarmAdapter.getData().size() > 4) {
            showTips(R.string.alarm_set_num_is_full);
            return;
        }
        editAlarm(mViewModel.createNewAlarm(), false);
    }

    private void editAlarm(AlarmBean alarmBean, boolean edit) {
        Bundle bundle = new Bundle();
        bundle.putString(AlarmSettingFragment.KEY_ALARM_EDIT, new Gson().toJson(alarmBean));
        bundle.putBoolean(AlarmSettingFragment.KEY_ALARM_EDIT_FLAG, edit);
        ContentActivity.startContentActivity(requireContext(), AlarmSettingFragment.class.getCanonicalName(), bundle);
    }

    private class AlarmAdapter extends BaseQuickAdapter<AlarmBean, BaseViewHolder> {

        public AlarmAdapter() {
            super(R.layout.item_alarm);
        }

        @SuppressLint("DefaultLocale")
        @Override
        protected void convert(@NotNull BaseViewHolder holder, AlarmBean alarmBean) {
            holder.setText(R.id.tv_alarm_time, CalendarUtil.formatString("%02d:%02d", alarmBean.getHour(), alarmBean.getMin()));
            holder.setText(R.id.tv_alarm_name, alarmBean.getName());
            holder.setText(R.id.tv_alarm_week, Util.getRepeatDescModify(requireContext(), alarmBean)
            );
            SwitchButton switchButton = holder.getView(R.id.sw_default_alarm);
            switchButton.setCheckedImmediatelyNoEvent(alarmBean.isOpen());
            switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                alarmBean.setOpen(isChecked);
                mViewModel.updateAlarm(alarmBean, success -> showTips(success ? R.string.save_alarm_success : R.string.save_alarm_failed));
            });
            holder.getView(R.id.btn_del_alarm).setOnClickListener(v -> mViewModel.deleteAlarm(alarmBean));
            holder.getView(R.id.rl_alarm_bg).setOnClickListener(v -> editAlarm(alarmBean, true));
        }
    }

}