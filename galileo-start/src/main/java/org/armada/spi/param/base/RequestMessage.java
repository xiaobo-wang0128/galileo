package org.armada.spi.param.base;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author xiaobo
 * @date 2021/6/18 10:12 上午
 */

@Data
@Accessors(chain = true)
public class RequestMessage {

    /**
     * 业务请求号
     */
    private String requestNo;

    /**
     * 发生时间
     */
    private Date happenTime;

    /**
     * 通信状态 doing 通信中, success 成功, fail 通信失败
     */
    private String status;

    /**
     * 业务执行结果 success 成功  fail 失败
     */
    private String bizStatus;

    /**
     * 通信传输类型: send 发送请求 receive 接收请求
     */
    private String transferType;

    /**
     * 调用接口名
     */
    private String apiServiceName;

    /**
     * 调用方法名
     */
    private String apiServiceMethod;

    /**
     * 请求方输入数据包大小 byte
     */
    private Integer requestByteSize;

    /**
     * 响应数据包大小 byte
     */
    private Integer responseByteSize;

    /**
     * 请求数据 json
     */
    private String requestJson;

    /**
     * 响应数据 json
     */
    private String responseJson;

    /**
     * 接口调用总耗时 ms
     */
    private Integer timeCost;

    /**
     * 异常信息
     */
    private String errorMessage;


}
