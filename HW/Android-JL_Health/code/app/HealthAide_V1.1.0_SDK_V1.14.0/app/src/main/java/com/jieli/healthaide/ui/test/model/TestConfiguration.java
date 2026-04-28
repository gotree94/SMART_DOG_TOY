package com.jieli.healthaide.ui.test.model;

import java.util.Objects;

/**
 * TestConfiguration
 *
 * @author zhongzhuocheng
 * email: zhongzhuocheng@zh-jieli.com
 * create: 2026/1/13
 * note: 测试配置信息
 */
public class TestConfiguration {

    /**
     * 是否使能LOG功能
     */
    private boolean isEnableLogFunc;
    /**
     * 是否使能设备认证功能
     */
    private boolean isEnableDeviceAuth;
    /**
     * 是否使能本地OTA测试
     */
    private boolean isEnableLocalOTATest;

    public TestConfiguration() {
        this(false, false, false);
    }

    public TestConfiguration(boolean isEnableLogFunc, boolean isEnableDeviceAuth, boolean isEnableLocalOTATest) {
        this.isEnableLogFunc = isEnableLogFunc;
        this.isEnableDeviceAuth = isEnableDeviceAuth;
        this.isEnableLocalOTATest = isEnableLocalOTATest;
    }

    public boolean isEnableLogFunc() {
        return isEnableLogFunc;
    }

    public TestConfiguration setEnableLogFunc(boolean enableLogFunc) {
        isEnableLogFunc = enableLogFunc;
        return this;
    }

    public boolean isEnableDeviceAuth() {
        return isEnableDeviceAuth;
    }

    public TestConfiguration setEnableDeviceAuth(boolean enableDeviceAuth) {
        isEnableDeviceAuth = enableDeviceAuth;
        return this;
    }

    public boolean isEnableLocalOTATest() {
        return isEnableLocalOTATest;
    }

    public TestConfiguration setEnableLocalOTATest(boolean enableLocalOTATest) {
        isEnableLocalOTATest = enableLocalOTATest;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TestConfiguration that = (TestConfiguration) o;
        return isEnableLogFunc == that.isEnableLogFunc && isEnableDeviceAuth == that.isEnableDeviceAuth && isEnableLocalOTATest == that.isEnableLocalOTATest;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isEnableLogFunc, isEnableDeviceAuth, isEnableLocalOTATest);
    }

    @Override
    public String toString() {
        return "TestConfiguration{" +
                "isEnableLogFunc=" + isEnableLogFunc +
                ", isEnableDeviceAuth=" + isEnableDeviceAuth +
                ", isEnableLocalOTATest=" + isEnableLocalOTATest +
                '}';
    }
}
