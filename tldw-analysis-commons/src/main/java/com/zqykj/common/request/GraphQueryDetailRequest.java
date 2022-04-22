package com.zqykj.common.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: 交易路径详情请求体
 * @Author zhangkehou
 * @Date 2021/12/18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphQueryDetailRequest {

    /**
     * 交易路径每一条路径所包含总表的id的集合
     */
    private List<String> ids;

    /**
     * 查询请求参数（包括模糊搜索字段和查询、分页参数）
     */
    private QueryRequest queryRequest;
}
