package com.jieli.healthaide.data.vo.preview;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.BaseParseVo;
import com.jieli.healthaide.data.vo.parse.IParserModify;

import java.util.List;

/**
 * @ClassName: PreviewStepVo
 * @Description: 预览视图-海拔高度
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/7/6 8:23
 */
public class PreviewAltitudeVo extends BaseParseVo {
    protected float totalClimb = 0.0f;

    public float getTotalClimb() {
        return totalClimb;
    }

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
   /* protected AltitudeParserImpl parse = new AltitudeParserImpl();
    private List<ParseEntityFloat> entities = new ArrayList<>();


    public List<ParseEntityFloat> getEntities() {
        return entities;
    }
    @Override
    protected void parse(HealthEntity healthEntity) {
        List<ParseEntityFloat> parseEntities = new ArrayList<>();
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
        return HealthEntity.DATA_TYPE_ALTITUDE;
    }

    @Override
    public LiveData<HealthEntity> createTestData(long startTime, long endTime) {

        HealthEntity entity = new HealthEntity();

        entity.setSync(false);
        entity.setSpace((byte) 60);
        entity.setId(CalendarUtil.removeTime(startTime));
        entity.setTime(startTime);
        entity.setVersion((byte) 0);
        entity.setType(getType());

        byte[] data = new byte[24 * 2];

        for (int i = 0; i < data.length; i += 2) {
            byte[] sourceData = CHexConver.int2byte2((int) (Math.random() * 2000));
            data[i] = sourceData[0];
            data[i + 1] = sourceData[1];
        }

        entity.setData(data);
        return new MutableLiveData<>(entity);
    }*/
}
