package test.location;

import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.Convert;
import org.armada.galileo.common.util.JsonUtil;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.*;

/**
 * @author xiaobo
 * @date 2021/12/6 10:39 上午
 */
@Slf4j
public class LocationReaderTest {

    static String path = "/Users/wangxiaobo/Downloads/16地图.json";

    /**
     * 地图url
     */
    private static String mapUrl = "http://172.20.110.41:9000/map/export";

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

        // 库位
        List<Map<String, Object>> mapArr = (List<Map<String, Object>>) out.get("location");
        List<KsEssLocation> locations = new ArrayList<>(mapArr.size());
        for (Map<String, Object> map : mapArr) {

            Map<String, Object> position = (Map<String, Object>) map.get("position");
            List<Map<String, Object>> links = (List<Map<String, Object>>) map.get("link");

            KsEssLocation l = new KsEssLocation();
            l.setLocationCode(Convert.asString(map.get("code")));
            if (position != null) {
                l.setLocX(Convert.asInt(position.get("x")));
                l.setLocY(Convert.asInt(position.get("y")));
                l.setLocZ(Convert.asInt(position.get("z")));
            }

            if (links != null && links.size() > 0) {
                l.setRobotMapTheta(Convert.asInt(links.get(0).get("robot2mapTheta"), null));
                l.setContainerMapTheta(Convert.asInt(links.get(0).get("container2mapTheta"), null));
                String pointCode = Convert.asString(links.get(0).get("pointCode"), null);
                if (CommonUtil.isNotEmpty(pointCode) && pointCode.matches("POINT:\\d+:\\d+")) {
                    String[] tmps = pointCode.split(":");
                    l.setRobotX(Convert.asInt(tmps[1]));
                    l.setRobotY(Convert.asInt(tmps[2]));
                }
            }
            l.setLocationType(Convert.asString(map.get("locationTypeCode")));
            l.setIsLocked(Convert.asBoolean(map.get("isLocked")));
            l.setIsAbnormal(Convert.asBoolean(map.get("isAbnormal")));
            l.setAbnormalReason(Convert.asString(map.get("abnormalReason")));

            locations.add(l);
        }
        // System.out.println(JsonUtil.toJsonPretty(locations));

        // 工作站
        List<KsEssStation> ksEssStationList = new ArrayList<>();
        mapArr = (List<Map<String, Object>>) out.get("locationActor");
        main:
        for (Map<String, Object> map : mapArr) {

            Map<String, Object> boundLocation = (Map<String, Object>) map.get("boundLocation");

            // ess 编码
            String stationCode = Convert.asString(map.get("code"));
            // 工作站类型
            String stationPhysicalType = null;
            // 绑定的地图点坐标 x
            Integer stationX = null;
            // 绑定的地图点坐标 y
            Integer stationY = null;

            for (Map.Entry<String, Object> entry : boundLocation.entrySet()) {

                String key = entry.getKey();
                Object v = entry.getValue();

                if (CommonUtil.isEmpty(key)) {
                    continue;
                }

                if (key.matches(".*?:POINT:\\d+:\\d+")) {
                    List<String> point = CommonUtil.getMatchedStrs(key, "\\d+");
                    stationX = Convert.asInt(point.get(0));
                    stationY = Convert.asInt(point.get(1));
                }

                // 输送线 入
                if (key.startsWith(EssLocationTypeEnum.LT_CONVEYOR_INPUT.toString())) {
                    stationPhysicalType = "CONVEYOR";
                }
                // 输送线 出
                else if (key.startsWith(EssLocationTypeEnum.LT_CONVEYOR_OUTPUT.toString())) {
                    stationPhysicalType = "CONVEYOR";
                }
                // 输送线 同进同出
                else if (key.startsWith(EssLocationTypeEnum.LT_CONVEYOR_INOUT.toString())) {
                    stationPhysicalType = "CONVEYOR";
                }
                // 缓存货架
                else if (key.startsWith(EssLocationTypeEnum.LT_CACHE_SHELF_ENTRY.toString())) {
                    stationPhysicalType = "SHELF";
                }
                // 人工
                else if (key.startsWith(EssLocationTypeEnum.LT_LABOR.toString())) {
                    stationPhysicalType = "ROBOT";
                }

                if (stationPhysicalType != null) {
                    break;
                }

            }

            if (stationPhysicalType == null) {
                continue;
            }

            KsEssStation ksEssStation = new KsEssStation();
            ksEssStation.setStationCode(stationCode)
                    .setStationPhysicalType(stationPhysicalType)
                    .setStationX(stationX)
                    .setStationY(stationY);
            ksEssStationList.add(ksEssStation);

        }


        // 输送线库位
        mapArr = (List<Map<String, Object>>) out.get("config");
        for (Map<String, Object> map : mapArr) {
            if ("EssTmsConveyorConfig".equals(map.get("code"))) {

                Map<String, Object> property = (Map<String, Object>) map.get("property");

                String jsonString = (String) property.get("conveyorSlotRelations");

                List<Map<String, Object>> subArr = JsonUtil.fromJson(jsonString, new TypeToken<List<Map<String, Object>> >() {
                }.getType());

                subArr.stream().forEach(e -> {
                    String stationCode = Convert.asString(e.get("stationCode"));
                    String slotCode = Convert.asString(e.get("slotCode"));

                    System.out.println(stationCode + " : " + slotCode);

                });

                break;
            }
        }


        Collections.sort(ksEssStationList, new Comparator<KsEssStation>() {
            @Override
            public int compare(KsEssStation o1, KsEssStation o2) {
                return fillZero(o1.getStationCode()).compareTo(fillZero(o2.getStationCode()));
            }
        });

        long l3 = System.currentTimeMillis();

        log.info("ess 地图初始化耗时: {} ms", (l3 - l2));

        System.out.println(JsonUtil.toJsonPretty(ksEssStationList));

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
