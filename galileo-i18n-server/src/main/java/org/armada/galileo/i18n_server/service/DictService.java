package org.armada.galileo.i18n_server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.armada.galileo.common.page.PageList;
import org.armada.galileo.common.page.ThreadPagingUtil;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.i18n_server.dal.entity.I18nApp;
import org.armada.galileo.i18n_server.dal.entity.I18nDictionaryKey;
import org.armada.galileo.i18n_server.dal.mapper.I18nAppMapper;
import org.armada.galileo.i18n_server.dal.mapper.I18nDictionaryKeyMapper;
import org.armada.galileo.i18n_server.dal.vo.DictQueryVO;
import org.armada.galileo.i18n_server.dal.vo.DictVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2022/12/26 16:20
 */
@Service
public class DictService {

    @Autowired
    private I18nDictionaryKeyMapper dictKeyMapper;

    @Autowired
    private I18nAppMapper appMapper;


    public void saveOrUpdate(DictVO dto, int languageSize) {

        // 去掉非法的值
        if (dto.getDictValues() != null && dto.getDictValues().size() > 0) {
            Map<String, String> newMap = dto.getDictValues().entrySet().stream()
                    .filter(e -> CommonUtil.isNotEmpty(e.getValue()) && !e.getValue().trim().equals("null"))
                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

            dto.setDictValues(newMap);
        }


        I18nDictionaryKey key = new I18nDictionaryKey();
        key.setAppId(dto.getAppId());
        key.setId(dto.getDictId());
        key.setAppCode(dto.getAppCode());
        key.setDictValueMap(dto.getDictValues());
        key.setDictionaryKey(dto.getDictKey());
        key.setIsFinish(dto.getIsFinish());
        key.setSort(dto.getSort());
        key.setByScan(dto.getByScan());
        key.setKeyGroup(dto.getGroup());

        int actual = key.getDictValueMap().entrySet().stream()
                .filter(e -> CommonUtil.isNotEmpty(e.getValue()))
                .collect(Collectors.toList()).size();

        if (actual < languageSize) {
            key.setIsFinish("n");
        } else {
            key.setIsFinish("y");
        }

        if (key.getId() == null) {
            dictKeyMapper.insert(key);
        } else {
            dictKeyMapper.updateById(key);
        }

        dto.setDictId(key.getId());
    }

    public void removeById(Long id) {
        dictKeyMapper.deleteById(id);
    }

    public List<DictVO> query(DictQueryVO queryVO) {

        QueryWrapper<I18nDictionaryKey> queryWrapper = new QueryWrapper<>();
        if (queryVO.getAppId() != null) {
            queryWrapper.eq("app_id", queryVO.getAppId());
        }
        if (!"_all_".equals(queryVO.getAppCode()) && CommonUtil.isNotEmpty(queryVO.getAppCode())) {
            queryWrapper.eq("app_code", queryVO.getAppCode());
        }
        if (CommonUtil.isNotEmpty(queryVO.getKeyword())) {

            String keyword = queryVO.getKeyword().trim();

            queryWrapper.and(new Consumer<QueryWrapper<I18nDictionaryKey>>() {
                @Override
                public void accept(QueryWrapper<I18nDictionaryKey> qw) {
                    qw.like("dictionary_key", keyword).or().like("dict_value_map", keyword);
                }
            });

        }
        if (CommonUtil.isNotEmpty(queryVO.getIsFinish())) {
            queryWrapper.eq("is_finish", queryVO.getIsFinish());
        }

        queryWrapper.orderByAsc("app_code", "dictionary_key");

        if (queryVO.isPage()) {
            ThreadPagingUtil.turnOn();

            PageList<I18nDictionaryKey> list = (PageList<I18nDictionaryKey>) dictKeyMapper.selectList(queryWrapper);
            PageList<DictVO> volist = new PageList<>();
            for (I18nDictionaryKey key : list) {
                DictVO vo = new DictVO();
                vo.setDictId(key.getId())
                        .setDictKey(key.getDictionaryKey())
                        .setAppCode(key.getAppCode())
                        .setSort(key.getSort())
                        .setDictValues(key.getDictValueMap());

                if (vo.getDictValues() == null) {
                    vo.setDictValues(new HashMap<>());
                }
                volist.add(vo);
            }

            volist.setPageIndex(list.getPageIndex());
            volist.setTotalSize(list.getTotalSize());
            volist.setTotalPage(list.getTotalPage());
            volist.setPageSize(list.getPageSize());

            return volist;
        } else {
            List<I18nDictionaryKey> list = dictKeyMapper.selectList(queryWrapper);
            List<DictVO> volist = new ArrayList<>();
            for (I18nDictionaryKey key : list) {
                DictVO vo = new DictVO();
                vo.setDictId(key.getId())
                        .setDictKey(key.getDictionaryKey())
                        .setAppCode(key.getAppCode())
                        .setDictValues(key.getDictValueMap());

                if (vo.getDictValues() == null) {
                    vo.setDictValues(new HashMap<>());
                }
                volist.add(vo);
            }
            return volist;
        }
    }

    public List<I18nApp> selectByAppCodes(String appCode) {
        QueryWrapper<I18nApp> query = new QueryWrapper<>();

        Map<String, Integer> sort = new HashMap<>();
        if (!"_all_".equals(appCode)) {
            String[] tmps = appCode.split(",");
            int i = 0;
            for (String tmp : tmps) {
                sort.put(tmp, i++);
            }
            query.in("app_code", CommonUtil.asList(tmps));
        }

        List<I18nApp> list = appMapper.selectList(query);
        if (list.size() > 0) {
            if (sort.size() > 0) {
                Collections.sort(list, Comparator.comparing(ee -> sort.get(ee.getAppCode())));
            }
            return list;
        }
        return null;
    }


    public I18nApp selectByAppCode(String appCode) {
        QueryWrapper<I18nApp> query = new QueryWrapper<>();

        if (appCode.indexOf(",") != -1) {
            String[] tmps = appCode.split(",");
            query.in("app_code", CommonUtil.asList(tmps));
        } else {
            query.eq("app_code", appCode);
        }

        List<I18nApp> list = appMapper.selectList(query);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;

    }

    public DictVO selectByAppAndDictKey(Integer appId, String dictKey) {

        QueryWrapper<I18nDictionaryKey> query = new QueryWrapper<>();
        query.eq("app_id", appId);
        query.eq("dictionary_key", dictKey);

        List<I18nDictionaryKey> list = dictKeyMapper.selectList(query);
        if (list.size() > 0) {
            I18nDictionaryKey key = list.get(0);
            DictVO vo = new DictVO();
            vo.setDictId(key.getId())
                    .setAppId(key.getAppId())
                    .setDictKey(key.getDictionaryKey())
                    .setAppCode(key.getAppCode())
                    .setSort(key.getSort())
                    .setDictValues(key.getDictValueMap());

            return vo;
        }
        return null;

    }


    public DictVO autoCreateDictKey(I18nApp app, String dictKey, Map<String, String> defaultValues, int sort, String group) {

        Integer appId = app.getId();
        String appCode = app.getAppCode();
        int languageSize = app.getLocales().size();

        DictVO vo = selectByAppAndDictKey(appId, dictKey);
        if (vo != null) {
            if (defaultValues != null && defaultValues.size() > 0) {

                Map<String, String> srcValues = vo.getDictValues();
                if (srcValues == null) {
                    srcValues = new HashMap<>();
                }

                for (Map.Entry<String, String> entry : defaultValues.entrySet()) {
                    if (CommonUtil.isEmpty(entry.getKey())) {
                        continue;
                    }
                    srcValues.put(entry.getKey(), entry.getValue());
                }
                vo.setDictValues(srcValues);
                this.saveOrUpdate(vo, languageSize);
            }
            return vo;
        }

        vo = new DictVO();
        vo.setAppId(appId)
                .setAppCode(appCode)
                .setDictKey(dictKey)
                .setIsFinish("n")
                .setDictValues(defaultValues != null ? defaultValues : new HashMap<>())
                .setSort(sort)
                .setGroup(group)
        ;

        this.saveOrUpdate(vo, languageSize);

        return vo;

    }
}
