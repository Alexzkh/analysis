/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
import com.zqykj.app.service.annotation.Sort;
import com.zqykj.common.vo.PageRequest;
import com.zqykj.common.vo.SortRequest;
import com.zqykj.infrastructure.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1> 交易字段类型占比柱状图 </h1>
 */
@Setter
@Getter
public class TransactionFieldTypeProportionResults {

    /**
     * 字段分组后的结果
     */
    @Agg(name = "field_group")
    @Key(name = "keyAsString")
    private String fieldGroupContent;

    /**
     * 交易总金额
     */
    @Agg(name = "trade_amount")
    @Key(name = "value")
    @Sort(name = "trade_amount")
    private BigDecimal tradeTotalAmount;

    /**
     * 交易次数
     */
    @Agg(name = "trade_times")
    @Key(name = "value")
    @Sort(name = "trade_times")
    private int tradeTimes;

    /**
     * <h2> 合并排序(将自定义归类查询结果 与 主要查询结果合并,重新排序) </h2>
     */
    public static List<TransactionFieldTypeProportionResults> mergeSort(TransactionFieldAnalysisRequest request, List<TransactionFieldTypeProportionResults> results) {

        SortRequest sortRequest = request.getSortRequest();
        PageRequest pageRequest = request.getPageRequest();
        List<TransactionFieldTypeProportionResults> newResults;
        if (StringUtils.equals(sortRequest.getProperty(), "trade_total_amount")) {
            newResults = results.stream().sorted(Comparator.comparing(TransactionFieldTypeProportionResults::getTradeTotalAmount, Comparator.reverseOrder()))
                    .limit(pageRequest.getPageSize()).collect(Collectors.toList());
        } else {
            newResults = results.stream().sorted(Comparator.comparing(TransactionFieldTypeProportionResults::getTradeTimes, Comparator.reverseOrder()))
                    .limit(pageRequest.getPageSize()).collect(Collectors.toList());
        }
        return newResults;
    }

    /**
     * <h2> 将自定义归类查询结果转成占比结果 </h2>
     */
    public static List<TransactionFieldTypeProportionResults> convertProportionResults(List<TransactionFieldTypeCustomResults> customResults) {
        List<TransactionFieldTypeProportionResults> fieldTypeProportionResults = new ArrayList<>();
        CopyOptions copyOptions = new CopyOptions();
        copyOptions.setIgnoreNullValue(true);
        copyOptions.setIgnoreError(true);
        customResults.forEach(e -> {
            TransactionFieldTypeProportionResults results = new TransactionFieldTypeProportionResults();
            BeanUtil.copyProperties(e, results, copyOptions);
            fieldTypeProportionResults.add(results);
        });
        return fieldTypeProportionResults;
    }
}
