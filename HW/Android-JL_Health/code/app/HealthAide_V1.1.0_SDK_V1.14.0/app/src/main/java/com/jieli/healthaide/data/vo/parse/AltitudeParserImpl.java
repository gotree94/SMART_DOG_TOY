package com.jieli.healthaide.data.vo.parse;

import com.jieli.healthaide.data.entity.HealthEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 5/28/21
 * @desc :
 */
public class AltitudeParserImpl implements IParserModify<ParseEntityFloat> {

    @Override
    public List<ParseEntityFloat> parse(List<HealthEntity> entities) {
        ArrayList<ParseEntityFloat> dataArray = new ArrayList<>();
        for (HealthEntity healthEntity : entities) {
            if (healthEntity.getVersion() == 0) {
                List<ParseEntityFloat> tempArray = new AltitudeParserImpl.ParserV0().parse(healthEntity);
                dataArray.addAll(tempArray);
            }
        }
        return dataArray;
    }


    private static class ParserV0 implements IParser<ParseEntityFloat> {
        @Override
        public List<ParseEntityFloat> parse(HealthEntity entity) {
            List<ParseEntityFloat> entities = new ArrayList<>();
            long time = entity.getTime();
            long space = entity.getSpace() * 60 * 1000;
            byte[] origin = entity.getData();
            for (int i = 0; i < origin.length; i += 4) {
                int altitudeInteger = (0xff & origin[i]) << 8;
                altitudeInteger = altitudeInteger | origin[i + 1];
                int altitudeDecimal = (0xff & origin[i + 2]) << 8;
                altitudeDecimal = altitudeDecimal | origin[i + 3];
                float altitude = altitudeInteger + altitudeDecimal / 1000;//todo 和固件协商，小数点后面怎么解析
                entities.add(new ParseEntityFloat(time, altitude));
                time += space;
            }
            return entities;
        }
    }
}
