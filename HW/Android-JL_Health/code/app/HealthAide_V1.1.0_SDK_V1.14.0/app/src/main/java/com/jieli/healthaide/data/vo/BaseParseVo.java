package com.jieli.healthaide.data.vo;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.parse.IParserModify;

import java.util.List;

/**
 * @ClassName: BaseParseVo
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/6/2 19:37
 */
public abstract class BaseParseVo extends BaseVo {
    protected String tag = getClass().getSimpleName();
    protected IParserModify parser;
    protected List entities = null;

    protected BaseParseVo() {
        parser = getParser();
    }

    public List getEntities() {
        return entities;
    }

    protected abstract IParserModify getParser();

    @Override
    protected void parse(List<HealthEntity> healthEntities) {
        if (parser != null) {
            List entities = parser.parse(healthEntities);
            this.entities = entities;
        }
    }

}
