package test.code_check;

import org.armada.galileo.common.util.CommonUtil;

/**
 * @author xiaobo
 * @date 2022/2/10 8:57 下午
 */
public class Md5Test {
    public static void main(String[] args) {

        try {
            test();
        } catch (Exception e) {
            System.out.println("error");
            e.printStackTrace();
        }
    }

    public static void test() {

        try {
            throw new RuntimeException("aaa");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {

        }
    }
}
