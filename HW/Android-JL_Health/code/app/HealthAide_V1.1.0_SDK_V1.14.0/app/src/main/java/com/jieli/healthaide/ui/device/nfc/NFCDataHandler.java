package com.jieli.healthaide.ui.device.nfc;

import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.lifecycle.MutableLiveData;

import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.jl_rcsp.util.JL_Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName: NFCDataHandler
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/7/14 19:56
 */
public class NFCDataHandler {
    public MutableLiveData<Integer> stateLiveData = new MutableLiveData<>();
    public static final int STATE_NFC_READ_IDLE = 0x80;
    public static final int STATE_NFC_READ_PREPARE = 0x81;
    public static final int STATE_NFC_READING = 0x82;
    public static final int STATE_NFC_READ_FAIL_MOVE_FAST = 0x83;
    public static final int STATE_NFC_READ_FAIL_ENCRYPTION = 0x84;
    public static final int STATE_NFC_READ_FAIL_TIME_OUT = 0x85;
    public static final int STATE_NFC_READ_CANCEL = 0x86;
    public static final int STATE_NFC_READ_SUCCESS = 0x87;
    private final String TAG = this.getClass().getSimpleName();
    private final HandlerThread handlerThread = new HandlerThread("HealthDataHandler");
    private final Handler workHandler;
    private int READ_FAILED_TIME_OUT = 10 * 1000;
    private final int MSG_READ_FAILED = 1;
    private boolean isTagLost = false;
    private boolean isCancelGetTagData = false;
    private byte[] idByte;
    private byte[] cardByteData;

    public static NFCDataHandler getInstance() {
        NFCDataHandler instance = new NFCDataHandler();
        return instance;
    }

    public NFCDataHandler() {
        handlerThread.start();
        workHandler = new Handler(handlerThread.getLooper(), msg -> {
            if (msg.obj instanceof Runnable) {
                Runnable r = (Runnable) msg.obj;
                r.run();
            }
            switch (msg.what) {
                case MSG_READ_FAILED://获取超时失败
                    stateLiveData.postValue(STATE_NFC_READ_FAIL_TIME_OUT);
                    break;
            }
            return true;
        });
        stateLiveData.observeForever(state -> {
            switch (state) {
                case STATE_NFC_READ_FAIL_MOVE_FAST:
                case STATE_NFC_READ_FAIL_ENCRYPTION:
                case STATE_NFC_READ_SUCCESS:
                    workHandler.removeMessages(MSG_READ_FAILED);
                    break;
            }
            if (state != STATE_NFC_READ_IDLE) {
                stateLiveData.postValue(STATE_NFC_READ_IDLE);
            }
        });
    }

    public void initNFCReadState() {
        stateLiveData.postValue(STATE_NFC_READ_PREPARE);
    }

    public void release() {
        workHandler.removeCallbacksAndMessages(null);
        handlerThread.quitSafely();
    }

    public void getTagData(final Tag tag) {
        this.idByte = new byte[0];
        this.cardByteData = new byte[0];
        isTagLost = false;
        isCancelGetTagData = false;
        stateLiveData.postValue(STATE_NFC_READING);
        workHandler.removeMessages(MSG_READ_FAILED);
        workHandler.sendEmptyMessageDelayed(MSG_READ_FAILED, READ_FAILED_TIME_OUT);
        workHandler.post(() -> {//todo 要兼容只读 模拟卡id的
            List<String> techList = Arrays.asList(tag.getTechList());
            byte[] idBytes = tag.getId();
            JL_Log.d(TAG, "getTagData", "data : " + Arrays.toString(tag.getTechList()) + " id :" + CHexConver.byte2HexStr(idBytes));
            byte[] readData = new byte[0];
          /*  if (techList.contains(android.nfc.tech.MifareClassic.class.getName())) {
                readData = readMifareClassicData(tag);
            } else if (techList.contains(android.nfc.tech.MifareUltralight.class.getName())) {
                readData = readMifareUltralightData(tag);
            } else if (techList.contains(android.nfc.tech.NdefFormatable.class.getName())) {
            } else {*/
            NfcA nfcA = NfcA.get(tag);
            JL_Log.d(TAG, "getTagData", "Atqa : " + CHexConver.byte2HexStr(nfcA.getAtqa()));
            JL_Log.d(TAG, "getTagData", "getMaxTransceiveLength : " + nfcA.getMaxTransceiveLength());
            JL_Log.d(TAG, "getTagData", "sak : " + nfcA.getSak());
            int firstBlockNum = 0;
            int lastBlockNum = 42;
            try {
                nfcA.connect();
                byte[] result = nfcA.transceive(new byte[]{
                        (byte) 0x3A,  // FAST_READ
                        (byte) (firstBlockNum & 0x0ff),
                        (byte) (lastBlockNum & 0x0ff),
                });
                JL_Log.d(TAG, "getTagData", "data : " + CHexConver.byte2HexStr(result));
            } catch (IOException e) {
                e.printStackTrace();
            }

            nfcA.getAtqa();
            JL_Log.d(TAG, "getTagData", "tag class is not support read");
//            }
            boolean isReadDataEmpty = readData == null || readData.length == 0;
            boolean isIdEmpty = idBytes == null || idBytes.length == 0;
            if (!isReadDataEmpty && !isIdEmpty && !isCancelGetTagData) {
                this.idByte = idBytes;
                this.cardByteData = readData;
                stateLiveData.postValue(STATE_NFC_READ_SUCCESS);
                JL_Log.d(TAG, "getTagData", CHexConver.str2HexStr(Arrays.toString(readData)));
                JL_Log.d(TAG, "getTagData", "readData success len" + readData.length + " idData len : " + idBytes.length);
            }
        });
    }

    public void cancelGetTagData() {
        if (workHandler.hasMessages(MSG_READ_FAILED)) {
            JL_Log.d(TAG, "cancelGetTagData", "");
            isCancelGetTagData = true;
            workHandler.removeMessages(MSG_READ_FAILED);
            stateLiveData.postValue(STATE_NFC_READ_CANCEL);
        }
    }

    public byte[] getCardByteData() {
        return cardByteData;
    }

    public byte[] getIdByte() {
        return idByte;
    }

    private byte[] readMifareClassicData(Tag tag) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        MifareClassic classic = MifareClassic.get(tag);
        int ttype = classic.getType();
        JL_Log.i(TAG, "readMifareClassicData", "MifareClassic tag type: " + ttype);
        int tsize = classic.getSize();
        JL_Log.i(TAG, "readMifareClassicData", "tag size: " + tsize);
        int s_len = classic.getSectorCount();
        JL_Log.i(TAG, "readMifareClassicData", "tag sector count: " + s_len);
        int b_len = classic.getBlockCount();
        JL_Log.d(TAG, "readMifareClassicData", "tag block count: " + b_len);
        try {
            classic.connect();
            boolean hasEncryption = false;
            for (int i = 0; i < s_len; i++) {
                boolean isAuthenticated = authSectorA(classic, i);
                if (!isAuthenticated) {
                    isAuthenticated = authSectorB(classic, i);
                }
                if (isTagLost) {
                    stateLiveData.postValue(STATE_NFC_READ_FAIL_MOVE_FAST);
                    return new byte[0];
                }
                if (isCancelGetTagData) {
                    return new byte[0];
                }
                int block_index = classic.sectorToBlock(i);
                int bCountSector = classic.getBlockCountInSector(i);
                JL_Log.w(TAG, "readMifareClassicData", "block_index = " + block_index);
                if (isAuthenticated) {
                    for (int j = 0; j < bCountSector; j++) {
                        byte[] block = classic.readBlock(block_index);
                        buf.write(block);
                        block_index++;
                        JL_Log.w(TAG, "readMifareClassicData", "data : " + CHexConver.byte2HexStr(block));
                    }
                } else {//认证失败
                    hasEncryption = true;
                    byte[] block = new byte[16 * bCountSector];
                    Arrays.fill(block, (byte) 0xff);
                    buf.write(block);
                }
            }
            if (hasEncryption) {
                stateLiveData.postValue(STATE_NFC_READ_FAIL_ENCRYPTION);
                return new byte[0];
            }
        } catch (IOException e) {
            e.printStackTrace();
            JL_Log.e(TAG, "readMifareClassicData", "error : " + e.getMessage());
        } finally {
            try {
                classic.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return buf.toByteArray();
    }

    private boolean authSectorA(MifareClassic classic, int i) {
        boolean ret = false;
        try {
            if (classic.authenticateSectorWithKeyA(i, MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY)) {
                ret = true;
            } else if (classic.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT)) {
                ret = true;
            } else if (classic.authenticateSectorWithKeyA(i, MifareClassic.KEY_NFC_FORUM)) {
                ret = true;
            } else {
                JL_Log.w(TAG, "authSectorA", "Authorization denied ");
            }

        } catch (IOException e) {
            if (e instanceof TagLostException) {
                isTagLost = true;
            }
        }
        return ret;
    }

    private boolean authSectorB(MifareClassic classic, int i) {
        boolean ret = false;
        try {
            if (classic.authenticateSectorWithKeyB(i, MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY)) {
                ret = true;
            } else if (classic.authenticateSectorWithKeyB(i, MifareClassic.KEY_DEFAULT)) {
                ret = true;
            } else if (classic.authenticateSectorWithKeyB(i, MifareClassic.KEY_NFC_FORUM)) {
                ret = true;
            } else {
                JL_Log.w(TAG, "authSectorB", "Authorization denied ");
            }
        } catch (IOException e) {
            if (e instanceof TagLostException) {
                isTagLost = true;
            }
        }
        return ret;
    }

    private byte[] readMifareUltralightData(Tag tag) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        MifareUltralight ultralight = MifareUltralight.get(tag);
        try {
            ultralight.connect();
            for (int i = 0; i < 256; i++) {
                if (isCancelGetTagData) {
                    return new byte[0];
                }
                byte[] data = ultralight.readPages(i);
                buf.write(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ultralight.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        JL_Log.e(TAG, "readMifareUltralightData", "buf = " + buf.size());
        return buf.toByteArray();
    }

}
