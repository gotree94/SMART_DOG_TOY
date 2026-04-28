package com.jieli.healthaide.ui.device.alarm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.contrarywind.adapter.WheelAdapter;
import com.contrarywind.view.WheelView;
import com.google.gson.Gson;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentAlarmSettingBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.alarm.bell.AlarmBellContainerFragment;
import com.jieli.healthaide.ui.device.alarm.bell.BellInfo;
import com.jieli.healthaide.ui.device.alarm.widget.DialogBellIntervalChose;
import com.jieli.healthaide.ui.device.alarm.widget.DialogBellTimeChose;
import com.jieli.jl_filebrowse.interfaces.OperatCallback;
import com.jieli.jl_rcsp.model.command.AlarmExpandCmd;
import com.jieli.jl_rcsp.model.device.AlarmBean;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/15/21 11:49 AM
 * @desc :  闹钟设置界面
 */
public class AlarmSettingFragment extends BaseFragment {
    public static final String KEY_ALARM_EDIT = "alarm";
    public static final String KEY_ALARM_EDIT_FLAG = "key_edit_flag"; //false:增加 true：编辑
    private final static int CODE_EDIT_ALARM_NAME = 0x22;
    private final static int CODE_EDIT_ALARM_BELL = 0x23;
    FragmentAlarmSettingBinding binding;
    private AlarmBean mAlarmBean;
    private AlarmViewModel mViewModel;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAlarmSettingBinding.inflate(inflater, container, false);
        binding.viewTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.viewTopbar.tvTopbarTitle.setText(R.string.alarm_edit_title);
        binding.viewTopbar.tvTopbarRight.setOnClickListener(v -> saveAlarm());
        binding.viewTopbar.tvTopbarRight.setVisibility(View.VISIBLE);
        binding.viewTopbar.tvTopbarRight.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_sure_black, 0);

        if (getArguments() != null) {
            Bundle arg = requireArguments();
            mAlarmBean = new Gson().fromJson(arg.getString(KEY_ALARM_EDIT), AlarmBean.class);
            binding.btnDelAlarm.setVisibility(arg.getBoolean(KEY_ALARM_EDIT_FLAG, false) ? View.VISIBLE : View.GONE);
            binding.viewTopbar.tvTopbarTitle.setText(arg.getBoolean(KEY_ALARM_EDIT_FLAG, false) ? R.string.alarm_edit_title : R.string.alarm_create_title);
        }
        if (mAlarmBean == null) {
            mAlarmBean = new AlarmBean();
        }

        String[] days = getResources().getStringArray(R.array.alarm_weeks_simple);
        List<String> list = new ArrayList<>(Arrays.asList(days));
        RepeatModeAdapter repeatModeAdapter = new RepeatModeAdapter(mAlarmBean.getRepeatMode());
        repeatModeAdapter.setList(list);
        binding.rvRepeat.setAdapter(repeatModeAdapter);
        binding.rvRepeat.setLayoutManager(new GridLayoutManager(requireContext(), 7));

        binding.hour.setDividerColor(R.color.divider_line_color);
        binding.min.setDividerColor(R.color.divider_line_color);

        binding.hour.setDividerWidth(4);
        binding.min.setDividerWidth(4);

        initTimeView(binding.hour, 23, mAlarmBean.getHour());
        initTimeView(binding.min, 59, mAlarmBean.getMin());

        binding.tvAlarmName.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(EditAlarmNameFragment.KEY_AlARM_NAME, mAlarmBean.getName());
            ContentActivity.startContentActivityForResult(AlarmSettingFragment.this, EditAlarmNameFragment.class.getCanonicalName(), bundle, CODE_EDIT_ALARM_NAME);
        });

        binding.tvAlarmBell.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(KEY_ALARM_EDIT, new Gson().toJson(mAlarmBean));
            ContentActivity.startContentActivityForResult(AlarmSettingFragment.this, AlarmBellContainerFragment.class.getCanonicalName(), bundle, CODE_EDIT_ALARM_BELL);
        });

        binding.btnDelAlarm.setOnClickListener(v -> mViewModel.deleteAlarm(mAlarmBean, new OperatCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() != null)
                    requireActivity().finish();
            }

            @Override
            public void onError(int code) {

            }
        }));
        return binding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AlarmViewModel.class);
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
                requireActivity().finish();
            }
        });
        refreshAlarmInfo();
        mViewModel.readExpandArg(mAlarmBean, this::updateAlarmTime);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null) return;
        if (requestCode == CODE_EDIT_ALARM_NAME) {
            mAlarmBean.setName(data.getStringExtra(EditAlarmNameFragment.KEY_AlARM_NAME));
        } else if (requestCode == CODE_EDIT_ALARM_BELL) {
            BellInfo bellInfo = new Gson().fromJson(data.getStringExtra(AlarmBellContainerFragment.KEY_BELL_INFO), BellInfo.class);
            mAlarmBean.setBellCluster(bellInfo.getCluster());
            mAlarmBean.setBellType(bellInfo.getType());
            mAlarmBean.setBellName(bellInfo.getName());
            mAlarmBean.setDevIndex(bellInfo.getDev());
        }
        refreshAlarmInfo();
    }

    private void refreshAlarmInfo() {
        binding.tvAlarmName.setText(mAlarmBean.getName());
        binding.tvAlarmBell.setText(mAlarmBean.getBellName());

    }

    private void saveAlarm() {
        if (!isFragmentValid() || null == binding.rvRepeat.getAdapter()) return;
        int mode = ((RepeatModeAdapter) binding.rvRepeat.getAdapter()).getMode();
        int hour = binding.hour.getCurrentItem();
        int min = binding.min.getCurrentItem();
        mAlarmBean.setHour((byte) hour);
        mAlarmBean.setMin((byte) min);
        mAlarmBean.setRepeatMode((byte) mode);
        mAlarmBean.setOpen(true);
        mViewModel.updateAlarm(mAlarmBean, isSuccess -> {
            if (isSuccess) {
                AlarmExpandCmd.BellArg bellArg = (AlarmExpandCmd.BellArg) binding.tvAlarmBellAlarmTimeTitle.getTag();
                //保存闹钟拓展参数
                mViewModel.saveBellArgs(bellArg, success -> {
                    showTips(success ? R.string.save_alarm_success : R.string.save_alarm_failed);
                    requireActivity().finish();
                });
            } else {
                showTips(R.string.save_alarm_failed);
            }
        });
    }


    private void initTimeView(WheelView wheelView, int max, int current) {
        wheelView.setDividerType(WheelView.DividerType.WRAP);
        wheelView.setItemsVisibleCount(5);
        wheelView.setAdapter(new ArrayWheelAdapter(max, 0));
        wheelView.setCurrentItem(current);
        wheelView.setTextSize(40);
    }


    private void updateAlarmTime(AlarmExpandCmd.BellArg bellArg) {
        if (bellArg == null) return;
        binding.tvAlarmBellAlarmTimeTitle.setTag(bellArg);//将闹钟参数放到tag中
        boolean showAlarmTime = bellArg.isCanSetAlarmBellTime();
        binding.tvAlarmBellAlarmTimeTitle.setVisibility(showAlarmTime ? View.VISIBLE : View.INVISIBLE);
        binding.tvAlarmBellAlarmTime.setVisibility(showAlarmTime ? View.VISIBLE : View.INVISIBLE);
        boolean showAlarmBellInterval = bellArg.isCanSetCount();
        binding.tvAlarmBellIntervalTitle.setVisibility(showAlarmBellInterval ? View.VISIBLE : View.INVISIBLE);
        binding.tvAlarmBellInterval.setVisibility(showAlarmBellInterval ? View.VISIBLE : View.INVISIBLE);
        View.OnClickListener intervalListener = v -> {
            DialogBellIntervalChose dialogBellIntervalChose = new DialogBellIntervalChose(bellArg, (count, interval) -> {
                bellArg.setCount((byte) count);
                bellArg.setInterval((byte) interval);
                updateAlarmTime(bellArg);
            });
            dialogBellIntervalChose.show(getChildFragmentManager(), dialogBellIntervalChose.getClass().getCanonicalName());
        };
        binding.tvAlarmBellIntervalTitle.setOnClickListener(intervalListener);
        binding.tvAlarmBellInterval.setOnClickListener(intervalListener);


        View.OnClickListener timeListener = v -> {
            DialogBellTimeChose dialogBellTimeChose = new DialogBellTimeChose(time -> {
                bellArg.setAlarmBellTime((byte) time);
                updateAlarmTime(bellArg);
            });
            dialogBellTimeChose.setCurrentTime(bellArg.getAlarmBellTime());
            dialogBellTimeChose.show(getChildFragmentManager(), dialogBellTimeChose.getClass().getCanonicalName());
        };

        binding.tvAlarmBellAlarmTimeTitle.setOnClickListener(timeListener);
        binding.tvAlarmBellAlarmTime.setOnClickListener(timeListener);
    }


    private static class ArrayWheelAdapter implements WheelAdapter<Integer> {

        private final int max;
        private final int min;

        public ArrayWheelAdapter(int max, int min) {
            this.max = max;
            this.min = min;
        }

        @Override
        public int getItemsCount() {
            return max - min + 1;
        }

        @Override
        public Integer getItem(int index) {
            return min + index;
        }

        @Override
        public int indexOf(Integer o) {
            return o - min;
        }
    }


    private static class RepeatModeAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
        private int mode;

        public RepeatModeAdapter(int mode) {
            super(R.layout.item_alarm_repeat);
            setMode(mode);
        }

        public int getMode() {
            return mode == 0xfe ? 0x01 : mode;
        }

        public void setMode(int mode) {
            this.mode = ((mode & 0x01) == 0x01) ? 0xfe : mode;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void convert(@NotNull BaseViewHolder holder, String item) {
            int pos = getItemPosition(item);
            boolean select = ((mode >> (pos + 1)) & 0x01) == 0x01;
            holder.setText(R.id.tv_week_text, item);
            holder.getView(R.id.tv_week_text).setSelected(select);
            holder.getView(R.id.cl_alarm_repeat_root).setOnClickListener(v -> {
                if (select) {
                    mode &= (0xfe << (pos + 1) | (0xfe >> (8 - (pos + 1))));//循环左移并置0
                } else {
                    mode |= 0x01 << (pos + 1);//置1
                }
                notifyDataSetChanged();
            });
        }
    }

}
