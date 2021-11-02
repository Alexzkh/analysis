package com.zqykj.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 人员地域返回体
 * @Author zhangkehou
 * @Date 2021/10/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeopleAreaReponse {

    /**
     * 地区
     */
    private String region;

    /**
     * 个数
     */
    private Long number;
}
