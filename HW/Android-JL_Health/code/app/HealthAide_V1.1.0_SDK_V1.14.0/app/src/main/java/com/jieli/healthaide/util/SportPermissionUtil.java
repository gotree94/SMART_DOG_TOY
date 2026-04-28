package com.jieli.healthaide.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.jieli.healthaide.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName: SelfStartingUtil
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/10/27 11:56
 */
public class SportPermissionUtil {

    /**
     * 功能:Intent跳转到[自启动]页面全网最全适配机型解决方案
     */
    private static HashMap<String, List<String>> autoStartHashMap = new HashMap<String, List<String>>() {
        {
            put("Xiaomi", Arrays.asList(
                    "com.miui.securitycenter/com.miui.permcenter.autostart.AutoStartManagementActivity",//MIUI10_9.8.1(9.0)
                    "com.miui.securitycenter"
            ));

            put("samsung", Arrays.asList(
                    "com.samsung.android.sm_cn/com.samsung.android.sm.ui.ram.AutoRunActivity",
                    "com.samsung.android.sm_cn/com.samsung.android.sm.ui.appmanagement.AppManagementActivity",
                    "com.samsung.android.sm_cn/com.samsung.android.sm.ui.cstyleboard.SmartManagerDashBoardActivity",
                    "com.samsung.android.sm_cn/.ui.ram.RamActivity",
                    "com.samsung.android.sm_cn/.app.dashboard.SmartManagerDashBoardActivity",

                    "com.samsung.android.lool/com.samsung.android.sm.ui.battery.BatteryActivity",
                    "com.samsung.android.sm_cn",
                    "com.samsung.android.sm"
            ));


            put("HUAWEI", Arrays.asList(
                    "com.huawei.systemmanager/.startupmgr.ui.StartupNormalAppListActivity",//EMUI9.1.0(方舟,9.0)
                    "com.huawei.systemmanager/.appcontrol.activity.StartupAppControlActivity",
                    "com.huawei.systemmanager/.optimize.process.ProtectActivity",
                    "com.huawei.systemmanager/.optimize.bootstart.BootStartActivity",
                    "com.huawei.systemmanager"//最后一行可以写包名, 这样如果签名的类路径在某些新版本的ROM中没找到 就直接跳转到对应的安全中心/手机管家 首页.
            ));

            put("vivo", Arrays.asList(
                    "com.iqoo.secure/.ui.phoneoptimize.BgStartUpManager",
                    "com.iqoo.secure/.safeguard.PurviewTabActivity",
                    "com.vivo.permissionmanager/.activity.BgStartUpManagerActivity",
//                    "com.iqoo.secure/.ui.phoneoptimize.AddWhiteListActivity", //这是白名单, 不是自启动
                    "com.iqoo.secure",
                    "com.vivo.permissionmanager"
            ));

            put("Meizu", Arrays.asList(
                    "com.meizu.safe/.permission.SmartBGActivity",//Flyme7.3.0(7.1.2)
                    "com.meizu.safe/.permission.PermissionMainActivity",//网上的
                    "com.meizu.safe"
            ));

            put("OPPO", Arrays.asList(
                    "com.coloros.safecenter/.startupapp.StartupAppListActivity",
                    "com.coloros.safecenter/.permission.startup.StartupAppListActivity",
                    "com.oppo.safe/.permission.startup.StartupAppListActivity",
                    "com.coloros.oppoguardelf/com.coloros.powermanager.fuelgaue.PowerUsageModelActivity",
                    "com.coloros.safecenter/com.coloros.privacypermissionsentry.PermissionTopActivity",
                    "com.coloros.safecenter",
                    "com.oppo.safe",
                    "com.coloros.oppoguardelf"
            ));

            put("oneplus", Arrays.asList(
                    "com.oneplus.security/.chainlaunch.view.ChainLaunchAppListActivity",
                    "com.oneplus.security"
            ));
            put("letv", Arrays.asList(
                    "com.letv.android.letvsafe/.AutobootManageActivity",
                    "com.letv.android.letvsafe/.BackgroundAppManageActivity",//应用保护
                    "com.letv.android.letvsafe"
            ));
            put("zte", Arrays.asList(
                    "com.zte.heartyservice/.autorun.AppAutoRunManager",
                    "com.zte.heartyservice"
            ));
            //金立
            put("F", Arrays.asList(
                    "com.gionee.softmanager/.MainActivity",
                    "com.gionee.softmanager"
            ));

            //以下为未确定(厂商名也不确定)
            put("smartisanos", Arrays.asList(
                    "com.smartisanos.security/.invokeHistory.InvokeHistoryActivity",
                    "com.smartisanos.security"
            ));
            //360
            put("360", Arrays.asList(
                    "com.yulong.android.coolsafe/.ui.activity.autorun.AutoRunListActivity",
                    "com.yulong.android.coolsafe"
            ));
            //360
            put("ulong", Arrays.asList(
                    "com.yulong.android.coolsafe/.ui.activity.autorun.AutoRunListActivity",
                    "com.yulong.android.coolsafe"
            ));
            //酷派
            put("coolpad"/*厂商名称不确定是否正确*/, Arrays.asList(
                    "com.yulong.android.security/com.yulong.android.seccenter.tabbarmain",
                    "com.yulong.android.security"
            ));
            //联想
            put("lenovo"/*厂商名称不确定是否正确*/, Arrays.asList(
                    "com.lenovo.security/.purebackground.PureBackgroundActivity",
                    "com.lenovo.security"
            ));
            put("htc"/*厂商名称不确定是否正确*/, Arrays.asList(
                    "com.htc.pitroad/.landingpage.activity.LandingPageActivity",
                    "com.htc.pitroad"
            ));
            //华硕
            put("asus"/*厂商名称不确定是否正确*/, Arrays.asList(
                    "com.asus.mobilemanager/.MainActivity",
                    "com.asus.mobilemanager"
            ));

        }
    };
    private static HashMap<String, String> autoStartGuideHashMap_zh = new HashMap<String, String>() {
        {
            put("Xiaomi".toLowerCase(), "安全中心 -> 应用管理 -> 权限 -> 自启动管理 -> 打开开关允许%1$s自启动。");

            put("samsung".toLowerCase(), "智能管理器 -> 自动运行应用程序 -> 打开开关允许%1$s自启动。");


            put("HUAWEI".toLowerCase(), "手机管家 -> 应用启动管理 -> 关闭自动管理%1$s并打开各允许启动开关。");

            put("vivo".toLowerCase(), "i管家 -> 软件管理 -> 自启动管理 -> 打开开关允许%1$s自启动。");

            put("Meizu".toLowerCase(), "手机管家 -> 后台管理 -> 选择%1$s允许后台运行。");

            put("OPPO".toLowerCase(), "手机管家 -> 权限隐私 -> 应用权限管理 -> 打开开关允许%1$s自启动。");

            put("oneplus".toLowerCase(), "设置 -> 应用和通知 -> 特殊应用权限 -> 电池优化 -> 选择%1$s不优化。");
            put("letv".toLowerCase(), "管家 -> 安全隐私 -> 自启动管理 -> 打开开关允许%1$s自启动。");
            //以下为未确定(厂商名也不确定) 锤子手机
            put("smartisanos".toLowerCase(), "安全中心 -> 应用程序权限管理 -> 自启动权限管理 -> 选择%1$s -> 打开开关允许%1$s自启动。");
            //联想
            put("lenovo".toLowerCase()/*厂商名称不确定是否正确*/, "安全中心 -> 应用管理 -> 选择%1$s -> 打开开关允许%1$s自启动。");

        }
    };
    private static HashMap<String, String> autoStartGuideHashMap_en = new HashMap<String, String>() {
        {
            put("Xiaomi".toLowerCase(), "Security Center - > Application Management - > permissions - > self start management - > turn on the switch to allow %1$s to start automatically.");

            put("samsung".toLowerCase(), "Smart manager - > run application automatically - > turn on switch to allow %1$s to start automatically.");


            put("HUAWEI".toLowerCase(), "Mobile Housekeeper - > application startup management - > turn off automatic management %1$s and turn on each start allowed switch.");

            put("vivo".toLowerCase(), "I Housekeeper - > software management - > self start management - > turn on the switch to allow %1$s to start automatically.");

            put("Meizu".toLowerCase(), "Mobile Manager - > background management - > select %1$s to allow background operation.");

            put("OPPO".toLowerCase(), "Mobile Manager - > rights privacy - > application rights management - > turn on the switch to allow %1$s to start automatically.");

            put("oneplus".toLowerCase(), "Settings - > apps and notifications - > Special app permissions - > battery optimization - > select %1$s not to optimize");

            put("letv".toLowerCase(), "Housekeeper - > Security and privacy - > self start management - > turn on the switch to allow %1$s to start automatically");
            //以下为未确定(厂商名也不确定) 锤子手机
            put("smartisanos".toLowerCase(), "Security Center - > application rights management - > self start rights management - > select %1$s - > turn on the switch to allow %1$s to start automatically");
            //联想
            put("lenovo".toLowerCase()/*厂商名称不确定是否正确*/, "Security Center - > Application Management - > select%1$s - > turn on the switch to allow% 1 $s to start automatically.");

        }
    };

    public static void startToAutoStartSetting(Context context) {
        Log.e("Util", "******************当前手机型号为：" + Build.MANUFACTURER);

        Set<Map.Entry<String, List<String>>> entries = autoStartHashMap.entrySet();
        boolean has = false;
        for (Map.Entry<String, List<String>> entry : entries) {
            String manufacturer = entry.getKey();
            List<String> actCompatList = entry.getValue();
            if (Build.MANUFACTURER.equalsIgnoreCase(manufacturer)) {
                for (String act : actCompatList) {
                    try {
                        Intent intent;
                        if (act.contains("/")) {
                            intent = new Intent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            ComponentName componentName = ComponentName.unflattenFromString(act);
                            intent.setComponent(componentName);
                        } else {
                            //找不到? 网上的做法都是跳转到设置... 这基本上是没意义的 基本上自启动这个功能是第三方厂商自己写的安全管家类app
                            //所以我是直接跳转到对应的安全管家/安全中心
                            intent = context.getPackageManager().getLaunchIntentForPackage(act);
                        }
                        context.startActivity(intent);
                        has = true;
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (!has) {
//            Toast.makeText(context, "兼容方案", Toast.LENGTH_SHORT).show();
            try {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }


    }

    public static String getAutoStartSettingGuide(Context context) {
        String guide;
        String appName = context.getString(R.string.app_name);
        String language = Locale.getDefault().getLanguage().toLowerCase();
        switch (language) {
            case "zh":
                guide = "请在设置中打开%1$s的自启动管理开关。";
                String tempGuidezh = autoStartGuideHashMap_zh.get(Build.MANUFACTURER.toLowerCase());
                if (!TextUtils.isEmpty(tempGuidezh)) {
                    guide = tempGuidezh;
                }
                break;
            case "en":
            default:
                guide = "Please turn on the self start management switch of %1$s in the settings.";
                String tempGuideen = autoStartGuideHashMap_en.get(Build.MANUFACTURER.toLowerCase());
                if (!TextUtils.isEmpty(tempGuideen)) {
                    guide = tempGuideen;
                }
                break;
        }
        guide = CalendarUtil.formatString(guide, appName);
        return guide;
    }

    /**
     * 功能: 电池优化白名单
     */
    //判断我们的应用是否在白名单中
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isIgnoringBatteryOptimizations(Context context) {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return isIgnoring;
    }

    //可以通过以下代码申请加入白名单：
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestIgnoreBatteryOptimizations(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 功能: 打开防睡眠系统设置
     *
     * https://blog.csdn.net/qq_41563374/article/details/103079582
     */
    /**
     * 功能:Intent跳转到防睡眠
     */
    private static HashMap<String, List<String>> sleepPreventionHashMap = new HashMap<String, List<String>>() {
        {
            put("Xiaomi", Arrays.asList(
                    "com.miui.powerkeeper/com.miui.powerkeeper.ui.HiddenAppsConfigActivity",
                    "com.miui.powerkeeper"
            ));

            put("samsung", Arrays.asList(
                    "com.samsung.android.sm_cn/com.samsung.android.sm.ui.ram.AutoRunActivity",
                    "com.samsung.android.sm_cn/com.samsung.android.sm.ui.appmanagement.AppManagementActivity",
                    "com.samsung.android.sm_cn/com.samsung.android.sm.ui.cstyleboard.SmartManagerDashBoardActivity",
                    "com.samsung.android.sm_cn/.ui.ram.RamActivity",
                    "com.samsung.android.sm_cn/.app.dashboard.SmartManagerDashBoardActivity",

                    "com.samsung.android.sm/com.samsung.android.sm.ui.ram.AutoRunActivity",
                    "com.samsung.android.sm/com.samsung.android.sm.ui.appmanagement.AppManagementActivity",
                    "com.samsung.android.sm/com.samsung.android.sm.ui.cstyleboard.SmartManagerDashBoardActivity",
                    "com.samsung.android.sm/.ui.ram.RamActivity",
                    "com.samsung.android.sm/.app.dashboard.SmartManagerDashBoardActivity",

                    "com.samsung.android.lool/com.samsung.android.sm.ui.battery.BatteryActivity",
                    "com.samsung.android.sm_cn",
                    "com.samsung.android.sm"
            ));


            put("HUAWEI", Arrays.asList(
                    "com.android.settings/com.android.settings.Settings$HighPowerApplicationsActivity",
                    "com.android.settings"
//                    "com.huawei.systemmanager/.startupmgr.ui.StartupNormalAppListActivity",//EMUI9.1.0(方舟,9.0)
//                    "com.huawei.systemmanager/.appcontrol.activity.StartupAppControlActivity",
//                    "com.huawei.systemmanager/.optimize.process.ProtectActivity",
//                    "com.huawei.systemmanager/.optimize.bootstart.BootStartActivity",
//                    "com.huawei.systemmanager"//最后一行可以写包名, 这样如果签名的类路径在某些新版本的ROM中没找到 就直接跳转到对应的安全中心/手机管家 首页.
            ));

            put("vivo", Arrays.asList(
                    "com.iqoo.powersaving/com.iqoo.powersaving.PowerSavingManagerActivity",
                    "com.iqoo.powersaving"
            ));

            put("Meizu", Arrays.asList(
                    "com.meizu.safe/.permission.SmartBGActivity",//Flyme7.3.0(7.1.2)
                    "com.meizu.safe/.permission.PermissionMainActivity",//网上的
                    "com.meizu.safe"
            ));

            put("OPPO", Arrays.asList(
                    "com.coloros.oppoguardelf/com.coloros.powermanager.fuelgaue.PowerConsumptionActivity",
                    "com.coloros.oppoguardelf"
            ));

            put("oneplus", Arrays.asList(
                    "com.oneplus.security/.chainlaunch.view.ChainLaunchAppListActivity",
                    "com.oneplus.security"
            ));
            put("letv", Arrays.asList(
                    "com.letv.android.letvsafe/.AutobootManageActivity",
                    "com.letv.android.letvsafe/.BackgroundAppManageActivity",//应用保护
                    "com.letv.android.letvsafe"
            ));
            put("zte", Arrays.asList(
                    "com.zte.heartyservice/.autorun.AppAutoRunManager",
                    "com.zte.heartyservice"
            ));
            //金立
            put("F", Arrays.asList(
                    "com.gionee.softmanager/.MainActivity",
                    "com.gionee.softmanager"
            ));

            //以下为未确定(厂商名也不确定)
            put("smartisanos", Arrays.asList(
                    "com.smartisanos.security/.invokeHistory.InvokeHistoryActivity",
                    "com.smartisanos.security"
            ));
            //360
            put("360", Arrays.asList(
                    "com.yulong.android.coolsafe/.ui.activity.autorun.AutoRunListActivity",
                    "com.yulong.android.coolsafe"
            ));
            //360
            put("ulong", Arrays.asList(
                    "com.yulong.android.coolsafe/.ui.activity.autorun.AutoRunListActivity",
                    "com.yulong.android.coolsafe"
            ));
            //酷派
            put("coolpad"/*厂商名称不确定是否正确*/, Arrays.asList(
                    "com.yulong.android.security/com.yulong.android.seccenter.tabbarmain",
                    "com.yulong.android.security"
            ));
            //联想
            put("lenovo"/*厂商名称不确定是否正确*/, Arrays.asList(
                    "com.lenovo.security/.purebackground.PureBackgroundActivity",
                    "com.lenovo.security"
            ));
            put("htc"/*厂商名称不确定是否正确*/, Arrays.asList(
                    "com.htc.pitroad/.landingpage.activity.LandingPageActivity",
                    "com.htc.pitroad"
            ));
            //华硕
            put("asus"/*厂商名称不确定是否正确*/, Arrays.asList(
                    "com.asus.mobilemanager/.MainActivity",
                    "com.asus.mobilemanager"
            ));

        }
    };
    private static HashMap<String, String> sleepPreventionGuideHashMap_zh = new HashMap<String, String>() {
        {
            put("Xiaomi".toLowerCase(), "手机管家 -> 手机管家分页 -> 应用智能省电 -> %1$s -> 无限制。");

            put("samsung".toLowerCase(), "智能管理器 -> 电池 -> %1$s -> 关闭【使应用程序进入休眠】开关");


            put("HUAWEI".toLowerCase(), "设置 -> 搜索【电池优化】 -> %1$s -> 选择【不允许】。");

            put("vivo".toLowerCase(), "i管家 -> 省电管理 -> 后台高耗电 -> 打开开关允许%1$s允许后台耗电。");

            put("Meizu".toLowerCase(), "手机管家 -> 后台管理 -> 选择%1$s允许后台运行。");

            put("OPPO".toLowerCase(), "设置 -> 电池 -> 耗电保护 -> %1$s -> 关闭【后台冻结】、【自动优化】和【深度睡眠】的开关。");

            put("oneplus".toLowerCase(), "设置 -> 应用和通知 -> 特殊应用权限 -> 电池优化 -> 选择%1$s不优化。");
            put("letv".toLowerCase(), "管家 -> 省电管理 -> 应用保护 -> 打开%1$s的开关。");
            //以下为未确定(厂商名也不确定) 锤子手机
            put("smartisanos".toLowerCase(), "安全中心 -> 省电优化 -> 耗电优化 -> 应用耗电优化 -> %1$s -> 关闭【后台运行智能控制】开关。");
            //联想
            put("lenovo".toLowerCase()/*厂商名称不确定是否正确*/, "安全中心 -> 后台管理 -> 后台运行 -> 选择%1$s -> 允许后台运行。");

        }
    };
    private static HashMap<String, String> sleepPreventionGuideHashMap_en = new HashMap<String, String>() {
        {
            put("Xiaomi".toLowerCase(), "Mobile Housekeeper - > mobile housekeeper paging - > application smart power saving - >%1$s - > unlimited.");

            put("samsung".toLowerCase(), "Smart manager - > battery - >%1$s - > turn off the [put application into sleep] switch");


            put("HUAWEI".toLowerCase(), "Set - > search [battery optimization] - > %1$s - > select [not allowed].");

            put("vivo".toLowerCase(), "Imanager - > power saving management - > background high power consumption - > turn on the switch to allow %1$s to allow background power consumption.");

            put("Meizu".toLowerCase(), "Mobile Manager - > background management - > select %1$s to allow background operation.");

            put("OPPO".toLowerCase(), "Settings - > battery - > power consumption protection - > %1$s - > turn off the switches of [background freeze], [automatic optimization] and [deep sleep].");

            put("oneplus".toLowerCase(), "Settings - > apps and notifications - > Special app permissions - > battery optimization - > select %1$s not to optimize.");
            put("letv".toLowerCase(), "Housekeeper - > power saving management - > application protection - > turn on the switch of %1$s.");
            //以下为未确定(厂商名也不确定) 锤子手机
            put("smartisanos".toLowerCase(), "Security Center - > power saving optimization - > power consumption optimization - > apply power consumption optimization - >%1$s - > turn off the [background operation intelligent control] switch.");
            //联想
            put("lenovo".toLowerCase()/*厂商名称不确定是否正确*/, "Security Center - > background management - > background operation - > select %1$s - > allow background operation.");

        }
    };
    public static void startToSleepPreventionSetting(Context context) {
        Log.e("Util", "******************当前手机型号为：" + Build.MANUFACTURER);

        Set<Map.Entry<String, List<String>>> entries = sleepPreventionHashMap.entrySet();
        boolean has = false;
        for (Map.Entry<String, List<String>> entry : entries) {
            String manufacturer = entry.getKey();
            List<String> actCompatList = entry.getValue();
            if (Build.MANUFACTURER.equalsIgnoreCase(manufacturer)) {
                for (String act : actCompatList) {
                    try {
                        Intent intent;
                        if (act.contains("/")) {
                            intent = new Intent();
                            if (Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")) {
                                intent.putExtra("package_name", context.getPackageName());
                                intent.putExtra("package_label", context.getString(R.string.app_name));
                            }
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            ComponentName componentName = ComponentName.unflattenFromString(act);
                            intent.setComponent(componentName);
                        } else {
                            //找不到? 网上的做法都是跳转到设置... 这基本上是没意义的 基本上自启动这个功能是第三方厂商自己写的安全管家类app
                            //所以我是直接跳转到对应的安全管家/安全中心
                            intent = context.getPackageManager().getLaunchIntentForPackage(act);
                        }
                        context.startActivity(intent);
                        has = true;
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (!has) {
//            Toast.makeText(context, "兼容方案", Toast.LENGTH_SHORT).show();
            try {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }


    }
    public static String getSleepPreventionGuide(Context context) {
        String guide ;
        String appName = context.getString(R.string.app_name);
        String language = Locale.getDefault().getLanguage().toLowerCase();
        switch (language) {
            case "zh":
                guide = "请在设置中打开%1$s的后台运行。";
                String tempGuidezh = sleepPreventionGuideHashMap_zh.get(Build.MANUFACTURER.toLowerCase());
                if (!TextUtils.isEmpty(tempGuidezh)) {
                    guide = tempGuidezh;
                }
                break;
            case "en":
            default:
                guide = "Please open the background running of %1$s in settings.";
                String tempGuideen = sleepPreventionGuideHashMap_en.get(Build.MANUFACTURER.toLowerCase());
                if (!TextUtils.isEmpty(tempGuideen)) {
                    guide = tempGuideen;
                }
                break;
        }
        guide = CalendarUtil.formatString(guide, appName);
        return guide;
    }
}
