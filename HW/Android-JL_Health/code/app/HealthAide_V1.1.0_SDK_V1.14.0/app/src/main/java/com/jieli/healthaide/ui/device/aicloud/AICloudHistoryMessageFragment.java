package com.jieli.healthaide.ui.device.aicloud;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentAiCloudHistoryMessageBinding;
import com.jieli.healthaide.tool.aiui.AIManager;
import com.jieli.healthaide.tool.aiui.chat.SessionInfo;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.watch.WatchViewModel;
import com.jieli.healthaide.ui.dialog.DeleteAiCloudHistoryDialog;
import com.jieli.healthaide.ui.widget.AICloudPopDialog;
import com.jieli.healthaide.ui.widget.CommonDecoration;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: AICloudFragment
 * @Description: AI云 消息历史记录Fragment -
 * 只负责从数据库中获取历史记录，AI交互处理放在Service去做，Service处理完结果存入数据库
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/7/17 14:02
 */
public class AICloudHistoryMessageFragment extends BaseFragment {
    private static final String TAG = "AICloudHistoryMessageFragment";
    private AICloudHistoryMessageViewModel mAICloudHistoryMessageViewModel;
    private WatchViewModel mWatchViewModel;
    private FragmentAiCloudHistoryMessageBinding mBinding;
    private int viewTopBarState = 0;
    private AICloudHistoryMessageAdapter mAdapter;
    private int page = 0;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean isStartRecord = false;
    private SessionInfo mLastSessionInfo = null;
    private boolean isFirstLoadMore = true;
    @SuppressLint("NotifyDataSetChanged")
    private final Observer<SessionInfo> mSessionInfoObserver = sessionInfo -> {
        JL_Log.e(TAG, "SessionInfoObserver", "" + sessionInfo);
        if (isFirstLoadMore) {
            isFirstLoadMore = false;
            mAICloudHistoryMessageViewModel.firstLoad();
        }
        if (sessionInfo != null) {
            if (isDeleteMode()) {//删除模式时切换正常模式
                viewTopBarState = 0;
                switchViewTopBar();
                mAdapter.setMode(0);
                mAdapter.cleanSelectedList();
                mAdapter.notifyDataSetChanged();
            }
            JL_Log.e(TAG, "SessionInfoObserver", "status : " + sessionInfo.getStatus());
            switch (sessionInfo.getStatus()) {
                case SessionInfo.STATE_IDLE://默认状态
                    isStartRecord = false;
//                        if ()
                    // TODO: 2023/8/16 当前的对话
                    break;
                case SessionInfo.STATE_RECORD_START://开始录音
                {
                    //删除掉上一条未完成的对话
                    JL_Log.d(TAG, "SessionInfoObserver", "STATE_RECORD_START ---> mLastSessionInfo: " + mLastSessionInfo);
                    if (mLastSessionInfo != null) {
                        JL_Log.d(TAG, "SessionInfoObserver", "STATE_RECORD_START ---> mLastSessionInfo getStatus: " + mLastSessionInfo.getStatus());
                    }
                    if (mLastSessionInfo != null && (mLastSessionInfo.getStatus() != SessionInfo.STATE_NLP_END
                            && mLastSessionInfo.getStatus() != SessionInfo.STATE_IDLE)) {
                        JL_Log.d(TAG, "SessionInfoObserver", "STATE_RECORD_START ---> mLastSessionInfo getSessionMessageList: " + mLastSessionInfo.getSessionMessageList().size());
                        for (AICloudMessage aiCloudMessage : mLastSessionInfo.getSessionMessageList()) {
                            mAdapter.remove(aiCloudMessage);
                        }
                    }
                }
                case SessionInfo.STATE_RECORDING://录音中
                {
                    if (!isStartRecord) {
                        isStartRecord = true;
                        mLastSessionInfo = sessionInfo;
                        List<AICloudMessage> aiCloudMessageList = sessionInfo.getSessionMessageList();
                        for (AICloudMessage aiCloudMessage : aiCloudMessageList) {
                            handleIsFirst(aiCloudMessage);
                        }
                        mAdapter.addData(aiCloudMessageList);
                        mBinding.rvAicloudMsgList.scrollToPosition(mAdapter.getData().size() - 1);
                    }
                }
                break;
                case SessionInfo.STATE_RECORD_END://录音结束
                case SessionInfo.STATE_IAT_END://语音识别结束
                    isStartRecord = false;
//                        mAdapter.notifyItemChanged(mAdapter.getData().size() - 1);
                    for (AICloudMessage aiCloudMessage : sessionInfo.getSessionMessageList()) {
                        JL_Log.d(TAG, "SessionInfoObserver", aiCloudMessage.getEntity().getText());
                        handleIsFirst(aiCloudMessage);
                        int position = mAdapter.getItemPosition(aiCloudMessage);
                        if (position == -1) {
                            JL_Log.e(TAG, "SessionInfoObserver", "end ---> addData: " + aiCloudMessage.getEntity().getText());
                            mAdapter.addData(aiCloudMessage);
                        } else {
                            mAdapter.notifyItemChanged(position);
                        }
                    }
                    mBinding.rvAicloudMsgList.scrollToPosition(mAdapter.getData().size() - 1);
                    break;
                case SessionInfo.STATE_NLP_END://语义识别结束
                    isStartRecord = false;
                    for (AICloudMessage aiCloudMessage : sessionInfo.getSessionMessageList()) {
                        JL_Log.e(TAG, "SessionInfoObserver", "STATE_NLP_END : " + aiCloudMessage.getEntity().getText());
                        handleIsFirst(aiCloudMessage);
                        if (mAdapter.getItemPosition(aiCloudMessage) == -1) {
                            JL_Log.e(TAG, "SessionInfoObserver", "STATE_NLP_END ---> addData: " + aiCloudMessage.getEntity().getText());
                            mAdapter.addData(aiCloudMessage);
                        }
                    }
                    mBinding.rvAicloudMsgList.scrollToPosition(mAdapter.getData().size() - 1);
                    break;
                case SessionInfo.STATE_FAIL://异常
                    //删除掉
                    isStartRecord = false;
                    for (AICloudMessage aiCloudMessage : sessionInfo.getSessionMessageList()) {
                        mAdapter.remove(aiCloudMessage);
                    }
                    break;
            }
            switchViewTopBar();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentAiCloudHistoryMessageBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAICloudHistoryMessageViewModel = new ViewModelProvider(this).get(AICloudHistoryMessageViewModel.class);
        initView();
        initViewModel();
        {
            Intent intent = new Intent(requireActivity().getApplicationContext(), AICloudPopDialog.class);
            intent.setAction(AICloudPopDialog.ACTION_AI_CLOUD);
            intent.putExtra(AICloudPopDialog.EXTRA_WRITE_SQL, false);
            requireActivity().getApplicationContext().getApplicationContext().startService(intent);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWatchViewModel = new ViewModelProvider(this).get(WatchViewModel.class);
        mWatchViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
                requireActivity().finish();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        {
            Intent intent = new Intent(requireActivity().getApplicationContext(), AICloudPopDialog.class);
            intent.setAction(AICloudPopDialog.ACTION_AI_CLOUD);
            intent.putExtra(AICloudPopDialog.EXTRA_WRITE_SQL, true);
            requireActivity().getApplicationContext().getApplicationContext().startService(intent);
        }
        if (!AIManager.isInit()) return;
        AIManager.getInstance().getAICloudServe().currentSessionMessageMLD.removeObserver(mSessionInfoObserver);
    }

    private void initView() {
        initViewTopBar();
        mBinding.llAicloudDelete.setOnClickListener(v -> {
            SessionInfo sessionInfo = AIManager.getInstance().getAICloudServe().currentSessionMessageMLD.getValue();
            if (sessionInfo != null && (sessionInfo.getStatus() != SessionInfo.STATE_NLP_END
                    && sessionInfo.getStatus() != SessionInfo.STATE_IDLE)) {
                showTips(R.string.wait_ai_response_tip);
                return;
            }
            DeleteAiCloudHistoryDialog dialog = new DeleteAiCloudHistoryDialog();
            dialog.setContentString(getResources().getString(R.string.ai_is_delete));
            dialog.setOnDialogListener(new DeleteAiCloudHistoryDialog.OnDialogListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onConfirm() {
                    dialog.dismiss();
                    List<AICloudMessage> list = mAdapter.getSelectedList();
                    boolean isContainCurrentSession = false;//是不是包含当前对话
                    int size = mAdapter.getData().size();
                    if (!list.isEmpty()) {
                        for (AICloudMessage item : list) {
                            if (mAdapter.getItemPosition(item) + 1 == size) {
                                isContainCurrentSession = true;
                                break;
                            }
                        }
                    }
                    if (isContainCurrentSession) {
                        AIManager.getInstance().getAICloudServe().stopTTS();
                    }
                    mAICloudHistoryMessageViewModel.deleteHistoryMessage(list);
                    if (!list.isEmpty()) {
                        for (AICloudMessage item : list) {
                            mAdapter.remove(item);
                        }
                    }
                    //切回正常模式
                    viewTopBarState = 0;
                    switchViewTopBar();
                    mAdapter.setMode(0);
                    mAdapter.cleanSelectedList();
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancel() {
                    dialog.dismiss();
                }
            });
            dialog.show(getChildFragmentManager(), "DeleteAiCloudHistoryDialog");
        });
        mAdapter = new AICloudHistoryMessageAdapter();
        mBinding.rvAicloudMsgList.addItemDecoration(new CommonDecoration(getContext(), RecyclerView.VERTICAL, getResources().getColor(R.color.half_transparent), ValueUtil.dp2px(getContext(), 16)));
        mBinding.rvAicloudMsgList.setAdapter(mAdapter);
        mAdapter.setEmptyView(R.layout.view_ai_message_empty);
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (isDeleteMode()) {//删除模式
                mAdapter.onSelectedItemClick(mAdapter.getItem(position));
            } else {//正常模式

            }
        });
        mAdapter.setOnItemLongClickListener((adapter, view, position) -> {
            AICloudMessage message = (AICloudMessage) adapter.getItem(position);
            View viewParent = view.findViewById(R.id.fl_aicloud_msg_content);
            showAddDevicePopWindow(viewParent, message);
            return true;
        });
        page = 1;
        mAdapter.getUpFetchModule().setUpFetchEnable(true);
        mAdapter.getUpFetchModule().setUpFetching(true);
        mAdapter.getUpFetchModule().setOnUpFetchListener(() -> {
            showWaitDialog(true);
            mAdapter.getUpFetchModule().setUpFetching(true);
            page++;
            JL_Log.i(TAG, "onUpFetch", "AICloudHistoryMessageFragment onUpFetch: ");
            mAICloudHistoryMessageViewModel.loadMore();
        });
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (page != 1) {//第一页的时候出现卡顿会加载三次
                    mAdapter.getUpFetchModule().setUpFetching(false);//手机卡顿时，会导致数据还没加进去，就走onUpFetch
                }
            }
        });
//        mAICloudHistoryMessageViewModel.loadMore();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showAddDevicePopWindow(View view, AICloudMessage message) {
        MessageMorePopWindow morePopWindow = new MessageMorePopWindow(getContext());
        morePopWindow.showPopupWindow(view);
        morePopWindow.setPopWindowListener(item -> {
            if (item == 0) {
                //复制文本
                ClipboardManager cmb = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(message.getEntity().getText().trim());
            } else {//多选
                viewTopBarState = 1;
                switchViewTopBar();
                mAdapter.setMode(1);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initViewModel() {
        mAICloudHistoryMessageViewModel.loadMoreMessageListMLD.observe(getViewLifecycleOwner(), aiCloudMessageEntities -> {//加载更多历史记录
            dismissWaitDialog();
            if (page == 1) {
                mAdapter.addData(0, aiCloudMessageEntities);//设置分页返回的数据，插入第一个位置
                mBinding.rvAicloudMsgList.scrollToPosition(mAdapter.getData().size() - 1);
                mHandler.postDelayed(() -> {
                    mAdapter.getUpFetchModule().setUpFetching(false);//手机卡顿时，会导致数据还没加进去，就走onUpFetch
                }, 100);
            } else {
                if (aiCloudMessageEntities.isEmpty()) {
                } else {
                    mAdapter.addData(0, aiCloudMessageEntities);//设置分页返回的数据，插入第一个位置
                }
            }
            switchViewTopBar();
        });
        if (!AIManager.isInit()) return;
        AIManager.getInstance().getAICloudServe().currentSessionMessageMLD.observeForever(mSessionInfoObserver);
    }

    private void handleIsFirst(AICloudMessage aiCloudMessage) {
        long time = aiCloudMessage.getEntity().getTime();
        boolean isFirst;
        List<AICloudMessage> mHistoryMessageList = mAdapter.getData();
        if (!mHistoryMessageList.isEmpty()) {
            JL_Log.d(TAG, "handleIsFirst", "history not Empty");
            AICloudMessage lastMessage = mHistoryMessageList.get(mHistoryMessageList.size() - 1);
            if (lastMessage != aiCloudMessage) {//是不是同一条消息
                long lastTime = lastMessage.getEntity().getTime();
                isFirst = mAICloudHistoryMessageViewModel.isFirst(time, lastTime);
            } else {
                isFirst = lastMessage.isFirstMessage();
            }
        } else {
            JL_Log.d(TAG, "handleIsFirst", "history isEmpty");
            isFirst = true;
        }
        aiCloudMessage.setFirstMessage(isFirst);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initViewTopBar() {
        mBinding.viewTopbar.tvTopbarLeft.setOnClickListener(v -> {
            if (isDeleteMode()) {
                viewTopBarState = 0;
                switchViewTopBar();
                mAdapter.setMode(0);
                mAdapter.cleanSelectedList();
                mAdapter.notifyDataSetChanged();
            } else {
                requireActivity().onBackPressed();
            }
        });
        mBinding.viewTopbar.tvTopbarLeft.setTextSize(16);
        mBinding.viewTopbar.tvTopbarLeft.setTextColor(getResources().getColor(R.color.black_242424));
        mBinding.viewTopbar.tvTopbarTitle.setText(R.string.ai_cloud_serve);
        mBinding.viewTopbar.tvTopbarRight.setText("");
        mBinding.viewTopbar.tvTopbarRight.setOnClickListener(v -> {
            SessionInfo sessionInfo = AIManager.getInstance().getAICloudServe().currentSessionMessageMLD.getValue();
            if (sessionInfo != null && (sessionInfo.getStatus() != SessionInfo.STATE_NLP_END
                    && sessionInfo.getStatus() != SessionInfo.STATE_IDLE)) {
                showTips(R.string.wait_ai_response_tip);
                return;
            }
            DeleteAiCloudHistoryDialog dialog = new DeleteAiCloudHistoryDialog();
            dialog.setContentString(getResources().getString(R.string.ai_is_delete_all));
            dialog.setOnDialogListener(new DeleteAiCloudHistoryDialog.OnDialogListener() {
                @Override
                public void onConfirm() {
                    dialog.dismiss();
                    mAICloudHistoryMessageViewModel.cleanHistoryMessage();
                    AIManager.getInstance().getAICloudServe().stopTTS();
                    mAdapter.setNewInstance(new ArrayList<>());
                }

                @Override
                public void onCancel() {
                    dialog.dismiss();
                }
            });
            dialog.show(getChildFragmentManager(), "DeleteAiCloudHistoryDialog");
        });
        switchViewTopBar();
    }

    private void switchViewTopBar() {
        mBinding.viewTopbar.tvTopbarLeft.setText(isDeleteMode() ? requireContext().getString(R.string.cancel) : "");
        mBinding.viewTopbar.tvTopbarLeft.setCompoundDrawablesRelativeWithIntrinsicBounds(isDeleteMode() ? 0 : R.drawable.ic_back_black, 0, 0, 0);
        int tvTopbarRightVisible = isDeleteMode() ? View.INVISIBLE : View.VISIBLE;
        if (mAdapter == null || mAdapter.getData().isEmpty()) {
            tvTopbarRightVisible = View.INVISIBLE;
        }
        mBinding.viewTopbar.tvTopbarRight.setVisibility(tvTopbarRightVisible);
        mBinding.viewTopbar.tvTopbarRight.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_delete_nol, 0);
        mBinding.llAicloudDelete.setVisibility(isDeleteMode() ? View.VISIBLE : View.GONE);
    }

    private boolean isDeleteMode() {
        return viewTopBarState == 1;
    }
}
