package org.armada.galileo.i18n;

/**
 * @author xiaobo
 * @date 2023/1/4 18:23
 */
public class I18nContext {

    private static ThreadLocal<String> local = new ThreadLocal<>();

    public static void setI18nContext(String locale) {
        local.set(locale);
    }

    public static String getI18nContext() {
        return local.get();
    }

    public static void remove() {
        local.remove();
    }
}
