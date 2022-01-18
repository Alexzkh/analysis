/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * <h1> 建议调单账号 </h1>
 */
@Setter
@Getter
@ExcelIgnoreUnannotated
@HeadRowHeight(value = 22)
@ColumnWidth(value = 26)
@ContentRowHeight(value = 22)
public class SuggestAdjustedAccountResult {

    /**
     * 数据标识唯一id
     */
    private int id;

    /**
     * 对方卡号(建议调单账号)
     */
    private String oppositeCard;

    /**
     * 账户开户名称
     */
    @ExcelProperty(value = "账户开户名称", index = 1)
    private String accountName;

    /**
     * 对方开户行
     */
    @ExcelProperty(value = "银行", index = 0)
    private String bank;

    /**
     * 关联账户数
     */
    private Integer numberOfLinkedAccounts;

    /**
     * 交易总次数
     */
    private Integer tradeTotalTimes;

    /**
     * 交易总金额
     */
    private BigDecimal tradeTotalAmount;

    /**
     * 入账总金额
     */
    private BigDecimal creditsTotalAmount;

    /**
     * 出账总金额
     */
    private BigDecimal payoutTotalAmount;

    /**
     * 交易总净和(入账总金额 - 出账总金额)
     */
    private BigDecimal tradeNet;

    /**
     * 账户特征(可能有多个) eg. 来源 中转(同时存在)
     */
    private String accountFeature;

    /**
     * 添加类型 1: 手动  2: 自动
     */
    private Integer addType;

    /**
     * 添加人
     */
    private String addAccount;

    /**
     * 添加时间
     */
    @JsonIgnore
    private String addDateTime;
}
