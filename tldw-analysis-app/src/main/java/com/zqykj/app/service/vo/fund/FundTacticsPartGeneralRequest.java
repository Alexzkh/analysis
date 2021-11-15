/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.zqykj.common.vo.PageRequest;
import com.zqykj.common.vo.SortRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 资金战法部分通用请求参数
 */
@Setter
@Getter
@NoArgsConstructor
public class FundTacticsPartGeneralRequest {

    /**
     * 模糊查询
     */
    private String keyword;

    /**
     * 分页
     */
    private PageRequest pageRequest;

    /**
     * 排序
     */
    private SortRequest sortRequest;


    private int groupInitPage = 0;

    private int groupInitSize = 60000;

    private String searchTag = "local";
}
