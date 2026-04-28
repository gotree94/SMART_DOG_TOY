package com.jieli.watchtesttool.ui.file.model;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc  测试类型项
 * @since 2022/6/24
 */
public class TestTypeItem {
    private final int type;
    private final String value;

    public TestTypeItem(int type, String value) {
        this.type = type;
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "TestTypeItem{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
