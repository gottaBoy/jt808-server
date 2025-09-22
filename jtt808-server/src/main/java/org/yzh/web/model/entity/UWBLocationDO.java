package org.yzh.web.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * UWB位置信息实体
 * 
 * @author yzh
 */
@Data
public class UWBLocationDO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 车载端序列号VID
     */
    private Integer vid;

    /**
     * 车辆坐标X
     */
    private Long coordinateX;

    /**
     * 车辆坐标Y
     */
    private Long coordinateY;

    /**
     * 航向角度
     */
    private Integer heading;

    /**
     * 速度
     */
    private Integer speed;

    /**
     * 横摆角速度
     */
    private Byte yawRate;

    /**
     * 侧倾角
     */
    private Byte rollAngle;

    /**
     * 俯仰角
     */
    private Byte pitchAngle;

    /**
     * 源IP地址
     */
    private String sourceIP;

    /**
     * 源端口号
     */
    private Integer sourcePort;

    /**
     * 目标IP地址
     */
    private String targetIP;

    /**
     * 目标端口号
     */
    private Integer targetPort;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 获取航向角度（度）
     */
    public double getHeadingDegrees() {
        return heading != null ? heading / 10.0 : 0.0;
    }

    /**
     * 获取速度（Km/h）
     */
    public double getSpeedKmh() {
        return speed != null ? speed / 100.0 : 0.0;
    }

    /**
     * 获取横摆角速度（度/秒）
     */
    public int getYawRateDegrees() {
        return yawRate != null ? yawRate : 0;
    }

    /**
     * 获取侧倾角（度）
     */
    public int getRollAngleDegrees() {
        return rollAngle != null ? rollAngle : 0;
    }

    /**
     * 获取俯仰角（度）
     */
    public int getPitchAngleDegrees() {
        return pitchAngle != null ? pitchAngle : 0;
    }
}
