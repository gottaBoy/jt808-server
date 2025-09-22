package org.yzh.uwb;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.yzh.protocol.uwb.UWBFrame;
import org.yzh.protocol.uwb.UWBVehicleData;
import org.yzh.protocol.uwb.UWBUtils;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * UWB客户端测试工具
 * 用于测试UWB服务器的TCP、UDP和HTTP接口
 * 
 * @author yzh
 */
@Slf4j
public class UWBClientTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int TCP_PORT = 7200;
    private static final int UDP_PORT = 7201;
    private static final int HTTP_PORT = 8100;

    /**
     * 测试TCP连接
     */
    @Test
    public void testTCPConnection() {
        try (Socket socket = new Socket(SERVER_HOST, TCP_PORT)) {
            log.info("TCP连接成功: {}:{}", SERVER_HOST, TCP_PORT);
            
            // 发送测试数据
            UWBFrame frame = UWBUtils.createTestFrame(12345, "192.168.1.100", 8080, 
                                                     "192.168.1.200", 8081);
            
            // 这里需要实现数据编码和发送逻辑
            log.info("发送UWB数据帧: {}", frame);
            
        } catch (IOException e) {
            log.error("TCP连接失败", e);
        }
    }

    /**
     * 测试UDP连接
     */
    @Test
    public void testUDPConnection() {
        try (DatagramSocket socket = new DatagramSocket()) {
            log.info("UDP连接成功: {}:{}", SERVER_HOST, UDP_PORT);
            
            // 发送测试数据
            UWBFrame frame = UWBUtils.createTestFrame(12345, "192.168.1.100", 8080, 
                                                     "192.168.1.200", 8081);
            
            // 这里需要实现数据编码和发送逻辑
            log.info("发送UWB数据帧: {}", frame);
            
        } catch (IOException e) {
            log.error("UDP连接失败", e);
        }
    }

    /**
     * 测试HTTP接口
     */
    @Test
    public void testHTTPInterface() {
        try {
            String url = "http://" + SERVER_HOST + ":" + HTTP_PORT + "/uwb/data";
            log.info("测试HTTP接口: {}", url);
            
            // 创建测试数据
            UWBVehicleData vehicleData = UWBUtils.createTestVehicleData(12345);
            
            // 构建HTTP请求
            String jsonData = buildJsonData(vehicleData);
            log.info("发送JSON数据: {}", jsonData);
            
            // 这里需要实现HTTP请求发送逻辑
            
        } catch (Exception e) {
            log.error("HTTP接口测试失败", e);
        }
    }

    /**
     * 模拟连续数据发送
     */
    @Test
    public void testContinuousDataSending() {
        log.info("开始模拟连续数据发送...");
        
        Random random = new Random();
        int deviceCount = 10;
        int dataCount = 100;
        
        for (int i = 0; i < dataCount; i++) {
            for (int deviceId = 1; deviceId <= deviceCount; deviceId++) {
                try {
                    // 创建模拟数据
                    UWBVehicleData vehicleData = createSimulatedData(deviceId, random);
                    
                    // 发送数据（这里可以调用实际的发送方法）
                    log.debug("发送设备{}的数据: {}", deviceId, vehicleData);
                    
                    // 模拟发送间隔
                    Thread.sleep(100);
                    
                } catch (Exception e) {
                    log.error("发送数据失败", e);
                }
            }
        }
        
        log.info("连续数据发送完成");
    }

    /**
     * 测试数据验证
     */
    @Test
    public void testDataValidation() {
        log.info("测试数据验证...");
        
        // 测试有效数据
        UWBVehicleData validData = UWBUtils.createTestVehicleData(12345);
        assert UWBUtils.validateVehicleData(validData) : "有效数据验证失败";
        log.info("有效数据验证通过: {}", validData);
        
        // 测试无效数据
        UWBVehicleData invalidData = new UWBVehicleData();
        invalidData.setVid(0); // 无效的VID
        assert !UWBUtils.validateVehicleData(invalidData) : "无效数据验证失败";
        log.info("无效数据验证通过: {}", invalidData);
        
        // 测试UWB帧验证
        UWBFrame validFrame = UWBUtils.createTestFrame(12345, "192.168.1.100", 8080, 
                                                      "192.168.1.200", 8081);
        assert UWBUtils.validateFrame(validFrame) : "有效帧验证失败";
        log.info("有效帧验证通过: {}", validFrame);
    }

    /**
     * 测试坐标转换
     */
    @Test
    public void testCoordinateConversion() {
        log.info("测试坐标转换...");
        
        // 测试经纬度坐标
        UWBVehicleData data = new UWBVehicleData();
        data.setLongitudeLatitude(116.3974, 39.9093); // 北京天安门
        
        assert data.getCoordinateType() == UWBVehicleData.CoordinateType.LONGITUDE_LATITUDE;
        assert Math.abs(data.getLongitude() - 116.3974) < 0.0001;
        assert Math.abs(data.getLatitude() - 39.9093) < 0.0001;
        
        log.info("经纬度坐标测试通过: 经度={}, 纬度={}", data.getLongitude(), data.getLatitude());
        
        // 测试UTM坐标
        data.setUtmCoordinates(500000, 4000000); // 示例UTM坐标
        
        assert data.getCoordinateType() == UWBVehicleData.CoordinateType.UTM;
        assert data.getUtmX() == 500000;
        assert data.getUtmY() == 4000000;
        
        log.info("UTM坐标测试通过: X={}, Y={}", data.getUtmX(), data.getUtmY());
    }

    /**
     * 测试距离计算
     */
    @Test
    public void testDistanceCalculation() {
        log.info("测试距离计算...");
        
        // 北京天安门
        UWBVehicleData beijing = new UWBVehicleData();
        beijing.setLongitudeLatitude(116.3974, 39.9093);
        
        // 上海外滩
        UWBVehicleData shanghai = new UWBVehicleData();
        shanghai.setLongitudeLatitude(121.4998, 31.2397);
        
        double distance = beijing.distanceTo(shanghai);
        log.info("北京到上海距离: {:.2f}米", distance);
        
        // 验证距离是否合理（北京到上海约1067公里）
        assert distance > 1000000 && distance < 1200000 : "距离计算不准确";
    }

    /**
     * 创建模拟数据
     */
    private UWBVehicleData createSimulatedData(int deviceId, Random random) {
        UWBVehicleData data = new UWBVehicleData();
        data.setVid(deviceId);
        
        // 模拟北京地区的随机坐标
        double longitude = 116.0 + random.nextDouble() * 2.0; // 116-118度
        double latitude = 39.0 + random.nextDouble() * 2.0;   // 39-41度
        data.setLongitudeLatitude(longitude, latitude);
        
        // 模拟随机航向
        data.setHeadingDegrees(random.nextDouble() * 360.0);
        
        // 模拟随机速度
        data.setSpeedKmh(random.nextDouble() * 120.0);
        
        // 模拟随机角度
        data.setYawRateDegrees(random.nextInt(241) - 120); // -120到120
        data.setRollAngleDegrees(random.nextInt(181) - 90); // -90到90
        data.setPitchAngleDegrees(random.nextInt(181) - 90); // -90到90
        
        return data;
    }

    /**
     * 构建JSON数据
     */
    private String buildJsonData(UWBVehicleData vehicleData) {
        return String.format(
            "{\n" +
            "  \"vid\": %d,\n" +
            "  \"coordinateX\": %d,\n" +
            "  \"coordinateY\": %d,\n" +
            "  \"heading\": %d,\n" +
            "  \"speed\": %d,\n" +
            "  \"yawRate\": %d,\n" +
            "  \"rollAngle\": %d,\n" +
            "  \"pitchAngle\": %d\n" +
            "}",
            vehicleData.getVid(),
            vehicleData.getCoordinateX(),
            vehicleData.getCoordinateY(),
            vehicleData.getHeading(),
            vehicleData.getSpeed(),
            vehicleData.getYawRate(),
            vehicleData.getRollAngle(),
            vehicleData.getPitchAngle()
        );
    }
}
