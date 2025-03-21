package transfer_code_test;

import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.open.util.compile.SdkCompileUtil;

import java.util.List;


/**
 * @author xiaobo
 * @date 2021/12/8 9:37 下午
 */
public class CompileTest {

    public static void main(String[] args) throws Exception {

        String path = "/Users/wangxiaobo/project/_nova/galileo.git/galileo-start/src/test/java/transfer_code_test/CustomerToHaiqHttpInterceptorImpl.java";

        String content = new String(CommonUtil.readFileFromLocal(path), "utf-8");

        List<SdkCompileUtil.CompileResult> list = SdkCompileUtil.compile(content);
        for (SdkCompileUtil.CompileResult c : list) {
            System.out.println(c.getClsName() + " : " + c.getIsInner());
        }

    }


}
