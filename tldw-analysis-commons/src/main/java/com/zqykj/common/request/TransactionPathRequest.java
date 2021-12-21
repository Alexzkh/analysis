package com.zqykj.common.request;

import com.zqykj.common.enums.AmountOperationSymbol;
import com.zqykj.common.vo.DateRangeRequest;
import com.zqykj.common.vo.PathDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: 路径分析请求体
 * @Author zhangkehou
 * @Date 2021/12/9
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPathRequest {

    /**
     * 路径分析--左侧选择的卡的集合
     */
    private List<String> left;

    /**
     * 路径分析--右侧选择的卡的集合
     */
    private List<String> right;

    /**
     * 路径分析--路径方向
     */
    private PathDirection direction;

    /**
     * 路径深度
     */
    private Integer depth = 0;

    /**
     * 日期范围   (时间范围固定是:  00:00:00-23:59:59)
     */
    private DateRangeRequest dateRange;

    /**
     * 比较符  大于、大于等于、小于、小于等于、等于   (默认是大于等于0)
     */
    private AmountOperationSymbol operator = AmountOperationSymbol.gte;

    /**
     * 交易金额
     */
    private String fund = "0";

    /**
     * 查询参数（这其中包括模糊搜索,分页和排序参数）
     */
    private QueryRequest queryRequest;
}
