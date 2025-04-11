package org.armada.galileo.i18n_server.dal.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author xiaobo
 * @date 2022/12/26 16:21
 */
@Data
@Accessors(chain = true)
public class DictVO {

    private Integer appId;

    /**
     * 应用 code
     */
    private String appCode;

    /**
     * 字条id
     */
    private Integer dictId;

    /**
     * 字条 key
     */
    private String dictKey;

    /**
     * 是否全部编辑完成
     */
    private String isFinish;

    /**
     * language: value
     */
    private Map<String, String> dictValues;

    /**
     * 排序
     */
    private Integer sort;


    /**
     * y / n
     */
    private String byScan;

    /**
     * 组
     */
    private String group;

}
