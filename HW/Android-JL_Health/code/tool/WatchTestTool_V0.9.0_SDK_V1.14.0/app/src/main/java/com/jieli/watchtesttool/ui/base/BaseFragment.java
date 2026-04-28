package com.jieli.watchtesttool.ui.base;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.jieli.component.utils.ToastUtil;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.watchtesttool.ui.widget.dialog.PermissionTipsDialog;

/**
 * Des:
 * Author: Bob
 * Date:21-3-2
 * UpdateRemark:
 */
public abstract class BaseFragment extends Fragment {
    protected final String tag = getClass().getSimpleName();

    private PermissionTipsDialog mPermissionTipsDialog;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dismissPermissionTipsDialog();
    }

    public void replaceFragment(int containerId, String fragmentName, Bundle bundle) {
        FragmentManager fragmentManager = getChildFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(fragmentName);
        if (fragment == null && fragmentName != null) {
            try {
                fragment = (Fragment) Class.forName(fragmentName).newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (fragment != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            for (Fragment f : fragmentManager.getFragments()) {
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

    public void showTips(int resId) {
        showTips(getString(resId));
    }

    public void showTips(String tips) {
        ToastUtil.showToastShort(tips);
        JL_Log.d(tag, tips);
    }

    protected void showPermissionTipsDialog(String content) {
        dismissPermissionTipsDialog();
        mPermissionTipsDialog = new PermissionTipsDialog.Builder().tips(content).build();
        mPermissionTipsDialog.show(getChildFragmentManager(), PermissionTipsDialog.class.getSimpleName());
    }

    protected void dismissPermissionTipsDialog() {
        if (null != mPermissionTipsDialog) {
            if (mPermissionTipsDialog.isShow()) {
                mPermissionTipsDialog.dismiss();
            }
            mPermissionTipsDialog = null;
        }
    }
}
