package org.armada.galileo.user.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author xiaobo
 * @date 2022/2/7 12:46 下午
 */
@Data
@Accessors(chain = true)
public class LoginLogSearchVO {

    /**
     * 用于模糊搜索的关键字
     */
    private String keyword;

    /**
     * 类型 login 登陆, logout 登出
     */
    private String type;

    /**
     * 时间 1
     */
    private Date happenTime1;
    /**
     * 时间 2
     */
    private Date happenTime2;

    /**
     * 分布参数， 从1开始
     */
    private Integer pageIndex = 1;

    /**
     * 每页显示数量, -1 表示不分页
     */
    private Integer pageSize = 20;

    /**
     * 排序字段
     */
    private String orderby;

}
