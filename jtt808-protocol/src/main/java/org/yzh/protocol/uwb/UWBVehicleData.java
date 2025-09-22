package org.yzh.protocol.uwb;

import io.github.yezhihao.protostar.annotation.Field;
import io.github.yezhihao.protostar.annotation.Message;

/**
 * UWB车载端数据
 * 包含车辆位置、姿态等信息
 * 
 * @author yzh
 */
@Message
public class UWBVehicleData {

    @Field(desc = "车载端序列号VID", length = 2)
    private int vid; // 由供应商确定

    @Field(desc = "车辆坐标X", length = 4)
    private long coordinateX; // UTM坐标X或经度，UTM坐标单位cm，经度单位1E-7度

    @Field(desc = "车辆坐标Y", length = 4)
    private long coordinateY; // UTM坐标Y或纬度，UTM坐标单位cm，纬度单位1E-7度

    @Field(desc = "航向Heading", length = 2)
    private int heading; // 单位：度，分辨率0.1°，范围0°~360°

    @Field(desc = "速度V", length = 2)
    private int speed; // 单位：Km/h，分辨率0.01Km/h，范围-200~200Km/h

    @Field(desc = "横摆角速度ω", length = 1)
    private byte yawRate; // 单位：度/秒，分辨率1°/s，范围-120°~120°/s

    @Field(desc = "侧倾角φ", length = 1)
    private byte rollAngle; // 单位：度，分辨率1°，范围-90°~90°

    @Field(desc = "俯仰角θ", length = 1)
    private byte pitchAngle; // 单位：度，分辨率1°，范围-90°~90°

    public UWBVehicleData() {
    }

    public UWBVehicleData(int vid, long coordinateX, long coordinateY, int heading, 
                         int speed, byte yawRate, byte rollAngle, byte pitchAngle) {
        this.vid = vid;
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
        this.heading = heading;
        this.speed = speed;
        this.yawRate = yawRate;
        this.rollAngle = rollAngle;
        this.pitchAngle = pitchAngle;
    }

    public int getVid() {
        return vid;
    }

    public void setVid(int vid) {
        this.vid = vid;
    }

    public long getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(long coordinateX) {
        this.coordinateX = coordinateX;
    }

    public long getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(long coordinateY) {
        this.coordinateY = coordinateY;
    }

    public int getHeading() {
        return heading;
    }

    public void setHeading(int heading) {
        this.heading = heading;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public byte getYawRate() {
        return yawRate;
    }

    public void setYawRate(byte yawRate) {
        this.yawRate = yawRate;
    }

    public byte getRollAngle() {
        return rollAngle;
    }

    public void setRollAngle(byte rollAngle) {
        this.rollAngle = rollAngle;
    }

    public byte getPitchAngle() {
        return pitchAngle;
    }

    public void setPitchAngle(byte pitchAngle) {
        this.pitchAngle = pitchAngle;
    }

    /**
     * 获取数据长度
     */
    public int getDataLength() {
        return 2 + 4 + 4 + 2 + 2 + 1 + 1 + 1; // 17字节
    }

    /**
     * 获取航向角度（度）
     */
    public double getHeadingDegrees() {
        return heading / 10.0;
    }

    /**
     * 设置航向角度（度）
     */
    public void setHeadingDegrees(double degrees) {
        this.heading = (int) (degrees * 10);
    }

    /**
     * 获取速度（Km/h）
     */
    public double getSpeedKmh() {
        return speed / 100.0;
    }

    /**
     * 设置速度（Km/h）
     */
    public void setSpeedKmh(double kmh) {
        this.speed = (int) (kmh * 100);
    }

    /**
     * 获取横摆角速度（度/秒）
     */
    public int getYawRateDegrees() {
        return yawRate;
    }

    /**
     * 设置横摆角速度（度/秒）
     */
    public void setYawRateDegrees(int degrees) {
        this.yawRate = (byte) Math.max(-120, Math.min(120, degrees));
    }

    /**
     * 获取侧倾角（度）
     */
    public int getRollAngleDegrees() {
        return rollAngle;
    }

    /**
     * 设置侧倾角（度）
     */
    public void setRollAngleDegrees(int degrees) {
        this.rollAngle = (byte) Math.max(-90, Math.min(90, degrees));
    }

    /**
     * 获取俯仰角（度）
     */
    public int getPitchAngleDegrees() {
        return pitchAngle;
    }

    /**
     * 设置俯仰角（度）
     */
    public void setPitchAngleDegrees(int degrees) {
        this.pitchAngle = (byte) Math.max(-90, Math.min(90, degrees));
    }

    /**
     * 检查数据是否有效
     */
    public boolean isValid() {
        return vid > 0 && 
               coordinateX != 0 && coordinateY != 0 &&
               heading >= 0 && heading <= 3600 &&
               speed >= -20000 && speed <= 20000 &&
               yawRate >= -120 && yawRate <= 120 &&
               rollAngle >= -90 && rollAngle <= 90 &&
               pitchAngle >= -90 && pitchAngle <= 90;
    }

    /**
     * 获取坐标类型（UTM或经纬度）
     */
    public CoordinateType getCoordinateType() {
        // 简单判断：如果X坐标在-180到180度范围内，认为是经纬度
        if (coordinateX >= -1800000000L && coordinateX <= 1800000000L &&
            coordinateY >= -900000000L && coordinateY <= 900000000L) {
            return CoordinateType.LONGITUDE_LATITUDE;
        }
        return CoordinateType.UTM;
    }

    /**
     * 获取经度（度）
     */
    public double getLongitude() {
        if (getCoordinateType() == CoordinateType.LONGITUDE_LATITUDE) {
            return coordinateX / 1E7;
        }
        throw new UnsupportedOperationException("当前坐标类型不是经纬度");
    }

    /**
     * 获取纬度（度）
     */
    public double getLatitude() {
        if (getCoordinateType() == CoordinateType.LONGITUDE_LATITUDE) {
            return coordinateY / 1E7;
        }
        throw new UnsupportedOperationException("当前坐标类型不是经纬度");
    }

    /**
     * 设置经纬度
     */
    public void setLongitudeLatitude(double longitude, double latitude) {
        this.coordinateX = (long) (longitude * 1E7);
        this.coordinateY = (long) (latitude * 1E7);
    }

    /**
     * 获取UTM坐标X（厘米）
     */
    public long getUtmX() {
        if (getCoordinateType() == CoordinateType.UTM) {
            return coordinateX;
        }
        throw new UnsupportedOperationException("当前坐标类型不是UTM");
    }

    /**
     * 获取UTM坐标Y（厘米）
     */
    public long getUtmY() {
        if (getCoordinateType() == CoordinateType.UTM) {
            return coordinateY;
        }
        throw new UnsupportedOperationException("当前坐标类型不是UTM");
    }

    /**
     * 设置UTM坐标
     */
    public void setUtmCoordinates(long x, long y) {
        this.coordinateX = x;
        this.coordinateY = y;
    }

    /**
     * 检查是否在指定区域内
     */
    public boolean isInArea(double minLon, double minLat, double maxLon, double maxLat) {
        if (getCoordinateType() != CoordinateType.LONGITUDE_LATITUDE) {
            return false;
        }
        double lon = getLongitude();
        double lat = getLatitude();
        return lon >= minLon && lon <= maxLon && lat >= minLat && lat <= maxLat;
    }

    /**
     * 计算到另一个点的距离（米）
     */
    public double distanceTo(UWBVehicleData other) {
        if (getCoordinateType() != CoordinateType.LONGITUDE_LATITUDE || 
            other.getCoordinateType() != CoordinateType.LONGITUDE_LATITUDE) {
            throw new UnsupportedOperationException("距离计算仅支持经纬度坐标");
        }
        
        return calculateDistance(getLatitude(), getLongitude(), 
                               other.getLatitude(), other.getLongitude());
    }

    /**
     * 计算两点间距离（使用Haversine公式）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // 地球半径（米）
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Override
    public String toString() {
        return "UWBVehicleData{" +
                "vid=" + vid +
                ", coordinateX=" + coordinateX +
                ", coordinateY=" + coordinateY +
                ", coordinateType=" + getCoordinateType() +
                ", heading=" + getHeadingDegrees() + "°" +
                ", speed=" + getSpeedKmh() + "km/h" +
                ", yawRate=" + getYawRateDegrees() + "°/s" +
                ", rollAngle=" + getRollAngleDegrees() + "°" +
                ", pitchAngle=" + getPitchAngleDegrees() + "°" +
                ", valid=" + isValid() +
                '}';
    }

    /**
     * 坐标类型枚举
     */
    public enum CoordinateType {
        LONGITUDE_LATITUDE,  // 经纬度
        UTM                  // UTM坐标
    }
}
