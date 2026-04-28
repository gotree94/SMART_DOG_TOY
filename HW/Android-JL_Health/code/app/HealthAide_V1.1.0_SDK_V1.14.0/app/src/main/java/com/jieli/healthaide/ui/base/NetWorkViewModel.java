package com.jieli.healthaide.ui.base;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jieli.healthaide.tool.net.NetWorkStateModel;
import com.jieli.healthaide.tool.net.NetworkStateHelper;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/22/21 10:56 AM
 * @desc :
 */
public class NetWorkViewModel extends ViewModel implements NetworkStateHelper.Listener {
    MutableLiveData<NetWorkStateModel> netWorkLiveData = new MutableLiveData<>();

    public NetWorkViewModel() {
        NetworkStateHelper.getInstance().registerListener(this);
//        netWorkLiveData.postValue(NetworkStateHelper.getInstance().getNetWorkStateModel());
    }

    @Override
    protected void onCleared() {
        NetworkStateHelper.getInstance().unregisterListener(this);
        super.onCleared();
    }

    @Override
    public void onNetworkStateChange(NetWorkStateModel model) {
        netWorkLiveData.postValue(model);
    }
}
