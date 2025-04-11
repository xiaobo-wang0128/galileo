package org.armada.galileo.i18n_server.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.i18n_server.dal.entity.I18nApp;
import org.armada.galileo.i18n_server.dal.entity.I18nDictionaryKey;
import org.armada.galileo.i18n_server.dal.mapper.I18nAppMapper;
import org.armada.galileo.i18n_server.dal.mapper.I18nDictionaryKeyMapper;
import org.armada.galileo.i18n_server.xunfei_api.XunFeiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2023/9/7 10:09
 */
@Component
@Slf4j
public class AutoTranslate {

    /**
     * 自动翻译
     */
    @Scheduled(fixedDelay = 300000)
    public void autoJob() {
        doJob();
    }

    @Autowired
    private I18nDictionaryKeyMapper dictionaryKeyMapper;

    @Autowired
    private I18nAppMapper i18nAppMapper;

    private AtomicBoolean status = new AtomicBoolean(false);

    public void doJob() {

        if (!status.compareAndSet(false, true)) {
            log.info("task is running");
            return;
        }

        try {

            List<I18nApp> allApps = i18nAppMapper.selectList(new QueryWrapper<>());
            Map<String, List<String>> appLocatMap = allApps.stream().collect(Collectors.toMap(e -> e.getAppCode(), e -> e.getLocales()));

            int start = 0;
            int limit = 1000;

            while (true) {

                QueryWrapper<I18nDictionaryKey> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("is_finish", "n");
                queryWrapper.last(" limit " + start + ", " + limit);

                List<I18nDictionaryKey> list = dictionaryKeyMapper.selectList(queryWrapper);

                if (CommonUtil.isNotEmpty(list)) {

                    for (I18nDictionaryKey i18nDictionaryKey : list) {

                        if (CommonUtil.isEmpty(i18nDictionaryKey.getAppCode())) {
                            continue;
                        }

                        Map<String, String> dictValueMap = i18nDictionaryKey.getDictValueMap();

                        String source = dictValueMap.get("zh");

                        if (CommonUtil.isEmpty(source)) {
                            continue;
                        }

                        List<String> lans = appLocatMap.get(i18nDictionaryKey.getAppCode());

                        boolean success = false;

                        for (String lan : lans) {
                            if ("zh".equals(lan)) {
                                continue;
                            }

                            if (CommonUtil.isNotEmpty(dictValueMap.get(lan))) {
                                continue;
                            }

                            try {
                                String dist = XunFeiUtil.doTranslate4Zh(source, lan);
                                if (CommonUtil.isNotEmpty(dist)) {
                                    dictValueMap.put(lan, dist);
                                    success = true;
                                }
                            } catch (Exception e) {
                                XunFeiUtil.putError(e.getMessage());
                                log.error(e.getMessage(), e);
                            }
                        }

                        if (success) {
                            I18nDictionaryKey update = new I18nDictionaryKey();
                            update.setId(i18nDictionaryKey.getId());
                            update.setDictValueMap(dictValueMap);
                            update.setAutoTranslate("y");


                            boolean allFinished = true;
                            if (dictValueMap.size() >= lans.size()) {
                                for (Map.Entry<String, String> entry : dictValueMap.entrySet()) {
                                    if (CommonUtil.isEmpty(entry.getValue())) {
                                        allFinished = false;
                                        break;
                                    }
                                }
                            }

                            if (allFinished) {
                                update.setIsFinish("y");
                            }

                            dictionaryKeyMapper.updateById(update);
                        }

                    }

                }


                if (list == null || list.size() < limit) {
                    break;
                }

                start += limit;

            }

        } finally {
            status.set(false);
        }


    }


}
