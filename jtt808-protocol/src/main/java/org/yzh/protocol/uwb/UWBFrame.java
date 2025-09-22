package org.yzh.protocol.uwb;

import io.github.yezhihao.protostar.annotation.Field;
import io.github.yezhihao.protostar.annotation.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.yzh.protocol.basics.JTMessage;

/**
 * UWB定位数据帧
 * 共迹公司UWB定位车载端数据协议
 * 
 * @author yzh
 */
@Message(0x8001)
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class UWBFrame extends JTMessage {

    @Field(desc = "特征字", length = 2)
    private byte[] signature = {0x3C, 0x3C}; // 固定值0x3C3C

    @Field(desc = "传输优先码", length = 1)
    private byte priority = 0x20; // 0x20~0x40，数值越小优先等级越高

    @Field(desc = "数据长度", length = 1)
    private byte dataLength; // 不含CRC码，本帧全部数据长度限制为255字节

    @Field(desc = "目标地址", length = 4)
    private byte[] targetAddress = new byte[4]; // IP地址的数值部分

    @Field(desc = "目标端口号", length = 2)
    private int targetPort; // 0~65535

    @Field(desc = "源地址", length = 4)
    private byte[] sourceAddress = new byte[4]; // 回送的地址

    @Field(desc = "源端口号", length = 2)
    private int sourcePort;

    // 车载端数据字段
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

    @Field(desc = "CRC16校验码", length = 2)
    private int crc16; // 自特征字开始校验

    public UWBFrame() {
    }

    public UWBFrame(int vid, long coordinateX, long coordinateY, int heading, int speed, 
                   byte yawRate, byte rollAngle, byte pitchAngle) {
        this.vid = vid;
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
        this.heading = heading;
        this.speed = speed;
        this.yawRate = yawRate;
        this.rollAngle = rollAngle;
        this.pitchAngle = pitchAngle;
        this.dataLength = (byte) (2 + 1 + 1 + 4 + 2 + 4 + 2 + 15 + 2); // 15字节车载数据
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public byte getPriority() {
        return priority;
    }

    public void setPriority(byte priority) {
        this.priority = priority;
    }

    public byte getDataLength() {
        return dataLength;
    }

    public void setDataLength(byte dataLength) {
        this.dataLength = dataLength;
    }

    public byte[] getTargetAddress() {
        return targetAddress;
    }

    public void setTargetAddress(byte[] targetAddress) {
        this.targetAddress = targetAddress;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    public byte[] getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(byte[] sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }


    public int getCrc16() {
        return crc16;
    }

    public void setCrc16(int crc16) {
        this.crc16 = crc16;
    }

    // 车载端数据字段的getter和setter方法
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

    // 便捷方法：获取UWBVehicleData对象
    public UWBVehicleData getVehicleData() {
        UWBVehicleData data = new UWBVehicleData();
        data.setVid(this.vid);
        data.setCoordinateX(this.coordinateX);
        data.setCoordinateY(this.coordinateY);
        data.setHeading(this.heading);
        data.setSpeed(this.speed);
        data.setYawRate(this.yawRate);
        data.setRollAngle(this.rollAngle);
        data.setPitchAngle(this.pitchAngle);
        return data;
    }

    // 便捷方法：设置UWBVehicleData对象
    public void setVehicleData(UWBVehicleData vehicleData) {
        if (vehicleData != null) {
            this.vid = vehicleData.getVid();
            this.coordinateX = vehicleData.getCoordinateX();
            this.coordinateY = vehicleData.getCoordinateY();
            this.heading = vehicleData.getHeading();
            this.speed = vehicleData.getSpeed();
            this.yawRate = vehicleData.getYawRate();
            this.rollAngle = vehicleData.getRollAngle();
            this.pitchAngle = vehicleData.getPitchAngle();
        }
    }

    /**
     * 设置目标IP地址
     */
    public void setTargetIP(String ip) {
        String[] parts = ip.split("\\.");
        for (int i = 0; i < 4; i++) {
            targetAddress[i] = (byte) Integer.parseInt(parts[i]);
        }
    }

    /**
     * 设置源IP地址
     */
    public void setSourceIP(String ip) {
        String[] parts = ip.split("\\.");
        for (int i = 0; i < 4; i++) {
            sourceAddress[i] = (byte) Integer.parseInt(parts[i]);
        }
    }

    /**
     * 获取目标IP地址字符串
     */
    public String getTargetIP() {
        return String.format("%d.%d.%d.%d", 
            targetAddress[0] & 0xFF, 
            targetAddress[1] & 0xFF, 
            targetAddress[2] & 0xFF, 
            targetAddress[3] & 0xFF);
    }

    /**
     * 获取源IP地址字符串
     */
    public String getSourceIP() {
        return String.format("%d.%d.%d.%d", 
            sourceAddress[0] & 0xFF, 
            sourceAddress[1] & 0xFF, 
            sourceAddress[2] & 0xFF, 
            sourceAddress[3] & 0xFF);
    }
}
