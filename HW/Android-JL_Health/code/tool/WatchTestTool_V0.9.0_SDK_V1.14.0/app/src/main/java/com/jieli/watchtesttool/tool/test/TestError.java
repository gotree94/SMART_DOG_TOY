package com.jieli.watchtesttool.tool.test;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 6/4/21
 * @desc : 错误类
 */
public class TestError {

    public static final TestError SUCCESS = new TestError(0, "测试完成");
    public static final TestError FAILED = new TestError(-1, "测试失败");

    public static final int ERR_SUCCESS = 0;                  //测试成功
    public static final int ERR_FAILED = -1;                  //测试失败
    public static final int ERR_INVALID_PARAM = -2;           //无效参数
    public static final int ERR_DEVICE_NOT_CONNECT = -3;      //设备未连接
    public static final int ERR_USER_STOP = -4;               //用户取消测试
    public static final int ERR_TEST_IN_PROGRESS = -5;        //测试进行中

    public static final int ERR_NOT_FOUND_DEVICE = -11;       //未找到回连设备
    public static final int ERR_RECONNECT_OVER_TIME = -12;    //回连次数超出限制

    public static final int ERR_NOT_FOUND_FILE = -20;         //未找到文件
    public static final int ERR_OTA_FIRMWARE = -21;           //固件升级失败
    public static final int ERR_OTA_RESOURCE = -22;           //更新资源失败
    public static final int ERR_FILE_ABNORMAL = -23;          //文件异常

    public int code;
    public String msg;

    public TestError(int code) {
        this(code, getTestMsg(code));
    }

    public TestError(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getTestMsg(int code) {
        String msg = "";
        switch (code) {
            case ERR_SUCCESS:
                msg = "测试完成";
                break;
            case ERR_FAILED:
                msg = "测试失败";
                break;
            case ERR_INVALID_PARAM:
                msg = "无效参数";
                break;
            case ERR_DEVICE_NOT_CONNECT:
                msg = "远端设备未连接";
                break;
            case ERR_USER_STOP:
                msg = "用户取消测试";
                break;
            case ERR_TEST_IN_PROGRESS:
                msg = "测试进行中";
                break;
            case ERR_NOT_FOUND_DEVICE:
                msg = "没有找到回连设备";
                break;
            case ERR_RECONNECT_OVER_TIME:
                msg = "回连次数超出限制";
                break;
            case ERR_NOT_FOUND_FILE:
                msg = "未找到文件";
                break;
            case ERR_OTA_FIRMWARE:
                msg = "固件升级失败";
                break;
            case ERR_OTA_RESOURCE:
                msg = "更新资源失败";
                break;
            case ERR_FILE_ABNORMAL:
                msg = "文件异常";
                break;
        }
        return msg;
    }

    @Override
    public String toString() {
        return "TestError{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
