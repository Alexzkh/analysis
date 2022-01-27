package com.zqykj;

import com.zqykj.app.service.factory.AggregationResultEntityParseFactory;
import com.zqykj.app.service.factory.TimeRuleAnalysisAggRequestParamFactory;
import com.zqykj.app.service.factory.TimeRuleAnalysisQueryRequestFactory;
import com.zqykj.app.service.interfaze.ITimeRuleAnalysisStatistics;
import com.zqykj.app.service.vo.fund.TimeRuleAnalysisDetailRequest;
import com.zqykj.app.service.vo.fund.TimeRuleAnalysisRequest;
import com.zqykj.app.service.vo.fund.TimeRuleAnalysisResult;
import com.zqykj.common.enums.LineChartDisplayType;
import com.zqykj.common.enums.StatisticType;
import com.zqykj.common.request.*;
import com.zqykj.domain.Page;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.domain.response.TimeRuleLineChartResponse;
import com.zqykj.domain.response.TimeRuleResultListResponse;
import com.zqykj.infrastructure.util.DateUtils;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @Description: 图路径分析测试类
 * @Author zhangkehou
 * @Date 2021/12/8
 */
@SpringBootTest
@Slf4j
public class TimeRuleAnalysisTest {

    @Autowired
    private EntranceRepository entranceRepository;

    @Autowired
    private TimeRuleAnalysisQueryRequestFactory timeRuleAnalysisQueryRequestFactory;

    @Autowired
    private TimeRuleAnalysisAggRequestParamFactory timeRuleAnalysisAggRequestParamFactory;

    @Autowired
    private AggregationResultEntityParseFactory aggregationResultEntityParseFactory;

    @Autowired
    private ITimeRuleAnalysisStatistics iTimeRuleAnalysisStatistics;

    @Test
    public void testTimeRuleAnalysis() {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");

        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("w");

        QuerySpecialParams querySpecialParams = timeRuleAnalysisQueryRequestFactory.buildTimeRuleAnalysisQueryRequest(analysisRequest, caseId);

        AggregationParams aggregationParams = timeRuleAnalysisAggRequestParamFactory.bulidTimeRuleAnalysisAggParams(analysisRequest, caseId);
        Map<String, List<List<Object>>> result = entranceRepository.compoundQueryAndAgg(querySpecialParams, aggregationParams, BankTransactionRecord.class, caseId);
        List<String> sourceOppositeTitles = new ArrayList<>(aggregationParams.getEntityAggColMapping().keySet());
        List<Map<String, Object>> localEntityMapping = aggregationResultEntityParseFactory.convertEntity(
                result.get(aggregationParams.getResultName()), sourceOppositeTitles, TimeRuleAnalysisResult.class);
        // 来源实体数据组装
        List<TimeRuleAnalysisResult> results = com.zqykj.util.JacksonUtils.parse(com.zqykj.util.JacksonUtils.toJson(localEntityMapping), new com.fasterxml.jackson.core.type.TypeReference<List<TimeRuleAnalysisResult>>() {
        });

        results.stream().forEach(a -> {
            try {
                System.out.println("第" + whatWeek(a.getTradingTime()) + "周");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });

        // 当请求的时间类型是24小时的时候 需要将各时间区段的值累加起来
        if (analysisRequest.getDateType().equals("h")) {

        }

        System.out.println();
    }


    /**
     * ------------------------------------- 时间规律分析-折线图结果单元测试 start -------------------------------------
     */

    // 按照统计方式为单一、时间周期为月、折线图统计方式为交易金额来计算
    @Test
    public void testTimeRuleLineChartResult单一_月_交易金额() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");
        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("M");
        analysisRequest.setStatisticType(StatisticType.SINGLE);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.TRANSACTION_MONEY);
        analysisRequest.setPaging(new PagingRequest(0, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleLineChartResponse response = iTimeRuleAnalysisStatistics.accessLineChartResult(analysisRequest, caseId);
        System.out.println("按照统计方式为单一、时间周期为月、折线图统计方式为交易金额来计算:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    // 按照统计方式为单一、时间周期为月、折线图统计方式为交易次数来计算
    @Test
    public void testTimeRuleLineChartResult单一_月_交易次数() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");
        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("M");
        analysisRequest.setStatisticType(StatisticType.SINGLE);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.NUMBER_OF_TRANSACTION);
        analysisRequest.setPaging(new PagingRequest(0, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleLineChartResponse response = iTimeRuleAnalysisStatistics.accessLineChartResult(analysisRequest, caseId);
        System.out.println("按照统计方式为单一、时间周期为月、折线图统计方式为交易次数来计算:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    // 按照统计方式为汇总、时间周期为月、折线图统计方式为交易金额来计算
    @Test
    public void testTimeRuleLineChartResult汇总_月_交易金额() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");
        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("M");
        analysisRequest.setStatisticType(StatisticType.SUMMARY);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.TRANSACTION_MONEY);
        analysisRequest.setPaging(new PagingRequest(0, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleLineChartResponse response = iTimeRuleAnalysisStatistics.accessLineChartResult(analysisRequest, caseId);
        System.out.println("按照统计方式为汇总、时间周期为月、折线图统计方式为交易金额来计算:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    // 按照统计方式为汇总、时间周期为月、折线图统计方式为交易次数来计算
    @Test
    public void testTimeRuleLineChartResult汇总_月_交易次数() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");
        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("M");
        analysisRequest.setStatisticType(StatisticType.SUMMARY);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.NUMBER_OF_TRANSACTION);
        analysisRequest.setPaging(new PagingRequest(0, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleLineChartResponse response = iTimeRuleAnalysisStatistics.accessLineChartResult(analysisRequest, caseId);
        System.out.println("按照统计方式为汇总、时间周期为周、折线图统计方式为交易次数来计算:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    // 按照统计方式为单一、时间周期为周、折线图统计方式为交易金额来计算
    @Test
    public void testTimeRuleLineChartResult_单一_周_交易金额() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");

        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("w");
        analysisRequest.setStatisticType(StatisticType.SINGLE);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.TRANSACTION_MONEY);
        analysisRequest.setPaging(new PagingRequest(0, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleLineChartResponse response = iTimeRuleAnalysisStatistics.accessLineChartResult(analysisRequest, caseId);
        System.out.println("按照统计方式为单一、时间周期为周、折线图统计方式为交易金额来计算:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    // 按照统计方式为单一、时间周期为周、折线图统计方式为交易次数来计算
    @Test
    public void testTimeRuleLineChartResult_单一_周_交易次数() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");

        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("w");
        analysisRequest.setStatisticType(StatisticType.SINGLE);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.NUMBER_OF_TRANSACTION);
        analysisRequest.setPaging(new PagingRequest(0, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleLineChartResponse response = iTimeRuleAnalysisStatistics.accessLineChartResult(analysisRequest, caseId);
        System.out.println("按照统计方式为单一、时间周期为周、折线图统计方式为交易次数来计算:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    // 按照统计方式为汇总、时间周期为周、折线图统计方式为交易金额来计算
    @Test
    public void testTimeRuleLineChartResult_汇总_周_交易金额() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");

        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("w");
        analysisRequest.setStatisticType(StatisticType.SUMMARY);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.TRANSACTION_MONEY);
        analysisRequest.setPaging(new PagingRequest(0, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleLineChartResponse response = iTimeRuleAnalysisStatistics.accessLineChartResult(analysisRequest, caseId);
        System.out.println("按照统计方式为汇总、时间周期为周、折线图统计方式为交易金额来计算:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    // 按照统计方式为汇总、时间周期为周、折线图统计方式为交易次数来计算
    @Test
    public void testTimeRuleLineChartResult_汇总_周_交易次数() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");

        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("w");
        analysisRequest.setStatisticType(StatisticType.SUMMARY);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.NUMBER_OF_TRANSACTION);
        analysisRequest.setPaging(new PagingRequest(0, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleLineChartResponse response = iTimeRuleAnalysisStatistics.accessLineChartResult(analysisRequest, caseId);
        System.out.println("按照统计方式为汇总、时间周期为周、折线图统计方式为交易次数来计算:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    // 按照统计方式为单一、时间周期为天（日）、折线图统计方式为交易金额来计算
    @Test
    public void testTimeRuleLineChartResult单一_日_交易金额() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");
        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("d");
        analysisRequest.setStatisticType(StatisticType.SINGLE);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.TRANSACTION_MONEY);
        analysisRequest.setPaging(new PagingRequest(0, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleLineChartResponse response = iTimeRuleAnalysisStatistics.accessLineChartResult(analysisRequest, caseId);
        System.out.println("按照统计方式为单一、时间周期为日、折线图统计方式为交易金额来计算:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    // 按照统计方式为单一、时间周期为天（日）、折线图统计方式为交易次数来计算
    @Test
    public void testTimeRuleLineChartResult单一_日_交易次数() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");
        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("d");
        analysisRequest.setStatisticType(StatisticType.SINGLE);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.NUMBER_OF_TRANSACTION);
        analysisRequest.setPaging(new PagingRequest(0, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleLineChartResponse response = iTimeRuleAnalysisStatistics.accessLineChartResult(analysisRequest, caseId);
        System.out.println("按照统计方式为单一、时间周期为日、折线图统计方式为交易次数来计算:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    // 按照统计方式为汇总、时间周期为天（日）、折线图统计方式为交易金额来计算
    @Test
    public void testTimeRuleLineChartResult汇总_日_交易金额() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");
        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("d");
        analysisRequest.setStatisticType(StatisticType.SUMMARY);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.TRANSACTION_MONEY);
        analysisRequest.setPaging(new PagingRequest(0, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleLineChartResponse response = iTimeRuleAnalysisStatistics.accessLineChartResult(analysisRequest, caseId);
        System.out.println("按照统计方式为汇总、时间周期为日、折线图统计方式为交易金额来计算:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    // 按照统计方式为汇总、时间周期为天（日）、折线图统计方式为交易次数来计算
    @Test
    public void testTimeRuleLineChartResult汇总_日_交易次数() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");
        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("d");
        analysisRequest.setStatisticType(StatisticType.SUMMARY);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.NUMBER_OF_TRANSACTION);
        analysisRequest.setPaging(new PagingRequest(0, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleLineChartResponse response = iTimeRuleAnalysisStatistics.accessLineChartResult(analysisRequest, caseId);
        System.out.println("按照统计方式为汇总、时间周期为日、折线图统计方式为交易次数来计算:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    // 按照统计方式为单一、时间周期为小时、折线图统计方式为交易金额来计算
    @Test
    public void testTimeRuleLineChartResult单一_小时_交易金额() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");
        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("h");
        analysisRequest.setStatisticType(StatisticType.SUMMARY);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.TRANSACTION_MONEY);
        analysisRequest.setPaging(new PagingRequest(0, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleLineChartResponse response = iTimeRuleAnalysisStatistics.accessLineChartResult(analysisRequest, caseId);
        System.out.println("按照统计方式为单一、时间周期为小时、折线图统计方式为交易金额来计算:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    // 按照统计方式为单一、时间周期为小时、折线图统计方式为交易金额来计算
    @Test
    public void testTimeRuleLineChartResult单一_小时_交易次数() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");
        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("h");
        analysisRequest.setStatisticType(StatisticType.SINGLE);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.NUMBER_OF_TRANSACTION);
        analysisRequest.setPaging(new PagingRequest(0, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleLineChartResponse response = iTimeRuleAnalysisStatistics.accessLineChartResult(analysisRequest, caseId);
        System.out.println("按照统计方式为单一、时间周期为小时、折线图统计方式为交易次数来计算:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }


    // 按照统计方式为汇总、时间周期为小时、折线图统计方式为交易金额来计算
    @Test
    public void testTimeRuleLineChartResult汇总_小时_交易金额() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");
        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("h");
        analysisRequest.setStatisticType(StatisticType.SUMMARY);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.TRANSACTION_MONEY);
        analysisRequest.setPaging(new PagingRequest(0, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleLineChartResponse response = iTimeRuleAnalysisStatistics.accessLineChartResult(analysisRequest, caseId);
        System.out.println("按照统计方式为汇总、时间周期为小时、折线图统计方式为交易金额来计算:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    // 按照统计方式为汇总、时间周期为小时、折线图统计方式为交易金额来计算
    @Test
    public void testTimeRuleLineChartResult汇总_小时_交易次数() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");
        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("h");
        analysisRequest.setStatisticType(StatisticType.SUMMARY);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.NUMBER_OF_TRANSACTION);
        analysisRequest.setPaging(new PagingRequest(0, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleLineChartResponse response = iTimeRuleAnalysisStatistics.accessLineChartResult(analysisRequest, caseId);
        System.out.println("按照统计方式为汇总、时间周期为小时、折线图统计方式为交易次数来计算:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    /**
     * 判断一年的第几周
     *
     * @param datetime
     * @return
     * @throws ParseException
     */
    public static Integer whatWeek(String datetime) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = format.parse(datetime);
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(date);
        Integer weekNumbe = calendar.get(Calendar.WEEK_OF_YEAR);
        return weekNumbe;
    }
    /**
     * ------------------------------------- 时间规律分析-折线图结果单元测试 end -------------------------------------
     * */


    /**
     * ------------------------------------- 时间规律分析-列表结果单元测试 start -------------------------------------
     */


    // 按照统计方式为单一、时间周期为月来计算列表数据
    @Test
    public void testTimeRuleLineChartResult单一_月() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");
        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("M");
        analysisRequest.setStatisticType(StatisticType.SINGLE);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.TRANSACTION_MONEY);
        analysisRequest.setPaging(new PagingRequest(1, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleResultListResponse response = iTimeRuleAnalysisStatistics.accessTimeRuleResultList(analysisRequest, caseId);
        System.out.println("[列表数据展示]-按照统计方式为单一、时间周期为月来计算列表数据:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    // 按照统计方式为汇总、时间周期为月列表数据
    @Test
    public void testTimeRuleResultList汇总_月() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");
        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("M");
        analysisRequest.setStatisticType(StatisticType.SUMMARY);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.NUMBER_OF_TRANSACTION);
        analysisRequest.setPaging(new PagingRequest(1, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleResultListResponse response = iTimeRuleAnalysisStatistics.accessTimeRuleResultList(analysisRequest, caseId);
        System.out.println("[列表数据展示]-按照统计方式为单一、时间周期为月来计算列表数据：" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    // 按照统计方式为单一、时间周期为周来计算列表数据
    @Test
    public void testTimeRuleLineChartResult单一_周() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");
        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("w");
        analysisRequest.setStatisticType(StatisticType.SINGLE);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.TRANSACTION_MONEY);
        analysisRequest.setPaging(new PagingRequest(1, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleResultListResponse response = iTimeRuleAnalysisStatistics.accessTimeRuleResultList(analysisRequest, caseId);
        System.out.println("[列表数据展示]-按照统计方式为单一、时间周期为周来计算列表数据:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    // 按照统计方式为汇总、时间周期为周列表数据
    @Test
    public void testTimeRuleResultList汇总_周() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");
        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("w");
        analysisRequest.setStatisticType(StatisticType.SUMMARY);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.NUMBER_OF_TRANSACTION);
        analysisRequest.setPaging(new PagingRequest(1, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleResultListResponse response = iTimeRuleAnalysisStatistics.accessTimeRuleResultList(analysisRequest, caseId);
        System.out.println("[列表数据展示]-按照统计方式为单一、时间周期为月来计算列表数据：" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    // 按照统计方式为单一、时间周期为日(按天)来计算列表数据
    @Test
    public void testTimeRuleLineChartResult单一_日() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");
        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("d");
        analysisRequest.setStatisticType(StatisticType.SINGLE);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.TRANSACTION_MONEY);
        analysisRequest.setPaging(new PagingRequest(1, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleResultListResponse response = iTimeRuleAnalysisStatistics.accessTimeRuleResultList(analysisRequest, caseId);
        System.out.println("[列表数据展示]-按照统计方式为单一、时间周期为日来计算列表数据:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    // 按照统计方式为汇总、时间周期为日（按天）列表数据
    @Test
    public void testTimeRuleResultList汇总_日() throws IOException {
        String caseId = "8d72363a6c3e4b97ad76a424d54e83e5";
        List<String> source = Arrays.asList("60138216660000014","60138216660001414","60138216660002814","60138216660004214","60138216660005614","60138216660007014","60138216660008414","60138216660009814","60138216660011214","60138216660012614","60138216660014014","60138216660015414","60138216660016814","60138216660018214","60138216660019614","60138216660021014","60138216660022414","60138216660023814","60138216660025214","60138216660026614","60138216660028014","60138216660029414","60138216660030814","60138216660032214","60138216660033614","60138216660035014","60138216660036414","60138216660037814","60138216660039214","60138216660040614","60138216660042014","60138216660043414","60138216660044814","60138216660046214","60138216660047614");
//        List<String> dest = Arrays.asList("60138216661548782");
        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
//        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("d");
        analysisRequest.setStatisticType(StatisticType.SUMMARY);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.NUMBER_OF_TRANSACTION);
        analysisRequest.setPaging(new PagingRequest(1, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(false);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleResultListResponse response = iTimeRuleAnalysisStatistics.accessTimeRuleResultList(analysisRequest, caseId);
        System.out.println("[列表数据展示]-按照统计方式为单一、时间周期为日来计算列表数据：" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }


    // 按照统计方式为单一、时间周期为小时来计算结果列表数据
    //[注意]：统计方式为单一时，不支持对时间类型为小时的统计
    @Test
    public void testTimeRuleAnalysisResultList单一_小时() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");
        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("h");
        analysisRequest.setStatisticType(StatisticType.SINGLE);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.NUMBER_OF_TRANSACTION);
        analysisRequest.setPaging(new PagingRequest(1, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleResultListResponse response = iTimeRuleAnalysisStatistics.accessTimeRuleResultList(analysisRequest, caseId);
        System.out.println("[列表数据展示]-按照统计方式为单一、时间周期为小时来计算列表数据:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    // 按照统计方式为汇总、时间周期为小时来计算列表数据
    @Test
    public void testTimeRuleAnalysisResultList汇总_小时() throws IOException {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");
        TimeRuleAnalysisRequest analysisRequest = new TimeRuleAnalysisRequest();
        analysisRequest.setSource(source);
        analysisRequest.setDest(dest);
        // 按照月、周、日、以及24小时
        analysisRequest.setDateType("h");
        analysisRequest.setStatisticType(StatisticType.SUMMARY);
        analysisRequest.setLineChartDisplayType(LineChartDisplayType.NUMBER_OF_TRANSACTION);
        analysisRequest.setPaging(new PagingRequest(1, 25));
        analysisRequest.setSorting(new SortingRequest("timePeriod", SortingRequest.Direction.DESC));
        analysisRequest.setHolidays(true);
        analysisRequest.setWeekdays(true);
        analysisRequest.setWeekend(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        TimeRuleResultListResponse response = iTimeRuleAnalysisStatistics.accessTimeRuleResultList(analysisRequest, caseId);
        System.out.println("[列表数据展示]-按照统计方式为汇总、时间周期为小时来计算列表数据:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    /**
     * ------------------------------------- 时间规律分析-列表结果单元测试 end -------------------------------------
     * */

    /**
     * ------------------------------------- 时间规律分析-详情数据单元测试 start -------------------------------------
     */

    @Test
    public void testTimeRuleAnalysisResultDetails() {
        String caseId = "b968df03f7384fa18f50049ff1dd5156";
     //   List<String> source = Arrays.asList("60138216660000014","60138216660001414","60138216660002814","60138216660004214","60138216660005614","60138216660007014","60138216660008414","60138216660009814","60138216660011214","60138216660012614","60138216660014014","60138216660015414","60138216660016814","60138216660018214","60138216660019614","60138216660021014","60138216660022414","60138216660023814","60138216660025214","60138216660026614","60138216660028014","60138216660029414","60138216660030814","60138216660032214","60138216660033614","60138216660035014","60138216660036414","60138216660037814","60138216660039214","60138216660040614","60138216660042014","60138216660043414","60138216660044814","60138216660046214","60138216660047614");
        List<String> source = Arrays.asList("60138216660030800", "60138216660047600");
        List<String> dest = Arrays.asList("60138216661548782");
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setSorting(new SortingRequest("trading_time", SortingRequest.Direction.DESC));
        queryRequest.setPaging(new PagingRequest(0, 25));
        String tradingTime = "2";

        TimeRuleAnalysisDetailRequest request = TimeRuleAnalysisDetailRequest.builder()
//                .dest(dest)
                .source(source)
                .statisticType(StatisticType.SUMMARY)
                .dateFiltering(Arrays.asList(TimeRuleAnalysisDetailRequest.DateFilteringEnum.weekdays))
                .queryRequest(queryRequest)
                .tradingTime(tradingTime)
                .dateType("h")
                .build();
        Page<BankTransactionFlow> page = iTimeRuleAnalysisStatistics.accessTimeRuleAnalysisResultDetail(request, caseId);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        try {
            System.out.println("*****************"+mapper.writerWithDefaultPrettyPrinter().writeValueAsString(page));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWeek() throws ParseException {
        String date ="2019-09-23";
        System.out.println(DateUtils.whatWeek(date));
    }

    /**
     * ------------------------------------- 时间规律分析-详情数据单元测试 start -------------------------------------
     * */
}
