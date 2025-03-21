package org.armada.galileo.common.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * 简易的 HTTP 请求类
 *
 * @author 王小波 2012-3-26
 */
@Slf4j
public class HttpUtil {


    private static final int API_TIMEOUT = 60000;

    private static final String POST = "POST";

    private static final String GET = "GET";

    private static String defaultEncoding = "utf-8";


    public static Map<String, String> asHeaders(String... kv) {
        if (kv == null || kv.length == 0 || kv.length % 2 != 0) {
            return null;
        }
        Map<String, String> header = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            header.put(kv[i], kv[i + 1]);
        }
        return header;
    }


    /**
     * http post json 请求
     *
     * @param url
     * @param jsonContent
     * @return
     */
    public static String postJson(String url, String jsonContent) {
        return httpAccessJDK(url, null, jsonContent, defaultEncoding, POST, true, null);
    }

    /**
     * http post json 请求
     *
     * @param url
     * @param jsonContent
     * @return
     */
    public static String postJson(String url, String jsonContent, Map<String, String> headers, String charset, Integer timeout) {
        return httpAccessJDK(url, headers, jsonContent, charset, POST, true, timeout);
    }


    /**
     * http post 表单请求
     *
     * @param url     地址
     * @param params  请求参数键值对
     * @param charset 编码
     * @return 以字符串形式返回
     */
    public static String post(String url, Map<String, String> params, Map<String, String> headers, String charset, Integer timeout) {
        String paramStr = getHttpRequestParams(url, params, false, charset);
        return httpAccessJDK(url, headers, paramStr, charset, POST, false, timeout);
    }


    /**
     * http post 表单请求.默认编码（utf-8)
     *
     * @param url    地址
     * @param params 请求参数键值对
     * @return 以字符串形式返回
     */
    public static String post(String url, Map<String, String> params) {
        String paramStr = getHttpRequestParams(url, params, false, defaultEncoding);
        return httpAccessJDK(url, null, paramStr, defaultEncoding, POST, false, null);
    }


    /*-------------------------------------------------------------------------------------------------------------------------*/
    /*-------------------------------------------------------------------------------------------------------------------------*/
    /*-------------------------------------------------------------------------------------------------------------------------*/


    public static String get(String url, Map<String, String> params, Map<String, String> headers, String charset, Integer timeout) {
        String paramStr = getHttpRequestParams(url, params, false, charset);
        return httpAccessJDK(url, headers, paramStr, charset, GET, false, timeout);
    }


    /**
     * http get 请求.默认编码（utf-8)
     *
     * @param url    地址（可带参数）
     * @param params 请求参数键值对
     * @return 以字符串形式返回
     */
    public static String get(String url, Map<String, String> params) {
        String paramStr = getHttpRequestParams(url, params, false, defaultEncoding);
        return httpAccessJDK(url, null, paramStr, defaultEncoding, GET, false, null);
    }


    /**
     * http get 请求.默认编码（utf-8)
     *
     * @param url 地址
     * @return 以字符串形式返回
     */
    public static String get(String url) {
        return httpAccessJDK(url, null, null, defaultEncoding, GET, false, null);
    }

    /**
     * 生成 form 参数
     *
     * @param url
     * @param params
     * @param charset
     * @return
     */
    public static String getHttpRequestParams(String url, Map<String, String> params, boolean jsonRequest, String charset) {
        if (params == null || params.size() == 0) {
            return null;
        }
        StringBuilder paramSb = new StringBuilder();
        if (charset == null) {
            charset = defaultEncoding;
        }

        if (jsonRequest) {
            return JsonUtil.toJson(params);
        }

        try {
            int i = 0, len = params != null ? params.size() : 0;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) {
                    i++;
                    continue;
                }
                paramSb.append(URLEncoder.encode(entry.getKey(), charset)).append("=").append(URLEncoder.encode(entry.getValue(), charset));
                if (i != len - 1) {
                    paramSb.append("&");
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return paramSb.toString();
    }

    /**
     * 请求
     *
     * @param url     地址
     * @param data    请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @param charset 字符编码
     * @param method
     * @return
     */
    public static String httpAccessJDK(String url, Map<String, String> httpHeaders, String data, String charset, String method, boolean isJsonRequest, Integer timeout) {
        if (method == null) {
            method = GET;
        }
        if (charset == null) {
            charset = defaultEncoding;
        }

        StringBuilder content = new StringBuilder();

        BufferedReader reader = null;
        HttpURLConnection conn = null;
        try {
            if (data != null && method.equals(GET)) {
                if (url.indexOf("?") == -1) {
                    url = new StringBuilder(url).append("?").append(data).toString();
                } else {
                    url = new StringBuilder(url).append("&").append(data).toString();
                }
            }
            URL getUrl = new URL(url);
            conn = (HttpURLConnection) getUrl.openConnection();
            if (timeout == null) {
                conn.setConnectTimeout(API_TIMEOUT);
                conn.setReadTimeout(API_TIMEOUT);
            } else {
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
            }


            if (httpHeaders != null && httpHeaders.size() > 0) {
                for (Map.Entry<String, String> entry : httpHeaders.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            if (isJsonRequest) {
                conn.setRequestProperty("Content-Type", "application/json");
            }

            conn.setRequestProperty("Charsert", charset); //设置请求编码
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod(method);
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(true);

            conn.connect();
            if (data != null && method.equals(POST)) {
                byte[] bdata = data.getBytes(charset);
                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                out.write(bdata);
                out.flush();
                out.close();
            }

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
            String inputLine;

            while ((inputLine = reader.readLine()) != null) {
                content.append(inputLine);
            }

        } catch (Exception e) {
            log.error("http error, address:{}, message:{}", url, e.toString());
            throw new RuntimeException(e);
        } finally {
            try {
                reader.close();
                conn.disconnect();
            } catch (Exception e) {
            }
        }

        String result = content.toString();
        if (log.isInfoEnabled()) {
            if (httpHeaders != null && httpHeaders.size() > 0) {
                log.info("[http] url:{}, header:{}, reqeust:{}, response:{}", url, httpHeaders, CommonUtil.substring(data, 100), CommonUtil.substring(result, 100));
            } else {
                log.info("[http] url:{}, reqeust:{}, response:{}", url, CommonUtil.substring(data, 100), CommonUtil.substring(result, 100));
            }
        }

        return result;
    }

    public static byte[] postJson(String url, Map<String, String> httpHeaders, String jsonData, Integer timeout) {

        String method = POST;

        String charset = defaultEncoding;

        StringBuilder content = new StringBuilder();

        BufferedReader reader = null;
        HttpURLConnection conn = null;

        byte[] result = null;

        long l1 = System.currentTimeMillis();

        try {

            URL getUrl = new URL(url);
            conn = (HttpURLConnection) getUrl.openConnection();
            if (timeout == null) {
                conn.setConnectTimeout(API_TIMEOUT);
                conn.setReadTimeout(API_TIMEOUT);
            } else {
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
            }

            if (httpHeaders != null && httpHeaders.size() > 0) {
                for (Map.Entry<String, String> entry : httpHeaders.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Charsert", charset); //设置请求编码
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod(method);
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(true);

            conn.connect();
            if (jsonData != null && method.equals(POST)) {
                byte[] bdata = jsonData.getBytes(charset);
                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                out.write(bdata);
                out.flush();
                out.close();
            }

            InputStream is = conn.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] bufs = new byte[4096];
            int len = 0;
            while ((len = is.read(bufs)) != -1) {
                bos.write(bufs, 0, len);
            }

            result = bos.toByteArray();

        } catch (Exception e) {
            log.error("[http] http error, address:{}, message:{}", url, e.toString());
            throw new RuntimeException(e);
        } finally {
            try {
                reader.close();
                conn.disconnect();
            } catch (Exception e) {
            }

            log.info("[http] download success, cost " + (System.currentTimeMillis() - l1) + " ms");
        }

        return result;
    }


    public static byte[] download(String url, Map<String, String> httpHeaders, Integer timeout) {

        byte[] result = null;

        BufferedReader reader = null;
        HttpURLConnection conn = null;
        try {

            URL getUrl = new URL(url);
            conn = (HttpURLConnection) getUrl.openConnection();
            if (timeout == null) {
                conn.setConnectTimeout(API_TIMEOUT);
                conn.setReadTimeout(API_TIMEOUT);
            } else {
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
            }

            if (httpHeaders != null && httpHeaders.size() > 0) {
                for (Map.Entry<String, String> entry : httpHeaders.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod(GET);
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(true);

            conn.connect();


            InputStream is = conn.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] tmp = new byte[4096];
            int len = 0;

            while ((len = is.read(tmp)) != -1) {
                bos.write(tmp, 0, len);
            }
            result = bos.toByteArray();

            bos.close();

        } catch (Exception e) {
            log.error("http error, address:{}, message:{}", url, e.toString());
            throw new RuntimeException(e);
        } finally {
            try {
                reader.close();
                conn.disconnect();
            } catch (Exception e) {
            }
        }

        return result;
    }


    public static void main(String[] args) throws Exception {
        byte[] bufs = download("http://120.25.234.125:8088/Upload/FundusPhoto/" + URLEncoder.encode("李培婷_23052700010937_Color_OS_20230527_020848__100850276.jpg"), null, null);

        new FileOutputStream("/Users/wangxiaobo/Downloads/aaaa.png").write(bufs);

    }

}
