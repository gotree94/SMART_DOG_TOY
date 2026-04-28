package com.jieli.healthaide.ui.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.component.utils.ToastUtil;
import com.jieli.healthaide.tool.net.NetworkStateHelper;
import com.jieli.healthaide.ui.dialog.PermissionDialog;
import com.jieli.healthaide.ui.dialog.PermissionTipsDialog;
import com.jieli.healthaide.ui.dialog.WaitingDialog;
import com.jieli.jl_rcsp.util.JL_Log;

import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.PermissionUtils;

/**
 * Des:
 * Author: Bob
 * Date:21-3-2
 * UpdateRemark:
 */
public abstract class BaseFragment extends Fragment {
    /***
     * 退出界面操作
     */
    public final static int OP_FINISH = 0;
    /**
     * 返回界面操作
     */
    public final static int OP_BACK = 1;

    protected final String tag = getClass().getSimpleName();
    protected NetWorkViewModel mNetWorkViewModel;
    protected WaitingDialog waitingDialog;

    private PermissionTipsDialog permissionTipsDialog;


    protected final Handler mHandler = new Handler(Looper.getMainLooper());

    private Runnable exitRunnable;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addNetWorkObserver();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disPermissionTipsDialog();
        dismissWaitDialog();
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

    protected boolean isFragmentValid() {
        return !isDetached() && isAdded();
    }

    protected void showPermissionDialog(String permission, PermissionDialog.OnPermissionClickListener listener) {
        showPermissionDialog(permission, null, listener);
    }

    protected void showPermissionDialog(String permission, PermissionRequest request, PermissionDialog.OnPermissionClickListener listener) {
        if (null == permission || !isFragmentValid()) return;
        if (PermissionUtils.hasSelfPermissions(requireContext(), permission)) {
            if (null != listener) listener.onRequest(permission);
            return;
        }
        PermissionDialog permissionDialog = new PermissionDialog(permission, request, listener);
        permissionDialog.setCancelable(true);
        permissionDialog.show(getChildFragmentManager(), PermissionDialog.class.getCanonicalName());
    }

    protected void showWaitDialog() {
        showWaitDialog(false);
    }

    protected void showWaitDialog(boolean isCancelable) {
        if (!isFragmentValid()) return;
        if (waitingDialog == null) {
            waitingDialog = new WaitingDialog(isCancelable);
        }
        if (!waitingDialog.isShow()) {
            waitingDialog.show(getChildFragmentManager(), WaitingDialog.class.getCanonicalName());
        }
    }

    protected void dismissWaitDialog() {
        if (!isFragmentValid()) return;
        if (waitingDialog != null) {
            if (waitingDialog.isShow()) {
                waitingDialog.dismiss();
            }
            waitingDialog = null;
        }
    }

    public void showPermissionTipsDialog(String tips) {
        if (!isFragmentValid()) return;
        if (null == permissionTipsDialog) {
            permissionTipsDialog = new PermissionTipsDialog.Builder()
                    .tips(tips)
                    .build();
        }
        if (!permissionTipsDialog.isShow()) {
            permissionTipsDialog.show(getChildFragmentManager(), PermissionTipsDialog.class.getSimpleName());
        }
    }

    public void disPermissionTipsDialog() {
        if (!isFragmentValid() || permissionTipsDialog == null) return;
        if (permissionTipsDialog.isShow()) {
            permissionTipsDialog.dismiss();
        }
        permissionTipsDialog = null;
    }

    protected void showTips(int resId) {
        showTips(requireContext().getString(resId));
    }

    protected void showTips(String content) {
        JL_Log.d(tag, "showTips", content);
        ToastUtil.showToastShort(content);
    }

    protected void back(long delay, ExitHandler handler) {
        exit(OP_BACK, delay, handler);
    }

    protected void back() {
        back(0, null);
    }

    protected void finish(long delay, ExitHandler handler) {
        exit(OP_FINISH, delay, handler);
    }

    protected void finish() {
        finish(0, null);
    }

    private void addNetWorkObserver() {
        if (this instanceof NetworkStateHelper.Listener) {
            mNetWorkViewModel = new ViewModelProvider(this).get(NetWorkViewModel.class);
            mNetWorkViewModel.netWorkLiveData.observe(getViewLifecycleOwner(), model -> {
                NetworkStateHelper.Listener listener = (NetworkStateHelper.Listener) BaseFragment.this;
                listener.onNetworkStateChange(model);
            });
        }
    }

    private void exit(int op, long delay, ExitHandler handler) {
        if (!isFragmentValid()) return;
        if (null != exitRunnable) {
            mHandler.removeCallbacks(exitRunnable);
            exitRunnable = null;
        }
        if (null != handler) {
            handler.run();
        }
        if (delay <= 0) {
            if (op == OP_BACK) {
                requireActivity().onBackPressed();
                return;
            }
            requireActivity().finish();
            return;
        }
        exitRunnable = () -> exit(op, 0, null);
        mHandler.postDelayed(exitRunnable, delay);
    }

    public interface ExitHandler {

        void run();
    }
}
