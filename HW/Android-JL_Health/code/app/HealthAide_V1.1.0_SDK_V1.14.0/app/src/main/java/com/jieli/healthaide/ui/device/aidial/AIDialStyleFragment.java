package com.jieli.healthaide.ui.device.aidial;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentAiDialStyleBinding;
import com.jieli.healthaide.tool.aiui.AIManager;
import com.jieli.healthaide.tool.aiui.iflytek.IflytekAIDialStyleHelper;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.watch.WatchViewModel;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.List;

/**
 * @ClassName: AIDialStyleFragment
 * @Description: AI表盘风格选择
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/9 14:12
 */
public class AIDialStyleFragment extends BaseFragment {
    private final String TAG = this.getClass().getSimpleName();
    private FragmentAiDialStyleBinding mBinding;
    private AIDialStyleAdapter mAIDialStyleAdapter;
    private WatchViewModel mWatchViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentAiDialStyleBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWatchViewModel = new ViewModelProvider(this).get(WatchViewModel.class);
        initViewTopBar();
        initView();
        mWatchViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
                finish();
            }
        });
    }

    private void initViewTopBar() {
        mBinding.viewTopbar.getRoot().setBackgroundColor(getResources().getColor(R.color.white));
        mBinding.viewTopbar.tvTopbarLeft.setOnClickListener(v -> back());
        mBinding.viewTopbar.tvTopbarLeft.setTextSize(16);
        mBinding.viewTopbar.tvTopbarLeft.setTextColor(getResources().getColor(R.color.black_242424));
        mBinding.viewTopbar.tvTopbarTitle.setText(getString(R.string.ai_dial));
        mBinding.viewTopbar.tvTopbarRight.setVisibility(View.INVISIBLE);
    }

    private void initView() {
        mAIDialStyleAdapter = new AIDialStyleAdapter();
        mBinding.rvAiDialStyle.setAdapter(mAIDialStyleAdapter);
//        List<AIDialStyleAdapter.AIDialStyle> styleList = new ArrayList<>();
//        styleList.add(new AIDialStyleAdapter.AIDialStyle(R.string.ai_dial_style_1, R.drawable.img_aiwatch_01));
//        styleList.add(new AIDialStyleAdapter.AIDialStyle(R.string.ai_dial_style_2, R.drawable.img_aiwatch_02));
//        styleList.add(new AIDialStyleAdapter.AIDialStyle(R.string.ai_dial_style_3, R.drawable.img_aiwatch_03));
//        styleList.add(new AIDialStyleAdapter.AIDialStyle(R.string.ai_dial_style_4, R.drawable.img_aiwatch_04));
//        styleList.add(new AIDialStyleAdapter.AIDialStyle(R.string.ai_dial_style_5, R.drawable.img_aiwatch_05));
//        styleList.add(new AIDialStyleAdapter.AIDialStyle(R.string.ai_dial_style_6, R.drawable.img_aiwatch_06));
        List<IflytekAIDialStyleHelper.IflytekAIDialStyle> styleList = IflytekAIDialStyleHelper.getInstance().getIflytekAIDialStyles();
        String paintStyle = IflytekAIDialStyleHelper.getInstance().getCurrentStyle();
        JL_Log.d(TAG, "initView", "setCurrentAIDialStyle : " + paintStyle);
        int selectPosition = 0;
        for (int i = 0; i < styleList.size(); i++) {
            IflytekAIDialStyleHelper.IflytekAIDialStyle style = styleList.get(i);
            if (TextUtils.equals(getString(style.textSrcId), paintStyle)) {
                selectPosition = i;
                break;
            }
        }
        mAIDialStyleAdapter.setSelectPosition(selectPosition);
        mAIDialStyleAdapter.setList(styleList);
        mAIDialStyleAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (position != mAIDialStyleAdapter.getSelectPosition()) {
                mAIDialStyleAdapter.setSelectPosition(position);
                IflytekAIDialStyleHelper.IflytekAIDialStyle style = mAIDialStyleAdapter.getData().get(position);
                IflytekAIDialStyleHelper.getInstance().setCurrentStyleId(style.id);
                if (!AIManager.isInit()) return;
                AIManager.getInstance().getAIDial().setCurrentAIDialStyle(getString(style.textSrcId));
            }
        });
    }
}
