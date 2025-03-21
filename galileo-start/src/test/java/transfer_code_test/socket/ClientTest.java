package transfer_code_test.socket;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 模拟客户测试， 发送下发接口
 *
 * @author xiaobo
 * @date 2021/12/30 5:45 下午
 */
public class ClientTest implements Runnable {


    public static void main(String[] args) {
        new Thread(new ClientTest()).start();
    }

    static String testInput = "[{\"lpnCode\": \"string value\", \"abnormal\": 0, \"supplier\": \"string value\", \"customerCode\": \"string value\", \"warehouseCode\": \"string value\", \"inboundReceipt\": \"string value\", \"customerOrderNo\": \"string value\", \"scanBatchNumber\": 0, \"inboundOrderType\": \"string value\", \"inboundOrderDetails\": [{\"skuCode\": \"string value\", \"skuName\": \"string value\", \"qtyInbound\": 0, \"containerTag\": \"string value\", \"qtyRestocked\": 0, \"containerCode\": \"string value\", \"subContainerCode\": \"string value\", \"containerSpecCode\": \"string value\"}]}]";


    @Override
    public void run() {

        try {
            // 和服务器创建连接
            Socket socket = new Socket("172.18.81.79", 7208);


            // 要发送给服务器的信息
            OutputStream os = socket.getOutputStream();

            int i = 0;
            while (true) {
                os.write(testInput.getBytes(StandardCharsets.UTF_8));
                os.write("\n".getBytes());

                os.flush();


                Thread.sleep(2000);
                if (i++ > 10000) {
                    break;
                }
            }

            socket.shutdownOutput();

            // 从服务器接收的信息
            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String info = null;
            while ((info = br.readLine()) != null) {
                System.out.println("我是客户端，服务器返回信息：" + info);
            }


            br.close();
            is.close();
            os.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void dd() {

    }
}
