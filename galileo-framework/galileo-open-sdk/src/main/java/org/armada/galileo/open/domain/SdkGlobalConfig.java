package org.armada.galileo.open.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaobo
 * @date 2021/11/16 11:14 上午
 */
@Data
@Accessors(chain = true)
public class SdkGlobalConfig {


    public SdkGlobalConfig() {
        this.hideUnOpen = false;
        this.customerToHaiq = new GlobalConfig();
        this.haiqToCustomer = new GlobalConfig();
    }

    /**
     * 隐藏菜单栏未开启的接口
     */
    private Boolean hideUnOpen;

    /**
     * customer -> haiq 接口配置
     */
    private GlobalConfig customerToHaiq;

    /**
     * haiq -> customer 接口配置
     */
    private GlobalConfig haiqToCustomer;

    @Data
    @Accessors(chain = true)
    public static class GlobalConfig {

        public GlobalConfig(){
            this.asyncApis = new ArrayList<>(0);
            this.apiInterface = new ArrayList<>(0);
        }
        /**
         * 可以异常调用的接口
         */
        private List<String> asyncApis;

        /**
         * 开启的接口
         */
        private List<String> apiInterface;

        /**
         * 调用 方式 http sdk ...
         */
        private String callType;

    }

}
