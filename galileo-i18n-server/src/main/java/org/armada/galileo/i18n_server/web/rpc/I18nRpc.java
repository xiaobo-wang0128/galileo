package org.armada.galileo.i18n_server.web.rpc;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.annotation.mvc.UserDefine;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.i18n_server.dal.entity.I18nApp;
import org.armada.galileo.i18n_server.dal.entity.I18nDictionaryKey;
import org.armada.galileo.i18n_server.dal.mapper.I18nDictionaryKeyMapper;
import org.armada.galileo.i18n_server.service.DictService;
import org.armada.galileo.i18n_server.util.I18nLocaleUtil;
import org.armada.galileo.annotation.mvc.NoToken;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author xiaobo
 * @date 2022/12/27 18:41
 */
@Controller
@Slf4j
public class I18nRpc {

    @Autowired
    private I18nDictionaryKeyMapper keyMapper;

    @Autowired
    private DictService dictService;


    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExportFile {
        private String fileName;
        private String fileContent;
        private byte[] fileBytes;
    }


    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TmpLabel {
        private String type;
        private String label;
    }


    @NoToken
    public Map<String, String> exportJson(String appCode, String lan, HttpServletResponse response) {

        List<I18nApp> apps = dictService.selectByAppCodes(appCode);
//        long count = apps.stream().map(e -> e.getExportType()).distinct().collect(Collectors.counting());
//        if (count != 1) {
//            throw new BizException("资源文件的导出类型不一致");
//        }
        List<Integer> appIds = apps.stream().map(e -> e.getId()).collect(Collectors.toList());

        QueryWrapper<I18nDictionaryKey> queryWrapper = new QueryWrapper<I18nDictionaryKey>();
        queryWrapper.in("app_id", appIds);
        List<I18nDictionaryKey> keys = keyMapper.selectList(queryWrapper);

        Map<String, String> dicts = new HashMap<>();

        for (I18nDictionaryKey key : keys) {
            if (key.getDictValueMap() == null) {
                continue;
            }

            String v = key.getDictValueMap().get(lan);
            if (CommonUtil.isEmpty(v)) {
                continue;
            }

            dicts.put(key.getDictionaryKey().trim(), v);
        }

        return dicts;
    }

    @NoToken
    @UserDefine
    public void exportResource(String appCode, HttpServletResponse response) {

        List<I18nApp> apps = dictService.selectByAppCodes(appCode);

//        long count = apps.stream().map(e -> e.getExportType()).distinct().collect(Collectors.counting());
//        if (count != 1) {
//            throw new BizException("资源文件的导出类型不一致");
//        }



        List<Integer> appIds = apps.stream().map(e -> e.getId()).collect(Collectors.toList());

        QueryWrapper<I18nDictionaryKey> queryWrapper = new QueryWrapper<I18nDictionaryKey>();
        queryWrapper.in("app_id", appIds);
        List<I18nDictionaryKey> keys = keyMapper.selectList(queryWrapper);

        List<String> locales = apps.get(0).getLocales();
        String type = apps.get(0).getExportType();

        log.info("export resource type: " + type);

        List<ExportFile> files = new ArrayList<>();

        // properties
        if ("java".equals(type)) {

            for (String locale : locales) {

                StringBuilder sb = new StringBuilder();

                for (I18nDictionaryKey key : keys) {
                    if (key.getDictValueMap() == null) {
                        continue;
                    }

                    String v = key.getDictValueMap().get(locale);
                    if (CommonUtil.isEmpty(v)) {
                        continue;
                    }

                    sb.append(key.getDictionaryKey())
                            .append("=")
                            .append(v)
                            .append("\n");

                }

                ExportFile exportFile = new ExportFile();
                exportFile.setFileName("messages_" + locale + ".properties");
                exportFile.setFileContent(sb.toString());

                files.add(exportFile);
            }

        }
        // js
        else if ("js".equals(type)) {

            List<TmpLabel> tmpLabels = new ArrayList<>();

//            Map<String, Object> main = new HashMap<>();
//            Map<String, Object> messages = new HashMap<>();

            for (String locale : locales) {

                Map<String, String> dicts = new HashMap<>();

                for (I18nDictionaryKey key : keys) {
                    if (key.getDictValueMap() == null) {
                        continue;
                    }

                    String v = key.getDictValueMap().get(locale);
                    if (CommonUtil.isEmpty(v)) {
                        continue;
                    }

                    dicts.put(key.getDictionaryKey().trim(), v);
                }

                // messages.put(locale, dicts);

                ExportFile lanFile = new ExportFile();
                lanFile.setFileContent("export default " + JsonUtil.toJson(dicts));
                lanFile.setFileName("lang/" + locale + ".js");
                files.add(lanFile);

                tmpLabels.add(new TmpLabel(locale, I18nLocaleUtil.getLanByCode(locale).getDesc()));

            }

            ExportFile typeFile = new ExportFile();
            typeFile.setFileContent(JsonUtil.toJsonPretty(tmpLabels));
            typeFile.setFileName("i18n_config.json");

            files.add(typeFile);

        } else if ("android".equals(type)) {

            for (String locale : locales) {


                Document doc = DocumentHelper.createDocument();
                //生成根元素
                Element root = doc.addElement("resources");

                for (I18nDictionaryKey key : keys) {
                    if (key.getDictValueMap() == null) {
                        continue;
                    }

                    String v = key.getDictValueMap().get(locale);
                    if (CommonUtil.isEmpty(v)) {
                        continue;
                    }

                    Element strEl = root.addElement("string");
                    strEl.addAttribute("name", key.getDictionaryKey());
                    strEl.setText(v);
                }

                //org.dom4j.XMLWriter
                try {
                    XMLWriter writer = new XMLWriter(OutputFormat.createPrettyPrint());
                    ByteArrayOutputStream fos = new ByteArrayOutputStream();
                    writer.setOutputStream(fos);
                    writer.write(doc);
                    writer.close();

                    ExportFile exportFile = new ExportFile();
                    exportFile.setFileName("values" + (locale.equals("zh") ? "" : "-" + locale) + "/strings.xml");
                    exportFile.setFileBytes(fos.toByteArray());
                    files.add(exportFile);

                    fos.close();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

            }
        }
        generateZip(files, response);

    }

    public static void generateZip(List<ExportFile> exportFiles, HttpServletResponse response) {
        try {
            String fileName = "i18n_" + CommonUtil.format(new Date(), "yyyy_MM_dd_HH_mm_ss") + ".zip";
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.addHeader("Pargam", "no-cache");
            response.addHeader("Cache-Control", "no-cache");

            // 创建zip输出流
            ZipOutputStream out = new ZipOutputStream(response.getOutputStream(), Charset.forName("utf-8"));

            for (ExportFile exportFile : exportFiles) {
                String zipTitle = exportFile.getFileName();
                byte[] bytes = exportFile.getFileBytes() != null ? exportFile.getFileBytes() : exportFile.getFileContent().getBytes(StandardCharsets.UTF_8);
                out.putNextEntry(new ZipEntry(zipTitle));
                out.write(bytes);
                out.closeEntry();
            }

            out.flush();
            out.close();

        } catch (Exception e) {
            throw new BizException(e);
        }
    }


}
