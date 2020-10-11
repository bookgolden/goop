package io.renren.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期处理
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年12月21日 下午12:53:33
 */
public class DateUtils {
    /**
     * 时间格式(yyyy-MM-dd)
     */
    public final static String DATE_PATTERN = "yyyy-MM-dd";
    /**
     * 时间格式(yyyy-MM-dd HH:mm:ss)
     */
    public final static String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static String format_D(Date date) {
        return format_D(date, DATE_PATTERN);
    }

    public static String format_DT(Date date) {
        return format_D(date, DATE_TIME_PATTERN);
    }

    public static String format_D(Date date, String pattern) {
        SimpleDateFormat df = null;
        if (date != null) {
            df = new SimpleDateFormat(pattern);
        }
        return df.format(date);
    }
}
