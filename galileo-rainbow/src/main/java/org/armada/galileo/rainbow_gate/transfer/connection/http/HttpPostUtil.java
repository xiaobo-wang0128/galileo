package org.armada.galileo.rainbow_gate.transfer.connection.http;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpPostUtil {

    static String BOUNDARY = java.util.UUID.randomUUID().toString();
    static String PREFIX = "--", LINEND = "\r\n";
    static String MULTIPART_FROM_DATA = "multipart/form-data";
    static String CHARSET = "UTF-8";
    private static int ReadTimeout = 120000;
    private static int ConnectionTimeout = 15000;


    public static void setReadTimeout(int mill) {
        HttpPostUtil.ReadTimeout = mill;
    }

    public static void setConnectionTimeout(int mill) {
        HttpPostUtil.ConnectionTimeout = mill;
    }

    private static ThreadLocal<Integer> timeOut = new ThreadLocal<>();

    public static void setTimeout(Integer mill) {
        timeOut.set(mill);
    }

    private static Logger log = LoggerFactory.getLogger(HttpPostUtil.class);

    static {
        try {
            SSLContext sslcontext = SSLContext.getInstance("SSL", "SunJSSE");// 第一个参数为协议,第二个参数为提供者(可以缺省)
            TrustManager[] tm = {new MyX509TrustManager()};
            sslcontext.init(null, tm, new SecureRandom());
            HostnameVerifier ignoreHostnameVerifier = new HostnameVerifier() {
                public boolean verify(String s, SSLSession sslsession) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static byte[] request(String url, Map<String, String> params, byte[] data) {
        return request(url, params, null, data);
    }

    public static byte[] request(String url, Map<String, String> params, Map<String, String> headers, byte[] data) {
        try {

            Map<String, byte[]> files = null;
            if (data != null) {
                files = new HashMap<>();
                files.put("upload", data);
            }

            URL uri = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();

            Integer timeout = timeOut.get();
            if (timeout != null) {
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
            } else {
                conn.setConnectTimeout(ConnectionTimeout);
                conn.setReadTimeout(ReadTimeout);
            }

            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("charset", "UTF-8");
            conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);

            // http 头
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            // 首先组拼文本类型的参数
            StringBuilder sb = new StringBuilder();
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    sb.append(PREFIX);
                    sb.append(BOUNDARY);
                    sb.append(LINEND);
                    sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
                    // #### sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
                    // ##### sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
                    sb.append("Content-Type: text/plain" + LINEND);
                    sb.append(LINEND);
                    sb.append(entry.getValue());
                    sb.append(LINEND);
                }
            }

            DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
            outStream.write(sb.toString().getBytes());

            // 发送文件数据
            sb = new StringBuilder();
            if (data != null && data.length > 0) {
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINEND);
                sb.append("Content-Disposition: form-data; name=\"_upload_file_\"; filename=\"_upload_file_\"" + LINEND);
                // ##### sb1.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINEND);

                sb.append("Content-Type: application/octet-stream" + LINEND);
                // sb.append("Content-Type: text/plain" + LINEND);

                sb.append(LINEND);

                outStream.write(sb.toString().getBytes());
                outStream.write(data);
                outStream.write(LINEND.getBytes());
            }
            // 请求结束标志
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
            outStream.write(end_data);
            outStream.flush();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            InputStream is = conn.getInputStream();
            int len = 0;
            byte[] buf = new byte[4096];
            while ((len = is.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }

            outStream.close();
            conn.disconnect();
            conn = null;

            byte[] returns = bos.toByteArray();

            bos.close();
            bos = null;

            return returns;

        } catch (Exception e) {
            log.error("远程调用出错, url: {}, message:{} ", url, e.getMessage());
            throw new RuntimeException("网络通信异常");
        } finally {
            timeOut.remove();
        }

    }

    public static byte[] request(String url, HttpPostParam postParam) {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("appId", postParam.getAppId());
            params.put("dc", postParam.getDc());
            params.put("sign", postParam.getSign());

            if (postParam.getUserId() != null) {
                params.put("userId", postParam.getUserId());
            }
            if (postParam.getToken() != null) {
                params.put("token", postParam.getToken());
            }

            Map<String, byte[]> files = new HashMap<>();
            files.put("upload", postParam.getBufs());

            URL uri = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();

            Integer timeout = timeOut.get();
            if (timeout != null) {
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
            } else {
                conn.setConnectTimeout(ConnectionTimeout);
                conn.setReadTimeout(ReadTimeout);
            }

            conn.setDoInput(true);// 允许输入
            conn.setDoOutput(true);// 允许输出
            conn.setUseCaches(false); // 不允许使用缓存
            conn.setRequestMethod("POST");
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);
            // 首先组拼文本类型的参数
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINEND);
                sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
                sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
                sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
                sb.append(LINEND);
                sb.append(entry.getValue());
                sb.append(LINEND);
            }
            DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
            outStream.write(sb.toString().getBytes());
            // 发送文件数据
            if (files != null) {
                for (Map.Entry<String, byte[]> file : files.entrySet()) {
                    StringBuilder sb1 = new StringBuilder();
                    sb1.append(PREFIX);
                    sb1.append(BOUNDARY);
                    sb1.append(LINEND);
                    sb1.append("Content-Disposition: form-data; name=\"j5eQkZqZlpOa\"; filename=\"" + file.getKey() + "\"" + LINEND);
                    sb1.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINEND);
                    sb1.append(LINEND);

                    outStream.write(sb1.toString().getBytes());
                    outStream.write(file.getValue());
                    outStream.write(LINEND.getBytes());
                }
            }
            // 请求结束标志
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
            outStream.write(end_data);
            outStream.flush();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            InputStream is = conn.getInputStream();
            int len = 0;
            byte[] buf = new byte[4096];
            while ((len = is.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }

            outStream.close();
            conn.disconnect();

            byte[] returns = bos.toByteArray();

            return returns;

        } catch (Exception e) {
            log.error("远程调用出错, url: {}, message:{} ", url, e.getMessage());
            throw new RuntimeException("网络通信异常");
        } finally {
            timeOut.remove();
        }

    }

}