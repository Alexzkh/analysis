package com.zqykj.app.service.factory;

import com.zqykj.app.service.field.FundTacticsFuzzyQueryField;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.enums.QueryType;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;

import java.util.List;

/**
 * @Description: 交易路径查询elasticsearch工厂类
 * @Author zhangkehou
 * @Date 2021/12/17
 */
public interface ITransactionPathQueryRequestFactory {

    /**
     * 构建根据图数据过滤出的ids的集合，查询出原始交易数据的参数
     *
     * @param request: 交易路径请求参数
     * @param param:   案件编号
     * @param ids:     主键id的集合
     * @return: com.zqykj.parameters.query.QuerySpecialParams
     **/
    <T, V> QuerySpecialParams accessTransactionPathDataByCondition(T request, V param, List<String> ids);

    /**
     * 构建根据总表id查询出详情数据结果的参数
     *
     * @param request: 交易路径详情请求参数
     * @param param:   案件编号
     * @return: com.zqykj.parameters.query.QuerySpecialParams
     **/
    <T, V> QuerySpecialParams accessTransactionPathDetailRequest(T request, V param);


    /**
     * 构建本方模糊参数参数
     *
     * @param keyword: 模糊查询关键字
     * @return: com.zqykj.parameters.query.CombinationQueryParams
     **/
    default CombinationQueryParams assembleLocalFuzzy(String keyword) {

        CombinationQueryParams localFuzzy = new CombinationQueryParams();
        localFuzzy.setType(ConditionType.should);
        for (String fuzzyField : FundTacticsFuzzyQueryField.localFuzzyFields) {

            localFuzzy.addCommonQueryParams(new CommonQueryParams(QueryType.wildcard, fuzzyField, keyword));
        }
        return localFuzzy;
    }

    /**
     * 构建对方模糊查询参数
     *
     * @param keyword: 模糊查询关键字
     * @return: com.zqykj.parameters.query.CombinationQueryParams
     **/
    default CombinationQueryParams assembleOppositeFuzzy(String keyword) {

        CombinationQueryParams oppositeFuzzy = new CombinationQueryParams();
        oppositeFuzzy.setType(ConditionType.should);
        for (String fuzzyField : FundTacticsFuzzyQueryField.oppositeFuzzyFields) {

            oppositeFuzzy.addCommonQueryParams(new CommonQueryParams(QueryType.wildcard, fuzzyField, keyword));
        }
        return oppositeFuzzy;
    }
}
