package com.zqykj.common.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: 资金追踪响应体
 * @Author zhangkehou
 * @Date 2021/11/29
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundTrackingResponse {

    /**
     * 卡号
     * */
    private String queryCard;

    /**
     * 账户开户名称
     * */
    private String queryName;

    /**
     * 对方卡号
     * */
    private String oppositeCard;

    /**
     * 对方账户开户名称
     * */
    private String oppositeName;

    /**
     * 交易时间
     * */
    private Date tradingTime;

    /**
     * 交易金额
     * */
    private BigDecimal transactionMoney;

    /**
     * 借贷标志
     * */
    private String loanFlag;

    /**
     * 交易类型
     * */
    private String tradingType;

    /**
     * 交易摘要
     * */
    private String transactionSummary;

}
