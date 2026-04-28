package com.jieli.watchtesttool.ui.file;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.module.BaseLoadMoreModule;
import com.jieli.component.utils.ToastUtil;
import com.jieli.component.utils.ValueUtil;
import com.jieli.jl_filebrowse.FileBrowseManager;
import com.jieli.jl_filebrowse.bean.FileStruct;
import com.jieli.jl_filebrowse.bean.Folder;
import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.jl_rcsp.constant.AttrAndFunCode;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.databinding.FragmentFilesBinding;
import com.jieli.watchtesttool.tool.test.ITaskFactory;
import com.jieli.watchtesttool.tool.test.ITestTask;
import com.jieli.watchtesttool.tool.test.LogDialog;
import com.jieli.watchtesttool.tool.test.TestTaskQueue;
import com.jieli.watchtesttool.tool.test.filetask.ReadFileByClusterTask;
import com.jieli.watchtesttool.tool.test.filetask.ReadFileByNameTask;
import com.jieli.watchtesttool.tool.test.fragment.CacheFileFragment;
import com.jieli.watchtesttool.tool.watch.WatchManager;
import com.jieli.watchtesttool.ui.ContentActivity;
import com.jieli.watchtesttool.ui.base.BaseFragment;
import com.jieli.watchtesttool.ui.file.adapter.FileListAdapter;
import com.jieli.watchtesttool.ui.file.adapter.FileRouterAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 设备文件浏览界面
 */
public class FilesFragment extends BaseFragment {
    FragmentFilesBinding binding;
    public static final String KEY_SDCARDBEAD_INDEX = "KEY_SDCARDBEAD_INDEX";

    private SDCardBean mSdCardBean = new SDCardBean();
    private FileListAdapter mFileListAdapter;
    private BaseLoadMoreModule mLoadMoreModule;
    private final FileRouterAdapter mFileRouterAdapter = new FileRouterAdapter();

    private DeviceFileViewModel mViewModel;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int index = requireArguments().getInt(KEY_SDCARDBEAD_INDEX, -1);
        List<SDCardBean> sdCardBeans = FileBrowseManager.getInstance().getSdCardBeans();
        for (SDCardBean sdCardBean : sdCardBeans) {
            if (index == sdCardBean.getIndex()) {
                mSdCardBean = sdCardBean;
                return;
            }
        }

        requireActivity().finish();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentFilesBinding.inflate(inflater, container, false);

        //初始化文件路径导航栏
        binding.rvFilePathNav.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        binding.rvFilePathNav.setAdapter(mFileRouterAdapter);
        mFileRouterAdapter.setOnItemClickListener((adapter, view12, position) -> mViewModel.back(mFileRouterAdapter.getItem(position)));

        //初始化文件列表ui
        mFileListAdapter = createFileAdapter();
        binding.rvDeviceFiles.addItemDecoration(new DividerItemDecoration(requireContext(), RecyclerView.VERTICAL));
        binding.rvDeviceFiles.setAdapter(mFileListAdapter);
        binding.rvDeviceFiles.setLayoutManager(new LinearLayoutManager(getContext()));
        mLoadMoreModule = mFileListAdapter.getLoadMoreModule();
        mLoadMoreModule.setOnLoadMoreListener(this::onLoadMoreRequested);

        //没有设备则返回
        if (mSdCardBean == null) {
            return binding.getRoot();
        }
        createEmptyView();
        mFileListAdapter.setOnItemClickListener((adapter, view1, position) -> {
            FileStruct fileStruct = mFileListAdapter.getItem(position);
            if (fileStruct != null && fileStruct.isFile()) {
                handleFileClick(fileStruct);
            } else {
                handleFolderClick(fileStruct);
            }
        });


        mFileListAdapter.setOnItemLongClickListener((adapter, view, position) -> {
            FileStruct fileStruct = mFileListAdapter.getItem(position);
            if (fileStruct.isFile()) {
                handleLongClick(fileStruct);
                return true;
            }
            return false;
        });
        //读取当前浏览信息


        return binding.getRoot();
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onActivityCreated(@Nullable Bundle bundle) {
        super.onActivityCreated(bundle);
        mViewModel = new ViewModelProvider(requireActivity()).get(DeviceFileViewModel.class);
        mViewModel.mSDCardBean = mSdCardBean;
        mViewModel.currentFolderMutableLiveData.observe(getViewLifecycleOwner(), this::refreshFileRouterView);
        mViewModel.filesMutableLiveData.observe(getViewLifecycleOwner(), fileStructs -> mFileListAdapter.setList(fileStructs));
        mViewModel.readStateMutableLiveData.observe(getViewLifecycleOwner(), state -> {
            JL_Log.e("sen", "onFileReadStop---> " + state);
            switch (state) {
                case DeviceFileViewModel.STATE_FINISH:
                    mLoadMoreModule.loadMoreEnd();
                    showEmptyView(true);
                    break;
                case DeviceFileViewModel.STATE_END:
                    mLoadMoreModule.loadMoreComplete();
                    showEmptyView(true);
                    break;
                case DeviceFileViewModel.STATE_FAILED:
                    mLoadMoreModule.loadMoreFail();
                    break;
                case DeviceFileViewModel.STATE_START:
                    showEmptyView(false);
                    break;
            }
        });

        mViewModel.musicPlayInfoLiveData.observe(getViewLifecycleOwner(), musicPlayInfo -> {
            mFileListAdapter.setDeviceMode(musicPlayInfo.getDeviceMode());
            if (musicPlayInfo.getDeviceMode() != AttrAndFunCode.SYS_INFO_FUNCTION_MUSIC) {
                mFileListAdapter.notifyDataSetChanged();
            }
            if (musicPlayInfo.getMusicStatusInfo() != null) {
                mFileListAdapter.setSelected((byte) musicPlayInfo.getMusicStatusInfo().getCurrentDev(), mFileListAdapter.getSelectedCluster());
            }
            if (musicPlayInfo.getMusicNameInfo() != null) {
                mFileListAdapter.setSelected(mFileListAdapter.getDevIndex(), musicPlayInfo.getMusicNameInfo().getCluster());
            }

        });

        mViewModel.getCurrentInfo();


        binding.btnLookReadDir.setOnClickListener(v -> ContentActivity.startContentActivity(getContext(), CacheFileFragment.class.getCanonicalName()));
        binding.btnClusterTest.setOnClickListener(v -> randomTest(true));
        binding.btnNameTest.setOnClickListener(v -> randomTest(false));
    }

    private List<FileStruct> getFileList() {
        List<FileStruct> list = new ArrayList<>();
        for (FileStruct file : mFileListAdapter.getData()) {
            if (file.isFile()) {
                list.add(file);
            }
        }
        return list;
    }

    private void randomTest(boolean cluster) {
        List<FileStruct> list = getFileList();
        if (list.isEmpty()) {
            ToastUtil.showToastShort(getString(R.string.folder_none_file));
            return;
        }
        /*if (WatchConstant.BAN_AUTO_TEST) {
            testReadFile(list, cluster, 1);
            return;
        }*/
        String[] item = new String[100];
        for (int i = 0; i < item.length; i++) {
            int count = Math.max(1, i * 5);
            item[i] = count + "";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(getString(R.string.choose_count))
                .setItems(item, (dialog, which) -> {
                    int count = Integer.parseInt(item[which]);
                    testReadFile(list, cluster, count);
                });

        builder.create().show();
    }

    private void testReadFile(List<FileStruct> list, boolean isCluster, int count) {
        try {
            TestTaskQueue task = new TestTaskQueue(count);
            for (int i = 0; i < count; i++) {
                int random = (int) (Math.random() * list.size());
                FileStruct fileStruct = list.get(random);
                ITaskFactory factory = isCluster
                        ? new ReadFileByClusterTask.Factory(WatchManager.getInstance(), mSdCardBean, fileStruct)
                        : new ReadFileByNameTask.Factory(mSdCardBean, fileStruct.getName(), fileStruct.isUnicode());
                task.add(factory.create());
            }

            LogDialog logDialog = new LogDialog(task, v -> task.stopTest());
            logDialog.show(getChildFragmentManager(), LogDialog.class.getSimpleName());
            task.setINextTask(error -> logDialog.setCancelable(true));
            task.startTest();
        }catch (Exception e){
            showTips(getString(R.string.create_test_task_failed) + e.getMessage());
        }
    }

    protected FileListAdapter createFileAdapter() {
        return new FileListAdapter();
    }

    protected FileListAdapter getFileListAdapter() {
        return mFileListAdapter;
    }

    //点击文件
    protected void handleFileClick(FileStruct fileStruct) {
//        mViewModel.play(mSdCardBean, fileStruct);

        String[] items = new String[]{
                getString(R.string.read_file_by_name),
                getString(R.string.read_file_by_cluster),
                getString(R.string.read_file_by_sport_file_id),
                getString(R.string.cancel)
        };
        new AlertDialog.Builder(getContext())
                .setTitle(String.format(Locale.ENGLISH, "%s: %s", getString(R.string.whether_read_file), fileStruct.getName()))
                .setCancelable(true)
                .setItems(items, (dialog, which) -> {
                    if (which >= items.length - 1) return;
                    ITaskFactory factory = which == 0
                            ? new ReadFileByNameTask.Factory(mSdCardBean, fileStruct.getName(), fileStruct.isUnicode())
                            : new ReadFileByClusterTask.Factory(WatchManager.getInstance(), mSdCardBean, fileStruct);
                    try {
                        ITestTask task = factory.create();
                        LogDialog logDialog = new LogDialog(task, v -> task.stopTest());
                        logDialog.show(getChildFragmentManager(), LogDialog.class.getSimpleName());
                        task.setINextTask(error -> {
                            logDialog.setCancelable(true);
                            if (error.code == 0 && which == 2) {
                                logDialog.dismiss();
                            /*File file = new File(ReadFileByClusterTask.READ_FILE_DIR + File.separator + fileStruct.getName());
                            try {
                                FileInputStream fis = new FileInputStream(file.getAbsolutePath());
                                byte[] data = new byte[fis.available()];

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }*/
                            }
                        });

                        task.startTest();
                    } catch (Exception e) {
                        showTips(getString(R.string.create_test_task_failed) + e.getMessage());
                    }
                })
                .create()
                .show();

    }


    //点击文件夹
    protected void handleFolderClick(FileStruct fileStruct) {
        showEmptyView(false);
        mViewModel.append(fileStruct);
    }


    protected void handleLongClick(FileStruct fileStruct) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.tips)
                .setMessage(String.format(Locale.ENGLISH, "%s: %s", getString(R.string.whether_delete_file), fileStruct.getName()))
                .setCancelable(true)
                .setPositiveButton(R.string.sure, (dialog, which) -> mViewModel.delete(fileStruct))
                .setNegativeButton(R.string.cancel, (d, w) -> {

                })
                .create()
                .show();
    }

    //更新顶部导航栏
    private void refreshFileRouterView(Folder folder) {
        if (folder == null) {
            return;
        }
        List<FileStruct> list = new ArrayList<>();
        list.add(folder.getFileStruct());
        while (folder.getParent() != null) {
            folder = folder.getParent();
            list.add(0, folder.getFileStruct());
        }
        mFileRouterAdapter.setNewInstance(list);
        binding.rvFilePathNav.scrollToPosition(mFileRouterAdapter.getData().size() - 1);
    }


    private void onLoadMoreRequested() {
        mViewModel.loadMore();
    }


    private void createEmptyView() {
        //设置空布局，可用xml文件代替
        TextView textView = new TextView(getContext());
        textView.setText(getString(R.string.empty_folder));
        textView.setTextSize(16);
        textView.setTextColor(getResources().getColor(R.color.text_secondary_disable_color));
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_folder_img_empty, 0, 0);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(0, ValueUtil.dp2px(requireContext(), 98), 0, 0);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(layoutParams);
        mFileListAdapter.setEmptyView(textView);
        mFileListAdapter.setUseEmpty(false);
    }


    @SuppressLint("NotifyDataSetChanged")
    private void showEmptyView(boolean show) {
        if (mFileListAdapter.isUseEmpty() != show) {
            mFileListAdapter.setUseEmpty(show);
            mFileListAdapter.notifyDataSetChanged();
        }
    }
}
