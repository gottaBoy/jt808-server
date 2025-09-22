package org.yzh.uwb;

import org.junit.jupiter.api.Test;
import org.yzh.protocol.uwb.UWBFrame;
import org.yzh.protocol.uwb.UWBVehicleData;
import org.yzh.protocol.uwb.UWBCRC16;

/**
 * UWB协议测试类
 * 
 * @author yzh
 */
public class UWBTest {

    @Test
    public void testUWBDataCreation() {
        // 创建UWB车辆数据
        UWBVehicleData vehicleData = new UWBVehicleData();
        vehicleData.setVid(12345);
        vehicleData.setCoordinateX(1163970000L); // 经度
        vehicleData.setCoordinateY(399000000L);  // 纬度
        vehicleData.setHeadingDegrees(90.5);     // 航向90.5度
        vehicleData.setSpeedKmh(60.0);           // 速度60km/h
        vehicleData.setYawRateDegrees(0);        // 横摆角速度0度/秒
        vehicleData.setRollAngleDegrees(0);      // 侧倾角0度
        vehicleData.setPitchAngleDegrees(0);     // 俯仰角0度

        System.out.println("UWB Vehicle Data: " + vehicleData);
    }

    @Test
    public void testUWBFrameCreation() {
        // 创建UWB车辆数据
        UWBVehicleData vehicleData = new UWBVehicleData(12345, 1163970000L, 399000000L, 
                                                       905, 6000, (byte)0, (byte)0, (byte)0);

        // 创建UWB帧
        UWBFrame frame = new UWBFrame();
        frame.setVehicleData(vehicleData);
        frame.setSourceIP("192.168.1.100");
        frame.setSourcePort(8080);
        frame.setTargetIP("192.168.1.200");
        frame.setTargetPort(8081);

        System.out.println("UWB Frame - Source: " + frame.getSourceIP() + ":" + frame.getSourcePort());
        System.out.println("UWB Frame - Target: " + frame.getTargetIP() + ":" + frame.getTargetPort());
        System.out.println("UWB Frame - Data Length: " + frame.getDataLength());
    }

    @Test
    public void testCRC16Calculation() {
        UWBCRC16 crc16 = new UWBCRC16();
        
        // 测试数据
        byte[] testData = {(byte)0x3C, (byte)0x3C, (byte)0x20, (byte)0x15, 0x00, 0x00, 0x00, 0x00, (byte)0x1F, (byte)0x90, 0x00, 0x00, 0x00, 0x00, (byte)0x1F, 0x40, 0x30, 0x39, 0x45, 0x6B, (byte)0x17, (byte)0xC0};
        
        int crc = crc16.calculate(testData);
        System.out.println("CRC16: 0x" + Integer.toHexString(crc).toUpperCase());
    }
}
