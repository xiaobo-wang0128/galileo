package org.armada.galileo.i18n_server.dal.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author xiaobo
 * @date 2022/12/26 16:21
 */
@Data
@Accessors(chain = true)
public class DictQueryVO {

    Integer appId;

    private String appCode;

    private String keyword;

    private String isFinish;

    private boolean page = true;

}
