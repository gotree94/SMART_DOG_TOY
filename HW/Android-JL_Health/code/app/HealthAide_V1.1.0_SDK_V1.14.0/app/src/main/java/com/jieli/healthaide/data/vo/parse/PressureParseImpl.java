package com.jieli.healthaide.data.vo.parse;

import com.jieli.healthaide.data.entity.HealthEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 5/31/21
 * @desc :
 */
public class PressureParseImpl implements IParserModify<ParseEntity> {
    @Override
    public List<ParseEntity> parse(List<HealthEntity> entities) {
        ArrayList<ParseEntity> dataArray = new ArrayList<>();
        for (HealthEntity healthEntity : entities) {
            if (healthEntity.getVersion() == 0) {
                List<ParseEntity> tempArray = new PressureParseImpl.ParserV0().parse(healthEntity);
                dataArray.addAll(tempArray);
            }
        }
        return dataArray;
    }


    private static class ParserV0 implements IParser<ParseEntity> {
        @Override
        public List<ParseEntity> parse(HealthEntity entity) {
            List<ParseEntity> entities = new ArrayList<>();
            long time = entity.getTime();
            long space = entity.getSpace() * 60 * 1000;
            byte[] origin = entity.getData();

            for (int i = 0; i < origin.length; i++) {
                int pressure = origin[i] & 0xFF;
                entities.add(new ParseEntity(time, pressure));
                time += space;
            }
            return entities;
        }
    }
}