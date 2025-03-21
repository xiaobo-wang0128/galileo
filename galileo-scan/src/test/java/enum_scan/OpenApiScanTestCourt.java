package enum_scan;

import org.armada.galileo.open.DocScanJob;

/**
 * @author xiaobo
 * @date 2023/1/4 14:07
 */
public class OpenApiScanTestCourt {
    public static void main(String[] args) throws Exception {


        DocScanJob.main(new String[]{
                "OPEN_API",
                "/Users/wangxiaobo/project/_codes/law_project_2023/court_java",
                "/Users/wangxiaobo/project/_codes/law_project_2023/court_java",
                "/Users/wangxiaobo/project/_codes/law_project_2023/court_java/court-start/src/main/resources/statics/open_api.json",
                "vv.armada"
        });



    }
}
