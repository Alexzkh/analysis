package com.zqykj.domain.vo;

import com.zqykj.infrastructure.compare.BaseCompareBean;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Description: 调单账号分析
 * @Author zhangkehou
 * @Date 2021/12/28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferAccountAnalysisResultVO implements BaseCompareBean {


    // 开户名称
    private String customerName;

    // 开户证件号码
    private String customerIdentityCard;

    // 开户银行
    private String bank;

    // 交易卡号
    private String queryCard;

    // 交易总次数
    private int relatedAccountTimes;

    // 交易总次数
    private int tradeTotalTimes;

    // 交易总金额
    private BigDecimal tradeTotalAmount;

    // 入账次数
    private int creditsTimes;

    // 入账金额
    private BigDecimal creditsAmount;

    // 出账次数
    private int payOutTimes;

    // 出账金额
    private BigDecimal payOutAmount;

    // 交易净和
    private BigDecimal tradeNet;

    // 最早交易时间
    private String earliestTradingTime;

    // 最晚交易时间
    private String latestTradingTime;

    // 处理器所处位置
    private int pos;

    // 账户特征
    private String accountCharacteristics;

}
