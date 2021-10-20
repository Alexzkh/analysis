package com.zqykj.infrastructure.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Description: excel 文件名称工具类。
 * @Author zhangkehou
 * @Date 2021/10/14
 */
public class ExcelFileNameUtil {

    public static final String PATTERN = "_yyyyMMdd_HHmmss";

    /**
     * 获取excel文件名称，规则为原始文件名+当前日期时间.
     *
     * @param originName: 文件原始名称
     * @return: java.lang.String
     **/
    public static String getExcelFileName(String originName) {
        String format = LocalDateTime.now().format(DateTimeFormatter.ofPattern(PATTERN));
        return originName + format;
    }
}
