package org.armada.spi.param.sku;


import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author xiaobo
 * @date 2021/4/22 3:33 下午
 */
@Data
@Accessors(chain = true)
public class SkuInfo {

    /**
     * 仓库	仓库编码
     */
    private String warehouseCode;

    /**
     * 货主	货主编码
     */
    private String customerCode;

    /**
     * 商品名称	无名称默认商品编码
     */
    private String skuName;

    /**
     * 商品条形码
     */
    private String skuBarCode;

    /**
     * 第三方包装条码
     */
    private List<String> thirdCodeList;

    /**
     * 基本单位	商品基本计量单位（个、件等） 默认件
     */
    private String unit;

    /**
     * 保质期天数 "保质期商品填写 非保质期商品不填写"
     */
    private String shelfLife;

    /**
     * 商品状态
     * 0：禁用
     * 1：启用
     * 默认1
     */
    private String skuStatus;

    /**
     * 商品长度	单位厘米（cm）默认0
     */
    private Double length;

    /**
     * 商品宽度	单位厘米（cm）默认0
     */
    private Double width;

    /**
     * 商品高度	单位厘米（cm）默认0
     */
    private Double height;

    /**
     * 商品体积	单位立方米（m3）默认0
     */
    private Double volume;

    /**
     * 商品重量	单位千克（kg）默认0
     * 毛重
     */
    private Double weight;

    /**
     * 商品管理方式 "0：序列号管理 1：非序列号管理 默认0"
     */
    private Integer manageType;

    /**
     * 货型大小
     */
    private String merchandiseSpec;

    /**
     * 颜色
     */
    private String itemColor;

    /**
     * 图片地址
     */
    private String picUrl;

    /**
     * 是否易丢失	"0：不易丢失 1：易丢失 默认0"
     */
    private Integer isBreakable;

    /**
     * 是否危险品	"0：不是危险品 1：危险品 默认0"
     */
    private Integer isDangerous;

    /**
     * 是否贵重品	"0：不是贵重品 1：贵重品 默认0"
     */
    private Integer isPrecious;

    /**
     * 是否纯电	"0：不是纯电 1：纯电 默认0"
     */
    private Integer isBattery;

    /**
     * 是否需要效期管理 "0：否;1：是 默认0"
     */
    private Integer isNeedExpManage;


    /**
     * 是否异常
     */
    private Boolean isException = false;

    /**
     * 是否批量同步
     */
    private Boolean isBatch = false;

}
