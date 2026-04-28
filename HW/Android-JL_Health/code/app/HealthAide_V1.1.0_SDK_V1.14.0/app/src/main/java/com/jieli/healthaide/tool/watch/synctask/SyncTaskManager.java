package com.jieli.healthaide.tool.watch.synctask;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;

import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.constant.Command;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.constant.WatchError;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.model.base.CommandBase;
import com.jieli.jl_rcsp.model.command.watch.SportsInfoStatusSyncCmd;
import com.jieli.jl_rcsp.task.smallfile.QueryFileTask;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/6/15
 * @desc :
 */
public class SyncTaskManager extends OnWatchCallback implements SyncTaskFinishListener {
    private final String tag = SyncTaskManager.class.getSimpleName();
    private final WatchManager mWatchManager = WatchManager.getInstance();

    private final List<AbstractSyncTask> syncTasks = Collections.synchronizedList(new ArrayList<>());
    private volatile static SyncTaskManager instance;
    private boolean isSyncing = false;
    private boolean isSupportSyncWeather = false;
    public MutableLiveData<Boolean> isSyncingLiveData = new MutableLiveData<>(false);

    private final static int MSG_ADD_WEATHER_TASK = 0x3621;
    private final Handler uiHandler = new Handler(Looper.getMainLooper(), msg -> {
        if (msg.what == MSG_ADD_WEATHER_TASK && isSupportSyncWeather) {
            addTask(new WeatherSyncTask(this, this));
        }
        return true;
    });

    private SyncTaskManager() {
        mWatchManager.registerOnWatchCallback(this);
        syncServerWhenLogin();
    }

    public static SyncTaskManager getInstance() {
        if (null == instance) {
            synchronized (SyncTaskManager.class) {
                if (null == instance) {
                    instance = new SyncTaskManager();
                }
            }
        }
        return instance;
    }

    public void destroy() {
        mWatchManager.unregisterOnWatchCallback(this);
        syncTasks.clear();
        uiHandler.removeCallbacksAndMessages(null);
        instance = null;
    }

    @Override
    public void onConnectStateChange(BluetoothDevice device, int status) {
        super.onConnectStateChange(device, status);
        JL_Log.w(tag, "onConnectStateChange", "status : " + status);
        dismissWaitingDialog();

        if (status != StateCode.CONNECTION_OK) {
            uiHandler.removeMessages(MSG_ADD_WEATHER_TASK);
        }
    }


    @Override
    public void onWatchSystemInit(int code) {
        super.onWatchSystemInit(code);
        boolean isSkip = mWatchManager.isBleChangeSpp();
        JL_Log.e(tag, "onWatchSystemInit", "code : " + code + ", isSkip = " + isSkip);
        if (code == 0) {
            if (isSkip) return;
            uiHandler.postDelayed(this::refreshTask, 300);
        }
    }

    @Override
    public void onFinish() {
        //切换到主线程
        uiHandler.post(() -> {
            if (!syncTasks.isEmpty()) {
                AbstractSyncTask syncTask = syncTasks.remove(0);
                JL_Log.i(tag, "onFinish", "结束任务. task : " + syncTask.getClass().getSimpleName());
            }
            startTask();
        });
    }

    @Override
    public void onRcspCommand(BluetoothDevice device, CommandBase command) {
        //运动状态变化
        if (command.getId() == Command.CMD_SPORTS_INFO_STATUS_SYNC) {
            SportsInfoStatusSyncCmd sportsInfoStatusSyncCmd = (SportsInfoStatusSyncCmd) command;
            if (sportsInfoStatusSyncCmd.getParam() instanceof SportsInfoStatusSyncCmd.StartSportsParam) {
                SportsInfoStatusSyncCmd.StartSportsParam startSportsParam = (SportsInfoStatusSyncCmd.StartSportsParam) sportsInfoStatusSyncCmd.getParam();
                if (isSyncing) {
                    //设备连接上的任务正在运行，等待
                    return;
                } else if (!syncTasks.isEmpty()) {
                    //有任务在执行, 放入任务队列等待开始
                    JL_Log.d(tag, "onRcspCommand", "add SyncSportsStatusTask");
                    addTask(new SyncSportsStatusTask(this));
                    return;
                }
                JL_Log.d(tag, "onRcspCommand", "toSportsUi ---> ");
                //没有任务在执行，直接开始运动;
                SyncSportsStatusTask.toSportsUi(startSportsParam.type);
            }
        }
    }

    public void addTask(AbstractSyncTask task) {
        addTask(task, false);
    }

    public void addTask(AbstractSyncTask task, boolean isUrgent) {
        if (null == task || syncTasks.contains(task)) {
            JL_Log.w(tag, "addTask", "task is null, or task is exist. " + (task == null ? "-1" : task.getType()));
            return;
        }
        if (isUrgent && syncTasks.size() > 2) {
            syncTasks.add(1, task);
        } else {
            syncTasks.add(task);
        }
        //只有一个task的时候执行该任务
        if (syncTasks.size() == 1) {
            startTask();
        }
    }

    public void refreshTask() {
//        syncTasks.clear();
        showWaitDialog();
        addTask(new RequestUidSyncTask(this));
        if (mWatchManager.isWatchSystemOk() && !mWatchManager.isBleChangeSpp()
                && !mWatchManager.isOTAResource()
                && !mWatchManager.getDeviceInfo().isMandatoryUpgrade()) {
            WatchConfigure configure = mWatchManager.getWatchConfigure(mWatchManager.getConnectedDevice());
            boolean isExistSportRecord = configure == null || (configure.getSportHealthConfigure() != null
                    && configure.getSportHealthConfigure().getSportModeFunc() != null
                    && configure.getSportHealthConfigure().getSportModeFunc().isSupportRecord());
            if (isExistSportRecord) {
                addTask(new DeviceSportRecordSyncTaskModify(this));
            }
            boolean isExistSportMode = configure == null || (configure.getSportHealthConfigure() != null
                    && configure.getSportHealthConfigure().getSportModeFunc() != null
                    && (configure.getSportHealthConfigure().getSportModeFunc().isSupportInDoor()
                    || configure.getSportHealthConfigure().getSportModeFunc().isSupportOutDoor()));
            if (isExistSportMode) {
                JL_Log.d(tag, "refreshTask", "add SyncSportsStatusTask");
                addTask(new SyncSportsStatusTask(this));
            }
            boolean isExistStep = configure == null || (configure.getSportHealthConfigure() != null
                    && configure.getSportHealthConfigure().isExistGSensor()
                    && configure.getSportHealthConfigure().getGSensorFunc().isEnableSportStep());
            if (isExistStep) {
                addTask(new DeviceHealthDataSyncTask(QueryFileTask.TYPE_STEP, this));
            }
            boolean isExistRate = configure == null || (configure.getSportHealthConfigure() != null
                    && configure.getSportHealthConfigure().isExistRate()
                    && configure.getSportHealthConfigure().getRateFunc().isOpen());
            if (isExistRate) {
                addTask(new DeviceHealthDataSyncTask(QueryFileTask.TYPE_HEART_RATE, this));
            }
            boolean isExistSleep = configure == null || (configure.getSportHealthConfigure() != null
                    && configure.getSportHealthConfigure().getCombineFunc() != null
                    && configure.getSportHealthConfigure().getCombineFunc().isSupportSleepDetection());
            if (isExistSleep) {
                addTask(new DeviceHealthDataSyncTask(QueryFileTask.TYPE_SLEEP, this));
            }
            boolean isExistBloodOxygen = configure == null || (configure.getSportHealthConfigure() != null
                    && configure.getSportHealthConfigure().isExistBloodOxygen()
                    && configure.getSportHealthConfigure().getBloodOxygenFunc().isOpen());
            if (isExistBloodOxygen) {
                addTask(new DeviceHealthDataSyncTask(QueryFileTask.TYPE_BLOOD_OXYGEN, this));
            }
            boolean isExistHealthData = configure == null || (configure.getSportHealthConfigure() != null
                    && (configure.getSportHealthConfigure().isExistGSensor() || configure.getSportHealthConfigure().isExistRate()
                    || configure.getSportHealthConfigure().isExistBloodOxygen()));
            if (isExistHealthData) {
                addTask(new RealTimeHealthDataSyncTask(this));
            }
            boolean isSyncWeather = configure == null || (configure.getFunctionOption() != null
                    && configure.getFunctionOption().isSupportWeatherSync());
            if (isSyncWeather) {
                addTask(new WeatherSyncTask(this, this));
            }
            addTask(new DeviceLogcatSyncTask(HealthApplication.getAppViewModel().getApplication(), this));
        }
        addTask(new ServerHealthDataSyncTask(this));
        addTask(new DownloadSportsRecordSyncTask(this));
        addTask(new UploadSportsRecordSyncTask(this));
    }

    public void setSupportSyncWeather(boolean supportSyncWeather) {
        isSupportSyncWeather = supportSyncWeather;
    }

    public void addTaskDelay(AbstractSyncTask syncTask, int delayMs) {
        uiHandler.postDelayed(() -> addTask(syncTask), delayMs);
    }

    public void addWeatherTask(int delayMs) {
        uiHandler.removeMessages(MSG_ADD_WEATHER_TASK);
        uiHandler.sendEmptyMessageDelayed(MSG_ADD_WEATHER_TASK, delayMs);
    }

    private void startTask() {
        if (syncTasks.isEmpty()) {
            JL_Log.i(tag, "startTask", "task list is empty.");
            dismissWaitingDialog();
            return;
        }
        AbstractSyncTask syncTask = syncTasks.get(0);
        JL_Log.i(tag, "startTask", "启动SyncTask. task : " + syncTask.getClass().getSimpleName());
        syncTask.start();
    }


    /**
     * 看实际效果决定是登录同步服务器数据，还是连接成功同步
     */
    private void syncServerWhenLogin() {
        uiHandler.post(() -> {
            showWaitDialog();
            addTask(new RequestUidSyncTask(this));
            addTask(new DownloadSportsRecordSyncTask(this));
            addTask(new UploadSportsRecordSyncTask(this));
            addTask(new ServerHealthDataSyncTask(this));
            if(mWatchManager.isConnected()){
                onWatchSystemInit(WatchError.ERR_NONE);
            }
        });
    }

    private void showWaitDialog() {
        isSyncing = true;
        isSyncingLiveData.postValue(true);
    }

    private void dismissWaitingDialog() {
        isSyncing = false;
        isSyncingLiveData.postValue(false);
    }
}
