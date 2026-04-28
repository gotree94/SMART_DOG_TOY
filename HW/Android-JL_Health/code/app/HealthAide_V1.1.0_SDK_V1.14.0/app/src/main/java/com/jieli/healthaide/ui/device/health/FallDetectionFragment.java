package com.jieli.healthaide.ui.device.health;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.component.utils.PreferencesHelper;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentFallDetectionBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.util.FormatUtil;
import com.jieli.jl_filebrowse.interfaces.OperatCallback;
import com.jieli.jl_rcsp.constant.AttrAndFunCode;
import com.jieli.jl_rcsp.model.device.health.FallDetection;
import com.jieli.jl_rcsp.model.device.health.HealthSettingInfo;
import com.jieli.jl_rcsp.util.JL_Log;

import static com.jieli.healthaide.ui.device.health.EmergencyContactSettingFragment.KEY_EMERGENCY_CONTACT;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/10/25
 * @desc :
 */
public class FallDetectionFragment extends BaseHealthSettingFragment implements View.OnClickListener {
    FragmentFallDetectionBinding binding;

    private static final int REQUEST_CODE_SET = 1245;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fall_detection, container, false);
        binding = FragmentFallDetectionBinding.bind(root);
        root.findViewById(R.id.tv_topbar_left).setOnClickListener(v -> requireActivity().onBackPressed());
        ((TextView) root.findViewById(R.id.tv_topbar_title)).setText(R.string.fall_detection);
        binding.swFallDetection.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FallDetection fallDetection = viewModel.getHealthSettingInfo().getFallDetection();
            FallDetection copy = new FallDetection(fallDetection.toAttr().getAttrData());
            copy.setEnable(isChecked);
            viewModel.sendSettingCmd(copy);
        });

        binding.tvModeBright.setOnClickListener(this);
        binding.tvModeShake.setOnClickListener(this);
        binding.tvModeCall.setOnClickListener(this);
        binding.tvModeBright.setTag(FallDetection.MODE_BRIGHT);
        binding.tvModeShake.setTag(FallDetection.MODE_SHAKE);
        binding.tvModeCall.setTag(FallDetection.MODE_CALL);

        binding.clEmergencyContact.setOnClickListener(v -> goToSetContact());

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel.healthSettingInfoLiveData().observe(getViewLifecycleOwner(), this::updateUI);
    }

    @Override
    public void onClick(View v) {
        byte status = (byte) v.getTag();
        FallDetection fallDetection = viewModel.getHealthSettingInfo().getFallDetection();
        FallDetection copy = new FallDetection(fallDetection.toAttr().getAttrData());
        copy.setMode(status);
        if (status == FallDetection.MODE_CALL && !FormatUtil.checkPhoneNumber(fallDetection.getContact())) {
            SharedPreferences sharedPreferences = PreferencesHelper.getSharedPreferences(getContext());
            String contact = sharedPreferences.getString(KEY_EMERGENCY_CONTACT, "");
            if (!FormatUtil.checkPhoneNumber(contact)) {
                goToSetContact();
                return;
            } else {
                FallDetection copy1 = new FallDetection(fallDetection.toAttr().getAttrData());
                copy1.setMode(FallDetection.MODE_CALL);
                copy1.setContact(contact);
                viewModel.sendSettingCmd(copy1, new OperatCallback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError(int code) {
                    }
                });
            }
        }
        viewModel.sendSettingCmd(copy);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SET) {
            JL_Log.i(tag, "onActivityResult", "REQUEST_CODE_SET");
            viewModel.requestHealthSettingInfo(0x01 << AttrAndFunCode.HEALTH_SETTING_TYPE_FALL_DETECTION);
        }
    }

    private void updateUI(HealthSettingInfo healthSettingInfo) {
        if (null == healthSettingInfo || !isFragmentValid()) return;
        FallDetection fallDetection = healthSettingInfo.getFallDetection();
        if (null == fallDetection) return;
        JL_Log.i(tag, "updateUI", "fallDetection : " + fallDetection);
        binding.swFallDetection.setCheckedImmediatelyNoEvent(fallDetection.isEnable());
        binding.tvModeBright.setCompoundDrawablesWithIntrinsicBounds(0, 0, fallDetection.isEnable() && fallDetection.getMode() == 0x00 ? R.drawable.ic_choose_blue : 0, 0);
        binding.tvModeShake.setCompoundDrawablesWithIntrinsicBounds(0, 0, fallDetection.isEnable() && fallDetection.getMode() == 0x01 ? R.drawable.ic_choose_blue : 0, 0);
        binding.tvModeCall.setCompoundDrawablesWithIntrinsicBounds(0, 0, fallDetection.isEnable() && fallDetection.getMode() == 0x02 ? R.drawable.ic_choose_blue : 0, 0);

        binding.tvModeBright.setClickable(fallDetection.isEnable());
        binding.tvModeShake.setClickable(fallDetection.isEnable());
        binding.tvModeCall.setClickable(fallDetection.isEnable());

        binding.tvModeBright.setAlpha(fallDetection.isEnable() ? 1.0f : 0.4f);
        binding.tvModeShake.setAlpha(fallDetection.isEnable() ? 1.0f : 0.4f);
        binding.tvModeCall.setAlpha(fallDetection.isEnable() ? 1.0f : 0.4f);


        binding.clEmergencyContact.setAlpha(fallDetection.isEnable() /*&& fallDetection.getMode() == 0x02 */ ? 1.0f : 0.4f);
        binding.clEmergencyContact.setClickable(fallDetection.isEnable() /*&& fallDetection.getMode() == 0x02*/);

        String contact = fallDetection.getContact();
        if (!FormatUtil.checkPhoneNumber(contact)) {
            SharedPreferences sharedPreferences = PreferencesHelper.getSharedPreferences(requireContext());
            contact = sharedPreferences.getString(KEY_EMERGENCY_CONTACT, "");
            if (!FormatUtil.checkPhoneNumber(contact)) {
                contact = getString(R.string.no_setting);
            }
        } else {
            PreferencesHelper.putStringValue(getContext(), KEY_EMERGENCY_CONTACT, contact);
        }

        binding.tvEmergencyContact.setText(contact);
    }

    private void goToSetContact() {
        ContentActivity.startContentActivityForResult(this, EmergencyContactSettingFragment.class.getCanonicalName(), null, REQUEST_CODE_SET);
    }
}
