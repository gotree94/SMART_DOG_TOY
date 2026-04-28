package com.jieli.healthaide;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.alipay.sdk.app.EnvUtils;
import com.jieli.bluetooth_connect.util.ConnectUtil;
import com.jieli.component.ActivityManager;
import com.jieli.component.utils.SystemUtil;
import com.jieli.component.utils.ToastUtil;
import com.jieli.healthaide.tool.aiui.iflytek.IflytekAIDialStyleHelper;
import com.jieli.healthaide.tool.config.ConfigHelper;
import com.jieli.healthaide.tool.customdial.CustomDialManager;
import com.jieli.healthaide.tool.unit.BaseUnitConverter;
import com.jieli.healthaide.tool.watch.WatchServerCacheHelper;
import com.jieli.healthaide.ui.base.HealthAppViewModel;
import com.jieli.healthaide.ui.service.NetStateCheckService;
import com.jieli.healthaide.ui.sports.model.SportsInfo;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.healthaide.util.MultiLanguageUtils;
import com.jieli.healthaide.util.phone.PhoneUtil;
import com.jieli.jl_health_http.HttpClient;
import com.jieli.jl_health_http.HttpConstant;
import com.jieli.jl_health_http.test.MockClient;
import com.jieli.jl_rcsp.util.JL_Log;
import com.king.wechat.qrcode.WeChatQRCodeDetector;

import org.opencv.OpenCV;

/**
 * 健康助手应用入口
 *
 * @author zqjasonZhong
 * @since 2021/1/27
 */
public class HealthApplication extends Application {

    /**
     * 不在运动模式
     */
    public static final int SPORT_MODE_IDLE = 0;
    /**
     * 室外运动模式
     */
    public static final int SPORT_MODE_OUTDOOR = SportsInfo.TYPE_OUTDOOR;
    /**
     * 室内运动模式
     */
    public static final int SPORT_MODE_INDOOR = SportsInfo.TYPE_INDOOR;

    private static HealthAppViewModel sHealthAppViewModel;

    /**
     * 当前设备的运动模式
     */
    private int sportMode = SPORT_MODE_IDLE;

    @Override
    public void onCreate() {
        super.onCreate();
        sHealthAppViewModel = new HealthAppViewModel(this);
        init();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    protected void finalize() throws Throwable {
        JL_Log.configureLog(this, false, false);
        super.finalize();
    }

    public static HealthAppViewModel getAppViewModel() {
        return sHealthAppViewModel;
    }

    public int getSportMode() {
        return sportMode;
    }

    public void setSportMode(int sportMode) {
        this.sportMode = sportMode;
    }

    public void initSDK() {
        //电话工具初始化
        PhoneUtil.init(this);

        //设置商用服务器域名
        HttpConstant.BASE_URL = "https://health.jieliapp.com";
        //测试服务器初始化
        if (HealthConstant.USE_TEST_SERVER) {
            MockClient.getInstance().start();
        }
        //网络检测
        new NetStateCheckService();
        //支付宝环境设置
        EnvUtils.setEnv(WatchServerCacheHelper.IS_SAND_BOX ? EnvUtils.EnvEnum.SANDBOX : EnvUtils.EnvEnum.ONLINE);

        //打印配置
        boolean isEnableLogFunc = ConfigHelper.getInstance().isEnableLogFunc();
        JL_Log.setTagPrefix("health");
        JL_Log.configureLog(this, isEnableLogFunc, isEnableLogFunc);
        com.jieli.bluetooth_connect.util.JL_Log.setLog(isEnableLogFunc);
        com.jieli.bluetooth_connect.util.JL_Log.setLogOutput(JL_Log::addLogOutput);
        com.jieli.jl_bt_ota.util.JL_Log.setLog(isEnableLogFunc);
        com.jieli.jl_bt_ota.util.JL_Log.setLogOutput(JL_Log::addLogOutput);

//        SQLiteStudioService.instance().start(this);

        //初始化OpenCV
        OpenCV.initOpenCV();
        //初始化WeChatQRCodeDetector
        WeChatQRCodeDetector.init(this);

        JL_Log.d("HealthApplication", "initSDK", ConnectUtil.formatString("APP Version : %s(%d)",
                SystemUtil.getVersionName(getApplicationContext()),
                SystemUtil.getVersion(getApplicationContext())));
    }

    private void init() {
        ActivityManager.init(this);
        ToastUtil.init(this);
        HttpClient.init(this);
        CustomDialManager.init(this);
        IflytekAIDialStyleHelper.init(this);
        BaseUnitConverter.setType(ConfigHelper.getInstance().getUnitType());

        //多语言设置监听
        registerActivityLifecycleCallbacks(MultiLanguageUtils.callbacks);
    }
}
