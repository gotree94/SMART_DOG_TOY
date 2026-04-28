package com.jieli.watchtesttool.data.bean;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/9/29
 * @desc :
 */
public class CompatRes {

    private ICompatRes iCompatRes;

    public CompatRes() {
        int ver = 17;
        if (ver == 17) {
            iCompatRes = new API17();
        } else if (ver == 18) {
            iCompatRes = new API17();
        } else if (ver == 19) {
            iCompatRes = new API17();
        }


    }

    String getStr() {

        return iCompatRes.getStr();
    }


    private interface ICompatRes {
        String getStr();
    }

    private static class API17 implements ICompatRes {
        @Override
        public String getStr() {
            return "17";
        }
    }

    private static class API18 implements ICompatRes {
        @Override
        public String getStr() {
            return "18";
        }
    }

    private static class API19 implements ICompatRes {
        @Override
        public String getStr() {
            return "19";
        }
    }
}
