package test.location;

/**
 * @author xiaobo
 * @date 2021/12/6 11:42 上午
 */
public enum EssLocationTypeEnum {

    LT_SHELF_STORAGE("默认库位类型"),

    LT_DEEP_SHELF_STORAGE("库位类型-双深位-深库位"),

    LT_CHARGE("默认充电类型"),

    LT_REST("默认休息类型"),

    LT_LABOR("默认人工工作站类型"),

    LT_CONVEYOR_INPUT("默认输送线流入口类型"),

    LT_CONVEYOR_OUTPUT("默认输送线流出口类型"),

    LT_CONVEYOR_INOUT("默认输送线出入口混合类型"),

    LT_CACHE_SHELF_ENTRY("默认缓存货架入口类型"),

    LT_CACHE_SHELF_STORAGE("默认缓存货架存储类型"),

    LT_HAIPORT("默认haiport类型 可以包含 上料待待 上料  卸料等待 卸料"),

    LT_MAINTAIN("默认维修区工作站类型"),

    ;

    private String name;

    private EssLocationTypeEnum(String name) {
        this.name = name;
    }
}
