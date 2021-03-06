/**
 * @作者 Mcj
 */
package com.zqykj.app.service.tools;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <h1> Excel导出 web HttpServletResponse 设置 </h1>
 */
@Slf4j
public class ExcelExport {

    /**
     * <h2> HttpServletResponse设置 </h2>
     */
    public static void withResponse(HttpServletResponse response, String fileName) {
        setResponse(response, fileName, StandardCharsets.UTF_8.name());
    }

    /**
     * <h2> HttpServletResponse设置 </h2>
     *
     * @param response HttpServletResponse
     * @param fileName Excel 名称
     * @param enc      内容编码
     */
    public static void setResponse(HttpServletResponse response, String fileName, String enc) {

        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(enc);
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        try {
            String name = URLEncoder.encode(fileName, enc).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=" + enc + "''" + name + ".xlsx");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            log.error("This encoding is not supported!");
        }
    }

    /**
     * <h2> 获取导出文件名称 </h2>
     * 原天网设置如此 <br>
     */
    public static String getExcelFileName(String originName) {
        String format = LocalDateTime.now().format(DateTimeFormatter.ofPattern("_yyyyMMdd_HHmmss"));
        // SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);
        return originName + format;
    }
}
