package com.jieli.healthaide.ui.device.upgrade;

import com.google.gson.GsonBuilder;

import java.util.List;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc SDK映射
 * @since 2023/12/27
 */
public class SdkMapInfo {
    /**
     * SDK信息
     */
    private CursdkDTO cursdk;
    /**
     * 升级差分包信息
     */
    private List<MapDTO> map;

    public CursdkDTO getCursdk() {
        return cursdk;
    }

    public void setCursdk(CursdkDTO cursdk) {
        this.cursdk = cursdk;
    }

    public List<MapDTO> getMap() {
        return map;
    }

    public void setMap(List<MapDTO> map) {
        this.map = map;
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }

    public static class CursdkDTO {
        /**
         * SDK版本号
         */
        private String version;
        /**
         * 厂商信息
         */
        private String vendor;
        /**
         * 芯片型号
         */
        private String chip;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getVendor() {
            return vendor;
        }

        public void setVendor(String vendor) {
            this.vendor = vendor;
        }

        public String getChip() {
            return chip;
        }

        public void setChip(String chip) {
            this.chip = chip;
        }

        @Override
        public String toString() {
            return new GsonBuilder().create().toJson(this);
        }
    }

    public static class MapDTO {
        /**
         * 升级版本号
         */
        private String version;
        /**
         * 升级包名
         */
        private String pakage;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getPakage() {
            return pakage;
        }

        public void setPakage(String pakage) {
            this.pakage = pakage;
        }

        @Override
        public String toString() {
            return new GsonBuilder().create().toJson(this);
        }
    }
}
