package org.armada.galileo.sample.nova_flow;


/**
 * @author xiaobo
 * @date 2021/10/8 8:06 下午
 */
public enum FlowEnum {

    /*** ------------------ 入库 ------------------ ***/
    /**
     * 入库
     */
    Inbound,

    /**
     * 上架
     */
    Putaway,


    /*** ------------------ 出库 ------------------ ***/
    /**
     * 客户出库单
     */
    CustomerOutbound,

    /**
     * algo 出库单
     */
    AlgoOutbound,

    /**
     * 拣选任务单
     */
    PickupOutbound,

    /**
     * 空箱出库
     */
    EmptyContainerOutbound,

    /**
     * 盘点
     */
    StockTake,
    ;

//
//    /**
//     * 流程定义 classpath 文件路径
//     */
//    private String[] xmlPaths;
//
//
//    private String bizClassName;
//
//    private String bizStatusFieldName;
//
//
//    public String[] getXmlPaths() {
//        return xmlPaths;
//    }
//
//
//    /**
//     * @param statusField 主业务对象状态字段名，格式 class->field
//     * @param xmlPaths
//     */
//    private FlowEnum(String statusField, String[] xmlPaths) {
//        this.xmlPaths = xmlPaths;
//
//        int index = statusField.indexOf("->");
//        if (index == -1) {
//            throw new RuntimeException("配置不正确");
//        }
//
//        this.bizClassName = statusField.substring(0, index);
//        this.bizStatusFieldName = statusField.substring(index + 2);
//    }
//
//
//    public String getBizClassName() {
//        return bizClassName;
//    }
//
//    public String getBizStatusFieldName() {
//        return bizStatusFieldName;
//    }
}
