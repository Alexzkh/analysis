/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.zqykj.app.service.config.ThreadPoolConfig;
import com.zqykj.app.service.factory.param.agg.TransactionFieldAggParamFactory;
import com.zqykj.app.service.factory.param.query.TransactionFieldQueryParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.field.FundTacticsFuzzyQueryField;
import com.zqykj.app.service.interfaze.ITransactionField;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.app.service.vo.fund.middle.TradeAnalysisDetailResult;
import com.zqykj.builder.AggregationParamsBuilders;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.common.util.EasyExcelUtils;
import com.zqykj.common.vo.PageRequest;
import com.zqykj.common.vo.SortRequest;
import com.zqykj.domain.Page;
import com.zqykj.domain.Sort;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.util.JacksonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * <h1> 交易字段分析 </h1>
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class TransactionFieldImpl extends FundTacticsCommonImpl implements ITransactionField {

    private final TransactionFieldQueryParamFactory transactionFieldQueryParamFactory;

    private final TransactionFieldAggParamFactory transactionFieldAggParamFactory;

    public ServerResponse<List<TransactionFieldTypeProportionResults>> fieldTypeProportionHistogram(TransactionFieldAnalysisRequest request) throws Exception {

        PageRequest pageRequest = request.getPageRequest();
        SortRequest sortRequest = request.getSortRequest();
        Future<List<TransactionFieldTypeProportionResults>> future = CompletableFuture.supplyAsync(() ->
                tradeFieldTypeQuery(request, TransactionFieldTypeProportionResults.class, 0, pageRequest.getPageSize()), ThreadPoolConfig.getExecutor());
        List<TransactionFieldTypeCustomResults> customResults = batchCustomCollationQuery(request, "tradeTotalAmount", "tradeTotalTimes");
        List<TransactionFieldTypeProportionResults> proportionResults = future.get();
        if (!CollectionUtils.isEmpty(customResults)) {
            // 转成交易类型占比结果
            proportionResults.addAll(TransactionFieldTypeProportionResults.convertProportionResults(customResults));
        }
        // 合并排序(将汇总后的占比结果重新排序)
        mergeSort(proportionResults, sortRequest.getProperty(), !sortRequest.getOrder().isAscending());
        // 截取结果
        List<TransactionFieldTypeProportionResults> newResults = proportionResults.stream().limit(pageRequest.getPageSize()).collect(Collectors.toList());
        return ServerResponse.createBySuccess(newResults);
    }

    public ServerResponse<FundAnalysisResultResponse<TransactionFieldTypeStatisticsResult>> fieldTypeStatistics(int from, int size, TransactionFieldAnalysisRequest request) throws Exception {

        PageRequest pageRequest = request.getPageRequest();
        SortRequest sortRequest = request.getSortRequest();
        request.setAggQueryType(2);
        int limit = (pageRequest.getPage() + 1) * pageRequest.getPageSize();
        Future<List<TransactionFieldTypeStatisticsResult>> future = CompletableFuture.supplyAsync(() ->
                tradeFieldTypeQuery(request, TransactionFieldTypeStatisticsResult.class, 0, limit), ThreadPoolConfig.getExecutor());
        List<TransactionFieldTypeCustomResults> customResults = batchCustomCollationQuery(request);
        List<TransactionFieldTypeStatisticsResult> statisticsResults = future.get();
        long total = total(request);
        if (!CollectionUtils.isEmpty(customResults)) {
            // 转成交易类型统计结果
            statisticsResults.addAll(TransactionFieldTypeStatisticsResult.convertStatisticsResults(customResults));
            total += customResults.size();
        }
        // 计算占比总额
        TransactionFieldTypeStatisticsResult.calculateProportionData(statisticsResults, customResults, false);
        // 合并排序(将汇总后的交易统计结果重新排序)
        mergeSort(statisticsResults, sortRequest.getProperty(), !sortRequest.getOrder().isAscending());
        List<TransactionFieldTypeStatisticsResult> newResults = statisticsResults.stream().skip(pageRequest.offset()).limit(pageRequest.getPageSize()).collect(Collectors.toList());
        // 还需要加上自定义查询的结果size
        return ServerResponse.createBySuccess(FundAnalysisResultResponse.build(newResults, total, size));
    }

    public ServerResponse<FundAnalysisResultResponse<Object>> customCollationContainField(TransactionFieldAnalysisRequest request) {

        PageRequest pageRequest = request.getPageRequest();
        int offset = PageRequest.getOffset(pageRequest.getPage(), pageRequest.getPageSize());
        QuerySpecialParams query = transactionFieldQueryParamFactory.transactionFieldTypeQuery(request);
        AggregationParams agg = aggParamFactory.groupByField(request.getStatisticsField(), fundThresholdConfig.getGroupByThreshold(),
                new Pagination(offset, pageRequest.getPageSize()));
        agg.setMapping(entityMappingFactory.buildGroupByAggMapping(request.getStatisticsField()));
        agg.setResultName("groupByStatisticsField");
        agg.addSiblingAggregation(totalAgg(request.getStatisticsField()));
        Map<String, List<List<Object>>> resultMap = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionFlow.class, request.getCaseId());
        List<List<Object>> resultList = resultMap.get(agg.getResultName());
        List<Object> result = resultList.stream().map(e -> e.get(0)).collect(Collectors.toList());
        List<List<Object>> totalResult = resultMap.get(CARDINALITY_TOTAL);
        long total;
        if (CollectionUtils.isEmpty(totalResult)) {
            total = 0L;
        } else {
            total = (long) totalResult.get(0).get(0);
        }
        return ServerResponse.createBySuccess(FundAnalysisResultResponse.build(result, total, pageRequest.getPageSize()));
    }

    public ServerResponse<FundAnalysisResultResponse<TradeAnalysisDetailResult>> getDetail(TransactionFieldAnalysisRequest request, int from, int size) {

        SortRequest sortRequest = request.getSortRequest();
        String[] detailFuzzyFields = FundTacticsFuzzyQueryField.detailFuzzyFields;
        QuerySpecialParams query = transactionFieldQueryParamFactory.fieldTypeStatisticsDetailQuery(request, detailFuzzyFields);
        // 设置需要查询的字段
        query.setIncludeFields(FundTacticsAnalysisField.detailShowFieldFlow());
        Page<BankTransactionFlow> page = entranceRepository.findAll(com.zqykj.domain.PageRequest.of(from, size,
                Sort.Direction.valueOf(sortRequest.getOrder().name()), sortRequest.getProperty()), request.getCaseId(), BankTransactionFlow.class, query);
        List<TradeAnalysisDetailResult> detailResults = getTradeAnalysisDetailResultsByFlow(page);
        return ServerResponse.createBySuccess(FundAnalysisResultResponse.build(detailResults, page.getTotalElements(), page.getSize()));
    }

    public ServerResponse<String> detailExport(ExcelWriter excelWriter, TransactionFieldAnalysisRequest request) throws ExecutionException, InterruptedException {

        ServerResponse<FundAnalysisResultResponse<TradeAnalysisDetailResult>> detail = getDetail(request, 0, 1);
        int total = Integer.parseInt(String.valueOf(detail.getData().getTotal()));
        if (total == 0) {
            excelWriter.write(new ArrayList<>(), EasyExcelUtils.generateWriteSheet(request.getExportFileName()));
        }
        // 单个sheet页即可
        WriteSheet sheet = EasyExcelUtils.generateWriteSheet(request.getExportFileName());
        writeDetailData(total, request, excelWriter, sheet);
        return ServerResponse.createBySuccess();
    }

    public ServerResponse<String> fieldTypeStatisticsExport(ExcelWriter excelWriter, TransactionFieldAnalysisRequest request) throws Exception {

        request.setAggQueryType(2);
        int total = Integer.parseInt(String.valueOf(total(request)));
        // 还需要加上自定义查询结果size
        if (!CollectionUtils.isEmpty(request.getCustomCollationQueryRequests())) {
            total += request.getCustomCollationQueryRequests().size();
        }
        if (total == 0) {
            // 生成一个sheet
            WriteSheet sheet = EasyExcelUtils.generateWriteSheet(request.getExportFileName());
            excelWriter.write(new ArrayList<>(), sheet);
            return ServerResponse.createBySuccess();
        }
        int perRowSheetCount = exportThresholdConfig.getPerSheetRowCount();
        if (total <= perRowSheetCount) {
            // 单个sheet页即可
            WriteSheet sheet = EasyExcelUtils.generateWriteSheet(request.getExportFileName());
            writeStatisticsData(0, total, excelWriter, sheet, request);
        } else {
            // 多个sheet页处理
            int limit = total;
            if (total > exportThresholdConfig.getExcelExportThreshold()) {
                limit = exportThresholdConfig.getExcelExportThreshold();
            }
            int position = 0;
            int perSheetRowCount = exportThresholdConfig.getPerSheetRowCount();
            // 这里就没必要在多线程了(一个sheet页假设50W,内部分批次查询,每次查询1W,就要查詢50次,若这里再开多线程分批次,ThreadPoolConfig.getExecutor()
            // 的最大线程就这么多,剩下的只能在队列中等待)
            int sheetNo = 0;
            while (position < limit) {
                int next = Math.min(position + perSheetRowCount, limit);
                WriteSheet sheet;
                if (sheetNo == 0) {
                    sheet = EasyExcelUtils.generateWriteSheet(sheetNo, request.getExportFileName());
                } else {
                    sheet = EasyExcelUtils.generateWriteSheet(sheetNo, request.getExportFileName() + "_" + sheetNo);
                }
                writeStatisticsData(position, next, excelWriter, sheet, request);
                position = next;
                sheetNo++;
            }
        }
        return ServerResponse.createBySuccess();
    }

    /**
     * <h2> 批量将交易汇聚结果写入到Excel中 </h2>
     */
    private void writeStatisticsData(int position, int limit, ExcelWriter writer, WriteSheet sheet, TransactionFieldAnalysisRequest request) throws Exception {

        if (limit == 0) {
            return;
        }
        int chunkSize = exportThresholdConfig.getPerWriteRowCount();
        List<Future<List<TransactionFieldTypeStatisticsResult>>> futures = new ArrayList<>();
        while (position < limit) {
            int next = Math.min(position + chunkSize, limit);
            int finalPosition = position;
            Future<List<TransactionFieldTypeStatisticsResult>> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            return tradeFieldTypeQuery(request, TransactionFieldTypeStatisticsResult.class, finalPosition, next - finalPosition);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    },
                    ThreadPoolConfig.getExecutor());
            position = next;
            futures.add(future);
        }
        List<TransactionFieldTypeStatisticsResult> statisticsResults = new ArrayList<>();
        for (Future<List<TransactionFieldTypeStatisticsResult>> future : futures) {
            List<TransactionFieldTypeStatisticsResult> dataList = future.get();
            statisticsResults.addAll(dataList);
        }
        // 合并自定义查询数据
        SortRequest sortRequest = request.getSortRequest();
        List<TransactionFieldTypeCustomResults> customResults = batchCustomCollationQuery(request);
        if (!CollectionUtils.isEmpty(customResults)) {
            // 转成交易类型统计结果
            statisticsResults.addAll(TransactionFieldTypeStatisticsResult.convertStatisticsResults(customResults));
        }
        // 计算占比总额
        TransactionFieldTypeStatisticsResult.calculateProportionData(statisticsResults, customResults, true);
        // 合并排序(将汇总后的交易统计结果重新排序)
        mergeSort(statisticsResults, sortRequest.getProperty(), !sortRequest.getOrder().isAscending());
        // 添加sheet
        if (!CollectionUtils.isEmpty(statisticsResults)) {
            writer.write(statisticsResults, sheet);
        }
    }

    private void writeDetailData(int limit, TransactionFieldAnalysisRequest request, ExcelWriter writer, WriteSheet sheet) throws ExecutionException, InterruptedException {

        if (limit == 0) {
            return;
        }
        int chunkSize = exportThresholdConfig.getPerWriteRowCount();
        List<Future<List<TradeAnalysisDetailResult>>> futures = new ArrayList<>();
        int page = 0;
        int position = 0;
        while (position < limit) {
            int next = Math.min(position + chunkSize, limit);
            int finalPage = page;
            CompletableFuture<List<TradeAnalysisDetailResult>> future =
                    CompletableFuture.supplyAsync(() -> getDetail(request, finalPage, chunkSize).getData().getContent(),
                            ThreadPoolConfig.getExecutor());
            position = next;
            futures.add(future);
            page++;
        }
        for (Future<List<TradeAnalysisDetailResult>> future : futures) {
            List<TradeAnalysisDetailResult> dataList = future.get();
            // 添加sheet
            if (!CollectionUtils.isEmpty(dataList)) {
                writer.write(dataList, sheet);
            }
        }
    }

    /**
     * <h2> 交易字段类型(自定义归类查询) - 包含字段类型占比/字段类型统计 </h2>
     */
    public List<TransactionFieldTypeCustomResults> batchCustomCollationQuery(TransactionFieldAnalysisRequest request, String... includeKeyMapping) throws ExecutionException, InterruptedException {

        List<TransactionFieldAnalysisRequest.CustomCollationQueryRequest> customCollationQueryRequests = request.getCustomCollationQueryRequests();
        if (CollectionUtils.isEmpty(customCollationQueryRequests)) {
            return null;
        }
        List<Future<List<TransactionFieldTypeCustomResults>>> futures = new ArrayList<>();
        for (TransactionFieldAnalysisRequest.CustomCollationQueryRequest customCollationQueryRequest : customCollationQueryRequests) {

            Future<List<TransactionFieldTypeCustomResults>> future = CompletableFuture.supplyAsync(
                    () -> tradeFieldTypeCustomCollationQuery(request, customCollationQueryRequest.getContainField(), TransactionFieldTypeCustomResults.class,
                            0, fundThresholdConfig.getGroupByThreshold(), includeKeyMapping), ThreadPoolConfig.getExecutor());
            futures.add(future);
        }
        List<TransactionFieldTypeCustomResults> results = new ArrayList<>();
        int i = 0;
        for (Future<List<TransactionFieldTypeCustomResults>> future : futures) {
            List<TransactionFieldTypeCustomResults> transactionFieldTypeProportionResults = future.get();
            if (!CollectionUtils.isEmpty(transactionFieldTypeProportionResults)) {
                TransactionFieldTypeCustomResults proportionResult = transactionFieldTypeProportionResults.get(i);
                proportionResult.setFieldGroupContent(customCollationQueryRequests.get(i).getClassificationName());
                results.add(proportionResult);
            }
            i++;
        }
        return results;
    }

    /**
     * <h2> 交易字段类型查询- 包含字段类型占比/字段类型统计</h2>
     */
    public <T> List<T> tradeFieldTypeQuery(TransactionFieldAnalysisRequest request, Class<T> entity, int from, int size, String... includeKeyMapping) {
        return tradeFieldTypeCustomCollationQuery(request, null, entity, from, size, includeKeyMapping);
    }

    /**
     * <h2> 交易字段类型结果查询(属于自定义归类查询)- 包含字段类型占比/字段类型统计 </h2>
     */
    public <T> List<T> tradeFieldTypeCustomCollationQuery(TransactionFieldAnalysisRequest request, List<String> containFieldContent, Class<T> entity,
                                                          int from, int size, String... includeKeyMapping) {
        QuerySpecialParams query;
        AggregationParams agg;
        Map<String, String> keyMapping = new LinkedHashMap<>();
        Map<String, String> entityMapping = new LinkedHashMap<>();
        if (!CollectionUtils.isEmpty(containFieldContent)) {
            query = transactionFieldQueryParamFactory.fieldTypeCustomCollationQuery(request, containFieldContent);
            agg = transactionFieldAggParamFactory.fieldTypeProportionCustomCollationQuery(request, fundThresholdConfig.getGroupByThreshold());
            entityMappingFactory.buildTradeAnalysisResultAggMapping(keyMapping, entityMapping, entity, Arrays.asList(includeKeyMapping));
        } else {
            query = transactionFieldQueryParamFactory.transactionFieldTypeQuery(request);
            agg = transactionFieldAggParamFactory.transactionFieldTypeProportion(request, from, size, fundThresholdConfig.getGroupByThreshold());
            entityMappingFactory.buildTradeAnalysisResultAggMapping(keyMapping, entityMapping, entity);
        }
        agg.setMapping(keyMapping);
        if (!CollectionUtils.isEmpty(containFieldContent)) {
            keyMapping.remove("field_group");
            entityMapping.remove("fieldGroupContent");
        }
        agg.setResultName("transactionFieldTypeResult");
        Map<String, List<List<Object>>> resultMap = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionFlow.class, request.getCaseId());
        List<List<Object>> resultList = resultMap.get(agg.getResultName());
        if (CollectionUtils.isEmpty(resultList)) {
            return new ArrayList<>();
        }
        List<String> entityTitles = new ArrayList<>(entityMapping.keySet());
        List<Map<String, Object>> maps = parseFactory.convertEntity(resultList, entityTitles, entity);
        List<T> result = new ArrayList<>();
        maps.forEach(map -> result.add(JacksonUtils.parse(map, entity)));
        return result;
    }

    protected AggregationParams totalAgg(String statisticsField) {
        AggregationParams totalAgg = AggregationParamsBuilders.cardinality("distinct_" + statisticsField, statisticsField);
        totalAgg.setResultName(CARDINALITY_TOTAL);
        totalAgg.setMapping(entityMappingFactory.buildDistinctTotalAggMapping(statisticsField));
        return totalAgg;
    }

    /**
     * <h2> 交易字段类型 </h2>
     */
    private long total(TransactionFieldAnalysisRequest request) {

        QuerySpecialParams query = transactionFieldQueryParamFactory.transactionFieldTypeQuery(request);
        AggregationParams agg = totalAgg(request.getStatisticsField());
        Map<String, List<List<Object>>> resultMap = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionFlow.class, request.getCaseId());
        List<List<Object>> resultList = resultMap.get(agg.getResultName());
        if (CollectionUtils.isEmpty(resultList)) {
            return 0L;
        }
        return (long) resultList.get(0).get(0);
    }
}
