package com.jieli.healthaide.data.vo.preview;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.BaseParseVo;
import com.jieli.healthaide.data.vo.parse.IParserModify;

import java.util.List;

/**
 * @ClassName: PreviewPressureVo
 * @Description: 预览视图-压力
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/7/6 8:28
 */
public class PreviewPressureVo extends BaseParseVo {
    @Override
    protected IParserModify getParser() {
        return null;
    }

    @Override
    public byte getType() {
        return 0;
    }

    @Override
    public List<HealthEntity> createTestData(long startTime, long endTime) {
        return null;
    }


/*
    protected PressureParseImpl parse = new PressureParseImpl();
    private List<ParseEntity> entities = new ArrayList<>();


    public List<ParseEntity> getEntities() {
        return entities;
    }
    @Override
    protected void parse(HealthEntity healthEntity) {
        List<ParseEntity> parseEntities = new ArrayList<>();
        HealthEntity entity = healthEntity;
        if (null != entity) {
            List<HealthEntity> tempArray = new ArrayList<>();
            tempArray.add(entity);
            parseEntities = parse.parse(tempArray);
        }
        if (parseEntities.size() != 0) {
            setStartTime(healthEntity.getTime());
        }
        entities = parseEntities;
    }

    @Override
    public byte getType() {
        return HealthEntity.DATA_TYPE_PRESSURE;
    }

    @Override
    public LiveData<HealthEntity> createTestData(long startTime, long endTime) {

        HealthEntity entity = new HealthEntity();

        entity.setSync(false);
        entity.setSpace((byte) 30);
        entity.setId(CalendarUtil.removeTime(startTime));
        entity.setTime(startTime);
        entity.setVersion((byte) 0);
        entity.setType(getType());

        byte[] data = new byte[24 * 2];

        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (Math.random() * 100);
        }

        entity.setData(data);
        return new MutableLiveData<>(entity);
    }
*/
}
