package com.zqykj.common.response;

import com.zqykj.common.enums.AmountOperationSymbol;
import com.zqykj.common.vo.DateRangeRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description: 资金来源去向战法请求体
 * @Author zhangkehou
 * @Date 2021/11/6
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FundsSourceAndDestinationTopNResponse {


    /**
     * 来源或者去向的名称.
     */
    private String oppositeName;

    /**
     * 身份证
     */
    private String identityCard;

    /**
     * 交易金额
     */
    private BigDecimal transactionMoney;

    /**
     * 返回来源和去向的个数
     */
    private Integer top;
}
