package test.location;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ess 原始工作位信息
 * <pre>
 * {
 *   "id": "1714413997117410304",
 *   "code": "LT_CONVEYOR_INPUT:POINT:46740:93340",
 *   "locationTypeCode": "LT_CONVEYOR_INPUT",
 *   "position": {
 *     "x": "46740",
 *     "y": "93343",
 *     "z": "750"
 *   },
 *   "link": [
 *     {
 *       "pointCode": "POINT:46740:93340",
 *       "robot2mapTheta": "270",
 *       "container2mapTheta": "270"
 *     }
 *   ],
 *   "isLocked": false,
 *   "isAbnormal": false,
 *   "abnormalReason": "NONE",
 *   "storageTag": "",
 *   "containerCode": "821206237",
 *   "actionFailedCount": {}
 * }
 * </pre>
 *
 * @author xiaobo
 * @date 2021/12/6 11:46上午
 */

@Data
@Accessors(chain = true)
public class KsEssLocation {

    /**
     * 库位编码
     */
    private String locationCode;

    /**
     * 库位坐标 x (mm)
     */
    private Integer locX;

    /**
     * 库位坐标 y (mm)
     */
    private Integer locY;

    /**
     * 库位坐标 z (mm) 高度
     */
    private Integer locZ;

    /**
     * 机器人对地图的角度 ( 0 180 270 ... )
     */
    private Integer robotMapTheta;

    /**
     * 机器人取货货叉角度( 0 180 270 ... )
     */
    private Integer containerMapTheta;

    /**
     * 机器人停靠点坐标 x (mm)
     */
    private Integer robotX;

    /**
     * 机器人停靠点坐标 y (mm)
     */
    private Integer robotY;

    /**
     * 库位类型 @See EssLocationTypeEnum
     */
    private String locationType;

    /**
     * 是否锁定
     */
    private Boolean isLocked;

    /**
     * 是否异常
     */
    private Boolean isAbnormal;

    /**
     * 异常原因
     */
    private String abnormalReason;
}
