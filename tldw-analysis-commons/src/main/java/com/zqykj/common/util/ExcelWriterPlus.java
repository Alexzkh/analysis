/**
 * @作者 Mcj
 */
package com.zqykj.common.util;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteWorkbook;

public class ExcelWriterPlus extends ExcelWriter {

    public ExcelWriterPlus(WriteWorkbook writeWorkbook) {
        super(writeWorkbook);
    }

    @Override
    protected void finalize() {
    }
}
