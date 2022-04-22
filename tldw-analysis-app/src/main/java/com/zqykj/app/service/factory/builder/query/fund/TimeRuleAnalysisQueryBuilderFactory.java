package com.zqykj.app.service.factory.builder.query.fund;

import com.zqykj.app.service.factory.TimeRuleAnalysisQueryRequestFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.vo.fund.FundDateRequest;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.enums.QueryType;
import com.zqykj.common.enums.StatisticType;
import com.zqykj.common.request.PagingRequest;
import com.zqykj.common.request.SortingRequest;
import com.zqykj.app.service.vo.fund.TimeRuleAnalysisDetailRequest;
import com.zqykj.app.service.vo.fund.TimeRuleAnalysisRequest;
import com.zqykj.parameters.FieldSort;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.query.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Description: 时间规律分析查询构建工厂类
 * @Author zhangkehou
 * @Date 2021/12/30
 */
@ConditionalOnProperty(name = "enable.datasource.type", havingValue = "elasticsearch")
@Service
public class TimeRuleAnalysisQueryBuilderFactory implements TimeRuleAnalysisQueryRequestFactory {


    @Override
    public <T, V> QuerySpecialParams buildTimeRuleAnalysisQueryRequest(T request, V param) {
        TimeRuleAnalysisRequest timeRuleAnalysisRequest = (TimeRuleAnalysisRequest) request;
        String caseId = param.toString();
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        combinationQueryParams.setType(ConditionType.must);
        // 指定caseId
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.CASE_ID, caseId));

        // 指定日期范围
        if (null != timeRuleAnalysisRequest.getDateRange() && StringUtils.isNotBlank(timeRuleAnalysisRequest.getDateRange().getStart())
                & StringUtils.isNotBlank(timeRuleAnalysisRequest.getDateRange().getEnd())
        ) {
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.range, FundTacticsAnalysisField.TRADING_TIME
                    , new DateRange(timeRuleAnalysisRequest.getDateRange().getStart()
                    , timeRuleAnalysisRequest.getDateRange().getEnd())));
        }

        List<String> destCards = timeRuleAnalysisRequest.getDest();
        // 如果选择的对端卡号不为空 需要指定对端卡号
        if (!CollectionUtils.isEmpty(destCards)) {
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.terms, FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, destCards));
        }
        // 指定选择的本方卡号
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.terms, FundTacticsAnalysisField.QUERY_CARD, timeRuleAnalysisRequest.getSource()));
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        return querySpecialParams;


    }

    @Override
    public <T, V> QuerySpecialParams buildTimeRuleAnalysisDetailQueryRequest(T request, V param ,List<String> festival) {
        TimeRuleAnalysisDetailRequest req = (TimeRuleAnalysisDetailRequest) request;
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        String caseId = param.toString();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.must);
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("festival",festival);
        if (req.getStatisticType().equals(StatisticType.SINGLE)) {
            setDateRange(req.getTradingTime(), req.getDateType(), filter);
        } else {
            // 这里的tradingTime 页面展示的是如：第50周,而下面函数入参第一个参数为:50 需要处理replaceaALL处理tradingTime. (暂时后台未处理)
            gengerateScriptQuery(req,paramMap, filter);
        }
        // 案件Id 过滤
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));

        // 合并卡号
        List<String> mergeCards = req.getDest() != null ? merge(req.getSource(), req.getDest()) : req.getSource();
        CombinationQueryParams cards = new CombinationQueryParams();
        cards.setType(ConditionType.should);
        // 指定本方和对方卡号
        cards.addCommonQueryParams(new CommonQueryParams(QueryType.terms, FundTacticsAnalysisField.QUERY_CARD, mergeCards));
        cards.addCommonQueryParams(new CommonQueryParams(QueryType.terms, FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, mergeCards));
        filter.addCombinationQueryParams(cards);
        String keyword = req.getQueryRequest().getKeyword();

        /**
         * 构建模糊查询参数
         * */
        if (StringUtils.isNotBlank(keyword)) {

            // 增加模糊查询条件
            if (StringUtils.isNotBlank(req.getQueryRequest().getKeyword())) {
                CombinationQueryParams localFuzzy = assembleLocalFuzzy(req.getQueryRequest().getKeyword());
                CombinationQueryParams oppositeFuzzy = assembleOppositeFuzzy(req.getQueryRequest().getKeyword());
                localFuzzy.getCommonQueryParams().addAll(oppositeFuzzy.getCommonQueryParams());
                filter.addCommonQueryParams(new CommonQueryParams(localFuzzy));
            }

        }

        PagingRequest pageRequest = req.getQueryRequest().getPaging();
        if (null != pageRequest) {
            querySpecialParams.setPagination(new Pagination(pageRequest.getPage(), pageRequest.getPageSize()));
        }
        SortingRequest sortRequest = req.getQueryRequest().getSorting();
        /**
         * 默认按照时间排序
         * */
        if (null != sortRequest) {
            querySpecialParams.setSort(new FieldSort(req.getQueryRequest().getSorting().getProperty() == null ?
                    FundTacticsAnalysisField.TRADING_TIME : sortRequest.getProperty(),
                    sortRequest.getOrder().name()));
        }

        querySpecialParams.addCombiningQueryParams(filter);
        return querySpecialParams;
    }

    /**
     * 构建range 查询
     *
     * @param tradingTime: 时间日期
     * @param dateType:    日期类型
     * @param filter:      父查询参数
     * @return: void
     **/
    private void setDateRange(String tradingTime, String dateType, CombinationQueryParams filter) {
        DateRange dateRange = new DateRange(tradingTime, tradingTime);
        String format = FundDateRequest.convertFromTimeType(dateType);
        dateRange.setFormat(format);
        filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRADING_TIME, dateRange));
    }

    /**
     * 构建script查询
     *
     * @param filter: 父查询参数
     * @return: java.lang.Object
     **/
    private void gengerateScriptQuery(TimeRuleAnalysisDetailRequest request, Map<String,Object> map,CombinationQueryParams filter) {
        filter.addCommonQueryParams(QueryParamsBuilders.script(request.getScriptCondition(),map));
    }


    /**
     * 合并两个list
     *
     * @param first:  第一个list数据
     * @param second: 第一个list 数据
     * @return: java.util.List<T>
     **/
    public static <T> List<T> merge(List<T> first, List<T> second) {
        return Stream.of(first, second).flatMap(x -> x.stream()).collect(Collectors.toList());
    }

}
