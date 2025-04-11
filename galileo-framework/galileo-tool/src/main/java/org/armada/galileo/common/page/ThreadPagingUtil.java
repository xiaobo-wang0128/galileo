package org.armada.galileo.common.page;

public class ThreadPagingUtil {

    private static ThreadLocal<PageParam> local = new InheritableThreadLocal<PageParam>();

    // /**
    // * 获取当前分页开关状态
    // *
    // * @return
    // */
    // public static boolean isOpen() {
    // PageParamObject pageParam = local.get();
    // if (pageParam == null) {
    // return false;
    // }
    //
    // if (pageParam.getOpenPage() != null && pageParam.getOpenPage()) {
    // return true;
    // }
    // return false;
    // }

    /**
     * 获取当前分页对象
     *
     * @return
     */
    public static PageParam get() {
        return local.get();
    }

    /**
     * 开启分页，线程内有效
     */
    public static void turnOn() {
        PageParam pageParam = local.get();
        if (pageParam == null) {
            pageParam = PageParam.instanceByPageIndex(1, 20);
            local.set(pageParam);
        }
        pageParam.setOpenPage(true);
    }


    /**
     * 开启分页，线程内有效
     */
    public static void turnOn(String countField) {
        PageParam pageParam = local.get();
        if (pageParam == null) {
            pageParam = PageParam.instanceByPageIndex(1, 20);
            local.set(pageParam);
        }
        pageParam.setCountField(countField);
        pageParam.setOpenPage(true);
    }

    /**
     * 通过参数开启分页，线程内有效
     */
    public static void pagingSwitch(Boolean flag) {
        if (flag) {
            turnOn();
        }
    }

    /**
     * 设置分页参数
     *
     * @param pageParam
     */
    public static void set(PageParam pageParam) {
        local.set(pageParam);
    }

    /**
     * 清除
     */
    public static void clear() {
        local.remove();
    }
}
