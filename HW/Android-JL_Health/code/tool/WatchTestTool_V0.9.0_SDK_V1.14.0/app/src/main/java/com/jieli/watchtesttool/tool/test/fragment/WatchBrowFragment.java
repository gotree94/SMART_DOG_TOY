package com.jieli.watchtesttool.tool.test.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.component.utils.ToastUtil;
import com.jieli.component.utils.ValueUtil;
import com.jieli.jl_fatfs.model.FatFile;
import com.jieli.jl_filebrowse.FileBrowseManager;
import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.jl_rcsp.constant.JLChipFlag;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.model.response.ExternalFlashMsgResponse;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.WatchApplication;
import com.jieli.watchtesttool.tool.test.ITestTask;
import com.jieli.watchtesttool.tool.test.LogDialog;
import com.jieli.watchtesttool.tool.test.fattask.FatDeleteWatchTask;
import com.jieli.watchtesttool.tool.test.fattask.FatInsertBgTask;
import com.jieli.watchtesttool.tool.test.fattask.FatInsertWatchTask;
import com.jieli.watchtesttool.tool.watch.WatchManager;
import com.jieli.watchtesttool.ui.base.BaseFragment;
import com.jieli.watchtesttool.util.AppUtil;
import com.jieli.watchtesttool.util.WLog;
import com.jieli.watchtesttool.util.WatchTestConstant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 6/9/21
 * @desc : 表盘浏览
 */
public class WatchBrowFragment extends BaseFragment implements OnWatchOpCallback<ArrayList<FatFile>> {
    private final WatchManager mWatchManager = WatchManager.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_watch_vrow, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWatchManager.listWatchList(this);
    }

    @Override
    public void onSuccess(ArrayList<FatFile> fatFiles) {
        WLog.e(tag, "list watch success flash size = " + fatFiles.size());
        LinearLayout parent = requireView().findViewById(R.id.ll_watch_brow);
        parent.removeAllViews();
        parent.addView(createTestItem("添加表盘", v -> showAddDialog(false), null));
        parent.addView(createTestItem("添加表盘背景", v -> showAddDialog(true), null));
        for (FatFile f : fatFiles) {
            parent.addView(createTestItem(f.getName(), v -> selectWatch(f), v -> {
                delete(f);
                return true;
            }));
        }

    }


    @Override
    public void onFailed(BaseError baseError) {

    }


    private void showAddDialog(boolean isBg) {
        DeviceInfo deviceInfo = mWatchManager.getDeviceInfo();
        if (null == deviceInfo) return;
        String subDir;
        String versionDir = null;
        String dirPath;
        switch (deviceInfo.getSdkType()) {
            case JLChipFlag.JL_CHIP_FLAG_701X_WATCH:
                subDir = WatchTestConstant.DIR_BR28;
                if (!isBg) {
                    versionDir = WatchTestConstant.VERSION_W001;
                    ExternalFlashMsgResponse flashMsg = mWatchManager.getExternalFlashMsg(mWatchManager.getConnectedDevice());
                    if (null != flashMsg) {
                        String[] matchVersions = flashMsg.getMatchVersions();
                        if (matchVersions != null) {
                            for (String matchVersion : matchVersions) {
                                if (WatchTestConstant.VERSION_W002.equalsIgnoreCase(matchVersion)) {
                                    versionDir = WatchTestConstant.VERSION_W002;
                                    break;
                                }
                            }
                        }
                    }
                }
                break;
            case JLChipFlag.JL_CHIP_FLAG_707N_WATCH:
                subDir = WatchTestConstant.DIR_BR35;
                break;
            default:
                subDir = WatchTestConstant.DIR_BR23;
                break;
        }
        if (TextUtils.isEmpty(versionDir)) {
            dirPath = AppUtil.createFilePath(WatchApplication.getWatchApplication(), isBg ? WatchTestConstant.DIR_WATCH_BG : WatchTestConstant.DIR_WATCH, subDir);
        } else {
            dirPath = AppUtil.createFilePath(WatchApplication.getWatchApplication(), isBg ? WatchTestConstant.DIR_WATCH_BG : WatchTestConstant.DIR_WATCH, subDir, versionDir);
        }
        String[] files = new File(dirPath).list();
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.file_selector))
                .setItems(files, (dialog, which) -> {
                    assert files != null;
                    String path = dirPath + File.separator + files[which];
                    ITestTask task = isBg ? new FatInsertBgTask(mWatchManager, path) : new FatInsertWatchTask(mWatchManager, path);
                    LogDialog logDialog = new LogDialog(task, v -> task.stopTest());
                    task.setINextTask(error -> {
                        logDialog.setCancelable(true);
                        if (error.code == 0) {
                            ToastUtil.showToastShort("插入成功");
                            mWatchManager.listWatchList(WatchBrowFragment.this);
                        } else {
                            ToastUtil.showToastShort("插入失败");
                        }
                    });
                    logDialog.show(getChildFragmentManager(), LogDialog.class.getSimpleName());
                    task.startTest();
                })
                .create();
        alertDialog.show();

    }


    protected void delete(final FatFile fatFile) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.tips)
                .setMessage("是否要删除：" + fatFile.getName())
                .setCancelable(true)
                .setPositiveButton(R.string.sure, (dialog, which) -> {
                    ITestTask task = new FatDeleteWatchTask(mWatchManager, fatFile);
                    LogDialog logDialog = new LogDialog(task, v -> task.stopTest());
                    task.setINextTask(error -> {
                        logDialog.setCancelable(true);
                        WLog.e("sen", "delete status-->" + error);
                        if (error.code == 0) {
                            ToastUtil.showToastShort("删除成功");
                            List<SDCardBean> list = FileBrowseManager.getInstance().getOnlineDev();
                            for (SDCardBean sdCardBean : list) {
                                if (sdCardBean.getType() == SDCardBean.FLASH) {
                                    FileBrowseManager.getInstance().cleanCache(sdCardBean);
                                }
                            }
                            mWatchManager.listWatchList(WatchBrowFragment.this);
                        } else {
                            ToastUtil.showToastShort("删除失败");
                        }

                    });
                    logDialog.show(getChildFragmentManager(), LogDialog.class.getSimpleName());
                    task.startTest();
                })
                .setNegativeButton(R.string.cancel, (d, w) -> {

                })
                .create()
                .show();
    }

    protected void selectWatch(final FatFile fatFile) {
        WLog.i("zzc", "selectWatch-->" + fatFile.getPath());
        mWatchManager.setCurrentWatchInfo(fatFile.getPath(), new OnWatchOpCallback<FatFile>() {
            @Override
            public void onSuccess(FatFile fatFile) {
                ToastUtil.showToastShort("设置成功");
            }

            @Override
            public void onFailed(BaseError baseError) {
                ToastUtil.showToastShort("设置失败");
            }
        });
    }

    public View createTestItem(String name, View.OnClickListener listener, View.OnLongClickListener longClickListener) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ValueUtil.dp2px(requireContext(), 44));
        Button btn = new Button(getContext());
        if (listener != null) {
            btn.setOnClickListener(listener);
        }
        if (longClickListener != null) {
            btn.setOnLongClickListener(longClickListener);
        }
        btn.setText(name);
        btn.setLayoutParams(lp);
        return btn;
    }


}
