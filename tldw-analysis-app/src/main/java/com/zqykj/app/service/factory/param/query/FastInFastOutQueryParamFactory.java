/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.param.query;

import com.zqykj.parameters.query.QuerySpecialParams;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * <h1> 快进快出查询请求参数构建工厂 </h1>
 */
public interface FastInFastOutQueryParamFactory {

    /**
     * <h2> 通过查询卡号获取进出记录 </h2>
     * <p>
     * 其中cards 卡号作为查询卡号
     */
    QuerySpecialParams getInoutRecordsViaAdjustCards(List<String> cards, String caseId, int singleQuota, boolean isIn);

    /**
     * <h2> 查询调单卡号 </h2>
     * <p>
     * 过滤条件为交易金额、案件Id、查询的表是{@link com.zqykj.domain.bank.BankTransactionFlow}
     */
    QuerySpecialParams getAdjustCards(String caseId, int singleQuota);

    /**
     * <h2> 通过查询卡号与对方卡号 获取进出记录 </h2>
     * <p>
     * 其中cards 卡号作为查询卡号
     */
    QuerySpecialParams getInoutRecordsViaQueryAndOpposite(List<String> cards, @Nullable List<String> oppositeCards, String caseId, int singleQuota, boolean isIn);
}
