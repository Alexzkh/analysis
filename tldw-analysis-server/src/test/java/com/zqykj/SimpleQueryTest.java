package com.zqykj;


import com.zqykj.app.service.interfaze.IIndividualPortraitStatistics;
import com.zqykj.app.service.interfaze.ISingleCardPortraitStatistics;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.vo.Direction;
import com.zqykj.common.vo.SortRequest;
import com.zqykj.domain.Page;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.parameters.FieldSort;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.DateRange;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter;
import org.elasticsearch.search.aggregations.bucket.terms.IncludeExclude;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@SpringBootTest
@Slf4j
public class SimpleQueryTest {
    private static final String indexName = "bank_transaction_flow";
    private static final String indexId = "907673916787392515";
    private static final String type = "_doc";

    @Resource
    private RestHighLevelClient restHighLevelClient;
    @Resource
    private EntranceRepository entranceRepository;

    @Resource
    private ISingleCardPortraitStatistics iSingleCardPortraitStatistics;

    @Resource
    private IIndividualPortraitStatistics iIndividualPortraitStatistics;

    /**
     * GET /bank_transaction_flow/_doc/907673916787392515
     */
    @Test
    public void queryByIndex() {
        try {
            GetRequest getRequest = new GetRequest(indexName, indexId);
            GetResponse documentFields = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
            // getSourceAsMap()
            // Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
            // getSource()
            // Map<String, Object> source = documentFields.getSource();
            // getSourceAsString()
            String sourceAsString = documentFields.getSourceAsString();
            log.info(sourceAsString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断索引是否存在
     */
    @Test
    public void testExistIndex() {
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        try {
            boolean exists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
            if (exists) {
                log.info("索引：{} 存在", indexName);
            } else {
                log.info("索引：{} 不存在", indexName);
            }
        } catch (IOException e) {
            log.error("error:", e);
        }
    }

    /**
     * GET /bank_transaction_flow/_search
     * {
     * "query": {
     * "match_all": {}
     * },
     * "from": 0,
     * "size": 5
     * }
     */
    @Test
    public void testMatchAll() {
        SearchRequest searchRequest = new SearchRequest(indexName);
        // 构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 查询条件 可以使用QueryBuilders工具来实现
        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        searchSourceBuilder.query(matchAllQueryBuilder);
        searchSourceBuilder.timeout(new TimeValue(20, TimeUnit.SECONDS));
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(2);
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                log.info(sourceAsString);
            }
        } catch (IOException e) {
            log.error("e:", e);
        }
    }

    /**
     * GET /bank_transaction_flow/_search
     * {
     * "query": {
     * "match": {
     * "customer_name": "苗若兰"
     * }
     * },
     * "from": 0,
     * "size": 1
     * }
     */
    @Test
    public void testMatchQuery() {
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // match query
//        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("customer_name", "苗若兰");
//        SearchSourceBuilder query = searchSourceBuilder.query(matchQueryBuilder);

        // term query
      /*  TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("customer_name", "苗若兰");
        SearchSourceBuilder query = searchSourceBuilder.query(termQueryBuilder);*/

        // terms query
        String[] values = {"苗若兰", "马行空"};
        TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery("customer_name", values);
        SearchSourceBuilder query = searchSourceBuilder.query(termsQueryBuilder);

        searchSourceBuilder.timeout(new TimeValue(20, TimeUnit.SECONDS));
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(3);

        // 执行查询
        searchRequest.source(query);
        // 滚动设置
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));

        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            // 解析结果
            String scrollId = searchResponse.getScrollId();
            long value = searchResponse.getHits().getTotalHits().value;
            TotalHits.Relation relation = searchResponse.getHits().getTotalHits().relation;
            SearchHit[] hits = searchResponse.getHits().getHits();
            log.info("result.value:{},result.relation:{},scrollId:{}", value, relation, scrollId);
            for (SearchHit hit : hits) {
                log.info(hit.getSourceAsString());
            }
        } catch (IOException e) {
            log.error("e", e);
        }
    }

    /**
     * GET /bank_transaction_flow/_search
     * {
     * "from": 0,
     * "size": 1,
     * "query": {
     * "bool": {
     * "must": [
     * {
     * "term": {
     * "case_id": {
     * "value": "3eeeab0b01a541eda6268a9370393cf5",
     * "boost": 1
     * }
     * }
     * },
     * {
     * "term": {
     * "query_card": {
     * "value": "60138216660030800",
     * "boost": 1
     * }
     * }
     * }
     * ],
     * "adjust_pure_negative": true,
     * "boost": 1
     * }
     * }
     * }
     */
    @Test
    public void boolQueryTest() {
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder ssb = new SearchSourceBuilder();

        String matchNameVar1 = "case_id";
        String matchValue1 = "3eeeab0b01a541eda6268a9370393cf5";
        String matchNameVar2 = "query_card";
        String matchValue2 = "60138216660030800";
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        boolQueryBuilder.must(QueryBuilders.termQuery(matchNameVar1, matchValue1));
        boolQueryBuilder.must(QueryBuilders.termQuery(matchNameVar2, matchValue2));
        boolQueryBuilder.filter(QueryBuilders.termQuery("transaction_opposite_card", "60138216660017566"));

        SearchSourceBuilder query = ssb.query(boolQueryBuilder);

        String sumTransactionMoneyAlias = "sumTransactionMoney";
        String sumField = "transaction_money";
        SumAggregationBuilder sumAggregationBuilder = AggregationBuilders.sum(sumTransactionMoneyAlias).field(sumField);
        SearchSourceBuilder aggregation = ssb.aggregation(sumAggregationBuilder);

        ssb.from(0);
        ssb.size(1);

        searchRequest.source(query);
        searchRequest.source(aggregation);

        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            long value = hits.getTotalHits().value;
            TotalHits.Relation relation = hits.getTotalHits().relation;
            log.info("total:{}", value);
            log.info("relation:{}", relation);
            for (SearchHit hit : hits.getHits()) {
                log.info(hit.getSourceAsString());
            }

            Aggregations aggregations = searchResponse.getAggregations();
            ParsedSum parsedSum = aggregations.get("sumTransactionMoney");
            String parsedSumName = parsedSum.getName();
            double parsedSumValue = parsedSum.getValue();
            System.out.println(parsedSumName + ":" + parsedSumValue);
        } catch (IOException e) {
            log.error("e:", e);
        }
    }

    @Test
    public void specialQuery() {
        String routing = "3eeeab0b01a541eda6268a9370393cf5";
        String termField = "case_id";
        String termValue = "3eeeab0b01a541eda6268a9370393cf5";
        String multiMatchQuery = "60138216660047600";
        String[] multiMatchFields = {"query_card", "transaction_opposite_card"};

        QuerySpecialParams querySpecialParams = new QuerySpecialParams();

        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        combinationQueryParams.setType(ConditionType.filter);

        // case_id="3eeeab0b01a541eda6268a9370393cf5"
        CommonQueryParams termCaseIdCommonQueryParams = QueryParamsBuilders.term(termField, termValue);
        combinationQueryParams.addCommonQueryParams(termCaseIdCommonQueryParams);

        // query_card="60138216660047600" OR transaction_opposite_card="60138216660047600"
//        CommonQueryParams multiMatchCommonQueryParams = new CommonQueryParams(QueryType.multi_match,multiMatchQuery,multiMatchFields);
        CommonQueryParams multiMatchCommonQueryParams = QueryParamsBuilders.multiMatch(multiMatchQuery, multiMatchFields);
        combinationQueryParams.addCommonQueryParams(multiMatchCommonQueryParams);

        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        querySpecialParams.setPagination(new Pagination(0, 1));
        querySpecialParams.setSort(new FieldSort("trading_time", "desc"));

        Page<BankTransactionFlow> bankTransactionFlows = entranceRepository.compoundQueryWithoutAgg(null, querySpecialParams,
                BankTransactionFlow.class, routing);
        List<BankTransactionFlow> content = bankTransactionFlows.getContent();
    }

    @Test
    public void querySingleCardPortrait() {
        String indexName = "bank_transaction_flow";
        String caseId = "3eeeab0b01a541eda6268a9370393cf5";
//        String queryCard = "60138216660030800";
        String queryCard = "60138216660016818";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd mm:HH:ss");

        SearchRequest searchRequest = new SearchRequest(indexName);

        SingleCardPortraitRequest singleCardPortraitRequest = SingleCardPortraitRequest.builder().caseId(caseId).queryCard(queryCard).build();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // term case_id
        TermQueryBuilder caseIdTermQuery = QueryBuilders.termQuery("case_id", singleCardPortraitRequest.getCaseId());
        // multi_match "60138216660030800" query_card、transaction_opposite_card
        String[] fields = {"query_card", "transaction_opposite_card"};
        MultiMatchQueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery(singleCardPortraitRequest.getQueryCard(), fields);
        boolQueryBuilder.filter(caseIdTermQuery);
        boolQueryBuilder.filter(multiMatchQuery);
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.size(1);

        // aggregation
        // 最早交易时间
        MinAggregationBuilder earliestTradingTimeBuilder = AggregationBuilders.min("earliestTradingTime").field("trading_time");
        // 最晚交易时间
        MaxAggregationBuilder latestTradingTimeBuilder = AggregationBuilders.max("latestTradingTime").field("trading_time");
        searchSourceBuilder.aggregation(earliestTradingTimeBuilder);
        searchSourceBuilder.aggregation(latestTradingTimeBuilder);

        // 作为本方，卡号分桶：termsAggregationBuilder
        String[] localIncludeCards = Collections.singletonList(queryCard).toArray(new String[0]);
        IncludeExclude localIncludeExclude = new IncludeExclude(localIncludeCards, null);
        TermsAggregationBuilder localCardTermsBuilder = AggregationBuilders
                .terms("localCardTerms")
                .field("query_card")
                .includeExclude(localIncludeExclude)
                .size(10);
        // 作为本方，当前查询卡号的进账金额
        FilterAggregationBuilder localInTransactionMoneySumBuilder = AggregationBuilders.filter("localInTransactionMoneySum", QueryBuilders.termQuery("loan_flag", "进"))
                .subAggregation(AggregationBuilders.sum("localTransactionMoneySum").field("transaction_money"));
        // 作为本方，当前查询卡号的出账金额
        FilterAggregationBuilder localOutTransactionMoneySumBuilder = AggregationBuilders.filter("localOutTransactionMoneySum", QueryBuilders.termQuery("loan_flag", "出"))
                .subAggregation(AggregationBuilders.sum("localTransactionMoneySum").field("transaction_money"));
        // 作为本方，当前卡号交易总金额
        SumAggregationBuilder localTotalTransactionMoneySumBuilder = AggregationBuilders.sum("localTotalTransactionMoneySum").field("transaction_money");
        // 作为本方，按交易时间降序，目的为了获取最晚交易余额，当前top_hits，size=1
        String[] localIncludes = {
                "customer_name",
                "customer_identity_card",
                "query_account",
                "query_card",
                "bank",
                "transaction_balance",
                "trading_time"};
        TopHitsAggregationBuilder localTopHitsAggregationBuilder = AggregationBuilders.topHits("localTopHits")
                .fetchSource(localIncludes, null).sort("trading_time", SortOrder.DESC).size(1);
        localCardTermsBuilder.subAggregation(localInTransactionMoneySumBuilder);
        localCardTermsBuilder.subAggregation(localOutTransactionMoneySumBuilder);
        localCardTermsBuilder.subAggregation(localTotalTransactionMoneySumBuilder);
        localCardTermsBuilder.subAggregation(localTopHitsAggregationBuilder);
        searchSourceBuilder.aggregation(localCardTermsBuilder);

        // 作为对方，卡号分桶：termsAggregationBuilder
        String[] oppositeIncludeCards = Collections.singletonList(queryCard).toArray(new String[0]);
        IncludeExclude oppositeIncludeExclude = new IncludeExclude(oppositeIncludeCards, null);
        TermsAggregationBuilder oppositeCardTermsBuilder = AggregationBuilders
                .terms("oppositeCardTerms")
                .field("transaction_opposite_card")
                .includeExclude(oppositeIncludeExclude)
                .size(10);
        // 作为对方，当前查询卡号的进账金额
        FilterAggregationBuilder oppositeInTransactionMoneySumBuilder = AggregationBuilders.filter("oppositeInTransactionMoneySum", QueryBuilders.termQuery("loan_flag", "进"))
                .subAggregation(AggregationBuilders.sum("oppositeTransactionMoneySum").field("transaction_money"));
        // 作为对方，当前查询卡号的出账金额
        FilterAggregationBuilder oppositeOutTransactionMoneySumBuilder = AggregationBuilders.filter("oppositeOutTransactionMoneySum", QueryBuilders.termQuery("loan_flag", "出"))
                .subAggregation(AggregationBuilders.sum("oppositeTransactionMoneySum").field("transaction_money"));
        // 作为对方，当前卡号交易总金额
        SumAggregationBuilder oppositeTotalTransactionMoneySumBuilder = AggregationBuilders.sum("oppositeTotalTransactionMoneySum").field("transaction_money");
        // 作为对方，按交易时间降序，目的为了获取最晚交易余额，当前top_hits，size=1
        String[] oppositeIncludes = {
                "transaction_opposite_name",
                "transaction_opposite_certificate_number",
                "transaction_opposite_account",
                "transaction_opposite_card",
                "transaction_opposite_account_open_bank",
                "transaction_opposite_balance",
                "trading_time"};
        TopHitsAggregationBuilder oppositeTopHitsAggregationBuilder = AggregationBuilders.topHits("oppositeTopHits")
                .fetchSource(oppositeIncludes, null).sort("trading_time", SortOrder.DESC).size(1);

        oppositeCardTermsBuilder.subAggregation(oppositeInTransactionMoneySumBuilder);
        oppositeCardTermsBuilder.subAggregation(oppositeOutTransactionMoneySumBuilder);
        oppositeCardTermsBuilder.subAggregation(oppositeTotalTransactionMoneySumBuilder);
        oppositeCardTermsBuilder.subAggregation(oppositeTopHitsAggregationBuilder);
        searchSourceBuilder.aggregation(oppositeCardTermsBuilder);

        // 查询执行
        searchRequest.source(searchSourceBuilder);

        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            log.info("total.value:{},total.relation:{}", hits.getTotalHits().value, hits.getTotalHits().relation);

            Aggregations aggregations = searchResponse.getAggregations();
            ParsedMin parsedMin = aggregations.get("earliestTradingTime");
            ParsedMax parsedMax = aggregations.get("latestTradingTime");
            String earliestTradingTime = parsedMin.getValueAsString();
            String latestTradingTime = parsedMax.getValueAsString();

            Map<String, Object> map = new LinkedHashMap<>(16);
            ParsedTerms parsedTerms = aggregations.get("localCardTerms");
            parsedTerms.getBuckets().forEach(localCardBucket -> {
                Aggregations aggregations1 = localCardBucket.getAggregations();
                ParsedFilter parsedFilter = aggregations1.get("localInTransactionMoneySum");
                ParsedSum parsedSum = parsedFilter.getAggregations().get("localTransactionMoneySum");
                map.put("localInTransactionMoneySum", parsedSum.getValue());
                log.info("本方进账交易总金额【localInTransactionMoneySum】:{}", parsedSum.getValue());

                ParsedFilter parsedFilter2 = aggregations1.get("localOutTransactionMoneySum");
                ParsedSum parsedSum2 = parsedFilter2.getAggregations().get("localTransactionMoneySum");
                map.put("localOutTransactionMoneySum", parsedSum2.getValue());
                log.info("本方出账交易总金额【localOutTransactionMoneySum】:{}", parsedSum2.getValue());

                ParsedSum parsedSum3 = aggregations1.get("localTotalTransactionMoneySum");
                map.put("localTotalTransactionMoneySum", parsedSum3.getValue());
                log.info("本方交易总金额【localTotalTransactionMoneySum】:{}", parsedSum3.getValue());

                ParsedTopHits localTopHits = aggregations1.get("localTopHits");
                SearchHit[] localHits = localTopHits.getHits().getHits();
                Map<String, Object> localSourceAsMap = localHits[0].getSourceAsMap();
                // 作为本方，最晚交易时间
                String localLatestTradingTime = (String) localSourceAsMap.get("trading_time");
                // 作为本方，最晚交易余额
                Double localLatestTransactionBalance = (Double) localSourceAsMap.get("transaction_balance");
                map.put("localLatestTradingTime", localLatestTradingTime);
                map.put("localLatestTransactionBalance", localLatestTransactionBalance);

                map.put("customerName", localSourceAsMap.get("customer_name"));
                map.put("customerIdentityCard", localSourceAsMap.get("customer_identity_card"));
                map.put("queryAccount", localSourceAsMap.get("query_account"));
                map.put("bank", localSourceAsMap.get("bank"));
            });

            ParsedTerms oppositeCardParsedTerms = aggregations.get("oppositeCardTerms");
            // 如果是自已的账号
            if (CollectionUtils.isEmpty(oppositeCardParsedTerms.getBuckets())) {
                map.put("oppositeInTransactionMoneySum", 0.0);
                log.info("对方进账交易总金额【oppositeInTransactionMoneySum】:{}", 0.0);

                map.put("oppositeOutTransactionMoneySum", 0.0);
                log.info("对方出账交易总金额【oppositeOutTransactionMoneySum】:{}", 0.0);

                map.put("oppositeTotalTransactionMoneySum", 0.0);
                log.info("对方交易总金额【oppositeTotalTransactionMoneySum】:{}", 0.0);

//                map.put("oppositeLatestTradingTime", oppositeLatestTradingTime);
//                map.put("oppositeLatestTransactionBalance", oppositeLatestTransactionBalance);
            } else {
                oppositeCardParsedTerms.getBuckets().forEach(localCardBucket -> {
                    Aggregations aggregations1 = localCardBucket.getAggregations();
                    ParsedFilter parsedFilter = aggregations1.get("oppositeInTransactionMoneySum");
                    ParsedSum parsedSum = parsedFilter.getAggregations().get("oppositeTransactionMoneySum");
                    map.put("oppositeInTransactionMoneySum", parsedSum.getValue());
                    log.info("对方进账交易总金额【oppositeInTransactionMoneySum】:{}", parsedSum.getValue());

                    ParsedFilter parsedFilter2 = aggregations1.get("oppositeOutTransactionMoneySum");
                    ParsedSum parsedSum2 = parsedFilter2.getAggregations().get("oppositeTransactionMoneySum");
                    map.put("oppositeOutTransactionMoneySum", parsedSum2.getValue());
                    log.info("对方出账交易总金额【oppositeOutTransactionMoneySum】:{}", parsedSum2.getValue());

                    ParsedSum parsedSum3 = aggregations1.get("oppositeTotalTransactionMoneySum");
                    map.put("oppositeTotalTransactionMoneySum", parsedSum3.getValue());
                    log.info("对方交易总金额【oppositeTotalTransactionMoneySum】:{}", parsedSum3.getValue());

                    ParsedTopHits oppositeTopHits = aggregations1.get("oppositeTopHits");
                    SearchHit[] oppositeHits = oppositeTopHits.getHits().getHits();
                    SearchHit oppositeHit = oppositeHits[0];
                    // 作为对方，最晚交易时间
                    String oppositeLatestTradingTime = (String) oppositeHit.getSourceAsMap().get("trading_time");
                    // 作为对方，最晚交易余额
                    Double oppositeLatestTransactionBalance = (Double) oppositeHit.getSourceAsMap().get("transaction_opposite_balance");
                    map.put("oppositeLatestTradingTime", oppositeLatestTradingTime);
                    map.put("oppositeLatestTransactionBalance", oppositeLatestTransactionBalance);
                });
            }


            Double localTotalTransactionMoneySum = (Double) map.get("localTotalTransactionMoneySum");
            Double oppositeTotalTransactionMoneySum = (Double) map.get("oppositeTotalTransactionMoneySum");
            // 交易金额
            Double totalTransactionMoney = localTotalTransactionMoneySum + oppositeTotalTransactionMoneySum;
            // 进账金额 = 作为本方卡进账金额 + 作为对方卡出账金额
            Double totalInTransactionMoney = (Double) map.get("localInTransactionMoneySum") + (Double) map.get("oppositeOutTransactionMoneySum");
            // 出账金额 = 作为本方出账金额 + 作为对方卡进账金额
            Double totalOutTransactionMoney = (Double) map.get("localOutTransactionMoneySum") + (Double) map.get("oppositeInTransactionMoneySum");
            // 账户余额
            Double transactionBalance;
            Date localLatestTradingDate = convertStrToDate((String) map.get("localLatestTradingTime"), sdf);
            if (Objects.isNull(map.get("oppositeLatestTradingTime"))) {
                transactionBalance = (Double) map.get("localLatestTransactionBalance");
            } else {
                Date oppositeLatestTradingDate = convertStrToDate((String) map.get("oppositeLatestTradingTime"), sdf);
                transactionBalance = (Objects.requireNonNull(localLatestTradingDate).getTime() > (Objects.requireNonNull(oppositeLatestTradingDate).getTime()) ?
                        (Double) map.get("localLatestTransactionBalance") : (Double) map.get("oppositeLatestTransactionBalance"));
            }
            String customerName = (String) map.get("customerName");
            String customerIdentityCard = (String) map.get("customerIdentityCard");
            String queryAccount = (String) map.get("queryAccount");
            String bank = (String) map.get("bank");

            log.info("======================================================================================================");
            log.info("卡号：{}", queryCard);
            log.info("账户:{}", queryAccount);
            log.info("开户银行:{}", bank);
            log.info("客户名称:{}", customerName);
            log.info("身份证号:{}", customerIdentityCard);
            log.info("开户日期:{}", "");
            log.info("最早交易时间:{}", earliestTradingTime);
            log.info("最晚交易时间:{}", latestTradingTime);
            log.info("交易金额：{}", totalTransactionMoney);
            log.info("入账金额：{}", totalInTransactionMoney);
            log.info("出账金额：{}", totalOutTransactionMoney);
            log.info("账户余额：{}", transactionBalance);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 字符串转换成日期
     *
     * @param str
     * @param sdf
     * @return
     */
    public static Date convertStrToDate(String str, SimpleDateFormat sdf) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        Date date;
        try {
            date = sdf.parse(str);
            return date;
        } catch (ParseException e) {
            log.error("ParseException:", e);
        }
        return null;
    }


    /**
     * 返回结果JSON格式：
     * {
     * "earliest_trading_time": [
     * [
     * "2018-12-25 18:18:34"
     * ]
     * ],
     * "latest_trading_time": [
     * [
     * "2020-03-22 23:31:15"
     * ]
     * ],
     * "query_card_terms": [
     * [
     * 296336.1,
     * -292845.51,
     * [
     * {
     * "bank": "中国建设银行",
     * "transaction_balance": 69320.62,
     * "query_card": "60138216660030800",
     * "query_account": "322125198702200000",
     * "trading_time": "2019-04-06 03:20:49",
     * "customer_identity_card": "322125198702200000",
     * "customer_name": "马行空"
     * }
     * ]
     * ]
     * ]
     * }
     */
    @Test
    public void singleCardQuery() {
        String caseId = "bf72ac24e9aa4929a58ab35d699ef50f";
        String queryCard = "60138216660030800";
        SingleCardPortraitRequest singleCardPortraitRequest = SingleCardPortraitRequest.builder().caseId(caseId).queryCard(queryCard).build();
        ServerResponse<SingleCardPortraitResponse> singleCardPortraitResponseServerResponse = iSingleCardPortraitStatistics.accessSingleCardPortraitStatistics(singleCardPortraitRequest);
        log.info(JacksonUtils.toJson(singleCardPortraitResponseServerResponse));
    }

    @Test
    public void individualInfoAndStatisticsQuery() {
        String caseId = "bf72ac24e9aa4929a58ab35d699ef50f";
        String customerIdentityCard = "322125198702200000";
        IndividualInfoAndStatisticsRequest individualInfoAndStatisticsRequest = IndividualInfoAndStatisticsRequest.builder().caseId(caseId).customerIdentityCard(customerIdentityCard).build();
        ServerResponse<IndividualInfoAndStatisticsResponse> response = iIndividualPortraitStatistics.accessIndividualInfoAndStatistics(individualInfoAndStatisticsRequest);
        log.info(JacksonUtils.toJson(response));
    }

    @Test
    public void individualCardTransactionStatisticsQuery() {
        String caseId = "bf72ac24e9aa4929a58ab35d699ef50f";
        String customerIdentityCard = "322125198702200000";
        String[] cards = {"60138216660030800",
                "60138216660047600"};
        List<String> queryCards = Arrays.asList(cards);
        String keyword = "6013821666003";
        DateRange dateRange = new DateRange("2019-12-09", "2020-12-09");
        dateRange.setFormat("yyyy-MM-dd");
        Pagination pagination = new Pagination(0, 3);
        SortRequest sortRequest = new SortRequest("netTransactionMoney", Direction.DESC);
        IndividualCardTransactionStatisticsRequest request = IndividualCardTransactionStatisticsRequest.builder()
                .caseId(caseId)
                .customerIdentityCard(customerIdentityCard)
                .queryCards(queryCards)
                .dateRange(dateRange)
                .keyword(keyword)
                .pagination(pagination)
                .sortRequest(sortRequest)
                .build();
        ServerResponse<List<IndividualCardTransactionStatisticsResponse>> response = iIndividualPortraitStatistics.accessIndividualCardTransactionStatistics(request);
        log.info(JacksonUtils.toJson(response));
    }

}
