package org.armada.galileo.open.util.open_api_scan;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.open.util.api_scan.domain.DocumentGenerateTask;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2022/12/21 11:17
 */
@Slf4j
public class OpenApiScan {


//    private DocumentGenerateTask documentGenerateTask;

    private List<String> srcPaths = null;

    // private String pathRegex = null;

    private List<String> apiFiles = null;

    private String uriHead = null;

    private String appDesc = null;

    private String appName = null;

    public OpenApiScan(DocumentGenerateTask documentGenerateTask, List<String> srcPaths) {
        // this.pathRegex = ".*?"+documentGenerateTask.getRootPackage().replaceAll("\\.", "/") + "/.*?/.*?/web/rpc/.*?\\.java";
        this.srcPaths = srcPaths;
        this.apiFiles = new ArrayList<>();
    }

    public void doScan(String scanPath) {

        List<String> webPaths = srcPaths.stream().filter(
                        e -> e.startsWith(scanPath) && (e.endsWith("-spi/src/main/java/") || e.endsWith("-open/src/main/java/"))
                ).collect(Collectors.toList());

        for (String webPath : webPaths) {
            try {
                scanOpenApiInterface(webPath);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        List<String> apiUrlConfigFiels = srcPaths.stream().filter(e -> e.startsWith(scanPath)).collect(Collectors.toList());

        for (String filePath : apiUrlConfigFiels) {
            try {
                scanOpenApiUrlConfig(filePath);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

    }

    public List<String> getApiFiles() {
        return apiFiles;
    }

    public String getUriHead() {
        return uriHead;
    }

    public String getAppDesc() {
        return appDesc;
    }

    public String getAppName() {
        return appName;
    }

    private void scanOpenApiUrlConfig(String path) throws Exception {
        File file = new File(path);

        if (uriHead != null && uriHead != null) {
            return;
        }

        if (file.isFile()) {

            String filePath = file.getPath();


            if (!filePath.endsWith(".java")) {
                return;
            }

            if (!filePath.contains("/src/main/java/")) {
                return;
            }

            FileInputStream in = new FileInputStream(filePath);

            AtomicInteger exist = new AtomicInteger(0);

            JavaParser.parse(in, Charset.forName("utf-8")).accept(new VoidVisitorAdapter<AtomicInteger>() {

                public void visit(final ClassOrInterfaceDeclaration n, AtomicInteger arg) {

                    for (AnnotationExpr annotation : n.getAnnotations()) {
                        if ("OpenApiUrlConfig".equals(annotation.getName().asString())) {

                            arg.incrementAndGet();

                            for (Node childNode : annotation.getChildNodes()) {
                                if (childNode instanceof MemberValuePair) {

                                    MemberValuePair mp = (MemberValuePair) childNode;

                                    String name = mp.getName().asString();
                                    String value = mp.getValue().toString();
                                    if (value != null) {
                                        value = value.replaceAll("\"", "");
                                    }

                                    if ("urlHead".equals(name)) {
                                        OpenApiScan.this.uriHead = value;
                                    }
                                    if ("appName".equals(name)) {
                                        OpenApiScan.this.appName = value;
                                    }
                                    if ("appDesc".equals(name)) {
                                        OpenApiScan.this.appDesc = value;
                                    }

                                }
                            }


                        }
                    }
                }

            }, exist);

        } else {

            for (File listFile : file.listFiles()) {
                scanOpenApiUrlConfig(listFile.getAbsolutePath());
            }
        }
    }

    private void scanOpenApiInterface(String path) throws Exception {
        File file = new File(path);

        if (file.isFile()) {

            String filePath = file.getPath();

            if (!filePath.endsWith(".java")) {
                return;
            }

            if (!filePath.contains("/src/main/java/")) {
                return;
            }

            // String javaContent = new String(CommonUtil.readFileFromLocal(filePath), StandardCharsets.UTF_8);

            FileInputStream in = new FileInputStream(filePath);

            AtomicInteger exist = new AtomicInteger(0);

            JavaParser.parse(in, Charset.forName("utf-8")).accept(new VoidVisitorAdapter<AtomicInteger>() {

                public void visit(final ClassOrInterfaceDeclaration n, AtomicInteger arg) {

                    for (AnnotationExpr annotation : n.getAnnotations()) {
                        if ("OpenApi".equals(annotation.getName().asString())) {
                            arg.incrementAndGet();
                        }
                    }
                }

            }, exist);


            if (exist.get() > 0) {
                apiFiles.add(filePath);
            }

        } else {

            for (File listFile : file.listFiles()) {
                try {
                    scanOpenApiInterface(listFile.getAbsolutePath());
                } catch (Exception e) {
                    log.error("error file:" + listFile.getAbsolutePath());
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

}
