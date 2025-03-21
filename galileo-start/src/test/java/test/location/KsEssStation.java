package test.location;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author xiaobo
 * @date 2021/12/7 11:53 上午
 */
@Data
@Accessors(chain = true)
public class KsEssStation {

    /**
     * ess 编码
     */
    private String stationCode;

    /**
     * 类型: SHELF("缓存货架"), ROBOT("机器人"), CONVEYOR("输送线"), UNLOADER("卸料机")
     */
    private String stationPhysicalType;

    /**
     * 绑定的地图点坐标 x
     */
    private Integer stationX;

    /**
     * 绑定的地图点坐标 y
     */
    private Integer stationY;

}
