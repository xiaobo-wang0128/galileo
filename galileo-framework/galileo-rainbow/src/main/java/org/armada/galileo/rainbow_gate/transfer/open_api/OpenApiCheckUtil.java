package org.armada.galileo.rainbow_gate.transfer.open_api;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.annotation.openapi.ApiRole;
import org.armada.galileo.annotation.openapi.AppId;
import org.armada.galileo.annotation.openapi.OpenApiMethod;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.open.util.api_scan.domain.DocItem;
import org.armada.galileo.open.util.api_scan.group_domain.InnerGroup;
import org.armada.galileo.open.util.api_scan.group_domain.MainGroup;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaobo
 * @date 2023/4/19 20:14
 */
@Slf4j
public class OpenApiCheckUtil {

    private static String baseType = "long, integer, short, byte, double, float, boolean";


    public static void main(String[] args) {

        for (Method method : OpenApiCheckUtil.class.getDeclaredMethods()) {

            if (!method.getName().equals("validate")) {
                continue;
            }

            Annotation[][] annotations = method.getParameterAnnotations();

            if (annotations == null || annotations.length == 0) {
                continue;
            }

            System.out.println(annotations[0][0].annotationType().getName());

            System.out.println(AppId.class.getName());


            if (annotations != null) {
                for (Annotation[] annotation : annotations) {
                    for (Annotation annotation1 : annotation) {
                        System.out.println(annotation1);
                    }
                }
            }


            if (annotations != null && annotations.length > 0 && annotations[0].getClass().getName().equals(AppId.class.getName())) {
                System.out.println(true);
            }


        }

    }

    public static Map<String, InvokeObject> validate(@AppId MainGroup mainGroup, ApplicationContext applicationContext) {

        if (mainGroup == null) {
            return null;
        }

        Map<String, Object> targetCache = new HashMap<>();
        Map<String, Class> classCache = new HashMap<>();

        Map<String, InvokeObject> targetObjectMap = new HashMap<>();

        for (InnerGroup group : mainGroup.getGroups()) {

            for (DocItem docItem : group.getChildren()) {

                String apiCode = docItem.getApiCode();
                String apiClassName = docItem.getApiClassName();

                Class clz = classCache.get(apiClassName);
                Object targetObject = targetCache.get(apiClassName);

                Method method = null;
                try {
                    if (targetObject == null || clz == null) {
                        clz = Class.forName(apiClassName);

                        // 被动接收请求
                        if (docItem.getApiFrom().equals(ApiRole.KYB.toString())) {
                            continue;
                        }

                        targetObject = applicationContext.getBean(clz);

                        classCache.put(apiClassName, clz);
                        targetCache.put(apiClassName, targetObject);
                    }

                    for (Method m : clz.getDeclaredMethods()) {
                        if (m.isAnnotationPresent(OpenApiMethod.class)) {
                            OpenApiMethod openApiMethod = m.getAnnotation(OpenApiMethod.class);
                            if (apiCode.equals(openApiMethod.code())) {
                                method = m;
                                break;
                            }
                        }
                    }

                    if (method == null) {
                        throw new BizException("开放平台校验失败, 没有找到对应的方法 apiCode:" + apiCode);
                    }

                    Type[] inputTypes = method.getGenericParameterTypes();


                    // 校验 开放平台方法入参必须只有一个对象， 且不能为基础类型 ( JSON 反序列化效率问题 )
                    int paramNums = 0;

                    for (int i = 0; i < inputTypes.length; i++) {

                        Type parameterType = inputTypes[i];

                        // 上下文对象
                        if (parameterType.getTypeName().equals(OpenApiContext.class.getName())) {
                            continue;
                        }
//                        // appid
//                        else if (parameterType.getClass().isAnnotationPresent(AppId.class)) {
//                            continue;
//                        }
                        // 数据参数对象
                        else {

                            // appid
                            Annotation[] annotations = method.getParameterAnnotations()[i];
                            if (annotations != null && annotations.length > 0 && annotations[0].annotationType().getName().equals(AppId.class.getName())) {
                                continue;
                            }

                            String typeName = parameterType.getTypeName();
                            if (baseType.indexOf(typeName) != -1) {
                                throw new BizException("开放平台入参不支持基础类型: " + typeName);
                            }

                            if (typeName.startsWith("java.lang")) {
                                throw new BizException("开放平台入参不支持基础类型: " + typeName);
                            }

                            paramNums++;
                        }
                    }

                    if (paramNums > 1) {
                        throw new BizException("开放平台入参只有一个对象入参, apiCode:{}, apiClassName:{} ", apiCode, apiClassName);
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }

                String[] paramNames = null;
                if (CommonUtil.isNotEmpty(docItem.getInputParams())) {
                    paramNames = new String[docItem.getInputParams().size()];
                    for (int i = 0; i < docItem.getInputParams().size(); i++) {
                        paramNames[i] = docItem.getInputParams().get(i).getName();
                    }
                }

                targetObjectMap.put(docItem.getApiCode(), new InvokeObject(method, paramNames, targetObject));
            }

        }

        return targetObjectMap;

    }


}
