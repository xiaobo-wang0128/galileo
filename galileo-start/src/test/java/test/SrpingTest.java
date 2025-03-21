package test;

import org.armada.galileo.sample.api.TestApi;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = {StartApplication.class})
public class SrpingTest {

    public static void main(String[] args) {

        int max = 10000000;
        Map<Integer, Boolean> exist = new HashMap<>();

        for (int i = 0; i < max; i++) {
            int num = UUID.randomUUID().toString().hashCode();
            if (num < 0) {
                num = -num;
            }

            if (exist.get(num) != null && exist.get(num)) {
                System.out.println(num + " exist");
            }

            exist.put(num, true);
        }

        System.out.println((float) (max - exist.size()) / (float) max);
    }

    @Autowired
    private ApplicationContext applicationContext;


    @Autowired
    DefaultListableBeanFactory listableBeanFactory;

//    @Test
//    public void test1() throws Exception {
//
////		String appname = applicationContext.getEnvironment().getProperty("spring.application.name");
////
////		System.out.println(appname);
////
//        String beanName = TestApi.class.getName();
//
//        Object obj = applicationContext.getAutowireCapableBeanFactory().createBean(TestApiImpl.class);
//
//        obj = applicationContext.getAutowireCapableBeanFactory().createBean(TestApiImpl2.class);
//
//        listableBeanFactory.registerSingleton(beanName, obj);
//
//
//        TestApi api = (TestApi) applicationContext.getBean(beanName);
//
//        api.test1();
//
//    }

    public void test22() {

        // com.alibaba.druid.pool.DruidDataSource a;

    }


    static DecimalFormat df = new DecimalFormat("###,###.###");

    static DecimalFormat df2 = new DecimalFormat("###,###");

//    public static void main(String[] args) {
//
//        System.out.println((df.format(12341.234234234)));
//
//        System.out.println((df.format(12341.23)));
//
//        System.out.println((df.format(12341.0000)));
//
//
//        System.out.println((df2.format(12341.234234234)));
//
//        System.out.println((df2.format(12341.23)));
//
//        System.out.println((df2.format(12341.0000)));
//
//
//        String stockResult = df.format(12341.0000);
//        System.out.println(stockResult);
//        if(stockResult.endsWith(".0")){
//            stockResult = stockResult.substring(0, stockResult.lastIndexOf("."));
//        }
//        System.out.println(stockResult);
//    }

}
