package enum_scan;

import org.armada.galileo.open.DocScanJob;

/**
 * @author xiaobo
 * @date 2023/1/3 18:12
 */
public class EnumScanJsonTest {

    public static void main(String[] args) throws Exception {


        DocScanJob.main(new String[]{
                "ENUM_SCAN_JSON",
                "/Users/wangxiaobo/project/_codes/vot_2024/galaxy-java",
                "/Users/wangxiaobo/Downloads/_galaxy_java_type_enum_.js",
                "org.vot"

        });

        System.out.println("EnumScanJsonTest done");

    }
}
