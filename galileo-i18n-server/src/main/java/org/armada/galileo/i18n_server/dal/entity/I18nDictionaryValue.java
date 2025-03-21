package org.armada.galileo.i18n_server.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author ake
 * @since 2021-12-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class I18nDictionaryValue extends BaseMysqlEntity implements Serializable  {

    private static final long serialVersionUID = 1L;
    /**
     * 应用主键
     */
    private Integer appId;
    /**
     * 词条键值id
     */
    private Integer dictionaryKeyId;
    /**
     * 词条对应值
     */
    private String dictionaryValue;
    /**
     * 词条对应语言
     */
    private String locale;


}
