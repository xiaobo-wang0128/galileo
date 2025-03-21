package org.armada.galileo.i18n_server.dal.dto;

import cn.hutool.core.util.StrUtil;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.i18n_server.dal.enums.StatusEnum;

import java.util.*;

/**
 * @author ake
 * @since 2021-12-21
 */
@Data
public class I18nDictionaryKeyDTO {
    /**
     * 主键
     */
    private Integer id;
    /**
     * 应用主键
     */
    private Integer appId;
    /**
     * 应用编码
     */
    private String appCode;
    /**
     * 应用名称
     */
    private String appName;
    /**
     * 词条键值
     */
    private String dictionaryKey;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date updateTime;
    /**
     * 创建人
     */
    private String createUser;
    /**
     * 修改人
     */
    private String updateUser;
    /**
     * 创建人id
     */
    private Long createUserId;
    /**
     * 修改人id
     */
    private Long updateUserId;
    /**
     * 原生枚举（带{@link StatusEnum}):
     * 启用或禁用->enable or disable
     */
    private StatusEnum status;

    /**
     * 国际化value
     */
    private Map<String, String> i18nValueMap;
    /**
     * 国际化语言
     */
    private List<String> locales;

    /**
     * 词条Id List的string
     */
    private String dictionaryKeyIds;

    /**
     * 是否只查询未完成的
     */
    private Boolean queryUnfinished = false;

    /**
     * 分支过滤筛选字符串 前端传参数限制，需要转化为 List<Map<String,String>>
     */
    private String branchSets;

    /**
     * 分支Map
     */
    private Map<String,List<String>> branchMaps;

    /**
     * 转换string词条Id List
     */
    private List<Integer> transitionDictionaryKeyIds = new ArrayList<>();

    public void transition() {
        if (StrUtil.isNotEmpty(this.dictionaryKeyIds)) {
            if (this.dictionaryKeyIds.length() > 2) {
                String[] split = this.dictionaryKeyIds.substring(1, this.dictionaryKeyIds.length() - 1).split(",");
                for (String s : split) {
                    this.transitionDictionaryKeyIds.add(Integer.valueOf(s));
                }
            }
        }
        if (StrUtil.isNotEmpty(this.branchSets)) {
            this.branchMaps = JsonUtil.fromJson(this.branchSets,new TypeToken<Map<String, List<String>>>() {
            }.getType());
        }
    }
}
