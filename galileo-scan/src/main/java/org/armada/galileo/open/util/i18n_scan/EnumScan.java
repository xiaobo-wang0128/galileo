package org.armada.galileo.open.util.i18n_scan;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.HttpUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.open.util.api_scan.SourcePathScan;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2022/12/25 17:06
 */
@Slf4j
public class EnumScan {


    public static void doScan(String projectPath, String targetJsonFilePath) throws Exception {

        List<String> srcPaths = new SourcePathScan(projectPath).generateSrcPath();

        List<String> enumFileContents = new ArrayList<>();

        List<EnumItem> allEnumItems = new ArrayList<>();

        for (String srcPath : srcPaths) {
            scanEnumFiles(enumFileContents, allEnumItems, new File(srcPath));
        }

        Map<EnumItem.EnumItemType, List<EnumItem>> enumItemTypeListMap = allEnumItems.stream().collect(Collectors.groupingBy(e -> e.getType()));

        for (Map.Entry<EnumItem.EnumItemType, List<EnumItem>> mainEntry : enumItemTypeListMap.entrySet()) {
            EnumItem.EnumItemType type = mainEntry.getKey();
            List<EnumItem> enumItems = mainEntry.getValue();

            List<I18nDictionaryKeyUpload> keyUploads = new ArrayList<>();
            for (EnumItem enumItem : enumItems) {
                if (CommonUtil.isEmpty(enumItem.getDefaultDesc())) {
                    continue;
                }
                try {
                    for (Map.Entry<String, String> entry : enumItem.getDefaultDesc().entrySet()) {
                        I18nDictionaryKeyUpload upload = new I18nDictionaryKeyUpload();
                        upload.setDictionaryKey(enumItem.getTypeName() + "__" + entry.getKey());
                        Map<String, String> values = new HashMap<>();
                        values.put("zh", entry.getValue());

                        upload.setDictValueMap(values);
                        keyUploads.add(upload);
                    }
                } catch (Exception e) {
                    log.error(JsonUtil.toJson(enumItem));
                    log.error(e.getMessage(), e);
                }
            }

//            String jsons = JsonUtil.toJson(keyUploads);
//
//            String appCode = (type == EnumItem.EnumItemType.I18nDictionary) ? "java_enum" : "java_error";

//            System.out.println(CommonUtil.format("start upload i18n dict, app: {}, json: {} ", appCode, jsons));
//
//            // 上传词条
//            HttpUtil.postJson("http://i18n.imlb2c.com/i18n/i18n_server/DictRpc/auto_create_dict_by_scan.json?appCode=" + appCode, jsons);

            // 前端国际枚举列表
            if (type == EnumItem.EnumItemType.I18nDictionary) {

                // 生成枚举js文件
                Map<String, Object> jsEnumFile = new LinkedHashMap<>();
                for (EnumItem enumItem : enumItems) {
                    jsEnumFile.put(enumItem.getTypeName(), enumItem.getKeys());
                }

                String jsContent = "var __JAVA_ENUM_OPTIONS__=" + JsonUtil.toJson(jsEnumFile);

                FileOutputStream fos = new FileOutputStream(targetJsonFilePath);

                fos.write(jsContent.getBytes(StandardCharsets.UTF_8));

                fos.close();

                System.out.println("js 枚举扫描结果:");
                System.out.println(jsContent);

            }
        }
    }


    private static void scanEnumFiles(List<String> enumFileContents, List<EnumItem> enumItems, File file) throws Exception {
        if (file.isDirectory()) {
            for (File listFile : file.listFiles()) {
                scanEnumFiles(enumFileContents, enumItems, listFile);
            }
        } else {
            if (!file.exists()) {
                return;
            }
            FileInputStream fis = new FileInputStream(file);

            try {
                byte[] bufs = CommonUtil.readBytesFromInputStream(fis);

                String javaFileContent = new String(bufs);
                if (javaFileContent.indexOf("implements I18nDictionary") != -1) {
                    EnumItem item = scanJavaEnum(javaFileContent);
                    item.setType(EnumItem.EnumItemType.I18nDictionary);

                    enumItems.add(item);

                } else if (javaFileContent.indexOf("implements I18nError") != -1) {
                    EnumItem item = scanJavaEnum(javaFileContent);
                    item.setType(EnumItem.EnumItemType.I18nError);

                    enumItems.add(item);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                try {
                    fis.close();
                } catch (Exception e) {
                }
            }

        }
    }


    private static EnumItem scanJavaEnum(String fileContent) throws Exception {

        // FileInputStream in = new FileInputStream(filePath);

        ByteArrayInputStream in = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

        EnumItem enumItem = new EnumItem();

        // 读取文件注释
        JavaParser.parse(in).accept(new VoidVisitorAdapter<EnumItem>() {


            public void visit(final EnumDeclaration n, final EnumItem arg) {

                arg.setTypeName(n.getName().asString());


                List<String> keys = new ArrayList<>();
                Map<String, String> defaultDesc = new LinkedHashMap<>();

                n.getEntries().forEach(p -> {

                    if (arg.getTypeName().equals("AcceptanceAbnormalCauseEnum")) {
                        log.debug("xx");
                    }

                    keys.add(p.getName().asString());

                    List<Expression> pargs = p.getArguments().stream().filter(node -> node instanceof StringLiteralExpr).collect(Collectors.toList());

                    defaultDesc.put(p.getName().asString(), pargs.get(0).toString().replaceAll("\"", ""));
                });

                if (arg.getTypeName().equals("AcceptanceAbnormalCauseEnum")) {
                    log.debug("xx");
                }

                arg.setKeys(keys);
                arg.setDefaultDesc(defaultDesc);

            }

        }, enumItem);

        in.close();

        return enumItem;
    }
}
