package com.jieli.healthaide.tool.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.FormatUtil;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.jl_rcsp.model.NotificationMsg;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 通知栏处理类
 * @since 2021/4/28
 */
public class NotificationHelper {
    private static final String TAG = NotificationHelper.class.getSimpleName();
    private static final String PACKAGE_NAME = "com_jieli_healthaidl_";
    private static final String CHANNEL = "channel_";
    private static final String ACTION = "action_";
    private static final String NOTIFICATION_CHANNEL_ID_ONE = PACKAGE_NAME + CHANNEL + "001";
    private static final String NOTIFICATION_CHANNEL_ONE_NAME = "Foreground Service Notification";


    public static final String ACTION_NOTIFY_MESSAGE = PACKAGE_NAME + ACTION + "notify_message";
    public static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    public static final String EXTRA_NOTIFICATION_MSG = "notification_msg";
    public static final String EXTRA_NOTIFICATION_ICON = "notification_icon";

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    private volatile static NotificationHelper instance;
    private final AppInfoManager mAppInfoManager = AppInfoManager.getInstance();
    private final Handler mUIHandler = new Handler(Looper.getMainLooper());
    private final WatchManager mWatchManager = WatchManager.getInstance();
//    private Thread readAppListThread;
//    private List<AppInfo> mAppInfoList;
//    private OnReadAppListListener mOnReadAppListListener;

    private final String[] defaultAppPackageNameArray = new String[]{
            HealthConstant.PACKAGE_NAME_SYS_MESSAGE,
            HealthConstant.PACKAGE_NAME_WECHAT,
            HealthConstant.PACKAGE_NAME_QQ,
            HealthConstant.PACKAGE_NAME_DING_DING
    };

    /**
     * Is Notification Service Enabled.
     * Verifies if the notification listener service is enabled.
     * Got it from: https://github.com/kpbird/NotificationListenerService-Example/blob/master/NLSExample/src/main/java/com/kpbird/nlsexample/NLService.java
     *
     * @return True if enabled, false otherwise.
     */
    public static boolean isNotificationServiceEnabled(Context context) {
        if (null == context) return false;
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(), ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null && TextUtils.equals(pkgName, cn.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断应用是否具有通知权限
     *
     * @param context 上下文
     * @return 寄过
     */
    public static boolean isNotificationEnable(Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        return notificationManagerCompat.areNotificationsEnabled();
    }

    public static Notification createNotification(Context context, String title, String content, int smallIconRes, PendingIntent intent) {
        return createNotification(context, title, content, smallIconRes, intent, null);
    }

    public static Notification createNotification(Context context, String title, String content, int smallIconRes, PendingIntent intent, RemoteViews remoteViews) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_ONE, NOTIFICATION_CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_ONE);
        builder.setContentTitle(title).setContentText(content).setSmallIcon(smallIconRes).setWhen(System.currentTimeMillis());
        if (intent != null) {
            builder.setContentIntent(intent);
        }
        if (remoteViews != null) {
            builder.setCustomContentView(remoteViews);
            builder.setCustomBigContentView(remoteViews);
            builder.setCustomHeadsUpContentView(remoteViews);
        }
        return builder.build();
    }

    public static void updateNotification(Context context, int notifyID, Notification notification) {
        if (null == context || null == notification) return;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notifyID, notification);
        }
    }

    public static void cancelNotification(Context context, int notifyID) {
        if (null == context) return;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(notifyID);
        }
    }

    private NotificationHelper() {
        List<String> cacheAppList = mAppInfoManager.getObservedAppList();
        if (cacheAppList.isEmpty()) {
            for (String name : defaultAppPackageNameArray) {
                mAppInfoManager.saveObservedPackageName(name);
            }
        }
    }

    public static NotificationHelper getInstance() {
        if (null == instance) {
            synchronized (NotificationHelper.class) {
                if (null == instance) {
                    instance = new NotificationHelper();
                }
            }
        }
        return instance;
    }

    public void destroy() {
//        mAppInfoList.clear();
        mUIHandler.removeCallbacksAndMessages(null);
        instance = null;
    }

    public boolean isEnableNotification() {
        return mAppInfoManager.isNotificationEnable();
    }

    public void setEnableNotification(boolean enable) {
        mAppInfoManager.saveNotificationEnable(enable);
    }

    public boolean isAllowOther() {
        return mAppInfoManager.isAllowOtherApp();
    }

    public void setAllowOther(boolean allowOther) {
        mAppInfoManager.setAllowOtherApp(allowOther);
    }

    public List<String> getPackageObserverList() {
        return mAppInfoManager.getObservedAppList();
    }

    public void removePackageName(String packageName) {
        mAppInfoManager.removeObservedPackageName(packageName);
    }

    public void addPackageName(String packageName) {
        mAppInfoManager.saveObservedPackageName(packageName);
    }

   /* public List<AppInfo> getInstalledAppInfoList() {
        return mAppInfoList;
    }*/

    public boolean isSelectedApp(String packageName) {
        if (null == packageName || !isEnableNotification()) return false;
        boolean ret = getPackageObserverList().contains(packageName);
        if (!ret && isAllowOther()) {
            ret = true;
            for (String defaultApp : defaultAppPackageNameArray) {
                if (defaultApp.equals(packageName)) {
                    ret = false;
                    break;
                }
            }
        }
        return ret;
    }


    public void checkAndHandleNotification(Context context, int op, StatusBarNotification sbn) {
        if (null == sbn || null == context) return;
        String packageName = sbn.getPackageName();
        if (!isSelectedApp(packageName)) {
            JL_Log.e(TAG, "checkAndHandleNotification", "no selected app.");
            return;
        }
        if (mWatchManager.isFirmwareOTA()) {
            JL_Log.e(TAG, "checkAndHandleNotification", "Device is updating.");
            return;
        }
        Notification notification = sbn.getNotification();
        Bundle bundle = notification.extras;
        CharSequence title = bundle.getCharSequence(Notification.EXTRA_TITLE);//通知title
        CharSequence content = bundle.getCharSequence(Notification.EXTRA_TEXT); //通知内容
        CharSequence bigText = null; //长文本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bigText = bundle.getCharSequence(Notification.EXTRA_BIG_TEXT);
        }
        CharSequence subContent = bundle.getCharSequence(Notification.EXTRA_SUB_TEXT); //通知内容子内容
        CharSequence externalContent = bundle.getCharSequence(Notification.EXTRA_INFO_TEXT); //额外信息
        CharSequence tickerText = notification.tickerText;
        long time = notification.when;
        int flags = notification.flags;
        String contentStr = getStr(content);
        String bigString = getStr(bigText);
        if (contentStr == null) {
            contentStr = bigString;
        } else {
            if (null != bigString && bigString.length() > contentStr.length()) {
                contentStr = bigString;
            }
        }
        if (contentStr == null) {
            contentStr = getStr(tickerText);
        }
        String category = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            category = notification.category;
        }
        JL_Log.i(TAG, "checkAndHandleNotification", CalendarUtil.formatString("%s通知>>>>>>>包名：%s, 标题 : %s,\n内容 ：%s\n子段内容: %s\t" +
                        "额外信息：%s\t大号文本: %s,\ntickerText : %s\n时间 ：%s\t类型 : %s,\tflags = %d", (op == 1 ? "移除" : "推送"), packageName, title, content,
                subContent, externalContent, bigText, tickerText, FormatUtil.formatterTime(time), category, flags));
        if (TextUtils.isEmpty(contentStr)) return;
        if (category == null || category.equals(Notification.CATEGORY_MESSAGE)) { //只关心通知信息
            contentStr = formatContent(packageName, contentStr);
            NotificationMsg msg = new NotificationMsg()
                    .setAppName(packageName)
                    .setTitle(getStr(title))
                    .setContent(contentStr)
                    .setFlag(getNotificationFlag(packageName))
                    .setTime(time)
                    .setOp(op);
            if (op == NotificationMsg.OP_REMOVE) {
                mWatchManager.removeMessageInfo(msg, null);
            } else {
                mWatchManager.pushMessageInfo(msg, null);
            }
            Intent intent = new Intent(ACTION_NOTIFY_MESSAGE);
            intent.putExtra(EXTRA_NOTIFICATION_MSG, msg);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Icon icon;
                icon = notification.getSmallIcon();
                /*if (icon == null) {
                    icon = notification.getLargeIcon();
                }*/
                if (icon != null) {
                    JL_Log.w(TAG, "checkAndHandleNotification", "notification icon = " + icon);
                    intent.putExtra(EXTRA_NOTIFICATION_ICON, icon);
                }
            }
            context.sendBroadcast(intent);
        }
    }

    /*public void readInstalledAppList(OnReadAppListListener listener) {
        if (null == readAppListThread) {
            mOnReadAppListListener = listener;
            readAppListThread = new Thread(() -> {
                mAppInfoList = SystemUtil.getInstallApp(HealthApplication.getAppViewModel().getApplication(), true);
                if (mAppInfoList == null || mAppInfoList.isEmpty()) {
                    postAppListResult(mAppInfoList);
                    return;
                }
                sortAppList();
                postAppListResult(mAppInfoList);
            });
            readAppListThread.start();
        }
    }

    private void sortAppList() {
        List<AppInfo> result = new ArrayList<>();
        for (AppInfo info : mAppInfoList) {
            if (isSelectedApp(info)) {
                result.add(0, info);
            } else if (!info.isSystem() || HealthConstant.PACKAGE_NAME_SYS_MESSAGE.equals(info.getPackageName())) {
                result.add(info);
            }
        }
        mAppInfoList = result;
    }

    private void postAppListResult(final List<AppInfo> list) {
        if (null == mOnReadAppListListener) return;
        mUIHandler.post(() -> {
            if (mOnReadAppListListener != null) {
                mOnReadAppListListener.onResult(list);
            }
        });
    }*/

    private String getStr(CharSequence charSequence) {
        if (charSequence == null) return null;
        return charSequence.toString();
    }

    private String formatContent(String packageName, String content) {
        if (null == packageName) return content;
        if (HealthConstant.PACKAGE_NAME_WECHAT.equals(packageName)) {
            String pattern = "(\\[)(.+?)(\\])(.+?):";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(content);
            if (m.find()) {
                String sub = m.group();
                content = content.substring(sub.length());
                JL_Log.i(TAG, "formatContent", "find sub : " + sub + ", content = " + content);
            } else {
                JL_Log.i(TAG, "formatContent", "not find.");
            }
        }
        return content;
    }

    public static int getNotificationFlag(String packageName) {
        if (null == packageName) return 0;
        int flag = 0;
        switch (packageName) {
            case HealthConstant.PACKAGE_NAME_SYS_MESSAGE:
                flag = 1;
                break;
            case HealthConstant.PACKAGE_NAME_WECHAT:
                flag = 2;
                break;
            case HealthConstant.PACKAGE_NAME_QQ:
                flag = 3;
                break;
            case HealthConstant.PACKAGE_NAME_DING_DING:
                flag = 4;
                break;
        }
        return flag;
    }
}
