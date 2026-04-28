package com.jieli.healthaide.data.vo.parse;

import com.jieli.healthaide.data.entity.HealthEntity;

import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 5/28/21
 * @desc :
 */
public interface IParser<T> {
    List<T> parse(HealthEntity entity);
}
