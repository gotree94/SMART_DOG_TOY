package com.jieli.healthaide.ui.device.nfc;

import android.bluetooth.BluetoothDevice;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.ui.device.nfc.bean.NfcStatus;
import com.jieli.healthaide.ui.device.watch.WatchViewModel;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.jl_filebrowse.FileBrowseManager;
import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.jl_rcsp.impl.NfcOpImpl;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.interfaces.nfc.OnNfcEventCallback;
import com.jieli.jl_rcsp.interfaces.nfc.OnSyncNfcMsgListener;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.NfcFile;
import com.jieli.jl_rcsp.model.device.NfcMsg;
import com.jieli.jl_rcsp.model.parameter.ModifyNfcMsgParam;
import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.jl_rcsp.util.CryptoUtil;
import com.jieli.jl_rcsp.util.JL_Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ClassName: NFCViewModel
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/7/14 19:59
 */
public class NFCViewModel extends WatchViewModel {
    private final static String TAG = "zzc_nfc";
    private final NfcOpImpl mNfcOp;
    public static final int STATUS_IDLE = 0;
    public static final int STATUS_WORKING = 1;
    public static final int STATUS_SUCCESS = 2;
    public static final int STATUS_FAIL = 3;
    public static final int ERROR_CODE_CARD_NO_ONLINE_DEVICE = 97;
    public static final int ERROR_CODE_CARD_FULL = 98;
    public static final int ERROR_CODE_CARD_EXISTED = 99;
    public final MutableLiveData<List<NfcMsg>> mNfcMsgMLD = new MutableLiveData<>();
    public final MutableLiveData<Short> mDefaultIdMLD = new MutableLiveData<>();
    public final MutableLiveData<Integer> mSyncNfcMsgStatusMLD = new MutableLiveData<>();
    public final MutableLiveData<Integer> mControlNfcMsgStatusMLD = new MutableLiveData<>();
    public final MutableLiveData<NfcStatus> mAddNfcFileStatusMLD = new MutableLiveData<>();
    private final boolean SUPPORT_OVERWRITE_NFC_FILE = false;//是否支持nfc文件覆盖
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mUIHandler = new Handler(Looper.getMainLooper());

    private NfcMsg mOperationNfcMsg;
    private final NFCDataHandler mNFCDataHandler = NFCDataHandler.getInstance();
    private final static byte[] nfc_id = new byte[]{
            (byte) 0x4A, (byte) 0x4C, (byte) 0x05, (byte) 0xFD, (byte) 0x66
    };

    private final static byte[] nfc_data = new byte[]{
            (byte) 0x63, (byte) 0x6F, (byte) 0x6D, (byte) 0x2E, (byte) 0x61,
            (byte) 0x6C, (byte) 0x69, (byte) 0x62, (byte) 0x61, (byte) 0x62,
            (byte) 0x61, (byte) 0x2E, (byte) 0x61, (byte) 0x6E, (byte) 0x64,
            (byte) 0x72, (byte) 0x6F, (byte) 0x69, (byte) 0x64, (byte) 0x2E,
            (byte) 0x72, (byte) 0x69, (byte) 0x6D, (byte) 0x65, (byte) 0x74,
            (byte) 0x63, (byte) 0x6F, (byte) 0x6D, (byte) 0x2E, (byte) 0x61,
            (byte) 0x6C, (byte) 0x69, (byte) 0x62, (byte) 0x61, (byte) 0x62,
            (byte) 0x61, (byte) 0x2E, (byte) 0x61, (byte) 0x6E, (byte) 0x64,
            (byte) 0x72, (byte) 0x6F, (byte) 0x69, (byte) 0x64, (byte) 0x2E,
            (byte) 0x72, (byte) 0x69, (byte) 0x6D, (byte) 0x65, (byte) 0x74,
            (byte) 0x63, (byte) 0x6F, (byte) 0x6D, (byte) 0x2E, (byte) 0x61,
            (byte) 0x6C, (byte) 0x69, (byte) 0x62, (byte) 0x61, (byte) 0x62,
            (byte) 0x61, (byte) 0x2E, (byte) 0x61, (byte) 0x6E, (byte) 0x64,
            (byte) 0x72, (byte) 0x6F, (byte) 0x69, (byte) 0x64, (byte) 0x2E,
            (byte) 0x72, (byte) 0x69, (byte) 0x6D, (byte) 0x65, (byte) 0x74
    };

    private final static String[] test_name = new String[]{
            "我的家", "公司大门", "宿舍门禁", "公交卡", "银行卡", "饭卡", "滴滴卡", "好人卡", "我要报警啦", "老人卡",
    };
    private boolean getTagOneTime = false;//接收一个tag

    public NFCViewModel() {
        mNfcOp = new NfcOpImpl(mWatchManager);
        mNfcOp.addOnNfcEventCallback(mOnNfcEventCallback);
        mAddNfcFileStatusMLD.observeForever(nfcStatus -> {
            if (nfcStatus.getStatus() != NfcStatus.NFC_STATUS_IDLE) {
                NfcStatus nfcStatusIDLE = new NfcStatus();
                nfcStatusIDLE.setStatus(NfcStatus.NFC_STATUS_IDLE);
                mAddNfcFileStatusMLD.postValue(nfcStatusIDLE);
            }
        });
        mControlNfcMsgStatusMLD.observeForever(status -> {
            if (status != NFCViewModel.STATUS_IDLE) {
                mControlNfcMsgStatusMLD.postValue(NFCViewModel.STATUS_IDLE);
            }
        });
    }

    public void release() {
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
        mUIHandler.removeCallbacksAndMessages(null);
        mNfcOp.removeOnNfcEventCallback(mOnNfcEventCallback);
        mNFCDataHandler.release();
    }

    public void syncNfcMessage() {
        if (getOnlineDevice() == null) return;
        final int devHandler = getOnlineDevice().getDevHandler();
        mNfcOp.syncNfcMsg(getConnectedDevice(), devHandler, new OnSyncNfcMsgListener() {
            @Override
            public void onStart() {
                mSyncNfcMsgStatusMLD.postValue(1); //开始
            }

            @Override
            public void onFinish(List<NfcMsg> list) {
                for (NfcMsg msg : list) {
                    JL_Log.d(TAG, "syncNfcMessage", "" + msg);
                }
                mSyncNfcMsgStatusMLD.postValue(0); //结束
                JL_Log.d(TAG, "syncNfcMessage", "SyncNfcMsgStatusMLD");
                mNfcMsgMLD.postValue(list);
                JL_Log.d(TAG, "syncNfcMessage", "NfcMsgMLD");
                mNfcOp.getDefaultNfc(getConnectedDevice(), devHandler, new OnOperationCallback<Short>() {
                    @Override
                    public void onSuccess(Short result) {
                        JL_Log.i(TAG, "syncNfcMessage", "getDefaultNfc ---> " + result);
                    }

                    @Override
                    public void onFailed(BaseError error) {
                        JL_Log.e(TAG, "syncNfcMessage", "onFailed ---> " + error);
                    }
                });
            }

            @Override
            public void onError(BaseError error) {
                JL_Log.e(TAG, "syncNfcMessage", "onError ---> " + error);
                mSyncNfcMsgStatusMLD.postValue(0);
            }
        });
    }

    public void initNFCReadState() {
        mNFCDataHandler.initNFCReadState();
    }

    public LiveData<Integer> getNFCReadState() {
        return mNFCDataHandler.stateLiveData;
    }

    public void addNfcFile() {
       /* if (getOnlineDevice() == null) {
//            ToastUtil.showToastShort("没有在线设备");
            NfcStatus status = new NfcStatus();
            status.setStatus(NfcStatus.NFC_STATUS_STOP);
            status.setResult(NfcStatus.RESULT_FAILURE);
            status.setCode(ERROR_CODE_CARD_NO_ONLINE_DEVICE);//已存在
            mAddNfcFileStatusMLD.setValue(status);
            return;
        }*/
        List<NfcMsg> msgList = mNfcOp.getNfcMsgList();
        byte[] idBytes = mNFCDataHandler.getIdByte();
        String idHexStr = ValueUtil.byte2HexStr(idBytes);

        NfcMsg[] msgArray = new NfcMsg[]{};
        if (msgList != null) {
            msgArray = msgList.toArray(msgArray);
        }
        Arrays.sort(msgArray, (nfcMsg1, nfcMsg2) -> (int) (nfcMsg1.getUpdateTime() - nfcMsg2.getUpdateTime()));
        List<NfcMsg> sortNfcMsgList = Arrays.asList(msgArray);
        short id = getAvailableNfcId(sortNfcMsgList);
        byte[] nfcData = mNFCDataHandler.getCardByteData();
        boolean isReadDataEmpty = nfcData == null || nfcData.length == 0;
        boolean isIdEmpty = idBytes == null || idBytes.length == 0;
        if (!isReadDataEmpty && !isIdEmpty) {
            final NfcFile nfcFile = HealthConstant.TEST_DEVICE_FUNCTION ? createNfcFile(id, getOnlineDevice().getDevHandler(), test_name[id], nfc_id, nfc_data)
                    : createNfcFile(id, /*getOnlineDevice().getDevHandler()*/1, "我的家", idBytes, nfcData);//todo 如果要实现crc校验 覆盖nfc文件。把id check
            boolean isOverwrite = false;
            if (msgList != null && !HealthConstant.TEST_NFC_FUNCTION) {
                for (NfcMsg nfcMsg : msgList) {
                    String msgNfcIDHexStr = ValueUtil.byte2HexStr(nfcMsg.getNfcID());
                    boolean isSampleId = TextUtils.equals(idHexStr, msgNfcIDHexStr);
                    boolean isSampleCrc16 = nfcFile.getMessage().getCrc16() == nfcMsg.getCrc16();
                    if (isSampleId) {
                        isOverwrite = isSampleCrc16 && SUPPORT_OVERWRITE_NFC_FILE;
                        if (!isOverwrite) {
                            NfcStatus status = new NfcStatus();
                            status.setStatus(NfcStatus.NFC_STATUS_STOP);
                            status.setResult(NfcStatus.RESULT_FAILURE);
                            status.setCode(ERROR_CODE_CARD_EXISTED);//已存在
                            mAddNfcFileStatusMLD.setValue(status);
                            return;
                        }
                    }
                }
            }

            if (!isOverwrite && msgList != null && msgList.size() >= 10) {
//            ToastUtil.showToastShort("NFC卡已满");
                NfcStatus status = new NfcStatus();
                status.setStatus(NfcStatus.NFC_STATUS_STOP);
                status.setResult(NfcStatus.RESULT_FAILURE);
                status.setCode(ERROR_CODE_CARD_FULL);//已存在
                mAddNfcFileStatusMLD.setValue(status);
                return;
            }
            mOperationNfcMsg = nfcFile.getMessage();
            executor.submit(() -> {
                byte[] data = nfcFile.convertRawData();
//                byte[] data = nfcFile.getNfcData();
                JL_Log.d(TAG, "addNfcFile", "E1 data : " + data[19] + " str :" + CHexConver.byteToHexString(data[19]) + " last : ");
                File file = new File(HealthApplication.getAppViewModel().getApplication().getExternalCacheDir().getParent() + "/" + nfcFile.getMessage().getNfcFileName());
                if (file.exists()) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] data2 = new byte[1074];
                        fileInputStream.read(data2);
                        fileInputStream.close();
                        JL_Log.d(TAG, "addNfcFile", "E1 data2 : " + data2[19] + " str :" + CHexConver.byte2HexStr(data2) + " last : ");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
               /* if (!file.exists()) {
                    try {
                        boolean ret = file.createNewFile();
                        if (!ret) {
                            JL_Log.e(TAG, "创建文件失败，请检查是否有读写文件权限");
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    FileOutputStream outputStream = new FileOutputStream(file);
                    outputStream.write(data);
                    outputStream.close();
                    JL_Log.d(TAG, "addNfcFile success");
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
               /* mUIHandler.post(() -> mNfcOp.addNfcFile(getConnectedDevice(), file.getPath(), nfcFile.getMessage().getDevHandler(), new TaskListener() {
                    @Override
                    public void onBegin() {
                        NfcStatus status = new NfcStatus();
                        status.setStatus(NfcStatus.NFC_STATUS_START);
                        mAddNfcFileStatusMLD.setValue(status);
                    }

                    @Override
                    public void onProgress(int progress) {
                        NfcStatus status = new NfcStatus();
                        status.setStatus(NfcStatus.NFC_STATUS_WORKING);
                        status.setProgress(progress);
                        mAddNfcFileStatusMLD.setValue(status);
                    }

                    @Override
                    public void onFinish() {
                        JL_Log.d(TAG, "onFinish");
                        NfcStatus status = new NfcStatus();
                        status.setStatus(NfcStatus.NFC_STATUS_STOP);
                        status.setResult(NfcStatus.RESULT_OK);
                        mAddNfcFileStatusMLD.setValue(status);
                    }

                    @Override
                    public void onError(int code, String msg) {
                        JL_Log.d(TAG, "onError" + msg);
                        NfcStatus status = new NfcStatus();
                        status.setStatus(NfcStatus.NFC_STATUS_STOP);
                        status.setResult(NfcStatus.RESULT_FAILURE);
                        status.setCode(code);
                        status.setMessage(msg);
                        mAddNfcFileStatusMLD.setValue(status);
                    }

                    @Override
                    public void onCancel(int reason) {
                        NfcStatus status = new NfcStatus();
                        status.setStatus(NfcStatus.NFC_STATUS_STOP);
                        status.setResult(NfcStatus.RESULT_CANCEL);
                        status.setCode(reason);
                        mAddNfcFileStatusMLD.setValue(status);
                    }
                }));*/
            });
        }
    }

    public void removeNfcMsg(int devHandler, final short id) {
        mControlNfcMsgStatusMLD.postValue(STATUS_WORKING);
        mNfcOp.removeNfcMsg(getConnectedDevice(), devHandler, id, new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                JL_Log.d(TAG, "removeNfcMsg", CalendarUtil.formatString("删除卡片[%d]成功", id));
                mControlNfcMsgStatusMLD.postValue(STATUS_SUCCESS);
            }

            @Override
            public void onFailed(BaseError error) {
                JL_Log.e(TAG, "removeNfcMsg", "onFailed ---> " + error);
                mControlNfcMsgStatusMLD.postValue(STATUS_FAIL);
            }
        });
    }

    public void setDefaultNfcID(final short id) {
        if (getOnlineDevice() == null) return;
        final int devHandler = getOnlineDevice().getDevHandler();
        mNfcOp.setDefaultNfc(getConnectedDevice(), devHandler, id, new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                JL_Log.d(TAG, "setDefaultNfcID", CalendarUtil.formatString("设置默认卡片[%d]成功", id));
            }

            @Override
            public void onFailed(BaseError error) {
                JL_Log.e(TAG, "setDefaultNfcID", "onFailed ---> " + error);
            }
        });
    }

    public void modifyNfcMsg(final short id, long time, final String name) {
        final NfcMsg nfcMsg = mNfcOp.getNfcMsgByID(id);
        if (nfcMsg == null) return;
        mControlNfcMsgStatusMLD.postValue(STATUS_WORKING);
        mNfcOp.modifyNfcMsg(getConnectedDevice(), new ModifyNfcMsgParam(nfcMsg.getDevHandler(), id, time, name),
                new OnOperationCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        JL_Log.d(TAG, "modifyNfcMsg", CalendarUtil.formatString("修改卡片[%d]内容成功", id));
                        mControlNfcMsgStatusMLD.postValue(STATUS_SUCCESS);
                    }

                    @Override
                    public void onFailed(BaseError error) {
                        JL_Log.e(TAG, "modifyNfcMsg", "onFailed ---> " + error);
                        mControlNfcMsgStatusMLD.postValue(STATUS_FAIL);
                    }
                });
    }

    public NfcMsg getNfcMsgById(short id) {
        NfcMsg nfcMsg = null;
        List<NfcMsg> list = mNfcMsgMLD.getValue();
        if (null == list) return null;
        for (NfcMsg tempMsg : list) {
            if (tempMsg.getId() == id) {
                nfcMsg = tempMsg;
                break;
            }
        }
        return nfcMsg;
    }

    /**
     * 准备接收处理一个tag，后续tag过滤
     */
    public void prepareGetTag() {
        getTagOneTime = true;
    }

    public void getTagData(Tag tag) {
        if (getTagOneTime) {
            getTagOneTime = false;
            mNFCDataHandler.getTagData(tag);
        }
    }

    public void cancelGetTagData() {
        mNFCDataHandler.cancelGetTagData();
    }

    /**
     * 设置当前操作的NfcMsg
     *
     * @param id NfcMsg的id
     */
    public void setOperationNfcMsg(short id) {
        this.mOperationNfcMsg = getNfcMsgById(id);
    }

    public NfcMsg getOperationNfcMsg() {
        return this.mOperationNfcMsg;
    }

    /**
     * 获取可用的id
     */
    private short getAvailableNfcId(List<NfcMsg> msgList) {
        if (msgList == null || msgList.isEmpty()) return (short) 0;
        short id = 0;
        for (int i = 0; i < 10; i++) {
            boolean ret = false;
            for (NfcMsg nfcMsg : msgList) {
                if (nfcMsg.getId() == id) {
                    ret = true;
                    break;
                }
            }
            if (!ret) {
                break;
            }
            id++;
        }
        return id;
    }

    private NfcFile createNfcFile(short id, int devHandler, String name, byte[] nfcId,
                                  byte[] nfcData) {
        NfcMsg nfcMsg = new NfcMsg()
                .setId(id)
                .setDevHandler(devHandler)
                .setUpdateTime(Calendar.getInstance().getTimeInMillis())
                .setNfcFileName(getIdString(id) + ".nfc")
                .setNickname(name)
                .setCrc16(CryptoUtil.CRC16(nfcData, (short) 0))
                .setNfcID(nfcId);
        return new NfcFile(nfcMsg, nfcData);
    }

    private String getIdString(short id) {
        if (id < 10) {
            return "0" + id;
        } else {
            return String.valueOf(id);
        }
    }

    private SDCardBean getOnlineDevice() {
        List<SDCardBean> list = FileBrowseManager.getInstance().getOnlineDev();
        if (list == null || list.isEmpty())
            return null;
        for (SDCardBean sdCardBean : list) {
            if (sdCardBean.getType() == SDCardBean.SD) {
                return sdCardBean;
            }
        }
        return list.get(0);
    }

    private final OnNfcEventCallback mOnNfcEventCallback = new OnNfcEventCallback() {
        @Override
        public void onNfcMsgChange(BluetoothDevice device, List<NfcMsg> nfcMsgs) {
            JL_Log.e(TAG, "onNfcMsgChange", "device =  " + device + ", list = " + nfcMsgs.size());
            mNfcMsgMLD.postValue(nfcMsgs);
        }

        @Override
        public void onModifyNfcMsg(BluetoothDevice device, NfcMsg nfcMsg) {
            JL_Log.i(TAG, "onModifyNfcMsg", "nfcMsg = " + nfcMsg);
        }

        @Override
        public void onDefaultNfc(BluetoothDevice device, short id) {
            JL_Log.i(TAG, "onDefaultNfc", "device = " + device + ", id = " + id);
            mDefaultIdMLD.postValue(id);
        }

        @Override
        public void onRequestSynNfcMsg(BluetoothDevice device) {
            syncNfcMessage();
        }
    };
}