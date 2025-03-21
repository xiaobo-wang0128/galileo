package org.armada.galileo.common.lock;

/**
 * @author xiaobo
 * @description: 通用的分布式同步锁接口
 * @date 2022/4/10 1:54 PM
 */
public interface ConcurrentLock {

    /**
     * 加锁. 不要在此方法中抛异常
     *
     * @param lockKey
     * @param lockValue 加锁的时候赋值
     * @return 锁成功需返回 true
     */
    public boolean lock(String lockKey, String lockValue);

    /**
     * 释放锁.  不要在此方法中抛异常
     *
     * @param lockKey
     */
    public void unlock(String lockKey);

    /**
     * 取消 key 的过期时间自动延长机制
     *
     * @param lockKey
     */
    public void cancelExpire(String lockKey);

    /**
     * 获取缓存值
     *
     * @param key
     * @return
     */
    public String getValue(String key);

    /**
     * 设置缓存值
     *
     * @param key
     * @param value
     * @param  expire >0 时有效
     */
    public void setValue(String key, String value, long expire);


}
