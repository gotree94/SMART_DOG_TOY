package com.jieli.watchtesttool.ui.widget.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.jieli.watchtesttool.R;


/**
 * 等待框
 */
public class WaitingDialog extends DialogFragment {


    private boolean isShow;

    public WaitingDialog() {
        // Required empty public constructor
    }

    public static WaitingDialog newInstance(String param1, String param2) {
        WaitingDialog fragment = new WaitingDialog();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireDialog().getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.bg_waiting_dialog, requireActivity().getTheme()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_waiting, container, false);
    }

    public boolean isShow() {
        return isShow;
    }

    public void show(FragmentManager manager, String tag) {
        isShow = true;
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        isShow = false;
        super.onDismiss(dialog);
    }
}