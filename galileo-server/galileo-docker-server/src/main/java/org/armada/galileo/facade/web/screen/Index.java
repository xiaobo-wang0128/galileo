package org.armada.galileo.facade.web.screen;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.rainbow_gate.transfer.discovery.LocalServerAddressUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;

/**
 * @author xiaobo
 * @date 2022/3/12 4:03 PM
 */
@Controller
@Slf4j
public class Index {

    @Value("${docker.image.out_download_url}")
    private String out_download_url;

    @Value("${docker.image.inner_download_url}")
    private String inner_download_url;

    public void execute(HttpServletRequest request) {

        String deployAddress = LocalServerAddressUtil.getLocalServerAddress();

//        String ip = request.getRemoteHost();
//        boolean lanIp = CommonUtil.isLanIp(ip);

        String sourceHost = request.getHeader("Host");

//        log.info("remote ip:" + ip + ", islanIp: " + lanIp);
//        log.info("deployAddress: " + deployAddress);
//        log.info("sourceHost:" + sourceHost);

        boolean localAccess = deployAddress.indexOf(sourceHost) != -1;

        // 本地访问
        if (localAccess) {
            request.setAttribute("down_path", inner_download_url);
            request.setAttribute("access_type", "inner");
        }
        // 外网访问
        else {
            request.setAttribute("down_path", out_download_url);
            request.setAttribute("access_type", "outer");
        }

        request.setAttribute("local_address", deployAddress);

    }
}
