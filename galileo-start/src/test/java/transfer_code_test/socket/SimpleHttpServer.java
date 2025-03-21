package transfer_code_test.socket;

import org.apache.http.ExceptionLogger;
import org.apache.http.impl.nio.reactor.IOReactorConfig;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.http.ExceptionLogger;
import org.apache.http.HttpConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.bootstrap.HttpServer;
import org.apache.http.impl.nio.bootstrap.ServerBootstrap;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.entity.NFileEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

/**
 * @author xiaobo
 * @date 2022/1/12 10:14 上午
 */
public class SimpleHttpServer {

    public static void main(String[] args) throws Exception {
        // 本地操作系统服务目录
        String docRoot = "D:\\Apps\\Java";

        IOReactorConfig config = IOReactorConfig.custom()
                .setSoTimeout(15000)
                .setTcpNoDelay(true)
                .build();

        final HttpServer server = ServerBootstrap.bootstrap()
                .setListenerPort(10021)
                .setServerInfo("What21/1.1")
                .setIOReactorConfig(config)
                .setExceptionLogger(ExceptionLogger.STD_ERR)
                .registerHandler("*", new HttpFileHandler(new File(docRoot)))
                .create();
        // 启动服务
        server.start();
        server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        // 运行一段时间后停止服务
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.shutdown(5, TimeUnit.SECONDS);
            }
        });

    }


    static class HttpFileHandler implements HttpAsyncRequestHandler<HttpRequest> {

        private final File docRoot;

        public HttpFileHandler(final File docRoot) {
            super();
            this.docRoot = docRoot;
        }

        public HttpAsyncRequestConsumer<HttpRequest> processRequest(
                final HttpRequest request,
                final HttpContext context) {
            // Buffer request content in memory for simplicity
            return new BasicAsyncRequestConsumer();
        }

        public void handle(
                final HttpRequest request,
                final HttpAsyncExchange httpexchange,
                final HttpContext context) throws HttpException, IOException {
            HttpResponse response = httpexchange.getResponse();
            handleInternal(request, response, context);
            httpexchange.submitResponse(new BasicAsyncResponseProducer(response));

        }

        private void handleInternal(
                final HttpRequest request,
                final HttpResponse response,
                final HttpContext context) throws HttpException, IOException {
            // 请求方法
            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }
            // 请求的URI
            String target = request.getRequestLine().getUri();



            final File file = new File(this.docRoot, URLDecoder.decode(target, "UTF-8"));
            if (!file.exists()) {
                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                // 返回信息
                StringBuilder sb = new StringBuilder();
                sb.append("<html><body><h1>");
                sb.append("File" + file.getPath() + "not found");
                sb.append("</h1></body></html>");

                NStringEntity entity = new NStringEntity(
                        sb.toString(),
                        ContentType.create("text/html", "UTF-8"));
                response.setEntity(entity);
                System.out.println("File " + file.getPath() + " not found");
            } else if (!file.canRead() || file.isDirectory()) {
                response.setStatusCode(HttpStatus.SC_FORBIDDEN);
                // 返回信息
                StringBuilder sb = new StringBuilder();
                sb.append("<html><body><h1>");
                sb.append("访问拒绝");
                sb.append("</h1></body></html>");
                NStringEntity entity = new NStringEntity(
                        sb.toString(),
                        ContentType.create("text/html", "UTF-8"));
                response.setEntity(entity);
                System.out.println("Cannot read file " + file.getPath());

            } else {
                HttpCoreContext coreContext = HttpCoreContext.adapt(context);
                HttpConnection conn = coreContext.getConnection(HttpConnection.class);
                response.setStatusCode(HttpStatus.SC_OK);
                NFileEntity body = new NFileEntity(file, ContentType.create("text/html"));
                response.setEntity(body);
                System.out.println(conn + ": serving file " + file.getPath());
            }
        }

    }

}
