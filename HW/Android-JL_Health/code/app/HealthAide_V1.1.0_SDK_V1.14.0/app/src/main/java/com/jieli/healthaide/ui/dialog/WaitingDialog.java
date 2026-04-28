package com.jieli.healthaide.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.base.BaseDialogFragment;

/**
 * 等待对话框
 */
public class WaitingDialog extends BaseDialogFragment {

    private final boolean isCancelable;

    public WaitingDialog() {
        this(false);
    }

    public WaitingDialog(boolean isCancelable) {
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
        return inflater.inflate(R.layout.dialog_waiting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (requireDialog().getWindow() == null) return;
        requireDialog().getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.bg_waiting_dialog, requireActivity().getTheme()));
    }
}