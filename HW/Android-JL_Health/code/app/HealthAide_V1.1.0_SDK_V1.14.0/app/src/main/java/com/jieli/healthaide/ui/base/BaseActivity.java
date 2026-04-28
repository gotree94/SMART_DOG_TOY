package com.jieli.healthaide.ui.base;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.component.utils.SystemUtil;
import com.jieli.component.utils.ToastUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.net.NetworkStateHelper;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * Des:
 * Author: Bob
 * Date:21-3-2
 * UpdateRemark:
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected final String tag = getClass().getSimpleName();
    protected NetWorkViewModel mNetWorkViewModel;
    protected OnBackPressIntercept mOnBackPressIntercept;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addNetWorkObserver();
    }

    public void setWindowStatus() {
        setWindowStatus(R.id.main);
    }

    public void setWindowStatus(int id) {
        setWindowStatus(id, false);
    }

    public void setWindowStatus(int id, boolean isFullScreen) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            SystemUtil.setImmersiveStateBar(getWindow(), true);
        } else {
            View view = findViewById(id);
            if (null == view) return;
            ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener() {
                @Override
                public @NonNull WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                    v.setPadding(statusBarInsets.left, isFullScreen ? 0 : statusBarInsets.top, statusBarInsets.right, statusBarInsets.bottom);
                    return insets;
                }
            });
        }
    }

    public void setOnBackPressIntercept(OnBackPressIntercept onBackPressIntercept) {
        this.mOnBackPressIntercept = onBackPressIntercept;
    }

    public void replaceFragment(int containerId, String fragmentName) {
        replaceFragment(containerId, fragmentName, null);
    }

    public void replaceFragment(int containerId, String fragmentName, Bundle bundle) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentName);
        if (fragment == null && fragmentName != null) {
            try {
                fragment = (Fragment) Class.forName(fragmentName).newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (fragment != null) {
            fragment.setArguments(getIntent().getExtras());
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            for (Fragment f : getSupportFragmentManager().getFragments()) {
                fragmentTransaction.hide(f);
            }

            if (!fragment.isAdded()) {
                fragmentTransaction.add(containerId, fragment, fragmentName);
            }
            if (null != bundle) {
                fragment.setArguments(bundle);
            }

            fragmentTransaction.show(fragment);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    private void addNetWorkObserver() {
        if (this instanceof NetworkStateHelper.Listener) {
            mNetWorkViewModel = new ViewModelProvider(this).get(NetWorkViewModel.class);
            mNetWorkViewModel.netWorkLiveData.observe(this, model -> {
                NetworkStateHelper.Listener listener = (NetworkStateHelper.Listener) BaseActivity.this;
                listener.onNetworkStateChange(model);
            });
        }
    }

    protected void showTips(String content) {
        JL_Log.d(tag, "showTips", content);
        ToastUtil.showToastShort(content);
    }

    @Override
    public void onBackPressed() {

        if (mOnBackPressIntercept == null || !mOnBackPressIntercept.intercept()) {
            super.onBackPressed();
        }

    }

    public interface OnBackPressIntercept {

        boolean intercept();
    }

}
