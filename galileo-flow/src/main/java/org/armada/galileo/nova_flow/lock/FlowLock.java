package org.armada.galileo.nova_flow.lock;

/**
 * @author xiaobo
 * @description: TODO
 * @date 2022/4/24 6:37 PM
 */
public interface FlowLock {
    public boolean lock(String lockKey) ;

    public void unlock(String lockKey) ;
}
