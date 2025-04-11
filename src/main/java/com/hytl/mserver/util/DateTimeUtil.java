package com.hytl.mserver.util;

import java.time.format.DateTimeFormatter;

/**
 * 时间工具类
 */
public class DateTimeUtil {
    /**
     * yyyy-MM-dd HH:mm:ss
     */
    public static final DateTimeFormatter DATETIME_FORMATTER_1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * yyyyMMddHHmmss
     */
    public static final DateTimeFormatter DATETIME_FORMATTER_3 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    /**
     * yyyyMMdd
     */
    public static final DateTimeFormatter DATE_FORMATTER_2 = DateTimeFormatter.ofPattern("yyyyMMdd");
}
