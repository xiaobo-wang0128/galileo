package enum_scan;

import org.armada.galileo.exception.BizException;
import org.armada.galileo.open.DocScanJob;

/**
 * @author xiaobo
 * @date 2023/1/3 18:12
 */
public class EnumScanTest {

    public static void main(String[] args) throws Exception {


        DocScanJob.main(new String[]{
                "ENUM_SCAN",
                "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git",
                "/Users/wangxiaobo/Downloads/enum.json",
                "com.iml"

        });


    }
}
