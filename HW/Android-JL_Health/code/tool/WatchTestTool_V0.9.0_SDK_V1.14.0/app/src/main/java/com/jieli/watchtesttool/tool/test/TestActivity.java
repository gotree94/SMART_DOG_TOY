package com.jieli.watchtesttool.tool.test;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.jieli.component.utils.ToastUtil;
import com.jieli.component.utils.ValueUtil;
import com.jieli.jl_filebrowse.FileBrowseManager;
import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.tool.config.ConfigHelper;
import com.jieli.watchtesttool.tool.test.contacts.NewContestTestTask;
import com.jieli.watchtesttool.tool.test.fattask.FatFileTestTask;
import com.jieli.watchtesttool.tool.test.filetask.FileTransferTask;
import com.jieli.watchtesttool.tool.test.fragment.WatchBrowFragment;
import com.jieli.watchtesttool.tool.watch.WatchManager;
import com.jieli.watchtesttool.ui.ContentActivity;
import com.jieli.watchtesttool.ui.base.BaseActivity;
import com.jieli.watchtesttool.ui.file.FilesFragment;
import com.jieli.watchtesttool.ui.file.SmallFileTransferCmdFragmentTest;
import com.jieli.watchtesttool.ui.message.msg.SyncMessageFragment;
import com.jieli.watchtesttool.ui.message.weather.SyncWeatherFragment;
import com.jieli.watchtesttool.ui.ota.NetworkOtaFragment;
import com.jieli.watchtesttool.ui.record.RecordTestFragment;
import com.jieli.watchtesttool.ui.upgrade.UpgradeFragment;
import com.jieli.watchtesttool.util.WLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class TestActivity extends BaseActivity {
    protected ConfigHelper configHelper = ConfigHelper.getInstance();

    private WatchManager mWatchManager;

    protected void initTestList(WatchManager manager, @NonNull ViewGroup viewGroup) {
        if (mWatchManager == null) {
            mWatchManager = manager;
        }
        viewGroup.removeAllViews();
//        viewGroup = findViewById(R.id.ll_test);

        if (configHelper.isTestFileTransfer()) {
            viewGroup.addView(createTestItem(getString(R.string.func_file_transfer), v -> showDevSelectDialog(this::startFileTransferTest)));

            viewGroup.addView(createTestItem(getString(R.string.func_music_transfer), v -> showDevSelectDialog(sdCardBean -> showCountSelectDialog(count -> {
                try {
                    ITestTask task = new TestTaskQueue.Factory(count, new FileTransferTask.RandomMusicFactory(sdCardBean)).create();
                    startTaskWithDialog(task, true);
                } catch (Exception e) {
                    showTips(getString(R.string.create_test_task_failed) + e.getMessage());
                }
            }))));

            viewGroup.addView(createTestItem(getString(R.string.func_contacts), v -> showCountSelectDialog(count -> {
                try {
                    ITestTask task = new TestTaskQueue.Factory(count,
                            new NewContestTestTask.Factory(getApplicationContext())).create();
                    startTaskWithDialog(task, true);
                } catch (Exception e) {
                    showTips(getString(R.string.create_test_task_failed) + e.getMessage());
                }
            })));
        }
        if (configHelper.isTestFileBrowse()) {
            viewGroup.addView(createTestItem(getString(R.string.func_file_browse), v -> showDevSelectDialog(sdCardBean -> {
                Bundle args = new Bundle();
//                String json = new Gson().toJson(sdCardBean);
//                FileBrowseManager.getInstance().cleanCache(sdCardBean);
                args.putInt(FilesFragment.KEY_SDCARDBEAD_INDEX, sdCardBean.getIndex());
                ContentActivity.startContentActivity(TestActivity.this, FilesFragment.class.getCanonicalName(), args);
            })));
        }

        if (configHelper.isTestWatchOp()) {
            if (!configHelper.isBanAutoTest()) {
                viewGroup.addView(createTestItem(getString(R.string.func_insert_watch), v -> showCountSelectDialog(count -> {
                    try {
                        ITestTask task = new TestTaskQueue.Factory(count,
                                new FatFileTestTask.Factory(false)).create();
                        startTaskWithDialog(task, false);
                    }catch (Exception e){
                        showTips(getString(R.string.create_test_task_failed) + e.getMessage());
                    }
                })));

                viewGroup.addView(createTestItem(getString(R.string.func_insert_watch_bg), v -> showCountSelectDialog(count -> {
                    try {
                        ITestTask task = new TestTaskQueue.Factory(count,
                                new FatFileTestTask.Factory(true)).create();
                        startTaskWithDialog(task, false);
                    }catch (Exception e){
                        showTips(getString(R.string.create_test_task_failed) + e.getMessage());
                    }
                })));
            }

            viewGroup.addView(createTestItem(getString(R.string.func_watch_browse), v -> {
                if (checkDeviceIsDisconnected()) return;
                ContentActivity.startContentActivity(TestActivity.this, WatchBrowFragment.class.getCanonicalName());
            }));
        }

        if (!configHelper.isBanAutoTest()) {
            viewGroup.addView(createTestItem(getString(R.string.func_random_test), v -> {
                List<SDCardBean> list = FileBrowseManager.getInstance().getOnlineDev();
                if (list.isEmpty()) {
                    ToastUtil.showToastShort(getString(R.string.msg_read_file_err_offline));
                    return;
                }
                showCountSelectDialog(count -> {
                    try {
                        ITestTask task = new RandomTaskFactory(list.get(0), count).create();
                        startTaskWithDialog(task, true);
                    }catch (Exception e){
                        showTips(getString(R.string.create_test_task_failed) + e.getMessage());
                    }
                });
            }));
        }

        if (configHelper.isTestSmallFileTransfer() && manager.isWatchSystemOk()
                && manager.getDeviceInfo().isContactsTransferBySmallFile()) {
            viewGroup.addView(createTestItem(getString(R.string.func_small_file_transfer),
                    v -> ContentActivity.startContentActivity(TestActivity.this, SmallFileTransferCmdFragmentTest.class.getCanonicalName())));
        }

        if (configHelper.isTestOTA()) {
            viewGroup.addView(createTestItem(getString(R.string.func_upgrade), v -> {
                if (checkDeviceIsDisconnected()) return;
                ContentActivity.startContentActivity(TestActivity.this, UpgradeFragment.class.getCanonicalName());
            }));
            viewGroup.addView(createTestItem(getString(R.string.func_network_update), v -> {
                if (checkDeviceIsDisconnected()) return;
                final WatchConfigure configure = mWatchManager.getWatchConfigure(mWatchManager.getConnectedDevice());
                JL_Log.d(tag, "configure = " + configure);
                if (configure != null && configure.getFunctionOption().isSupportNetworkModule()) {
                    ContentActivity.startContentActivity(TestActivity.this, NetworkOtaFragment.class.getCanonicalName());
                } else {
                    ToastUtil.showToastShort(getString(R.string.unsupport_func_tips));
                }
            }));
        }

        if (configHelper.isTestMessageSync()) {
            viewGroup.addView(createTestItem(getString(R.string.func_weather_sync), v -> {
                if (checkDeviceIsDisconnected()) return;
                ContentActivity.startContentActivity(TestActivity.this, SyncWeatherFragment.class.getCanonicalName());
            }));
            viewGroup.addView(createTestItem(getString(R.string.func_message_sync), v -> {
                if (checkDeviceIsDisconnected()) return;
                ContentActivity.startContentActivity(TestActivity.this, SyncMessageFragment.class.getCanonicalName());
            }));
        }
        if (configHelper.isTestRecord()) {
            viewGroup.addView(createTestItem(getString(R.string.func_record_test), v -> {
                if (checkDeviceIsDisconnected()) return;
                ContentActivity.startContentActivity(TestActivity.this, RecordTestFragment.class.getCanonicalName());
            }));
        }
    }

    private void startFileTransferTest(SDCardBean sdCardBean) {
        showFileSelect(file -> showCountSelectDialog(count -> {
            try {
                ITestTask task = new TestTaskQueue.Factory(count,
                        new FileTransferTask.Factory(sdCardBean, file.getPath())).create();
                startTaskWithDialog(task, true);
            }catch (Exception e){
                showTips(getString(R.string.create_test_task_failed) + e.getMessage());
            }
        }));
    }

    private void startTaskWithDialog(ITestTask testTask, boolean needCheckCard) {

        TextView tvDelay = findViewById(R.id.et_delay);

        final LogDialog dialog = new LogDialog(testTask, v -> testTask.stopTest());
        if (testTask instanceof TestTaskQueue) {
            TestTaskQueue queue = (TestTaskQueue) testTask;
            if (TextUtils.isEmpty(tvDelay.getText().toString())) {
                queue.delayTask = 3000;
            } else {
                queue.delayTask = Integer.parseInt(tvDelay.getText().toString());
            }
        }
        dialog.show(getSupportFragmentManager(), LogDialog.class.getSimpleName());
        testTask.startTest();
    }


    public View createTestItem(String name, View.OnClickListener listener) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ValueUtil.dp2px(this, 44));
        Button btn = new Button(this);
        btn.setOnClickListener(listener);
        btn.setText(name);
        btn.setLayoutParams(lp);
        return btn;
    }


    public void showDevSelectDialog(OnSelect<SDCardBean> select) {
        if (checkDeviceIsDisconnected()) return;
        List<SDCardBean> tmp = FileBrowseManager.getInstance().getOnlineDev();
        List<SDCardBean> list = new ArrayList<>();
        for (SDCardBean sdCardBean : tmp) {
            /*if (sdCardBean.getType() != SDCardBean.FLASH || true) */
            {
                list.add(sdCardBean);
            }
        }
        JL_Log.d(tag, "showDevSelectDialog : " + list);
        if (list.isEmpty()) {
            ToastUtil.showToastShort(getString(R.string.msg_read_file_err_offline));
        } else if (list.size() == 1) {
            select.onSelect(list.get(0));
        } else {
            String[] item = new String[list.size()];
            for (int i = 0; i < item.length; i++) {
                SDCardBean sdCardBean = list.get(i);
                item[i] = sdCardBean.getName();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.choose_device))
                    .setItems(item, (dialog, which) -> {
                        if (select != null) {
                            select.onSelect(list.get(which));
                        }
                    });
            builder.create().show();
        }
    }


    private void showCountSelectDialog(OnSelect<Integer> timeSelect) {
        if (!checkDeviceConnectedAndStorageExist()) return;
        if (configHelper.isBanAutoTest()) {
            if (timeSelect != null) timeSelect.onSelect(1);
            return;
        }
        String[] item = new String[100];
        for (int i = 0; i < item.length; i++) {
            int count = Math.max(1, i * 5);
            item[i] = count + "";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.choose_count))
                .setItems(item, (dialog, which) -> {
//                    WLog.e(tag, "which = " + which);
                    if (timeSelect != null) {
                        timeSelect.onSelect(Integer.parseInt(item[which]));
                    }
                });

        builder.create().show();

    }

    private void listFile(File file, List<File> output) {
        if (null == file || !file.exists()) return;
        final File[] fileArray = file.listFiles();
        if (fileArray == null) return;
        for (File f : fileArray) {
            if (f.isDirectory()) {
                listFile(f, output);
            } else {
                output.add(f);
            }
        }
    }

    private void showFileSelect(OnSelect<File> onSelect) {
        if (!checkDeviceConnectedAndStorageExist()) return;
        File file = getApplication().getExternalFilesDir(null);
        if (file == null) return;
        List<File> files = new ArrayList<>();
        listFile(file, files);
        String[] names = new String[files.size()];
        for (int i = 0; i < files.size(); i++) {
            names[i] = files.get(i).getName();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.choose_file))
                .setItems(names, (dialog, which) -> {
                    WLog.e(tag, "which = " + files.get(which).getName());
                    if (onSelect != null) {
                        onSelect.onSelect(files.get(which));
                    }
                });

        builder.create().show();
    }


    private boolean checkDeviceIsDisconnected() {
        if (mWatchManager.getConnectedDevice() == null) {
            ToastUtil.showToastShort(getString(R.string.device_is_disconnected));
            return true;
        }
        return false;
    }

    private boolean checkDeviceConnectedAndStorageExist() {
        if (checkDeviceIsDisconnected()) return false;
        if (FileBrowseManager.getInstance().getOnlineDev().isEmpty()) {
            ToastUtil.showToastShort(getString(R.string.msg_read_file_err_offline));
            return false;
        }
        return true;
    }

    private interface OnSelect<T> {
        void onSelect(T t);
    }

}