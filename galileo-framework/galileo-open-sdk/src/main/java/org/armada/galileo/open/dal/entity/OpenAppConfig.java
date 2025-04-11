package org.armada.galileo.open.dal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.experimental.Accessors;
import org.armada.galileo.es.entity.EsBaseEntity;
import org.armada.galileo.open.dal.domain.CompileResult;

import java.util.List;

/**
 * <p>
 * 开放平台应用配置信息
 * </p>
 *
 * @author 
 * @since 2023-02-03
 */
@Data
@Accessors(chain = true)
@TableName(autoResultMap = true)
public class OpenAppConfig implements EsBaseEntity {

    @Override
    public String getIdValue() {
        return id;
    }

    private String id;

    /**
     * 创建时间
     */
    private Long gmtCreate;

    /**
     * 更新时间
     */
    private Long gmtModify;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 更新人
     */
    private String modifier;

    /**
     * 删除标记 y / n
     */
    private String isDelete;

    /**
     * 开发者账号id
     */
    private Long accountId;

    /**
     * appId
     */
    private String appId;

    /**
     * 应用名
     */
    private String appName;

    /**
     * 密钥
     */
    private String appSecret;

    /**
     * 回传地址
     */
    private String callbackUrl;

    /**
     * request 拦截器代码
     */
    private String requestCodeContent;

    /**
     * request code 特征码
     */
    private String requestCodeSign;

    /**
     * request code 编译结果
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<CompileResult> requestCodeCompileResult;

    /**
     * reponse 拦截器代码
     */
    private String responseCodeContent;

    /**
     * response code 特征码
     */
    private String responseCodeSign;

    /**
     * response code 编译结果
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<CompileResult> responseCodeCompileResult;

    /**
     * 开通的接口清单
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> apiUrls;

}
