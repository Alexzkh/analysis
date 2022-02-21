/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
import com.zqykj.app.service.annotation.Sort;
import com.zqykj.common.util.CompareFieldUtil;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <h1> 交易字段类型占比柱状图 </h1>
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    private int tradeTotalTimes;

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

    public static void main(String[] args) {
        // 根据多字段动态排序
        TransactionFieldTypeProportionResults result = new TransactionFieldTypeProportionResults();
        result.setTradeTotalAmount(new BigDecimal("1.00"));
        result.setTradeTotalTimes(80);
        TransactionFieldTypeProportionResults result1 = new TransactionFieldTypeProportionResults();
        result1.setTradeTotalAmount(new BigDecimal("32.00"));
        result1.setTradeTotalTimes(50);
        TransactionFieldTypeProportionResults result2 = new TransactionFieldTypeProportionResults();
        result2.setTradeTotalAmount(new BigDecimal("32.00"));
        result2.setTradeTotalTimes(89);
        List<TransactionFieldTypeProportionResults> results = new ArrayList<>();
        results.add(result);
        results.add(result1);
        results.add(result2);
        CompareFieldUtil.sort(results, true, "tradeTotalAmount", "tradeTimes");
    }
}
