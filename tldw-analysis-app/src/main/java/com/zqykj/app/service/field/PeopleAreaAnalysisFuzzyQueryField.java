package com.zqykj.app.service.field;

/**
 * @Description: 人员地域模糊查询字段
 * @Author zhangkehou
 * @Date 2021/10/27
 */
public interface PeopleAreaAnalysisFuzzyQueryField {

    /**
     * 人员地域分析模糊查询字段
     */
    String[] fuzzyFields = new String[]{
            /**
             * 模糊查询省份名称
             * */
            "province.province_wildcard",

            /**
             * 模糊查询城市名称
             * */
            "city.city_wildcard",

            /**
             * 模糊查询区、县名称
             * */
            "area.area_wildcard",

            /**
             * 模糊查询持卡人名称
             * */
            "account_holder_name.account_holder_name_wildcard"};
}
