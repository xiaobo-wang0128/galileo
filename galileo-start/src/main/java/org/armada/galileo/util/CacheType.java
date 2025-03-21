package org.armada.galileo.util;

public enum CacheType {

    /**
     * 文书签章
     */
    ApiDocCache

    ;

    public String toString() {
        return "api_" + super.toString();
    }
}
