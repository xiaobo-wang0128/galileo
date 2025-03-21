//package org.armada.galileo.sample.biz_config;
//
//import lombok.Data;
//import org.armada.galileo.autoconfig.AutoConfigGalileo;
//import org.armada.galileo.autoconfig.annotation.ConfigField;
//import org.armada.galileo.autoconfig.annotation.ConfigGroup;
//import org.armada.galileo.autoconfig.annotation.ConfigOption;
//import org.armada.galileo.autoconfig.annotation.Option;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author xiaobo
// * @date 2021/11/12 4:22 下午
// */
//// @Configuration
//@Data
//@ConfigGroup(group = "库存模块配置", sort = 3)
//public class IwmsSysConfigStock implements AutoConfigGalileo {
//
//    /*=====================库存相关配置==============================*/
//
//    @ConfigField(name = "库存 - 0库存保存天数配置", desc = "当SKU批次库存为0时，保存多少天后系统自动清理")
//    public Integer STOCK_ZERO_EXPIRE_DAYS = 30;
//
//    @ConfigField(name = "库存 - 库存差异是否需要上报", desc = "短拣、盘点、理库等产生的库存差异，是否允许上报给客户系统，开启后，只有上报的差异才允许进行调整")
//    public Boolean STOCK_DIFF_FEEDBACK_STRATEGY = false;
//
//    @ConfigField(name = "库存 - 是否只允许上报复盘差异", desc = "只有复盘产生的差异才允许上报给客户系统")
//    public Boolean STOCK_DIFF_FEEDBACK_REVIEW_STRATEGY = true;
//
//    @ConfigField(name = "库存 - 库存调整是否反馈给客户", desc = "库存有调整时自动回调给客户系统")
//    public Boolean ADJUSTMENT_FEEDBACK_AUTO = true;
//
//    @ConfigField(name = "库存 - 库存调整单是否自动执行调整", desc = "当系统生成库存调整单时，是否自动执行调整")
//    public Boolean ADJUSTMENT_AUTO = false;
//
//    @ConfigField(name = "库存 - 批次属性调整是否反馈给客户", desc = "批次属性调整完成，是否需要回传客户系统")
//    public Boolean ATTRIBUTE_ADJUSTMENT_CALLBACK_STRATEGY = false;
//
//    @ConfigField(name = "库存 - 是否开启每天库存接口同步", desc = "开启库存接口同步，每天定时同步将库存数据主动推送给客户系统")
//    public Boolean STOCK_SYNC_STRATEGY = false;
//
//
//    @ConfigField(name = "库存 - 库存接口同步每页推送记录数", desc = " 库存接口同步开启时，分页推送给客户系统，每页推送的记录数最大值")
//    public Integer STOCK_SYNC_PAGE_SIZE = 100;
//
//    @ConfigField(name = "库存 - 是否开启每天生成库存Excel文件", desc = "开启生成库存Excel文件，每天定时生成全量库存Excel表格")
//    public Boolean STOCK_FILE_STRATEGY = false;
//
//    @ConfigField(name = "库存 - 库存Excel文件最大记录数", desc = "库存Excel文件生成开启时，每个文件生成的最大记录数，超出部分将生成一个新的文件")
//    public Integer STOCK_FILE_PAGE_SIZE = 10000;
//
//    @ConfigField(name = "库存 - 库存Excel文件保存路径", desc = "库存Excel文件生成后，保存到指定路径，默认D:\\stock")
//    public String STOCK_FILE_PATH = "D:\\stock";
//
//    @ConfigField(name = "库存 - 库存Excel文件是否需要上传FTP", desc = "库存Excel文件生成后，上传到指定的FTP地址")
//    public Boolean STOCK_FILE_UPLOAD_FTP = false;
//
//    @ConfigField(name = "库存 - FTP配置信息", desc = "库存Excel文件生成后，上传到指定的FTP地址,JSON格式，如：{\"ip\":\"xxx.xxx.xxx.xxx\",\"port\":21,\"user\":\"test\",\"pwd\":\"***\",\"path\":\"path\"}}")
//    public String STOCK_FILE_FTP_CONFIG;
//
//    @ConfigField(name = "库存 - 库存Excel文件保留天数", desc = "库存Excel文件生成后，历史文件保留一定的时间后会自动清理，默认保留7天")
//    public Integer STOCK_FILE_DAY = 7;
//
//
//}
