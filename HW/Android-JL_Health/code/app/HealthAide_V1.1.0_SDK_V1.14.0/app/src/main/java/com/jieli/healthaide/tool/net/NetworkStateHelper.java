package com.jieli.healthaide.tool.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.jieli.healthaide.HealthApplication;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2020/11/24 2:37 PM
 * @desc :
 */
public class NetworkStateHelper {
    private static volatile NetworkStateHelper instance;
    private final String tag = getClass().getSimpleName();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ConnectivityManager mConnectivityManager;
    private NetWorkStateModel netWorkStateModel;
    private final List<Listener> listeners = new ArrayList<>();
    private TimeOutTask timeOutTask;
    private NetworkStateReceiver mNetworkStateReceiver;

    public static NetworkStateHelper getInstance() {
        if (null == instance) {
            synchronized (NetworkStateHelper.class) {
                if (null == instance) {
                    instance = new NetworkStateHelper();
                }
            }
        }
        return instance;
    }

    private NetworkStateHelper() {
        mConnectivityManager = (ConnectivityManager) HealthApplication.getAppViewModel().getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        checkNetworkIsOpen(mConnectivityManager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                    .build();
            mConnectivityManager.requestNetwork(request, new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    NetworkInfo networkInfo = mConnectivityManager.getNetworkInfo(network);
                    if (networkInfo == null) return;
                    JL_Log.d(tag, "onAvailable", " network : " + networkInfo.getClass() + "\tthread==" + Thread.currentThread().getName());
                    checkNetworkAvailable(networkInfo);
                }


                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    JL_Log.d(tag, "onLost", "network : " + network);
                    NetworkInfo networkInfo = mConnectivityManager.getNetworkInfo(network);
                    int type = networkInfo == null ? ConnectivityManager.TYPE_MOBILE : networkInfo.getType();
                    handleState(type, false);
                }
            });
        } else {
            registerNetworkStateReceiver();
        }
    }

    public void destroy() {
        listeners.clear();
        unregisterNetworkStateReceiver();
        handler.removeCallbacksAndMessages(null);
        instance = null;
    }

    public NetWorkStateModel getNetWorkStateModel() {
        return netWorkStateModel;
    }

    public void registerListener(Listener listener) {
        if (null == listener || listeners.contains(listener)) return;
        if (listeners.add(listener) && null != netWorkStateModel) {
            listener.onNetworkStateChange(netWorkStateModel);
        }
    }

    public void unregisterListener(Listener listener) {
        if (null == listener || listeners.isEmpty()) return;
        listeners.remove(listener);
    }

    private void handleState(int type, boolean available) {
        netWorkStateModel = new NetWorkStateModel(type, available);
        handler.post(() -> {
            for (Listener l : new ArrayList<>(listeners)) {
                l.onNetworkStateChange(new NetWorkStateModel(type, available));
            }
        });
    }


    public interface Listener {
        void onNetworkStateChange(NetWorkStateModel model);
    }

    private void checkNetworkIsOpen(ConnectivityManager cm) {
        if (null == cm) return;
        int[] types = new int[]{
                ConnectivityManager.TYPE_MOBILE,
                ConnectivityManager.TYPE_WIFI,
        };
        for (int type : types) {
            NetworkInfo info = cm.getNetworkInfo(type);
            if (info != null) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    handleState(type, info.isConnected());
                    return;
                }
            }
        }
        handleState(-1, false);
    }

    private static class TimeOutTask implements Runnable {
        private Thread thread;


        public void setThread(Thread thread) {
            this.thread = thread;
        }

        @Override
        public void run() {
            thread.interrupt();
        }
    }


    private boolean checkNetworkIsAvailable(String ip, int pingWay) {
        int timeOut = 3000;
        Process process = null;
        boolean ret = false;
        try {
            String command;
            switch (pingWay) {
                case 1://输出数据不进行ip与主机名反查
                    command = "/system/bin/ping  -n 1 -w 1000 " + ip;
                    break;
                case 2://ping的次数
                default:
                    command = "/system/bin/ping  -c 1 -w 1000 " + ip;
                    break;
            }
            process = Runtime.getRuntime().exec(command);
            long time = new Date().getTime();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ret = process.waitFor(timeOut, TimeUnit.MILLISECONDS);
            } else {
                if (timeOutTask == null) {
                    timeOutTask = new TimeOutTask();
                }
                timeOutTask.setThread(Thread.currentThread());
                handler.postDelayed(timeOutTask, timeOut);
                ret = process.waitFor() == 0;
            }
            JL_Log.d(tag, "checkNetworkIsAvailable", "address=" + ip + "\ttake time=" + (new Date().getTime() - time) + "\tstate:" + ret);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            handler.removeCallbacks(timeOutTask);
            if (process != null) {
                process.destroy();
            }
        }
        return ret;
    }

    private void checkNetworkAvailable(@NonNull NetworkInfo networkInfo) {
        int type = networkInfo.getType();
        long currentTime = System.currentTimeMillis();
        JL_Log.d(tag, "checkNetworkAvailable", "type = " + type + "\tthread==" + Thread.currentThread().getName() + "\tstart = " + currentTime);
        //测试网络是否连接外网
        boolean available = checkNetworkIsAvailable("www.baidu.com", 1) || checkNetworkIsAvailable("www.baidu.com", 2)
                || checkNetworkIsAvailable("www.aliyun.com", 1) || checkNetworkIsAvailable("www.aliyun.com", 2)
                || checkNetworkIsAvailable("www.qq.com", 1) || checkNetworkIsAvailable("www.qq.com", 2);
        JL_Log.d(tag, "checkNetworkAvailable", "type = " + type + "\tthread==" + Thread.currentThread().getName() + "\tavailable = " + available + "\tused : " + (System.currentTimeMillis() - currentTime));
        networkInfo = mConnectivityManager.getNetworkInfo(type);
        if (networkInfo == null)
            return;//网络检测的过程中，该网络已失效
        handleState(type, available);
    }

    private void registerNetworkStateReceiver() {
        if (null == mNetworkStateReceiver) {
            mNetworkStateReceiver = new NetworkStateReceiver();
            IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            HealthApplication.getAppViewModel().getApplication().registerReceiver(mNetworkStateReceiver, intentFilter);
        }
    }

    private void unregisterNetworkStateReceiver() {
        if (null != mNetworkStateReceiver) {
            HealthApplication.getAppViewModel().getApplication().unregisterReceiver(mNetworkStateReceiver);
            mNetworkStateReceiver = null;
        }
    }

    /*
     * 网络状态监听
     */
    public class NetworkStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent) return;
            int networkType = intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, 0);
            final NetworkInfo networkInfo = mConnectivityManager.getNetworkInfo(networkType);
            if (null == networkInfo) return;
            if (networkInfo.isConnected()) {
                JL_Log.i(tag, "onReceive", "Network connected");
                checkNetworkAvailable(networkInfo);
            } else {
                handleState(networkType, false);
            }
        }
    }
}
