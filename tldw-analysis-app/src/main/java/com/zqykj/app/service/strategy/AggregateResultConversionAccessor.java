package com.zqykj.app.service.strategy;

import com.zqykj.common.enums.TacticsTypeEnum;
import com.zqykj.common.request.AssetTrendsRequest;
import com.zqykj.common.response.AssetTrendsResponse;
import com.zqykj.app.service.tools.SplicingStringTools;
import com.zqykj.common.response.PeopleAreaReponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 聚合结果转换成具体需要的实例工厂.
 * @Author zhangkehou
 * @Date 2021/10/21
 */
@Component
public class AggregateResultConversionAccessor {

    private static final Integer SCALE = 2;

    /**
     * 解析资产趋势返回的result.
     */
    private static final TriFunction<List<List<Object>>, String, AssetTrendsRequest, List<AssetTrendsResponse>> ASSET_TRENDS_CONVERSION =
            (lists, caseId, assetTrendsRequest) -> {
                List<AssetTrendsResponse> responses = new ArrayList<>();

                lists.forEach(rowData -> {
                    AssetTrendsResponse assetTrendsResponse = AssetTrendsResponse.builder()
                            .caseId(caseId)
                            .date((String) SplicingStringTools.transfer((String) rowData.get(0), assetTrendsRequest.getDateType()))
                            .totalTransactionMoney(new BigDecimal((String) rowData.get(1)).setScale(SCALE, RoundingMode.HALF_UP))
                            .totolIncome(new BigDecimal((String) rowData.get(4)).setScale(SCALE, RoundingMode.HALF_UP))
                            .totalExpenditure(new BigDecimal((String) rowData.get(6)).setScale(SCALE, RoundingMode.HALF_UP))
                            .transactionNet(new BigDecimal((String) rowData.get(7)).setScale(SCALE, RoundingMode.HALF_UP))
                            .build();
                    responses.add(assetTrendsResponse);
                });
                return responses;
            };


    /**
     * 静态映射表.
     */
    private static final Map<TacticsTypeEnum, TriFunction> functionMap = new ConcurrentHashMap<>();

    static {
        /**
         * 资产趋势的结果解析.
         * */
        functionMap.put(TacticsTypeEnum.ASSET_TRENDS, ASSET_TRENDS_CONVERSION);

    }


    /**
     * 用于获取将标准对象转换为业务对象返回给界面.
     *
     * @param toBeParsedResult: 待转换的结果.未转化成业务对象的结果。
     * @param caseId:           案件编号
     * @param tacticsTypeEnum:
     * @return: Object
     **/
    public Object access(List<List<Object>> toBeParsedResult, String caseId, AssetTrendsRequest assetTrendsRequest, TacticsTypeEnum tacticsTypeEnum) {
        if (functionMap.containsKey(tacticsTypeEnum)) {
            return functionMap.get(tacticsTypeEnum).apply(toBeParsedResult, caseId, assetTrendsRequest);
        }
        return null;
    }


    /**
     * Represents a function that accepts three arguments and produces a result.
     *
     * @param <S> the type of the first argument
     * @param <T> the type of the second argument
     * @param <U> the type of the third argument
     * @param <R> the return type
     */
    @FunctionalInterface
    interface TriFunction<S, T, U, R> {
        /**
         * Applies this function to the given arguments.
         *
         * @param s the first function argument
         * @param t the second function argument
         * @param u the third function argument
         * @return the result
         */
        R apply(S s, T t, U u);
    }


}
