package test.location;

import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.Convert;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.common.util.PrintSetter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author xiaobo
 * @date 2021/12/7 10:11 上午
 */
@Slf4j
public class PrintTest {

    static String path = "/Users/wangxiaobo/Downloads/16地图.json";

    /**
     * 地图url
     */
    private static String mapUrl = "http://nova-ess-p:9000/map/export";

    public static void main(String[] args) throws Exception {

        byte[] bytes = null;
        URL url = new URL(mapUrl);
        InputStream is = null;
        long l1 = System.currentTimeMillis();
        try {
            is = url.openStream();
            bytes = decompress(is);

        } catch (Exception e) {

            log.error(e.getMessage(), e);
            return;

        } finally {

            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        long l2 = System.currentTimeMillis();
        log.info("ess 地图加载耗时: {} ms", (l2 - l1));

        String json = new String(bytes);

        Map<String, Object> out = JsonUtil.fromJson(json, new TypeToken<Map<String, Object>>() {
        }.getType());

        for (Map.Entry<String, Object> entry : out.entrySet()) {
            System.out.println(entry.getKey());
        }

    }


    public static byte[] decompress(InputStream is) throws Exception {

        ZipInputStream zis = new ZipInputStream(is);
        // 解压操作
        ZipEntry zipEntry = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((zipEntry = zis.getNextEntry()) != null) {

            int len;
            byte[] buff = new byte[4096];
            while ((len = zis.read(buff, 0, 4096)) != -1) {
                bos.write(buff, 0, len);
            }
            bos.close();


            zis.closeEntry();
        }
        zis.close();

        return bos.toByteArray();
    }

    private static String fillZero(String input) {
        int max = 10;
        String tmp = new String(input);
        for (int i = 0; i < max - input.length(); i++) {
            tmp = "0" + tmp;
        }
        return tmp;
    }

}
