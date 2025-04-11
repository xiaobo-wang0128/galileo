package org.armada.galileo.docker_center.web.rpc;

import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.docker_center.util.DockerCookieUtil;
import org.armada.galileo.docker_center.util.DownloadThread;
import org.armada.galileo.docker_center.vo.ImageRequestVO;
import org.armada.galileo.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2022/3/12 12:11 PM
 */
@Controller
public class ImageDownloaderRpc {

    @Autowired
    private DownloadThread downloadThread;

    public static void main(String[] args) {

        System.out.println("172.20.8.203/hairounova/nova-iwms-core:asfdasfasf96".matches("\\d+\\.\\d+\\.\\d+\\.\\d+/\\w+/.*?:.*?"));

        String tmp = "172.20.8.203/hairounova/nova-iwms-core:master_adidas_202203261121";
        if (!tmp.matches("\\d+\\.\\d+\\.\\d+\\.\\d+/[\\w-_]+/.*?:.*?")) {
            throw new BizException("镜像地址格式不正确: " + tmp);
        }
    }

    public List<ImageRequestVO> submitRequest(String requestList, HttpServletRequest request, HttpServletResponse response) {

        if (CommonUtil.isEmpty(requestList)) {
            throw new BizException("镜像址不能为空");
        }

        String[] tmps = requestList.split("\\s+");
        if (CommonUtil.isEmpty(tmps)) {
            throw new BizException("镜像址不能为空");
        }

        List<String> submitImagesPaths = new ArrayList<>();
        for (String tmp : tmps) {
            tmp = tmp.trim();
            if (CommonUtil.isEmpty(tmp)) {
                continue;
            }
            if (!tmp.matches("\\d+\\.\\d+\\.\\d+\\.\\d+/[\\w-_]+/.*?:.*?")) {
                throw new BizException("镜像地址格式不正确: " + tmp);
            }
            submitImagesPaths.add(tmp);
        }

        // 添加到已有队列中
        List<ImageRequestVO> exist = DockerCookieUtil.getLocalRequests(request);
        if (exist == null) {
            exist = new ArrayList<>();
        }

        if (submitImagesPaths.size() > 0) {
            for (String submitImagePath : submitImagesPaths) {
                boolean e = exist.stream().anyMatch(kk -> kk.getImagePath().equals(submitImagePath));
                if (e) {
                    continue;
                }

                String v = submitImagePath.substring(submitImagePath.lastIndexOf("/") + 1);
                v = CommonUtil.replaceAll(v, ":", "_");
                v += ".tar";

                ImageRequestVO vo = new ImageRequestVO();
                vo.setImagePath(submitImagePath);
                vo.setFileName(v);
                vo.setStatus("saving");
                exist.add(vo);

                downloadThread.pushRequest(vo);
            }
        }

        DockerCookieUtil.updateToCookie(exist, response);
        return exist;
    }

    public List<ImageRequestVO> getDownloadUrls(HttpServletRequest request, HttpServletResponse response) {
        List<ImageRequestVO> exist = DockerCookieUtil.getLocalRequests(request);

        if (CommonUtil.isEmpty(exist)) {
            return null;
        }

        for (ImageRequestVO imageRequestVO : exist) {

            if ("done".equals(imageRequestVO.getStatus())) {
                continue;
            }

            ImageRequestVO cache = downloadThread.getImage(imageRequestVO.getImagePath());
            if (cache == null) {
                continue;
            }

            imageRequestVO.setStatus(cache.getStatus());
            imageRequestVO.setFailMessage(cache.getFailMessage());
        }
        return exist;
    }


    public void doDownload(@RequestBody List<String> imagePaths, HttpServletRequest request, HttpServletResponse response) {
        if (CommonUtil.isEmpty(imagePaths)) {
            return;
        }

        List<ImageRequestVO> exist = DockerCookieUtil.getLocalRequests(request);

        for (String imagePath : imagePaths) {

            List<ImageRequestVO> tmp = exist.stream().filter(e -> e.getImagePath().equals(imagePath)).collect(Collectors.toList());
            if (CommonUtil.isNotEmpty(tmp)) {
                ImageRequestVO vo = tmp.get(0);
                vo.setStatus("done");
            }
        }

        DockerCookieUtil.updateToCookie(exist, response);
    }


    public void clearCookie(HttpServletResponse response) {
        DockerCookieUtil.clearCookie(response);
    }

}
