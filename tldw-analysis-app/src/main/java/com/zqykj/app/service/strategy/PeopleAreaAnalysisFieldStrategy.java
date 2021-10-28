package com.zqykj.app.service.strategy;

import com.zqykj.app.service.field.TacticsAnalysisField;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 根据省市区获取库中对应字段
 * @Author zhangkehou
 * @Date 2021/10/27
 */
@Component
public class PeopleAreaAnalysisFieldStrategy {

    public static final Map<String, String> PEOPLE_AREA_MAP = new ConcurrentHashMap<>();

    static {
        /**
         *省
         * */
        PEOPLE_AREA_MAP.put("province", TacticsAnalysisField.PROVINCE_FIELD);

        /**
         *市
         * */
        PEOPLE_AREA_MAP.put("city", TacticsAnalysisField.CITY_FIELD);

        /**
         *区、县
         * */
        PEOPLE_AREA_MAP.put("area", TacticsAnalysisField.AREA_FIELD);
    }
}
