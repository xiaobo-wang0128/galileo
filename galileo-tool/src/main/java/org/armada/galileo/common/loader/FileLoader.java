package org.armada.galileo.common.loader;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author xiaobo
 * @date 2021/8/21 3:35 下午
 */
@Slf4j
public class FileLoader {

    /**
     * 文件对应的输入流
     *
     * @param fileName classpath 文件路径，不能以 / 开头
     * @return
     */
    public static List<byte[]> loadResourceFiles(String fileName) {

        List<URL> list = new ArrayList<URL>();
        try {
            Enumeration<URL> urls = ClassHelper.getClassLoader().getResources(fileName);
            list = new ArrayList<java.net.URL>();
            while (urls.hasMoreElements()) {
                list.add(urls.nextElement());
            }
        } catch (Throwable t) {
            log.debug("Fail to load " + fileName + " file: " + t.getMessage(), t);
            throw new RuntimeException("文件读取失败：" + t.getMessage(), t);
        }

        List<byte[]> results = new ArrayList<byte[]>();

        for (java.net.URL url : list) {
            InputStream is = null;
            ByteArrayOutputStream bos = null;
            try {
                is = url.openStream();
                bos = new ByteArrayOutputStream();

                byte[] buf = new byte[4096];
                int len = 0;
                while ((len = is.read(buf)) != -1) {
                    bos.write(buf, 0, len);
                }

                byte[] result = bos.toByteArray();

                results.add(result);

            } catch (Throwable e) {
                log.debug("Fail to load " + fileName + " file from " + url + "(ingore this file): " + e.getMessage());
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e) {
                    }
                }
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
        return results;
    }


    /**
     * 从项目classpath目录读取文件
     *
     * @param filePath 不要以/开头
     * @return
     */
    public static byte[] loadResourceFile(String filePath) {
        byte[] result = null;
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        try {
            is = ConfigLoader.loadResource(filePath, true, false);
            bos = new ByteArrayOutputStream();

            byte[] buf = new byte[4096];
            int len = 0;
            while ((len = is.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }

            result = bos.toByteArray();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error("close input stream fail");
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    log.error("close output stream fail");
                }
            }
        }
        return result;
    }
}
