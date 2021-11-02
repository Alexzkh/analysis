package com.zqykj.app.service.transform;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 人员地域字段转换为elasticsearch字段
 * @Author zhangkehou
 * @Date 2021/10/26
 */
public class PeopleAreaConversion {

    public static final Map<String, String> REGION_NAME = new ConcurrentHashMap<>();

    static {
        REGION_NAME.put("province", "province.province_wildcard");
        REGION_NAME.put("city", "city.city_wildcard");
        REGION_NAME.put("area", "area.area_wildcard");

    }
}
