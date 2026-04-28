package com.jieli.healthaide.util.phone;

import android.content.Context;
import android.text.TextUtils;

import com.jieli.jl_rcsp.util.JL_Log;

import java.util.Locale;
import java.util.Set;

import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;

/**
 * @ClassName: PhoneUtil
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/4/1 17:44
 */
public class PhoneUtil {
    static PhoneNumberUtil phoneUtil;

    public static void init(Context context) {
        phoneUtil = PhoneNumberUtil.createInstance(context);
    }

    /**
     * 根据电话号码判断有无效
     *
     * @param context
     * @param phoneNumber
     * @return
     **/
    public static boolean isPhoneNumberValid(Context context, String phoneNumber) {
        boolean isValid = false;
        if (!TextUtils.isEmpty(phoneNumber)) {
            Phonenumber.PhoneNumber swissNumberProto = getStructedNumber(context, phoneNumber);
            isValid = phoneUtil.isValidNumber(swissNumberProto); // returns true
        }
        return isValid;
    }

    /**
     * 根据电话号码获取国家代码
     *
     * @param context
     * @param phoneNumber
     * @return
     */
    public static int getCountryCode(Context context, String phoneNumber) {
        Phonenumber.PhoneNumber structuredNumber = getStructedNumber(context, phoneNumber);
        if (structuredNumber != null) {
            return structuredNumber.getCountryCode();
        }
        return 0;
    }

    public static Phonenumber.PhoneNumber getStructedNumber(Context context, String phoneNumber) {
        try {
            return phoneUtil.parse(phoneNumber, getCurrentCountryIso(context));
        } catch (NumberParseException e) {
            return null;
        }
    }

    // 获取国家码 “CN”
    public static String getCurrentCountryIso(Context context) {
        // The {@link CountryDetector} should never return null so this is safe to return as-is.
        return CountryDetector.getInstance(context).getCurrentCountryIso();
    }

    //获取全部的国家区号
    public static void getAllRegionIso() {
        Set<String> set = phoneUtil.getSupportedRegions();

        String[] arr = set.toArray(new String[set.size()]);

        for (int i = 0; i < set.size(); i++) {
            Locale locale = new Locale("en", arr[i]);
            JL_Log.d("PhoneUtil", "getAllRegionIso", " lib country:" + arr[i] + "  " + locale.getDisplayCountry());
        }
    }
}
