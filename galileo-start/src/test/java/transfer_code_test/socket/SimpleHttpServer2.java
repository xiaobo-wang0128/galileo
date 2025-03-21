package transfer_code_test.socket;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.armada.galileo.common.util.JsonUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaobo
 * @date 2022/1/12 10:26 上午
 */
public class SimpleHttpServer2 {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);
        server.createContext("/post", new BasicPostHttpHandler());
        // 使用默认的 excutor
        server.setExecutor(null);
        server.start();
    }



    public static class BasicPostHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            InputStream is = httpExchange.getRequestBody();
            String requestData = is2string(is);
            Map<String, Object> map = new HashMap<>();
            map.put("code", 0);
            String response = JsonUtil.toJson(map);

            System.out.println("response: " + response);
            is.close();

            Headers headers = httpExchange.getResponseHeaders();
            headers.set("Content-Type", "application/json; charset=utf-8");

            OutputStream os = httpExchange.getResponseBody();
            byte[] bufs = response.getBytes("utf-8");
            httpExchange.sendResponseHeaders(200, bufs.length);

            os.write(bufs);
            os.close();
        }

        private String is2string(InputStream is) throws IOException {
            byte[] buf = new byte[4096];
            int len = 0;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((len = is.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            String str = new String(bos.toByteArray(), "utf-8");
            bos.close();
            bos = null;
            return str;
        }

    }
}
