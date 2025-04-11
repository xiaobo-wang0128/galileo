package org.armada.galileo.open.web.rpc;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.annotation.mvc.NoToken;
import org.armada.galileo.open.dal.entity.OpenRequestMessage;
import org.armada.galileo.open.proxy.HttpOpenApiCallbackProxy;
import org.armada.galileo.open.service.OpenApiService;
import org.armada.galileo.open.util.ApiDataCache;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaobo
 * @date 2021/11/2 9:40 下午
 */
@Controller
@Slf4j
@NoToken
public class AutoDocumentRpc {

//    static final String versionFilepath = "client_api_doc.json";
//
//    static String version = "";
//
//    static {
//        try {
//            byte[] bufs = CommonUtil.readFileToBuffer(versionFilepath);
//            if (bufs == null) {
//                log.warn("file not exist: " + versionFilepath);
//            }
//            if (bufs != null) {
//                String json = new String(bufs, "utf-8");
//                Map<String, Object> map = JsonUtil.fromJson(json, new TypeToken<Map<String, Object>>() {
//                }.getType());
//                version = (String) map.get("version");
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
//    }


    public static String uriHead;


    public Map<String, Object> getAllDocs() {

        Map<String, Object> data = new HashMap<>();
        data.put("apiHead", uriHead);
        // data.put("version", version);
        data.put("list", ApiDataCache.getDocGroupList());

        return data;
    }



//    @UserDefine
//    @NoToken
//    public void view(HttpServletResponse response) throws Exception {
//        response.getOutputStream().write(generateHtml().getBytes(StandardCharsets.UTF_8));
//    }

//    private String generateHtml() throws Exception {

//        String path = "/Users/wangxiaobo/project/_nova/galileo.git/galileo-start/src/main/resources/config/sdk_doc_template.vm";
//        byte[] bufs = CommonUtil.readFileFromLocal(path);

//        byte[] bufs = CommonUtil.readFileToBuffer("sdk_doc_template.vm");
//
//        String tpl = new String(bufs, "utf-8");
//
//        Map<String, Object> data = new HashMap<>();
//        data.put("util", new CommonUtil());
//        data.put("apiHead", uriHead);
//        data.put("list", moduleApiDocsList);
//        data.put("css_type", "word");
//
//        String content = VelocityUtil.render(tpl, data);
//        return content;
//    }


//    @UserDefine
//    @NoToken
//    public void export(HttpServletResponse response) throws Exception {
//
//        String content = generateHtml();
//
//        String mimeType = "text/html";
//        // String fileName = "haqi-iwms-" + version + ".html";
//
//        response.setContentType(mimeType);
//        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
//        response.addHeader("Pargam", "no-cache");
//        response.addHeader("Cache-Control", "no-cache");
//        response.getOutputStream().write(content.getBytes(StandardCharsets.UTF_8));
//    }



//    @NoToken
//    @UserDefine
//    public void mockRequest(HttpServletRequest request, HttpServletResponse response) {
//        try {
//            if (CommonUtil.isOnline()) {
//                throw new RuntimeException("生产环境不支持");
//            }
//
//            String apiMethod = request.getHeader("X-Haiq-Api-Method");
//            DocItem doc = ApiDataCache.getByApiUrl(apiMethod);
//
//            // 客户调用 haiq， 可以直接访问 servlet
//            if ("customer_to_haiq".equals(doc.getApiType())) {
//                servlet.doExecute(true, request, response);
//            }
//            // haiq 回传客户的大部分接口是 void类型，直接用反映调用拿不到客户的回传信息，所以需要直接调用代理对象的方法执行
//            else {
//
//                String jsonContent = CommonUtil.readJsonForm(request);
//
//                int tmpIndex = apiMethod.lastIndexOf(".");
//                if (tmpIndex == -1) {
//                    throw new BizException("接口不正确");
//                }
//                String clsName = apiMethod.substring(0, tmpIndex);
//                String methodName = apiMethod.substring(tmpIndex + 1);
//
//                Object[] args = ParamUtil.transferRequestJson(clsName, methodName, jsonContent);
//
//
//
//                Class<?> cls = null;
//                Method method = null;
//                try {
//                    cls = Class.forName(clsName);
//                } catch (Exception e) {
//                    throw new BizException("无法加载class: " + clsName);
//                }
//                Method[] methods = cls.getMethods();
//                for (Method m : methods) {
//                    if (m.getName().equals(methodName)) {
//                        method = m;
//                        break;
//                    }
//                }
//                if (method == null) {
//                    throw new BizException("没有找到待执行方法: " + clsName + "." + methodName);
//                }
//
//                try {
//                    // void 方法默认为 异步调用
//                    boolean isAsync = method.getReturnType().getName().equals("void") ? true : false;
//
//                    Object obj = new HttpOpenApiCallbackProxy(OpenApiService).invokeMethod(method, args, isAsync);
//
//                    if (obj instanceof String) {
//                        response.getOutputStream().write(((String) obj).getBytes("utf-8"));
//                    } else {
//                        response.getOutputStream().write(JsonUtil.toJson(obj).getBytes("utf-8"));
//                    }
//                } catch (Throwable e) {
//                    throw new BizException(e);
//                }
//
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//
//            String msg = e.getMessage();
//            try {
//                response.getOutputStream().write((msg != null ? msg : "服务端异常").getBytes(StandardCharsets.UTF_8));
//            } catch (Exception ex) {
//                log.error(ex.getMessage(), ex);
//            }
//        }
//    }


//    /**
//     * 保存全局配置
//     */
//    public void saveConfig(@RequestBody SdkGlobalConfig newConfig) throws Exception {
//        if (newConfig.getCustomerToHaiq() == null || newConfig.getHaiqToCustomer() == null) {
//            throw new BizException("请求数据格式不正确");
//        }
//
//        OpenApiService.saveGlobalConfig(newConfig);
//    }

    /**
     * 获取全局配置
     *
     * @return
     */
//    public SdkGlobalConfig getConfig() {
//
//        SdkGlobalConfig cfg = OpenApiService.getGlobalConfig();
//        if (cfg == null) {
//            cfg = new SdkGlobalConfig();
//        }
//        if (cfg.getHaiqToCustomer() == null) {
//            cfg.setHaiqToCustomer(new SdkGlobalConfig.GlobalConfig());
//        }
//        if (cfg.getCustomerToHaiq() == null) {
//            cfg.setCustomerToHaiq(new SdkGlobalConfig.GlobalConfig());
//        }
//
//        return cfg;
//    }

    /**
     * 获取开启的url
     *
     * @return
     */
//    public Map<String, Object> getOpenUrl() {
//
//        Map<String, Object> map = new HashMap<>();
//
//        map.put("hideUnOpen", false);
//        List<String> openUrls = new ArrayList<>();
//
//        try {
//
//            SdkGlobalConfig config = OpenApiService.getGlobalConfig();
//
//            if (config != null && config.getHideUnOpen() != null && config.getHideUnOpen()) {
//
//                map.put("hideUnOpen", true);
//
//                if (config.getCustomerToHaiq().getApiInterface() != null) {
//                    openUrls.addAll(config.getCustomerToHaiq().getApiInterface());
//                }
//
//                if (config.getHaiqToCustomer().getApiInterface() != null) {
//                    openUrls.addAll(config.getHaiqToCustomer().getApiInterface());
//                }
//            }
//
//
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
//        map.put("openUrls", CommonUtil.join(openUrls, ","));
//        return map;
//    }

//
//    /**
//     * 获取适配代码信息
//     *
//     * @param apiUrl
//     * @return
//     */
//    public OpenApiTransferConfig getApiTransferConfig(String apiUrl) {
//
//        return OpenApiService.getApiTransferConfig(apiUrl);
//    }
//
//    /**
//     * 保存适配层代码
//     */
//    public void updateApiTransferConfig(@RequestBody OpenApiTransferConfig openApiTransferConfig) {
//
//        OpenApiService.updateApiTransferConfig(openApiTransferConfig);
//    }
//
//    /**
//     * 更新对接层配置
//     *
//     * @param inputConfig
//     */
//    public void saveUpdateSdkInterceptorConfig(@RequestBody OpenDeveloperConfig inputConfig) {
//        try {
//            Class<?> cls = null;
//
//            List<OpenDeveloperConfig.CompileResult> list = new ArrayList<>();
//
//            if ("http".equals(inputConfig.getCallType()) || "ftp".equals(inputConfig.getCallType()) || "db".equals(inputConfig.getCallType()) || "socket".equals(inputConfig.getCallType())) {
//
//                List<SdkCompileUtil.CompileResult> compileResultList = SdkCompileUtil.compile(inputConfig.getCodeContent());
//
//                SdkCompileUtil.CompileResult sdkCompileUtilcompileResult = compileResultList.stream().filter(e -> !e.getIsInner()).findFirst().orElse(null);
//                if (sdkCompileUtilcompileResult == null) {
//                    throw new BizException("编译失败");
//                }
//
//                cls = sdkCompileUtilcompileResult.getCls();
//
//                list = compileResultList.stream().map(e -> {
//                    OpenDeveloperConfig.CompileResult d = new OpenDeveloperConfig.CompileResult(e.getClsName(), e.getIsInner(), e.getClassBytes());
//                    return d;
//                }).collect(Collectors.toList());
//
//                inputConfig.setCompileResultList(list);
//                inputConfig.setCompileStatus("success");
//            }
//
//            if ("http".equals(inputConfig.getCallType())) {
//
//                if ("customer_to_haiq".equals(inputConfig.getApiType())) {
//
//                    if (cls.getInterfaces() == null || !cls.getInterfaces()[0].equals(RequestApiInterceptor.class)) {
//                        throw new BizException("该 java 类必须实现 " + RequestApiInterceptor.class.getName() + " 接口");
//                    }
//                    inputConfig.setInterfaceName(RequestApiInterceptor.class.getName());
//
//                    RequestApiInterceptor interceptor = (RequestApiInterceptor) applicationContext.getAutowireCapableBeanFactory().createBean(cls);
//                    SdkProxyClassCache.put(SdkProxyClassCache.ClassType.CustomerToHaiqGlobal.toString(), interceptor);
//
//                } else if ("haiq_to_customer".equals(inputConfig.getApiType())) {
//
//                    if (cls.getInterfaces() == null || !cls.getInterfaces()[0].equals(ResponseApiInterceptor.class)) {
//                        throw new BizException("该 java 类必须实现 " + ResponseApiInterceptor.class.getName() + " 接口");
//                    }
//                    inputConfig.setInterfaceName(ResponseApiInterceptor.class.getName());
//
//                    ResponseApiInterceptor interceptor = (ResponseApiInterceptor) applicationContext.getAutowireCapableBeanFactory().createBean(cls);
//                    SdkProxyClassCache.put(SdkProxyClassCache.ClassType.HaiqToCustomerGlobal.toString(), interceptor);
//
//                }
//
//            } else if ("socket".equals(inputConfig.getCallType()) || "ftp".equals(inputConfig.getCallType()) || "db".equals(inputConfig.getCallType()) || "socket".equals(inputConfig.getCallType())) {
//
//                if (cls.getInterfaces() == null || !cls.getInterfaces()[0].equals(RunnableProxy.class)) {
//                    throw new BizException("该 java 类必须实现 " + RunnableProxy.class.getName() + " 接口");
//                }
//                inputConfig.setInterfaceName(Runnable.class.getName());
//
//                RunnableProxy runnable = (RunnableProxy) applicationContext.getAutowireCapableBeanFactory().createBean(cls);
//
//                SdkProxyClassCache.startMainThread(inputConfig.getApiType(), runnable);
//
//            } else if ("sdk".equals(inputConfig.getCallType())) {
//
//                AssertUtil.isNotNull(inputConfig.getAppId(), "appId");
//                AssertUtil.isNotNull(inputConfig.getAppSecret(), "appSecret");
//                AssertUtil.isNotNull(inputConfig.getCallbackAddress(), "appAddress");
//
//            } else {
//                throw new BizException("类型不允许");
//            }
//
//
//
//            OpenApiService.saveUpdateSdkInterceptorConfig(inputConfig);
//
//            SdkGlobalConfig sdkGlobalConfig = OpenApiService.getGlobalConfig();
//            if (sdkGlobalConfig == null) {
//                sdkGlobalConfig = new SdkGlobalConfig();
//            }
//            if ("customer_to_haiq".equals(inputConfig.getApiType())) {
//                sdkGlobalConfig.getCustomerToHaiq().setCallType(inputConfig.getCallType());
//            } else {
//                sdkGlobalConfig.getHaiqToCustomer().setCallType(inputConfig.getCallType());
//            }
//            OpenApiService.saveGlobalConfig(sdkGlobalConfig);
//
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            throw new BizException("编译出错：" + e.getMessage());
//        }
//    }
//
//    public List<OpenDeveloperConfig> getSdkInterceptorConfig() {
//
//
//        OpenDeveloperConfig customer_to_haiq = OpenApiService.getSdkInterceptorConfigByType("customer_to_haiq");
//        if (customer_to_haiq != null) {
//            customer_to_haiq.setCompileResultList(null);
//        } else {
//            customer_to_haiq = new OpenDeveloperConfig("customer_to_haiq");
//        }
//
//        OpenDeveloperConfig haiq_to_customer = OpenApiService.getSdkInterceptorConfigByType("haiq_to_customer");
//        if (haiq_to_customer != null) {
//            haiq_to_customer.setCompileResultList(null);
//        } else {
//            haiq_to_customer = new OpenDeveloperConfig("haiq_to_customer");
//        }
//
//        return CommonUtil.asList(customer_to_haiq, haiq_to_customer);
//    }

    /**
     * 回传测试
     *
     * @param request
     * @param response
     */
    public void print(HttpServletRequest request, HttpServletResponse response) {
        String json = CommonUtil.readJsonForm(request);
        try {
            if (json != null) {
                response.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
            } else {
                response.getOutputStream().write("ok".getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

//
//    /**
//     * 接口日志查询
//     *
//     * @param vo
//     * @return
//     */
//    public List<OpenRequestMessage> queryRequestMsg(OpenRequestMessageQueryVO vo, HttpServletResponse response) throws Exception {
//
//
//
//        // log.info("query param: \n " + JsonUtil.toJsonPretty(vo));
//
//        List<OpenRequestMessage> pr = OpenApiService.queryRequestMsg(vo);
//
//        return pr;
//    }
//
//

//
//    public void batchRetryRequest(String ids) {
//        if (CommonUtil.isEmpty(ids)) {
//            throw new BizException("输入id不能为空");
//        }
//        String[] requestIds = ids.split(",");
//
//
//        HttpOpenApiCallbackProxy executor = new HttpOpenApiCallbackProxy(OpenApiService);
//
//        for (String requestId : requestIds) {
//            if (CommonUtil.isEmpty(requestId)) {
//                continue;
//            }
//            OpenRequestMessage r = OpenApiService.findById(requestId);
//            if (r == null) {
//                throw new BizException("记录不存在: " + requestId);
//            }
//            try {
//                executor.retry(CommonUtil.asList(r), false, false);
//            } catch (Exception e) {
//                log.error(e.getMessage(), e);
//            }
//        }
//    }


}
