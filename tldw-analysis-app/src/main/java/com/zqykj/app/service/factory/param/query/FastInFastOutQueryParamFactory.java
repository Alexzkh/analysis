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
     * <h2> 通过查询卡号与对方卡号 获取进出记录 </h2>
     * <p>
     * 其中cards 卡号作为查询卡号
     */
    QuerySpecialParams getInoutRecordsViaQueryAndOpposite(List<String> cards, @Nullable List<String> oppositeCards, String caseId, int singleQuota, boolean isIn);
}
