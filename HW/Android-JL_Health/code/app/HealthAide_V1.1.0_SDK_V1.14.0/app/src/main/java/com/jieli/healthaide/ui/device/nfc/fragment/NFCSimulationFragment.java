package com.jieli.healthaide.ui.device.nfc.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentNFCSimulationBinding;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.nfc.NFCActivity;
import com.jieli.healthaide.ui.device.nfc.NFCViewModel;
import com.jieli.healthaide.ui.device.nfc.adapter.BannerAdapter;
import com.jieli.healthaide.ui.device.nfc.bean.NfcStatus;
import com.jieli.jl_dialog.Jl_Dialog;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.List;

import static com.jieli.healthaide.ui.device.nfc.NFCDataHandler.STATE_NFC_READING;
import static com.jieli.healthaide.ui.device.nfc.NFCDataHandler.STATE_NFC_READ_FAIL_ENCRYPTION;
import static com.jieli.healthaide.ui.device.nfc.NFCDataHandler.STATE_NFC_READ_FAIL_MOVE_FAST;
import static com.jieli.healthaide.ui.device.nfc.NFCDataHandler.STATE_NFC_READ_FAIL_TIME_OUT;
import static com.jieli.healthaide.ui.device.nfc.NFCDataHandler.STATE_NFC_READ_PREPARE;
import static com.jieli.healthaide.ui.device.nfc.NFCDataHandler.STATE_NFC_READ_SUCCESS;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NFCSimulationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NFCSimulationFragment extends BaseFragment {
    private FragmentNFCSimulationBinding mBinding;
    private final int STEP_READ_CARD_PREPARE = 0;
    private final int STEP_READ_CARD_CHECKING = 1;
    private final int STEP_READ_CARD_SUCCESS = 2;
    private final int STEP_READ_CARD_FAIL_MOVE_FAST = 3;
    private final int STEP_READ_CARD_FAIL_ENCRYPTION = 4;
    private final int STEP_READ_CARD_FAIL_TIME_OUT = 5;
    private final int STEP_SIMULATION = 6;
    private final int STEP_SIMULATION_FAIL = 7;
    private NFCViewModel mNFCViewModel;
    private Jl_Dialog mTipsDialog;
    private boolean TEST_FUNCTION_VIEW = true;
    private final Handler mHandler = new Handler(msg -> {
        return true;
    });

    public NFCSimulationFragment() {
    }

    public static NFCSimulationFragment newInstance() {
        NFCSimulationFragment fragment = new NFCSimulationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_n_f_c_simulation, container, false);
        mBinding = FragmentNFCSimulationBinding.bind(view);
        mBinding.setFragment(this);
        mBinding.viewTopbar.tvTopbarLeft.setOnClickListener(view1 -> {
                    if (mNFCViewModel != null) {
                        mNFCViewModel.cancelGetTagData();
                    }
                    requireActivity().onBackPressed();
                }
        );
        mBinding.viewTopbar.tvTopbarTitle.setText(R.string.door_key_simulation);
        BannerAdapter bannerAdapter = new BannerAdapter();
        bannerAdapter.setNewInstance(getBannerSrc());
        mBinding.vpSimulation.setAdapter(bannerAdapter);
        mBinding.vpSimulation.setCurrentItem(getBannerSrc().size() * 5);
        mBinding.vpSimulation.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                int current = position % getBannerSrc().size();
                int[] rbSrc = new int[]{R.id.rb_1, R.id.rb_2};
                mBinding.radioGroup.check(rbSrc[current]);
            }
        });
        startBannerAutoScroll();
        mBinding.btNfcStartTesting.setText(getString(R.string.start_check));
        mBinding.btNfcStartTesting.setVisibility(View.VISIBLE);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mNFCViewModel = new ViewModelProvider(requireActivity()).get(NFCViewModel.class);
        mNFCViewModel.initNFCReadState();
        addObserve();
    }

    public void onButtonTestingClick(View view) {
        int tag = (int) view.getTag();
        switch (tag) {
            case STEP_READ_CARD_PREPARE:
                startNFCRead();
                break;
            case STEP_READ_CARD_FAIL_MOVE_FAST:
                retryNFCRead();
                break;
            case STEP_READ_CARD_FAIL_ENCRYPTION:
                analogCardNumber();
                break;
        }
    }

    public void onButtonAddFailClick() {
        retryNFCRead();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeNfcRead();
        if (requireActivity() instanceof NFCActivity) {
            NFCActivity nfcActivity = (NFCActivity) requireActivity();
            nfcActivity.setIsBanOnBackPressed(false);
        }
    }

    private void startBannerAutoScroll() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(bannerRunnable, 10000);
    }

    private List<Integer> getBannerSrc() {
        ArrayList<Integer> bannerList = new ArrayList<>();
        bannerList.add(R.drawable.ic_nfc_simulation_wait_1);
        bannerList.add(R.drawable.ic_nfc_simulation_wait_2);
        return bannerList;
    }

    private void addObserve() {
        mNFCViewModel.getNFCReadState().observe(getViewLifecycleOwner(), state -> {
            JL_Log.d(tag, "addObserve", "state :: " + state);
            switch (state) {
                case STATE_NFC_READ_PREPARE:
                    updateView(STEP_READ_CARD_PREPARE);
                    break;
                case STATE_NFC_READING:
                    updateView(STEP_READ_CARD_CHECKING);
                    break;
                case STATE_NFC_READ_FAIL_MOVE_FAST:
                    updateView(STEP_READ_CARD_FAIL_MOVE_FAST);
                    break;
                case STATE_NFC_READ_FAIL_ENCRYPTION:
                    updateView(STEP_READ_CARD_FAIL_ENCRYPTION);
                    break;
                case STATE_NFC_READ_FAIL_TIME_OUT:
                    updateView(STEP_READ_CARD_FAIL_TIME_OUT);
                    break;
                case STATE_NFC_READ_SUCCESS:
                    updateView(STEP_READ_CARD_SUCCESS);
                    mNFCViewModel.addNfcFile();
                    break;
            }
        });
        mNFCViewModel.mAddNfcFileStatusMLD.observe(getViewLifecycleOwner(), this::handleNFCFileStatus);
    }

    /**
     * 打开NFC接收
     */
    private void openNfcRead() {
        if (requireActivity() instanceof NFCActivity) {
            NFCActivity nfcActivity = (NFCActivity) requireActivity();
            nfcActivity.openNfcRead();
        }
    }

    /**
     * 关闭NFC接收
     **/
    private void closeNfcRead() {
        if (requireActivity() instanceof NFCActivity) {
            NFCActivity nfcActivity = (NFCActivity) requireActivity();
            nfcActivity.closeNfcRead();
        }
    }

    /**
     * 开始读NFC卡
     */
    private void startNFCRead() {
        mNFCViewModel.prepareGetTag();
        mBinding.btNfcStartTesting.setVisibility(View.GONE);
        openNfcRead();
    }

    /**
     * 重试NFC读卡
     */
    private void retryNFCRead() {
        updateView(STEP_READ_CARD_PREPARE);
        startNFCRead();
    }

    /**
     * 模拟卡号
     */
    private void analogCardNumber() {

    }

    private void updateView(int step) {
        JL_Log.d(tag, "updateView", "step : " + step);
        mBinding.groupAddError.setVisibility(View.GONE);
        mBinding.groupNfcRead.setVisibility(View.GONE);
        mBinding.groupSimulation.setVisibility(View.GONE);
        Group visibleGroup = mBinding.groupNfcRead;
        switch (step) {
            case STEP_READ_CARD_PREPARE:
            case STEP_READ_CARD_CHECKING:
            case STEP_READ_CARD_SUCCESS:
            case STEP_READ_CARD_FAIL_MOVE_FAST:
            case STEP_READ_CARD_FAIL_ENCRYPTION:
                visibleGroup = mBinding.groupNfcRead;
                break;
            case STEP_READ_CARD_FAIL_TIME_OUT:
            case STEP_SIMULATION_FAIL:
                visibleGroup = mBinding.groupAddError;
                break;
            case STEP_SIMULATION:
                visibleGroup = mBinding.groupSimulation;
                break;
        }
        visibleGroup.setVisibility(View.VISIBLE);
        mBinding.viewTopbar.tvTopbarLeft.setVisibility(step == STEP_SIMULATION ? View.GONE : View.VISIBLE);
        if (requireActivity() instanceof NFCActivity) {
            NFCActivity nfcActivity = (NFCActivity) requireActivity();
            nfcActivity.setIsBanOnBackPressed(step == STEP_SIMULATION);
        }
        switch (step) {
            case STEP_READ_CARD_PREPARE:
                mBinding.tvNfcReadOperationName.setText(getString(R.string.prepare_read_card));
                mBinding.tvNfcReadOperationTip.setText(getString(R.string.card_close_phone));
                mBinding.ivNfcOperation.setImageResource(R.drawable.ic_nfc_read_prepare);
                mBinding.btNfcStartTesting.setTag(step);
                break;
            case STEP_READ_CARD_CHECKING:
                mBinding.tvNfcReadOperationName.setText(R.string.detecting);
                mBinding.tvNfcReadOperationTip.setText(R.string.detecting_no_move_card);
                mBinding.ivNfcOperation.setImageResource(R.drawable.ic_nfc_reading);
                mBinding.btNfcStartTesting.setVisibility(View.GONE);
                break;
            case STEP_READ_CARD_SUCCESS:
                mBinding.tvNfcReadOperationName.setText(R.string.detect_success);
                mBinding.tvNfcReadOperationTip.setText("");
                mBinding.ivNfcOperation.setImageResource(R.drawable.ic_nfc_read_success);
                mBinding.btNfcStartTesting.setVisibility(View.GONE);
                break;
            case STEP_READ_CARD_FAIL_MOVE_FAST:
                mBinding.tvNfcReadOperationName.setText(R.string.read_card_failed);
                mBinding.tvNfcReadOperationTip.setText(R.string.card_move_fast);
                mBinding.ivNfcOperation.setImageResource(R.drawable.ic_nfc_reading);
                mBinding.btNfcStartTesting.setTag(step);
                mBinding.btNfcStartTesting.setVisibility(View.VISIBLE);
                mBinding.btNfcStartTesting.setText(R.string.retry);
                break;
            case STEP_READ_CARD_FAIL_ENCRYPTION:
                mBinding.tvNfcReadOperationName.setText(R.string.read_card_failed);
                mBinding.tvNfcReadOperationTip.setText(R.string.read_card_failed_encryption);
                mBinding.ivNfcOperation.setImageResource(R.drawable.ic_nfc_reading);
                mBinding.btNfcStartTesting.setTag(step);
                mBinding.btNfcStartTesting.setVisibility(View.VISIBLE);
                mBinding.btNfcStartTesting.setText(R.string.analog_card_number);
                break;
            case STEP_READ_CARD_FAIL_TIME_OUT:
                break;
            case STEP_SIMULATION:
                break;
            case STEP_SIMULATION_FAIL:
                break;
        }
    }

    private void handleNFCFileStatus(NfcStatus nfcStatus) {
        switch (nfcStatus.getStatus()) {
            case NfcStatus.NFC_STATUS_START:
                updateView(STEP_SIMULATION);
                break;
            case NfcStatus.NFC_STATUS_WORKING:
                int progress = nfcStatus.getProgress();
                mBinding.progressBarWeight.setProgress(progress);
                break;
            case NfcStatus.NFC_STATUS_STOP:
                if (nfcStatus.getResult() == NfcStatus.RESULT_OK) {//传输完成
                    toDetailEdit();
                } else if (nfcStatus.getResult() == NfcStatus.RESULT_FAILURE) {//传输失败
                    if (nfcStatus.getCode() == NFCViewModel.ERROR_CODE_CARD_EXISTED) {
                        showDialog(getString(R.string.card_is_existed));
                    } else if (nfcStatus.getCode() == NFCViewModel.ERROR_CODE_CARD_NO_ONLINE_DEVICE) {
                        showDialog(getString(R.string.no_storage_device));
                    } else if (nfcStatus.getCode() == NFCViewModel.ERROR_CODE_CARD_FULL) {
                        showDialog(getString(R.string.nfc_card_over_limit));
                    } else {
                        updateView(STEP_SIMULATION_FAIL);
                    }
                } else if (nfcStatus.getResult() == NfcStatus.RESULT_CANCEL) {//传输取消
                    updateView(STEP_SIMULATION_FAIL);
                }
                break;
        }

    }

    /**
     * todo 前提是ViewModel 的cardList中包含了这个cardlist
     */
    private void toDetailEdit() {
        if (requireActivity() instanceof NFCActivity) {
            NFCActivity nfcActivity = (NFCActivity) requireActivity();
            nfcActivity.setIsBanOnBackPressed(true);
        }
        NavController navController = NavHostFragment.findNavController(this);
        Bundle bundle = new Bundle();
        bundle.putInt(NFCCardSettingFragment.NFC_CARD_SETTING_FRAGMENT_TYPE, NFCCardSettingFragment.SETTING_TYPE_ADD_DOOR_KEY);
        navController.navigate(R.id.action_NFCSimulationFragment_to_NFCCardSettingFragment, bundle);
    }

    private void showDialog(String content) {
        if (isDetached() || !isAdded()) return;
        if (null == mTipsDialog) {
            mTipsDialog = Jl_Dialog.builder()
                    .width(0.8f)
                    .cancel(false)
                    .title(getString(R.string.tips))
                    .content(content)
                    .contentColor(getResources().getColor(R.color.black_242424))
                    .right(getString(R.string.sure))
                    .rightColor(getResources().getColor(R.color.blue_558CFF))
                    .rightClickListener(((view, dialogFragment) -> {
                        dialogFragment.dismiss();
                        requireActivity().onBackPressed();
                    }))
                    .build();
        }
        if (!mTipsDialog.isShow()) {
            mTipsDialog.show(getChildFragmentManager(), "tips_dialog");
        }
    }

    private final Runnable bannerRunnable = new Runnable() {
        @Override
        public void run() {
            mBinding.vpSimulation.setCurrentItem(mBinding.vpSimulation.getCurrentItem() + 1);
            mBinding.vpSimulation.postDelayed(bannerRunnable, 10000);
        }
    };
}