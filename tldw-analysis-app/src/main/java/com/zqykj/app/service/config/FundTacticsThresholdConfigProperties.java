/**
 * @作者 Mcj
 */
package com.zqykj.app.service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <h1> 资金战法临界值配置 </h1>
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "tactics.fund")
@Component
public class FundTacticsThresholdConfigProperties {

    // 全部查询配置
    // 最大调单卡号数量限制
    private int maxAdjustCardCount = 8000;
    // 最大未调单卡号数量限制
    private int maxUnadjustedCardCount = 20_0000;
    // 针对查询的总数据量拆分
    private int perTotalSplitCount = 10000;
    // 针对查询的总数据量拆分(继续拆分做数据批量查询)
    private int perTotalSplitQueryCount = 1000;

    // 普通查询配置
    // group by size 的 临界值
    private int groupByThreshold = 100_0000;

    // 次查询数量(普通批量查询)
    private int perQueryCount = 2000;

    // 每次聚合查询数量(普通批量查询)
    private int perAggCount = 5000;

    // 导出配置
    @Setter
    @Getter
    @ConfigurationProperties(prefix = "tactics.fund.export")
    @Component
    public static class Export {
        // 每个sheet页存储的记录数( 若查询的数据量超过,则开始拆分多sheet页)
        private int perSheetRowCount = 50_0000;
        // 每次向EXCEL写入的记录数(查询每页数据大小)
        private int perWriteRowCount = 10_000;
        // 导出Excel 的临界值 (超过这个值,不再导出)
        private int excelExportThreshold = 200_0000;
    }
}
