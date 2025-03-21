package org.armada.galileo.docker_center.util;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.docker_center.vo.ImageRequestVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author xiaobo
 * @date 2022/3/12 5:46 PM
 */
@Slf4j
@Component
public class DownloadThread implements CommandLineRunner {

    private static BlockingQueue<ImageRequestVO> quene = new LinkedBlockingQueue<>();

    private static Map<String, ImageRequestVO> cache = new ConcurrentHashMap<>();

    @Value("${docker.image.save_path}")
    private String SavePath;

    @Override
    public void run(String... args) throws Exception {

        log.info("docker image file save path: " + SavePath);

        new Thread(() -> {

            while (true) {
                try {
                    ImageRequestVO image = quene.take();

                    if (new File(SavePath + "/" + image.getFileName()).exists()) {
                        image.setStatus("success");
                        cache.put(image.getImagePath(), image);
                        continue;
                    }

                    CommonUtil.makeSureFolderExists(SavePath + "/" + image.getFileName());

                    try {
                        String cmd = CommonUtil.format("docker pull {}", image.getImagePath());
                        log.info(cmd);
                        CommonUtil.execCmd(cmd);

                        cmd = CommonUtil.format("docker save -o {} {}", SavePath + "/" + image.getFileName(), image.getImagePath());
                        log.info(cmd);
                        CommonUtil.execCmd(cmd);

                        // 上传至 oss
//                        cmd = CommonUtil.format("/data/workspace/docker-center/util/ossutil64 cp -r -f {} oss://hairouapollo/{} --config-file /data/workspace/docker-center/util/docker_upload.cfg"
//                                , SavePath + "/" + image.getFileName(), "docker_image/" + image.getFileName());
//                        log.info(cmd);
//                        CommonUtil.execCmd(cmd);

                        try {
                            cmd = CommonUtil.format("docker rmi {} --force", image.getImagePath());
                            log.info(cmd);
                            CommonUtil.execCmd(cmd);
                        } catch (Exception e) {
                            log.warn(e.getMessage());
                        }

                        image.setStatus("success");

                    } catch (Exception e) {
                        image.setStatus("fail");
                        image.setFailMessage("镜像不存在");
                        log.warn(e.getMessage());
                    }

                    image.setDoneTime(System.currentTimeMillis());
                    cache.put(image.getImagePath(), image);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

        }).start();


        new Thread(() -> {

            while (true) {
                try {
                    Thread.sleep(24 * 60 * 60 * 1000);

                    List<String> needDelete = new ArrayList<>();
                    for (Map.Entry<String, ImageRequestVO> entry : cache.entrySet()) {
                        ImageRequestVO image = entry.getValue();
                        if (image.getDoneTime() != null && image.getDoneTime() < System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000) {
                            needDelete.add(entry.getKey());
                        }
                    }
                    for (String s : needDelete) {
                        cache.remove(s);
                    }


                    File folder = new File(SavePath);

                    // 镜像缓存天数控制
                    long ago = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000;

                    if (folder.exists()) {

                        for (File f : folder.listFiles()) {

                            // 只删业务镜像， 其他中间件镜像保留
                            if (f.getName().matches("\\d+\\.\\d+\\.\\d+\\.\\d+.*?") && f.lastModified() < ago) {
                                try {
                                    f.delete();
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

        }).start();

    }

    private static Executor ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    public void pushRequest(ImageRequestVO vo) {
        ex.execute(() -> {
            quene.add(vo);
        });
    }

    public ImageRequestVO getImage(String imtPath) {
        return cache.get(imtPath);
    }


}
