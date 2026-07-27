package org.armada.galileo.model.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 角色类型
 * @author xiaobo
 *
 * @date 2023/4/11 10:57
 */
public enum RoleTypeEnum {

    ADMIN("管理员"),

    OPERATOR("操作员"),

    ;

    private String desc;

    RoleTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return this.desc;
    }


    private static Map<RoleTypeEnum, Integer> sort = new HashMap<>();
    static {
        int i =0 ;
        for (RoleTypeEnum value : RoleTypeEnum.values()) {
            sort.put(value, i++);
        }
    }
    public static Integer getSort(RoleTypeEnum v){
        if(v==null){
            return Integer.MAX_VALUE;
        }
        Integer i = sort.get(v);
        if(i!=null){
            return i;
        }
        return -1;
    }


}
