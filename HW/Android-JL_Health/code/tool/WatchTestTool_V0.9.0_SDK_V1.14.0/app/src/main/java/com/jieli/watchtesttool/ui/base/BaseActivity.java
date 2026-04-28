package com.jieli.watchtesttool.ui.base;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.jieli.component.utils.ToastUtil;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * Des:
 * Author: Bob
 * Date:21-3-2
 * UpdateRemark:
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected final String tag = getClass().getSimpleName();


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

    public void showTips(String tips) {
        ToastUtil.showToastLong(tips);
        JL_Log.d(tag, tips);
    }

}
