package org.armada.galileo.nova_flow.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 操作类型
 *
 * @author xiaobowang 2019年3月28日
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Action {

    /**
     * 操作名称
     */
    private String name;

    /**
     * manual 手动， auto 自动（会在上一个action结束后，自动执行当前action）, task 定时任务
     */
    private String type;

    /**
     * task 类型的 定时任务时间表达式
     */
    private String expression;

    /**
     * 编码
     */
    private String code;

    /**
     * 执行业务类（可能有多个）
     */
    private List<ActionClassImpl> actionClassImpls;

    /**
     * 业务执行类（当前生效的）
     */
    private String activeClass;

    /**
     * 允许进行该操作的状态（ 除 beforeStatus 外的其他状态, 这些状态下进行此操作 不会引起业务对象状态的变化）
     */
    private List<String> allowStatus;

    /**
     * 如果该状态要进行状态变更，它的前置状态必须是什么？
     */
    private String beforeStatus;

    /**
     * 如果操作执行成功，目标状态是什么？
     */
    private String targetStatus;

    /**
     * 如果执行失败，需要变更成什么状态
     */
    private String exceptionStatus;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActionClassImpl {

        /**
         * 实现类的名称
         */
        private String name;

        /**
         * 实现类的详情描述
         */

        private String desc;

        /**
         * 目标实现类 classname
         */
        private String className;

    }

}
