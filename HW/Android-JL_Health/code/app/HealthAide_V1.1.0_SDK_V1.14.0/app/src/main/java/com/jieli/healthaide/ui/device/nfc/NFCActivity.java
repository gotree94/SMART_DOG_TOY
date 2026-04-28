package com.jieli.healthaide.ui.device.nfc;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.base.BaseActivity;

public class NFCActivity extends BaseActivity {
    private final String TAG = this.getClass().getSimpleName();
    private NFCViewModel mViewModel;
    private PendingIntent pendingIntent;
    private NfcAdapter nfcAdapter;
    //    private NFCDataHandler nfcDataHandler;
    private boolean mIsOpenNfcRead = false;
    private boolean mIsBanOnBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nfc);
        setWindowStatus();
        mViewModel = new ViewModelProvider(this).get(NFCViewModel.class);
        int flags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), flags);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        checkNFC(nfcAdapter);
//        nfcDataHandler = NFCDataHandler.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsOpenNfcRead && null != nfcAdapter) {
            IntentFilter ndef;
            IntentFilter tech;
            IntentFilter tag;
            try {
                ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED, "*/*"); /* Handles all MIME based dispatches.You should specify only the ones that you need. */
                tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED); /* Handles all MIME based dispatches.You should specify only the ones that you need. */
                tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED); /* Handles all MIME based dispatches.You should specify only the ones that you need. */
            } catch (IntentFilter.MalformedMimeTypeException e) {
                throw new RuntimeException("fail", e);
            }
            IntentFilter[] intentFiltersArray = new IntentFilter[]{ndef, tech, tag};
//            String[][] techLists = new String[][]{new String[]{NfcF.class.getName()}};
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray /*intentFiltersArray*/, null/*techLists*/);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //恢复默认状态
        if (mIsOpenNfcRead && nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewModel.release();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            //todo 如果已经在创建卡的过程，应该直接拦截
            mViewModel.getTagData(tag);
        }
    }

    @Override
    public void onBackPressed() {
        if (!mIsBanOnBackPressed) {
            super.onBackPressed();
        }
    }

    public void setIsBanOnBackPressed(boolean mIsBanOnBackPressed) {
        this.mIsBanOnBackPressed = mIsBanOnBackPressed;
    }

    public void openNfcRead() {
        if (!mIsOpenNfcRead && null != nfcAdapter) {
            mIsOpenNfcRead = true;
            IntentFilter ndef;
            IntentFilter tech;
            IntentFilter tag;
            try {
                ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED, "*/*"); /* Handles all MIME based dispatches.You should specify only the ones that you need. */
                tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED); /* Handles all MIME based dispatches.You should specify only the ones that you need. */
                tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED); /* Handles all MIME based dispatches.You should specify only the ones that you need. */
            } catch (IntentFilter.MalformedMimeTypeException e) {
                throw new RuntimeException("fail", e);
            }
            IntentFilter[] intentFiltersArray = new IntentFilter[]{ndef, tech, tag};
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray /*intentFiltersArray*/, null/*techLists*/);
        }
    }

    public void closeNfcRead() {
        if (mIsOpenNfcRead && nfcAdapter != null) {
            mIsOpenNfcRead = false;
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    private void checkNFC(NfcAdapter nfcAdapter) {
        if (nfcAdapter == null) {//判断设备是否支持NFC功能
            Toast.makeText(this, "设备不支持NFC功能!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!nfcAdapter.isEnabled()) {//判断设备NFC功能是否打开
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("应用需要启用NFC功能，才能正常使用，请打开NFC功能")
                    .setCancelable(false)
                    .setNegativeButton("退出", (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                    })
                    .setPositiveButton("好的", (dialog, which) -> {
                        dialog.dismiss();
                        startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                    })
                    .create().show();
        }
    }

}