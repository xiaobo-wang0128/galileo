package org.armada.galileo.i18n_server.web.rpc;

import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.i18n_server.dal.entity.I18nApp;
import org.armada.galileo.i18n_server.dal.entity.I18nDictionaryKey;
import org.armada.galileo.i18n_server.dal.mapper.I18nAppMapper;
import org.armada.galileo.i18n_server.dal.mapper.I18nDictionaryKeyMapper;
import org.armada.galileo.i18n_server.dal.vo.DictQueryVO;
import org.armada.galileo.i18n_server.dal.vo.DictVO;
import org.armada.galileo.i18n_server.scheduler.AutoTranslate;
import org.armada.galileo.i18n_server.service.DictService;
import org.armada.galileo.i18n_server.util.ExcelData;
import org.armada.galileo.i18n_server.util.ExcelUtil;
import org.armada.galileo.i18n_server.util.I18nLocaleUtil;
import org.armada.galileo.annotation.mvc.NoToken;
import org.armada.galileo.annotation.mvc.UserDefine;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2022/12/26 16:18
 */
@Slf4j
@Controller
public class DictRpc {

    @Autowired
    private DictService dictService;

    @Autowired
    private AutoTranslate autoTranslate;

    @Autowired
    private I18nDictionaryKeyMapper dictionaryKeyMapper;

    @Autowired
    private I18nAppMapper i18nAppMapper;

    /**
     * 新增或修改
     *
     * @param dto
     */
    public void saveUpdate(@RequestBody DictVO dto) {

        Integer dictId = dto.getDictId();

        I18nDictionaryKey dict = dictionaryKeyMapper.selectById(dictId);
        if (dict == null) {
            throw new BizException("词条不存在");
        }
        I18nApp app = i18nAppMapper.selectById(dict.getAppId());
        if (app == null) {
            throw new BizException("词条不存在");
        }
        int languageSize = app.getLocales().size();

        dto.setAppCode(app.getAppCode());

        dictService.saveOrUpdate(dto, languageSize);
    }

    /**
     * 根据id删除
     *
     * @param securityId
     */
    public void remove(Long securityId) {
        dictService.removeById(securityId);
    }

    /**
     * 查询所有记录，只返回前1000条
     *
     * @return
     */
    @NoToken
    public List<DictVO> query(DictQueryVO queryVO) {
        return dictService.query(queryVO);
    }

    /**
     * 下载 excel 导入样板
     *
     * @param appCode
     * @param response
     * @throws Exception
     */
    @UserDefine
    @NoToken
    public void downloadSample(String appCode, HttpServletResponse response) throws Exception {
        I18nApp appInfo = dictService.selectByAppCode(appCode);
        List<String> excelHead = CommonUtil.asList("appCode", "词条code");
        List<String> record = CommonUtil.asList(appInfo.getAppCode(), "test");

        for (String locale : appInfo.getLocales()) {
            excelHead.add(I18nLocaleUtil.getLanByCode(locale).getLabel());
            record.add("");
        }

        ExcelUtil.exportToWeb("国际化词条", excelHead, CommonUtil.asList(record), response);
    }


    /**
     * 下载词条信息 excel
     *
     * @param appCode
     * @param type
     * @param response
     * @throws Exception
     */
    @UserDefine
    @NoToken
    public void download_dict(String appCode, String type, HttpServletResponse response) throws Exception {

        DictQueryVO queryVO = new DictQueryVO();
        queryVO.setAppCode(appCode);

        if ("un_finish".equals(type)) {
            queryVO.setIsFinish("n");
        }

        List<I18nApp> appInfo = dictService.selectByAppCodes(appCode);

        if (CommonUtil.isEmpty(appInfo)) {
            throw new BizException("appInfo 信息为空");
        }

        List<String> locales = appInfo.get(0).getLocales();

        List<String> excelHead = CommonUtil.asList("appCode", "词条code");
        List<List<String>> excelRows = new ArrayList<>();

        if ("sample".equals(type)) {
            List<String> record = CommonUtil.asList(appInfo.get(0).getAppCode(), "test");
            for (String locale : locales) {
                excelHead.add(I18nLocaleUtil.getLanByCode(locale).getLabel());
                record.add("");
            }
            excelRows.add(record);
        } else {
            queryVO.setPage(false);
            List<DictVO> dictVOS = dictService.query(queryVO);

            for (String locale : locales) {
                excelHead.add(I18nLocaleUtil.getLanByCode(locale).getLabel());
            }

            for (DictVO dictVO : dictVOS) {
                List<String> record = CommonUtil.asList(dictVO.getAppCode(), dictVO.getDictKey());
                for (String locale : locales) {
                    if (dictVO.getDictValues() != null && dictVO.getDictValues().get(locale) != null) {
                        record.add(dictVO.getDictValues().get(locale));
                    } else {
                        record.add("");
                    }
                }
                excelRows.add(record);
            }
        }

        ExcelUtil.exportToWeb("国际化词条", excelHead, excelRows, response);
    }


    /**
     * 导入 excel
     *
     * @param request
     */
    public void importExcel(HttpServletRequest request) {

        ExcelData excelData = ExcelUtil.uploadExcel(request);
        List<DictVO> voList = new ArrayList<>(excelData.getData().size());

        Map<String, Integer> columnIndex = new HashMap<>();
        for (int i = 0; i < excelData.getTitle().size(); i++) {
            String head = excelData.getTitle().get(i);
            if (CommonUtil.isEmpty(head)) {
                continue;
            }
            if ("appCode".equals(head)) {
                columnIndex.put("appCode", i);
            } else if ("词条code".equals(head)) {
                columnIndex.put("词条code", i);
            } else {
                columnIndex.put(excelData.getTitle().get(i), i);
            }
        }

        for (List<String> row : excelData.getData()) {
            DictVO dictVO = new DictVO();
            Map<String, String> dictValue = new HashMap<>();
            for (Map.Entry<String, Integer> entry : columnIndex.entrySet()) {
                String entryKey = entry.getKey();
                Integer index = entry.getValue();

                if ("appCode".equals(entryKey)) {
                    dictVO.setAppCode(row.get(index));
                } else if ("词条code".equals(entryKey)) {
                    dictVO.setDictKey(row.get(index));
                } else {
                    dictValue.put(I18nLocaleUtil.getLanByDesc(entryKey).getValue(), row.get(index));
                }
            }
            dictVO.setDictValues(dictValue);
            voList.add(dictVO);
        }

        Map<String, I18nApp> appCache = new HashMap<>();

        for (DictVO dictVO : voList) {

            I18nApp app = appCache.get(dictVO.getAppCode());
            if (app == null) {
                app = dictService.selectByAppCode(dictVO.getAppCode());
                appCache.put(dictVO.getAppCode(), app);
            }
            if (app == null) {
                throw new BizException("app info not exist");
            }
            dictVO.setAppId(app.getId());

            DictVO old = dictService.selectByAppAndDictKey(app.getId(), dictVO.getDictKey());
            if (old == null) {
                continue;
            }
            dictVO.setDictId(old.getDictId());

            int fullSize = app.getLocales().size();
            int actual = dictVO.getDictValues().entrySet().stream().filter(e -> CommonUtil.isNotEmpty(e.getValue())).collect(Collectors.toList()).size();

            if (actual < fullSize) {
                dictVO.setIsFinish("n");
            } else {
                dictVO.setIsFinish("y");
            }
            int languageSize = app.getLocales().size();

            dictService.saveOrUpdate(dictVO, languageSize);
        }
    }


    @NoToken
    public void auto_create_dict_by_scan(String appCode, HttpServletRequest request) throws Exception {

        String json = CommonUtil.readJsonForm(request);


        I18nApp app = dictService.selectByAppCode(appCode);
        if (app == null) {
            throw new BizException("app not exist: " + appCode);
        }

        new Thread(() -> {

            List<I18nDictionaryKey> keyList = JsonUtil.fromJson(json, new TypeToken<List<I18nDictionaryKey>>() {
            }.getType());

            int sort = 99999;
            for (I18nDictionaryKey i18nDictionaryKey : keyList) {
                String key = i18nDictionaryKey.getDictionaryKey();
                DictVO dictvo = dictService.autoCreateDictKey(app, key, i18nDictionaryKey.getDictValueMap(), sort, "scan");
                if (dictvo.getSort() != null) {
                    sort = dictvo.getSort();
                }
            }

            autoTranslate.doJob();

        }).start();
    }

    @NoToken
    public void auto_create_dict_by_js(String appCode, HttpServletRequest request) throws Exception {

        I18nApp app = dictService.selectByAppCode(appCode);
        if (app == null) {
            throw new BizException("app not exist: " + appCode);
        }

        byte[] bufs = CommonUtil.getUploadBytes(request);


        String json = new String(bufs, StandardCharsets.UTF_8);

        log.info("input: " + json);

        json = json.replaceAll("//.*?\n", "");
        json = json.replaceAll("export default", "");

        Map<String, String> map = JsonUtil.fromJson(json, new TypeToken<Map<String, String>>() {
        }.getType());

        new Thread(() -> {

            int sort = 99999;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();

                Map<String, String> values = new HashMap<>();
                values.put("zh", entry.getValue());
                DictVO dictvo = dictService.autoCreateDictKey(app, key, values, sort, "js");
                if (dictvo.getSort() != null) {
                    sort = dictvo.getSort();
                }
            }

            autoTranslate.doJob();

        }).start();
    }


    @NoToken
    public void auto_create_dict_by_xml(String appCode, HttpServletRequest request) throws Exception {

        I18nApp app = dictService.selectByAppCode(appCode);
        if (app == null) {
            throw new BizException("app not exist: " + appCode);
        }

        byte[] bufs = CommonUtil.getUploadBytes(request);

        new Thread(() -> {

            SAXReader saxReader = new SAXReader();
            Document document = null;
            try {
                document = saxReader.read(new InputStreamReader(new ByteArrayInputStream(bufs), StandardCharsets.UTF_8));
            } catch (DocumentException e) {
                e.printStackTrace();
                return;
            }

            Element root = document.getRootElement();

            Map<String, String> map = new LinkedHashMap<>();

            for (Element el : root.elements("string")) {
                String key = el.attribute("name").getValue();
                if (CommonUtil.isEmpty(key)) {
                    continue;
                }
                String value = el.getText();
                map.put(key, value);
            }

            int sort = 99999;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();

                Map<String, String> values = new HashMap<>();
                values.put("zh", entry.getValue());
                DictVO dictvo = dictService.autoCreateDictKey(app, key, values, sort, "xml");
                if (dictvo.getSort() != null) {
                    sort = dictvo.getSort();
                }
            }

            autoTranslate.doJob();


        }).start();
    }


    @NoToken
    public void auto_create_dict_by_keys(String appCode, String keys) throws Exception {

        I18nApp app = dictService.selectByAppCode(appCode);
        if (app == null) {
            throw new BizException("app not exist: " + appCode);
        }

        new Thread(() -> {

            String[] keyArr = keys.split(",");

            int sort = 99999;
            for (String key : keyArr) {
                if (CommonUtil.isEmpty(key)) {
                    continue;
                }
                key = key.trim();
                DictVO dictvo = dictService.autoCreateDictKey(app, key, null, sort, "keys");
                sort = dictvo.getSort();
            }
        }).start();

    }

//    public static void main(String[] args) {
//
//        Locale[] availableLocales = Locale.getAvailableLocales();
//
//        List<Map<String, Object>> list = new ArrayList<>();
//
//        for (int i = 1; i < availableLocales.length; i++) {
//            Locale locale = availableLocales[i];
//
//            Map<String, Object> map = new HashMap<>();
//            map.put("value", locale.toString());
//            map.put("label", locale.getDisplayName());
//            map.put("desc", locale.getDisplayName(Locale.forLanguageTag(locale.toString())));
//
//            list.add(map);
//
//            // System.out.println(locale.getDisplayName() + " : " + locale.getLanguage() + " : " + locale.toString() + "" + locale.getDisplayName(Locale.forLanguageTag("ru")));
//            //i18nAllLanguage.add(Dict.create().set("label", locale.getDisplayName(request.getLocale())).set("value", locale.toString()));
//        }
//
//        System.out.println(JsonUtil.toJsonPretty(list));
//
//    }

}
