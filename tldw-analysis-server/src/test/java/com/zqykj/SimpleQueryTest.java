package com.zqykj;


import com.zqykj.app.service.interfaze.IIndividualPortraitStatistics;
import com.zqykj.app.service.interfaze.ISingleCardPortraitStatistics;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.common.vo.Direction;
import com.zqykj.common.vo.SortRequest;
import com.zqykj.infrastructure.core.ServerResponse;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.query.DateRange;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
     * ????????????????????????
     */
    @Test
    public void testExistIndex() {
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        try {
            boolean exists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
            if (exists) {
                log.info("?????????{} ??????", indexName);
            } else {
                log.info("?????????{} ?????????", indexName);
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
        // ??????????????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // ???????????? ????????????QueryBuilders???????????????
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
     * "customer_name": "?????????"
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
//        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("customer_name", "?????????");
//        SearchSourceBuilder query = searchSourceBuilder.query(matchQueryBuilder);

        // term query
      /*  TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("customer_name", "?????????");
        SearchSourceBuilder query = searchSourceBuilder.query(termQueryBuilder);*/

        // terms query
        String[] values = {"?????????", "?????????"};
        TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery("customer_name", values);
        SearchSourceBuilder query = searchSourceBuilder.query(termsQueryBuilder);

        searchSourceBuilder.timeout(new TimeValue(20, TimeUnit.SECONDS));
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(3);

        // ????????????
        searchRequest.source(query);
        // ????????????
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));

        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            // ????????????
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
        // multi_match "60138216660030800" query_card???transaction_opposite_card
        String[] fields = {"query_card", "transaction_opposite_card"};
        MultiMatchQueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery(singleCardPortraitRequest.getQueryCard(), fields);
        boolQueryBuilder.filter(caseIdTermQuery);
        boolQueryBuilder.filter(multiMatchQuery);
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.size(1);

        // aggregation
        // ??????????????????
        MinAggregationBuilder earliestTradingTimeBuilder = AggregationBuilders.min("earliestTradingTime").field("trading_time");
        // ??????????????????
        MaxAggregationBuilder latestTradingTimeBuilder = AggregationBuilders.max("latestTradingTime").field("trading_time");
        searchSourceBuilder.aggregation(earliestTradingTimeBuilder);
        searchSourceBuilder.aggregation(latestTradingTimeBuilder);

        // ??????????????????????????????termsAggregationBuilder
        String[] localIncludeCards = Collections.singletonList(queryCard).toArray(new String[0]);
        IncludeExclude localIncludeExclude = new IncludeExclude(localIncludeCards, null);
        TermsAggregationBuilder localCardTermsBuilder = AggregationBuilders
                .terms("localCardTerms")
                .field("query_card")
                .includeExclude(localIncludeExclude)
                .size(10);
        // ????????????????????????????????????????????????
        FilterAggregationBuilder localInTransactionMoneySumBuilder = AggregationBuilders.filter("localInTransactionMoneySum", QueryBuilders.termQuery("loan_flag", "???"))
                .subAggregation(AggregationBuilders.sum("localTransactionMoneySum").field("transaction_money"));
        // ????????????????????????????????????????????????
        FilterAggregationBuilder localOutTransactionMoneySumBuilder = AggregationBuilders.filter("localOutTransactionMoneySum", QueryBuilders.termQuery("loan_flag", "???"))
                .subAggregation(AggregationBuilders.sum("localTransactionMoneySum").field("transaction_money"));
        // ??????????????????????????????????????????
        SumAggregationBuilder localTotalTransactionMoneySumBuilder = AggregationBuilders.sum("localTotalTransactionMoneySum").field("transaction_money");
        // ????????????????????????????????????????????????????????????????????????????????????top_hits???size=1
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

        // ??????????????????????????????termsAggregationBuilder
        String[] oppositeIncludeCards = Collections.singletonList(queryCard).toArray(new String[0]);
        IncludeExclude oppositeIncludeExclude = new IncludeExclude(oppositeIncludeCards, null);
        TermsAggregationBuilder oppositeCardTermsBuilder = AggregationBuilders
                .terms("oppositeCardTerms")
                .field("transaction_opposite_card")
                .includeExclude(oppositeIncludeExclude)
                .size(10);
        // ????????????????????????????????????????????????
        FilterAggregationBuilder oppositeInTransactionMoneySumBuilder = AggregationBuilders.filter("oppositeInTransactionMoneySum", QueryBuilders.termQuery("loan_flag", "???"))
                .subAggregation(AggregationBuilders.sum("oppositeTransactionMoneySum").field("transaction_money"));
        // ????????????????????????????????????????????????
        FilterAggregationBuilder oppositeOutTransactionMoneySumBuilder = AggregationBuilders.filter("oppositeOutTransactionMoneySum", QueryBuilders.termQuery("loan_flag", "???"))
                .subAggregation(AggregationBuilders.sum("oppositeTransactionMoneySum").field("transaction_money"));
        // ??????????????????????????????????????????
        SumAggregationBuilder oppositeTotalTransactionMoneySumBuilder = AggregationBuilders.sum("oppositeTotalTransactionMoneySum").field("transaction_money");
        // ????????????????????????????????????????????????????????????????????????????????????top_hits???size=1
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

        // ????????????
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
                log.info("??????????????????????????????localInTransactionMoneySum???:{}", parsedSum.getValue());

                ParsedFilter parsedFilter2 = aggregations1.get("localOutTransactionMoneySum");
                ParsedSum parsedSum2 = parsedFilter2.getAggregations().get("localTransactionMoneySum");
                map.put("localOutTransactionMoneySum", parsedSum2.getValue());
                log.info("??????????????????????????????localOutTransactionMoneySum???:{}", parsedSum2.getValue());

                ParsedSum parsedSum3 = aggregations1.get("localTotalTransactionMoneySum");
                map.put("localTotalTransactionMoneySum", parsedSum3.getValue());
                log.info("????????????????????????localTotalTransactionMoneySum???:{}", parsedSum3.getValue());

                ParsedTopHits localTopHits = aggregations1.get("localTopHits");
                SearchHit[] localHits = localTopHits.getHits().getHits();
                Map<String, Object> localSourceAsMap = localHits[0].getSourceAsMap();
                // ?????????????????????????????????
                String localLatestTradingTime = (String) localSourceAsMap.get("trading_time");
                // ?????????????????????????????????
                Double localLatestTransactionBalance = (Double) localSourceAsMap.get("transaction_balance");
                map.put("localLatestTradingTime", localLatestTradingTime);
                map.put("localLatestTransactionBalance", localLatestTransactionBalance);

                map.put("customerName", localSourceAsMap.get("customer_name"));
                map.put("customerIdentityCard", localSourceAsMap.get("customer_identity_card"));
                map.put("queryAccount", localSourceAsMap.get("query_account"));
                map.put("bank", localSourceAsMap.get("bank"));
            });

            ParsedTerms oppositeCardParsedTerms = aggregations.get("oppositeCardTerms");
            // ????????????????????????
            if (CollectionUtils.isEmpty(oppositeCardParsedTerms.getBuckets())) {
                map.put("oppositeInTransactionMoneySum", 0.0);
                log.info("??????????????????????????????oppositeInTransactionMoneySum???:{}", 0.0);

                map.put("oppositeOutTransactionMoneySum", 0.0);
                log.info("??????????????????????????????oppositeOutTransactionMoneySum???:{}", 0.0);

                map.put("oppositeTotalTransactionMoneySum", 0.0);
                log.info("????????????????????????oppositeTotalTransactionMoneySum???:{}", 0.0);

//                map.put("oppositeLatestTradingTime", oppositeLatestTradingTime);
//                map.put("oppositeLatestTransactionBalance", oppositeLatestTransactionBalance);
            } else {
                oppositeCardParsedTerms.getBuckets().forEach(localCardBucket -> {
                    Aggregations aggregations1 = localCardBucket.getAggregations();
                    ParsedFilter parsedFilter = aggregations1.get("oppositeInTransactionMoneySum");
                    ParsedSum parsedSum = parsedFilter.getAggregations().get("oppositeTransactionMoneySum");
                    map.put("oppositeInTransactionMoneySum", parsedSum.getValue());
                    log.info("??????????????????????????????oppositeInTransactionMoneySum???:{}", parsedSum.getValue());

                    ParsedFilter parsedFilter2 = aggregations1.get("oppositeOutTransactionMoneySum");
                    ParsedSum parsedSum2 = parsedFilter2.getAggregations().get("oppositeTransactionMoneySum");
                    map.put("oppositeOutTransactionMoneySum", parsedSum2.getValue());
                    log.info("??????????????????????????????oppositeOutTransactionMoneySum???:{}", parsedSum2.getValue());

                    ParsedSum parsedSum3 = aggregations1.get("oppositeTotalTransactionMoneySum");
                    map.put("oppositeTotalTransactionMoneySum", parsedSum3.getValue());
                    log.info("????????????????????????oppositeTotalTransactionMoneySum???:{}", parsedSum3.getValue());

                    ParsedTopHits oppositeTopHits = aggregations1.get("oppositeTopHits");
                    SearchHit[] oppositeHits = oppositeTopHits.getHits().getHits();
                    SearchHit oppositeHit = oppositeHits[0];
                    // ?????????????????????????????????
                    String oppositeLatestTradingTime = (String) oppositeHit.getSourceAsMap().get("trading_time");
                    // ?????????????????????????????????
                    Double oppositeLatestTransactionBalance = (Double) oppositeHit.getSourceAsMap().get("transaction_opposite_balance");
                    map.put("oppositeLatestTradingTime", oppositeLatestTradingTime);
                    map.put("oppositeLatestTransactionBalance", oppositeLatestTransactionBalance);
                });
            }


            Double localTotalTransactionMoneySum = (Double) map.get("localTotalTransactionMoneySum");
            Double oppositeTotalTransactionMoneySum = (Double) map.get("oppositeTotalTransactionMoneySum");
            // ????????????
            Double totalTransactionMoney = localTotalTransactionMoneySum + oppositeTotalTransactionMoneySum;
            // ???????????? = ??????????????????????????? + ???????????????????????????
            Double totalInTransactionMoney = (Double) map.get("localInTransactionMoneySum") + (Double) map.get("oppositeOutTransactionMoneySum");
            // ???????????? = ???????????????????????? + ???????????????????????????
            Double totalOutTransactionMoney = (Double) map.get("localOutTransactionMoneySum") + (Double) map.get("oppositeInTransactionMoneySum");
            // ????????????
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
            log.info("?????????{}", queryCard);
            log.info("??????:{}", queryAccount);
            log.info("????????????:{}", bank);
            log.info("????????????:{}", customerName);
            log.info("????????????:{}", customerIdentityCard);
            log.info("????????????:{}", "");
            log.info("??????????????????:{}", earliestTradingTime);
            log.info("??????????????????:{}", latestTradingTime);
            log.info("???????????????{}", totalTransactionMoney);
            log.info("???????????????{}", totalInTransactionMoney);
            log.info("???????????????{}", totalOutTransactionMoney);
            log.info("???????????????{}", transactionBalance);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ????????????????????????
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
     * ????????????JSON?????????
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
     * "bank": "??????????????????",
     * "transaction_balance": 69320.62,
     * "query_card": "60138216660030800",
     * "query_account": "322125198702200000",
     * "trading_time": "2019-04-06 03:20:49",
     * "customer_identity_card": "322125198702200000",
     * "customer_name": "?????????"
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
