package com.zqykj.common.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 人员地域详情请求体
 * @Author zhangkehou
 * @Date 2021/10/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeopleAreaDetailRequest {
    /**
     * 地区名称
     */
    private String regionName;

    /**
     * 字段名称（主要用于区分省、市、区）
     */
    private String field;

    /**
     * 查询请求体，这其中包括模糊搜索、分页、排序等请求参数
     */
    private QueryRequest queryRequest;

}
