/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.query.fund;

import com.zqykj.app.service.factory.param.query.TransactionFieldQueryParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.vo.fund.FundTacticsPartGeneralRequest;
import com.zqykj.app.service.vo.fund.TransactionFieldAnalysisRequest;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.enums.QueryType;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.DateRange;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <h1> 交易字段分析查询参数构建 </h1>
 */
@Service
public class TransactionFieldQueryBuilder extends FundTacticsCommonQueryBuilder implements TransactionFieldQueryParamFactory {


    public QuerySpecialParams transactionFieldTypeQuery(TransactionFieldAnalysisRequest request) {

        QuerySpecialParams query = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        addCommonParams(filter, request);
        adjustCardsFilter(filter, request);
        // 关键字查询
        if (StringUtils.isNotBlank(request.getKeyword())) {
            filter.addCommonQueryParams(QueryParamsBuilders.terms(request.getStatisticsField(), request.getKeyword()));
        }
        query.addCombiningQueryParams(filter);
        return query;
    }

    public QuerySpecialParams fieldTypeCustomCollationQuery(TransactionFieldAnalysisRequest request, List<String> containFieldContent) {

        QuerySpecialParams query = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        addCommonParams(filter, request);
        // 统计字段,指定内容查询
        if (!CollectionUtils.isEmpty(containFieldContent)) {
            filter.addCommonQueryParams(QueryParamsBuilders.terms(request.getStatisticsField(), containFieldContent));
        }
        // 关键字查询
        if (StringUtils.isNotBlank(request.getKeyword())) {
            filter.addCommonQueryParams(QueryParamsBuilders.fuzzy(request.getStatisticsField(), request.getKeyword()));
        }
        query.addCombiningQueryParams(filter);
        return query;
    }

    public QuerySpecialParams fieldTypeStatisticsDetailQuery(TransactionFieldAnalysisRequest request, String... detailFuzzyFields) {
        QuerySpecialParams query = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        addCommonParams(filter, request);
        adjustCardsFilter(filter, request);
        // 统计字段,指定内容查询
        if (StringUtils.isNotBlank(request.getStatisticsFieldContent())) {
            filter.addCommonQueryParams(QueryParamsBuilders.terms(request.getStatisticsField(), request.getStatisticsFieldContent()));
        }
        // 增加模糊查询
        if (StringUtils.isNotBlank(request.getKeyword())) {
            CombinationQueryParams detailFuzzy = new CombinationQueryParams();
            detailFuzzy.setType(ConditionType.should);
            for (String fuzzyField : detailFuzzyFields) {
                detailFuzzy.addCommonQueryParams(new CommonQueryParams(QueryType.wildcard, fuzzyField, request.getKeyword()));
            }
            filter.addCombinationQueryParams(detailFuzzy);
        }
        query.addCombiningQueryParams(filter);
        return query;
    }

    private void addCommonParams(CombinationQueryParams filter, TransactionFieldAnalysisRequest request) {
        // 案件id
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, request.getCaseId()));
        // 日期筛选(开始时间-结束时间)
        DateRange dateRange = FundTacticsPartGeneralRequest.getDateRange(request.getDateRange());
        if (null != dateRange) {
            filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRADING_TIME, dateRange));
        }
    }

    private void adjustCardsFilter(CombinationQueryParams filter, TransactionFieldAnalysisRequest request) {
        // 选择个体查询的时候
        if (request.getAnalysisType() == SELECT_INDIVIDUAL) {
            String identityCard = StringUtils.isBlank(request.getIdentityCard()) ? "" : request.getIdentityCard();
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD, identityCard));
        }
        // 调单卡号条件筛选
        if (!CollectionUtils.isEmpty(request.getCardNum())) {
            CombinationQueryParams filterCards = new CombinationQueryParams(ConditionType.should);
            filterCards.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, request.getCardNum()));
            filterCards.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, request.getCardNum()));
            filter.addCombinationQueryParams(filterCards);
        }
    }
}
