package org.armada.galileo.common.redis;

import org.armada.galileo.common.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

public class RedisSyncLock {

    private Logger log = LoggerFactory.getLogger(RedisSyncLock.class);

    private List<String> cacheLockedKey = new ArrayList<String>();

    private String localHostIp = null;

    // 分布式锁默认过期时间 (秒)
    private int defaultExpireTime = 10;

    // 加锁失败后等待时间 (ms)
    private long lockIntervalTime = 5000;

    private RedisUtil redisUtil;

    public RedisSyncLock(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    public void init() {
        if (!redisUtil.getIsAvailable()) {
            return;
        }
        new Thread() {
            public void run() {
                while (true) {
                    synchronized (log) {
                        if (cacheLockedKey != null && cacheLockedKey.size() > 0) {
                            for (String key : cacheLockedKey) {
                                redisUtil.expire(key, defaultExpireTime);
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

    /**
     * 加锁
     *
     * @param cacheType 缓存类型
     * @param subKey    缓存key
     * @return
     */
    public boolean lock(CacheType cacheType, String subKey) {
        return lockExpire(cacheType, subKey, null, 0, defaultExpireTime);
    }


    /**
     * 加锁
     *
     * @param cacheType  缓存类型
     * @param subKey     缓存key
     * @param lockValue  锁定值
     * @param retryTimes 重试次数
     * @return
     */
    public boolean lock(CacheType cacheType, String subKey, String lockValue, int retryTimes) {
        return lockExpire(cacheType, subKey, lockValue, retryTimes, defaultExpireTime);
    }


    /**
     * 加锁
     *
     * @param cacheType  缓存类型
     * @param subKey     缓存key
     * @param lockValue  锁定值
     * @param retryTimes 重试次数
     * @param expireTime 锁定时间
     * @return
     */
    public boolean lockExpire(CacheType cacheType, String subKey, String lockValue, int retryTimes, long expireTime) {

        if (!redisUtil.getIsAvailable()) {
            return true;
        }
        if (retryTimes < 0) {
            retryTimes = 0;
        }
        if (lockValue == null) {
            lockValue = localHostIp;
        }
        String redisKey = new StringBuilder(cacheType.toString()).append("_").append(subKey).toString();

        int time = 0;
        while (true) {

            boolean success = false;

            try {
                success = redisUtil.setnxex(cacheType, subKey, lockValue, expireTime);
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            if (!success) {
                if (time >= retryTimes) {
                    log.warn("[RedisSyncLock] 加锁失败, key:" + redisKey);
                    return false;
                }

                try {
                    Thread.sleep(lockIntervalTime);
                } catch (Exception exx) {
                }
                time++;
                log.info("[RedisSyncLock] 重试第{}次，", time);
                continue;
            }

            break;
        }

        synchronized (log) {
            cacheLockedKey.add(redisKey);
        }

        return true;
    }

    /**
     * 解锁， 直接删 key
     *
     * @param cacheType
     * @param subKey
     */
    public void unlock(CacheType cacheType, String subKey) {
        if (!redisUtil.getIsAvailable()) {
            return;
        }

        synchronized (log) {
            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(subKey).toString();
            cacheLockedKey.remove(redisKey);
            redisUtil.del(cacheType, subKey);
        }
    }

    /**
     * 取消 key 的过期时间自动延长机制
     *
     * @param cacheType
     * @param subKey
     */
    public void cancelExpire(CacheType cacheType, String subKey) {
        if (!redisUtil.getIsAvailable()) {
            return;
        }

        synchronized (log) {
            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(subKey).toString();
            cacheLockedKey.remove(redisKey);
        }
    }

    /**
     * redis get
     *
     * @param cacheType
     * @param key
     * @return
     */
    public String get(CacheType cacheType, String key) {
        return redisUtil.get(cacheType, key);
    }

    /**
     * redis set
     *
     * @param cacheType
     * @param key
     * @param value
     */
    public void set(CacheType cacheType, String key, String value) {
        redisUtil.set(cacheType, key, value);
    }

    /**
     * 是否已加锁
     * @param cacheType
     * @param key
     * @return
     */
    public boolean isLocked(CacheType cacheType, String key) {
        return this.get(cacheType, key) != null;
    }
}
