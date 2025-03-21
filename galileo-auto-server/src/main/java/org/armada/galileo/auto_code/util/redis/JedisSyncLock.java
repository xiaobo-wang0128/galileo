package org.armada.galileo.auto_code.util.redis;

import org.armada.galileo.common.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;


//@Service
public class JedisSyncLock {

    private Logger log = LoggerFactory.getLogger(JedisSyncLock.class);

    private List<String> cacheLockedKey = new ArrayList<String>();

    private String localHostIp = null;

    // 分布式锁默认过期时间 (秒)
    private long defaultExpireTime = 10;

    // 加锁失败后等待时间 (ms)
    private long lockIntervalTime = 5000;

    @Autowired
    private JedisUtil jedisUtil;


    @PostConstruct
    public void init(){
        new Thread() {
            public void run() {
                while (true) {
                    synchronized (log) {
                        if (cacheLockedKey != null && cacheLockedKey.size() > 0) {
                            for (String key : cacheLockedKey) {
                                jedisUtil.expire(key, defaultExpireTime);
                            }
                        }
                    }
                    try {
                        Thread.sleep(defaultExpireTime / 2);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        break;
                    }
                }
            }
        }.start();

        List<String> list = CommonUtil.getLocalIpAddress();
        if (list != null && list.size() > 0) {
            localHostIp = list.get(0);
        } else {
            localHostIp = "value";
        }
    }

    public void lock(CacheType cacheType, String subKey) {
        lock(cacheType, subKey, 0);
    }

    /**
     * 加锁
     *
     * @param cacheType
     * @param subKey
     * @param retryTimes
     */
    public void lock(CacheType cacheType, String subKey, int retryTimes) {

        if (retryTimes < 0) {
            retryTimes = 0;
        }

        String redisKey = new StringBuilder(cacheType.toString()).append("_").append(subKey).toString();

        int time = 0;
        while (true) {

            boolean success = false;

            try {
                success = jedisUtil.setnxex(cacheType, subKey, localHostIp, defaultExpireTime);
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            if (!success) {
                if (time >= retryTimes) {
                    throw new JedisLockException("[JedisSyncLock] 加锁失败");
                }

                try {
                    Thread.sleep(lockIntervalTime);
                } catch (Exception exx) {
                }
                time++;
                log.info("[JedisSyncLock] 重试第{}次，", time);
                continue;
            }

            break;
        }

        synchronized (log) {
            cacheLockedKey.add(redisKey);
        }

    }

    public void unlock(CacheType cacheType, String subKey) {
        synchronized (log) {
            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(subKey).toString();
            cacheLockedKey.remove(redisKey);
            jedisUtil.del(cacheType, subKey);
        }
    }

    public boolean isLocked(CacheType cacheType, String subKey) {
        String lock = jedisUtil.get(cacheType, subKey);
        return lock != null;
    }
}