package com.zqykj.domain.vo;

import com.zqykj.infrastructure.compare.BaseCompareBean;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
/**
 * @Description: 交易路径查找详情页返回结果vo
 * @Author zhangkehou
 * @Date 2021/12/18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPathDetailResultVO implements BaseCompareBean {

    /**
     * 交易卡号
     */
    private String queryCard;

    /**
     * 开户名称
     */
    private String customerName;


    /**
     * 开户人证件号码
     */
    private String customerIdentityCard;

    /**
     * 开户银行名称
     */

    private String bank;

    /**
     * 交易对方卡号
     */
    private String transactionOppositeCard;


    /**
     * 交易对方姓名
     */
    private String transactionOppositeName;

    /**
     * 交易对方证件号码
     */
    private String transactionOppositeCertificateNumber;


    /**
     * 交易对方开户行
     */
    private String transactionOppositeAccountOpenBank;

    /**
     * 交易时间
     */
    private Date tradingTime;

    /**
     * 交易金额
     */
    private Double transactionMoney;

    /**
     * 借贷标志
     */
    private String loanFlag;

    /**
     * 交易类型
     */
    private String transactionType;

    /**
     * 交易摘要
     */
    private String transactionSummary;

}
