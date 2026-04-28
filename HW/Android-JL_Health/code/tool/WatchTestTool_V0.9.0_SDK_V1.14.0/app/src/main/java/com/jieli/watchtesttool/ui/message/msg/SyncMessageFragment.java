package com.jieli.watchtesttool.ui.message.msg;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jieli.component.utils.ToastUtil;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.model.NotificationMsg;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.data.db.message.MessageEntity;
import com.jieli.watchtesttool.databinding.FragmentSyncMessageBinding;
import com.jieli.watchtesttool.tool.test.LogDialog;
import com.jieli.watchtesttool.tool.test.message.SyncNotifyMessageTask;
import com.jieli.watchtesttool.ui.base.BaseFragment;
import com.jieli.watchtesttool.ui.widget.dialog.AddMessageDialog;
import com.jieli.watchtesttool.util.AppUtil;

import java.util.List;
import java.util.Locale;

/**
 * 同步消息测试
 */
public class SyncMessageFragment extends BaseFragment {

    private SyncMessageViewModel mViewModel;
    private FragmentSyncMessageBinding mBinding;
    private MessageAdapter mAdapter;

    private final Handler mUIHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = FragmentSyncMessageBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SyncMessageViewModel.class);
        initUI();
        addObserver();
        mViewModel.queryMessageList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewModel.destroy();
    }

    private void initUI() {
        mBinding.viewMessageTopBar.tvTopbarTitle.setText(getString(R.string.func_message_sync));
        mBinding.viewMessageTopBar.tvTopbarLeft.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_back_black, 0, 0, 0);
        mBinding.viewMessageTopBar.tvTopbarLeft.setOnClickListener(v -> requireActivity().finish());

        mBinding.btnMessageAutoTest.setOnClickListener(v -> {
            int testCount = AppUtil.getTextValue(mBinding.etTestCount, 1);
            if (testCount <= 0 || testCount > 999) {
                mBinding.etTestCount.setError(String.format(Locale.getDefault(), "%s [%d, %d]",
                        getString(R.string.input_value_err), 1, 999));
                return;
            }
            List<MessageEntity> entities = mAdapter.getData();
            if (entities.isEmpty()) {
                ToastUtil.showToastShort(getString(R.string.add_test_message));
                return;
            }
            boolean isOnlyPush = mBinding.rbtnPush.isChecked();
            NotificationMsg[] messages;
            if (isOnlyPush) {
                messages = new NotificationMsg[entities.size()];
                for (int i = 0; i < messages.length; i++) {
                    messages[i] = entities.get(i).convertData();
                }
            } else {
                messages = new NotificationMsg[entities.size() * 2];
                int j = 0;
                for (int i = 0; i < messages.length; i += 2) {
                    messages[i] = entities.get(j).convertData();
                    messages[i + 1] = entities.get(j).convertData(NotificationMsg.OP_REMOVE);
                    j++;
                }
            }
            SyncNotifyMessageTask task = new SyncNotifyMessageTask(mViewModel.getWatchManager(), testCount, messages);
            startTaskWithDialog(task);
        });

        mBinding.rvMessageInfo.setLayoutManager(new LinearLayoutManager(requireContext()));
        mAdapter = new MessageAdapter(new MessageAdapter.OnEventListener() {
            @Override
            public void onSend(int position, MessageEntity message) {
                NotificationMsg msg = message.convertData();
                SyncNotifyMessageTask task = new SyncNotifyMessageTask(mViewModel.getWatchManager(), msg);
                startTaskWithDialog(task);
            }

            @Override
            public void onRetract(int position, MessageEntity message) {
                NotificationMsg msg = message.convertData(NotificationMsg.OP_REMOVE);
                SyncNotifyMessageTask task = new SyncNotifyMessageTask(mViewModel.getWatchManager(), msg);
                startTaskWithDialog(task);
            }

            @Override
            public void onDelete(int position, MessageEntity message) {
                mViewModel.deleteMessage(message);
            }
        });
        mBinding.rvMessageInfo.setAdapter(mAdapter);
        View footerView = LayoutInflater.from(requireContext()).inflate(R.layout.view_add_item, mBinding.rvMessageInfo, false);
        footerView.setOnClickListener(v -> {
            AddMessageDialog dialog = null;
            Fragment fragment = getChildFragmentManager().findFragmentByTag(AddMessageDialog.class.getSimpleName());
            if (fragment instanceof AddMessageDialog) {
                dialog = (AddMessageDialog) fragment;
            }
            if (null == dialog) {
                dialog = new AddMessageDialog((dialog1, message) -> {
                    if (!mViewModel.insertMessage(message)) {
                        ToastUtil.showToastShort(getString(R.string.repeat_info));
                    }
                    dialog1.dismiss();
                });
            }
            if (!dialog.isShow()) {
                dialog.show(getChildFragmentManager(), AddMessageDialog.class.getSimpleName());
            }

        });
        mAdapter.addFooterView(footerView);
        mAdapter.setFooterWithEmptyEnable(true);
    }

    private void addObserver() {
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), connection -> {
            if (connection.getStatus() != StateCode.CONNECTION_OK) {
                requireActivity().finish();
            }
        });
        mViewModel.messageListMLD.observe(getViewLifecycleOwner(), messageEntities -> mAdapter.setList(messageEntities));
    }

    private void startTaskWithDialog(final SyncNotifyMessageTask task) {
        if (null == task) return;
        final LogDialog dialog = new LogDialog(task, v -> task.stopTest());
        dialog.show(getChildFragmentManager(), LogDialog.class.getSimpleName());
        mUIHandler.postDelayed(task::startTest, 500);
    }
}