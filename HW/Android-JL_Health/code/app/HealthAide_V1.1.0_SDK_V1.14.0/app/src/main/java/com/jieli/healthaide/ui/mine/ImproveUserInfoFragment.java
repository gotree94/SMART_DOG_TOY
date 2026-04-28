package com.jieli.healthaide.ui.mine;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;

import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentImproveUserInfoBinding;
import com.jieli.healthaide.tool.unit.BaseUnitConverter;
import com.jieli.healthaide.tool.unit.CMUintConverter;
import com.jieli.healthaide.tool.unit.Converter;
import com.jieli.healthaide.tool.unit.KGUnitConverter;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.dialog.ChooseDateDialog;
import com.jieli.healthaide.ui.dialog.ChooseNumberDialog;
import com.jieli.healthaide.ui.dialog.ChooseSexDialog;
import com.jieli.healthaide.ui.home.HomeActivity;
import com.jieli.healthaide.ui.mine.entries.CommonItem;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_filebrowse.interfaces.OperatCallback;
import com.jieli.jl_health_http.model.UserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/10/21 9:44 AM
 * @desc :
 */
public class ImproveUserInfoFragment extends CommonFragment {

    private static final int CODE_NICKNAME = 0x11;
    private FragmentImproveUserInfoBinding binding;
    private UserInfoViewModel viewModel;
    private final UserInfo userInfo = new UserInfo();


    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentImproveUserInfoBinding.inflate(inflater, container, false);
        CommonAdapter commonAdapter = new CommonAdapter();
        binding.rvPersonalInfo.setAdapter(commonAdapter);
        binding.rvPersonalInfo.setLayoutManager(new LinearLayoutManager(requireContext()));
        DividerItemDecoration decoration = new DividerItemDecoration(requireContext(), OrientationHelper.VERTICAL);
        decoration.setDrawable(getResources().getDrawable(R.drawable.line_gray_1dp));
        binding.rvPersonalInfo.addItemDecoration(decoration);
        binding.layoutTopbar.tvTopbarTitle.setText(R.string.input_user_msg);
        binding.layoutTopbar.tvTopbarLeft.setVisibility(View.GONE);
        commonAdapter.setOnItemClickListener((adapter, view, position) -> {
            CommonItem item = commonAdapter.getItem(position);
            if (item.getTitle().equals(getString(R.string.nickname))) {
                Intent intent = new Intent(requireContext(), ContentActivity.class);
                intent.putExtra(ContentActivity.FRAGMENT_TAG, ImproveEditNickNameFragment.class.getCanonicalName());
                Bundle bundle = new Bundle();
                bundle.putString(ImproveEditNickNameFragment.KEY_NICKNAME, userInfo.getNickname());
                intent.putExtra(ContentActivity.FRAGMENT_DATA, bundle);
                startActivityForResult(intent, CODE_NICKNAME);
            } else if (item.getTitle().equals(getString(R.string.sex))) {
                showSexDialog();
            } else if (item.getTitle().equals(getString(R.string.stature))) {
                showHeightDialog();
            } else if (item.getTitle().equals(getString(R.string.weight))) {
                showWeightDialog();
            } else if (item.getTitle().equals(getString(R.string.target))) {
                showStepTargetDialog();
            } else if (item.getTitle().equals(getString(R.string.date_of_birth))) {
                showBirthdayDialog();
            }
        });
        binding.layoutTopbar.tvTopbarRight.setTextSize(14);
        binding.layoutTopbar.tvTopbarRight.setText(getString(R.string.skip_progress));
        binding.layoutTopbar.tvTopbarRight.setVisibility(View.VISIBLE);
        binding.layoutTopbar.tvTopbarRight.setTextColor(getResources().getColor(R.color.auxiliary_widget));
        binding.layoutTopbar.tvTopbarRight.setOnClickListener(v -> toHomeActivity());
        binding.btnNextStep.setOnClickListener(v -> viewModel.updateUserInfo(userInfo));
        updateUserInfoView(userInfo);
        return binding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UserInfoViewModel.class);
        viewModel.httpStateLiveData.observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case UserInfoViewModel.HTTP_STATE_UPDATING:
                    showWaitDialog();
                    break;
                case UserInfoViewModel.HTTP_STATE_UPDATED_FINISH:
                    toHomeActivity();
                case UserInfoViewModel.HTTP_STATE_UPDATED_ERROR:
                    dismissWaitDialog();
                    break;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_NICKNAME && resultCode == Activity.RESULT_OK && data != null) {
            String nickName = data.getStringExtra(ImproveEditNickNameFragment.KEY_NICKNAME);
            userInfo.setNickname(nickName);
            updateUserInfoView(userInfo);
        }
    }

    private void showWeightDialog() {
        Converter converter = new KGUnitConverter().getConverter(BaseUnitConverter.getType());
        ChooseNumberDialog dialog = new ChooseNumberDialog((int) converter.value(10), (int) converter.value(250),
                getString(R.string.weight),
                converter.unit()
                , (int) converter.value(10), value -> {
            userInfo.setWeight((float) converter.origin(value));
            updateUserInfoView(userInfo);
        });
        dialog.show(getChildFragmentManager(), dialog.getClass().getCanonicalName());

    }

    private void showStepTargetDialog() {

        ChooseNumberDialog dialog = new ChooseNumberDialog(2000, 20000, getString(R.string.target), getString(R.string.step), userInfo.getStep(), value -> {
            userInfo.setStep(value);
            updateUserInfoView(userInfo);
        });
        dialog.setStep(1000);
        dialog.show(getChildFragmentManager(), dialog.getClass().getCanonicalName());
    }

    private void showHeightDialog() {
        Converter converter = new CMUintConverter().getConverter(BaseUnitConverter.getType());
        ChooseNumberDialog dialog = new ChooseNumberDialog((int) converter.value(50), (int) converter.value(250), getString(R.string.stature), converter.unit(), (int) converter.value(userInfo.getHeight()), value -> {
            userInfo.setHeight((int) converter.origin(value));
            updateUserInfoView(userInfo);
        });
        dialog.show(getChildFragmentManager(), dialog.getClass().getCanonicalName());
    }


    private void showBirthdayDialog() {
        UserInfo userInfo = this.userInfo;
        ChooseDateDialog dialog = new ChooseDateDialog(userInfo.getBirthYear(), userInfo.getBirthMonth(), userInfo.getBirthDay(), (year, month, day) -> {
            this.userInfo.setBirthYear(year);
            this.userInfo.setBirthMonth(month);
            this.userInfo.setBirthDay(day);
            updateUserInfoView(this.userInfo);
        });
        dialog.show(getChildFragmentManager(), dialog.getClass().getCanonicalName());

    }

    private void showSexDialog() {
        ChooseSexDialog chooseSexDialog = new ChooseSexDialog();
        chooseSexDialog.setCancelable(true);
        chooseSexDialog.setCurrentSex(userInfo.getGender() == 0 ? getString(R.string.man) : getString(R.string.woman));
        chooseSexDialog.setOnSexChooseListener(sex -> {
            if (sex.equals(getString(R.string.woman))) {
                userInfo.setGender(1);
            } else {
                userInfo.setGender(0);
            }
            updateUserInfoView(userInfo);
            chooseSexDialog.dismiss();

        });
        chooseSexDialog.show(getChildFragmentManager(), ChooseSexDialog.class.getCanonicalName());
    }


    private void updateUserInfoView(UserInfo userInfo) {
        List<CommonItem> list = new ArrayList<>();


        CommonItem nickName = new CommonItem();
        nickName.setTitle(getString(R.string.nickname));
        nickName.setShowNext(true);
        if (TextUtils.isEmpty(userInfo.getNickname())) {
            nickName.setTailString(getString(R.string.please_input));
        } else {
            nickName.setTailString(userInfo.getNickname());
        }
        list.add(nickName);

        CommonItem sex = new CommonItem();
        sex.setTitle(getString(R.string.sex));
        sex.setShowNext(true);

        if (userInfo.getGender() == -1) {
            sex.setTailString(getString(R.string.please_choose));
        } else {
            sex.setTailString(userInfo.getGender() == 0 ? getString(R.string.man) : getString(R.string.woman));
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
        Converter converter = new CMUintConverter().getConverter(BaseUnitConverter.getType());

        height.setTitle(getString(R.string.stature));
        height.setShowNext(true);
        if (userInfo.getHeight() > 0) {
            height.setTailString(((int) converter.value(userInfo.getHeight())) + converter.unit());
        } else {
            height.setTailString(getString(R.string.please_choose));
        }
        list.add(height);


        CommonItem weight = new CommonItem();
        converter = new KGUnitConverter().getConverter(BaseUnitConverter.getType());

        weight.setTitle(getString(R.string.weight));
        weight.setShowNext(true);
        if (userInfo.getWeight() > 0) {
            weight.setTailString(CalendarUtil.formatString("%.1f%s", converter.value(userInfo.getWeight()), converter.unit()));
        } else {
            weight.setTailString(getString(R.string.please_choose));
        }
        list.add(weight);


        CommonItem target = new CommonItem();
        target.setTitle(getString(R.string.target));
        target.setShowNext(true);
        if (userInfo.getStep() > 0) {
            target.setTailString(getString(R.string.format_step_number_target, userInfo.getStep()));
        } else {
            target.setTailString(getString(R.string.please_choose));
        }
        list.add(target);
        if (binding.rvPersonalInfo.getAdapter() != null) {
            ((CommonAdapter) binding.rvPersonalInfo.getAdapter()).setList(list);
        }

        for (CommonItem item : list) {
            if (item.getTailString().equals(getString(R.string.please_choose))) {
                binding.btnNextStep.setEnabled(false);
                return;
            } else if (item.getTailString().equals(getString(R.string.please_input))) {
                binding.btnNextStep.setEnabled(false);
                return;
            }
        }

        binding.btnNextStep.setEnabled(true);
    }

    private void toHomeActivity() {
        HealthApplication.getAppViewModel().requestProfile(new OperatCallback() {
            @Override
            public void onSuccess() {
                Intent i = new Intent(requireActivity(), HomeActivity.class);
                startActivity(i);
                requireActivity().finish();
            }

            @Override
            public void onError(int code) {
                showTips(getString(R.string.save_failed));
            }
        });

    }

}
