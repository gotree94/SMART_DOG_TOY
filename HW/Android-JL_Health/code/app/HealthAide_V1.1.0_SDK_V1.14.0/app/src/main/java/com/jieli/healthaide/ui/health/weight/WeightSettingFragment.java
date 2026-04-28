package com.jieli.healthaide.ui.health.weight;

import static com.jieli.healthaide.tool.unit.BaseUnitConverter.TYPE_METRIC;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.dao.HealthDao;
import com.jieli.healthaide.data.db.HealthDataDbHelper;
import com.jieli.healthaide.data.db.HealthDatabase;
import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.databinding.FragmentWeightSettingBinding;
import com.jieli.healthaide.tool.unit.BaseUnitConverter;
import com.jieli.healthaide.tool.unit.Converter;
import com.jieli.healthaide.tool.unit.KGUnitConverter;
import com.jieli.healthaide.tool.watch.synctask.ServerHealthDataSyncTask;
import com.jieli.healthaide.tool.watch.synctask.SyncTaskManager;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.dialog.ChooseDateDialog;
import com.jieli.healthaide.ui.dialog.ChooseTimeDialog2;
import com.jieli.healthaide.ui.mine.UserInfoViewModel;
import com.jieli.healthaide.ui.widget.rulerview.RulerView;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.CustomTimeFormatUtil;
import com.jieli.jl_health_http.model.UserInfo;
import com.jieli.jl_rcsp.util.CHexConver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 体重设定
 */
public class WeightSettingFragment extends BaseFragment {
    public static final String CURRENT_WEIGHT_VALUE = "current_weight_value";
    private FragmentWeightSettingBinding fragmentWeightSettingBinding;
    private final Calendar currentCalendar = Calendar.getInstance();
    private float targetWeight = 0;//公制的值 kg
    private UserInfoViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentWeightSettingBinding = FragmentWeightSettingBinding.inflate(inflater, container, false);
        fragmentWeightSettingBinding.layoutTopbar.tvTopbarTitle.setText(getString(R.string.weight_setting));
        fragmentWeightSettingBinding.layoutTopbar.tvTopbarLeft.setOnClickListener(view -> requireActivity().finish());
        Converter converter = new KGUnitConverter().getConverter(BaseUnitConverter.getType());
        if (getArguments() != null) {
            targetWeight = getArguments().getFloat(CURRENT_WEIGHT_VALUE, 10.0f);
        }
        targetWeight = Math.max(targetWeight, 10.0f);
        fragmentWeightSettingBinding.tvTargetWeightUnit.setText(converter.unit());
        fragmentWeightSettingBinding.layoutCalender.tvSettingTarget2.setText(getString(R.string.date));
        fragmentWeightSettingBinding.layoutCalender.tvHint.setText(getDate(currentCalendar));
        fragmentWeightSettingBinding.layoutTime.tvSettingTarget2.setText(getString(R.string.time));
        fragmentWeightSettingBinding.layoutTime.tvHint.setText(getTime(currentCalendar));
        fragmentWeightSettingBinding.layoutCalender.getRoot().setOnClickListener(view -> showDateDialog());
        fragmentWeightSettingBinding.layoutTime.getRoot().setOnClickListener(view -> showTimeDialog());
        fragmentWeightSettingBinding.btnConfirm.setOnClickListener(view -> {
            HealthDao healthDao = HealthDatabase.buildHealthDb(HealthApplication.getAppViewModel().getApplication()).HealthDao();
            String uid = HealthApplication.getAppViewModel().getUid();
            HealthEntity lastEntity = healthDao.getHealthLiveDataLast(HealthEntity.DATA_TYPE_WEIGHT, uid);

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
            data[4] = CHexConver.intToByte((int) targetWeight);
            data[5] = CHexConver.intToByte((int) (targetWeight * 100 % 100));
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

            if (lastEntity == null || lastEntity.getTime() < currentCalendar.getTimeInMillis()) {
                UserInfo userInfo = viewModel.userInfoLiveData.getValue();
                if (userInfo != null) {
                    userInfo.setWeight(targetWeight);
                    viewModel.updateUserInfo(userInfo);
                }
            }
            showTips(R.string.execution_succeeded);
            back();
        });
        fragmentWeightSettingBinding.scaleViewWeight.setMinValueAndMaxValue((int) converter.value(10) * 10, (int) converter.value(250) * 10);
        double converterValue = converter.value(targetWeight);
        converterValue = BaseUnitConverter.getType() == TYPE_METRIC ? formatKg(converterValue) : formatPound(converterValue);
        converterValue = ((double) Math.round(converterValue * 10)) / 10;
        fragmentWeightSettingBinding.tvTargetWeightValue.setText(CalendarUtil.formatString("%.1f", converterValue));
        fragmentWeightSettingBinding.scaleViewWeight.setCurrentPosition((int) (converterValue * 10));
        fragmentWeightSettingBinding.scaleViewWeight.setOnValueChangeListener(new RulerView.OnValueChangeListener() {
            @Override
            public void onChange(int value) {
                float weight = ((float) value) / ((float) 10);
                fragmentWeightSettingBinding.tvTargetWeightValue.setText(CalendarUtil.formatString("%.1f", weight));
            }

            @Override
            public void onActionUp(int value) {
                float weight = ((float) value) / ((float) 10);
                fragmentWeightSettingBinding.tvTargetWeightValue.setText(CalendarUtil.formatString("%.1f", weight));
                targetWeight = (float) converter.origin(weight);
            }

            @Override
            public void onFling(int value, boolean isFlingEnd) {
                float weight = ((float) value) / ((float) 10);
                fragmentWeightSettingBinding.tvTargetWeightValue.setText(CalendarUtil.formatString("%.1f", weight));
                targetWeight = (float) converter.origin(weight);
            }

            @Override
            public void onCanNotSlide() {

            }
        });
        return fragmentWeightSettingBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UserInfoViewModel.class);
        viewModel.getUserInfo();
    }

    private void showDateDialog() {
        int currentYear = currentCalendar.get(Calendar.YEAR);
        int currentMonth = currentCalendar.get(Calendar.MONTH) + 1;
        int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);
        ChooseDateDialog dialog = new ChooseDateDialog(currentYear, currentMonth, currentDay, new ChooseDateDialog.OnDateSelected() {
            @Override
            public void onSelected(int year, int month, int day) {
                currentCalendar.set(year, month - 1, day);
                fragmentWeightSettingBinding.layoutCalender.tvHint.setText(getDate(currentCalendar));
            }
        });
        dialog.setTitle(-1);
        dialog.show(getChildFragmentManager(), dialog.getClass().getCanonicalName());
    }

    private void showTimeDialog() {
        int currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = currentCalendar.get(Calendar.MINUTE);
        currentHour = currentHour == 0 ? 24 : currentHour;
        ChooseTimeDialog2 dialog = new ChooseTimeDialog2(currentHour, currentMinute, -1, (hour, minute) -> {
            currentCalendar.set(Calendar.HOUR_OF_DAY, hour == 24 ? 0 : hour);
            currentCalendar.set(Calendar.MINUTE, minute);
            fragmentWeightSettingBinding.layoutTime.tvHint.setText(getTime(currentCalendar));
        });
        dialog.show(getChildFragmentManager(), dialog.getClass().getCanonicalName());
    }

    private String getDate(Calendar c) {
        Date date = c.getTime();
        if (isLocaleChinese()) {
            SimpleDateFormat format = CustomTimeFormatUtil.dateFormat("yyyy年MM月dd日");
            String dayText = format.format(date);
            return CalendarUtil.formatString("%s", dayText);
        } else {
            SimpleDateFormat format = CustomTimeFormatUtil.dateFormat("dd MM,yyyy");
            String dayText = format.format(date);
            return CalendarUtil.formatString("%s", dayText);
        }
    }

    private String getTime(Calendar c) {
        Date date = c.getTime();
        SimpleDateFormat format = CustomTimeFormatUtil.dateFormat("HH:mm");
        return format.format(date);
    }

    private boolean isLocaleChinese() {
        return Locale.getDefault().getLanguage().equalsIgnoreCase("zh")
                && Locale.getDefault().getCountry().equalsIgnoreCase("cn");
    }

    private double formatKg(double kg) {
        int minKG = 10;
        int maxKG = 250;
        kg = Math.max(minKG, kg);
        kg = Math.min(kg, maxKG);
        return kg;
    }

    private double formatPound(double pound) {
        int minPound = 22;
        int maxPound = 551;
        pound = Math.max(minPound, pound);
        pound = Math.min(pound, maxPound);
        return pound;
    }
}