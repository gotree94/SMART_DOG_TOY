package com.jieli.healthaide.data.vo.preview;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.parse.StepParserImpl;
import com.jieli.healthaide.data.vo.step.StepDayVo;

/**
 * @ClassName: PreviewStepVo
 * @Description: 预览步数视图
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/7/6 8:23
 */
public class PreviewStepVo extends StepDayVo {
    protected StepParserImpl parse = new StepParserImpl();
    public int step;//总步数 (步)
    public float distance;//总距离 (km)
    public int cal;//总热量 (千卡)
    @Override
    public byte getType() {
        return HealthEntity.DATA_TYPE_STEP;
    }

}
