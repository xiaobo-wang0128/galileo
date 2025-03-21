package org.armada.galileo.i18n_server.dal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * @author ake
 * @since 2021-12-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(autoResultMap = true)
public class I18nDictionaryKey extends BaseMysqlEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 应用主键
     */
    private Integer appId;

    /**
     * 应用 code
     */
    private String appCode;

    /**
     * 词条键值
     */
    private String dictionaryKey;


    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> dictValueMap;

    private Integer sort;

    /**
     * y / n
     */
    private String isFinish;

    /**
     * y / n
     */
    private String byScan;

    /**
     * 组
     */
    private String keyGroup;


    /**
     * y / n 自动翻译
     */
    private String autoTranslate;

}
