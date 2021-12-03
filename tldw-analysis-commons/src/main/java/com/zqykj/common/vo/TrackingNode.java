package com.zqykj.common.vo;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: 资金追踪出来的节点信息
 * @Author zhangkehou
 * @Date 2021/11/30
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingNode {

    /**
     * 姓名
     */
    private String name;

    /**
     * 卡号
     */
    private String cardNumber;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 交易时间
     */
    private String tradingTime;

//    /**
//     * 追踪的下一个节点
//     */
//    private TrackingNode next;
}
