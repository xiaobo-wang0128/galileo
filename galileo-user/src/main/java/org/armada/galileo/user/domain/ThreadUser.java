package org.armada.galileo.user.domain;


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

}
