package com.zqykj.common.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/9/28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatisticsRequest implements Serializable {

    private List<TransactionStatisticsQuery> transactionStatisticsQuery;

    private TransactionStatisticsAggs transactionStatisticsAggs;

}
