package org.armada.galileo.open.util.i18n_scan;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.open.util.api_scan.SourcePathScan;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2022/12/25 17:06
 */
@Slf4j
public class EnumJsonScan {


    public static void doScan(String projectPath, String targetJsonFilePath) throws Exception {

        List<String> srcPaths = new SourcePathScan(projectPath).generateSrcPath();

        List<String> enumFileContents = new ArrayList<>();

        List<EnumItem> allEnumItems = new ArrayList<>();

        for (String srcPath : srcPaths) {
            scanEnumFiles(enumFileContents, allEnumItems, new File(srcPath));
        }


        LinkedHashMap map = new LinkedHashMap();
        for (EnumItem enumItem : allEnumItems) {
            if (EnumItem.EnumItemType.I18nDictionary == enumItem.getType()) {
                map.put(enumItem.getTypeName(), enumItem.getDefaultDesc());
            }
        }


        FileOutputStream fos = new FileOutputStream(targetJsonFilePath);

        String outputJson = JsonUtil.toJson(map);

        String jsContent = "var __JAVA_ENUM_CONFIGS__=" + outputJson;

        fos.write(jsContent.getBytes(StandardCharsets.UTF_8));

        fos.close();

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
