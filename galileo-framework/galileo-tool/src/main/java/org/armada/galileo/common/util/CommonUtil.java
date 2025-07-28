package org.armada.galileo.common.util;

import java.io.*;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.armada.galileo.common.hash.MurmurHash;
import org.armada.galileo.common.loader.ConfigLoader;
import org.armada.galileo.common.page.PageList;
import org.armada.galileo.common.page.PageParam;
import org.armada.galileo.common.page.ThreadPagingUtil;
import org.armada.galileo.exception.BizException;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.ReflectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonUtil {

    public static final long millMonth = 30l * 24l * 60l * 60l * 1000l;

    public static final long millweek = 7l * 24l * 60l * 60l * 1000l;

    public static final long millDay = 24l * 60l * 60l * 1000l;

    public static final long millHour = 60l * 60l * 1000l;

    public static final long millMinute = 60l * 1000l;

    public static final long millSecond = 1000l;

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private static Map<String, DateFormat> formats = new ConcurrentHashMap<String, DateFormat>();

    private static final Object lockObj = new Object();

    private static final String df = "yyyy-MM-dd";
    private static final String df2 = "yyyy-MM-dd HH:mm:ss";
    private static final String df3 = "yyyy-MM-dd HH:mm:ss.S";

    static CommonUtil util = new CommonUtil();

    public static CommonUtil instance() {
        return util;
    }

    private static Map<String, ThreadLocal<SimpleDateFormat>> sdfMap = new HashMap<String, ThreadLocal<SimpleDateFormat>>();

    public static <T> List<T> asList(T... a) {
        ArrayList<T> list = new ArrayList<T>(a.length);
        for (T t : a) {
            list.add(t);
        }
        return list;
    }

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

    public static String replaceAll(String input, String oldChars, String newChars) {

        if (input == null) {
            return input;
        }
        if (oldChars == null || oldChars.length() == 0) {
            return input;
        }

        if (newChars == null) {
            return input;
        }

        StringBuilder sb = new StringBuilder();

        int start = 0;
        int tmpIndex = 0;

        int len = oldChars.length();

        for (; ; ) {

            tmpIndex = input.indexOf(oldChars, start);

            if (tmpIndex != -1) {

                if (tmpIndex > 0) {
                    if (tmpIndex > start) {
                        sb.append(input.substring(start, tmpIndex));
                    }
                    sb.append(newChars);
                } else {
                    sb.append(newChars);
                }

                start = tmpIndex + len;
            } else {

                if (start == 0) {
                    return input;
                } else {
                    sb.append(input.substring(start, input.length()));
                }

                break;
            }

        }

        return sb.toString();
    }

    /**
     * ip地址转成整数.
     *
     * @param ip
     * @return
     */
    public static long ip2long(String ip) {
        String[] ips = ip.split("[.]");
        long num = 16777216L * Long.parseLong(ips[0]) + 65536L * Long.parseLong(ips[1]) + 256 * Long.parseLong(ips[2]) + Long.parseLong(ips[3]);
        return num;
    }

    // A类
    private static long lanA1 = ip2long("10.0.0.0");
    private static long lanA2 = ip2long("10.255.255.255");

    // B类
    private static long lanB1 = ip2long("172.16.0.0");
    private static long lanB2 = ip2long("172.31.255.255");

    // C类
    private static long lanC1 = ip2long("192.168.0.0");
    private static long lanC2 = ip2long("192.168.255.255");

    /**
     * 是否为内网IP
     *
     * @param ip
     * @return
     */
    public static boolean isLanIp(String ip) {
        try {

            long ipNumber = ip2long(ip);

            if (lanA1 <= ipNumber && ipNumber <= lanA2) {
                return true;
            }
            if (lanB1 <= ipNumber && ipNumber <= lanB2) {
                return true;
            }
            if (lanC1 <= ipNumber && ipNumber <= lanC2) {
                return true;
            }

        } catch (Exception e) {
            return true;
        }

        return false;
    }

    /**
     * 获取IP地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = null;
        try {

            ip = request.getHeader("Proxy-Nodeway-Ip");
            if (ip != null && ip.length() != 0 && !("unknown".equalsIgnoreCase(ip)) && !("127.0.0.1".equalsIgnoreCase(ip))) {
                return ip;
            }

            ip = request.getHeader("X-Real-IP");
            if (ip != null && ip.length() != 0 && !("unknown".equalsIgnoreCase(ip)) && !("127.0.0.1".equalsIgnoreCase(ip))) {
                return ip;
            }

            ip = request.getRemoteHost();
            if (!isLanIp(ip)) {
                return ip;
            }

            return ip;
        } catch (Exception e) {
            log.warn("没有获取到ip");
            return null;
        }
    }

    /**
     * 驼蜂转下划线
     *
     * @param input
     * @return
     */
    public static String convertJavaField2DB(String input) {

        if (input == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (char c : input.toCharArray()) {

            int asscii = (int) c;
            if (asscii >= 65 && asscii <= 90) {
                if (index == 0) {
                    sb.append((char) (asscii + 32));
                } else {
                    sb.append("_");
                    sb.append((char) (asscii + 32));
                }
            } else {
                sb.append(c);
            }

            index++;
        }
        return sb.toString();

    }

    public static String convertDBField2Jave(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean needUppper = false;
        for (char c : input.toCharArray()) {
            if (c == '_') {
                needUppper = true;
                continue;
            } else {
                if (needUppper) {
                    int asscii = (int) c;
                    if (asscii >= 97 && asscii <= 122) {
                        sb.append((char) (c - 32));
                    } else {
                        sb.append(c);
                    }
                } else {
                    sb.append(c);
                }
                needUppper = false;
            }

        }
        return sb.toString();
    }


    /**
     * 判断输入是否为 email
     *
     * @param input
     * @return
     */
    public static boolean isEmail(String input) {
        if (input == null) {
            return false;
        }
        return input.matches("^[a-zA-Z0-9\\._-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
    }

    /**
     * 判断是否为空字符串
     *
     * @param input
     * @return
     */
    public static Boolean isEmpty(String input) {
        if (input == null || input.matches("\\s*") || input.trim().equals("null")) {
            return true;
        }
        return false;
    }

    public static Boolean isEmpty(Object input) {
        if (input == null) {
            return true;
        }
        if (input instanceof String) {
            return ((String) input).matches("\\s*") || ((String) input).trim().equals("null");
        } else if (input instanceof List) {
            return ((List<?>) input).isEmpty();
        } else if (input instanceof Map) {
            return ((Map<?, ?>) input).isEmpty();
        } else if (input instanceof Set) {
            return ((Set<?>) input).isEmpty();
        }
        return false;
    }

    public static Boolean isEmpty(Integer input) {
        if (input == null) {
            return true;
        }
        return false;
    }

    public static Boolean isEmpty(Collection<?> collection) {
        if (collection == null || collection.size() == 0) {
            return true;
        }
        return false;
    }

    public static Boolean isEmpty(BigDecimal input) {
        if (input == null) {
            return true;
        }
        return false;
    }

    /**
     * 判断输入字符串是否为手机号
     *
     * @param phone
     * @return
     */
    public static Boolean isPhone(String phone) {
        if (phone == null) {
            return false;
        }
        if (phone.matches("1\\d{10}")) {
            return true;
        }
        return false;
    }

    public static Boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    public static Boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static Boolean isNotEmpty(Double input) {
        return !isEmpty(input);
    }

    public static Boolean isNotEmpty(BigDecimal input) {
        return !isEmpty(input);
    }

    private static boolean isEmpty(Double input) {
        return input == null;
    }

    public static Boolean isNotEmpty(Integer input) {
        return !isEmpty(input);
    }

    public static Boolean isNotEmpty(Map<?, ?> collection) {
        return !isEmpty(collection);
    }

    public static Boolean isEmpty(Map<?, ?> map) {
        if (map == null) {
            return true;
        }
        if (map.size() == 0) {
            return true;
        }
        return false;
    }

    public static Boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static Boolean isNull(Object obj) {
        return obj == null;
    }

    public static Boolean isNotNull(Object obj) {
        return obj != null;
    }


    public static boolean isMobileNO(String mobiles) {

        if (isEmpty(mobiles)) {
            return false;
        }
//        Pattern p = Pattern.compile("^1[123456789]\\d{9}$");
//        // Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
//        Matcher m = p.matcher(mobiles);
//        return m.matches();

        return mobiles.matches("^1\\d{10}$");
    }

    /**
     * 时间格式化
     *
     * @param date   需要格式化的时间
     * @param format 时间格式
     * @return
     */
    public static Date parse(String date, String format) {
        try {
            return getSdf(format).parse(date);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }


    public static Date parse(String date) {
        try {
            return getSdf("yyyy-MM-dd").parse(date);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

//    public String format(Double input, String format){
//        return new DecimalFormat(format).format(input);
//    }
//
//    public String format(BigDecimal input, String format){
//        return new DecimalFormat(format).format(input);
//    }
//

    public static String format(Number input, String format) {
        return new DecimalFormat(format).format(input);
    }

    /**
     * 时间格式化
     *
     * @param date   需要格式化的时间
     * @param format 时间格式
     * @return
     */
    public static String format(Date date, String format) {
        return getSdf(format).format(date);
    }

    public static String format(Date date) {
        return getSdf("yyyy-MM-dd").format(date);
    }


    public static String formatTimestamp(Long mill, String format) {
        return getSdf(format).format(new Date(mill));
    }

    public static String formatTimestamp(Long mill) {
        return getSdf("yyyy-MM-dd").format(new Date(mill));
    }

    public static <T> T transfer(Object obj, Class<T> cls) {
        if (obj == null) {
            return null;
        }
        try {
            T t = (T) cls.newInstance();
            copyPropertiesIgnoreNull(obj, t);
            return t;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static <T> List<T> transfer(List<? extends Object> list, Class<T> cls) {
        if (list == null) {
            return null;
        }
        try {
            List<T> result = new ArrayList<>(list.size());
            for (Object obj : list) {
                T t = (T) cls.newInstance();
                copyPropertiesIgnoreNull(obj, t);
                result.add(t);
            }
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    public static void copyPropertiesIgnoreNull(Object src, Object target) {
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }

    /**
     * 获取匹配的字符串列表
     *
     * @param input
     * @param regex
     * @return
     */
    public static List<String> getMatchedStrs(String input, String regex) {
        if (input == null) {
            return null;
        }
        List<String> list = new ArrayList<String>();
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);
        while (m.find()) {
            String str = m.group();
            list.add(str.trim());
        }
        return list;
    }

    /**
     * 采用正则分组的正则匹配相应的字符串
     *
     * @param input
     * @param regex 需带有 ()
     * @return
     */
    public static String getMatchedByGroup(String input, String regex) {
        Pattern pat = Pattern.compile(regex);
        Matcher mat = pat.matcher(input);
        if (mat.find()) {
            return (mat.group(1));
        }
        return null;
    }


    public static String join(Object[] list, String split) {
        if (split == null) {
            split = ",";
        }
        if (list == null || list.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Object object : list) {
            if (object == null) {
                continue;
            }

            sb.append(object.toString());
            if (i != list.length - 1) {
                sb.append(split);
            }
            i++;
        }

        String result = sb.toString();

        return result;
    }

    public static String join(List<? extends Object> list, String split) {
        if (split == null) {
            split = ",";
        }
        if (list == null || list.size() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Object object : list) {
            if (object == null) {
                continue;
            }

            sb.append(object.toString());
            if (i != list.size() - 1) {
                sb.append(split);
            }
            i++;
        }

        String result = sb.toString();

        return result;
    }

    /**
     * 从项目classpath目录读取文件
     *
     * @param filePath 不要以/开头
     * @return
     */
    public static String readFileToString(String filePath) {
        String tpl = null;
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        try {
            is = ConfigLoader.loadResource(filePath, false, false);

            if (is == null) {
                log.warn("file not exist: " + filePath);
                return null;
            }

            bos = new ByteArrayOutputStream();

            byte[] buf = new byte[4096];
            int len = 0;
            while ((len = is.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }

            tpl = new String(bos.toByteArray(), "utf-8");

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error("close input stream fail");
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    log.error("close output stream fail");
                }
            }
        }
        return tpl;
    }


    public static InputStream readFileInputStream(String filePath) {
        InputStream is = ConfigLoader.loadResource(filePath, false, false);

        if (is == null) {
            log.error("file not exist: " + filePath);
            return null;
        }
        return is;
    }

    /**
     * @param path 本地绝对路径
     * @return
     */
    public static String readFileContent(String path) {

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "utf-8"));
            String tmp = null;
            StringBuilder sb = new StringBuilder();
            while ((tmp = br.readLine()) != null) {
                sb.append(tmp).append("\n");
            }
            br.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 从项目classpath目录读取文件
     *
     * @param filePath 不要以/开头
     * @return
     */
    public static byte[] readFileToBuffer(String filePath) {
        byte[] result = null;
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        try {
            is = ConfigLoader.loadResource(filePath, false, false);

            if (is == null) {
                log.error("file not exist: " + filePath);
                return null;
            }

            bos = new ByteArrayOutputStream();

            byte[] buf = new byte[4096];
            int len = 0;
            while ((len = is.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }

            result = bos.toByteArray();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error("close input stream fail");
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    log.error("close output stream fail");
                }
            }
        }
        return result;
    }


    /**
     * 从本地目录读取文件
     *
     * @param filePath 本地文件完整路径
     * @return
     */
    public static byte[] readFileFromLocal(String filePath) {

        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("文件路径不存在");
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] bufs = new byte[fis.available()];
            fis.read(bufs);

            return bufs;

        } catch (Exception e) {

            log.error(e.getMessage(), e);
            throw new RuntimeException(e);

        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e2) {
                    log.error(e2.getMessage(), e2);
                }
            }
        }
    }

    public static byte[] readFileFromLocal(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("文件路径不存在");
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] bufs = new byte[fis.available()];
            fis.read(bufs);

            return bufs;

        } catch (Exception e) {

            log.error(e.getMessage(), e);
            throw new IOException(e);

        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e2) {
                    log.error(e2.getMessage(), e2);
                }
            }
        }
    }

    /**
     * 是否为ipv4格式的地址
     *
     * @param ip
     * @return
     */
    public static boolean isIpv4(String ip) {
        if (ip == null) {
            return false;
        }
        String[] tmps = ip.split("\\.");
        if (tmps.length != 4) {
            return false;
        }
        for (String str : tmps) {
            if (str.length() > 1 && str.startsWith("0")) {
                return false;
            }
            try {
                int num = Integer.valueOf(str);
                if (num > 255 || num < 0) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取本机ip地址
     *
     * @return
     */
    public static List<String> getLocalIpAddress() {
        List<String> list = new ArrayList<String>();
        try {
            for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements(); ) {
                NetworkInterface net = e.nextElement();
                for (Enumeration<InetAddress> ips = net.getInetAddresses(); ips.hasMoreElements(); ) {
                    String ip = ips.nextElement().getHostAddress();
                    if (!(ip.equals("127.0.0.1")) && isIpv4(ip)) {
                        list.add(ip);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return list;
    }

    public static void makeSureFolderExists(String filePath) {
        String folderPath = filePath.substring(0, filePath.lastIndexOf(File.separator));
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    /**
     * 字符串截取
     *
     * @param input 输入源
     * @param start 开始位置
     * @param end   结束位置
     * @return
     */
    public static String substr(String input, String start, String end) {
        if (input == null) {
            return null;
        }
        int index = input.indexOf(start);
        int index2 = input.indexOf(end, index + start.length());
        if (index == -1 || index2 == -1 || index >= index2) {
            return null;
        }

        return input.substring(index + start.length(), index2);
    }


    public static String format(String format, Object... args) {

        if (args != null && args.length > 0) {

            for (int i = 0; i < args.length; i++) {
                Object value = args[i];
                if (value != null && value instanceof Double) {
                    Double v = (Double) value;
                    if (v.intValue() - v == 0) {
                        args[i] = v.intValue();
                    }
                }
            }
        }

        return MessageFormatter.arrayFormat(format, args).getMessage();
    }

    /**
     * 字符串截取
     *
     * @param input
     * @param size
     * @return
     */
    public static String substring(String input, Integer size) {
        if (input == null) {
            return "";
        }
        input = input.trim();
        if (input.length() > size) {
            input = input.substring(0, size) + "...";
        }
        return input;
    }

    public static byte[] readBytesFromInputStream(InputStream is) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] bufs = new byte[4096];
            int len = 0;
            while ((len = is.read(bufs)) != -1) {
                bos.write(bufs, 0, len);
            }
            byte[] readBytes = bos.toByteArray();

            bos.close();

            return readBytes;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取 application/json 表单提交数据，存储到指定类型中<br/>
     * <p>
     * 示例：TBean tb = CommonUtil.parseJsonForm(request, TBean.class);
     *
     * @param request
     * @param cls
     * @return
     * @author Wang Xiaobo 2020年4月2日
     */
    public static <T> T parseJsonForm(HttpServletRequest request, Class<T> cls) {

        try {
            InputStream is = request.getInputStream();

            byte[] readBytes = readBytesFromInputStream(is);
            is.close();
            String str = new String(readBytes, "utf-8");

            return JsonUtil.fromJson(str, cls);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    public static String readJsonForm(HttpServletRequest request) {

        try {
            InputStream is = request.getInputStream();

            byte[] readBytes = readBytesFromInputStream(is);
            is.close();
            String str = new String(readBytes, "utf-8");

            return str;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }


    public static byte[] readJsonFormBytes(HttpServletRequest request) {

        try {
            InputStream is = request.getInputStream();

            byte[] readBytes = readBytesFromInputStream(is);
            is.close();

            return readBytes;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }


    public static String md5(String text) {
        MessageDigest msgDigest = null;
        try {
            msgDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("System doesn't support MD5 algorithm.");
        }

        msgDigest.update(text.getBytes(StandardCharsets.UTF_8)); // 注意改接口是按照指定编码形式签名

        byte[] bytes = msgDigest.digest();

        String md5Str = new String(encodeHex(bytes));

        return md5Str;
    }

    public static String md5(byte[] input) {
        MessageDigest msgDigest = null;
        try {
            msgDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("System doesn't support MD5 algorithm.");
        }

        msgDigest.update(input); // 注意改接口是按照指定编码形式签名

        byte[] bytes = msgDigest.digest();

        String md5Str = new String(encodeHex(bytes));

        return md5Str;
    }

    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static char[] encodeHex(byte[] data) {

        int l = data.length;

        char[] out = new char[l << 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS[0x0F & data[i]];
        }

        return out;
    }

    public static String kv(String k, Object v) {
        return k + ":" + JsonUtil.toJson(v);
    }

    private static Pattern p = Pattern.compile("\\d+");

    /**
     * 获取匹配的字符串
     *
     * @param input
     * @param regex
     * @return
     */
    public static String getFirstMatchedStrs(String input, String regex) {
        if (input == null) {
            return null;
        }
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);
        while (m.find()) {
            return m.group();
        }
        return null;
    }

    public static List<Long> getMatchedLongs(String input) {
        if (input == null) {
            return null;
        }
        List<Long> list = new ArrayList<Long>();
        Matcher m = p.matcher(input);
        while (m.find()) {
            String str = m.group();
            list.add(Long.valueOf(str));
        }
        return list;
    }

    /**
     * 将map结果合并
     *
     * <pre>
     * [{
     *     "Ka":{
     *         a: 'a',
     *         b: 'b'
     *     },
     *     "Kb":{
     *         c: 'a',
     * 	       a: 'b'
     *      }
     * }]
     * 合并后
     * [{
     *      a: 'a',
     *      b: 'b',
     *      c: 'a',
     *      _a: 'b'
     * }]
     * </pre>
     *
     * @param input
     * @param keepHead 是否保留表头
     * @return
     */
    public static List<Map<String, Object>> mergeResult(List<Map<String, Object>> input, boolean keepHead) {

        if (CommonUtil.isEmpty(input)) {
            return input;
        }

        List<Map<String, Object>> result = null;

        if (input instanceof PageList) {
            PageList<Map<String, Object>> newResult = new PageList<>();

            PageList old = (PageList) input;
            newResult.setTotalSize(old.getTotalSize());
            newResult.setHasPre(old.getHasPre());
            newResult.setHasNext(old.getHasNext());
            newResult.setPageIndex(old.getPageIndex());
            newResult.setTotalPage(old.getTotalPage());
            newResult.setPageSize(old.getPageSize());

            result = newResult;
        } else {
            result = new ArrayList<>();
        }

        List<Map<String, Object>> merge = input.stream().map(e -> {

            Map<String, Object> row = new HashMap();

            for (Map.Entry<String, Object> entry : e.entrySet()) {

                Map<String, Object> sub = (Map<String, Object>) entry.getValue();

                for (Map.Entry<String, Object> obj : sub.entrySet()) {
                    String k = obj.getKey();
                    Object v = obj.getValue();

                    while (true) {
                        if (row.get(k) == null) {
                            break;
                        }
                        k = "_" + k;
                    }
                    if (keepHead) {
                        row.put(entry.getKey() + "." + k, v);
                    } else {
                        row.put(k, v);
                    }
                }

            }
            return row;
        }).collect(Collectors.toList());

        result.addAll(merge);

        return result;
    }

    public static List<Map<String, Object>> toMap(List<? extends Object> list, String head) {

        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        List<Method> method = Stream.of(list.get(0).getClass().getMethods()).filter(e -> e.getName().startsWith("get")).collect(Collectors.toList());

        List<Map<String, Object>> result = list.stream().map(e -> {

            Map<String, Object> map = new HashMap<>();
            method.forEach(m -> {
                String fieldName = m.getName().substring(3, 4).toLowerCase() + m.getName().substring(4);
                Object value = ReflectionUtils.invokeMethod(m, e);
                map.put(head + "." + fieldName, value);
            });
            return map;

        }).collect(Collectors.toList());

        return result;
    }

    public static List<Map<String, Object>> mergeResult(List<Map<String, Object>> input) {
        return mergeResult(input, false);
    }


    /**
     * 获取获取数字字符串
     *
     * @param length 长度
     * @return
     */
    public static String getRandomNumber(Integer length) {
        if (length == null || length <= 0) {
            throw new NullPointerException();
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }

    /**
     * 取当天时间，时分秒为当天最后一毫秒
     *
     * @param day
     * @return
     */
    public static Date dayToEnd(Date day) {
        if (day == null) {
            return null;
        }
        try {
            return getSdf(df3).parse(getSdf(df).format(day) + " 23:59:59.999");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 取当天时间，时分秒为当天最后一毫秒
     *
     * @param mill
     * @return
     */
    public static Date dayToEnd(Long mill) {
        if (mill == null) {
            return null;
        }
        Date day = new Date(mill);
        try {
            return getSdf(df3).parse(getSdf(df).format(day) + " 23:59:59.999");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 取当前日期，不含时分秒
     *
     * @param day
     * @return
     */
    public static Date dayToShort(Date day) {
        if (day == null) {
            return null;
        }
        try {
            return getSdf(df).parse(getSdf(df).format(day));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 取当前日期，不含时分秒
     *
     * @param mill
     * @return
     */
    public static Date dayToShort(Long mill) {
        if (mill == null) {
            return null;
        }
        Date day = new Date(mill);
        try {
            return getSdf(df).parse(getSdf(df).format(day));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取N天前的日期
     *
     * @param day
     * @param afterDays 天数
     * @return
     */
    public static Date dayToAfter(Date day, Integer afterDays) {
        return new Date(day.getTime() + millDay * afterDays);
    }

    /**
     * 获取今天的毫秒数（精确到0点）
     *
     * @return
     */
    public static Long currentDayMill() {
        return dayToShort(new Date()).getTime();
    }


    /**
     * 将指定日期截断到0点
     *
     * @return
     */
    public static Long dayToMillShort(Date day) {
        return dayToShort(day).getTime();
    }


    /**
     * 将指定日期截断到0点
     *
     * @return
     */
    public static Long dayToMillShort(Long day) {
        return dayToShort(new Date(day)).getTime();
    }


    /**
     * html escape
     *
     * @param input
     * @return
     */
    public static String escape(String input) {
        return HtmlRegexpUtil.replaceTag(input);
    }

    /**
     * 当前环境 是否为线上生产
     *
     * @return
     */
    public static boolean isOnline() {
        String springProfile = System.getProperties().getProperty("spring.profiles.active");
        return "online".equals(springProfile);
    }

    /**
     * 深度对比2个对象的字符是否一致
     *
     * @param obj1
     * @param obj2
     * @param field ep: field1.field2.field3
     * @return
     */
    public static boolean equeals(Object obj1, Object obj2, String field) {
        if (obj1 == null || obj2 == null) {
            return false;
        }
//        if(!obj1.getClass().equals(obj2.getClass())){
//            return false;
//        }

        String[] tmps = field.split(".");

        Field f1 = null;
        Object o1 = obj1;

        Field f2 = null;
        Object o2 = obj2;

        for (int i = 0; i < tmps.length; i++) {

            String tmpField = tmps[i];

            f1 = ReflectionUtils.findField(obj1.getClass(), tmpField);
            if (f1 == null) {
                return false;
            }
            ReflectionUtils.makeAccessible(f1);
            o1 = ReflectionUtils.getField(f1, o1);

            f2 = ReflectionUtils.findField(obj2.getClass(), tmpField);
            if (f2 == null) {
                return false;
            }
            ReflectionUtils.makeAccessible(f2);
            o2 = ReflectionUtils.getField(f2, o2);

            if (o1 == null || o2 == null) {
                return false;
            }

            if (i == tmps.length - 1) {
                return o1.equals(o2);
            }
        }

        return false;
    }


    /**
     * 将二进制转换成16进制字符串
     *
     * @param buf
     * @return
     */
    public static String byte2Hex(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex);
        }
        return sb.toString();
    }


    /**
     * @param inputByte 待解压缩的字节数组
     * @return 解压缩后的字节数组
     * @throws IOException
     */
    public static byte[] uncompress(byte[] inputByte) throws IOException {
        int len = 0;
        Inflater infl = new Inflater(true);

        infl.setInput(inputByte);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] outByte = new byte[1024];
        try {
            while (!infl.finished()) {
                // 解压缩并将解压缩后的内容输出到字节输出流bos中
                len = infl.inflate(outByte);
                if (len == 0) {
                    break;
                }
                bos.write(outByte, 0, len);
            }
            infl.end();
        } catch (Exception e) {
            //
        } finally {
            bos.close();
        }
        return bos.toByteArray();
    }

    /**
     * 压缩.
     *
     * @param inputByte 待压缩的字节数组
     * @return 压缩后的数据
     * @throws IOException
     */
    public static byte[] compress(byte[] inputByte) throws IOException {
        int len = 0;
        Deflater defl = new Deflater(4, true);
        defl.setInput(inputByte);
        defl.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] outputByte = new byte[1024];
        try {
            while (!defl.finished()) {
                // 压缩并将压缩后的内容输出到字节输出流bos中
                len = defl.deflate(outputByte);
                bos.write(outputByte, 0, len);
            }
            defl.end();
        } finally {
            bos.close();
        }
        return bos.toByteArray();
    }

    /**
     * 将字节码转换成 base64 字符串
     *
     * @param bufs
     * @return
     */
    public static String base64Encode(byte[] bufs) {
        return Base64.getEncoder().encodeToString(bufs);
    }

    /**
     * 将 base64 字符串还原成 byte[] 数组
     *
     * @param input
     * @return
     */
    public static byte[] base64Decode(String input) {
        return Base64.getDecoder().decode(input);
    }

    private static String allFilesExts = "js|jpg|jpeg|png|gif|bmp|msg|pdf|doc|docx|rar|zip|xls|xlsx|txt|mp3|wav|cda|ttf|tif|mp4|flv|mpeg|rmvb|avi|mov|tif|jar|db|sql|json|zip|yml|xml|properties";

    private static List<String> allowTypes = Arrays.asList(allFilesExts.split("\\|"));

    public static byte[] getUploadBytes(HttpServletRequest request) throws Exception {

        String encoding = "utf-8";
        request.setCharacterEncoding(encoding);

        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload sevletFileUpload = new ServletFileUpload(factory);
        sevletFileUpload.setSizeMax(500 * 1024 * 1024); // 500M
        List<FileItem> fileItems = sevletFileUpload.parseRequest(request);

        // 依次处理每个上传的文件
        for (Iterator<FileItem> it = fileItems.iterator(); it.hasNext(); ) {

            final FileItem item = (FileItem) it.next();
            if (!item.isFormField()) {
                String fileFullName = item.getName();
                int tmpIndex = fileFullName.lastIndexOf(".");
                if (tmpIndex == -1) {
                    throw new BizException("上传类型不支持");
                }

                String ext = fileFullName.substring(tmpIndex + 1).toLowerCase();
                if (!allowTypes.contains(ext)) {
                    throw new BizException("上传类型不支持:" + ext);
                }

                java.io.InputStream is = item.getInputStream();
                long fileSize = is.available();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                int len = 0;
                byte[] bufs = new byte[4096];
                while ((len = is.read(bufs)) != -1) {
                    bos.write(bufs, 0, len);
                }

                byte[] result = bos.toByteArray();

                return result;
            }
        }

        return null;
    }

    public static void execCmd(String cmd) throws Exception {
        // log.info("cmd: " + cmd);
        Process p;

        //执行命令
        p = Runtime.getRuntime().exec(cmd);
        //取得命令结果的输出流
        InputStream fis = p.getErrorStream();
        //用一个读输出流类去读
        InputStreamReader isr = new InputStreamReader(fis);
        //用缓冲器读行
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        //直到读完为止
        List<String> errors = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            log.error("build error: " + line);
            errors.add(line);
        }
        if (isNotEmpty(errors)) {
            throw new RuntimeException(CommonUtil.join(errors, "\n"));
        }
    }

    /**
     * 集合拆分
     *
     * @param list
     * @param size
     * @param <E>
     * @return
     */
    public static <E> List<List<E>> split(List<E> list, int size) {

        List<List<E>> result = new ArrayList<List<E>>();

        int start = 0;
        int limit = 0;
        while (true) {

            limit = start + size;

            if (limit >= list.size()) {
                limit = list.size();
            }

            List<E> sub = new ArrayList<>(list.subList(start, limit));

            result.add(sub);

            start += size;

            if (start >= list.size()) {
                break;
            }
        }

        return result;
    }


    public static boolean equals(CharSequence cs1, CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        } else if (cs1 != null && cs2 != null) {
            if (cs1.length() != cs2.length()) {
                return false;
            } else if (cs1 instanceof String && cs2 instanceof String) {
                return cs1.equals(cs2);
            } else {
                int length = cs1.length();

                for (int i = 0; i < length; ++i) {
                    if (cs1.charAt(i) != cs2.charAt(i)) {
                        return false;
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }


    public static String getDeepExceptionMsg(Exception e) {
        Throwable t = e;
        while (true) {
            if (t instanceof InvocationTargetException) {
                t = ((InvocationTargetException) t).getTargetException();
                continue;
            }
            if (t.getCause() != null && t.getCause() instanceof InvocationTargetException) {
                Throwable throwable = ((InvocationTargetException) t.getCause()).getTargetException();
                t = throwable;
                continue;
            }
            break;
        }
        String msg = t.getMessage();
        return msg;
    }


    private static KryoPool kryoPool = new KryoPool.Builder(new KryoFactory() {
        public Kryo create() {
            final Kryo kryo = new Kryo();
            kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new org.objenesis.strategy.StdInstantiatorStrategy()));
            kryo.setRegistrationRequired(false);
            kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
            return kryo;
        }
    }).softReferences().build();

    public static byte[] serialize(Object obj) {

        Kryo kryo = null;
        try {
            kryo = kryoPool.borrow();
            Output output = new Output(10000, -1);

            kryo.writeObject(output, obj);
            output.close();

            return output.toBytes();
        } finally {
            kryoPool.release(kryo);
        }
    }

    public static <T> T deserialize(byte[] by, Class<T> type) {
        if (by == null) {
            return null;
        }
        Kryo kryo = null;
        try {

            kryo = kryoPool.borrow();

            Input input = new Input(by);

            T outObject = kryo.readObject(input, type);
            input.close();

            return outObject;
        } finally {
            kryoPool.release(kryo);
        }
    }


    /**
     * <p>
     * 反射对象获取泛型
     * </p>
     *
     * @param clazz 对象
     * @param index 泛型所在位置
     * @return Class
     */
    public static Class<?> getSuperClassGenericType(final Class<?> clazz, final int index) {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            log.warn(String.format("Warn: %s's superclass not ParameterizedType", clazz.getSimpleName()));
            return Object.class;
        }
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            log.warn(String.format("Warn: Index: %s, Size of %s's Parameterized Type: %s .", index, clazz.getSimpleName(), params.length));
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            log.warn(String.format("Warn: %s not set the actual class on superclass generic parameter", clazz.getSimpleName()));
            return Object.class;
        }
        return (Class<?>) params[index];
    }


    public static String uuid() {
        return UUID.randomUUID().toString();
    }


    /**
     * 计算 hash 值
     *
     * @param key
     * @return
     */
    public static long hash(byte[] key) {
        return MurmurHash.hashValue(key);
    }

    /**
     * 计算 hash 值
     *
     * @param key
     * @return
     */
    public static long hash(String key) {
        return MurmurHash.hashValue(key.getBytes(StandardCharsets.UTF_8));
    }
//
//
//    public static void main(String[] args) {
//        TimerCheck.start();
//        long l1 = System.currentTimeMillis();
//        for (int i = 0; i < 100000; i++) {
//            String str = CommonUtil.getRandomNumber(10);
//            System.out.println(str + ":" + hash(str));
//            System.out.println(str + ":" + hash(str));
//            System.out.println(str + ":" + hash(str));
//            System.out.println(str + ":" + hash(str));
//            System.out.println(str + ":" + hash(str));
//        }
//
//        System.out.println(((double) System.currentTimeMillis() - l1) / (double)(5 * 100000));
//    }


    /**
     * 当前应用是否以 spring fat-jar 方式启动
     *
     * @return
     */
    public static boolean isSpringApp() {
        String cmd = (String) System.getProperties().get("sun.java.command");
        return cmd.indexOf(".jar") != -1;
    }


    private static final char[] toBase64URL = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};


    public static String getRandomString(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(toBase64URL[(int) (Math.random() * toBase64URL.length)]);
        }
        return sb.toString();
    }


    private static final char[] toBase64URL_simple = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};


    public static String getRandomStringSimple(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(toBase64URL_simple[(int) (Math.random() * toBase64URL_simple.length)]);
        }
        return sb.toString();
    }


    public static List<String> split(String input, String split) {
        if (input == null || input.matches("\\s*")) {
            return null;
        }
        List<String> result = new ArrayList<String>();
        for (String tmp : input.split(split)) {
            if (tmp == null || tmp.matches("\\s*")) {
                continue;
            }
            result.add(tmp.trim());
        }
        return result;
    }


    public static String sha256(byte[] bytes) {
        MessageDigest messageDigest;
        String encodestr = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(bytes);
            encodestr = byte2Hex(messageDigest.digest());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return encodestr;
    }

    public static String sha256(String str) {
        MessageDigest messageDigest;
        String encodestr = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodestr = byte2Hex(messageDigest.digest());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return encodestr;
    }

    public static String sha256Objects(Object... objects) {
        if (objects == null || objects.length == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (Object obj : objects) {
            byte[] bytes = serialize(obj);
            if (bytes == null) {
                continue;
            }
            sb.append(sha256(bytes));
        }

        return sha256(sb.toString());
    }


    public static String getDuplicateFieldWithErrormsg(String errorStack) {
        if (errorStack == null) {
            return null;
        }
        String[] strs = errorStack.split("\n");
        for (String str : strs) {
            int index = str.indexOf("Duplicate entry");
            if (index != -1) {
                str = str.substring(index);
                str = str.substring(0, str.lastIndexOf("'"));
                str = str.substring(str.lastIndexOf("'") + 1);
                return str;
            }
        }
        return null;
    }


    /**
     * 将用默认分隔的字符串转换成 List
     *
     * @param input
     * @return
     */
    public static List<String> convertStr2List(String input) {
        return convertStr2List(input, ",");
    }

    /**
     * 将用指定字符分隔的字符串转换成 List
     *
     * @param input
     * @param split 分隔符
     * @return
     */
    public static List<String> convertStr2List(String input, String split) {
        if (input == null) {
            return null;
        }
        List<String> list = new ArrayList<String>();
        String[] strs = input.split(split);
        for (String str : strs) {
            if (!isEmpty(str)) {
                list.add(str.trim());
            }
        }
        return list;
    }


    public static <T> List<List<T>> splitList(List<T> source, int batchSize) {
        if (source == null) {
            return null;
        }
        int limit = countStep(source.size(), batchSize);

        return Stream.iterate(0, n -> n + 1).limit(limit).parallel().map(n -> source.stream().skip(n * batchSize).limit(batchSize).parallel().collect(Collectors.toList())).collect(Collectors.toList());
    }

    /**
     * 计算切分次数
     */
    private static int countStep(int size, int batchSize) {
        return size % batchSize == 0 ? size / batchSize : size / batchSize + 1;
    }


    public static String replaceFirst(String input, String oldChars, String newChars) {

        int oldCharLens = oldChars.length();

        int index = input.indexOf(oldChars);

        if (index == -1) {
            return input;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(input.substring(0, index)).append(newChars).append(input.substring(index + oldCharLens));

        return sb.toString();
    }


    /**
     * 赤道半径（单位：米）
     */
    private static final double EQUATOR_RADIUS = 6378137;

    /**
     * 方法一：（反余弦计算方式）
     *
     * @param longitude1 第一个点的经度
     * @param latitude1  第一个点的纬度
     * @param longitude2 第二个点的经度
     * @param latitude2  第二个点的纬度
     * @return 返回距离，单位m
     */
    public static double getDistance1(double longitude1, double latitude1, double longitude2, double latitude2) {
        // 纬度
        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);
        // 经度
        double lon1 = Math.toRadians(longitude1);
        double lon2 = Math.toRadians(longitude2);
        // 纬度之差
        double a = lat1 - lat2;
        // 经度之差
        double b = lon1 - lon2;
        // 计算两点距离的公式
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(b / 2), 2)));
        // 弧长乘赤道半径, 返回单位: 米
        s = s * EQUATOR_RADIUS;
        return s;
    }

    /**
     * 地球平均半径（单位：米）
     */
    private static final double EARTH_AVG_RADIUS = 6371000;

    /**
     * 方法二：（反余弦计算方式）
     *
     * @param longitude1 第一点的经度
     * @param latitude1  第一点的纬度
     * @param longitude2 第二点的经度
     * @param latitude2  第二点的纬度
     * @return 返回的距离，单位m
     */
    public static double getDistance3(double longitude1, double latitude1, double longitude2, double latitude2) {
        // 经纬度（角度）转弧度。弧度作为作参数，用以调用Math.cos和Math.sin
        // A经弧度
        double radiansAX = Math.toRadians(longitude1);
        // A纬弧度
        double radiansAY = Math.toRadians(latitude1);
        // B经弧度
        double radiansBX = Math.toRadians(longitude2);
        // B纬弧度
        double radiansBY = Math.toRadians(latitude2);

        // 公式中“cosβ1cosβ2cos（α1-α2）+sinβ1sinβ2”的部分，得到∠AOB的cos值
        double cos = Math.cos(radiansAY) * Math.cos(radiansBY) * Math.cos(radiansAX - radiansBX) + Math.sin(radiansAY) * Math.sin(radiansBY);
        // System.out.println("cos = " + cos); // 值域[-1,1]

        // 反余弦值
        double acos = Math.acos(cos);
        // System.out.println("acos = " + acos); // 值域[0,π]
        // System.out.println("∠AOB = " + Math.toDegrees(acos)); // 球心角 值域[0,180]

        // 最终结果
        return EARTH_AVG_RADIUS * acos;
    }


    /**
     * 经纬度转化为弧度(rad)
     *
     * @param d 经度/纬度
     */
    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 方法三：（基于googleMap中的算法得到两经纬度之间的距离,计算精度与谷歌地图的距离精度差不多。）
     *
     * @param longitude1 第一点的经度
     * @param latitude1  第一点的纬度
     * @param longitude2 第二点的经度
     * @param latitude2  第二点的纬度
     * @return 返回的距离，单位m
     */
    public static double getDistance2(double longitude1, double latitude1, double longitude2, double latitude2) {
        double radLat1 = rad(latitude1);
        double radLat2 = rad(latitude2);
        double a = radLat1 - radLat2;
        double b = rad(longitude1) - rad(longitude2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_AVG_RADIUS;
        s = Math.round(s * 10000d) / 10000d;
        return s;
    }


    /**
     * 方法四：（高德地图计算方法）
     *
     * @param longitude1 第一点的经度
     * @param latitude1  第一点的纬度
     * @param longitude2 第二点的经度
     * @param latitude2  第二点的纬度
     * @return 返回的距离，单位m
     */
    public static Double getDistance4(double longitude1, double latitude1, double longitude2, double latitude2) {
        if (longitude1 == 0 || latitude1 == 0 || latitude2 == 0 || longitude2 == 0) {
            return -1.0;
        }
        longitude1 *= 0.01745329251994329;
        latitude1 *= 0.01745329251994329;
        longitude2 *= 0.01745329251994329;
        latitude2 *= 0.01745329251994329;
        double var1 = Math.sin(longitude1);
        double var2 = Math.sin(latitude1);
        double var3 = Math.cos(longitude1);
        double var4 = Math.cos(latitude1);
        double var5 = Math.sin(longitude2);
        double var6 = Math.sin(latitude2);
        double var7 = Math.cos(longitude2);
        double var8 = Math.cos(latitude2);
        double[] var10 = new double[3];
        double[] var20 = new double[3];
        var10[0] = var4 * var3;
        var10[1] = var4 * var1;
        var10[2] = var2;
        var20[0] = var8 * var7;
        var20[1] = var8 * var5;
        var20[2] = var6;
        return Math.asin(Math.sqrt((var10[0] - var20[0]) * (var10[0] - var20[0]) + (var10[1] - var20[1]) * (var10[1] - var20[1]) + (var10[2] - var20[2]) * (var10[2] - var20[2])) / 2.0) * 1.27420015798544E7;
        // 结果四舍五入 保留2位小数
        //return new BigDecimal(distance).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 从字符串提取数字
     *
     * @param input
     * @return
     */
    public static Integer getNumberFromStr(String input) {
        if (input == null) {
            return null;
        }
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(input);
        while (m.find()) {
            String str = m.group();
            return Integer.valueOf(str);
        }
        return null;
    }

    public static void appendCorsHead(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with,content-type");
    }


    static DecimalFormat numberFormat = new DecimalFormat("0.00");

    public static String formatNumber(Double v) {
        if (v == null) {
            return "";
        }
        if (v > 0) {
            return "+" + numberFormat.format(v);
        }
        return numberFormat.format(v);
    }

    public static String formatNumber(String v) {
        if (v == null) {
            return "";
        }
        try {
            Double d = Double.valueOf(v);
            return formatNumber(d);
        } catch (Exception e) {
        }
        return v;
    }


    public static void main(String[] args) {
        System.out.println(formatNumber("+3.11"));
        System.out.println(formatNumber("3.11"));
        System.out.println(formatNumber("-3"));
        System.out.println(formatNumber("3"));

        Double d = null;
        System.out.println(formatNumber(d));
        System.out.println(formatNumber("pl"));
    }
}
