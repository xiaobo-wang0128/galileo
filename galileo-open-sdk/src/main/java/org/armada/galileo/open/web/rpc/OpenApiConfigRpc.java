package org.armada.galileo.open.web.rpc;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.open.dal.entity.OpenAccount;
import org.armada.galileo.open.dal.entity.OpenAppConfig;
import org.armada.galileo.open.dal.entity.OpenInterfaceConfig;
import org.armada.galileo.open.dal.entity.OpenRequestMessage;
import org.armada.galileo.open.proxy.HttpOpenApiCallbackProxy;
import org.armada.galileo.open.service.OpenApiService;
import org.armada.galileo.open.util.AsyncExecutorUtil;
import org.armada.galileo.open.vo.OpenAccountQueryVO;
import org.armada.galileo.open.vo.OpenAppConfigQueryVO;
import org.armada.galileo.open.vo.OpenInterfaceConfigQueryVO;
import org.armada.galileo.open.vo.OpenRequestMessageQueryVO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author xiaobo
 * @date 2023/2/3 18:35
 */
@Controller
public class OpenApiConfigRpc {

    private static OpenApiService openApiService;

    public static void setOpenApiService(OpenApiService openApiService) {
        OpenApiConfigRpc.openApiService = openApiService;
    }

    // open account
    public List<OpenAccount> queryOpenInterfaceConfig(OpenAccountQueryVO queryVO) {
        return openApiService.queryAccount(queryVO);
    }

    public void saveUpdateOpenAppConfig(OpenAccount account) {
        openApiService.saveUpdateAccount(account);
    }

    public void login(String loginId, String pwd) {
        openApiService.login(loginId, pwd);
    }

    // app info
    public List<OpenAppConfig> queryMyApp(OpenAppConfigQueryVO queryVO) {
        return openApiService.queryAppConfig(queryVO);
    }

    public List<OpenAppConfig> queryAllApp(OpenAppConfigQueryVO queryVO) {
        return openApiService.queryAppConfig(queryVO);
    }

    public void delAppInfo(Long id) {
        openApiService.delAppInfo(id);
    }

    public OpenAppConfig getAppDetail(Long id) {
        return openApiService.queryAppConfig(id);
    }

    public String saveUpdateApp(@RequestBody OpenAppConfig appConfig) {
        openApiService.saveUpdateOpenAppConfig(appConfig);
        return appConfig.getId();
    }

    public Long getRandomId() {
        return IdWorker.getId(null);
    }

    // interface config

    public List<OpenInterfaceConfig> queryInterface(OpenInterfaceConfigQueryVO queryVO) {
        return openApiService.queryInterfaceConfig(queryVO);
    }

    public void saveUpdateInterface(OpenInterfaceConfig interfaceConfig) {
        openApiService.saveUpdateOpenInterfaceConfig(interfaceConfig);
    }

    public OpenInterfaceConfig queryInterfaceByAppUrl(Long appId, String apiUrl) {
        return openApiService.queryInterfaceByAppUrl(appId, apiUrl);
    }

    // request message
    public List<OpenRequestMessage> queryOpenRequestMessage(OpenRequestMessageQueryVO queryVO) {
        return openApiService.queryOpenRequestMessage(queryVO);
    }

    /**
     * 请求回传重试
     *
     * @param requestId
     * @param requestJson
     */
    public void retryRequest(String requestId, String requestJson) {

        OpenRequestMessage r = openApiService.findById(requestId);
        r.setRequestJson(requestJson);

        AsyncExecutorUtil.push(new AsyncExecutorUtil.RequestTask(requestId, r.getApiUrl(), requestJson));


    }

}
