package org.armada.galileo.rainbow_gate.transfer.gate_point.app_server;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.armada.galileo.annotation.openapi.OpenApiMethod;
import org.armada.galileo.annotation.rainbow.RainbowService;
import org.armada.galileo.common.loader.SpringMvcUtil;
import org.armada.galileo.common.lock.ConcurrentLock;
import org.armada.galileo.common.util.AssertUtil;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.rainbow_gate.transfer.domain.protocol.AppRequestDomain;
import org.armada.galileo.rainbow_gate.transfer.domain.protocol.AppResponseDomain;
import org.armada.galileo.rainbow_gate.transfer.domain.protocol.RainbowRequestType;
import org.armada.galileo.rainbow_gate.transfer.gate_codec.GateCodecUtil;
import org.armada.galileo.rainbow_gate.transfer.gate_point.app_client.AsyncRequestCache;
import org.armada.galileo.rainbow_gate.transfer.interceptor.RainbowInterceptor;
import org.armada.galileo.rainbow_gate.transfer.util.RainbowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

public class AppServer extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public AppServer() {
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getOutputStream().write("not support".getBytes());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        call(request, response);
    }

    private static Logger log = LoggerFactory.getLogger(AppServer.class);

    /**
     * 拦截器
     */
    private static org.armada.galileo.rainbow_gate.transfer.interceptor.RainbowInterceptor rainbowInterceptor = null;


    private static ConcurrentLock concurrentLock;

    public static void setConcurrentLock(ConcurrentLock concurrentLock) {
        AppServer.concurrentLock = concurrentLock;
    }


    private static final long DefaultCacheTime = CommonUtil.millDay / 1000;

    public void call(HttpServletRequest request, HttpServletResponse response) {

        long l1 = System.currentTimeMillis();
        OutputStream os = null;

        AppRequestDomain appRequestDomain = null;
        AppResponseDomain appResponseDomain = new AppResponseDomain();


        try {
            os = response.getOutputStream();

            byte[] requestData = parseRequestData(request);

            appRequestDomain = GateCodecUtil.decodeRequest(requestData);


            // ----------- 执行业务方法 -----------
            if (appRequestDomain.getClassName() == null) {
                throw new RainbowException("待执行的业务ClassName不能为空");
            }

            AssertUtil.isNotNull(appRequestDomain, "appRequestDomain");
            AssertUtil.isNotNull(appRequestDomain.getClassName(), "待执行的业务 className");
            AssertUtil.isNotNull(appRequestDomain.getMethodName(), "待执行的业务 methodName");

            String className = appRequestDomain.getClassName();
            String methodName = appRequestDomain.getMethodName();
            String[] paramTypeNames = appRequestDomain.getParamTypeNames();
            Object[] paramInputs = appRequestDomain.getParamInputs();

            Class<?> cls = getSimpleCLass(className); //   Class.forName(className);
            Class<?>[] inputTypes = null;
            if (paramTypeNames != null) {
                inputTypes = new Class<?>[paramTypeNames.length];
                for (int i = 0; i < paramTypeNames.length; i++) {
                    inputTypes[i] = getSimpleCLass(paramTypeNames[i]); //   Class.forName(paramTypeNames[i]);
                }
            }

            java.lang.reflect.Method method = null;
            if (inputTypes != null && inputTypes.length > 0) {
                method = ReflectionUtils.findMethod(cls, methodName, inputTypes);
            } else {
                method = ReflectionUtils.findMethod(cls, methodName);
            }
            if (method == null) {
                throw new RuntimeException("没到找待执行方法: " + methodName);
            }

            // 路由转发类请求
            if (RainbowRequestType.Group_Route.equals(appRequestDomain.getRequestType())) {
                if (!method.isAnnotationPresent(OpenApiMethod.class)) {
                    throw new RainbowException("路由转发送请求必须是 OpenApiMethod 型接口");
                }
                OpenApiMethod openApiMethod = method.getAnnotation(OpenApiMethod.class);
                String apiCode = openApiMethod.code();
                appRequestDomain.setRequestType(RainbowRequestType.Request);
                AsyncRequestCache.push(appRequestDomain, apiCode);

                appResponseDomain.setCode(0);
            }
            // 直接调用请求
            else {

                boolean idempotent = false;

                // 幂等性处理，只针对异步接口
                if (method.isAnnotationPresent(OpenApiMethod.class) && concurrentLock != null) {
                    idempotent = true;
                }

                if (idempotent) {

                    String cacheKey = appRequestDomain.getRequestNo();

                    String status = concurrentLock.getValue(cacheKey);

                    if (status != null) {
                        if ("executing".equals(status)) {
                            throw new RainbowException("request is executing, requestNo: " + cacheKey);
                        }
                        if ("done".equals(status)) {
                            appResponseDomain.setCode(0);
                            log.info("[app-server] 幂等校验通过，直接返回执行结果, ip:{}, class:{}, method:{}", request.getRemoteHost(), appRequestDomain.getClassName(), methodName);
                        }
                    } else {
                        boolean lock = concurrentLock.lock(cacheKey, "executing");
                        if (!lock) {
                            throw new RainbowException("请求正在执行中, requestNo: " + cacheKey);
                        }
                        try {
                            executeMethod(appRequestDomain, appResponseDomain, cls, method, paramInputs);

                            concurrentLock.cancelExpire(cacheKey);

                            concurrentLock.setValue(cacheKey, "done", DefaultCacheTime);

                        } catch (Exception e) {

                            concurrentLock.unlock(cacheKey);

                            throw e;

                        }
                    }

                } else {
                    executeMethod(appRequestDomain, appResponseDomain, cls, method, paramInputs);
                }

            }

        } catch (Exception e) {

            Throwable t = e;
            while (true) {
                if (t.getCause() != null && t.getCause() instanceof InvocationTargetException) {
                    t = ((InvocationTargetException) t.getCause()).getTargetException();
                    continue;
                }
                break;
            }
            log.error(t.getMessage(), t);

            String msg = t.getMessage();

            // ----------- 输出结果 -----------
            appResponseDomain.setCode(500);
            if (msg != null && !msg.matches("\\s*")) {
                appResponseDomain.setMessage("服务端执行异常: " + msg);
            } else {
                appResponseDomain.setMessage("服务端执行异常: " + e.getClass().getName());
            }

        } finally {

            long l2 = System.currentTimeMillis();

            appResponseDomain.setCostTime((int) (System.currentTimeMillis() - l1));

            if (os != null) {

                try {
                    byte[] out = GateCodecUtil.encodeResponse(appResponseDomain);
                    os.write(out);

                } catch (Exception e2) {
                    log.error(e2.getMessage());
                }
                try {
                    os.flush();
                    os.close();
                } catch (Exception e2) {
                    log.error(e2.getMessage(), e2);
                }
            }

            if (log.isDebugEnabled()) {
                if (appRequestDomain != null) {
                    log.debug("[app-server] execute done, class: {}, method: {}, cost: {}ms ", appRequestDomain.getClassName(), appRequestDomain.getMethodName(), (l2 - l1));
                } else {
                    log.debug("[app-server] cost: {}ms ", (l2 - l1));
                }
            }
        }
    }


    private static void executeMethod(AppRequestDomain appRequestDomain, AppResponseDomain appResponseDomain, Class cls, Method method, Object[] paramInputs) {

        Object targetObject = SpringMvcUtil.getBean(cls);

        if (targetObject == null) {
            throw new RainbowException("没有找到接口实现类");
        }

        //if (!targetObject.getClass().isAnnotationPresent(RainbowService.class)) {
        //    throw new RainbowException("该接口不是对外接口，无法调用");
        //}

        if (rainbowInterceptor != null) {
            rainbowInterceptor.before(appRequestDomain);
        }

        Object result = null;

        //if (log.isDebugEnabled()) {
        //    log.debug("[app-server] receive request from ip:{}, class:{}, method:{}", request.getRemoteHost(), appRequestDomain.getClassName(), methodName);
        //}

        // 执行目标方法
        if (paramInputs != null && paramInputs.length > 0) {
            result = ReflectionUtils.invokeMethod(method, targetObject, paramInputs);
        } else {
            result = ReflectionUtils.invokeMethod(method, targetObject);
        }

        // ----------- 输出结果 -----------
        appResponseDomain.setCode(0);
        appResponseDomain.setResult(result);

        if (rainbowInterceptor != null) {
            rainbowInterceptor.complete(appRequestDomain, appResponseDomain);
        }

    }


    public static Class<?> getSimpleCLass(String className) throws ClassNotFoundException {
        // 小写字母开头的基础类型
        if (className.indexOf(".") == -1) {

            if ("int".equals(className)) {
                return Integer.class;
            } else if ("long".equals(className)) {
                return Long.class;
            } else if ("char".equals(className)) {
                return Character.class;
            } else if ("float".equals(className)) {
                return Float.class;
            } else if ("double".equals(className)) {
                return Double.class;
            } else if ("byte".equals(className)) {
                return Byte.class;
            } else if ("short".equals(className)) {
                return Short.class;
            }
            throw new ClassNotFoundException(className);
        } else {
            return Class.forName(className);
        }
    }

    public static byte[] parseRequestData(HttpServletRequest request) throws Exception {

        String encoding = "utf-8";
        request.setCharacterEncoding(encoding);

        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload sevletFileUpload = new ServletFileUpload(factory);
        // 最大500M
        sevletFileUpload.setSizeMax(500 * 1024 * 1024);

        @SuppressWarnings("unchecked") List<FileItem> fileItems = (List<FileItem>) sevletFileUpload.parseRequest(request);

        // 依次处理每个上传的文件
        byte[] data = null;

        for (Iterator<FileItem> it = fileItems.iterator(); it.hasNext(); ) {

            final FileItem item = (FileItem) it.next();

            if (!item.isFormField()) {
                java.io.InputStream is = item.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                int len = -1;
                byte[] buf = new byte[10240];
                while ((len = is.read(buf)) != -1) {
                    bos.write(buf, 0, len);
                }
                is.close();
                bos.flush();
                bos.close();
                data = bos.toByteArray();
            }
        }

        return data;
    }


    public static void setProviderInterceptor(RainbowInterceptor rainbowInterceptor) {
        AppServer.rainbowInterceptor = rainbowInterceptor;
    }
}