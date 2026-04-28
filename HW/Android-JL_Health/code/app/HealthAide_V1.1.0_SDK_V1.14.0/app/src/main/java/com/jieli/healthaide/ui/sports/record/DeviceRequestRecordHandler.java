package com.jieli.healthaide.ui.sports.record;

import android.app.Activity;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.jieli.component.ActivityManager;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.db.HealthDataDbHelper;
import com.jieli.healthaide.data.entity.SportRecord;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.ui.home.HomeActivity;
import com.jieli.jl_dialog.Jl_Dialog;
import com.jieli.jl_filebrowse.interfaces.OperatCallback;
import com.jieli.jl_rcsp.task.SimpleTaskListener;
import com.jieli.jl_rcsp.task.smallfile.QueryFileTask;
import com.jieli.jl_rcsp.task.smallfile.ReadFileTask;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/11/4
 * @desc :
 */
public class DeviceRequestRecordHandler implements IRequestRecordHandler {

    public static final int SPORTS_RECORD_FILE_BROKEN = 0x90;
    public static final int SPORTS_DISTANCE_IS_TOO_SHORT_NOT_HAS_RECORD = 0x91;
    public static final int SPORTS_RECORD_FILE_ERROR = 0x99;
    private final String tag = getClass().getSimpleName();
    private final QueryFileTask.File file;
    private final OperatCallback operatCallback;

    public DeviceRequestRecordHandler(QueryFileTask.File file, OperatCallback operatCallback) {
        this.file = file;
        this.operatCallback = operatCallback;
    }

    @Override
    public void request() {
        readSportsRecord();
    }


    public void handlerTooShortTime() {
        JL_Log.w(tag, "handlerTooShortTime", "运动时间过短" + file);
        String fragTag = DeviceRequestRecordHandler.class.getSimpleName();
        Activity activity = ActivityManager.getInstance().findActivityByName(HomeActivity.class.getName());
        if (activity == null || activity.isDestroyed()) return;
        Fragment fragment = ((FragmentActivity) activity).getSupportFragmentManager().findFragmentByTag(fragTag);
        Jl_Dialog jl_dialog;
        if (fragment == null) {
            Jl_Dialog.Builder builder = new Jl_Dialog.Builder()
                    .contentLayoutRes(R.layout.dialog_too_short_distance)
                    .title(activity.getString(R.string.tips))
                    .cancel(true)
                    .content(activity.getString(R.string.running_too_short))
                    .left(activity.getString(R.string.sure))
                    .leftColor(ResourcesCompat.getColor(activity.getResources(), R.color.blue_558CFF, activity.getTheme()))
                    .leftClickListener((view, dialogFragment) -> dialogFragment.dismiss());

            jl_dialog = builder.build();
        } else {
            JL_Log.d(tag, "handlerTooShortTime", "运动时间过短提示框已在fragment");
            jl_dialog = (Jl_Dialog) fragment;
        }

        if (jl_dialog.isShow()) {
            JL_Log.d(tag, "handlerTooShortTime", "运动时间过短提示框已显示");
            return;
        }
        jl_dialog.show(((FragmentActivity) activity).getSupportFragmentManager(), fragTag);

    }

    private void readSportsRecord() {
        if (file == null) {
            JL_Log.w(tag, "readSportsRecord", "failed: file is null");
            if (operatCallback != null) operatCallback.onError(SPORTS_RECORD_FILE_ERROR);
            return;
        }


//        if (file.id < 0) {
//            JL_Log.w(tag, "readSportsRecord  failed: file   is error " + file);
//            if (operatCallback != null) operatCallback.onError(SPORTS_RECORD_FILE_BROKEN);
//            return;
//        }

        if (file.size == 0 && file.id == 0) {
            if (operatCallback != null)
                operatCallback.onError(SPORTS_DISTANCE_IS_TOO_SHORT_NOT_HAS_RECORD);
            handlerTooShortTime();
            return;
        }

        ReadFileTask.Param param = new ReadFileTask.Param(QueryFileTask.TYPE_SPORTS_RECORD, (short) file.id, file.size, 0);
        ReadFileTask readFileTask = new ReadFileTask(WatchManager.getInstance(), param);
        readFileTask.setListener(new SimpleTaskListener() {
            @Override
            public void onFinish() {
                JL_Log.w(tag, "onFinish", "运动结束获取运动记录成功  ");

                byte[] data = readFileTask.getReadData();
                //todo 运动结束后保存运动数据到数据库
                SportRecord sportRecord = SportRecord.from(data);
                sportRecord.setUid(HealthApplication.getAppViewModel().getUid());
                HealthDataDbHelper.getInstance().getSportRecordDao().insert(sportRecord);
                if (operatCallback != null) operatCallback.onSuccess();
            }

            @Override
            public void onError(int code, String msg) {
                JL_Log.w(tag, "onError", "运动结束获取运动记录失败：code = " + code + "\tmsg = " + msg);
                //todo 获取运动记录失败，退出
                if (operatCallback != null) operatCallback.onError(code);
            }

        });
        readFileTask.start();
    }
}
