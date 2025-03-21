package test.simple_test;

import java.util.Enumeration;

/**
 * @author xiaobo
 * @date 2022/2/13 6:21 下午
 */
public class PrintEnv {
    public static void main(String[] args) throws Exception {

        Enumeration en = System.getProperties().propertyNames();

        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            System.out.println(key + "\t\t\t" + System.getProperty(key));
        }


    }
}
