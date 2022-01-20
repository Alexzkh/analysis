/**
 * @作者 Mcj
 */
package com.zqykj.common.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;
import com.alibaba.excel.write.style.row.SimpleRowHeightStyleStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.springframework.util.CollectionUtils;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * <h1> Easy Excel 简单封装 </h1>
 * 一些方法可以继续重载(根据需要自行添加) <br>
 */
@Slf4j
public class EasyExcelUtils {

    // 下面方法主要是导出Excel(生成多个 sheet页)  需要先生成ExcelWriter,然后在不断的写多个WriteSheet(有几个就是几个sheet页)  ExcelWriter.write()

    /**
     * <h2> 若想Excel 中 生成多个sheet, 请使用此方法生成 WriteSheet </h2>
     * 这样外层定义好ExcelWriter 就可以写多个 WriteSheet了 <br>
     */
    public static WriteSheet generateWriteSheet(Integer sheetNo, String sheetName) {
        return EasyExcel.writerSheet(sheetNo, sheetName).build();
    }

    public static WriteSheet generateWriteSheet(String sheetName) {
        return EasyExcel.writerSheet(sheetName).build();
    }

    /**
     * <h2> 获取ExcelWriter </h2>
     * 它可以写多个 WriteSheet , 即一个Excel中有多个sheet页, 适用于分页数据查询, 每一页对应一个sheet/或者一组固定数据量(10w)数据作为一个sheet <br>
     */
    public static ExcelWriter getExcelWriter(OutputStream outputStream, ExcelTypeEnum type, List<WriteHandler> handlers, List<Converter<?>> converters, Class<?> head, List<String> headString) {

        ExcelWriterBuilder excelWriterBuilder = getExcelWriterBuilder(outputStream, type, head, headString);
        if (!CollectionUtils.isEmpty(handlers)) {
            handlers.forEach(excelWriterBuilder::registerWriteHandler);
        }
        if (!CollectionUtils.isEmpty(converters)) {
            converters.forEach(excelWriterBuilder::registerConverter);
        }
        return excelWriterBuilder.build();
    }

    public static ExcelWriterBuilder getExcelWriterBuilder(OutputStream outputStream, ExcelTypeEnum type, Class<?> head, List<String> headString) {

        ExcelWriterBuilder excelWriterBuilder = EasyExcel.write(outputStream).autoCloseStream(false).excelType(type);
        if (null != head) {
            excelWriterBuilder.head(head);
        } else if (!CollectionUtils.isEmpty(headString)) {
            List<List<String>> headList = generateHeadList(headString);
            excelWriterBuilder.head(headList);
        }
        return excelWriterBuilder;
    }

    /**
     * <h2> 获取ExcelWriter (需要实体对象-Excel表头需要实体类上的注解支撑)  </h2>
     */
    public static ExcelWriter getExcelWriterEntity(OutputStream outputStream, ExcelTypeEnum type, Class<?> head) {

        return getExcelWriter(outputStream, type, defaultWriteHandlers(), null, head, null);
    }

    public static ExcelWriter getExcelWriterEntity(OutputStream outputStream, ExcelTypeEnum type, List<WriteHandler> handlers, List<Converter<?>> converters, Class<?> head) {

        if (CollectionUtils.isEmpty(handlers)) {
            handlers = defaultWriteHandlers();
        }
        return getExcelWriter(outputStream, type, handlers, converters, head, null);
    }

    /**
     * <h2> 获取ExcelWriter (不需要实体对象)  </h2>
     */
    public static ExcelWriter getExcelWriterNoEntity(OutputStream outputStream, ExcelTypeEnum type, List<String> headString) {

        return getExcelWriter(outputStream, type, defaultWriteHandlers(), null, null, headString);
    }

    public static ExcelWriter getExcelWriterNoEntity(OutputStream outputStream, ExcelTypeEnum type, List<WriteHandler> handlers, List<Converter<?>> converters, List<String> headString) {

        if (CollectionUtils.isEmpty(handlers)) {
            handlers = defaultWriteHandlers();
        }
        return getExcelWriter(outputStream, type, handlers, converters, null, headString);
    }


    // 下面方法主要是导出Excel(只生成单个 sheet页) 直接导出

    /**
     * <h2> web写Excel </h2>
     * 依赖于实体对象 <br>
     *
     * @param outputStream    输出流
     * @param type            写Excel的类型 {@link ExcelTypeEnum}
     * @param autoCloseStream 是否自动关闭流
     * @param head            Excel表头类
     * @param sheetNo         sheet页码
     * @param sheetName       sheet页名称
     * @param handlers        写Excel 自定义处理器
     * @param converters      格式转化器(如果不传,默认使用EasyExcel的29种(3.0.5版本))
     * @param data            需要写的Excel数据
     */
    public static void writeExcel(OutputStream outputStream, ExcelTypeEnum type, boolean autoCloseStream, Integer sheetNo, String sheetName, List<WriteHandler> handlers,
                                  List<Converter<?>> converters, Class<?> head, List<?> data) {
        ExcelWriterSheetBuilder sheet = getExcelWriterSheetBuilder(outputStream, type, autoCloseStream, sheetNo, sheetName, handlers, converters, head, null);
        sheet.doWrite(data);
    }

    public static void writeExcel(OutputStream outputStream, ExcelTypeEnum type, String sheetName, List<WriteHandler> handlers, List<Converter<?>> converters, Class<?> head, List<?> data) {

        writeExcel(outputStream, type, true, 1, sheetName, handlers, converters, head, data);
    }

    public static void writeExcel(OutputStream outputStream, String sheetName, List<WriteHandler> handlers, List<Converter<?>> converters, Class<?> head, List<?> data) {

        writeExcel(outputStream, ExcelTypeEnum.XLSX, true, 1, sheetName, handlers, converters, head, data);
    }

    /**
     * <h2> web写Excel 默认方法 </h2>
     */
    public static void writeExcel(OutputStream outputStream, String sheetName, Class<?> head, List<?> data) {

        writeExcel(outputStream, ExcelTypeEnum.XLSX, true, 1, sheetName, defaultWriteHandlers(), null, head, data);
    }

    public static void writeExcel(OutputStream outputStream, ExcelTypeEnum type, String sheetName, Class<?> head, List<?> data) {

        writeExcel(outputStream, type, true, 1, sheetName, defaultWriteHandlers(), null, head, data);
    }

    /**
     * <h2> web写Excel 默认方法 </h2>
     * 沒有设置自定义 WriteHandler 的时候,需要加EasyExcel的特定注解在实体类对象上才可以设置 字体、宽度、高度等 <br>
     */
    public static void writeExcelWithNoHandler(OutputStream outputStream, String sheetName, Class<?> head, List<?> data) {

        writeExcel(outputStream, ExcelTypeEnum.XLSX, true, 1, sheetName, null, null, head, data);
    }

    public static void writeExcelWithNoHandler(OutputStream outputStream, ExcelTypeEnum type, String sheetName, Class<?> head, List<?> data) {

        writeExcel(outputStream, type, true, 1, sheetName, null, null, head, data);
    }


    /**
     * <h2> 获取ExcelWriteSheet </h2>
     */
    private static ExcelWriterSheetBuilder getExcelWriterSheetBuilder(OutputStream outputStream, ExcelTypeEnum type, boolean autoCloseStream, Integer sheetNo, String sheetName,
                                                                      List<WriteHandler> handlers, List<Converter<?>> converters, Class<?> headClass, List<String> head) {
        ExcelWriterBuilder excelWriterBuilder = EasyExcel.write().excelType(type).autoCloseStream(autoCloseStream);
        if (null != outputStream) {
            excelWriterBuilder.file(outputStream);
        }
        if (null != headClass) {
            excelWriterBuilder.head(headClass);
        } else {
            List<List<String>> headList = generateHeadList(head);
            excelWriterBuilder.head(headList);
        }
        ExcelWriterSheetBuilder sheet = excelWriterBuilder.sheet(sheetNo, sheetName);
        if (!CollectionUtils.isEmpty(handlers)) {
            handlers.forEach(sheet::registerWriteHandler);
        }
        if (!CollectionUtils.isEmpty(converters)) {
            converters.forEach(sheet::registerConverter);
        }
        return sheet;
    }

    /**
     * <h2> web写Excel (不创建对象的写Excel) </h2>
     *
     * @param outputStream    输出流
     * @param type            写Excel的类型 {@link ExcelTypeEnum}
     * @param autoCloseStream 是否自动关闭流
     * @param head            Excel表头
     * @param sheetName       sheet页名称
     * @param handlers        写Excel 自定义处理器
     * @param converters      自定义格式转化器
     * @param data            需要写的Excel数据
     */
    public static void writeExcelNoEntity(OutputStream outputStream, ExcelTypeEnum type, boolean autoCloseStream, Integer sheetNo, String sheetName, List<WriteHandler> handlers,
                                          List<Converter<?>> converters, List<String> head, List<List<Object>> data) {
        ExcelWriterSheetBuilder sheetBuilder = getExcelWriterSheetBuilder(outputStream, type, autoCloseStream, sheetNo, sheetName, handlers, converters, null, head);
        sheetBuilder.doWrite(data);
    }

    public static void writeExcelNoEntity(OutputStream outputStream, String sheetName, List<WriteHandler> handlers, List<Converter<?>> converters, List<String> head, List<List<Object>> data) {

        writeExcelNoEntity(outputStream, ExcelTypeEnum.XLSX, true, 1, sheetName, handlers, converters, head, data);
    }

    /**
     * <h2> web写Excel 默认方法 </h2>
     */
    public static void writeExcelNoEntity(OutputStream outputStream, String sheetName, List<String> head, List<List<Object>> data) {

        writeExcelNoEntity(outputStream, ExcelTypeEnum.XLSX, true, 1, sheetName, defaultWriteHandlers(), null, head, data);
    }

    public static void writeExcelNoEntity(OutputStream outputStream, ExcelTypeEnum type, String sheetName, List<String> head, List<List<Object>> data) {

        writeExcelNoEntity(outputStream, type, true, 1, sheetName, defaultWriteHandlers(), null, head, data);
    }

    public static void writeExcelNoEntityWithHandler(OutputStream outputStream, String sheetName, List<String> head, List<List<Object>> data) {

        writeExcelNoEntity(outputStream, ExcelTypeEnum.XLSX, true, 1, sheetName, null, null, head, data);
    }

    /**
     * <h2> 生成 </h2>
     */
    private static List<List<String>> generateHeadList(List<String> head) {

        List<List<String>> headList = new ArrayList<>();
        head.forEach(e -> {
            List<String> headNum = new ArrayList<>();
            headNum.add(e);
            headList.add(headNum);
        });
        return headList;
    }


    /**
     * <h2> 设置Excel 表头 和 内容 字体样式 </h2>
     * <p>
     * eg. 列宽、行高等需要用注解实现(需要打在head 类上) </br>
     * 相关注解: {@link HeadRowHeight 头行高}、{@link ContentRowHeight 内容行高}、{@link ColumnWidth 列宽} </br>
     *
     * @param headFontHeightInPoints    头字体大小
     * @param contentFontHeightInPoints 内容字体大小
     */
    private static WriteHandler setFontStyle(int headFontHeightInPoints, int contentFontHeightInPoints) {
        // 头的策略
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontName("Arial");
        // 头字体大小
        headWriteFont.setFontHeightInPoints((short) headFontHeightInPoints);
        // 头加粗
        headWriteFont.setBold(true);
        headWriteCellStyle.setWriteFont(headWriteFont);
        // 内容的策略
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        WriteFont contentWriteFont = new WriteFont();
        contentWriteFont.setFontName("Arial");
        // 内容字体大小
        contentWriteFont.setFontHeightInPoints((short) contentFontHeightInPoints);
        // 内容样式
        contentWriteCellStyle.setWriteFont(contentWriteFont);
        // 水平居中
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
        return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
    }

    /**
     * <h2> 设置表头行高度、内容行高度 </h2>
     *
     * @param headRowHeight    表头行高度
     * @param contentRowHeight 内容行高度
     */
    private static WriteHandler setRowHeight(int headRowHeight, int contentRowHeight) {

        return new SimpleRowHeightStyleStrategy((short) headRowHeight, (short) contentRowHeight);
    }


    /**
     * <h2> 设置表头、内容列宽度 </h2>
     *
     * @param columnWidth 列宽度
     */
    private static WriteHandler setWidth(Integer columnWidth) {

        return new SimpleColumnWidthStyleStrategy(columnWidth);
    }

    /**
     * <h2> 默认写Excel handler组合设置 </h2>
     */
    private static List<WriteHandler> defaultWriteHandlers() {
        List<WriteHandler> writeHandlers = new ArrayList<>();
        writeHandlers.add(setFontStyle(11, 11));
        writeHandlers.add(setRowHeight(16, 16));
        writeHandlers.add(setWidth(24));
        return writeHandlers;
    }
}
