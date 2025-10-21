package enum_scan;

import org.armada.galileo.open.DocScanJob;

/**
 * @author xiaobo
 * @date 2023/1/4 14:07
 */
public class OpenApiScanTest {
    public static void main(String[] args) throws Exception {

        DocScanJob.main(new String[]{
                "OPEN_API",
                "/Users/wangxiaobo/project/_codes/vot_2024/galaxy-java",
                "/Users/wangxiaobo/project/_codes/vot_2024/galaxy-java/galaxy-dts",
                "/Users/wangxiaobo/project/_codes/vot_2024/galaxy-java/galaxy-dts/dts-web/src/main/resources/open_api.json",
                "org.vot"
        });




//        DocScanJob.main(new String[]{
//                "/Users/wangxiaobo/project/_codes/aml_2022/galileo",
//                "/Users/wangxiaobo/project/_codes/aml_2022/galileo/galileo-open-test1/src/main/resources/api_doc.json",
//                "com.iml",
//                "OPEN_API"
//        });

//        DocScanJob.main(new String[]{
//                "OPEN_API",
//                "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git",
//                "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git/bronze-wms",
//                "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git/bronze-wms/wms-web/src/main/resources/open_api.json",
//                "com.iml",
//        });

        // --------------------------------------------------------

//        DocScanJob.main(new String[]{
//                "OPEN_API",
//                "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git",
//                "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git/bronze-mms",
//                "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git/bronze-mms/mms-web/src/main/resources/open_api.json",
//                "com.iml"
//        });
//
//
//        DocScanJob.main(new String[]{
//                "OPEN_API",
//                "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git",
//                "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git/bronze-fms",
//                "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git/bronze-fms/fms-web/src/main/resources/open_api.json",
//                "com.iml"
//        });

//
//        DocScanJob.main(new String[]{
//                "OPEN_API",
//                "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git",
//                "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git/bronze-ums",
//                "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git/bronze-ums/ums-web/src/main/resources/open_api.json",
//                "com.iml"
//        });


//        DocScanJob.main(new String[]{
//                "OPEN_API",
//                "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git",
//                "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git/bronze-oms",
//                "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git/bronze-oms/oms-web/src/main/resources/open_api.json",
//                "com.iml"
//        });


    }
}
