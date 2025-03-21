package maven;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.mvc_plus.util.VelocityUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author xiaobo
 * @date 2022/12/3 18:41
 */
public class MavenCreate {

    public static String root = "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git";

    static String tpm = CommonUtil.readFileToString("/Users/wangxiaobo/project/_codes/aml_2022/galileo/galileo-start/src/test/java/maven/pom_tpl.vm.xml");

    static List<String> finalJars = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        List<String> folders = CommonUtil.asList(
                "/bronze-common",
                "/bronze-common/bronze-common-config",
                "/bronze-common/bronze-common-model",
                "/bronze-common/bronze-common-util",

                // wms
                "/bronze-wms",
                "/bronze-wms/wms-web",
                "/bronze-wms/wms-config",

                "/bronze-wms/wms-base/wms-base-spi",
                "/bronze-wms/wms-base/wms-base-dal",
                "/bronze-wms/wms-base/wms-base-service",

                "/bronze-wms/wms-inbound/wms-inbound-spi",
                "/bronze-wms/wms-inbound/wms-inbound-dal",
                "/bronze-wms/wms-inbound/wms-inbound-service",


                "/bronze-wms/wms-outbound/wms-outbound-spi",
                "/bronze-wms/wms-outbound/wms-outbound-dal",
                "/bronze-wms/wms-outbound/wms-outbound-service",

                "/bronze-wms/wms-stock/wms-stock-spi",
                "/bronze-wms/wms-stock/wms-stock-dal",
                "/bronze-wms/wms-stock/wms-stock-service",


                // oms
                "/bronze-oms",
                "/bronze-oms/oms-web",
                "/bronze-oms/oms-config",

                "/bronze-oms/oms-order/oms-order-spi",
                "/bronze-oms/oms-order/oms-order-dal",
                "/bronze-oms/oms-order/oms-order-service",

                "/bronze-oms/oms-stock/oms-stock-spi",
                "/bronze-oms/oms-stock/oms-stock-dal",
                "/bronze-oms/oms-stock/oms-stock-service",


                // fms
                "/bronze-fms",
                "/bronze-fms/fms-web",
                "/bronze-fms/fms-config",

                "/bronze-fms/fms-financial/fms-financial-spi",
                "/bronze-fms/fms-financial/fms-financial-dal",
                "/bronze-fms/fms-financial/fms-financial-service",

                "/bronze-fms/fms-product/fms-product-spi",
                "/bronze-fms/fms-product/fms-product-dal",
                "/bronze-fms/fms-product/fms-product-service",


                //mms
                "/bronze-mms/mms-web",
                "/bronze-mms/mms-service",
                "/bronze-mms/mms-spi",
                "/bronze-mms/mms-dal",


                //srm
                "/bronze-srm/srm-web",
                "/bronze-srm/srm-service",
                "/bronze-srm/srm-spi",
                "/bronze-srm/srm-dal",


                //ums
                "/bronze-ums/ums-web",
                "/bronze-ums/ums-service",
                "/bronze-ums/ums-spi",
                "/bronze-ums/ums-dal",


                //tms
                "/bronze-tms/tms-web",
                "/bronze-tms/tms-service",
                "/bronze-tms/tms-spi",
                "/bronze-tms/tms-dal"


        );


        for (String folder : folders) {
            String path = root + folder;
            CommonUtil.makeSureFolderExists(path + "/pom.xml");

        }

        checkFolder(root);

        for (String finalJar : finalJars) {

            if (finalJar.endsWith("-web")) {
                continue;
            }
            System.out.println("<dependency>");
            System.out.println("<groupId>com.iml</groupId>");
            System.out.println("<artifactId>" + finalJar + "</artifactId>");
            System.out.println("<version>${bronze.version}</version>");
            System.out.println("</dependency>");


        }


    }


    public static void checkFolder(String path) throws Exception {

        if (path.indexOf("/.git/") != -1 || path.indexOf(".idea") != -1) {
            return;
        }

        File file = new File(path);

        if (file.isFile()) {
            return;
        }

        if (file.getName().equals("bronze-module")) {
            return;
        }

        Map<String, Object> map = new HashMap<>();

        map.put("util", new CommonUtil());

        map.put("artifactId", file.getName());


        File[] subFiles = file.listFiles();

        boolean isPom = false;
        List<String> subNames = null;
        if (subFiles != null && subFiles.length > 0) {

            subNames = Stream.of(subFiles).filter(e -> !"pom.xml".equals(e.getName())).map(e -> e.getName()).collect(Collectors.toList());

            if (subNames != null && subNames.size() > 0) {
                isPom = true;
            }
            for (File listFile : file.listFiles()) {
                checkFolder(listFile.getAbsolutePath());
            }
        }


        if (isPom) {
            map.put("packaging", "pom");
            map.put("modules", subNames);
        } else {
            map.put("packaging", "jar");
        }

        String modelName = file.getParentFile().getName();
        String parentId = file.getParentFile().getName();
        if (parentId.equals("bronze.git")) {
            parentId = "bronze";
        }
        if (modelName.startsWith("bronze-")) {
            modelName = modelName.substring(modelName.indexOf("-") + 1);
        }

        map.put("parentId", parentId);

        if (!isPom) {
            String name = file.getName();
            List<Dependency> deps = new ArrayList<>();

            if (name.endsWith("-dal")) {
                deps.add(new Dependency("com.iml", "bronze-common-model"));
                deps.add(new Dependency("com.iml", "bronze-common-config"));
                deps.add(new Dependency("com.iml", modelName + "-spi"));
            }

            if (name.endsWith("-service")) {
                deps.add(new Dependency("com.iml", modelName + "-dal"));
            }

            if (name.endsWith("-web")) {
                deps.add(new Dependency("com.iml", modelName + "-service"));
            }

            map.put("dependencies", deps);

        }


        if (path.equals(root)) {
            return;
        }

        String context = VelocityUtil.render(tpm, map);

        String targetPath = path + "/pom.xml";

        FileOutputStream fos = new FileOutputStream(targetPath);

        fos.write(context.getBytes(StandardCharsets.UTF_8));

        fos.flush();
        fos.close();

        if (!isPom) {
            finalJars.add(file.getName());
        }

    }


    @Data
    @AllArgsConstructor
    public static class Dependency {
        String groupId;
        String artifactId;
    }
}
