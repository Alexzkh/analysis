package com.zqykj.infrastructure.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @Description: 日期工具类
 * @Author zhangkehou
 * @Date 2022/1/5
 */
public class DateUtils {

    /**
     * 判断一年的第几周
     * @param datetime
     * @return
     * @throws java.text.ParseException
     */
    public static Integer whatWeek(String datetime) throws java.text.ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = format.parse(datetime);
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(date);
        Integer weekNumber = calendar.get(Calendar.WEEK_OF_YEAR);
        return weekNumber;
    }
}
