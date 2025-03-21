package maven;

import org.armada.galileo.common.util.CommonUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2022/12/3 19:44
 */
public class GenateSrcPath {


    public static void main(String[] args) {

        generateSrc(MavenCreate.root);
        String p = "/Users/wangxiaobo/project/_codes/aml_2022/galileo/galileo-start/src/test/java";

    }


    public static void generateSrc(String path) {

        File f = new File(path);

        File[] subFiles = f.listFiles();
        if (subFiles != null) {

            if (subFiles.length == 1 && subFiles[0].getName().equals("pom.xml")) {

                String srcPath = path + "/src/test/java";

                String folder = f.getName();

                String fname = f.getName();

                if (fname.indexOf("-") != -1) {
                    fname = fname.substring(0, fname.indexOf("-"));
                }

                List<String> cfs = new ArrayList<>();


                if (path.endsWith("-config")) {
                    cfs.add(path + "/src/main/java/com/iml/" + fname + "/config/");
                }


                List<String> subModels = new ArrayList<>();
                if ("wms-web".equals(folder)) {
                    subModels = CommonUtil.asList("inbound", "outbound", "stock", "base", "sku", "warehouse", "container", "station", "print");
                } else if (folder.startsWith("wms-stock-")) {
                    subModels = CommonUtil.asList("stock", "stocktake", "movement");
                } else if (folder.startsWith("wms-outbound-")) {
                    subModels = CommonUtil.asList("outbound");
                } else if (folder.startsWith("wms-inbound-")) {
                    subModels = CommonUtil.asList("inbound", "putaway");
                } else if (folder.startsWith("wms-base-")) {
                    subModels = CommonUtil.asList("sku", "warehouse", "container", "station", "print");
                } else if (folder.startsWith("ums-")) {
                    subModels = CommonUtil.asList("user", "role", "customer", "manager");
                }

                else if (folder.startsWith("tms-")) {
                    subModels = CommonUtil.asList("truck", "transport");
                }

                else if (folder.startsWith("srm-")) {
                    subModels = CommonUtil.asList("supplier", "openapi");
                }

                else if (folder.startsWith("mms-")) {
                    subModels = CommonUtil.asList("sku");
                }

                // oms
                else if ("oms-web".equals(folder)) {
                    subModels = CommonUtil.asList("order", "stock" );
                }
                else if (folder.startsWith("oms-order-")) {
                    subModels = CommonUtil.asList("order");
                } else if (folder.startsWith("oms-stock-")) {
                    subModels = CommonUtil.asList("stock", "sku");
                }

                // fms
                else if ("fms-web".equals(folder)) {
                    subModels = CommonUtil.asList("product", "financial" );
                }
                else if (folder.startsWith("fms-product-")) {
                    subModels = CommonUtil.asList("product");
                } else if (folder.startsWith("fms-financial")) {
                    subModels = CommonUtil.asList("financial");
                }


                if (path.endsWith("-web")) {
                    for (String subModel : subModels) {
                        cfs.add(path + "/src/test/java/com/iml/" + fname);
                        cfs.add(path + "/src/main/java/com/iml/" + fname + "/" + subModel + "/vo");
                        cfs.add(path + "/src/main/java/com/iml/" + fname + "/" + subModel + "/web/rpc");
                        // cfs.add(path + "/src/main/java/com/iml/" + fname + "/" + subModel + "/web/screen");
                    }

                }

                if (path.endsWith("-dal")) {
                    for (String subModel : subModels) {
                        cfs.add(path + "/src/main/java/com/iml/" + fname + "/" + subModel + "/dal/entity");
                        cfs.add(path + "/src/main/java/com/iml/" + fname + "/" + subModel + "/dal/mapper");
                        cfs.add(path + "/src/main/java/com/iml/" + fname + "/" + subModel + "/dal/domain");
                    }
                }


                if (path.endsWith("-service")) {
                    for (String subModel : subModels) {
                        cfs.add(path + "/src/main/java/com/iml/" + fname + "/" + subModel + "/bo/impl");
                        cfs.add(path + "/src/main/java/com/iml/" + fname + "/" + subModel + "/api/impl");
                        cfs.add(path + "/src/main/java/com/iml/" + fname + "/" + subModel + "/schedule");
                        cfs.add(path + "/src/main/java/com/iml/" + fname + "/" + subModel + "/service/impl");
                        cfs.add(path + "/src/main/java/com/iml/" + fname + "/" + subModel + "/util");
                    }
                }

                if (path.endsWith("-spi")) {
                    for (String subModel : subModels) {
                        cfs.add(path + "/src/main/java/com/iml/" + fname + "/" + subModel + "/constant");
                        cfs.add(path + "/src/main/java/com/iml/" + fname + "/" + subModel + "/dto");
                        cfs.add(path + "/src/main/java/com/iml/" + fname + "/" + subModel + "/api");
                    }
                }


                cfs = cfs.stream().map(e -> e + "/ddd").collect(Collectors.toList());

                for (String cf : cfs) {
                    CommonUtil.makeSureFolderExists(cf);
                }


            } else {
                for (File subFile : subFiles) {
                    generateSrc(subFile.getAbsolutePath());
                }
            }
        }


    }

}
