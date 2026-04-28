package com.jieli.watchtesttool.ui.file;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.jl_rcsp.constant.RcspErrorCode;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspCallback;
import com.jieli.jl_rcsp.interfaces.rcsp.RcspCommandCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.command.file_op.SmallFileTransferCmd;
import com.jieli.jl_rcsp.task.TaskListener;
import com.jieli.jl_rcsp.task.smallfile.AddFileTask;
import com.jieli.jl_rcsp.task.smallfile.QueryFileTask;
import com.jieli.jl_rcsp.task.smallfile.ReadFileTask;
import com.jieli.jl_rcsp.task.smallfile.UpdateFileTask;
import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.data.bean.SportRecord;
import com.jieli.watchtesttool.databinding.FragmentSmallFileTestBinding;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothEventListener;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothHelper;
import com.jieli.watchtesttool.tool.watch.WatchManager;
import com.jieli.watchtesttool.ui.base.BaseFragment;
import com.jieli.watchtesttool.ui.file.adapter.TestSpinnerAdapter;
import com.jieli.watchtesttool.ui.file.model.TestTypeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/10/14
 * @desc :
 */
public class SmallFileTransferCmdFragmentTest extends BaseFragment {
    private static final int FILE_SIZE = 1024;
    FragmentSmallFileTestBinding binding;
    private Adapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSmallFileTestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BluetoothHelper.getInstance().addBluetoothEventListener(bluetoothEventListener);
        WatchManager.getInstance().registerOnRcspCallback(rcspCallback);
        initUI();
        loadTestType();
    }

    @Override
    public void onDestroyView() {
        BluetoothHelper.getInstance().removeBluetoothEventListener(bluetoothEventListener);
        WatchManager.getInstance().unregisterOnRcspCallback(rcspCallback);
        super.onDestroyView();
    }

    private void initUI() {
        adapter = new Adapter();
        binding.rvIds.setAdapter(adapter);
        binding.rvIds.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvIds.addItemDecoration(new DividerItemDecoration(requireContext(), RecyclerView.VERTICAL));

        binding.btnQueryIds.setOnClickListener(v -> {
            TestTypeItem item = (TestTypeItem) binding.spTestType.getSelectedItem();
            JL_Log.d(tag, "query type = " + item);
            queryFile(item.getType());
        });

        binding.btnAddFile.setOnClickListener(v -> {
            TestTypeItem item = (TestTypeItem) binding.spTestType.getSelectedItem();
            JL_Log.d(tag, "add type = " + item);
            startAddTask(CHexConver.intToByte(item.getType()));
        });

        binding.btnClearLog.setOnClickListener(v -> binding.tvCmdLog.setText(""));

        adapter.addChildClickViewIds(R.id.btn_update, R.id.btn_read, R.id.btn_delete);
        adapter.setOnItemChildClickListener((a, view, position) -> {
            SmallFileTransferCmd.QueryResponse.File file = adapter.getItem(position);
            if (view.getId() == R.id.btn_read) {
                Log.e("sen", "read");
                startReadFile(file);
            } else if (view.getId() == R.id.btn_delete) {
                Log.e("sen", "delete");
                deleteFile(file);
            } else if (view.getId() == R.id.btn_update) {
                Log.e("sen", "update");
                startUpdateTask(file);
            }
        });
        List<SmallFileTransferCmd.QueryResponse.File> list = new ArrayList<>();
//        list.add(new SmallFileTransferCmd.QueryResponse.File((byte) 0, (short) 0, 123));
        adapter.setList(list);
    }

    private void loadTestType() {
        List<TestTypeItem> list = new ArrayList<>();
        list.add(new TestTypeItem(SmallFileTransferCmd.TYPE_CONTACTS, "联系人"));
        list.add(new TestTypeItem(SmallFileTransferCmd.TYPE_SPORTS_RECORD, "运动记录"));
        list.add(new TestTypeItem(SmallFileTransferCmd.TYPE_HEART_RATE, "心率数据"));
        list.add(new TestTypeItem(SmallFileTransferCmd.TYPE_BLOOD_OXYGEN, "血氧数据"));
        list.add(new TestTypeItem(SmallFileTransferCmd.TYPE_SLEEP, "睡眠数据"));
        list.add(new TestTypeItem(SmallFileTransferCmd.TYPE_STEP, "步数数据"));
        binding.spTestType.setAdapter(new TestSpinnerAdapter(requireContext(), list));
        binding.spTestType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TestTypeItem item = (TestTypeItem) binding.spTestType.getSelectedItem();
                JL_Log.d(tag, "query type = " + item);
                queryFile(item.getType());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding.spTestType.setSelection(1);
    }

    private void queryFile(int type) {
        SmallFileTransferCmd.Param param = new SmallFileTransferCmd.QueryParam(CHexConver.intToByte(type));
        WatchManager.getInstance().sendRcspCommand(WatchManager.getInstance().getTargetDevice(), new SmallFileTransferCmd(param),
                new RcspCommandCallback<SmallFileTransferCmd>() {
                    @Override
                    public void onCommandResponse(BluetoothDevice device, SmallFileTransferCmd cmd) {
                        if (cmd.getStatus() != RcspErrorCode.ERR_NONE) {
                            onErrCode(device, new BaseError(0, "status = " + cmd.getStatus()));
                            return;
                        }
                        SmallFileTransferCmd.QueryResponse response = (SmallFileTransferCmd.QueryResponse) cmd.getResponse();
                        adapter.setList(response.getFiles());
                        binding.tvCmdLog.append(" 读取文件列表结束  ");

                    }

                    @Override
                    public void onErrCode(BluetoothDevice device, BaseError error) {
                        binding.tvCmdLog.append(" 读取文件列表失败  " + error.getMessage());
                    }
                });
    }

    private void startReadFile(SmallFileTransferCmd.QueryResponse.File file) {
        ReadFileTask.Param param = new ReadFileTask.Param(file.type, file.id, file.size, 0);

        ReadFileTask readFileTask = new ReadFileTask(WatchManager.getInstance(), param);
        readFileTask.setListener(new TaskListener() {
            @Override
            public void onBegin() {

            }

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onFinish() {
                byte[] fileData = readFileTask.getReadData();
                binding.tvCmdLog.append("文件读取完成: \n");
                binding.tvCmdLog.append(CHexConver.byte2HexStr(fileData));
                if (file.type == QueryFileTask.TYPE_SPORTS_RECORD) {
                    SportRecord sportRecord = SportRecord.from(fileData);
                    AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                            .setMessage(sportRecord.toString())
                            .create();
                    alertDialog.show();
                }
            }

            @Override
            public void onError(int code, String msg) {
                binding.tvCmdLog.append("read file failed " + msg);

            }

            @Override
            public void onCancel(int reason) {

            }
        });
        readFileTask.start();
    }

    private void deleteFile(SmallFileTransferCmd.QueryResponse.File file) {
        SmallFileTransferCmd.Param param = new SmallFileTransferCmd.DeleteFileParam(file.type, file.id);
        SmallFileTransferCmd cmd = new SmallFileTransferCmd(param);
        WatchManager.getInstance().sendRcspCommand(WatchManager.getInstance().getTargetDevice(), cmd, new RcspCommandCallback<SmallFileTransferCmd>() {
            @Override
            public void onCommandResponse(BluetoothDevice device, SmallFileTransferCmd cmd) {
                if (cmd.getStatus() != RcspErrorCode.ERR_NONE) {
                    onErrCode(device, new BaseError(0, "status = " + cmd.getStatus()));
                    return;
                }
                SmallFileTransferCmd.ResultResponse response = (SmallFileTransferCmd.ResultResponse) cmd.getResponse();
                if (response.ret != 0x00) {
                    onErrCode(device, new BaseError(0, "ret  = " + cmd.getStatus()));
                    return;
                }
                queryFile(file.type);
                binding.tvCmdLog.append(" 删除文件结束  ");
            }

            @Override
            public void onErrCode(BluetoothDevice device, BaseError error) {
                binding.tvCmdLog.append(" 删除文件失败  " + error.getMessage());
            }
        });
    }

    private void startUpdateTask(SmallFileTransferCmd.QueryResponse.File file) {
        UpdateFileTask.Param param = new UpdateFileTask.Param(file.type, file.id, new byte[FILE_SIZE]);

        UpdateFileTask task = new UpdateFileTask(WatchManager.getInstance(), param);
        task.setListener(new TaskListener() {
            @Override
            public void onBegin() {

            }

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onFinish() {
                binding.tvCmdLog.append(" 修改文件结束");
            }

            @Override
            public void onError(int code, String msg) {
                binding.tvCmdLog.append("update file error:" + msg);

            }

            @Override
            public void onCancel(int reason) {

            }
        });
        task.start();
    }

    private void startAddTask(byte type) {
        AddFileTask.Param param = new AddFileTask.Param(type, new byte[FILE_SIZE]);
        AddFileTask task = new AddFileTask(WatchManager.getInstance(), param);
        task.setListener(new TaskListener() {
            @Override
            public void onBegin() {

            }

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onFinish() {
                queryFile(type);
                binding.tvCmdLog.append(" 新增文件结束");
            }

            @Override
            public void onError(int code, String msg) {
                binding.tvCmdLog.append("add file error:" + msg);
            }

            @Override
            public void onCancel(int reason) {

            }
        });
        task.start();
    }


    private final BluetoothEventListener bluetoothEventListener = new BluetoothEventListener() {

        @Override
        public void onReceiveData(BluetoothDevice device, byte[] data) {
            super.onReceiveData(device, data);
            String text = "recv:" + CHexConver.byte2HexStr(data);
            binding.tvCmdLog.append(text);
            binding.tvCmdLog.append("\n");
        }


    };

    private final OnRcspCallback rcspCallback = new OnRcspCallback() {

        @Override
        public void onPutDataToDataHandler(BluetoothDevice device, byte[] data) {
            super.onPutDataToDataHandler(device, data);
            binding.tvCmdLog.append("write:");
            binding.tvCmdLog.append(CHexConver.byte2HexStr(data));
            binding.tvCmdLog.append("\n");
        }
    };

    private static class Adapter extends BaseQuickAdapter<SmallFileTransferCmd.QueryResponse.File, BaseViewHolder> {

        public Adapter() {
            super(R.layout.item_small_file);
        }

        @Override
        protected void convert(@NonNull BaseViewHolder baseViewHolder, SmallFileTransferCmd.QueryResponse.File file) {
            baseViewHolder.setText(R.id.tv_id, "type:" + file.type + "\tid:" + file.id);

        }
    }
}
