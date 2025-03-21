package org.armada.galileo.nova_flow.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaobo
 * @date 2021/10/13 10:41 上午
 */
@Slf4j
public class FlowCommonUtil {

    private static final Object lockObj = new Object();

    private static Map<String, ThreadLocal<SimpleDateFormat>> sdfMap = new HashMap<String, ThreadLocal<SimpleDateFormat>>();

    public static SimpleDateFormat getSdf(final String pattern) {

        ThreadLocal<SimpleDateFormat> tl = sdfMap.get(pattern);

        // 此处的双重判断和同步是为了防止sdfMap这个单例被多次put重复的sdf
        if (tl == null) {
            synchronized (lockObj) {
                tl = sdfMap.get(pattern);
                if (tl == null) {
                    tl = new ThreadLocal<SimpleDateFormat>() {
                        protected SimpleDateFormat initialValue() {
                            return new SimpleDateFormat(pattern);
                        }
                    };
                    sdfMap.put(pattern, tl);
                }
            }
        }

        return tl.get();
    }


    public static Date parse(String date) {
        return parse(date, "yyyy-MM-dd");
    }

    /**
     * 默认的时间格式化 yyyy-MM-dd
     *
     * @param date
     * @return
     */
    public static String format(Date date) {
        return format(date, "yyyy-MM-dd");
    }

    public static String formatDate(Integer mill, String format) {
        if (mill == null || format == null) {
            return null;
        }
        return format(new Date(Long.valueOf(mill) * 1000l), format);
    }

    public static String format(String format, Object... args) {
        return MessageFormatter.arrayFormat(format, args).getMessage();
    }

    /**
     * 时间格式化
     *
     * @param date 需要格式化的时间
     * @param format 时间格式
     * @return
     */
    public static String format(Date date, String format) {
        return getSdf(format).format(date);
    }

    /**
     * 时间格式化
     *
     * @param date 需要格式化的时间
     * @param format 时间格式
     * @return
     */
    public static Date parse(String date, String format) {
        try {
            return getSdf(format).parse(date);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }
}
