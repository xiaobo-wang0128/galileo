package test.simple_test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 新 op 推荐解
 *
 * @author xiaobo
 * @date 2021/12/20 11:31 下午
 */
@Data
@Accessors(chain = true)
public class AlgoOrderResult implements Serializable {

    /**
     * 算法订单头id
     */
    private Long algoOrderId;

    /**
     * 分配结果
     */
    private List<AlgoDetailResult> algoDetailResults;


    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AlgoDetailResult  implements Serializable{

        /**
         * algo 明细id
         */
        private Long algoDetailId;

        /**
         * algo 明细的库存命中、操作台分配结果
         */
        private List<PickTaskItem> pickTaskItems;

    }

    @Data
    @Accessors(chain = true)
    public static class PickTaskItem  implements Serializable{

        /**
         * algo 明细里的 skuId
         */
        private Long skuId;

        /**
         * 批次id
         */
        private Long skuBatchId;

        /**
         * 命中的容器 id
         */
        private Long containerId;

        /**
         * 命中的容器 code
         */
        private String containerCode;

        /**
         * 命中的子容器 id
         */
        private Long subContainerId;

        /**
         * 命中的子容器 code
         */
        private String subContainerCode;

        /**
         * 命中的数量
         */
        private Integer matchedQty;

        /**
         * 被分配的`操作台Code`
         */
        private String stationCode;

        /**
         * 被分配的`播种墙格口`
         */
        private String stationSlot;
    }

}
