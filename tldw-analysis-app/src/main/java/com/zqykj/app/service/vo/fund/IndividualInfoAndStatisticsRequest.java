package com.zqykj.app.service.vo.fund;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 个体画像-基本信息与统计请求体
 *
 * @author: SunChenYu
 * @date: 2021年12月06日 16:02:03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IndividualInfoAndStatisticsRequest {
    /**
     * 案件id
     */
    private String caseId;

    /**
     * 身份证号
     */
    private String customerIdentityCard;
}
