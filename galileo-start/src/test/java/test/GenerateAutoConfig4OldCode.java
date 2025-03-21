package test;

import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;

import java.util.List;

/**
 * @author xiaobo
 * @date 2021/12/10 1:43 下午
 */
public class GenerateAutoConfig4OldCode {

    public static void main(String[] args) {

        String path = "/Users/wangxiaobo/Downloads/sysConfig.json";

        byte[] bytes = CommonUtil.readFileFromLocal(path);

        String json = new String(bytes);

        ResultMap map = JsonUtil.fromJson(json, ResultMap.class);

        map.getData().getResults().stream().forEach(e -> {

            if (e.getKsSysConfig_kind().equals("OP_ALGORITHM_GROUP_LIST")) {
                return;
            }

            String type = "";
            if ("BOOLEAN".equals(e.getKsSysConfig_type())) {
                type = "Boolean";
            } else if ("DOUBLE".equals(e.getKsSysConfig_type())) {
                type = "Double";
            } else if ("INT".equals(e.getKsSysConfig_type())) {
                type = "Integer";
            } else if ("STRING".equals(e.getKsSysConfig_type())) {
                type = "String";
            }

            String def = CommonUtil.format("@ConfigField(name = \"{}\")", e.getKsSysConfig_description());
            String str = CommonUtil.format("private {} {} = {};\n", type, e.getKsSysConfig_code(), e.getKsSysConfig_value());

            System.out.println(def);
            System.out.println(str);


        });

    }


    @lombok.Data
    private static class ResultMap {
        private ResultData data;
    }

    @lombok.Data
    private static class ResultData {
        private List<Config> results;
    }

    @lombok.Data
    private static class Config {
        private String ksSysConfig_kind;// ": "OUTBOUND_ORDER",
        private String ksSysConfig_code;// ": "OUTBOUND_ORDER_GLOBAL_UNIQUE_CHECK",
        private String ksSysConfig_description;// ": "出库订单校验策略",
        private String ksSysConfig_type;// ": "BOOLEAN",
        private String ksSysConfig_value;// ": "false"
    }


}
