package com.jieli.healthaide.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.amap.api.maps.MapsInitializer;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.base.BaseActivity;
import com.jieli.healthaide.ui.login.LoginActivity;
import com.jieli.healthaide.ui.mine.about.WebFragment;
import com.jieli.healthaide.ui.widget.UserServiceDialog;

/**
 * LauncherActivity
 *
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 启动页面
 * @since 2025/10/24
 */
public final class LauncherActivity extends BaseActivity implements UserServiceDialog.OnUserServiceListener {

    private LauncherViewModel mViewModel;
    private final Handler mUIHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_launcher);
        setWindowStatus();

        mViewModel = new ViewModelProvider(this).get(LauncherViewModel.class);
        mViewModel.tokenStateLiveData.observe(this, effective -> {
            final Intent intent = new Intent(LauncherActivity.this, effective ? HomeActivity.class : LoginActivity.class);
            mUIHandler.postDelayed(() -> {
                startActivity(intent);
                finish();
            }, 1000);
        });
        checkAppStatus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        checkAppStatus();
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUIHandler.removeCallbacksAndMessages(null);
    }


    @Override
    public void onUserService() {
        WebFragment.start(this, getString(R.string.user_agreement), getString(R.string.user_agreement_url));
    }

    @Override
    public void onPrivacyPolicy() {
        WebFragment.start(this, getString(R.string.privacy_policy), getString(R.string.app_privacy_policy));
    }

    @Override
    public void onExit(DialogFragment dialogFragment) {
        finish();
    }

    @Override
    public void onAgree(DialogFragment dialogFragment) {
        mViewModel.setAgreePolicy(true);
        MapsInitializer.updatePrivacyShow(getApplicationContext(), true, true);
        MapsInitializer.updatePrivacyAgree(getApplicationContext(), true);
        ((HealthApplication) getApplication()).initSDK(); //隐私政策已同意，开始初始化
        mViewModel.refreshToken();
        /*PermissionsHelper permissionsHelper = new PermissionsHelper(this);
        permissionsHelper.checkAppRequestPermissions(new PermissionsHelper.OnPermissionListener() {
            @Override
            public void onPermissionsSuccess(String[] permissions) {
                mViewModel.refreshToken();
            }

            @Override
            public void onPermissionFailed(String permission) {
                JL_Log.w(tag, "-onPermissionFailed- " + permission);
                mViewModel.refreshToken();
            }
        });*/
    }

    private void checkAppStatus() {
        if (mViewModel.isAgreePolicy()) {
            onAgree(null);
        } else {
            showPolicyDialog();
        }
    }

    private void showPolicyDialog() {
        UserServiceDialog userServiceDialog = new UserServiceDialog(this);
        userServiceDialog.setCancelable(false);
        userServiceDialog.show(getSupportFragmentManager(), userServiceDialog.getClass().getCanonicalName());
    }
}