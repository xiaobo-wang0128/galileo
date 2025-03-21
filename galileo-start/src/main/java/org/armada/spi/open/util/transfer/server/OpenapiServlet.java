package org.armada.spi.open.util.transfer.server;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.armada.spi.open.util.*;
import org.armada.spi.open.util.transfer.util.CompressUtil22;
import org.armada.spi.open.util.transfer.util.HttpParserUtil22;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

/**
 *
 */
public class OpenapiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * 密钥生成器
     */
    private ServerAppSecretGetter serverAppSecretGetter;

    /**
     * 接口日志监听
     */
    private ApiLogListener apiLogListener;

    private Map<String, Object> targetObjectMap = null;

    /**
     * proxy 构造方法
     *
     * @param serverAppSecretGetter 密钥生成器
     */
    public OpenapiServlet(List<Object> targetObjects, ServerAppSecretGetter serverAppSecretGetter, ApiLogListener apiLogListener) {
        this.serverAppSecretGetter = serverAppSecretGetter;
        this.apiLogListener = apiLogListener;
        this.targetObjectMap = new ConcurrentHashMap<>();
        for (Object obj : targetObjects) {
            if (obj != null) {
                Class<?>[] interfaces = obj.getClass().getInterfaces();
                if (interfaces == null || interfaces.length == 0) {
                    throw new RuntimeException("执行类必须实现一个接口");
                }
                for (Class clz : interfaces) {
                    targetObjectMap.put(clz.getName(), obj);
                }
            }
        }
    }

    /**
     * 服务端sevlet构造
     *
     * @param serverAppSecretGetter 密钥生成器
     */
    public OpenapiServlet(ServerAppSecretGetter serverAppSecretGetter) {
        this.serverAppSecretGetter = serverAppSecretGetter;
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getOutputStream().write("not support".getBytes());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        call(request, response);
    }

    private static Logger log = LoggerFactory.getLogger(OpenapiServlet.class);

    /**
     * 最大并发线程数
     */
    private static Integer maxThreads = Runtime.getRuntime().availableProcessors() + 1;

    /**
     * 请求计数器
     */
    private static AtomicInteger concurrentThread = new AtomicInteger(0);

    private String getAppSecret(String appId) {
        return this.serverAppSecretGetter.getAppSecret(appId);
    }

    public void call(HttpServletRequest request, HttpServletResponse response) {

        long l1 = System.currentTimeMillis();
        OutputStream os = null;

        HaiqRequestDomain requestDomain = null;
        HaiqActionResult responseResult = null;
        Exception ex = null;
        int costTime = 0;
        int requestByteSize = 0;
        int responseByteSize = 0;
        Date happenTime = new Date();

        try {
            os = response.getOutputStream();

            concurrentThread.incrementAndGet();

            HaiqSdkPostParam httpPostParam = HttpParserUtil22.parseRequestData(request);
            String appId = httpPostParam.getAppId();
            String sign = httpPostParam.getSign();
            String dc = httpPostParam.getDc();
            byte[] bufs = httpPostParam.getBufs();

            HqiqAssert.assertIsNull(appId, "appId");
            HqiqAssert.assertIsNull(sign, "sign");
            HqiqAssert.assertIsNull(dc, "dc");

            String appSecret = getAppSecret(appId);
            if (appSecret == null) {
                throw new RuntimeException("appId 对应的密钥信息不存在：" + appId);
            }
            // AppTimeMonitor.check("start");

            // ----------- 校验签名 -----------
            String checkSign = HaiqSdkUtil.getByateArraysSHA256(appSecret.getBytes("utf-8"), bufs, dc.getBytes("utf-8"));
            if (!checkSign.equals(sign)) {
                throw new RuntimeException("接口签名不正确");
            }
            // AppTimeMonitor.check("校验签名");

            // ----------- 解压 -----------
            requestByteSize = bufs.length;
            bufs = CompressUtil22.uncompress(bufs);
            // AppTimeMonitor.check("解压");

            // ----------- 反序列化 -----------
            requestDomain = HaiqSdkUtil.deserialize(bufs, HaiqRequestDomain.class);
            // AppTimeMonitor.check("反序列化");

            // ----------- 执行业务方法 -----------
            if (requestDomain.getClassName() == null) {
                throw new HaiqApiException("待执行的业务ClassName不能为空");
            }
            Object targetObject = targetObjectMap.get(requestDomain.getClassName());

            if (targetObject == null) {
                throw new RuntimeException("没到找待执行对象: " + requestDomain.getClassName());
            }
            Class<?> cls = targetObject.getClass(); //Class.forName(requestDomain.getClassName());

            // 方法入参类型
            Class<?>[] inputTypes = null;
            if (requestDomain.getParamTypeNames() != null) {
                inputTypes = new Class<?>[requestDomain.getParamTypeNames().length];
                for (int i = 0; i < requestDomain.getParamTypeNames().length; i++) {
                    inputTypes[i] = Class.forName(requestDomain.getParamTypeNames()[i]);
                }
            }
            String methodName = requestDomain.getMethodName();
            java.lang.reflect.Method method = null;
            if (inputTypes != null && inputTypes.length > 0) {
                method = ReflectionUtils.findMethod(cls, methodName, inputTypes);
            } else {
                method = ReflectionUtils.findMethod(cls, methodName);
            }
            if (method == null) {
                throw new RuntimeException("没到找待执行方法: " + methodName);
            }

            // 方法入参数据
            Object[] param = requestDomain.getParamInputs();

            Object result = null;

            // 执行目标方法
            if (param != null && param.length > 0) {
                result = ReflectionUtils.invokeMethod(method, targetObject, param);
            } else {
                result = ReflectionUtils.invokeMethod(method, targetObject);
            }
            // AppTimeMonitor.check("执行业务方法");

            // ----------- 输出结果 -----------
            responseResult = new HaiqActionResult();
            responseResult.setData(result);

            byte[] out = HaiqSdkUtil.serialize(responseResult);
            // AppTimeMonitor.check("输出结果 serial");
            // out = CompressUtil22.compress(out);

            os.write(out);
            responseByteSize = out.length;

        } catch (Exception e) {

            ex = e;

            int errorCode = 501;
            if (e instanceof HaiqApiException) {
                errorCode = 500;
            }

            String msg = e.getMessage();
            responseResult = new HaiqActionResult();

            responseResult.setCode(errorCode);
            if (msg != null && !msg.matches("\\s*")) {
                responseResult.setMessage(msg);
            } else {
                responseResult.setMessage("服务调用异常");
            }
            byte[] out = HaiqSdkUtil.serialize(responseResult);
            try {
                os.write(out);
            } catch (Exception e2) {
            }

            responseByteSize = out.length;

            log.error(e.getMessage(), e);
        } finally {
            concurrentThread.decrementAndGet();
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (Exception e2) {
                    log.error(e2.getMessage(), e2);
                }
            }

            long l2 = System.currentTimeMillis();

            if (requestDomain != null) {
                log.info("[openapi] class: {}, method: {}, cost: {}ms ", requestDomain.getClassName(), requestDomain.getMethodName(), (l2 - l1));
            } else {
                log.info("[openapi] cost: {}ms ", (l2 - l1));
            }

            costTime = new Long(l2 - l1).intValue();

            if (apiLogListener != null) {
                try{
                    apiLogListener.afterRequest(
                            requestDomain,
                            responseResult,
                            happenTime,
                            costTime,
                            requestByteSize,
                            responseByteSize,
                            ex);
                }
                catch(Exception e){
                    log.error(e.getMessage(), e);
                }
            }

        }
    }

}
