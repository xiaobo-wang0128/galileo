package org.armada.galileo.mybatis.mysql_auto;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.armada.galileo.common.util.CommonUtil;

/**
 * @author xiaobo
 * @date 2023/4/21 17:19
 */
public class MybatisQueryUtil {


    public static void like(QueryWrapper queryWrapper, String column, String value) {
        if (CommonUtil.isEmpty(value)) {
            return;
        }
        value = value.replaceAll("['\"\\s]", "");
        queryWrapper.apply(CommonUtil.format("{} like '%{}%'", column, value));
    }


    public static void like(LambdaQueryWrapper queryWrapper, String column, String value) {
        if (CommonUtil.isEmpty(value)) {
            return;
        }
        value = value.replaceAll("['\"\\s]", "");
        queryWrapper.apply(CommonUtil.format("{} like '%{}%'", column, value));
    }
}
