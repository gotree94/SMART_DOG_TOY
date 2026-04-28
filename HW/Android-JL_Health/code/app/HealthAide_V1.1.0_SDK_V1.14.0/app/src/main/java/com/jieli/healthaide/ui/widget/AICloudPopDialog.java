package com.jieli.healthaide.ui.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.jieli.component.ActivityManager;
import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.aiui.AIManager;
import com.jieli.healthaide.tool.aiui.chat.SessionInfo;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.device.aicloud.AICloudHistoryMessageFragment;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: AICloudPopDialog
 * @Description: AI云消息弹窗
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/7/17 15:57
 */
public class AICloudPopDialog extends Service {
    private final int MSG_SHOW_DIALOG = 1;
    private final int MSG_DISMISS_DIALOG = 2;
    /**
     * AI云行为
     */
    public static final String ACTION_AI_CLOUD = "com.jieli.health.action.ai_cloud";
    /**
     * AI写入sql
     */
    public static final String EXTRA_WRITE_SQL = "write_sql";
    public static final String EXTRA_INIT = "init";
    private final String tag = getClass().getSimpleName();
    private final Handler mHandler = new Handler(Looper.getMainLooper(), msg -> {
        if (MSG_SHOW_DIALOG == msg.what) {
            Activity activity = (Activity) msg.obj;
            showDialog(activity);
        } else if (MSG_DISMISS_DIALOG == msg.what) {
            Activity activity = (Activity) msg.obj;
            dismissDialog(activity);
        }
        return true;
    });
    private boolean isNeedShow = false;//是否显示
    private boolean isCanWriteSQL = true;//能发写入数据
    private final Map<Activity, View> mViewMap = new HashMap<>();
    private AIManager mAIManager;
    private final Application.ActivityLifecycleCallbacks callbacks = new Application.ActivityLifecycleCallbacks() {
        public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
            JL_Log.e(tag, "onActivityCreated", "clazz name :" + activity.getClass().getName());
        }

        public void onActivityStarted(@NonNull Activity activity) {
        }

        public void onActivityResumed(@NonNull Activity activity) {
            if (!mViewMap.containsKey(activity)) {
                JL_Log.e(tag, "onActivityResumed", "activity : " + activity);
                if (isNeedShow) {
                    Message message = mHandler.obtainMessage(MSG_SHOW_DIALOG, activity);
                    mHandler.removeMessages(MSG_SHOW_DIALOG);
                    mHandler.sendMessageDelayed(message, 50);
                }
            }
            JL_Log.i(tag, "onActivityResumed", "clazz name : " + activity.getClass().getName());
        }

        public void onActivityPaused(@NonNull Activity activity) {
            JL_Log.i(tag, "onActivityDestroyed", "clazz name : " + activity.getClass().getName());
            mHandler.removeMessages(MSG_SHOW_DIALOG);
            if (isNeedShow) {
                mHandler.post(() -> dismissDialog(activity));
            }
        }

        public void onActivityStopped(@NonNull Activity activity) {

        }

        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        }

        public void onActivityDestroyed(Activity activity) {//这里dismiss 会窗体泄露

        }
    };
    private boolean isRecording = false;
    private final Observer<SessionInfo> mRecordStatusObserver = sessionInfo -> {
        if (sessionInfo != null) {
            switch (sessionInfo.getStatus()) {
                case SessionInfo.STATE_RECORD_START://开始录音
                case SessionInfo.STATE_RECORDING://录音中
                {
                    if (!isRecording) {
                        isRecording = true;
                        isNeedShow = true;
                        Activity activity = ActivityManager.getInstance().getTopActivity();
                        if (!mViewMap.containsKey(activity)) {
                            Message message = mHandler.obtainMessage(MSG_SHOW_DIALOG, activity);
                            mHandler.sendMessageDelayed(message, 50);
                        }
                    }
                }
                break;
                case SessionInfo.STATE_IDLE://默认状态
                case SessionInfo.STATE_RECORD_END://录音结束
                case SessionInfo.STATE_IAT_END://语音识别结束
                case SessionInfo.STATE_NLP_END://语义识别结束
                case SessionInfo.STATE_FAIL://异常
                    if (isRecording) {
                        isRecording = false;
                        isNeedShow = false;
                        mHandler.removeMessages(MSG_SHOW_DIALOG);
                        mHandler.post(() -> {
                            Activity activity = ActivityManager.getInstance().getTopActivity();
                            dismissDialog(activity);
                        });
                    }
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Application application = (Application) getApplicationContext();
        application.registerActivityLifecycleCallbacks(this.callbacks);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAIManager != null) {
            releaseViewModel();
        }
        Application application = (Application) getApplicationContext();
        application.unregisterActivityLifecycleCallbacks(this.callbacks);
        mAIManager = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        String action = intent.getAction();
        if (null == action) return START_NOT_STICKY;
        JL_Log.w(tag, "onStartCommand", "action : " + action);
        if (ACTION_AI_CLOUD.equals(action)) {
            isCanWriteSQL = intent.getBooleanExtra(EXTRA_WRITE_SQL, true);
            boolean isInit = intent.getBooleanExtra(EXTRA_INIT, false);
            if (isInit) {
                isNeedShow = false;
                isCanWriteSQL = true;
                if (mAIManager != null) {
                    releaseViewModel();
                }
                mAIManager = AIManager.getInstance();
                initViewModel();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void dismissDialog(@NonNull Activity activity) {
        View root = mViewMap.remove(activity);
        if (null != root) {
            WindowManager wm = (WindowManager) root.getTag();
            if (null != wm) {
                wm.removeViewImmediate(root);
            }
        }
    }

    @SuppressLint("InflateParams")
    private void showDialog(@NonNull Activity activity) {
        View root;
        root = LayoutInflater.from(this).inflate(R.layout.dialog_ai_cloud, null);

        root.setActivated(true);
        WindowManager wm;
//        Activity activity = ActivityManager.getInstance().getTopActivity();
        if (null == activity || activity.isDestroyed() || activity.isFinishing()) {
            JL_Log.w(tag, "showDialog", "none activity");
            return;
        }
        JL_Log.d(tag, "showDialog", "activity = " + activity);
        wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;//表示悬浮窗口不会阻塞事件传递，即用户点击悬浮窗口以外的区域时，事件会传递给后面的窗口处理。

        lp.format = PixelFormat.RGBA_8888;
        //window类型
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        if (wm.getDefaultDisplay() != null && wm.getDefaultDisplay().isValid()) {
            try {
                wm.addView(root, lp);
                root.setTag(wm);
                mViewMap.put(activity, root);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        root.setOnClickListener(v -> {
            if (isCanWriteSQL) {//说明在AICloudHistoryMessageFragment页面
                Activity topActivity = ActivityManager.getInstance().getTopActivity();
                ContentActivity.startContentActivity(topActivity, AICloudHistoryMessageFragment.class.getCanonicalName());
            }
        });
    }

    private void initViewModel() {
        if (mAIManager.isInit()) {
            mAIManager.getAICloudServe().currentSessionMessageMLD.observeForever(mRecordStatusObserver);
        }
    }

    private void releaseViewModel() {
        if (mAIManager.isInit()) {
            mAIManager.getAICloudServe().currentSessionMessageMLD.removeObserver(mRecordStatusObserver);
        }
    }
}
