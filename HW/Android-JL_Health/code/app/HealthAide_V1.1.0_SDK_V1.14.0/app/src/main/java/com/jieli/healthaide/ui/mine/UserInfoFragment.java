package com.jieli.healthaide.ui.mine;

import android.annotation.SuppressLint;
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

import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.dao.HealthDao;
import com.jieli.healthaide.data.db.HealthDataDbHelper;
import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.databinding.FragmentUserInfoBinding;
import com.jieli.healthaide.tool.unit.BaseUnitConverter;
import com.jieli.healthaide.tool.unit.CMUintConverter;
import com.jieli.healthaide.tool.unit.Converter;
import com.jieli.healthaide.tool.unit.KGUnitConverter;
import com.jieli.healthaide.tool.watch.synctask.ServerHealthDataSyncTask;
import com.jieli.healthaide.tool.watch.synctask.SyncTaskManager;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.dialog.ChooseDateDialog;
import com.jieli.healthaide.ui.dialog.ChooseNumberDialog;
import com.jieli.healthaide.ui.dialog.ChooseSexDialog;
import com.jieli.healthaide.ui.login.LoginActivity;
import com.jieli.healthaide.ui.mine.entries.CommonItem;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_dialog.Jl_Dialog;
import com.jieli.jl_health_http.model.UserInfo;
import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/10/21 9:44 AM
 * @desc :
 */
public class UserInfoFragment extends CommonFragment {

    private FragmentUserInfoBinding binding;
    private UserInfoViewModel viewModel;


    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserInfoBinding.inflate(inflater, container, false);
        CommonAdapter commonAdapter = new CommonAdapter();
        binding.rvPersonalInfo.setAdapter(commonAdapter);
        binding.rvPersonalInfo.setLayoutManager(new LinearLayoutManager(requireContext()));
        DividerItemDecoration decoration = new DividerItemDecoration(requireContext(), OrientationHelper.VERTICAL);
        decoration.setDrawable(getResources().getDrawable(R.drawable.line_gray_1dp));
        binding.rvPersonalInfo.addItemDecoration(decoration);
        binding.layoutTopbar.tvTopbarTitle.setText(R.string.user_info);
        commonAdapter.setOnItemClickListener((adapter, view, position) -> {
            CommonItem item = commonAdapter.getItem(position);
            if (item.getTitle().equals(getString(R.string.nickname))) {
                ContentActivity.startContentActivity(requireContext(), EditNickNameFragment.class.getCanonicalName());
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
        binding.layoutTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.btnExitLogin.setOnClickListener(v -> showExitLoginDialog());
//        binding.btnDeleteAccount.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
//        binding.btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        return binding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UserInfoViewModel.class);
        viewModel.userInfoLiveData.observe(getViewLifecycleOwner(), this::updateUserInfoView);

        viewModel.httpStateLiveData.observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case UserInfoViewModel.HTTP_STATE_REQUESTING:
                case UserInfoViewModel.HTTP_STATE_UPDATING:
                    showWaitDialog();
                    break;
                default:
                    dismissWaitDialog();
                    break;
            }
        });
//        viewModel.getUserInfo();
    }


    @Override
    public void onResume() {
        super.onResume();
        viewModel.getUserInfo();
    }


    private void showWeightDialog() {
        Converter converter = new KGUnitConverter().getConverter(BaseUnitConverter.getType());
        ChooseNumberDialog dialog = new ChooseNumberDialog((int) converter.value(10), (int) converter.value(250),
                getString(R.string.weight),
                converter.unit()
                , (int) converter.value(getUserInfo().getWeight()), value -> {
            Calendar currentCalendar = Calendar.getInstance();
            float originValue = (float) converter.origin(value);
            updateWeight(currentCalendar, originValue);
        });
        dialog.show(getChildFragmentManager(), dialog.getClass().getCanonicalName());
    }

    private void updateWeight(Calendar currentCalendar, float originValue) {
        String uid = HealthApplication.getAppViewModel().getUid();
        HealthDao healthDao = HealthDataDbHelper.getInstance().getHealthDao();
        long id = CalendarUtil.removeTime(currentCalendar.getTimeInMillis()) * 1000 + HealthEntity.DATA_TYPE_WEIGHT;
        // 先查询数据库取出当前的数据，然后插入
        HealthEntity entity = healthDao.findHealthById(HealthEntity.DATA_TYPE_WEIGHT, uid, id);
        if (entity == null) {
            entity = new HealthEntity();
            entity.setSpace((byte) 60);
            entity.setId(id);
            entity.setUid(uid);
            entity.setType(HealthEntity.DATA_TYPE_WEIGHT);
            byte[] data = new byte[11];
            data[0] = HealthEntity.DATA_TYPE_WEIGHT;
            data[1] = (byte) ((currentCalendar.get(Calendar.YEAR) >> 8) & 0xff);
            data[2] = (byte) (currentCalendar.get(Calendar.YEAR) & 0xff);
            data[3] = (byte) ((currentCalendar.get(Calendar.MONTH) + 1) & 0xff);
            data[4] = (byte) (currentCalendar.get(Calendar.DAY_OF_MONTH) & 0xff);
            data[5] = (byte) 0xcc;
            data[6] = (byte) 0xcc;
            data[7] = (byte) 0x00;
            data[8] = (byte) 0x05;
            entity.setData(data);
        }
        entity.setSync(false);
        entity.setVersion((byte) 0);
        entity.setTime(currentCalendar.getTimeInMillis());
        byte[] data = new byte[6];
        data[0] = (byte) (currentCalendar.get(Calendar.HOUR_OF_DAY) & 0xff);
        data[1] = (byte) (currentCalendar.get(Calendar.MINUTE) & 0xff);
        data[2] = (byte) 0x00;
        data[3] = (byte) 0x02;
        data[4] = CHexConver.intToByte((int) originValue);
        data[5] = CHexConver.intToByte((int) (originValue * 100 % 100));
        //不修改crc否则不会同步
        byte[] srcData = entity.getData();
        int crcCode = ValueUtil.bytesToInt(srcData[5], srcData[6]);
        crcCode++;
        byte[] newCrc = ValueUtil.int2byte2(crcCode);
        srcData[5] = newCrc[0];
        srcData[6] = newCrc[1];
        //找到对应位置插入
        int insertPosition = 11;
        int currentTime = ValueUtil.bytesToInt(data[0], data[1]);
        for (int i = 11; i + 6 <= srcData.length; i += 6) {
            int time = ValueUtil.bytesToInt(srcData[i], srcData[i + 1]);
            if (currentTime < time) {
                break;
            }
            insertPosition = i + 6;
        }
        byte[] resultData = new byte[srcData.length + data.length];
        System.arraycopy(srcData, 0, resultData, 0, insertPosition);
        System.arraycopy(data, 0, resultData, insertPosition, data.length);
        System.arraycopy(srcData, insertPosition, resultData, insertPosition + data.length, srcData.length - insertPosition);
        entity = HealthEntity.from(resultData);
        entity.setUid(uid);
        HealthDataDbHelper.getInstance().getHealthDao().insert(entity);//插入数据库
        SyncTaskManager syncTaskManager = SyncTaskManager.getInstance();
        syncTaskManager.addTaskDelay(new ServerHealthDataSyncTask(syncTaskManager), 200);
        UserInfo userInfo = viewModel.copyUserInfo();
        userInfo.setWeight(originValue);
        viewModel.updateUserInfo(userInfo);
    }

    private void showStepTargetDialog() {
        ChooseNumberDialog dialog = new ChooseNumberDialog(2000, 20000, getString(R.string.target), getString(R.string.step), getUserInfo().getStep(), value -> {
            UserInfo data = viewModel.copyUserInfo();
            data.setStep(value);
            viewModel.updateUserInfo(data);
        });
        dialog.setStep(1000);
        dialog.show(getChildFragmentManager(), dialog.getClass().getCanonicalName());
    }

    private void showHeightDialog() {
        Converter converter = new CMUintConverter().getConverter(BaseUnitConverter.getType());
        ChooseNumberDialog dialog = new ChooseNumberDialog((int) converter.value(50), (int) converter.value(250), getString(R.string.stature), converter.unit(), getUserInfo().getHeight(), value -> {
            UserInfo data = viewModel.copyUserInfo();
            data.setHeight((int) converter.origin(value));
            viewModel.updateUserInfo(data);
        });
        dialog.show(getChildFragmentManager(), dialog.getClass().getCanonicalName());
    }


    private void showBirthdayDialog() {
        UserInfo data = viewModel.copyUserInfo();
        ChooseDateDialog dialog = new ChooseDateDialog(data.getBirthYear(), data.getBirthMonth(), data.getBirthDay(), (year, month, day) -> {
            JL_Log.i(tag, "showBirthdayDialog", CalendarUtil.formatString("%d/%d/%d", year, month, day));
            data.setBirthYear(year);
            data.setBirthMonth(month);
            data.setBirthDay(day);
            viewModel.updateUserInfo(data);
        });
        dialog.show(getChildFragmentManager(), dialog.getClass().getCanonicalName());

    }

    private void showSexDialog() {
        ChooseSexDialog chooseSexDialog = new ChooseSexDialog();
        chooseSexDialog.setCancelable(true);
        chooseSexDialog.setCurrentSex(getUserInfo().getGender() == 0 ? getString(R.string.man) : getString(R.string.woman));
        chooseSexDialog.setOnSexChooseListener(sex -> {
            UserInfo userInfo = viewModel.copyUserInfo();
            if (sex.equals(getString(R.string.woman))) {
                userInfo.setGender(1);
            } else {
                userInfo.setGender(0);
            }
            chooseSexDialog.dismiss();
            viewModel.updateUserInfo(userInfo);
        });
        chooseSexDialog.show(getChildFragmentManager(), ChooseSexDialog.class.getCanonicalName());
    }


    private void updateUserInfoView(UserInfo userInfo) {
        List<CommonItem> list = new ArrayList<>();
//        int avatarRes = R.drawable.ic_userinfo_avatar_man;
//
//        CommonItem avatar = new CommonItem();
//        avatar.setTitle(getString(R.string.user_info_avatar));
//        avatar.setShowNext(true);
//        if (userInfo.getGender() == 1) {
//            avatarRes = R.drawable.ic_userinfo_avatar_woman;
//        }
//        avatar.setRightImg(avatarRes);
//        list.add(avatar);


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

//        CommonItem target = new CommonItem();
//        target.setTitle(getString(R.string.target));
//        target.setShowNext(true);
//        if (userInfo.getStep() > 0) {
//            target.setTailString(getString(R.string.format_step_number_target, userInfo.getStep()));
//        } else {
//            target.setTailString(getString(R.string.please_choose));
//        }
//        list.add(target);

        if (binding.rvPersonalInfo.getAdapter() instanceof CommonAdapter) {
            ((CommonAdapter) binding.rvPersonalInfo.getAdapter()).setList(list);
        }
    }

    private void showExitLoginDialog() {
        new Jl_Dialog.Builder()
                .cancel(true)
                .left(getString(R.string.cancel))
                .right(getString(R.string.sure))
                .content(getString(R.string.tip_logout))
                .title(getString(R.string.tips))
                .leftClickListener((view, dialogFragment) -> dialogFragment.dismiss())
                .rightClickListener((view, dialogFragment) -> {
                    dialogFragment.dismiss();
                    viewModel.logout();
                    startActivity(new Intent(requireActivity(), LoginActivity.class));
                })
                .build()
                .show(getChildFragmentManager(), "exit_dialog");
    }

    private void showDeleteAccountDialog() {
        new Jl_Dialog.Builder()
                .cancel(true)
                .left(getString(R.string.cancel))
                .right(getString(R.string.sure))
                .content("是否要删除当前账号")
                .title(getString(R.string.tips))
                .leftClickListener((view, dialogFragment) -> dialogFragment.dismiss())
                .rightClickListener((view, dialogFragment) -> {
                    dialogFragment.dismiss();
                    viewModel.deleteAccount(requireActivity());
                })
                .build()
                .show(getChildFragmentManager(), "delete_account_dialog");
    }

    private UserInfo getUserInfo() {
        UserInfo info = viewModel.userInfoLiveData.getValue();
        if (null == info) {
            info = new UserInfo();
        }
        return info;
    }


}
