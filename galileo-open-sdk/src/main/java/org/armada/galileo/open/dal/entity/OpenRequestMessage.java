package org.armada.galileo.open.dal.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.armada.galileo.annotation.openapi.ApiRole;
import org.armada.galileo.es.annotation.EsIndex;
import org.armada.galileo.es.entity.EsBaseEntity;
import org.armada.galileo.open.constant.RequestErrorType;
import org.armada.galileo.open.constant.RequestMessageStatus;

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
public class OpenRequestMessage implements EsBaseEntity {

    @Override
    public String getIdValue() {
        return requestId;
    }


    /**
     * 请求号（uuid）
     */
    @EsIndex
    private String requestId;

    /**
     * 接口地址
     */
    @EsIndex
    private String apiUrl;

    /**
     * 接口名
     */
    private String apiName;

    /**
     * 发生时间
     */
    @EsIndex
    private Long happenTime;

    /**
     * 调用方
     */
    @EsIndex
    private ApiRole apiFrom;

    /**
     * 被调用方
     */
    @EsIndex
    private ApiRole apiTo;

    /**
     * 是否为异步 Y N
     */
    @EsIndex
    private String isAsync;

    /**
     * 通信状态 doing 通信中, success 成功, fail 通信失败
     */
    @EsIndex
    private RequestMessageStatus status;

    /**
     * 业务执行结果 io IO异常 biz 业务异常
     */
    private RequestErrorType errorType;

    /**
     * 请求方输入数据包大小 byte
     */
    private Integer requestSize;

    /**
     * 响应数据包大小 byte
     */
    private Integer responseSize;

    /**
     * 请求数据
     */
    @EsIndex(fullIndex = true)
    private String requestJson;

    /**
     * 响应数据
     */
    private String responseJson;

    /**
     * 接口耗时
     */
    private Integer timeCost;

    /**
     * 重试次数
     */
    @EsIndex
    private Integer retryTime;

    /**
     * 最后更新时间
     */
    @EsIndex
    private Long updateTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 消息分组code
     */
    @EsIndex
    private String msgGroup;

    /**
     * 消息分组排序
     */
    @EsIndex
    private Integer msgSort;


}
