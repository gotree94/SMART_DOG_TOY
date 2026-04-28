package com.jieli.healthaide.ui.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.healthaide.databinding.DialogTransferWaitingBinding;
import com.jieli.healthaide.ui.base.BaseDialogFragment;

/**
 * @ClassName: TransferWaitingDialog
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/11/1 13:59
 */
public class TransferWaitingDialog extends BaseDialogFragment {
    private final boolean isCancelable;
    private DialogTransferWaitingBinding mBinding;
    public TransferWaitingDialog() {
        this(false);
    }

    public TransferWaitingDialog(boolean isCancelable) {
        this.isCancelable = isCancelable;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(isCancelable);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DialogTransferWaitingBinding.inflate(inflater,container,false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (requireDialog().getWindow() == null) return;
        requireDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
    public void updateText(String text){
        mBinding.tvContent.setText(text);
    }
}
