package org.armada.spi.param.base;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 容器占用接口参数
 *
 * @author xiaobo
 * @date 2021/4/22 3:58 下午
 */
@Data
@Accessors(chain = true)
public class ContainerBindInput {

    /**
     * 容器号 "拣货车/周转箱条码"
     */
    private String containerCode;

    /**
     * 容器类型 "订单指定容器类型 1：拣货车 2：周转箱  默认1"
     */
    private String containerType;

}
