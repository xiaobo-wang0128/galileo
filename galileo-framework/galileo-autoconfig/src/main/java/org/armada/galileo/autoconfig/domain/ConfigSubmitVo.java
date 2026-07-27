package org.armada.galileo.autoconfig.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.armada.galileo.autoconfig.form.ATFormGroup;

/**
 * @author xiaobo
 * @date 2024/1/16 15:44
 */
@Data
@Accessors(chain = true)
public class ConfigSubmitVo {

    private ATFormGroup form ;

    private Object value;
}
