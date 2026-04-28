package com.jieli.healthaide.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jieli.healthaide.R;

public class ModifyNicknameFragment extends Fragment {

    //public final static String FRAGMENT_NICKNAME = "fragment_nickname";

    public static ModifyNicknameFragment newInstance() {
        return new ModifyNicknameFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_modify_nickname, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //ModifyNicknameViewModel mViewModel = new ViewModelProvider(this).get(ModifyNicknameViewModel.class);
        // TODO: Use the ViewModel
    }

}