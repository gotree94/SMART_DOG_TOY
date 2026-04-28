package com.jieli.healthaide.demos;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import androidx.annotation.NonNull;

import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_filebrowse.FileBrowseManager;
import com.jieli.jl_filebrowse.FileBrowseUtil;
import com.jieli.jl_filebrowse.bean.FileStruct;
import com.jieli.jl_filebrowse.bean.Folder;
import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.jl_filebrowse.interfaces.DeleteCallback;
import com.jieli.jl_filebrowse.interfaces.FileObserver;
import com.jieli.jl_filebrowse.interfaces.OperatCallback;
import com.jieli.jl_rcsp.constant.AttrAndFunCode;
import com.jieli.jl_rcsp.constant.Command;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.interfaces.rcsp.RcspCommandCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.base.CommandBase;
import com.jieli.jl_rcsp.model.command.file_op.DeleteFileByNameCmd;
import com.jieli.jl_rcsp.model.command.sys.GetSysInfoCmd;
import com.jieli.jl_rcsp.model.device.AttrBean;
import com.jieli.jl_rcsp.model.device.DevStorageInfo;
import com.jieli.jl_rcsp.model.device.DevStorageState;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.model.parameter.GetSysInfoParam;
import com.jieli.jl_rcsp.model.response.SysInfoResponse;
import com.jieli.jl_rcsp.task.GetFileByClusterTask;
import com.jieli.jl_rcsp.task.GetFileByNameTask;
import com.jieli.jl_rcsp.task.SimpleTaskListener;
import com.jieli.jl_rcsp.task.TaskListener;
import com.jieli.jl_rcsp.task.format.FormatTask;
import com.jieli.jl_rcsp.tool.BooleanRcspActionCallback;
import com.jieli.jl_rcsp.util.CommandBuilder;
import com.jieli.jl_rcsp.util.JL_Log;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/12/2
 * @desc : 文件管理功能测试
 */
public class FileManagerDemo {
    @Test
    public void brow() {
        FileBrowseManager manager = FileBrowseManager.getInstance();
        FileObserver fileObserver = new FileObserver() {
            @Override
            public void onFileReceiver(List<FileStruct> fileStructs) {
                // 读取到文件列表，仅仅回调本次读取的文件列表
            }

            @Override
            public void onFileReadStop(boolean isEnd) {
                // 文件列表读取结束
            }

            @Override
            public void onFileReadStart() {
                // 开始文件列表读取
            }

            @Override
            public void onFileReadFailed(int reason) {
                // 文件列表读取失败
            }

            @Override
            public void onSdCardStatusChange(List<SDCardBean> onLineCards) {
                //在线设备有变化
            }

            @Override
            public void OnFlayCallback(boolean success) {
                //歌曲点播回调
            }
        };
        //第一步：注册观察者

        // 第2步：获取在线设备列表，可以通过fileObserver处理设备状态变化
        manager.addFileObserver(fileObserver);

        // 第3步：读取当前设备正在读的当前目录
        List<SDCardBean> list = manager.getOnlineDev();
        if (list.size() < 1) {
            //没有在线设备
            return;
        }
        SDCardBean sdCardBean = list.get(0);//获取设备，如果有多个设备，请根据需求获取相应的设备
        Folder currentFolder = manager.getCurrentReadFile(sdCardBean);

        //第4步：获取当前目录下已经读了但在缓存中的子文件
        List<FileStruct> fileStructs = currentFolder.getChildFileStructs();

        //第5步：浏览操作
        //加载更多
        manager.loadMore(sdCardBean);
        //进入下一级目录
        FileStruct fileStruct = currentFolder.getChildFileStructs().get(0);//根据需要获取需要读取的文件夹
        manager.appenBrowse(fileStruct, sdCardBean);
        //返回上一级目录没有列表回调
        boolean hasEvent = true;//是否需要FileObserver的事件回调
        manager.backBrowse(sdCardBean, hasEvent);
        //点播文件
        manager.playFile(fileStruct, sdCardBean);
    }

    @Test
    public void delete() {
        FileBrowseManager manager = FileBrowseManager.getInstance();
        List<SDCardBean> list = manager.getOnlineDev();
        if (list.size() < 1) {
            //没有在线设备
            return;
        }
        SDCardBean sdCardBean = list.get(0);//获取设备，如果有多个设备，请根据需求获取相应的设备
        List<FileStruct> fileStructs = new ArrayList<>();//注意fileStructs一定要是在sdCardBean中
        boolean withEnv = false;//准备环境，一般使用false
        manager.deleteFile(sdCardBean, fileStructs, withEnv, new DeleteCallback() {
            @Override
            public void onSuccess(FileStruct fileStruct) {
                //成功
            }

            @Override
            public void onError(int code, FileStruct fileStruct) {
                //fileStruct 删除失败
            }

            @Override
            public void onFinish() {
                //删除结束，通过onError判断是否有删除失败的文件
            }
        });
    }

    @Test
    public void read() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        List<SDCardBean> list = FileBrowseManager.getInstance().getOnlineDev();
        if (list.size() < 1) {
            //没有在线设备
            return;
        }
        SDCardBean sdCardBean = list.get(0);//获取设备，如果有多个设备，请根据需求获取相应的设备
        FileStruct fileStruct = null;//注意fileStructs一定要是在sdCardBean中
        int devHandle = sdCardBean.getDevHandler();
        int cluster = fileStruct.getCluster();
        int offset = 0;
        String path = "读取内容的保存路径";
        GetFileByClusterTask.Param param = new GetFileByClusterTask.Param(devHandle, 0, cluster, path);
        GetFileByClusterTask task = new GetFileByClusterTask(watchManager, param);
        task.setListener(new TaskListener() {
            @Override
            public void onBegin() {
                //开始
            }

            @Override
            public void onProgress(int progress) {
                //进度回调
            }

            @Override
            public void onFinish() {
                //成功
            }

            @Override
            public void onError(int code, String msg) {
                //失败
            }

            @Override
            public void onCancel(int reason) {
                //取消
            }
        });
        task.start();

    }

    @Test
    public void format(Context context) {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        List<SDCardBean> list = FileBrowseManager.getInstance().getOnlineDev();
        if (list.size() < 1) {
            //没有在线设备
            return;
        }
        SDCardBean sdCardBean = list.get(0);//获取设备，如果有多个设备，请根据需求获取相应的设备
        FormatTask task = new FormatTask(watchManager, context, sdCardBean);
        task.setListener(new SimpleTaskListener() {
            @Override
            public void onBegin() {
                //开始
            }

            @Override
            public void onFinish() {
                //完成
            }

            @Override
            public void onError(int code, String msg) {
                //失败
            }
        });
    }

    @Test
    public void format() {
        FileBrowseManager manager = FileBrowseManager.getInstance();
        List<SDCardBean> list = manager.getOnlineDev();
        if (list.size() < 1) {
            //没有在线设备
            return;
        }
        SDCardBean sdCardBean = list.get(0);//获取设备，如果有多个设备，请根据需求获取相应的设备
        manager.formatDevice(sdCardBean, new OperatCallback() {
            @Override
            public void onSuccess() {
                //成功

            }

            @Override
            public void onError(int code) {
                //失败
            }
        });

    }


    @Test
    public void deleteByName() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        String name = "文件名";
        DeleteFileByNameCmd deleteFileByNameCmd = new DeleteFileByNameCmd(new DeleteFileByNameCmd.Param(name));
        watchManager.sendRcspCommand(watchManager.getTargetDevice(), deleteFileByNameCmd, new BooleanRcspActionCallback("DeleteFileByName",
                new OnOperationCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        //成功
                    }

                    @Override
                    public void onFailed(BaseError error) {
                        //失败
                    }
                }));
    }

    @Test
    public void readByName() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        String name = "文件名";
        String path = "保存路径";
        List<SDCardBean> list = FileBrowseManager.getInstance().getOnlineDev();
        if (list.size() < 1) {
            //没有在线设备
            return;
        }
        SDCardBean sdCardBean = list.get(0);//获取设备，如果有多个设备，请根据需求获取相应的设备
        int devHandle = sdCardBean.getDevHandler();
        boolean unicode = false;//文件名是否支持长文件名（长文件采用unicode编码，短文件名使用8+3结构的ASCII编码）
        GetFileByNameTask.Param param = new GetFileByNameTask.Param(devHandle, name, path, unicode);
        GetFileByNameTask task = new GetFileByNameTask(watchManager, param);
        task.setListener(new TaskListener() {
            @Override
            public void onBegin() {
                //开始
            }

            @Override
            public void onProgress(int progress) {
                //进度回调
            }

            @Override
            public void onFinish() {
                //成功
            }

            @Override
            public void onError(int code, String msg) {
                //失败
            }

            @Override
            public void onCancel(int reason) {
                //取消
            }
        });
        task.start();
    }

    public void syncDeviceStorageState() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //构建获取系统信息命令
        GetSysInfoCmd getSysInfoCmd = (GetSysInfoCmd) CommandBuilder.buildGetSysInfoCmd(AttrAndFunCode.SYS_INFO_FUNCTION_PUBLIC,
                0x01 << AttrAndFunCode.SYS_INFO_ATTR_MUSIC_DEV_STATUS);
        //发送获取系统信息命令
        watchManager.sendRcspCommand(watchManager.getConnectedDevice(), getSysInfoCmd, new RcspCommandCallback<GetSysInfoCmd>() {
            @Override
            public void onCommandResponse(BluetoothDevice device, GetSysInfoCmd cmd) {
                if (cmd.getStatus() == StateCode.STATUS_SUCCESS) {
                    SysInfoResponse sysInfoResponse = cmd.getResponse();
                    if (sysInfoResponse != null && sysInfoResponse.getAttrs() != null) {
                        for (AttrBean attrBean : sysInfoResponse.getAttrs()) {
                            if ((attrBean.getType() & 0xff) == AttrAndFunCode.SYS_INFO_ATTR_MUSIC_DEV_STATUS) {
                                //只处理设备存储器状态信息
                                List<SDCardBean> sdCardBeans = coverAttrBeanDataToSdCardBean(watchManager, device, attrBean.getAttrData());

                                break;
                            }
                        }
                    }
                    return;
                }
                //处理命令操作失败情况
            }

            @Override
            public void onErrCode(BluetoothDevice device, BaseError error) {
                //回调命令操作异常情况
            }
        });
    }

    private List<SDCardBean> coverAttrBeanDataToSdCardBean(@NonNull WatchManager watchManager, @NonNull BluetoothDevice device, byte[] data) {
        final DeviceInfo deviceInfo = watchManager.getDeviceInfo(device);
        if (null == deviceInfo) return new ArrayList<>();
        DevStorageInfo devStorageInfo = new DevStorageInfo();
        devStorageInfo.parseData(data);
        final List<DevStorageState> states = devStorageInfo.getStorageStates();
        List<SDCardBean> list = new ArrayList<>();
        for (DevStorageState state : states) {
            final int index = state.getIndex();
            list.add(new SDCardBean()
                    .setDevice(device)
                    .setIndex(state.getIndex())
                    .setOnline(state.isOnline())
                    .setDevHandler(state.getHandle())
                    .setName(FileBrowseUtil.getDevName(index))
                    .setType(SDCardBean.getStorageType(index)));
        }

        //根据设备的支持情况显示设备
        List<SDCardBean> result = new ArrayList<>();
        for (SDCardBean sdCardBean : list) {
            if (sdCardBean.getIndex() == SDCardBean.INDEX_USB && deviceInfo.isSupportUsb()) {
                result.add(sdCardBean);
            } else if (sdCardBean.getIndex() == SDCardBean.INDEX_SD0 && deviceInfo.isSupportSd0()) {
                result.add(sdCardBean);
            } else if (sdCardBean.getIndex() == SDCardBean.INDEX_SD1 && deviceInfo.isSupportSd1()) {
                result.add(sdCardBean);
            } else {
                result.add(sdCardBean);
            }
        }
        return result;
    }
}
