package com.jieli.healthaide.data.vo.parse;

/**
 * @ClassName: ParseEntityObject
 * @Description: 解析多个属性
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/11/8 11:05
 */
public class ParseEntityObject extends ParseEntity {
    public double attr1;
    public double attr2;
    public double attr3;

    public ParseEntityObject(long startTime, double attr1, double attr2, double attr3) {
        super();
        setStartTime(startTime);
        this.attr1 = attr1;
        this.attr2 = attr2;
        this.attr3 = attr3;
    }
}
