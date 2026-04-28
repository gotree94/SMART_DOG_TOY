package com.jieli.healthaide.ui.login;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;

import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.base.DoubleClickBackExitActivity;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/22/21 5:13 PM
 * @desc : 登录页面
 */
public class LoginActivity extends DoubleClickBackExitActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        setWindowStatus(R.id.launcher_container);

        replaceFragment(R.id.launcher_container, LoginByPasswordFragment.class.getCanonicalName());
    }
}
