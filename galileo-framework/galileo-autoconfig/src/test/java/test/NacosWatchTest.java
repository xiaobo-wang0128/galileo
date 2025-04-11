package test;

import org.armada.galileo.autoconfig.NacosConfig;
import org.armada.galileo.autoconfig.NacosUtil;
import org.armada.galileo.common.util.JsonUtil;

import java.util.List;

/**
 * @author xiaobo
 * @date 2023/2/21 10:52
 */
public class NacosWatchTest {

    public static void main(String[] args) throws Exception {

        NacosUtil nacosUtil = new NacosUtil("localhost:8848", "test_file");

        nacosUtil.watchFormValueChange(new NacosUtil.NacosListener() {
            @Override
            public void receive(List<NacosConfig> nacosConfigs) {

                System.out.println(JsonUtil.toJson(nacosConfigs));
            }
        });

        Thread.sleep(Integer.MAX_VALUE);
    }
}
