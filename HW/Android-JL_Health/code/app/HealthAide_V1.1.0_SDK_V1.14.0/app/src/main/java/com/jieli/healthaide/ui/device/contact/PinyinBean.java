package com.jieli.healthaide.ui.device.contact;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc
 * @since 2022/6/13
 */
public class PinyinBean {
    private String pinyinName;
    private String pinyinNameWithNoMatch;

    private String letterName;

    public void setPinyinNameWithNoMatch(String pinyinNameWithNoMatch) {
        this.pinyinNameWithNoMatch = pinyinNameWithNoMatch;
    }

    public void setPinyinName(String pinyinName) {
        this.pinyinName = pinyinName;
    }

    public void setLetterName(String letterName) {
        this.letterName = letterName;
    }

    public String getPinyinNameWithNoMatch() {
        return pinyinNameWithNoMatch;
    }

    public String getPinyinName() {
        return pinyinName;
    }

    public String getLetterName() {
        return letterName;
    }

    @Override
    public String toString() {
        return "PinyinBean{" +
                "pinyinName='" + pinyinName + '\'' +
                ", pinyinNameWithNoMatch='" + pinyinNameWithNoMatch + '\'' +
                ", letterName='" + letterName + '\'' +
                '}';
    }
}
