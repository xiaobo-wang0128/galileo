package org.armada.galileo.open.vo;

import lombok.Data;
import lombok.experimental.Accessors;
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
public class OpenAppConfigQueryVO {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 开发者账号id
     */
    private Long accountId;

    /**
     * appId
     */
    private String appId;

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
    private List<CompileResult> responseCodeCompileResult;

    /**
     * 开通的接口清单
     */
    private List<String> apiUrls;


}
