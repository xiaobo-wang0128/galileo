package org.armada.galileo.rainbow_gate.transfer.gate_point.gate_transfer;

/**
 * @author xiaobo
 * @date 2023/5/18 21:07
 */

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.armada.galileo.rainbow_gate.transfer.connection.http.HttpPostUtil;
import org.armada.galileo.rainbow_gate.transfer.domain.protocol.AppRequestDomain;
import org.armada.galileo.rainbow_gate.transfer.domain.protocol.AppResponseDomain;
import org.armada.galileo.rainbow_gate.transfer.gate_codec.GateCodecUtil;
import org.armada.galileo.rainbow_gate.transfer.gate_point.app_client.AppClient;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

/**
 * 数据摆渡 网关， 用于将接收到的 rainbow-rpc 数据包转发至内网应用
 */
@Slf4j
public class GateTransferServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public GateTransferServlet() {
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getOutputStream().write("not support".getBytes());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        call(request, response);
    }

    public void call(HttpServletRequest request, HttpServletResponse response) {
        long l1 = System.currentTimeMillis();
        OutputStream os = null;
        try {
            os = response.getOutputStream();

            // 解析数据流
            byte[] buffer = parseRequestData(request);

            AppRequestDomain appRequestDomain = GateCodecUtil.decodeRequestHead(buffer);

            String className = appRequestDomain.getClassName();

            String appServerAddress = AppClient.getTargetRequestUrl(null, className);

            byte[] result = HttpPostUtil.request(appServerAddress, null, buffer);

            os.write(result);

        } catch (Exception e) {

            AppResponseDomain responseDomain = new AppResponseDomain(301);
            responseDomain.setMessage(e.getMessage());
            try {
                os.write(GateCodecUtil.encodeResponse(responseDomain));
            } catch (Exception e2) {
            }

            log.error(e.getMessage(), e);

        } finally {
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (Exception e2) {
                    log.error(e2.getMessage(), e2);
                }
            }
        }
    }

    public static byte[] parseRequestData(HttpServletRequest request) throws Exception {

        String encoding = "utf-8";
        request.setCharacterEncoding(encoding);

        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload sevletFileUpload = new ServletFileUpload(factory);
        // 最大500M
        sevletFileUpload.setSizeMax(50 * 1024 * 1024);

        @SuppressWarnings("unchecked")
        List<FileItem> fileItems = (List<FileItem>) sevletFileUpload.parseRequest(request);

        byte[] data = null;

        for (Iterator<FileItem> it = fileItems.iterator(); it.hasNext(); ) {

            final FileItem item = (FileItem) it.next();

            if (!item.isFormField()) {
                java.io.InputStream is = item.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                int len = -1;
                byte[] buf = new byte[5000];
                while ((len = is.read(buf)) != -1) {
                    bos.write(buf, 0, len);
                }
                is.close();
                bos.flush();
                bos.close();
                data = bos.toByteArray();

                break;
            }
        }
        return data;
    }
}