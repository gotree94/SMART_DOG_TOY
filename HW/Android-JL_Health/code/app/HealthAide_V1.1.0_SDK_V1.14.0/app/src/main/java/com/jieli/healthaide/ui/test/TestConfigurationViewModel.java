package com.jieli.healthaide.ui.test;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.tool.config.ConfigHelper;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.ui.test.model.TestConfiguration;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * TestConfigurationViewModel
 *
 * @author zhongzhuocheng
 * email: zhongzhuocheng@zh-jieli.com
 * create: 2026/1/13
 * note: 测试配置逻辑实现
 */
public class TestConfigurationViewModel extends ViewModel {

    private final String tag = TestConfigurationViewModel.class.getSimpleName();

    private final ConfigHelper configHelper = ConfigHelper.getInstance();
    private final WatchManager watchManager = WatchManager.getInstance();

    public final MutableLiveData<Boolean> cfgChangeMLD = new MutableLiveData<>(false);

    private final TestConfiguration cacheCfg;

    private TestConfiguration modifyCfg;

    public TestConfigurationViewModel() {
        cacheCfg = new TestConfiguration(configHelper.isEnableLogFunc(), configHelper.isEnableDeviceAuth(), configHelper.isEnableLocalOTATest());
    }

    public boolean isChangeCfg() {
        return cfgChangeMLD.getValue() == Boolean.TRUE;
    }

    public boolean isDeviceConnected() {
        return watchManager.isConnected();
    }

    public String getSaveLogFilePath() {
        return JL_Log.getSaveLogPath(getContext());
    }

    public TestConfiguration getTestConfiguration() {
        return cacheCfg;
    }

    public void syncTestConfiguration(TestConfiguration cfg) {
        if (null == cfg) return;
        boolean cacheValue = isChangeCfg();
        boolean isChange = !cfg.equals(cacheCfg);
        if (cacheValue != isChange) {
            if (isChange) {
                modifyCfg = cfg;
            } else {
                modifyCfg = null;
            }
            cfgChangeMLD.postValue(isChange);
        }
    }

    public void saveTestConfiguration() {
        if (null == modifyCfg) return;
        if (cacheCfg.isEnableLogFunc() != modifyCfg.isEnableLogFunc()) {
            configHelper.setEnableLogFunc(modifyCfg.isEnableLogFunc());
            JL_Log.configureLog(getContext(), modifyCfg.isEnableLogFunc(), modifyCfg.isEnableLogFunc());
        }
        if (cacheCfg.isEnableDeviceAuth() != modifyCfg.isEnableDeviceAuth()) {
            configHelper.setEnableDeviceAuth(modifyCfg.isEnableDeviceAuth());
            if (watchManager.isConnected()) {
                watchManager.getBluetoothHelper().disconnectDevice(watchManager.getConnectedDevice());
            }
        }
        if (cacheCfg.isEnableLocalOTATest() != modifyCfg.isEnableLocalOTATest()) {
            configHelper.setEnableLocalOtaTest(modifyCfg.isEnableLocalOTATest());
        }
    }

    private Context getContext() {
        return HealthApplication.getAppViewModel().getApplication();
    }

}
