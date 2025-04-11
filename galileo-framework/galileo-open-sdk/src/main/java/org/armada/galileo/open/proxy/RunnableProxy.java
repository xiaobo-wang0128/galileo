package org.armada.galileo.open.proxy;

/**
 * 针对 ftp db sokct 连接方式的代码层接口
 *
 * @author xiaobo
 * @date 2021/12/31 4:12 下午
 */
public interface RunnableProxy extends Runnable {

    /**
     * 需要在此方法中做资源回收，否则多次保存后，会造成资源无法重新分配或内存溢出等现象
     */
    public void beforeDestroy();

}
