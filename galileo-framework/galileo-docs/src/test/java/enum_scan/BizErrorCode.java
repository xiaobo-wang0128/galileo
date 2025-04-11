package enum_scan;

import org.armada.galileo.exception.I18nError;

/**
 * @author xiaobo
 * @date 2023/1/4 19:13
 */
public enum BizErrorCode implements I18nError {

    // 用户模块
    USER_PASSWORD_IS_NULL("密码不能为空"),
    USER_NOT_EXIST("用户不存在或密码错误"),
    USER_HAS_NO_RIGHT("用户没有配置权限，请联系管理员"),
    USER_IS_FREEZE("用户已冻结，请联系管理员")

    // wms 模块


    ;


    private String desc;

    BizErrorCode(String desc) {
        this.desc = desc;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }
}
