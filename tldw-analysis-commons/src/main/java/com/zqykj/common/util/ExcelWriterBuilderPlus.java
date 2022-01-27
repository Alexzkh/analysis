/**
 * @作者 Mcj
 */
package com.zqykj.common.util;

import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteWorkbook;

import java.io.InputStream;
import java.io.OutputStream;

public class ExcelWriterBuilderPlus extends ExcelWriterBuilder {

    private WriteWorkbook writeWorkbook = new WriteWorkbook();


    public static ExcelWriterBuilderPlus write(OutputStream outputStream) {
        return write(outputStream, null);
    }


    public static ExcelWriterBuilderPlus write(OutputStream outputStream, Class head) {
        ExcelWriterBuilderPlus excelWriterBuilder = new ExcelWriterBuilderPlus();
        excelWriterBuilder.file(outputStream);
        if (head != null) {
            excelWriterBuilder.head(head);
        }
        return excelWriterBuilder;
    }


    @Override
    public ExcelWriterBuilderPlus withTemplate(InputStream templateInputStream) {
        writeWorkbook.setTemplateInputStream(templateInputStream);
        return this;
    }


    @Override
    public ExcelWriterPlus build() {
        return new ExcelWriterPlus(writeWorkbook);
    }
}
