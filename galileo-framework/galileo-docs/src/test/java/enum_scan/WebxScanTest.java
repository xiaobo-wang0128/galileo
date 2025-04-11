package enum_scan;

import org.armada.galileo.open.DocScanJob;

/**
 * @author xiaobo
 * @date 2023/1/4 14:07
 */
public class WebxScanTest {
    public static void main(String[] args) throws Exception{

        DocScanJob.main(new String[]{
                "WEBX_URL",
                "/Users/wangxiaobo/project/_codes/vot_2024/galaxy-java",
                "/Users/wangxiaobo/Downloads/api.json",
                "org.vot"
        });


//        DocScanJob.main(new String[]{
//                "ENUM_SCAN",
//                "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git",
//                "/Users/wangxiaobo/Downloads/api.json",
//                "com.iml"
//        });

    }
}
