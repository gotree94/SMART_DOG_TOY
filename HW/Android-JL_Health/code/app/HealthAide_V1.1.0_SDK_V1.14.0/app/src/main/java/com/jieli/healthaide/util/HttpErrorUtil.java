package com.jieli.healthaide.util;

import android.content.Context;

import com.jieli.component.utils.ToastUtil;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/11/23
 * @desc :
 */
public class HttpErrorUtil {


    public static void showErrorToast(int code) {
        Context context = HealthApplication.getAppViewModel().getApplication();;
        String msg = "";
        switch (code) {
            case 400:
                msg = context.getString(R.string.http_tip_400);
                break;
            case 401:
                msg = context.getString(R.string.http_tip_401);
                break;
            case 403:
                msg = context.getString(R.string.http_tip_403);
                break;
            case 404:
                msg = context.getString(R.string.http_tip_404);
                break;
            case 405:
                msg = context.getString(R.string.http_tip_405);
                break;

            case 500:
                msg = context.getString(R.string.http_tip_500);
                break;
            case 502:
                msg = context.getString(R.string.http_tip_501);
                break;
            case 504:
                msg = context.getString(R.string.http_tip_504);
                break;
            default:
                msg = context.getString(R.string.http_tip_unknow);
                break;
        }

        ToastUtil.showToastShort(msg);

    }
}
