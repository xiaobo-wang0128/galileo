package compare;

import org.armada.galileo.common.util.CommonUtil;

import java.io.File;

/**
 * @author xiaobo
 * @date 2022/1/17 3:48 下午
 */
public class CompareJava {

    private static String path1 = "/Users/wangxiaobo/Downloads/iwms-station-service-79";

    private static String path2 = "/Users/wangxiaobo/Downloads/iwms-station-service-local";

    public static void main(String[] args) throws Exception {

        compare(new File(path1));

    }

    private static void compare(File f1) throws Exception {

        if (f1.isDirectory()) {
            for (File file : f1.listFiles()) {
                compare(file);
            }
        } else {

            if (f1.getAbsolutePath().startsWith(path1)) {

                String subPath = f1.getAbsolutePath().substring(path1.length());

                File f2 = new File(path2 + subPath);

                if (!f2.exists()) {

                    System.out.println(subPath + " not in " + path2);
                    return;
                }

                byte[] b1 = CommonUtil.readFileFromLocal(f1.getAbsolutePath());
                byte[] b2 = CommonUtil.readFileFromLocal(path2 + subPath);

                String s1 = CommonUtil.sha256(b1);
                String s2 = CommonUtil.sha256(b2);

                if (!s1.equals(s2)) {
                    System.out.println(subPath + " not same");
                }

            }

        }
    }

}
