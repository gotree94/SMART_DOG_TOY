package com.jieli.healthaide.ui.device.health;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentUserInfoBinding;
import com.jieli.healthaide.tool.unit.BaseUnitConverter;
import com.jieli.healthaide.tool.unit.CMUintConverter;
import com.jieli.healthaide.tool.unit.Converter;
import com.jieli.healthaide.tool.unit.KGUnitConverter;
import com.jieli.healthaide.ui.dialog.ChooseDateDialog;
import com.jieli.healthaide.ui.dialog.ChooseNumberDialog;
import com.jieli.healthaide.ui.dialog.ChooseSexDialog;
import com.jieli.healthaide.ui.mine.CommonAdapter;
import com.jieli.healthaide.ui.mine.entries.CommonItem;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_rcsp.constant.AttrAndFunCode;
import com.jieli.jl_rcsp.model.device.health.UserInfo;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/10/11
 * @desc :
 */
public class UserInfoFragment extends BaseHealthSettingFragment {
    private FragmentUserInfoBinding binding;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user_info, container, false);
        binding = FragmentUserInfoBinding.bind(root);
        CommonAdapter commonAdapter = new CommonAdapter();
        binding.rvPersonalInfo.setAdapter(commonAdapter);
        binding.rvPersonalInfo.setLayoutManager(new LinearLayoutManager(requireContext()));
        DividerItemDecoration decoration = new DividerItemDecoration(requireContext(), OrientationHelper.VERTICAL);
        decoration.setDrawable(getResources().getDrawable(R.drawable.line_gray_1dp));
        binding.rvPersonalInfo.addItemDecoration(decoration);
        binding.layoutTopbar.tvTopbarTitle.setText(R.string.user_info);
        commonAdapter.setOnItemClickListener((adapter, view, position) -> {
            CommonItem item = commonAdapter.getItem(position);
            if (item.getTitle().equals(getString(R.string.sex))) {
                showSexDialog();
            } else if (item.getTitle().equals(getString(R.string.stature))) {
                showHeightDialog();
            } else if (item.getTitle().equals(getString(R.string.weight))) {
                showWeightDialog();
            } else if (item.getTitle().equals(getString(R.string.date_of_birth))) {
                showBirthdayDialog();
            }
        });
        binding.layoutTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.btnExitLogin.setVisibility(View.GONE);
        binding.btnDeleteAccount.setVisibility(View.GONE);

        return root;

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel.healthSettingInfoLiveData().observe(getViewLifecycleOwner(), healthSettingInfo -> {
            UserInfo userInfo = healthSettingInfo.getUserInfo();
            updateUserInfo(userInfo);

        });
        viewModel.requestHealthSettingInfo(0x01 << AttrAndFunCode.HEALTH_SETTING_TYPE_USER_INFO);
    }

    private void showWeightDialog() {
        UserInfo userInfo = viewModel.getHealthSettingInfo().getUserInfo();
        Converter converter = new KGUnitConverter().getConverter(BaseUnitConverter.getType());
        ChooseNumberDialog dialog = new ChooseNumberDialog((int) converter.value(10), (int) converter.value(250),
                getString(R.string.weight),
                converter.unit()
                , (int) converter.value(userInfo.getWeight()), value -> {

            userInfo.setWeight((short) converter.origin(value));
            JL_Log.i(tag, "showWeightDialog", userInfo.toString());
            viewModel.sendSettingCmd(userInfo);
        });
        dialog.show(getChildFragmentManager(), dialog.getClass().getCanonicalName());
    }


    private void showHeightDialog() {
        UserInfo userInfo = viewModel.getHealthSettingInfo().getUserInfo();
        Converter converter = new CMUintConverter().getConverter(BaseUnitConverter.getType());
        ChooseNumberDialog dialog = new ChooseNumberDialog((int) converter.value(25), (int) converter.value(250),
                getString(R.string.stature), converter.unit(), userInfo.getHeight(), value -> {
            userInfo.setHeight((short) converter.origin(value));
            viewModel.sendSettingCmd(userInfo);
        });
        dialog.show(getChildFragmentManager(), dialog.getClass().getCanonicalName());
    }


    private void showBirthdayDialog() {
        UserInfo userInfo = viewModel.getHealthSettingInfo().getUserInfo();
        ChooseDateDialog dialog = new ChooseDateDialog(userInfo.getBirthYear(), userInfo.getBirthMonth(), userInfo.getBirthDay(), (year, month, day) -> {
            userInfo.setBirthYear(year)
                    .setBirthMonth((byte) month)
                    .setBirthDay((byte) day);
            viewModel.sendSettingCmd(userInfo);
        });
        dialog.show(getChildFragmentManager(), dialog.getClass().getCanonicalName());

    }

    private void showSexDialog() {
        UserInfo userInfo = viewModel.getHealthSettingInfo().getUserInfo();
        ChooseSexDialog chooseSexDialog = new ChooseSexDialog();
        chooseSexDialog.setCancelable(true);
        chooseSexDialog.setCurrentSex(userInfo.getSex() == 0x01 ? getString(R.string.man) : getString(R.string.woman));
        chooseSexDialog.setOnSexChooseListener(sex -> {
            if (sex.equals(getString(R.string.woman))) {
                userInfo.setSex((byte) 0x00);
            } else {
                userInfo.setSex((byte) 0x01);
            }
            chooseSexDialog.dismiss();
            viewModel.sendSettingCmd(userInfo);
        });
        chooseSexDialog.show(getChildFragmentManager(), ChooseSexDialog.class.getCanonicalName());
    }


    private void updateUserInfo(UserInfo userInfo) {
        List<CommonItem> list = new ArrayList<>();
        CommonItem sex = new CommonItem();
        sex.setTitle(getString(R.string.sex));
        sex.setShowNext(true);
        if (userInfo.getSex() == -1) {
            sex.setTailString(getString(R.string.please_choose));
        } else {
            sex.setTailString(userInfo.getSex() == 0x01 ? getString(R.string.man) : getString(R.string.woman));
        }
        list.add(sex);
        boolean selectBirth = userInfo.getBirthYear() == 0 || userInfo.getBirthMonth() == 0 || userInfo.getBirthDay() == 0;
        CommonItem birthday = new CommonItem();
        birthday.setTitle(getString(R.string.date_of_birth));
        birthday.setShowNext(true);
        if (selectBirth) {
            birthday.setTailString(getString(R.string.please_choose));
        } else {
            birthday.setTailString(getString(R.string.format_birthday_year_month, userInfo.getBirthYear(), userInfo.getBirthMonth()));
        }
        list.add(birthday);

        CommonItem height = new CommonItem();
        height.setTitle(getString(R.string.stature));
        height.setShowNext(true);

        new CMUintConverter(null, userInfo.getHeight(), (value, unit) -> {
            if (value > 0) {
                height.setTailString(((int) value) + unit);
            } else {
                height.setTailString(getString(R.string.please_choose));
            }
        });

        list.add(height);
        CommonItem weight = new CommonItem();
        weight.setTitle(getString(R.string.weight));
        weight.setShowNext(true);


        new KGUnitConverter(null, userInfo.getWeight(), (value, unit) -> {
            if (value > 0) {
                weight.setTailString(CalendarUtil.formatString("%.1f%s", value, unit));
            } else {
                weight.setTailString(getString(R.string.please_choose));
            }
        });
        list.add(weight);


        ((CommonAdapter) Objects.requireNonNull(binding.rvPersonalInfo.getAdapter())).setList(list);
    }


}
