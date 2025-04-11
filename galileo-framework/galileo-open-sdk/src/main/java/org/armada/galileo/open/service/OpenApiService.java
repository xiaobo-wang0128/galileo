package org.armada.galileo.open.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.armada.galileo.common.page.PageParam;
import org.armada.galileo.common.page.ThreadPagingUtil;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.es.query.EsQueryWrapper;
import org.armada.galileo.open.cache.OpenApiCacheUtil;
import org.armada.galileo.open.dal.entity.OpenAccount;
import org.armada.galileo.open.dal.entity.OpenAppConfig;
import org.armada.galileo.open.dal.entity.OpenInterfaceConfig;
import org.armada.galileo.open.dal.entity.OpenRequestMessage;
import org.armada.galileo.open.dal.es_mapper.OpenRequestMessageMapper;
import org.armada.galileo.open.dal.es_mapper.impl.OpenRequestMessageMapperImpl;
import org.armada.galileo.open.dal.mapper.OpenAccountMapper;
import org.armada.galileo.open.dal.mapper.OpenInterfaceConfigMapper;
import org.armada.galileo.open.dal.mapper.OpenAppConfigMapper;
import org.armada.galileo.open.vo.OpenAccountQueryVO;
import org.armada.galileo.open.vo.OpenAppConfigQueryVO;
import org.armada.galileo.open.vo.OpenInterfaceConfigQueryVO;
import org.armada.galileo.open.vo.OpenRequestMessageQueryVO;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheType;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @description: TODO
 * @date 2023/2/3 17:50
 */
public class OpenApiService {

    @Autowired
    private OpenAccountMapper accountMapper;

    @Autowired
    private OpenInterfaceConfigMapper interfaceConfigMapper;

    @Autowired
    private OpenAppConfigMapper appConfigMapper;

    @Autowired
    private OpenRequestMessageMapper requestMessageMapper ;

    @PostConstruct
    public void initCache() {
        OpenAppConfigQueryVO queryVO = new OpenAppConfigQueryVO();
        List<OpenAppConfig> appConfigs = queryAppConfig(queryVO);
        for (OpenAppConfig appConfig : appConfigs) {
            OpenApiCacheUtil.putOpenAppConfig(appConfig);
        }
    }

    // open account
    public List<OpenAccount> queryAccount(OpenAccountQueryVO queryVO) {
        QueryWrapper<OpenAccount> queryWrapper = new QueryWrapper<>();
        return accountMapper.selectList(queryWrapper);
    }


    public void saveUpdateAccount(OpenAccount account) {
        if (account.getId() == null) {
            accountMapper.insert(account);
        } else {
            accountMapper.updateById(account);
        }
    }

    public void login(String loginId, String pwd) {
    }

    // app info

    public List<OpenAppConfig> queryAppConfig(OpenAppConfigQueryVO queryVO) {
        QueryWrapper<OpenAppConfig> queryWrapper = new QueryWrapper<>();
        return appConfigMapper.selectList(queryWrapper);
    }

    public OpenAppConfig queryAppConfig(Long id) {
        return appConfigMapper.selectById(id);
    }

    public void saveUpdateOpenAppConfig(OpenAppConfig appConfig) {
        if (appConfig.getId() == null) {
            appConfig.setGmtCreate(System.currentTimeMillis());
            appConfig.setGmtModify(System.currentTimeMillis());
            appConfig.setCreator("system");
            appConfig.setIsDelete("N");
            appConfigMapper.insert(appConfig);
        } else {
            appConfigMapper.updateById(appConfig);
        }
        OpenApiCacheUtil.putOpenAppConfig(appConfig);
    }

    // interface config

    public List<OpenInterfaceConfig> queryInterfaceConfig(OpenInterfaceConfigQueryVO queryVO) {
        QueryWrapper<OpenInterfaceConfig> queryWrapper = new QueryWrapper<>();
        return interfaceConfigMapper.selectList(queryWrapper);
    }

    public void saveUpdateOpenInterfaceConfig(OpenInterfaceConfig account) {
        if (account.getId() == null) {
            interfaceConfigMapper.insert(account);
        } else {
            interfaceConfigMapper.updateById(account);
        }
    }

    public OpenInterfaceConfig queryInterfaceByAppUrl(Long appId, String apiUrl) {
        OpenInterfaceConfigQueryVO queryVO = new OpenInterfaceConfigQueryVO();
        queryVO.setApiUrl(apiUrl);
        queryVO.setAppId(appId);
        List<OpenInterfaceConfig> list = queryInterfaceConfig(queryVO);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    // request message

    public List<OpenRequestMessage> queryOpenRequestMessage(OpenRequestMessageQueryVO vo) {

        EsQueryWrapper<OpenRequestMessage> query = new EsQueryWrapper<>();

        if (vo != null) {
            if (CommonUtil.isNotEmpty(vo.getApiUrl())) {
                query.eq("apiUrl", vo.getApiUrl());
            }
            if(vo.getApiFrom()!=null){
                query.eq("apiFrom", vo.getApiFrom());
            }
            if(vo.getApiTo()!=null){
                query.eq("apiTo", vo.getApiTo());
            }
            if (CommonUtil.isNotEmpty(vo.getStatus())) {
                query.eq("status", vo.getStatus());
            }
            if (vo.getHappenTime1() != null) {
                query.gt("happenTime", vo.getHappenTime1());
            }
            if (vo.getHappenTime2() != null) {
                query.lt("happenTime", vo.getHappenTime2() + CommonUtil.millDay - 1);
            }
            if (CommonUtil.isNotEmpty(vo.getKeyword())) {
                query.like("requestJson", vo.getKeyword().trim());
            }
            if (vo.getMaxRetryTime() != null) {
                query.lt("retryTime", vo.getMaxRetryTime());
            }
            if (CommonUtil.isNotEmpty(vo.getIsAsync())) {
                query.lt("isAsync", vo.getIsAsync());
            }

            if (vo.getLastUpdateTime() != null) {
                query.lt("updateTime", vo.getLastUpdateTime());
            }
            if (CommonUtil.isNotEmpty(vo.getGroups())) {
                query.in("msgGroup", vo.getGroups());
            }
        }

        query.sort("happenTime", SortOrder.DESC);

        PageParam pageParam = ThreadPagingUtil.get();
        if (pageParam == null) {
            pageParam = PageParam.instanceByPageIndex(1, 100);
            ThreadPagingUtil.set(pageParam);
        }
        return requestMessageMapper.selectList(query);
    }

    public void saveUpdateOpenRequestMessage(OpenRequestMessage requestMessage) {
        requestMessageMapper.save(requestMessage);
    }

    public OpenRequestMessage findById(String requestId) {
        return requestMessageMapper.selectById(requestId);
    }

    public void update(OpenRequestMessage rm) {
        requestMessageMapper.save(rm);
    }

    public long getRetrySleepTime() {
        return 30L;
    }

    public Integer getMaxRetryTime() {
        return 100;
    }

    public void insert(OpenRequestMessage msg) {
        requestMessageMapper.save(msg);
    }

    public void delAppInfo(Long id) {
        appConfigMapper.deleteById(id);
    }

}
