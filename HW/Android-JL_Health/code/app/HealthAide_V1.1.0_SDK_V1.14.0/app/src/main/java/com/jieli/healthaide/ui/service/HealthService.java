package com.jieli.healthaide.ui.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.notification.NotificationHelper;
import com.jieli.healthaide.ui.home.HomeActivity;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_rcsp.model.NotificationMsg;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.List;

/**
 * 健康服务（前台服务）
 */
public class HealthService extends NotificationListenerService {
    private static final String TAG = HealthService.class.getSimpleName();
    /**
     * 前台通知ID
     */
    public static final int FOREGROUND_NOTIFICATION_ID = 0x1369;
    /**
     * 跳转的activity
     */
    public static final String ACTION_ACTIVITY_CLASS = "com.jieli.health.action.activity_class";
    /**
     * 当前模式
     */
    public static final String ACTION_CURRENT_MODE = "current_mode";
    /**
     * 健康记录行为
     */
    public static final String ACTION_HEALTH_RECORD = "com.jieli.health.action.health_record";
    /**
     * 运动模式行为
     */
    public static final String ACTION_SPORT_MODE = "com.jieli.health.action.sport_mode";
    /**
     * 跳转的activity
     */
    public static final String EXTRA_ACTIVITY_CLASS = "activity_class";
    /**
     * 当前模式
     */
    public static final String EXTRA_CURRENT_MODE = "current_mode";

    /**
     * 步数
     */
    public static final String EXTRA_STEP = "step";
    /**
     * 步数
     */
    public static final String EXTRA_KCAL = "kcal";
    /**
     * 运动时间
     */
    public static final String EXTRA_SPORT_TIME = "sport_time";
    /**
     * 运动距离
     */
    public static final String EXTRA_SPORT_DISTANCE = "sport_distance";
    /**
     * 运动配速
     */
    public static final String EXTRA_SPORT_PACE = "sport_pace";

    private final static int CHECK_NOTIFICATION_INTERVAL = 30 * 1000; //30s
    private final static int MSG_CHECK_NOTIFICATION_LISTENER = 3644;

    private final Handler mUIHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_CHECK_NOTIFICATION_LISTENER) {
                ensureCollectorRunning();
                if (isCheckNotification && isNeedCheckNotificationListener()) {
                    mUIHandler.sendEmptyMessageDelayed(MSG_CHECK_NOTIFICATION_LISTENER, CHECK_NOTIFICATION_INTERVAL);
                } else {
                    stopNotificationListenerCheck();
                }
            }
            return true;
        }
    });

    private boolean isCheckNotification = false;
    private String mCurrentMode = ACTION_HEALTH_RECORD;
    private Intent mCacheIntent = new Intent();

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundNotification();
        toggleNotificationListenerService();
        startNotificationListenerCheck();
    }

    @Override
    public void onDestroy() {
        JL_Log.w(TAG, "onDestroy", "");
        stopNotificationListenerCheck();
        NotificationHelper.cancelNotification(getApplicationContext(), FOREGROUND_NOTIFICATION_ID);
        super.onDestroy();
        mUIHandler.removeCallbacksAndMessages(null);
        stopForeground(true);
//        System.exit(0);
    }

    @Override
    public void onTimeout(int startId, int fgsType) {
        stopSelf();
        super.onTimeout(startId, fgsType);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        NotificationHelper.getInstance().checkAndHandleNotification(getApplicationContext(), NotificationMsg.OP_PUSH, sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        NotificationHelper.getInstance().checkAndHandleNotification(getApplicationContext(), NotificationMsg.OP_REMOVE, sbn);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        String action = intent.getAction();
        if (null == action) return START_NOT_STICKY;
        JL_Log.w(TAG, "onStartCommand", "action : " + action);
        switch (action) {
            case ACTION_CURRENT_MODE: {
                dealWithNotificationMode(intent);
                break;
            }
            case ACTION_HEALTH_RECORD: {
                updateHealthRecord(intent);
                break;
            }
            case ACTION_SPORT_MODE: {
                updateSportMode(intent);
                break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    //确认NotificationMonitor是否开启
    private void ensureCollectorRunning() {
        ComponentName collectorComponent = new ComponentName(this, HealthService.class);
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean collectorRunning = false;
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);
        if (runningServices == null) {
            return;
        }
        for (ActivityManager.RunningServiceInfo service : runningServices) {
            if (service.service.equals(collectorComponent)) {
                if (service.pid == android.os.Process.myPid()) {
                    collectorRunning = true;
                    break;
                }
            }
        }
        if (collectorRunning) {
//            JL_Log.i(TAG, "监听服务已激活 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            return;
        }
        toggleNotificationListenerService();
    }

    //重新开启NotificationMonitor
    private void toggleNotificationListenerService() {
        JL_Log.e(TAG, "toggleNotificationListenerService", "重启服务 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        ComponentName thisComponent = new ComponentName(this, HealthService.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    private boolean isNeedCheckNotificationListener() {
        return NotificationHelper.isNotificationServiceEnabled(getApplicationContext()) && NotificationHelper.isNotificationEnable(getApplicationContext());
    }

    private void startNotificationListenerCheck() {
        if (isCheckNotification || !isNeedCheckNotificationListener()) return;
        mUIHandler.removeMessages(MSG_CHECK_NOTIFICATION_LISTENER);
        mUIHandler.sendEmptyMessageDelayed(MSG_CHECK_NOTIFICATION_LISTENER, 3000);
        isCheckNotification = true;
    }

    private void stopNotificationListenerCheck() {
        if (!isCheckNotification) return;
        mUIHandler.removeMessages(MSG_CHECK_NOTIFICATION_LISTENER);
        isCheckNotification = false;
    }

    private void dealWithNotificationMode(Intent intent) {
        if (null == intent) return;
        String action = intent.getAction();
        if (!TextUtils.equals(action, ACTION_CURRENT_MODE)) {
            return;
        }
        String mode = intent.getStringExtra(EXTRA_CURRENT_MODE);
        if (TextUtils.isEmpty(mode) || TextUtils.equals(mCurrentMode, mode)) return;
        switch (mode) {
            case ACTION_HEALTH_RECORD:
                mCurrentMode = mode;
                updateHealthRecord(mCacheIntent);
                break;
            case ACTION_SPORT_MODE:
                mCurrentMode = mode;
                updateSportMode(new Intent());
                break;
        }
    }

    private void startForegroundNotification() {
        int flags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        Notification notification = NotificationHelper.createNotification(getApplicationContext(), getString(R.string.app_name), getString(R.string.service_health_data), R.mipmap.ic_logo,
                PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), HomeActivity.class), flags));
        startForeground(FOREGROUND_NOTIFICATION_ID, notification);
    }

    private void updateHealthRecord(Intent intent) {//如果是多行数据时，小米手机会出现不显示，暂缓解决，怀疑可能是小米需要自己实现调用BigTextStyle
        if (!TextUtils.equals(mCurrentMode, ACTION_HEALTH_RECORD)) return;
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_health_record);
        int flags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        //FIXME: Android 12+ 不能在广播和服务启动Activity
        Notification notification = NotificationHelper.createNotification(getApplicationContext(), getString(R.string.app_name), getString(R.string.service_health_data), R.mipmap.ic_logo,
                PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(ACTION_ACTIVITY_CLASS), flags), remoteViews);
        int step = intent.getIntExtra(EXTRA_STEP, 0);
        int kcal = intent.getIntExtra(EXTRA_KCAL, 0);
        remoteViews.setViewVisibility(R.id.ll_head, Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ? View.GONE : View.VISIBLE);
        remoteViews.setImageViewResource(R.id.iv_app_logo, R.mipmap.ic_logo);
        remoteViews.setTextViewText(R.id.tv_notification_title, getString(R.string.app_name));
        remoteViews.setTextViewText(R.id.tv_health_step, String.valueOf(step));
        remoteViews.setTextViewText(R.id.tv_health_step_unit, getString(R.string.step));
        remoteViews.setTextViewText(R.id.tv_health_kcal, String.valueOf(kcal));
        remoteViews.setTextViewText(R.id.tv_health_kcal_unit, getString(R.string.kcal));
        NotificationHelper.updateNotification(getApplicationContext(), FOREGROUND_NOTIFICATION_ID, notification);
        mCacheIntent = intent;
    }

    private void updateSportMode(Intent intent) {
        if (!TextUtils.equals(mCurrentMode, ACTION_SPORT_MODE)) return;
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_health_running);
        int flags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        //FIXME: Android 12+ 不能在广播和服务启动Activity
        Notification notification = NotificationHelper.createNotification(getApplicationContext(), getString(R.string.app_name), getString(R.string.service_health_data), R.mipmap.ic_logo,
                PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(ACTION_ACTIVITY_CLASS), flags), remoteViews);
        long time = intent.getLongExtra(EXTRA_SPORT_TIME, 0);
        double distance = intent.getDoubleExtra(EXTRA_SPORT_DISTANCE, 0d);
        double pace = intent.getDoubleExtra(EXTRA_SPORT_PACE, 0d);
        remoteViews.setImageViewResource(R.id.iv_app_logo, R.mipmap.ic_logo);
        remoteViews.setTextViewText(R.id.tv_run_time, CalendarUtil.formatSeconds(time / 1000));
        remoteViews.setTextViewText(R.id.tv_run_distance, CalendarUtil.formatString("%.2f", distance / 1000f));
        remoteViews.setTextViewText(R.id.tv_run_pace, CalendarUtil.formatString("%02.0f:%02.0f", pace / 60 % 60, pace % 60));
        NotificationHelper.updateNotification(getApplicationContext(), FOREGROUND_NOTIFICATION_ID, notification);
    }
}