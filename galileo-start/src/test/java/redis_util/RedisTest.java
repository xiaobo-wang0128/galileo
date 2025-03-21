package redis_util;

import org.armada.galileo.StartApplication;
import org.armada.galileo.util.CacheType;
import org.armada.galileo.util.JedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xiaobo
 * @date 2022/12/7 10:44
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {StartApplication.class})
public class RedisTest {

    @Autowired
    private JedisUtil jedisUtil;

    @Test
    public void tes1() {
        jedisUtil.set(CacheType.ApiDocCache, "set", "value");
        jedisUtil.setnxex(CacheType.ApiDocCache, "setnxex", "value", 1200);
    }

}
