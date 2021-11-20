/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze;

import com.zqykj.app.service.vo.fund.FundTacticsPartGeneralPreRequest;

import java.util.List;

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
}
