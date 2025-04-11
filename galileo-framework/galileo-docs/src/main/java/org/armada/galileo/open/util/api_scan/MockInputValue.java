package org.armada.galileo.open.util.api_scan;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.open.util.api_scan.domain.ParamType;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiaobo
 * @date 2022/12/22 18:51
 */
@Slf4j
public class MockInputValue {

    private static List<String> numbers = CommonUtil.asList("short,byte,int,integer,float,double,long".split(","));

    public static Object generateMockValue(List<ParamType> paramTypes) {

        if (CommonUtil.isEmpty(paramTypes)) {
            return null;
        }

        Map<String, Object> map = new LinkedHashMap<>();
        for (ParamType paramType : paramTypes) {
            map.put(paramType.getName(), generateMockValue(paramType));
        }
        return map;
    }

    public static Object generateMockValue(ParamType paramType) {

//        if (paramType.getName().equals("platformOrderNoList")) {
//            log.debug("skuCount");
//        }

        if (CommonUtil.isEmpty(paramType) || paramType.getType() == null) {
            return null;
        }

        if (CommonUtil.isNotEmpty(paramType.getSubParams())) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (ParamType subParam : paramType.getSubParams()) {
                map.put(subParam.getName(), generateMockValue(subParam));
            }
            if (paramType.getType().indexOf("array") != -1) {
                return CommonUtil.asList(map);
            } else {
                return map;
            }
        }
        if (numbers.contains(paramType.getType()) || paramType.getType().equals("bigdecimal")) {
            return 100;
        } else if ("string".equals(paramType.getType())) {
            return "string";
        } else if ("boolean".equals(paramType.getType())) {
            return true;
        }  else if ("map".equals(paramType.getType())) {
            return new HashMap<>();
        } else if (paramType.getType().startsWith("array<")) {
            String tmpType = paramType.getType();
            int index1 = tmpType.indexOf("<");
            int index2 = tmpType.lastIndexOf(">");
            if (index1 != -1 && index2 != -1 && index1 < index2) {
                String innerType = tmpType.substring(index1 + 1, index2);
                ParamType innerParamType = new ParamType();
                innerParamType.setType(innerType);
                innerParamType.setName("");

                return new Object[]{
                        generateMockValue(innerParamType)
                };
            }

        }

        return null;
    }

}
