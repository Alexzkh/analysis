package com.zqykj.app.service.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * @Description: 单sheet页文件导出功能
 * @Author zhangkehou
 * @Date 2021/10/14
 */
public abstract class SingleSheetExcelFileExportTask<T> extends FileExportTask {

    @JsonIgnore
    private String sheetName;

    @JsonIgnore
    private List<T> datas;

    public SingleSheetExcelFileExportTask(String fileName, String sheetName, List<T> datas) {
        this.sheetName = sheetName;
        this.datas = datas;
        setFileName(fileName);
    }

    public SingleSheetExcelFileExportTask(String sheetName, List<T> datas) {
        this.sheetName = sheetName;
        this.datas = datas;
        String fileName = String.valueOf(System.currentTimeMillis());
        setFileName(fileName);
    }

    @Override
    protected File exportData2LocalFile() {
        String filePath = FileUtils.getTempDirectoryPath() + File.separator + getFileName() + ".xlsx";
        File excel = new File(filePath);
        OutputStream out = null;
        FileOutputStream fileOut = null;
        SXSSFWorkbook workbook = new SXSSFWorkbook(1000);
        workbook.setCompressTempFiles(true);
        try {
            exportDatas2Excel(datas, workbook, sheetName);

            fileOut = new FileOutputStream(excel);
            out = new BufferedOutputStream(fileOut);
            workbook.write(out);
        } catch (Exception e) {
            logger.error("exportData2LocalFile error", e);
        } finally {
            if (fileOut != null) {
                IOUtils.closeQuietly(fileOut);
            }
            if (out != null) {
                IOUtils.closeQuietly(out);
            }
            if (workbook != null) {
                workbook.dispose();
                IOUtils.closeQuietly(workbook);
            }
        }
        return excel;
    }

    private void exportDatas2Excel(List<T> datas, SXSSFWorkbook workbook, String sheetName) {

        List<String> headerNames = Lists.newArrayList(getSheetHeaders());

        // create sheet
        SXSSFSheet sheet = createExcelSheetWithFixedHeader(workbook, headerNames, sheetName);

        if (CollectionUtils.isEmpty(datas)) {
            return;
        }
        // write data to sheet
        int colCount = headerNames.size();
        int rowIndex = 1;
        for (T data : datas) {
            Row row = sheet.createRow(rowIndex);
            String[] rowData = data2Array(data);
            for (int c = 0; c < colCount; c++) {
                row.createCell(c).setCellValue(rowData[c]);
            }
            rowIndex++;
        }
    }

    protected abstract String[] data2Array(T data);

    protected abstract String[] getSheetHeaders();

    private SXSSFSheet createExcelSheetWithFixedHeader(final SXSSFWorkbook workbook, List<String> headerNames,
                                                       String sheetName) {
        Font titleFont = workbook.createFont();
        titleFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        CellStyle titleCellStyle = workbook.createCellStyle();
        titleCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        titleCellStyle.setFont(titleFont);

        SXSSFSheet sheet = workbook.createSheet(sheetName);
        Row header = sheet.createRow(0);
        int index = 0;
        for (String h : headerNames) {
            Cell pcell = header.createCell(index);
            pcell.setCellStyle(titleCellStyle);
            pcell.setCellValue(h);
            index++;
        }

        return sheet;
    }

    protected String percentConvert(double d) {
        try {
            d = d * 100;
            return String.format("%.2f", d) + "%";
        } catch (NumberFormatException e) {
            return "0.00%";
        }
    }

    protected String doubleFormat(double d) {
        return String.format("%.2f", d);
    }

    public List<T> getDatas() {
        return datas;
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
    }
}