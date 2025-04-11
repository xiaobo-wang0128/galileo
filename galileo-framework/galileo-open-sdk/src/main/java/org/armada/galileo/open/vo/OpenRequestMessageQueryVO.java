package org.armada.galileo.open.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.armada.galileo.annotation.openapi.ApiRole;

import java.util.List;

/**
 * <p>
 * 开放平台接口请求日志
 * </p>
 *
 * @author
 * @since 2023-02-03
 */
@Data
@Accessors(chain = true)
public class OpenRequestMessageQueryVO {

    /**
     * 调用方
     */
    private ApiRole apiFrom;

    /**
     * 被调用方
     */
    private ApiRole apiTo;

    /**
     * 开始时间1
     */
    private Long happenTime1;

    /**
     * 开始时间2
     */
    private Long happenTime2;

    /**
     * 状态 success fail doing
     */
    private String status;

    /**
     * 关键字
     */
    private String keyword;

    /**
     * 调用接口名
     */
    private String apiUrl;

    /**
     * 最大重试次数
     */
    private Integer maxRetryTime;

    /**
     * 是否为异步请求 Y N
     */
    private String isAsync;

    /**
     * 最后更新时间, 查询小于这个时间的记录
     */
    private Long lastUpdateTime;

    /**
     * 消息分组字段
     */
    private List<String> groups;

    private Boolean queryForNotify;

}
