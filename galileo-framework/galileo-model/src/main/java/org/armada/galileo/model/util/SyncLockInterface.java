package org.armada.galileo.model.util;

import org.armada.galileo.model.constant.CacheType;

/**
 * @author xiaobo
 * @description: TODO
 * @date 2026/6/15 10:55
 */
public interface SyncLockInterface {
    /**
     * 加锁
     *
     * @param cacheType 缓存类型
     * @param subKey    缓存key
     * @return
     */
    public boolean lock(CacheType cacheType, String subKey);


    /**
     * 加锁
     *
     * @param cacheType  缓存类型
     * @param subKey     缓存key
     * @param lockValue  锁定值
     * @param retryTimes 重试次数
     * @return
     */
    public boolean lock(CacheType cacheType, String subKey, String lockValue, int retryTimes);


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
    public boolean lockExpire(CacheType cacheType, String subKey, String lockValue, int retryTimes, long expireTime);

    /**
     * 解锁， 直接删 key
     *
     * @param cacheType
     * @param subKey
     */
    public void unlock(CacheType cacheType, String subKey);

    /**
     * 取消 key 的过期时间自动延长机制
     *
     * @param cacheType
     * @param subKey
     */
    public void cancelExpire(CacheType cacheType, String subKey);


    /**
     * 加缓存
     *
     * @param cacheType 缓存类型
     * @param key       缓存key
     * @param value     缓存值
     * @return
     */
    public String set(CacheType cacheType, String key, String value);


    /**
     * 获取缓存值
     *
     * @param cacheType 缓存类型
     * @param key       缓存key
     * @return
     */
    public String get(CacheType cacheType, String key);


}
