/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze;

import com.zqykj.app.service.vo.fund.AdjustIndividualRequest;
import com.zqykj.app.service.vo.fund.FundTacticsPartGeneralPreRequest;
import com.zqykj.common.core.ServerResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * <h1> 资金战法分析基类 </h1>
 */

public interface IFundTacticsAnalysis {

    /**
     * <h2> 批量获取调单卡号集合 </h2>
     */
    List<String> getAllMainCardsViaPageable(FundTacticsPartGeneralPreRequest request, int from, int size, String caseId);

    /**
     * <h2> 获取调单卡号总量 </h2>
     */
    int getAllMainCardsCount(FundTacticsPartGeneralPreRequest request, String caseId);

    /**
     * <h2> 给定一组卡号集合(过滤出调单) </h2>
     * <p>
     * 返回map 结构 是为了 eg. convergenceResults.stream().filter(e -> filterMainCards.containsKey(e.getTradeCard()))
     * 能够筛选出一组对象集合
     */
    Map<String, String> asyncFilterMainCards(String caseId, List<String> cards) throws ExecutionException, InterruptedException;

    /**
     * <h2> 获取调单个体分析结果 </h2>
     */
    ServerResponse getAdjustIndividuals(AdjustIndividualRequest request);

    /**
     * <h2> 获取调单卡号集合(选个个体账号展示下面的调单卡号) </h2>
     */
    ServerResponse getAdjustCardsViaIndividual(AdjustIndividualRequest request);
}
