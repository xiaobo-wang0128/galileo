package org.armada.galileo.model.domain;


public class ThreadUser {

    private static ThreadLocal<LoginUser> local = new ThreadLocal<LoginUser>();

    public static void set(LoginUser u) {
        local.set(u);
    }

    public static LoginUser get() {
        return local.get();
    }

    public static void remove() {
        local.remove();
    }

    /**
     * 设置用户上下文贷代id - 仅定时任务中使用
     *
     * @param tenantId
     */
    public static void setCurrentTenantId(Long tenantId) {
        LoginUser u = get();
        if (u == null) {
            u = new LoginUser();
        }
        u.setTenantId(tenantId);
        u.setUserName("system");
        set(u);
    }

}
