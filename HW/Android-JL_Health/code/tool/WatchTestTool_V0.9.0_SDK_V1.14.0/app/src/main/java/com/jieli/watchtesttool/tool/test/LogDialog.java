package com.jieli.watchtesttool.tool.test;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jieli.component.utils.ToastUtil;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.ui.base.BaseDialogFragment;

import java.util.Locale;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/29/21
 * @desc :
 */
public class LogDialog extends BaseDialogFragment implements OnTaskChangeCallback, OnTestLogCallback, INextTask {

    private TextView tvTaskName;
    private TextView tvTaskCount;
    private Button btnTaskCancel;

    private ITestTask task;
    private int taskIndex = 1;
    private int size = 1;

    private final View.OnClickListener mCancelClickListener;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    public LogDialog(@NonNull ITestTask task, View.OnClickListener cancelClickListener) {
        this.task = task;
        this.mCancelClickListener = cancelClickListener;
        task.setOnTestLogCallback(this);
        task.setINextTask(this);
        if (task instanceof TestTaskQueue) {
            TestTaskQueue queue = (TestTaskQueue) task;
            setSize(queue.size());
            queue.setOnTaskChangeCallback(this);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Window window = requireDialog().getWindow();
        if (window == null) return;
        WindowManager.LayoutParams mLayoutParams = window.getAttributes();
        mLayoutParams.gravity = Gravity.CENTER;
        mLayoutParams.dimAmount = 0.5f;
        mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;

        mLayoutParams.width = (int) (getScreenWidth() * 0.9f);
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        window.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.text_transparent)));
        window.getDecorView().getRootView().setBackgroundColor(Color.TRANSPARENT);
        window.setAttributes(mLayoutParams);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        btnTaskCancel.setVisibility(View.VISIBLE);
        updateTask(task);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Light_NoTitleBar);
        setCancelable(false);
        View view = inflater.inflate(R.layout.dialog_log, container, false);
        tvTaskName = view.findViewById(R.id.tv_log_task);
        tvTaskCount = view.findViewById(R.id.tv_log_task_pos);
        btnTaskCancel = view.findViewById(R.id.btn_cancel_test);
        btnTaskCancel.setOnClickListener(mCancelClickListener);
        return view;
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        super.onDestroyView();
        this.task.setINextTask(null);
        this.task.setOnTestLogCallback(null);
        uiHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onTaskChange(ITestTask task, int index) {
        this.task = task;
        this.taskIndex = index + 1;
        uiHandler.post(() -> updateTask(this.task));
    }

    @Override
    public void onLog(String log) {
        uiHandler.post(() -> {
            if (isDetached() || !isAdded() || getView() == null) return;
            TextView textView = getView().findViewById(R.id.tv_log_msg);
            textView.setText(log);
        });
    }

    @Override
    public void next(TestError error) {
        uiHandler.post(() -> {
            if (isAdded() && !isDetached() && btnTaskCancel != null && btnTaskCancel.getVisibility() != View.GONE) {
                btnTaskCancel.setVisibility(View.GONE);
            }
            setCancelable(true);
            ToastUtil.showToastShort(error.msg);
        });
    }

    private void setSize(int size) {
        this.size = size;
    }

    private void updateTask(ITestTask task) {
        if (null == task || isDetached() || !isAdded()) return;
        tvTaskName.setText(task.getName());
        String text = String.format(Locale.getDefault(), "(%d/%d)", taskIndex, size);
        tvTaskCount.setText(text);
    }
}
