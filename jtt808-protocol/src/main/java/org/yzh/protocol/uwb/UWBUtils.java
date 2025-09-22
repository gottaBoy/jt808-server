package org.yzh.protocol.uwb;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * UWB工具类
 * 提供数据验证、转换等实用方法
 * 
 * @author yzh
 */
public class UWBUtils {

    private static final Pattern IP_PATTERN = Pattern.compile(
        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 验证IP地址格式
     */
    public static boolean isValidIP(String ip) {
        return ip != null && IP_PATTERN.matcher(ip).matches();
    }

    /**
     * 验证端口号范围
     */
    public static boolean isValidPort(int port) {
        return port >= 0 && port <= 65535;
    }

    /**
     * 验证VID范围
     */
    public static boolean isValidVID(int vid) {
        return vid > 0 && vid <= 65535;
    }

    /**
     * 验证UWB帧数据
     */
    public static boolean validateFrame(UWBFrame frame) {
        if (frame == null) {
            return false;
        }

        // 验证特征字
        byte[] signature = frame.getSignature();
        if (signature == null || signature.length != 2 || 
            signature[0] != 0x3C || signature[1] != 0x3C) {
            return false;
        }

        // 验证优先码
        byte priority = frame.getPriority();
        if (priority < 0x20 || priority > 0x40) {
            return false;
        }

        // 验证数据长度
        byte dataLength = frame.getDataLength();
        if (dataLength < 18 || dataLength > 255) {
            return false;
        }

        // 验证IP地址
        if (!isValidIP(frame.getSourceIP()) || !isValidIP(frame.getTargetIP())) {
            return false;
        }

        // 验证端口号
        if (!isValidPort(frame.getSourcePort()) || !isValidPort(frame.getTargetPort())) {
            return false;
        }

        // 验证车辆数据
        UWBVehicleData vehicleData = frame.getVehicleData();
        if (vehicleData == null || !vehicleData.isValid()) {
            return false;
        }

        return true;
    }

    /**
     * 验证车辆数据
     */
    public static boolean validateVehicleData(UWBVehicleData vehicleData) {
        if (vehicleData == null) {
            return false;
        }

        return vehicleData.isValid();
    }

    /**
     * 计算CRC16校验码
     */
    public static int calculateCRC16(byte[] data) {
        UWBCRC16 crc16 = new UWBCRC16();
        return crc16.calculate(data);
    }

    /**
     * 验证CRC16校验码
     */
    public static boolean verifyCRC16(byte[] data, int expectedCRC) {
        int calculatedCRC = calculateCRC16(data);
        return calculatedCRC == expectedCRC;
    }

    /**
     * 将IP地址字符串转换为字节数组
     */
    public static byte[] ipToBytes(String ip) {
        if (!isValidIP(ip)) {
            throw new IllegalArgumentException("Invalid IP address: " + ip);
        }
        
        String[] parts = ip.split("\\.");
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte) Integer.parseInt(parts[i]);
        }
        return bytes;
    }

    /**
     * 将字节数组转换为IP地址字符串
     */
    public static String bytesToIP(byte[] bytes) {
        if (bytes == null || bytes.length != 4) {
            throw new IllegalArgumentException("Invalid IP bytes");
        }
        
        return String.format("%d.%d.%d.%d", 
            bytes[0] & 0xFF, 
            bytes[1] & 0xFF, 
            bytes[2] & 0xFF, 
            bytes[3] & 0xFF);
    }

    /**
     * 格式化时间戳
     */
    public static String formatTimestamp(LocalDateTime dateTime) {
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 解析时间戳
     */
    public static LocalDateTime parseTimestamp(String timestamp) {
        return LocalDateTime.parse(timestamp, DATETIME_FORMATTER);
    }

    /**
     * 获取当前时间戳字符串
     */
    public static String getCurrentTimestamp() {
        return formatTimestamp(LocalDateTime.now());
    }

    /**
     * 创建测试用的UWB车辆数据
     */
    public static UWBVehicleData createTestVehicleData(int vid) {
        UWBVehicleData data = new UWBVehicleData();
        data.setVid(vid);
        data.setLongitudeLatitude(116.3974, 39.9093); // 北京天安门
        data.setHeadingDegrees(90.0);
        data.setSpeedKmh(60.0);
        data.setYawRateDegrees(0);
        data.setRollAngleDegrees(0);
        data.setPitchAngleDegrees(0);
        return data;
    }

    /**
     * 创建测试用的UWB帧
     */
    public static UWBFrame createTestFrame(int vid, String sourceIP, int sourcePort, 
                                         String targetIP, int targetPort) {
        UWBVehicleData vehicleData = createTestVehicleData(vid);
        UWBFrame frame = new UWBFrame();
        frame.setVehicleData(vehicleData);
        frame.setSourceIP(sourceIP);
        frame.setSourcePort(sourcePort);
        frame.setTargetIP(targetIP);
        frame.setTargetPort(targetPort);
        return frame;
    }

    /**
     * 检查两个UWB数据是否来自同一设备
     */
    public static boolean isSameDevice(UWBVehicleData data1, UWBVehicleData data2) {
        return data1 != null && data2 != null && data1.getVid() == data2.getVid();
    }

    /**
     * 计算速度等级
     */
    public static SpeedLevel getSpeedLevel(double speedKmh) {
        if (speedKmh < 0) {
            return SpeedLevel.REVERSE;
        } else if (speedKmh == 0) {
            return SpeedLevel.STOPPED;
        } else if (speedKmh <= 30) {
            return SpeedLevel.LOW;
        } else if (speedKmh <= 60) {
            return SpeedLevel.MEDIUM;
        } else if (speedKmh <= 100) {
            return SpeedLevel.HIGH;
        } else {
            return SpeedLevel.VERY_HIGH;
        }
    }

    /**
     * 速度等级枚举
     */
    public enum SpeedLevel {
        REVERSE("倒车"),
        STOPPED("静止"),
        LOW("低速"),
        MEDIUM("中速"),
        HIGH("高速"),
        VERY_HIGH("超高速");

        private final String description;

        SpeedLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
