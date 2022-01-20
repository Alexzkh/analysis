/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund.middle;

import com.alibaba.excel.write.metadata.WriteSheet;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;

/**
 * <h1> 导出Excel sheet 数据对象包装 </h1>
 */
@Setter
@Getter
public class ExportSheetData {

    /**
     * 一个sheet页对象
     */
    private WriteSheet sheet;

    /**
     * 一个sheet页所需要的数据
     */
    private Collection<?> data;

    /**
     * 一组sheet 页
     */
    private List<WriteSheet> writeSheetList;

    /**
     * 一组sheet 数据
     */
    private List<Collection<?>> dataList;
}
