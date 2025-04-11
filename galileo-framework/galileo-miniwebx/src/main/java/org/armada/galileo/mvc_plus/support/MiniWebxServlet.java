package org.armada.galileo.mvc_plus.support;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.app.Velocity;
import org.armada.galileo.common.loader.ClassHelper;
import org.armada.galileo.common.loader.ConfigLoader;
import org.armada.galileo.common.threadlocal.ThreadJsonParam;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.exception.LoginTimeoutException;
import org.armada.galileo.exception.PrivException;
import org.armada.galileo.annotation.mvc.UserDefine;
import org.armada.galileo.mvc_plus.constant.SessionConstant;
import org.armada.galileo.mvc_plus.constant.SuffixConstant;
import org.armada.galileo.mvc_plus.domain.JsonResult;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.mvc_plus.util.MimeTypes;
import org.armada.galileo.mvc_plus.velocity.ControllUtil;
import org.armada.galileo.mvc_plus.velocity.MvcVelocityHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndViewDefiningException;

/**
 * 基于 proxy 容器的 核心类，修改部分代码，用于在 interceptor中可接收 controller 中 annotation参数 快速启动的
 * sevlet，不依赖于 servlet容器 .<br/>
 * 本servlet 只处理 .json 请求
 *
 * @author wang xiaobo
 */
public class MiniWebxServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(MiniWebxServlet.class);

    private static CommonUtil util = new CommonUtil();

    /**
     * 是否启用id自动加解功能
     */
    // public static boolean AutoEncrptyIdField = false;

    /*----------------------------------------常量定义 ----------------------------------------*/
    /*---------------------------------------------------------------------------------------*/
    // spring 上下文方法名
    public static final String SPRING_CONTROLLER_INVOKE_METHOD = "SpringServlet_Spring_Controller_invoke_Method";

    // 页面路径相关
    private static final String SCREEN = "screen";
    private static final String ERRORS = "errors";
    private static final String ERROR_MESSAGE = "errorMsg";
    private static final String INDEX = "index";
    private static final String SPLIT = "/";
    private static final String VM_EXT = ".vm";

    private static final String LOGIN_PAGE = "/login.htm";
    private static final String N = "\n";

    // 错误相关
    private static final String POINT = ".";
    private static final int ERROR_NOT_FOUND = 404;
    private static final int ERROR_BIZ_ERROR = 500;
    private static final int ERROR_BIZ_SYSTEM = 503;
    private static final int ERROR_NOT_LOGIN = 401;
    private static final int ERROR_AUTH = 403;

    private static final String RESOURCE_NOT_FOUND = "resource not found";

    // content type相关
    private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
    private static final String CONTENT_TYPE_HTML = "text/html; charset=UTF-8";

    // jsonp相关
    private static final String JSONP_CALLBACK_VAR = "_callback_var";
    private static final String JSONP_CALLBACK_FUNC = "_callback_jsonp";
    private static final String EQUALS = "=";
    private static final String END = ";";
    private static final String LEFT = "(";
    private static final String RIGHT = ")";
    private static final String VAR = "var";

    // 模板渲染相关
    private static final String UTF_8 = "utf-8";
    private static final String BLANK = "";
    private static final String UTIL = "util";
    private static final String CONTROLL = "controll";

    // vm模板缓存
    private static final boolean VELOCITY_CACHE = false;

    /*---------------------------------------- 常量定义 end -----------------------------------*/
    /*---------------------------------------------------------------------------------------*/

    private ApplicationContext applicationContext;

    // 页面片段工具
    private ControllUtil controll;

    // rpc扫描包根路径
    private String webBasePackage;

    // vm 的物理路径
    private String VM_LOADER_ROOT = "views";

    // 接口地址上下文
    private String contextPath;

    private Map<String, String> vmCache = new ConcurrentHashMap<String, String>();

    private AtomicBoolean hasInit = new AtomicBoolean(false);

    /**
     * 拦截器
     */
    private List<HandlerInterceptor> interceptors;

    private UriHandleMappedUtil uriHandleMappedUtil;

    public MiniWebxServlet(String webBasePackag, String contextPath, ApplicationContext applicationContext, List<HandlerInterceptor> interceptors) {

        this.applicationContext = applicationContext;
        this.interceptors = interceptors;
        this.webBasePackage = webBasePackag;
        if (contextPath.startsWith(SPLIT)) {
            this.contextPath = contextPath;
        } else {
            this.contextPath = SPLIT + contextPath;
        }

        this.initCfg();
    }

    // private static String ServletContext = System.getProperty("server.servlet.context-path");


    /**
     * 初始化 minwebx
     */
    @SuppressWarnings("unchecked")
    public void initCfg() {

        if (!hasInit.compareAndSet(false, true)) {
            return;
        }

        // 初始化 controll 工具类
        controll = applicationContext.getAutowireCapableBeanFactory().createBean(ControllUtil.class);
        controll.setRootPath(VM_LOADER_ROOT);

        // 初始化 uri handler映射
        if (webBasePackage != null) {
            webBasePackage = webBasePackage.trim();
        }
        this.uriHandleMappedUtil = applicationContext.getAutowireCapableBeanFactory().createBean(UriHandleMappedUtil.class);
        if (contextPath != null) {
            uriHandleMappedUtil.setContextPath(contextPath);
        }
        uriHandleMappedUtil.setRootPackage(webBasePackage);
        uriHandleMappedUtil.detectHandlerByServlet();

    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatch(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatch(req, resp);
    }

    public void doDispatch(HttpServletRequest request, HttpServletResponse response) {
        // 获取 uri
        String uri = request.getRequestURI().substring(request.getContextPath().length());

//        if (ServletContext != null && uri.startsWith(ServletContext)) {
//            uri = uri.substring(ServletContext.length());
//        }

        log.info("[miniwebx] <<< uri:{}, params:{}", uri, getParamMap(request));

        int interceptorIndex = -1;
        Object handlerObject = null;
        String vmName = null;
        RuntimeException error = null;

        long time1 = System.currentTimeMillis();
        try {

            if (uri.equals("/favicon.ico")) {
                response.sendError(404);
                return;
            }

            if (uri.startsWith("/statics")) {
                renderStaticFiles(uri, response);
                return;
            }

            if(uri.equals("/")){
                uri = "/index.htm";
            }

            UriMappedHandler handler = uriHandleMappedUtil.getMappedHandler(uri);
            if (handler == null) {
                handler = uriHandleMappedUtil.getMappedRewriteHandler(uri);

            }
            if (handler != null) {
                handlerObject = handler.getHandleObject();
                request.setAttribute(SPRING_CONTROLLER_INVOKE_METHOD, handler.getHandleMethod());

            }
            // 如果没有找到 handler 直接渲染页面
            else {

                // 判断对应vm是否存在 (只针对 .htm结尾的请求)
                if (uri.endsWith(SuffixConstant.HTM_SUFFIX)) {
                    String path = SCREEN + uri.substring(0, uri.length() - SuffixConstant.HTM_SUFFIX.length());
                    String vmPath = new StringBuilder(VM_LOADER_ROOT).append(SPLIT).append(path).append(VM_EXT).toString();

                    InputStream is = ConfigLoader.loadResource(vmPath, false, false);
                    if (is != null) {
                        vmName = path;

                        // 便于在页面上直接使用请求参数
                        Enumeration<String> names = request.getParameterNames();
                        while (names.hasMoreElements()) {
                            String key = names.nextElement();
                            String[] values = request.getParameterValues(key);
                            if (values != null) {
                                if (values.length > 1) {
                                    request.setAttribute(key, values);
                                } else if (values.length == 1) {
                                    request.setAttribute(key, values[0]);
                                }
                            }
                        }
                    } else {
                        throw new ServletException(vmPath + " not exists");
                    }
                }
                // 非.htm请求 直接报错
                else {
                    throw new ServletException(new StringBuilder(uri).append(ERROR_NOT_FOUND).toString());
                }
            }

            // 执行拦截器 preHandle
            if (interceptors != null) {
                for (HandlerInterceptor interceptor : interceptors) {
                    interceptorIndex++;
                    if (!interceptor.preHandle(request, response, handlerObject)) {
                        break;
                    }
                }
            }

            // 执行目标方法
            Object result = null;
            if (handler != null) {
                result = uriHandleMappedUtil.invokeHandlerMethod(handler, handler.getHandleMethod(), handler.getHandleObject(), request, response);
            }

            // 执行拦截器 postHandle
            if (interceptors != null) {
                for (int i = interceptors.size() - 1; i >= 0; i--) {
                    HandlerInterceptor interceptor = interceptors.get(i);

                    interceptor.postHandle(request, response, result, null);
                }
            }

            // 渲染页面
            renderSuccessView(vmName, uri, result, handler, request, response, time1);

        } catch (Throwable ex) {

            while (true) {
                if (ex instanceof InvocationTargetException) {
                    ex = ((InvocationTargetException) ex).getTargetException();
                }
                if (!(ex instanceof InvocationTargetException)) {
                    break;
                }
            }

            if (ex instanceof BizException) {
                log.warn(ex.getClass().getName() + ":" + ex.getMessage(), ex);
            } else if (ex instanceof LoginTimeoutException) {
                log.warn("用户未登陆，request uri：" + uri);
            } else if (ex instanceof ServletException) {
                log.warn(ex.getClass().getName() + ":" + ex.getMessage());
            } else {
                log.error(ex.getMessage(), ex);
            }

            String errMsg = ex.getMessage();
            if (errMsg != null && errMsg.startsWith(BizException.class.getName())) {
                errMsg = errMsg.substring((BizException.class.getName() + ":").length()).trim();
                if (errMsg.indexOf(N) != -1) {
                    errMsg = errMsg.substring(0, errMsg.indexOf(N));
                }
                ex = new BizException(errMsg);
            }

            // 渲染页面
            renderExceptionView(request, response, ex, handlerObject != null, time1);

            error = new RuntimeException(ex);
        } finally {
            // 拦截器收尾
            if (interceptors != null) {
                for (int i = interceptorIndex; i >= 0; i--) {
                    HandlerInterceptor interceptor = interceptors.get(i);
                    try {
                        interceptor.afterCompletion(request, response, handlerObject, error);
                    } catch (Throwable ex2) {
                        log.error("HandlerInterceptor.afterCompletion threw exception", ex2);
                    }
                }
            }

            ThreadJsonParam.remove();
        }
    }

    private void renderSuccessView(String vmName,  String uri, Object result, UriMappedHandler handler, HttpServletRequest request, HttpServletResponse response, long startTime) throws Exception {
        // mv不为空，说明是直接访问.vm 页面，不经过 screen类
        if (vmName != null) {
            renderHtml(vmName, request, response, startTime);
            return;
        }

        // uri = uri.substring(request.getContextPath().length());

        if (handler.getIsRewrite()) {
            uri = uri.substring(0, uri.lastIndexOf(SPLIT)) + uri.substring(uri.lastIndexOf(POINT));
        }

        // 默认根路径请求
        if (uri.equals(SPLIT)) {
            if (handler.getHandleMethod().isAnnotationPresent(UserDefine.class)) {
                printAccessLog(request, null, startTime);
                return;
            }

            String path = new StringBuilder(SCREEN).append(SPLIT).append(INDEX).toString();

            renderHtml(path, request, response, startTime);
        }
        // screen 页面请求
        else if (uri.endsWith(SuffixConstant.HTM_SUFFIX)) {
            if (handler.getHandleMethod().isAnnotationPresent(UserDefine.class)) {
                printAccessLog(request, null, startTime);
                return;
            }

            String path = new StringBuilder(SCREEN).append(uri.substring(0, uri.length() - SuffixConstant.HTM_SUFFIX.length())).toString();

            renderHtml(path, request, response, startTime);
        }
        // json 格式请求
        else if (uri.endsWith(SuffixConstant.JSON_SUFFIX)) {
            if (handler.getHandleMethod().isAnnotationPresent(UserDefine.class)) {
                printAccessLog(request, null, startTime);
                return;
            }
            JsonResult jr = new JsonResult(result);
            outJson(jr, request, response, handler, startTime);
        }
    }

    private void renderExceptionView(HttpServletRequest request, HttpServletResponse response, Throwable ex, Boolean hasHandler, long startTime) {
        try {
            Integer errorCode = ERROR_BIZ_ERROR;
            // for exception
            if (ex != null) {
                if (ex instanceof javax.servlet.ServletException || ex instanceof ModelAndViewDefiningException) {
                    ex = new RuntimeException(RESOURCE_NOT_FOUND);
                    errorCode = ERROR_NOT_FOUND;
                } else if (ex instanceof BizException) {
                    errorCode = ERROR_BIZ_ERROR;
                } else if (ex instanceof LoginTimeoutException) {
                    errorCode = ERROR_NOT_LOGIN;
                } else if (ex instanceof PrivException) {
                    errorCode = ERROR_AUTH;
                } else {
                    errorCode = ERROR_BIZ_SYSTEM;
                }
            } else {
                errorCode = ERROR_BIZ_SYSTEM;
                ex = new RuntimeException("接口服务异常");
            }

            String uri = request.getRequestURI();
            uri = uri.substring(request.getContextPath().length());

            // redirect to error page
            if (uri.equals(SPLIT) || uri.endsWith(SuffixConstant.HTM_SUFFIX)) {
                if (ex instanceof LoginTimeoutException) {
                    request.setAttribute(SessionConstant.LAST_ACCESS_URL, uri);
                    response.sendRedirect(LOGIN_PAGE);
                } else {
                    request.setAttribute(ERROR_MESSAGE, ex.getMessage());
                    renderHtml(new StringBuilder(ERRORS).append(SPLIT).append(errorCode).toString(), request, response, startTime);
                }
            }
            // write error json
            else if (uri.endsWith(SuffixConstant.JSON_SUFFIX)) {
                JsonResult result = new JsonResult();
                result.setCode(errorCode);
                if (ex != null && ex.getMessage() != null && ex instanceof Exception) {
                    result.setMessage(ex.getMessage());
                }
                if (ex != null && ex instanceof BizException) {

                    result.setMessage(ex.getMessage());

                    BizException bex = (BizException) ex;
                    Object errorObject = bex.getErrorMap();
                    if (errorObject != null) {
                        result.setData(errorObject);
                    }
                }

                outJson(result, request, response, null, startTime);
            }
            // 其他请求跳404
            else {
                renderHtml(new StringBuilder(ERRORS).append(SPLIT).append(ERROR_NOT_FOUND).toString(), request, response, startTime);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void outJson(JsonResult obj, final HttpServletRequest request, HttpServletResponse response, final UriMappedHandler handler, long startTime) {
        String outMsg = BLANK;
        try {
            if (handler != null && handler.getHandleMethod().isAnnotationPresent(RequestMapping.class)) {
                Object data = obj.getData();

                if (data != null) {
                    // 方法头部加了 @ResponseBody
                    if (handler.getHandleMethod().isAnnotationPresent(ResponseBody.class)) {
                        outMsg = data.toString();
                    } else {
                        outMsg = JsonUtil.toJson4Web(data);
                    }
                } else {
                    outMsg = BLANK;
                }
            } else {
                // 方法头部加了 @ResponseBody
                if (handler != null && handler.getHandleMethod().isAnnotationPresent(ResponseBody.class)) {
                    Object data = obj.getData();
                    outMsg = data.toString();
                } else {
                    outMsg = JsonUtil.toJson4Web(obj);
                }
            }
            if (response != null) {
                response.setContentType(CONTENT_TYPE_JSON);

                if (request != null) {
                    String output = null;
                    // 变量形式
                    if (request.getParameter(JSONP_CALLBACK_VAR) != null) {
                        output = new StringBuilder(VAR).append(request.getParameter(JSONP_CALLBACK_VAR)).append(EQUALS).append(outMsg).append(END).toString();
                    }
                    // 函数调用 形式
                    else if (request.getParameter(JSONP_CALLBACK_FUNC) != null) {
                        output = new StringBuilder(request.getParameter(JSONP_CALLBACK_FUNC)).append(LEFT).append(outMsg).append(RIGHT).append(END).toString();
                    } else {
                        output = outMsg;
                    }
                    response.getWriter().write(output);
                } else {
                    response.getWriter().write(outMsg);
                }
            }

            printAccessLog(request, outMsg, startTime);

        } catch (Exception e) {
            JsonResult jr = new JsonResult();
            jr.setMessage(e.getMessage());
            jr.setCode(ERROR_BIZ_ERROR);
            String s = JsonUtil.toJson4Web(jr);
            try {
                response.getWriter().write(s);
            } catch (Exception e2) {
            }
            log.error("json输出出错：", e);

            printAccessLog(request, s, startTime);
        }
    }

    /**
     * 渲染页面
     *
     * @param vmName    views下vm文件的路径，不包含 .vm 后缀
     * @param request
     * @param response
     * @param startTime
     */
    private void renderHtml(String vmName, HttpServletRequest request, HttpServletResponse response, long startTime) {
        try {
            response.setContentType(CONTENT_TYPE_HTML);

            String vmData = vmCache.get(vmName);
            if (vmData == null) {

                String vmPath = new StringBuilder(VM_LOADER_ROOT).append(SPLIT).append(vmName).append(VM_EXT).toString();

                InputStream is = ConfigLoader.loadResource(vmPath, false, false);

                if (is == null) {
                    return;
                }
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[5000];
                int len = -1;
                while ((len = is.read(buf)) != -1) {
                    bos.write(buf, 0, len);
                }

                is.close();
                vmData = new String(bos.toByteArray(), UTF_8);
                if (VELOCITY_CACHE) {
                    vmCache.put(vmName, vmData);
                }

            }

            CharArrayWriter writer = new CharArrayWriter();

            request.setAttribute(UTIL, util);
            request.setAttribute(CONTROLL, controll);

            Velocity.evaluate(MvcVelocityHelper.convertVelocityContext(request), writer, BLANK, vmData);

            response.getOutputStream().write(writer.toString().getBytes(UTF_8));

            printAccessLog(request, null, startTime);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private Map<String, Object> getParamMap(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            params.put(name, request.getParameter(name));
        }
        return params;
    }

    public static void printAccessLog(HttpServletRequest request, String result, long startTime) {
        if (request == null) {
            return;
        }

        // 请求参数日志打印
        Enumeration<String> en = request.getParameterNames();

        String requestParam = ThreadJsonParam.get();
        if (requestParam != null) {
            requestParam = requestParam.replaceAll("[(\n\\s*)(\\s*\n)]", "");
            requestParam = CommonUtil.substring(requestParam, 300);
        } else {
            Map<String, String> param = new HashMap<String, String>();
            while (en.hasMoreElements()) {
                String key = en.nextElement();

                String[] values = request.getParameterValues(key);
                if (values.length <= 1) {
                    param.put(key, values[0]);
                } else {
                    param.put(key, JsonUtil.toJson4Web(values));
                }
            }
            requestParam = CommonUtil.substring(JsonUtil.toJson4Web(param), 300);
        }

        if (result == null) {
            result = BLANK;
        }

        long cost = System.currentTimeMillis() - startTime;

        String resultStr = CommonUtil.substring(result, 300);

        String msg = CommonUtil.format("[miniwebx] >>> uri:{}, cost:{}ms, remote:{}, param:{}, response:{}", //
                request.getRequestURI(), cost, CommonUtil.getIpAddr(request), requestParam, resultStr);

        log.info(msg);
    }

    static ClassLoader classLoader = ClassHelper.getClassLoader();

    private void renderStaticFiles(String uri, HttpServletResponse response) {

        try {
            int index = uri.lastIndexOf(".");

            String mimeType = null;
            if (index != -1) {
                String fileExt = uri.substring(index + 1);
                mimeType = MimeTypes.getMimeType(fileExt);
            } else {
                mimeType = "text/plain";
            }
            if (uri.startsWith("/")) {
                uri = uri.substring(1, uri.length());
            }

            InputStream is = classLoader.getResourceAsStream(uri);
            if (is == null) {
                response.getWriter().println("404");
                return;
            }

            response.setHeader("Cache-Control", "max-age=3600, s-maxage=31536000");
            response.setContentType(mimeType);

            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = is.read(buf)) != -1) {
                response.getOutputStream().write(buf, 0, len);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
