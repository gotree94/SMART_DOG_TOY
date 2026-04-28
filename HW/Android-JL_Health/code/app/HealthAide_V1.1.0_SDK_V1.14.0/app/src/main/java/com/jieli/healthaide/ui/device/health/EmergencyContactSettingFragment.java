package com.jieli.healthaide.ui.device.health;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.component.utils.PreferencesHelper;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmemtEmergencyContactSettingBinding;
import com.jieli.healthaide.ui.widget.CustomTextWatcher;
import com.jieli.healthaide.util.FormatUtil;
import com.jieli.jl_filebrowse.interfaces.OperatCallback;
import com.jieli.jl_rcsp.model.device.health.FallDetection;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/10/26
 * @desc :
 */
public class EmergencyContactSettingFragment extends BaseHealthSettingFragment {
    public static String KEY_EMERGENCY_CONTACT = "key_emergency_contact";
    FragmemtEmergencyContactSettingBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragmemt_emergency_contact_setting, container, false);
        binding = FragmemtEmergencyContactSettingBinding.bind(root);
        binding.viewTopbar.tvTopbarTitle.setText(R.string.title_emergency_contact);
        binding.viewTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());


        binding.btnEditContact.setOnClickListener(v -> {
            binding.rlShowContact.setVisibility(View.GONE);
            binding.rlEditContact.setVisibility(View.VISIBLE);
            binding.btnSaveContact.setVisibility(View.VISIBLE);
        });

        binding.btnSaveContact.setOnClickListener(v -> {
            FallDetection fallDetection = viewModel.getHealthSettingInfo().getFallDetection();
            EditText editText = binding.etContact;
            String contact = editText.getText().toString().trim();
            if (TextUtils.isEmpty(contact)) {
                showTips(getString(R.string.phone_number_is_empty));
            } else if (!FormatUtil.checkPhoneNumber(contact)) {
                showTips(getString(R.string.phone_tips_format_err));
            } else {
                PreferencesHelper.putStringValue(requireContext(), KEY_EMERGENCY_CONTACT, contact);
                FallDetection copy = new FallDetection(fallDetection.toAttr().getAttrData());
                copy.setMode(FallDetection.MODE_CALL);
                copy.setContact(contact);
                binding.btnSaveContact.setClickable(false);
                viewModel.sendSettingCmd(copy, new OperatCallback() {
                    @Override
                    public void onSuccess() {
                        binding.btnSaveContact.setClickable(true);
                        requireActivity().onBackPressed();
                    }

                    @Override
                    public void onError(int code) {
                        binding.btnSaveContact.setClickable(true);
                    }
                });
            }

        });
        binding.etContact.addTextChangedListener(new CustomTextWatcher() {
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                boolean isPhoneNumber = FormatUtil.checkPhoneNumber(s.toString());
                binding.btnSaveContact.setEnabled(isPhoneNumber);
                binding.btnSaveContact.setTextColor(getResources().getColor(isPhoneNumber ? R.color.white : R.color.gray_B3B3B3));
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel.healthSettingInfoLiveData().observe(getViewLifecycleOwner(), healthSettingInfo -> {
            FallDetection fallDetection = healthSettingInfo.getFallDetection();
            String contact = fallDetection.getContact();
            if (!FormatUtil.checkPhoneNumber(contact)) {
                SharedPreferences sharedPreferences = PreferencesHelper.getSharedPreferences(getContext());
                contact = sharedPreferences.getString(KEY_EMERGENCY_CONTACT, "");
                if (!FormatUtil.checkPhoneNumber(contact)) {
                    contact = getString(R.string.no_setting);
                } else {
                    binding.etContact.setText(contact);
                }
            } else {
                binding.etContact.setText(contact);
            }
            binding.tvContact.setText(contact);
        });
    }
}
