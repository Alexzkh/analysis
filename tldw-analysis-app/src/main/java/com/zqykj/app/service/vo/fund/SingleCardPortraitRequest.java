package com.zqykj.app.service.vo.fund;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单卡画像请求体
 *
 * @author: SunChenYu
 * @date: 2021年11月11日 20:07:34
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SingleCardPortraitRequest {
    /**
     * 案件id
     */
    private String caseId;

    /**
     * 查询卡号
     */
    private String queryCard;
}
