package test.nacos;

import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;

import java.util.Properties;

/**
 * @author xiaobo
 * @date 2021/12/29 4:57 下午
 */
public class NacosNameTest {

    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        properties.setProperty("serverAddr", "bronze-nacos:8848");
        // properties.setProperty("namespace", "test_name_sapce");

        NamingService naming = NamingFactory.createNamingService(properties);

        String ip = CommonUtil.getRandomNumber(2) + "." + CommonUtil.getRandomNumber(2) + "." + CommonUtil.getRandomNumber(2) + "." + CommonUtil.getRandomNumber(2);

        naming.registerInstance("com.xxxx", ip, 8888, "default");

        // naming = NamingFactory.createNamingService(properties);

        naming.registerInstance("com.aaa", ip, 9999, "default");

        System.out.println("##############");
        System.out.println(naming.getAllInstances("com.xxxx"));

        //naming.deregisterInstance("com.aaa", ip, 9999, "DEFAULT");

        System.out.println("##############");
        //System.out.println(naming.getAllInstances("com.aaa"));



        System.out.println("##############");
        System.out.println("##############");
        System.out.println("##############");
        System.out.println("##############");
        naming.subscribe("com.aaa", new EventListener() {
            @Override
            public void onEvent(Event event) {
                for (Instance instance : ((NamingEvent) event).getInstances()) {
                    System.out.println(JsonUtil.toJson(instance));
                }
            }
        });

        Thread.sleep(Integer.MAX_VALUE);
    }
}
