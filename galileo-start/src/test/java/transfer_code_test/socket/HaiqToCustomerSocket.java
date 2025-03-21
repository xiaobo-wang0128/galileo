//package transfer_code_test.socket;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.http.HttpException;
//import org.apache.http.impl.bootstrap.HttpServer;
//import org.apache.http.impl.nio.reactor.IOReactorConfig;
//import org.apache.http.nio.ContentDecoder;
//import org.apache.http.nio.ContentEncoder;
//import org.apache.http.nio.NHttpServerConnection;
//import org.apache.http.nio.NHttpServerEventHandler;
//import org.apache.http.nio.protocol.HttpAsyncService;
//import org.armada.galileo.common.util.JsonUtil;
//import org.armada.galileo.document.domain.OpenRequestMessageQueryVO;
//import org.armada.galileo.document.proxy.RunnableProxy;
//import org.armada.galileo.document.proxy.quene.HaiqToCustomerUtil;
//import org.armada.galileo.document.service.SdkService;
//import org.armada.galileo.document.util.RequestMessageQueneUtil;
//import org.armada.galileo.mvc_plus.support.MiniWebxServlet;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.web.servlet.ServletRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.*;
//import java.net.InetAddress;
//import java.net.Socket;
//import java.nio.charset.StandardCharsets;
//import java.util.*;
//
///**
// * @author xiaobo
// * @date 2022/1/10 3:42 下午
// */
//@Slf4j
//public class HaiqToCustomerSocket implements RunnableProxy {
//
//    @Autowired
//    private SdkService sdkService;
//
//    static Map<String, String> notifyAddress = new HashMap<>();
//
//    // TODO 记录每个接口的回传地址
//    static {
//        notifyAddress.put("com.hairoutech.iwms.client.api.CallBackApi1.outboundSkuLossNotify", "12.3.1.3:2222");
//        notifyAddress.put("com.hairoutech.iwms.client.api.CallBackApi1.stocktakeResultNotify", "12.3.1.3:1111");
//    }
//
//
//
//
//
//    @Override
//    public void run() {
//
//        while (true) {
//            try {
//                /* -----  step 1 ------ */
//                // 从列队中获取一个待回传的任务， 如果没有待回传的任务， 这里会阻塞
//                HaiqToCustomerUtil.HaiqToCustomerNotifyMessageGroup msg = HaiqToCustomerUtil.poll();
//                log.info("[sdk-sokcet] 开始处理回传, input:" + JsonUtil.toJson(msg));
//                String socketAddress = notifyAddress.get(msg.getApiUrl());
//                if (socketAddress == null) {
//                    log.warn("未配置回传地址");
//                    continue;
//                }
//                Long l1 = System.currentTimeMillis();
//
//
//                /* -----  step 2 ------  */
//                //  数据转换，将我方 json 转换成客户需要的回传格式；如果是拆成多次回传，需要拆成多个数据包
//                byte[] reqData = doTransfer(msg.getApiUrl(), msg.getNotifyJson());
//
//
//                /* -----  step 3 ------ */
//                // 网络请求
//                String res = doRequest(socketAddress, reqData);
//
//
//                /*-----  step 4 ------*/
//                // 更新日志 （以下代码必须要执行成功，否则会一直重试）
//                String requestId = msg.getRequestId();
//                // TODO 判断回传成功、失败？ 需要根据客户返回的数据包来判断
//                String status = "success"; // success fail
//                int costTime = (int) (System.currentTimeMillis() - l1);
//                String errorMsg = null;
//                updateRequestLog(requestId, status, costTime, errorMsg);
//
//
//                // 休眠
//                Thread.sleep(100);
//
//
//            } catch (Exception e) {
//
//                log.error(e.getMessage(), e);
//
//                try {
//                    Thread.sleep(100);
//                    continue;
//                } catch (Exception exxx) {
//                }
//            }
//        }
//
//    }
//
//
//    @Override
//    public void beforeDestroy() {
//
//    }
//
//
//    /**
//     * 数据协议转换， 将 inputjson 转换成客户要求的格式
//     *
//     * @param apiUrl
//     * @param inputJson
//     * @return
//     */
//    public byte[] doTransfer(String apiUrl, String inputJson) {
//
//        // TODO 如果是出库回传
//        if ("com.hairoutech.iwms.client.api.CallBackApi1.outboundSkuLossNotify".equals(apiUrl)) {
//        }
//        // TODO 如果是xxx业务回传
//        else if ("xxxx".equals(apiUrl)) {
//        }
//
//        return null;
//    }
//
//
//    public String doRequest(String socketAddress, byte[] inputs) throws Exception {
//
//        // 和服务器创建连接
//        Socket socket = new Socket("172.18.81.79", 7208);
//
//        // 要发送给服务器的信息
//        OutputStream os = socket.getOutputStream();
//
//        os.write(inputs);
//        os.flush();
//
//        socket.shutdownOutput();
//
//        // 从服务器接收的信息
//        InputStream is = socket.getInputStream();
//        BufferedReader br = new BufferedReader(new InputStreamReader(is));
//
//        // 需要在这里解析网络通信包  TODO
//        String info = null;
//        while ((info = br.readLine()) != null) {
//            System.out.println("我是客户端，服务器返回信息：" + info);
//        }
//
//        br.close();
//        is.close();
//        os.close();
//        socket.close();
//
//        return info;
//    }
//
//
//    public void updateRequestLog(String requestId, String status, int costTime, String errorMsg) {
//
//        OpenRequestMessageQueryVO rm = sdkService.findById(requestId);
//        if (rm == null) {
//            log.error("数据异常, request log 不存在, requestId: " + requestId);
//            return;
//        }
//        rm.setStatus(status);
//        rm.setErrorMessage(errorMsg);
//        rm.setTimeCost(costTime);
//        rm.setUpdateTime(new Date());
//
//        sdkService.update(rm);
//    }
//
//
//
//}
