//package org.armada.galileo.i18n_server.service;
//
//import cn.hutool.core.bean.BeanUtil;
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.date.DateTime;
//import cn.hutool.core.date.DateUtil;
//import cn.hutool.core.io.FileUtil;
//import cn.hutool.core.io.IoUtil;
//import cn.hutool.core.io.file.FileReader;
//import cn.hutool.core.io.file.FileWriter;
//import cn.hutool.core.lang.Dict;
//import cn.hutool.core.util.ObjectUtil;
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.poi.excel.ExcelUtil;
//import cn.hutool.poi.excel.ExcelWriter;
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.baomidou.mybatisplus.core.toolkit.Wrappers;
//import com.github.houbb.opencc4j.util.ZhConverterUtil;
//import com.google.gson.JsonSyntaxException;
//import com.google.gson.reflect.TypeToken;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.compress.utils.Lists;
//import org.armada.galileo.common.page.ThreadPagingUtil;
//import org.armada.galileo.common.util.CommonUtil;
//import org.armada.galileo.common.util.JsonUtil;
//import org.armada.galileo.i18n_server.dal.bo.I18nAppBO;
//import org.armada.galileo.i18n_server.dal.bo.I18nBranchKeysBO;
//import org.armada.galileo.i18n_server.dal.bo.I18nDictionaryKeyBO;
//import org.armada.galileo.i18n_server.dal.bo.I18nDictionaryValueBO;
//import org.armada.galileo.i18n_server.dal.dto.DictionaryDTO;
//import org.armada.galileo.i18n_server.dal.dto.I18nAppDTO;
//import org.armada.galileo.i18n_server.dal.dto.I18nDictionaryKeyDTO;
//import org.armada.galileo.i18n_server.dal.dto.I18nDictionaryValueDTO;
//import org.armada.galileo.i18n_server.dal.entity.I18nApp;
//import org.armada.galileo.i18n_server.dal.entity.I18nBranchKeys;
//import org.armada.galileo.i18n_server.dal.entity.I18nDictionaryKey;
//import org.armada.galileo.i18n_server.dal.entity.I18nDictionaryValue;
//import org.armada.galileo.i18n_server.dal.enums.BranchTypeEnum;
//import org.armada.galileo.i18n_server.dal.mapper.I18nAppMapper;
//import org.armada.galileo.i18n_server.dal.mapper.I18nBranchKeysMapper;
//import org.armada.galileo.i18n_server.dal.mapper.I18nDictionaryKeyMapper;
//import org.armada.galileo.i18n_server.dal.mapper.I18nDictionaryValueMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.IOException;
//import java.net.URLEncoder;
//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.Collectors;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipInputStream;
//import java.util.zip.ZipOutputStream;
//
//@Service
//@Slf4j
//public class I18nService {
//
//    @Autowired
//    private I18nAppBO i18nAppBO;
//
//    @Autowired
//    private I18nDictionaryKeyBO i18nDictionaryKeyBO;
//
//    @Autowired
//    private I18nDictionaryValueBO i18nDictionaryValueBO;
//
//    @Autowired
//    private I18nBranchKeysMapper i18nBranchKeysMapper;
//
//    @Autowired
//    private I18nBranchKeysBO i18nBranchKeysBO;
//
//    @Autowired
//    private I18nAppMapper appMapper;
//
//    @Autowired
//    private I18nDictionaryKeyMapper dictionaryKeyMapper;
//
//    @Autowired
//    private I18nDictionaryValueMapper dictionaryValueMapper;
//
//
//    private final List<Dict> i18nAllLanguage = new ArrayList<>(159);
//    private final Map<String, Locale> localeMap = new HashMap<>();
//    private Locale webLocale = Locale.CHINESE;
//
//    {
//        Locale[] availableLocales = Locale.getAvailableLocales();
//        for (int i = 1; i < availableLocales.length; i++) {
//            Locale locale = availableLocales[i];
//            i18nAllLanguage.add(Dict.create().set("label", locale.getDisplayName(webLocale)).set("value", locale.toString()));
//            localeMap.put(locale.toString(), locale);
//        }
//    }
//
//    public Integer createOrUpdateI18nApp(I18nAppDTO i18nApp) {
//        if (i18nApp == null ||
//                StrUtil.isEmpty(i18nApp.getAppCode()) ||
//                // StrUtil.isEmpty(i18nApp.getAppName()) ||
//                CollUtil.isEmpty(i18nApp.getLocales()))
//            throw new RuntimeException("参数异常，无法创建");
//        List<I18nAppDTO> apps = i18nAppBO.selectList(new QueryWrapper<I18nApp>().eq("app_code", i18nApp.getAppCode()));
//        if (apps.isEmpty()) {
//            i18nAppBO.save(i18nApp);
//        } else {
//            i18nApp.setId(apps.get(0).getId());
//            i18nAppBO.updateById(i18nApp);
//        }
//        return i18nApp.getId();
//    }
//
//    public Object getI18nAllLanguage(HttpServletRequest request) {
//        if (request.getLocale().equals(webLocale)) {
//            return i18nAllLanguage;
//        }
//        i18nAllLanguage.clear();
//        Locale[] availableLocales = Locale.getAvailableLocales();
//        for (int i = 1; i < availableLocales.length; i++) {
//            Locale locale = availableLocales[i];
//            i18nAllLanguage.add(Dict.create().set("label", locale.getDisplayName(request.getLocale())).set("value", locale.toString()));
//        }
//        webLocale = request.getLocale();
//        return i18nAllLanguage;
//    }
//
//    public List<I18nAppDTO> getI18nAllApp() {
//        return i18nAppBO.selectList(null);
//    }
//
//    public List<I18nDictionaryKeyDTO> getI18nAllAppDictionary(I18nDictionaryKeyDTO i18nDictionaryKeyDTO, Boolean turnOn) {
//        I18nAppDTO i18nAppDTO = i18nAppBO.selectById(i18nDictionaryKeyDTO.getAppId());
//        if (i18nAppDTO == null) {
//            return null;
//        }
//        i18nDictionaryKeyDTO.setLocales(i18nAppDTO.getLocales());
//        // 查询分支key设置到查询参数中
//        Set<String> keys = queryBranchKeysByBranch(i18nDictionaryKeyDTO.getBranchMaps(), i18nAppDTO.getId());
//        if (turnOn) {
//            ThreadPagingUtil.turnOn();
//        }
//
//        List<I18nDictionaryKeyDTO> i18nDictionaryKeyDTOS = i18nDictionaryKeyBO.selectList(
//                new QueryWrapper<I18nDictionaryKey>()
//                        .eq("app_id", i18nDictionaryKeyDTO.getAppId())
//                        .in(CollUtil.isNotEmpty(i18nDictionaryKeyDTO.getTransitionDictionaryKeyIds()), "id", i18nDictionaryKeyDTO.getTransitionDictionaryKeyIds())
//                        .in(CollUtil.isNotEmpty(keys), "dictionary_key", keys)
//                        .like(StrUtil.isNotEmpty(i18nDictionaryKeyDTO.getDictionaryKey()), "dictionary_key", i18nDictionaryKeyDTO.getDictionaryKey()));
//        if (CollUtil.isNotEmpty(i18nDictionaryKeyDTOS)) {
//            Set<Integer> dictionaryKeyId = i18nDictionaryKeyDTOS.stream().map(I18nDictionaryKeyDTO::getId).collect(Collectors.toSet());
//            List<I18nDictionaryValueDTO> i18nDictionaryValueDTOS = i18nDictionaryValueBO.selectList(
//                    new QueryWrapper<I18nDictionaryValue>()
//                            .in("dictionary_key_id", dictionaryKeyId)
//                            .in("locale", i18nDictionaryKeyDTO.getLocales()));
//            if (CollUtil.isNotEmpty(i18nDictionaryValueDTOS)) {
//                Map<Integer, List<I18nDictionaryValueDTO>> listMap = i18nDictionaryValueDTOS.stream().collect(Collectors.groupingBy(I18nDictionaryValueDTO::getDictionaryKeyId));
//                for (I18nDictionaryKeyDTO nDictionaryKeyDTO : i18nDictionaryKeyDTOS) {
//                    nDictionaryKeyDTO.setAppCode(i18nAppDTO.getAppCode());
//                    nDictionaryKeyDTO.setAppName(i18nAppDTO.getAppName());
//                    List<I18nDictionaryValueDTO> i18nDictionaryValueDTOS1 = listMap.get(nDictionaryKeyDTO.getId());
//                    nDictionaryKeyDTO.setI18nValueMap(new HashMap<>());
//                    if (CollUtil.isNotEmpty(i18nDictionaryValueDTOS1)) {
//                        for (I18nDictionaryValueDTO i18nDictionaryValueDTO : i18nDictionaryValueDTOS1) {
//                            nDictionaryKeyDTO.getI18nValueMap().put(i18nDictionaryValueDTO.getLocale(), i18nDictionaryValueDTO.getDictionaryValue());
//                        }
//                    }
//                }
//            }
//        }
//        return i18nDictionaryKeyDTOS;
//    }
//
//    private Set<String> queryBranchKeysByBranch(Map<String, List<String>> branchMaps, Integer appId) {
//        Set<String> keys = new HashSet<>();
//        if (CollUtil.isNotEmpty(branchMaps) && CollUtil.isNotEmpty(branchMaps.values())) {
//            branchMaps.forEach((k, v) -> {
//                if (CollUtil.isNotEmpty(v)) {
//                    for (BranchTypeEnum value : BranchTypeEnum.values()) {
//                        if (StrUtil.equals(value.getName(), k))
//                            keys.addAll(queryBranchKeysByAppIdAndPathAndType(appId, v, value.getCode()));
//                    }
//                }
//            });
//        }
//        return keys;
//    }
//
//    private Collection<String> queryBranchKeysByAppIdAndPathAndType(Integer appId, List<String> v, String code) {
//        return i18nBranchKeysMapper.selectList(Wrappers.<I18nBranchKeys>lambdaQuery()
//                        .eq(I18nBranchKeys::getBranchType, code)
//                        .eq(I18nBranchKeys::getAppId, appId)
//                        .in(I18nBranchKeys::getBranchPath, v))
//                .stream().map(I18nBranchKeys::getDictionaryKeys)
//                .filter(CollUtil::isNotEmpty)
//                .flatMap(Collection::stream)
//                .collect(Collectors.toSet());
//    }
//
//    public void createOrUpdateDictionary(I18nDictionaryKeyDTO i18nDictionaryKeyDTO) {
//        if (i18nDictionaryKeyBO.saveOrUpdate(i18nDictionaryKeyDTO)) {
//            if (CollUtil.isNotEmpty(i18nDictionaryKeyDTO.getI18nValueMap())) {
//                removeValueByKeyId(i18nDictionaryKeyDTO.getId());
//                i18nDictionaryKeyDTO.getI18nValueMap().forEach((k, v) -> {
//                    I18nDictionaryValueDTO i18nDictionaryValueDTO = BeanUtil.copyProperties(i18nDictionaryKeyDTO, I18nDictionaryValueDTO.class, "id");
//                    i18nDictionaryValueDTO.setLocale(k);
//                    i18nDictionaryValueDTO.setDictionaryKeyId(i18nDictionaryKeyDTO.getId());
//                    i18nDictionaryValueDTO.setDictionaryValue(v);
//                    i18nDictionaryValueBO.save(i18nDictionaryValueDTO);
//                });
//            }
//        }
//    }
//
//    private void removeValueByKeyId(Integer keyId) {
//        if (keyId != null) {
//            List<I18nDictionaryValueDTO> i18nDictionaryValueDTOS = i18nDictionaryValueBO.selectList(new QueryWrapper<I18nDictionaryValue>().eq("dictionary_key_id", keyId));
//            if (CollUtil.isNotEmpty(i18nDictionaryValueDTOS)) {
//                for (I18nDictionaryValueDTO i18nDictionaryValueDTO : i18nDictionaryValueDTOS) {
//                    i18nDictionaryValueBO.removeById(i18nDictionaryValueDTO.getId());
//                }
//            }
//        }
//    }
//
//    @Autowired
//    private I18nDictionaryValueMapper i18nDictionaryValueMapper;
//
//    private final String COMMON_PLATFORM = "iwms";
//
//    public void getI18nZipByAppCode(String appCode, HttpServletResponse response) throws IOException {
//
//        I18nAppDTO i18nAppDTO = null;
//        I18nAppDTO i18nCommonAppDTO = null;
//        for (I18nAppDTO itme : i18nAppBO.selectList(null)) {
//            if (StrUtil.equals(itme.getAppCode(), COMMON_PLATFORM)) {
//                i18nCommonAppDTO = itme;
//            }
//            if (StrUtil.equals(itme.getAppCode(), appCode)) {
//                i18nAppDTO = itme;
//            }
//        }
//        if (i18nAppDTO != null // && i18nCommonAppDTO != null
//        ) {
//            try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
//                Map<String, Map<String, String>> mergeDictionaryMap = new ConcurrentHashMap<>();
////                for (String locale : i18nCommonAppDTO.getLocales()) {
////                    List<DictionaryDTO> dictionaryDTOS = i18nDictionaryValueMapper.getValueByAppAndlocale(i18nCommonAppDTO.getId(), locale);
////                    Map<String, String> dictionaryMap = new ConcurrentHashMap<>();
////                    mergeDictionaryMap.put(localeMap.get(locale).toLanguageTag(), dictionaryMap);
////                    for (DictionaryDTO dictionaryDTO : dictionaryDTOS) {
////                        if (dictionaryDTO.getDictionaryValue() == null)
////                            continue;
////                        dictionaryMap.put(dictionaryDTO.getDictionaryKey(), dictionaryDTO.getDictionaryValue());
////                    }
////                }
//                for (String locale : i18nAppDTO.getLocales()) {
//                    List<DictionaryDTO> dictionaryDTOS = i18nDictionaryValueMapper.getValueByAppAndlocale(i18nAppDTO.getId(), locale);
//                    Map<String, String> dictionaryMap = mergeDictionaryMap.getOrDefault(localeMap.get(locale).toLanguageTag(), new ConcurrentHashMap<>());
//                    mergeDictionaryMap.put(localeMap.get(locale).toLanguageTag(), dictionaryMap);
//                    for (DictionaryDTO dictionaryDTO : dictionaryDTOS) {
//                        if (dictionaryDTO.getDictionaryValue() == null)
//                            continue;
//                        dictionaryMap.put(dictionaryDTO.getDictionaryKey(), dictionaryDTO.getDictionaryValue());
//                    }
//                }
//                mergeDictionaryMap.forEach((k, v) -> {
//                    String fileName = "messages_" + k.replace('-', '_') + ".properties";
//                    List<String> list = new ArrayList<>();
//                    v.forEach((k1, v1) -> {
//                        list.add(k1 + "=" + v1);
//                    });
//                    ZipEntry zipEntry = new ZipEntry(fileName);
//                    try {
//                        zipOut.putNextEntry(zipEntry);
//                        FileUtil.del(fileName);
//                        FileWriter writer = new FileWriter(fileName);
//                        File file = writer.writeLines(list);
//                        FileReader fileReader = new FileReader(file);
//                        fileReader.writeToStream(zipOut);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } finally {
//                        FileUtil.del(fileName);
//                    }
//                });
//            }
//        }
//    }
//
//    public void getImportTemplate(Integer appId, HttpServletResponse response) {
//        I18nDictionaryKeyDTO i18nDictionaryKeyDTO = new I18nDictionaryKeyDTO();
//        i18nDictionaryKeyDTO.setAppId(appId);
//        List<I18nDictionaryKeyDTO> i18nAllAppDictionarys = (List) getI18nAllAppDictionary(i18nDictionaryKeyDTO, false);
//        if (CollUtil.isNotEmpty(i18nAllAppDictionarys)) {
//            I18nDictionaryKeyDTO i18nDictionaryKeyDTO1 = i18nAllAppDictionarys.get(0);
//            String fileName = i18nDictionaryKeyDTO1.getAppCode() + "-importI18nDictionaryTemplate.xlsx";
//            Map<String, Object> row1 = new LinkedHashMap<>();
//            row1.put("appCode", i18nDictionaryKeyDTO1.getAppCode());
//            row1.put("dictionaryKey", i18nDictionaryKeyDTO1.getDictionaryKey());
//            for (String locale : i18nDictionaryKeyDTO.getLocales()) {
//                Locale locale1 = localeMap.get(locale);
//                row1.put(locale1.getDisplayName(locale1) + "-" + locale
//                        , null);
//            }
//            i18nDictionaryKeyDTO1.getI18nValueMap().forEach((k, v) -> {
//                Locale locale1 = localeMap.get(k);
//                row1.put(locale1.getDisplayName(locale1) + "-" + k
//                        , v);
//            });
//            export(fileName, CollUtil.newArrayList(row1), response);
//        }
//    }
//
//    public void exportAppDictionary(I18nDictionaryKeyDTO i18nDictionaryKeyDTO, HttpServletResponse response) {
//        if (i18nDictionaryKeyDTO.getQueryUnfinished()) {
//            i18nDictionaryKeyDTO.setTransitionDictionaryKeyIds(getUnfinishedDictionary(i18nDictionaryKeyDTO));
//        }
//        i18nDictionaryKeyDTO.transition();
//        List<I18nDictionaryKeyDTO> i18nAllAppDictionarys = (List<I18nDictionaryKeyDTO>) getI18nAllAppDictionary(i18nDictionaryKeyDTO, false);
//        if (CollUtil.isNotEmpty(i18nAllAppDictionarys)) {
//            List<Map<String, Object>> rows = CollUtil.newArrayList();
//            for (I18nDictionaryKeyDTO i18nDictionaryKeyDTO1 : i18nAllAppDictionarys) {
//                Map<String, Object> row1 = new LinkedHashMap<>();
//                row1.put("appCode", i18nDictionaryKeyDTO1.getAppCode());
//                row1.put("dictionaryKey", i18nDictionaryKeyDTO1.getDictionaryKey());
//                for (String locale : i18nDictionaryKeyDTO.getLocales()) {
//                    Locale locale1 = localeMap.get(locale);
//                    row1.put(locale1.getDisplayName(locale1) + "-" + locale
//                            , null);
//                }
//                i18nDictionaryKeyDTO1.getI18nValueMap().forEach((k, v) -> {
//                    Locale locale1 = localeMap.get(k);
//                    row1.put(locale1.getDisplayName(locale1) + "-" + k
//                            , v);
//                });
//                rows.add(row1);
//            }
//            String fileName = i18nAllAppDictionarys.get(0).getAppCode() + "-export18nDictionary.xlsx";
//            export(fileName, rows, response);
//        }
//    }
//
//    private void export(String fileName, List<Map<String, Object>> rows, HttpServletResponse response) {
//        ExcelWriter writer = null;
//        try {
//            writer = new ExcelWriter(true);
//            writer.setColumnWidth(-1, 30);
//            writer.write(rows, true);
//            fileName = URLEncoder.encode(fileName, "UTF-8");
//            response.setContentType("application/vnd.ms-excel;charset=utf-8");
//            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ";" + "filename*=utf-8''" + fileName);
//            writer.flush(response.getOutputStream());
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (writer != null) {
//                writer.close();
//            }
//        }
//    }
//
//    @Autowired
//    private I18nDictionaryKeyMapper i18nDictionaryKeyMapper;
//
//    private final static Executor ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//
//    Map<String, Map<String, Object>> importMap = new ConcurrentHashMap<>();
//
//    public void importAppDictionary(HttpServletRequest request) {
//        List<I18nAppDTO> i18nAppDTOS = i18nAppBO.selectAll();
//        Map<String, Integer> codeByIdMap = i18nAppDTOS.stream().collect(Collectors.toMap(I18nAppDTO::getAppCode, I18nAppDTO::getId));
//        try {
//            byte[] uploadBytes = CommonUtil.getUploadBytes(request);
//            List<Map<String, Object>> maps = ExcelUtil.getReader(IoUtil.toStream(uploadBytes)).readAll();
//            AtomicInteger taskNum = new AtomicInteger(0);
//            for (Map<String, Object> map : maps) {
//                ex.execute(() -> doImport(map, codeByIdMap, taskNum));
//            }
//            while (maps.size() != taskNum.get()) {
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void doImport(Map<String, Object> map, Map<String, Integer> codeByIdMap, AtomicInteger taskNum) {
//        try {
//            String appCode = map.getOrDefault("appCode", "").toString();
//            String dictionaryKey = map.getOrDefault("dictionaryKey", "").toString();
//            if (StrUtil.isNotEmpty(appCode) && StrUtil.isNotEmpty(dictionaryKey)) {
//                Integer appId = codeByIdMap.get(appCode);
//                if (appId != null) {
//                    Map<String, Object> map1 = importMap.get(appCode + dictionaryKey);
//                    map.remove("appCode");
//                    map.remove("dictionaryKey");
//                    if (map.equals(map1)) {
//                        return;
//                    }
//                    importMap.put(appCode + dictionaryKey, map);
//                    I18nDictionaryKeyDTO i18nDictionaryKeyDTO = i18nDictionaryKeyMapper.getDictionaryByAppCodeAndDictionaryKey(appId, dictionaryKey);
//                    if (i18nDictionaryKeyDTO == null) {
//                        i18nDictionaryKeyDTO = new I18nDictionaryKeyDTO();
//                        i18nDictionaryKeyDTO.setAppId(appId);
//                        i18nDictionaryKeyDTO.setDictionaryKey(dictionaryKey);
//                    }
//                    Map<String, String> var = new HashMap<>();
//                    map.forEach((k, v) -> {
//                        k = k.substring(k.lastIndexOf("-") + 1);
//                        String s = v.toString();
//                        if (StrUtil.isNotEmpty(s))
//                            var.put(k, s);
//                    });
//                    if (var.isEmpty()) {
//                        return;
//                    }
//                    i18nDictionaryKeyDTO.setI18nValueMap(var);
//                    createOrUpdateDictionary(i18nDictionaryKeyDTO);
//                }
//            }
//        } catch (Exception ignored) {
//        } finally {
//            taskNum.incrementAndGet();
//        }
//    }
//
//    public I18nDictionaryKeyDTO getI18nDictionaryKey(String resKey) {
//        return i18nDictionaryKeyBO.selectI18nDictionaryByKey(resKey);
//    }
//
//    public void importAppDictionaryByZipFile(HttpServletRequest request, String branch_path) {
//        ZipInputStream zipInputStream = null;
//        try {
//            zipInputStream = new ZipInputStream(request.getInputStream());
//            Map<String, String> paramMap = new HashMap<>();
//            ZipEntry nextEntry = zipInputStream.getNextEntry();
//            while (nextEntry != null) {
//                if (nextEntry.getName().contains("zh_CN.json")) {
//                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                    int len;
//                    byte[] bufs = new byte[4096];
//                    while ((len = zipInputStream.read(bufs)) != -1) {
//                        bos.write(bufs, 0, len);
//                    }
//                    if (StrUtil.isNotEmpty(bos.toString())) {
//                        try {
//                            paramMap.putAll(JsonUtil.fromJson(bos.toString(), new TypeToken<Map<String, String>>() {
//                            }.getType()));
//                        } catch (JsonSyntaxException e) {
//                            log.error("导入的zip包,json格式错误,来自文件名{}", nextEntry.getName());
//                        }
//                    }
//                }
//                nextEntry = zipInputStream.getNextEntry();
//            }
//            ex.execute(() -> doBusinessImport(paramMap));
//            ex.execute(() -> doBranchKeyImport(paramMap, branch_path, BranchTypeEnum.IWMS_FRONT));
//        } catch (IOException ignored) {
//        } finally {
//            if (zipInputStream != null) {
//                try {
//                    zipInputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//
//    }
//
//    private void doBranchKeyImport(Map<String, String> paramMap, String branch_path, BranchTypeEnum branchType) {
//        if (ObjectUtil.isAllNotEmpty(paramMap, branch_path, branchType)) {
//            List<I18nAppDTO> app = i18nAppBO.selectList(new QueryWrapper<I18nApp>().eq("app_code", branchType.getAppCode()));
//            if (CollUtil.isEmpty(app))
//                return;
//            Integer id = app.get(0).getId();
//            I18nBranchKeys i18nBranchKeys = i18nBranchKeysMapper.selectOne(Wrappers.<I18nBranchKeys>lambdaQuery()
//                    .eq(I18nBranchKeys::getBranchType, branchType.getCode())
//                    .eq(I18nBranchKeys::getAppId, id)
//                    .eq(I18nBranchKeys::getBranchPath, branch_path));
//            if (i18nBranchKeys == null) {
//                i18nBranchKeys = new I18nBranchKeys();
//                i18nBranchKeys.setAppId(id);
//                i18nBranchKeys.setBranchType(branchType.getCode());
//                i18nBranchKeys.setBranchPath(branch_path);
//            }
//            i18nBranchKeys.setDictionaryKeys(paramMap.keySet());
//            i18nBranchKeysBO.saveOrUpdate(i18nBranchKeysBO.convertToDto(i18nBranchKeys));
//        }
//    }
//
//    private void doBusinessImport(Map<String, String> paramMap) {
//        if (CollUtil.isNotEmpty(paramMap)) {
//            List<I18nDictionaryKeyDTO> dictionaryKeys = i18nDictionaryKeyBO.selectList(new QueryWrapper<I18nDictionaryKey>().in("dictionary_key", paramMap.keySet()));
//            for (I18nDictionaryKeyDTO dictionaryKey : dictionaryKeys) {
//                paramMap.remove(dictionaryKey.getDictionaryKey());
//            }
//            if (CollUtil.isNotEmpty(paramMap)) {
//                List<I18nAppDTO> i18nAppDTOS = i18nAppBO.selectList(new QueryWrapper<I18nApp>().eq("app_code", "iwms"));
//                paramMap.forEach((k, v) -> {
//                    int appId = CollUtil.isEmpty(i18nAppDTOS) ? 1 : i18nAppDTOS.get(0).getId();
//                    I18nDictionaryKeyDTO i18nDictionaryKeyDTO = new I18nDictionaryKeyDTO();
//                    i18nDictionaryKeyDTO.setDictionaryKey(k);
//                    i18nDictionaryKeyDTO.setAppId(appId);
//                    Map<String, String> i18nValueMap = new HashMap<>();
//                    i18nValueMap.put("zh_CN", v);
//                    i18nValueMap.put("zh_HK", ZhConverterUtil.toTraditional(v));
//                    i18nDictionaryKeyDTO.setI18nValueMap(i18nValueMap);
//                    createOrUpdateDictionary(i18nDictionaryKeyDTO);
//                });
//            }
//        }
//    }
//
//    // @Scheduled(fixedRate = 1000 * 60 * 60)
//    public void createBackups() {
//        Map<String, Object> backups = new HashMap<>();
//        List<I18nAppDTO> i18nAppDTOS = i18nAppBO.selectList(null);
//        List<I18nDictionaryKeyDTO> i18nDictionaryKeyDTOS = i18nDictionaryKeyBO.selectList(null);
//        List<I18nDictionaryValueDTO> i18nDictionaryValueDTOS = i18nDictionaryValueBO.selectList(null);
//        backups.put("i18nAppDTOS", i18nAppDTOS);
//        backups.put("i18nDictionaryKeyDTOS", i18nDictionaryKeyDTOS);
//        backups.put("i18nDictionaryValueDTOS", i18nDictionaryValueDTOS);
//        String s = "";
//        File lastFile = null;
//        if (FileUtil.exist("/hairou/backups")) {
//            File[] ls = FileUtil.ls("/hairou/backups");
//            if (ls.length > 0) {
//                // 创建一个删除一个月前的记录集合
//                List<File> delFile = new ArrayList<File>();
//                for (File l : ls) {
//                    if (lastFile == null || lastFile.lastModified() < l.lastModified())
//                        lastFile = l;
//                    // 添加一个月前的记录到删除集合
//                    if (l.lastModified() < System.currentTimeMillis() - 30L * 24 * 3600000)
//                        delFile.add(l);
//                }
//                // 最后一个记录不能删除
//                delFile.remove(lastFile);
//                for (File file : delFile) {
//                    FileUtil.del(file);
//                }
//                s = FileUtil.readUtf8String(lastFile);
//            }
//        }
//        String s1 = JsonUtil.toJson(backups);
//        if (!s.equals(s1)) {
//            FileWriter writer = new FileWriter("/hairou/backups/backup" + DateUtil.format(new Date(), "yyyy-MM-dd-HH-mm-ss") + ".txt", StandardCharsets.UTF_8);
//            writer.write(s1);
//        }
//    }
//
//
//    public List<Integer> getUnfinishedDictionary(I18nDictionaryKeyDTO i18nDictionaryKeyDTO) {
//        I18nAppDTO i18nAppDTO = i18nAppBO.selectById(i18nDictionaryKeyDTO.getAppId());
////        if (i18nAppDTO != null && CollUtil.isNotEmpty(i18nAppDTO.getLocales())) {
////            i18nDictionaryKeyDTO.transition();
////            Set<String> keys = queryBranchKeysByBranch(i18nDictionaryKeyDTO.getBranchMaps(), i18nDictionaryKeyDTO.getAppId());
////            return i18nDictionaryValueBO.getUnfinishedDictionaryByAppIdAndLocals(keys, i18nDictionaryKeyDTO.getAppId(), i18nAppDTO.getLocales().size());
////        }
//
//        List<I18nApp> appList = appMapper.selectList(null);
//        Map<Integer, I18nApp> appMap = appList.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
//
//        QueryWrapper<I18nDictionaryKey> keyQuery = new QueryWrapper<>();
//        List<I18nDictionaryKey> keyList = dictionaryKeyMapper.selectList(keyQuery);
//        Map<Integer, List<I18nDictionaryKey>> appKeyCache = keyList.stream().collect(Collectors.groupingBy(e -> e.getAppId()));
//
//        QueryWrapper<I18nDictionaryValue> valueQuery = new QueryWrapper<>();
//        valueQuery.isNotNull("dictionary_value");
//        List<I18nDictionaryValue> valueList = dictionaryValueMapper.selectList(valueQuery);
//        Map<Integer, List<I18nDictionaryValue>> appValueCache = valueList.stream().collect(Collectors.groupingBy(e -> e.getAppId()));
//
//        List<Integer> results = new ArrayList<>();
//        for (I18nApp i18nApp : appList) {
//            List<String> locales = i18nApp.getLocales();
//
//            if (CommonUtil.isEmpty(locales)) {
//                continue;
//            }
//
//            List<I18nDictionaryKey> appKeys = appKeyCache.get(i18nApp.getId());
//            if (appKeys == null) {
//                continue;
//            }
//
//            List<I18nDictionaryValue> appValues = appValueCache.get(i18nApp.getId());
//            if (appValues == null) {
//                continue;
//            }
//
//            // 每个字词 各有 多少种翻译  keyId: count
//            Map<Integer, List<I18nDictionaryValue>> keyCount = appValues.stream().collect(Collectors.groupingBy(e -> e.getDictionaryKeyId()));
//
//            for (I18nDictionaryKey appKey : appKeys) {
//                if (CommonUtil.isEmpty(keyCount.get(appKey.getId())) || keyCount.get(appKey.getId()).size() < locales.size()) {
//                    results.add(appKey.getId());
//                }
//            }
//
//        }
//
//        return results;
//    }
//
//    public void importAppDictionaryByMap(Map<String, String> map, String branch_path) {
//        ex.execute(() -> doBusinessImport(map));
//        ex.execute(() -> doBranchKeyImport(map, branch_path, BranchTypeEnum.IWMS_BACK));
//    }
//
//    public void getWriteAbleCookie(String password, HttpServletResponse response) {
//
//        if (password != null && password.contains(CommonUtil.format(new Date(), "yyyyMMdd"))) {
//            return;
//        }
//        throw new RuntimeException("密钥不正确");
//    }
//
//    public Object getAppBranchs(Integer appId) {
//        Map<String, Set<String>> maps = new LinkedHashMap<>();
//        if (appId != null) {
//            I18nAppDTO i18nAppDTO = i18nAppBO.selectById(appId);
//            if (i18nAppDTO != null) {
//                String appCode = i18nAppDTO.getAppCode();
//                for (BranchTypeEnum value : BranchTypeEnum.values()) {
//                    if (StrUtil.equals(value.getAppCode(), appCode)) {
//                        // 分支名称作为前端页面展示的lable
//                        String name = value.getName();
//                        List<I18nBranchKeys> i18nBranchKeys = i18nBranchKeysMapper.selectList(Wrappers.<I18nBranchKeys>lambdaQuery()
//                                .eq(I18nBranchKeys::getBranchType, value.getCode())
//                                .eq(I18nBranchKeys::getAppId, appId));
//                        if (CollUtil.isNotEmpty(i18nBranchKeys))
//                            maps.put(name, i18nBranchKeys.stream().map(I18nBranchKeys::getBranchPath).collect(Collectors.toSet()));
//                    }
//                }
//            }
//        }
//        return maps;
//    }
//}
