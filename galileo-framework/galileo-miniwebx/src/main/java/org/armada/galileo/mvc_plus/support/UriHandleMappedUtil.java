package org.armada.galileo.mvc_plus.support;

import org.armada.galileo.annotation.mvc.RewritePath;
import org.armada.galileo.common.threadlocal.ThreadJsonParam;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.common.util.ParameterNameDiscovererCache;
import org.armada.galileo.mvc_plus.constant.SuffixConstant;
import org.armada.galileo.mvc_plus.velocity.ControllUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.Conventions;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.WebArgumentResolver;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class UriHandleMappedUtil implements ApplicationContextAware {

    private String rootPackage;

    public final String htmSuffixt = ".htm";

    public final String jsonSuffix = ".json";

    private boolean detectHandlersInAncestorContexts = false;

    private static Map<String, UriMappedHandler> uriMap = new TreeMap<String, UriMappedHandler>();

    private static Map<String, UriMappedHandler> uriRewritelMap = new TreeMap<String, UriMappedHandler>();

    private static Logger log = LoggerFactory.getLogger(UriHandleMappedUtil.class);

    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void detectHandlerByServlet() throws BeansException {
        log.info("Looking for URL mappings in application context: " + getApplicationContext());

        String[] beanNames = (this.detectHandlersInAncestorContexts ? BeanFactoryUtils.beanNamesForTypeIncludingAncestors(getApplicationContext(), Object.class) : getApplicationContext().getBeanNamesForType(Object.class));

        // 扫描 rootPackage包下的 Controller
        for (String beanName : beanNames) {
            if (beanName.equals("mxServletRegistrationBean")) {
                continue;
            }
            try {
                generateUriPath4MiniWebx(beanName);
                // generateUriPath4SpringMvc(beanName);
            } catch (Exception e) {
                // log.error("request URI 初始化失败，包路径有误，beanName:" + beanName);
                log.error(e.getMessage(), e);
            }

        }
    }


    public void generateUriPath4SpringMvc(String beanName) {

        ApplicationContext context = getApplicationContext();
        Class<?> cls = context.getType(beanName);


        if (cls == null) {
            return;
        }

        if (!cls.getName().startsWith(rootPackage)) {
            return;
        }


        Object handleObject = context.getBean(beanName);

        RequestMapping classRequestMapping = cls.getAnnotation(RequestMapping.class);
        if (classRequestMapping == null) {
            return;
        }

        if (classRequestMapping.value().length == 0) {
            throw new RuntimeException("RequestMapping 配置不正确");
        }

        String rootPath = classRequestMapping.value()[0];

        Method[] ms = cls.getDeclaredMethods();
        for (Method method : ms) {
            RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
            if (methodRequestMapping == null) {
                continue;
            }
            if (methodRequestMapping.value().length == 0) {
                throw new RuntimeException("RequestMapping 配置不正确");
            }

            String subPath = methodRequestMapping.value()[0];

            String pathName = "/" + rootPath + "/" + subPath;
            pathName = pathName.replaceAll("/+", "/");
            if (pathName.endsWith("/")) {
                pathName = pathName.substring(0, pathName.length() - 1);
            }
            pathName = pathName + ".json";

            log.info("[miniwebx] mapped uri \"" + pathName + "\" to " + handleObject.getClass().getName());
            uriMap.put(pathName, new UriMappedHandler(pathName, handleObject, method));

        }
    }

    public void generateUriPath4MiniWebx(String beanName) {

        ApplicationContext context = getApplicationContext();
        Class<?> cls = context.getType(beanName);

        if (cls == null) {
            return;
        }

        if (!cls.getName().startsWith(rootPackage)) {
            return;
        }

        Object handleObject = context.getBean(beanName);

        String clsName = cls.getName();
        if (!clsName.startsWith(rootPackage)) {
            return;
        }
        if (!clsName.matches(rootPackage + ".\\S+.web.rpc.\\S+") && !clsName.matches(rootPackage + ".\\S+.web.screen.\\S+")) {
            return;
        }

        int sp1 = rootPackage.length() + 1;
        int sp2 = clsName.indexOf(".", sp1 + 1);
        int sp3 = clsName.indexOf(".", sp2 + 1);
        String moduleName = clsName.substring(sp1, sp2);
        String layerName = clsName.substring(sp2 + 1, sp3);
        if (!layerName.equals("web")) {
            return;
        }
        int sp4 = clsName.indexOf(".", sp3 + 1);
        String webType = clsName.substring(sp3 + 1, sp4);

        // screen
        if (webType.equals("screen")) {
            String pathName = clsName.substring(sp4 + 1);

            // facade模块默认为根路径
            if (moduleName.equals("facade")) {
                pathName = "/" + pathName.replace(".", "/").toLowerCase() + htmSuffixt;
            } else {
                pathName = "/" + moduleName + "/" + pathName.replace(".", "/").toLowerCase() + htmSuffixt;
            }

            // Method[] ms = cls.getDeclaredMethods();
            Method[] ms = cls.getMethods();
            for (Method method : ms) {
                if (method.getName().equals("execute")) {

                    // url重写处理
                    if (method.isAnnotationPresent(RewritePath.class)) {
                        RewritePath rm = method.getAnnotation(RewritePath.class);
                        String rmValue = rm.value();

                        if (rmValue != null && !rmValue.matches("\\s*")) {
                            // 将 RequestMapping中定义的 {xxx}变量名取出
                            ArrayList<String> vars = new ArrayList<String>();
                            int index1 = -1;
                            int index2 = -1;
                            while (true) {
                                index1 = rmValue.indexOf("{", index2 + 1);
                                if (index1 == -1) {
                                    break;
                                }
                                index2 = rmValue.indexOf("}", index1 + 1);
                                if (index2 == -1) {
                                    break;
                                }
                                String tmp = rmValue.substring(index1 + 1, index2);
                                if (!tmp.matches("\\s*")) {
                                    vars.add(tmp);
                                }
                            }
                            if (vars.size() == 0) {
                                throw new RuntimeException("请在 RequestMapping 中设置变量，如  {textId}_{textId}");
                            }
                            vars.trimToSize();
                            String pattern = rmValue;
                            for (String var : vars) {
                                pattern = pattern.replace("{" + var + "}", "*");
                            }
                            pathName = pathName.substring(0, pathName.length() - htmSuffixt.length()) + "/" + pattern + htmSuffixt;

                            UriMappedHandler handler = new UriMappedHandler(pathName, handleObject, method);
                            handler.setIsRewrite(true);
                            handler.setFieldNames(vars);

                            String mappedUriHead = pathName.substring(0, pathName.lastIndexOf("/"));
                            regUrlRewriteMapper(mappedUriHead, handler);
                        }
                    }
                    // 普通请求
                    else {
                        regUrlMapper(pathName, new UriMappedHandler(pathName, handleObject, method));
                        if (pathName.equals("/index.htm")) {
                            regUrlMapper("/", new UriMappedHandler("/", handleObject, method));
                        }
                    }

                    break;
                }
            }
            return;
        }
        // rpc
        else if (webType.equals("rpc")) {
            String pathName = clsName.substring(sp4 + 1);
            // String pathName = clsName.substring(sp4 + 1).toLowerCase();
            // if (pathName.endsWith("rpc")) {
            // pathName = pathName.substring(0, pathName.length() - 3);
            // }

            Method[] ms = cls.getDeclaredMethods();
            String[] uris = new String[ms.length];
            for (int i = 0; i < ms.length; i++) {
                // 只扫描public方法
                if (!Modifier.isPublic(ms[i].getModifiers())) {
                    continue;
                }
                String methodName = ms[i].getName();
                if (methodName.indexOf("$") != -1) {
                    continue;
                }

                String uriName = new StringBuilder("/").append(moduleName).append("/").append(pathName.replace(".", "/")).append("/").append(methodName).append(jsonSuffix).toString();
                // String uriName = "/" + moduleName + "/" + pathName.replace(".",
                // "/").toLowerCase() + "/" + methodName + jsonSuffix;
                uris[i] = uriName;

                regUrlMapper(uriName, new UriMappedHandler(uriName, handleObject, ms[i]));
            }
            return;
        }
        // controll
        else if (webType.equals("controll")) {
            String pathName = clsName.substring(sp4 + 1);
            pathName = "/" + moduleName + "/" + pathName.replace(".", "/").toLowerCase() + ".vm";
            if (handleObject instanceof ControllInterface) {
                ControllUtil.putPaths(pathName, (ControllInterface) handleObject);
            } else {
                throw new RuntimeException(cls + "必须实现 ControllInterface 接口");
            }
        }
        return;
    }


    public Object invokeHandlerMethod(UriMappedHandler uriMappedHandler, Method handlerMethod, Object handler, HttpServletRequest request, HttpServletResponse response) throws Exception {

        Method handlerMethodToInvoke = BridgeMethodResolver.findBridgedMethod(handlerMethod);

        Object[] args = resolveHandlerArguments(uriMappedHandler, handlerMethodToInvoke, handler, request, response, null);

        ReflectionUtils.makeAccessible(handlerMethodToInvoke);
        return handlerMethodToInvoke.invoke(handler, args);
    }

    @SuppressWarnings("rawtypes")
    private Object[] resolveHandlerArguments(UriMappedHandler uriMappedHandler, Method handlerMethod, Object handler, HttpServletRequest httpServletRequest, HttpServletResponse response, ExtendedModelMap implicitModel) throws Exception {

        HttpServletRequest request = null;

        // 启用id自动加解密
//		if (MiniWebxServlet.AutoEncrptyIdField) {
//			request = new ServletRequestWrap(httpServletRequest);
//		} else {
//			request = httpServletRequest;
//		}

        request = httpServletRequest;

        Class[] paramTypes = handlerMethod.getParameterTypes();
        Object[] args = new Object[paramTypes.length];

        for (int i = 0; i < args.length; i++) {
            MethodParameter methodParam = new MethodParameter(handlerMethod, i);
            methodParam.initParameterNameDiscovery(ParameterNameDiscovererCache.getParameterNameDiscoverer());
            GenericTypeResolver.resolveParameterType(methodParam, handler.getClass());
            String paramName = null;
            // String pathVarName = null;
            String attrName = null;
            boolean required = false;
            String defaultValue = null;

            // 是否是json请求格式
            boolean isJsonParam = handlerMethod.getParameters()[i].isAnnotationPresent(RequestBody.class);
            if (isJsonParam) {

                String requestJson = CommonUtil.readJsonForm(request);
                args[i] = JsonUtil.fromJson(requestJson, handlerMethod.getParameters()[i].getParameterizedType());

                ThreadJsonParam.set(requestJson);
                continue;
            }

            {
                Object argValue = resolveCommonArgument(methodParam, request, response);
                if (argValue != WebArgumentResolver.UNRESOLVED) {
                    args[i] = argValue;
                } else {
                    Class paramType = methodParam.getParameterType();
                    if (BeanUtils.isSimpleProperty(paramType)) {
                        paramName = "";
                    } else {
                        attrName = "";
                    }
                }
            }

            // 基础参数
            if (paramName != null) {
                args[i] = resolveRequestParam(paramName, required, defaultValue, methodParam, request, handler);
            }
            // 对象参数
            else if (attrName != null) {
                WebDataBinder binder = resolveModelAttribute(attrName, methodParam, implicitModel, request, handler);
                boolean assignBindingResult = (args.length > i + 1 && Errors.class.isAssignableFrom(paramTypes[i + 1]));
                if (binder.getTarget() != null) {

                    ServletRequestDataBinder servletBinder = (ServletRequestDataBinder) binder;
                    servletBinder.bind(request);
                }
                args[i] = binder.getTarget();
                if (assignBindingResult) {
                    args[i + 1] = binder.getBindingResult();
                    i++;
                }
            }
        }

        return args;
    }

    private WebDataBinder resolveModelAttribute(String attrName, MethodParameter methodParam, ExtendedModelMap implicitModel, HttpServletRequest webRequest, Object handler) throws Exception {

        // Bind request parameter onto object...
        String name = attrName;
        if ("".equals(name)) {
            name = Conventions.getVariableNameForParameter(methodParam);
        }
        Class<?> paramType = methodParam.getParameterType();
        Object bindObject = BeanUtils.instantiateClass(paramType);
        WebDataBinder binder = createBinder(webRequest, bindObject, name);
        initBinder(handler, name, binder, webRequest);
        return binder;
    }

    @SuppressWarnings("rawtypes")
    protected Object resolveCommonArgument(MethodParameter methodParameter, HttpServletRequest request, HttpServletResponse response) throws Exception {

        // Resolution of standard parameter types...
        Class paramType = methodParameter.getParameterType();
        Object value = resolveStandardArgument(paramType, request, response);
        if (value != WebArgumentResolver.UNRESOLVED && !ClassUtils.isAssignableValue(paramType, value)) {
            throw new IllegalStateException("Standard argument type [" + paramType.getName() + "] resolved to incompatible value of type [" + (value != null ? value.getClass() : null) + "]. Consider declaring the argument type in a less specific fashion.");
        }
        return value;
    }

    @SuppressWarnings("rawtypes")
    protected Object resolveStandardArgument(Class parameterType, HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (ServletRequest.class.isAssignableFrom(parameterType)) {
            return request;
        } else if (ServletResponse.class.isAssignableFrom(parameterType)) {
            return response;
        } else if (HttpSession.class.isAssignableFrom(parameterType)) {
            return request.getSession();
        }
        return WebArgumentResolver.UNRESOLVED;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object resolveRequestParam(String paramName, boolean required, String defaultValue, MethodParameter methodParam, HttpServletRequest webRequest, Object handlerForInitBinderCall) throws Exception {

        Class<?> paramType = methodParam.getParameterType();

        if (Map.class.isAssignableFrom(paramType) && paramName.length() == 0) {
            return resolveRequestParamMap((Class<? extends Map>) paramType, webRequest);
        }
        if (paramName.length() == 0) {
            paramName = getRequiredParameterName(methodParam);
        }
        Object paramValue = null;
        if (paramValue == null) {
            String[] paramValues = webRequest.getParameterValues(paramName);
            if (paramValues != null) {
                paramValue = (paramValues.length == 1 ? paramValues[0] : paramValues);
            }
        }

//        // 对前端 id 自动解密
//        if (paramName != null && (paramName.equals("id") || paramName.endsWith("Id"))) {
//            if (paramValue != null) {
//                String v = (String) paramValue;
//                if (v.length() == 32) {
//                    paramValue = SecurityIDUtil.decryptId(v);
//                }
//            } else {
//                String[] paramValues = webRequest.getParameterValues("s_" + paramName);
//                if (paramValues != null && paramValues[0].length() == 32) {
//                    paramValue = SecurityIDUtil.decryptId(paramValues[0]);
//                }
//            }
//        }

        WebDataBinder binder = createBinder(webRequest, null, paramName);
        initBinder(handlerForInitBinderCall, paramName, binder, webRequest);

        return binder.convertIfNecessary(paramValue, paramType, methodParam);

    }

    protected void initBinder(Object handler, String attrName, WebDataBinder binder, HttpServletRequest webRequest) throws Exception {
        FormattingConversionServiceFactoryBean conversionServiceBean = getApplicationContext().getBean(FormattingConversionServiceFactoryBean.class);
        binder.setConversionService(conversionServiceBean.getObject());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map resolveRequestParamMap(Class<? extends Map> mapType, HttpServletRequest webRequest) {
        Map<String, String[]> parameterMap = webRequest.getParameterMap();
        if (MultiValueMap.class.isAssignableFrom(mapType)) {
            MultiValueMap<String, String> result = new LinkedMultiValueMap<String, String>(parameterMap.size());
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                for (String value : entry.getValue()) {
                    result.add(entry.getKey(), value);
                }
            }
            return result;
        } else {
            Map<String, String> result = new LinkedHashMap<String, String>(parameterMap.size());
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                if (entry.getValue().length > 0) {
                    result.put(entry.getKey(), entry.getValue()[0]);
                }
            }
            return result;
        }
    }

    private String getRequiredParameterName(MethodParameter methodParam) {
        String name = methodParam.getParameterName();
        if (name == null) {
            throw new IllegalStateException("No parameter name specified for argument of type [" + methodParam.getParameterType().getName() + "], and no parameter name information found in class file either.");
        }
        return name;
    }

    protected WebDataBinder createBinder(HttpServletRequest webRequest, Object target, String objectName) throws Exception {
        return new ServletRequestDataBinder(target, objectName);
    }

    private void regUrlMapper(String uri, UriMappedHandler handler) {
        if (this.contextPath != null && uri.endsWith(SuffixConstant.JSON_SUFFIX)) {
            uri = this.contextPath + uri;
        }
        uriMap.put(uri, handler);
        log.info("[miniwebx] mapped uri \"" + uri + "\" to " + handler.getHandleObject().getClass().getName());
    }

    private void regUrlRewriteMapper(String uri, UriMappedHandler handler) {
        uriRewritelMap.put(uri, handler);
        log.info("[miniwebx] mapped uri \"" + uri + "\" to " + handler.getHandleObject().getClass().getName());
    }

    public UriMappedHandler getMappedHandler(String uri) {
        return uriMap.get(uri);
    }

    public Map<String, UriMappedHandler> getUriMap() {
        return uriMap;
    }

    public Map<String, UriMappedHandler> getUriRewritelMap() {
        return uriRewritelMap;
    }

    public UriMappedHandler getMappedRewriteHandler(String uri) {
        if (uri == null) {
            return null;
        }
        uri = uri.substring(0, uri.lastIndexOf("/"));
        return uriRewritelMap.get(uri);
    }

    public void setRootPackage(String webBasePackage) {
        this.rootPackage = webBasePackage;
    }

    private String contextPath;

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

}
