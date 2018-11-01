package com.weixin.pay.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间处理工具类
 *
 * @author yclimb
 * @date 2018/11/1
 */
public class DateTimeUtil {

    public static final String TIME_FORMAT_SHORT = "yyyyMMddHHmmss";
    public static final String TIME_FORMAT_SHORT_HOUR = "yyyyMMddHH";
    public static final String TIME_FORMAT_YMD = "yyyy/MM/dd HH:mm:ss";
    public static final String TIME_FORMAT_NORMAL = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_FORMAT_ENGLISH = "MM/dd/yyyy HH:mm:ss";
    public static final String TIME_FORMAT_CHINA = "yyyy年MM月dd日 HH时mm分ss秒";
    public static final String TIME_FORMAT_CHINA_M = "yyyy年MM月dd日 HH时mm分";
    public static final String TIME_FORMAT_CHINA_S = "yyyy年M月d日 H时m分s秒";
    public static final String TIME_FORMAT_SHORT_S = "HH:mm:ss";

    public static final String DATE_FORMAT_SHORT = "yyyyMMdd";
    public static final String DATE_FORMAT_NORMAL = "yyyy-MM-dd";
    public static final String DATE_FORMAT_ENGLISH = "MM/dd/yyyy";
    public static final String DATE_FORMAT_CHINA = "yyyy年MM月dd日";
    public static final String DATE_FORMAT_CHINA_YEAR_MONTH = "yyyy年MM月";
    public static final String MONTH_FORMAT = "yyyyMM";
    public static final String YEAR_MONTH_FORMAT = "yyyy-MM";
    public static final String DATE_FORMAT_MINUTE = "yyyyMMddHHmm";
    public static final String MONTH_DAY_FORMAT = "MM-dd";
    public static final String YEAR_FORMAT = "yyyy";
    public static final String TIME_FORMAT_TIME = "yyyy/MM/dd HH:mm";
    private static final SimpleDateFormat sdf = new SimpleDateFormat(
            DATE_FORMAT_NORMAL);

    private static final SimpleDateFormat sdfTime = new SimpleDateFormat(
            TIME_FORMAT_NORMAL);

    private static final SimpleDateFormat sdfTimes = new SimpleDateFormat(
            "yyyyMMddHHmmssSSS");

    private static final SimpleDateFormat sdfTChina = new SimpleDateFormat(
            TIME_FORMAT_CHINA);

    /**
     * 把日期字符串转换为日期类型
     *
     * @param dateStr 日期字符串
     * @return 日期
     * @since 0.1
     */
    public static Date convertAsDate(String dateStr) {
        if (dateStr == null || "".equals(dateStr)) {
            return null;
        }
        DateFormat fmt = null;
        if (dateStr.matches("\\d{14}")) {
            fmt = new SimpleDateFormat(TIME_FORMAT_SHORT);
        } else if (dateStr
                .matches("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}")) {
            fmt = new SimpleDateFormat(TIME_FORMAT_NORMAL);
        } else if (dateStr
                .matches("\\d{1,2}/\\d{1,2}/\\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}")) {
            fmt = new SimpleDateFormat(TIME_FORMAT_ENGLISH);
        } else if (dateStr
                .matches("\\d{4}年\\d{1,2}月\\d{1,2}日 \\d{1,2}时\\d{1,2}分\\d{1,2}秒")) {
            fmt = new SimpleDateFormat(TIME_FORMAT_CHINA);
        } else if (dateStr.matches("\\d{8}")) {
            fmt = new SimpleDateFormat(DATE_FORMAT_SHORT);
        } else if (dateStr.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
            fmt = new SimpleDateFormat(DATE_FORMAT_NORMAL);
        } else if (dateStr.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
            fmt = new SimpleDateFormat(DATE_FORMAT_ENGLISH);
        } else if (dateStr.matches("\\d{4}年\\d{1,2}月\\d{1,2}日")) {
            fmt = new SimpleDateFormat(DATE_FORMAT_CHINA);
        } else if (dateStr.matches("\\d{4}\\d{1,2}\\d{1,2}\\d{1,2}\\d{1,2}")) {
            fmt = new SimpleDateFormat(DATE_FORMAT_MINUTE);
        } else if (dateStr.matches("\\d{1,2}:\\d{1,2}:\\d{1,2}")) {
            fmt = new SimpleDateFormat(TIME_FORMAT_SHORT_S);
        }
        try {
            return fmt.parse(dateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException(
                    "Date or Time String is invalid.");
        }
    }

    /**
     * 得到时间字符串,格式为 yyyyMMddHHmmss
     * @return 返回当前时间的字符串
     *
     * @author yclimb
     * @date 2018/11/1
     */
    public static String getTimeShortString(Date date) {
        return new SimpleDateFormat(TIME_FORMAT_SHORT).format(date);
    }

    /**
     * 得到十位数的时间戳
     * @param date 时间对象
     * @return long
     *
     * @author yclimb
     * @date 2018/9/18
     */
    public static long getTenTimeByDate(Date date) {
        return date.getTime() / 1000;
    }

    /**
     * 得到十位数的时间戳
     * @param dateStr 时间字符串
     * @return long
     *
     * @author yclimb
     * @date 2018/9/18
     */
    public static long getTenTimeByDate(String dateStr) {
        return convertAsDate(dateStr).getTime() / 1000;
    }


    /**
     * Description: 比较两个字符串格式的时间大小<br/>
     * 如果第二个时间大于第一个时间返回true,否则返回false
     *
     * @param strFirst  第一个时间
     * @param strSecond 第二个时间
     * @param strFormat 时间格式化方式 eg:"yyyy-MM-dd HH:mm:ss"," yyyy-MM-dd"
     * @return true-第二个时间晚于第一个时间,false-第二个时间不晚于第一个时间
     *
     * @author yclimb
     * @date 2018/11/1
     */
    public static boolean latterThan(String strFirst, String strSecond,
                                     String strFormat) {
        SimpleDateFormat ft = new SimpleDateFormat(strFormat);
        try {
            Date date1 = ft.parse(strFirst);
            Date date2 = ft.parse(strSecond);
            long quot = date2.getTime() - date1.getTime();
            if (0 < quot) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

}
