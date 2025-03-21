package org.armada.galileo.common.util;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * @author xiaobo
 * @date 2021/11/29 10:24 上午
 */
public class PrintSetter {


    public static void printQuery(Class doClass, Class queryCLas) {

        String doName = doClass.getName().substring(doClass.getName().lastIndexOf(".") + 1);
        StringBuilder sb = new StringBuilder();
        sb.append("LambdaQueryWrapper<" + doName + "> queryWrapper = new LambdaQueryWrapper<>();\n");

        for (Method m : queryCLas.getDeclaredMethods()) {
            if (!m.getName().startsWith("get")) {
                continue;
            }
            String methodName = m.getName();
            String fieldName = methodName.substring(3);
            String rName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
            String rtype = m.getReturnType().getName();

            String ifLine = "";
            boolean isin = false;
            if (rtype.endsWith("String")) {
                ifLine = "if(CommonUtil.isNotEmpty(vo.{}))";
            }
            if (rtype.indexOf("List") != -1) {
                ifLine = "if(CommonUtil.isNotEmpty(vo.{}))";
                isin = true;
            } else {
                ifLine = "if(vo.{}()!=null)";
            }
            sb.append(CommonUtil.format(ifLine + "{\n", methodName));
            sb.append(CommonUtil.format("\tqueryWrapper.{}({}::{}, vo.{}());\n"
                    ,
                    isin ? "in" : "eq",
                    doName, methodName, methodName));
            sb.append("}\n");

            // System.out.println(head + "." + methodName + "(" + fieldName + ");");
        }
        System.out.println(sb.toString());
    }


    public static void print(Class<?> cls, String head) {

        System.out.println("copy from here");
        System.out.println();
        for (Method m : cls.getDeclaredMethods()) {
            if (!m.getName().startsWith("set")) {
                continue;
            }
            String methodName = m.getName();
            String fieldName = methodName.substring(3);
            fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);

            System.out.println(head + "." + methodName + "(" + fieldName + ");");
        }
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }

    public static void print(Class<?> cls, String targetHead, String srcHead) {

        System.out.println("copy from here");
        System.out.println();
        for (Method m : cls.getDeclaredMethods()) {
            if (!m.getName().startsWith("set")) {
                continue;
            }
            String methodName = m.getName();
            String fieldName = methodName.substring(3);

            String getName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

            System.out.println(targetHead + "." + methodName + "(" + srcHead + "." + getName + "());");
        }
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }
}
