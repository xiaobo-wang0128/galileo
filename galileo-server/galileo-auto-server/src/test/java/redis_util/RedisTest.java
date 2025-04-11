package redis_util;

import org.armada.galileo.AutoServer;
import org.armada.galileo.auto_code.util.redis.CacheType;
import org.armada.galileo.auto_code.util.redis.JedisUtil;
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {AutoServer.class})
public class RedisTest {


}
