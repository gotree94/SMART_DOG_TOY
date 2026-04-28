package com.jieli.watchtesttool.ui.widget.dialog;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.data.bean.SettingItem;
import com.jieli.watchtesttool.data.db.message.MessageEntity;
import com.jieli.watchtesttool.databinding.DialogAddMessageBinding;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothViewModel;
import com.jieli.watchtesttool.ui.base.BaseDialogFragment;
import com.jieli.watchtesttool.util.MessageUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 添加信息对话框
 * @since 2023/1/31
 */
public class AddMessageDialog extends BaseDialogFragment {

    private final OnResultListener mListener;
    private DialogAddMessageBinding mBinding;
    private BluetoothViewModel mViewModel;

    public AddMessageDialog(OnResultListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requireDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = requireDialog().getWindow();
        if (window != null) {
            //去掉dialog默认的padding
            window.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = Math.round(0.9f * getScreenWidth());
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.CENTER;
            //设置dialog的动画
//                lp.windowAnimations = R.style.BottomToTopAnim;
            window.setAttributes(lp);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        mBinding = DialogAddMessageBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(BluetoothViewModel.class);
        setCancelable(false);
        if (!mViewModel.isConnected()) {
            dismiss();
            return;
        }
        initUI();
        addObserver();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewModel.destroy();
    }

    private void initUI() {
        List<SettingItem> list = new ArrayList<>();
        for (int i = 1; i < 5; ++i) {
            list.add(new SettingItem(i, MessageUtil.getAppName(requireContext(), i)));
        }
        SettingAdapter adapter = new SettingAdapter(requireContext(), list);
        mBinding.spAppName.setAdapter(adapter);
        mBinding.spAppName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SettingAdapter adapter = (SettingAdapter) parent.getAdapter();
                SettingItem item = adapter.getItem(position);
                if (null == item) return;
                adapter.updateSelectedId(item.getId());
                mBinding.spAppName.setSelection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mBinding.btnCancel.setOnClickListener(v -> dismiss());
        mBinding.btnConfirm.setOnClickListener(v -> confirmResult());

        mBinding.etMessageTime.setHint(String.format(Locale.getDefault(), "%s: 2022-12-15 12:08:36", getString(R.string.hint_time_format)));
    }

    private void addObserver() {
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), connection -> {
            if (connection.getStatus() != StateCode.CONNECTION_OK) {
                dismiss();
            }
        });
    }

    private void confirmResult() {
        if (null == mViewModel.getConnectedDevice()) return;
        String title = mBinding.etMessageTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            mBinding.etMessageTitle.setError(getString(R.string.input_data_err));
            return;
        }
        long time = convertToTime(mBinding.etMessageTime.getText().toString());
        if (time == 0) {
            mBinding.etMessageTime.setText("");
            mBinding.etMessageTime.setError(getString(R.string.input_correct_format));
            return;
        }
        String content = mBinding.etMessageContent.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            mBinding.etMessageContent.setError(getString(R.string.input_data_err));
            return;
        }
        int flag = ((SettingItem) mBinding.spAppName.getSelectedItem()).getId();
        MessageEntity message = new MessageEntity();
        message.setMac(mViewModel.getConnectedDevice().getAddress());
        message.setFlag(flag);
        message.setPackageName(MessageUtil.getPackageName(flag));
        message.setTitle(title);
        message.setContent(content);
        message.setUpdateTime(time);
        if (null != mListener) {
            mListener.onResult(this, message);
        }
    }

    private long convertToTime(String text) {
        JL_Log.d(tag, "convertToTime >> " + text);
        if (TextUtils.isEmpty(text)) return Calendar.getInstance().getTimeInMillis();
        long time = 0;
        @SuppressLint("SimpleDateFormat") final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = dateFormat.parse(text);
            if (null != date) time = date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            JL_Log.e(tag, "convertToTime >> " + e.getMessage());
        }
        return time;
    }

    public interface OnResultListener {

        void onResult(DialogFragment dialog, MessageEntity message);
    }
}
