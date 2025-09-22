package org.yzh.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yzh.protocol.uwb.UWBFrame;
import org.yzh.protocol.uwb.UWBVehicleData;
import org.yzh.protocol.uwb.UWBUtils;
import org.yzh.web.model.entity.UWBDeviceDO;
import org.yzh.web.model.entity.UWBLocationDO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * UWB数据处理服务
 * 
 * @author yzh
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UWBDataService {

    // 这里可以注入数据库操作相关的Repository
    // private final UWBLocationRepository locationRepository;
    // private final UWBDeviceRepository deviceRepository;

    /**
     * 保存车辆数据到数据库
     */
    public void saveVehicleData(UWBVehicleData vehicleData, UWBFrame frame) {
        try {
            // 创建位置记录
            UWBLocationDO location = new UWBLocationDO();
            location.setVid(vehicleData.getVid());
            location.setCoordinateX(vehicleData.getCoordinateX());
            location.setCoordinateY(vehicleData.getCoordinateY());
            location.setHeading(vehicleData.getHeading());
            location.setSpeed(vehicleData.getSpeed());
            location.setYawRate(vehicleData.getYawRate());
            location.setRollAngle(vehicleData.getRollAngle());
            location.setPitchAngle(vehicleData.getPitchAngle());
            location.setSourceIP(frame.getSourceIP());
            location.setSourcePort(frame.getSourcePort());
            location.setTargetIP(frame.getTargetIP());
            location.setTargetPort(frame.getTargetPort());
            location.setCreateTime(LocalDateTime.now());

            // 异步保存到数据库
            CompletableFuture.runAsync(() -> {
                try {
                    // locationRepository.save(location);
                    log.debug("Saved UWB location data: VID={}, X={}, Y={}", 
                             vehicleData.getVid(), vehicleData.getCoordinateX(), vehicleData.getCoordinateY());
                } catch (Exception e) {
                    log.error("Failed to save UWB location data", e);
                }
            });

            // 更新设备信息
            updateDeviceInfo(vehicleData, frame);

        } catch (Exception e) {
            log.error("Failed to save vehicle data", e);
        }
    }

    /**
     * 更新设备信息
     */
    private void updateDeviceInfo(UWBVehicleData vehicleData, UWBFrame frame) {
        try {
            UWBDeviceDO device = new UWBDeviceDO();
            device.setVid(vehicleData.getVid());
            device.setLastUpdateTime(LocalDateTime.now());
            device.setSourceIP(frame.getSourceIP());
            device.setSourcePort(frame.getSourcePort());
            device.setCoordinateX(vehicleData.getCoordinateX());
            device.setCoordinateY(vehicleData.getCoordinateY());
            device.setHeading(vehicleData.getHeading());
            device.setSpeed(vehicleData.getSpeed());
            device.setYawRate(vehicleData.getYawRate());
            device.setRollAngle(vehicleData.getRollAngle());
            device.setPitchAngle(vehicleData.getPitchAngle());

            // 异步更新设备信息
            CompletableFuture.runAsync(() -> {
                try {
                    // deviceRepository.saveOrUpdate(device);
                    log.debug("Updated UWB device info: VID={}", vehicleData.getVid());
                } catch (Exception e) {
                    log.error("Failed to update device info", e);
                }
            });

        } catch (Exception e) {
            log.error("Failed to update device info", e);
        }
    }

    /**
     * 转发数据到目标地址
     */
    public void forwardToTarget(UWBFrame frame) {
        try {
            log.info("Forwarding UWB data to target: {}:{}", frame.getTargetIP(), frame.getTargetPort());
            
            // 这里可以实现具体的转发逻辑
            // 例如：通过UDP发送到目标地址
            // udpForwardService.send(frame, frame.getTargetIP(), frame.getTargetPort());
            
        } catch (Exception e) {
            log.error("Failed to forward UWB data to target", e);
        }
    }

    /**
     * 处理车辆数据的业务逻辑
     */
    public void processVehicleData(UWBVehicleData vehicleData) {
        try {
            // 这里可以实现具体的业务逻辑
            // 例如：轨迹分析、异常检测、告警处理等
            
            // 检查速度异常
            UWBUtils.SpeedLevel speedLevel = UWBUtils.getSpeedLevel(vehicleData.getSpeedKmh());
            if (speedLevel == UWBUtils.SpeedLevel.VERY_HIGH) {
                log.warn("High speed detected: VID={}, Speed={}km/h, Level={}", 
                        vehicleData.getVid(), vehicleData.getSpeedKmh(), speedLevel.getDescription());
                // 触发告警
                triggerSpeedAlarm(vehicleData);
            }

            // 检查角度异常
            if (Math.abs(vehicleData.getRollAngleDegrees()) > 45 || 
                Math.abs(vehicleData.getPitchAngleDegrees()) > 45) {
                log.warn("Abnormal angle detected: VID={}, Roll={}°, Pitch={}°", 
                        vehicleData.getVid(), 
                        vehicleData.getRollAngleDegrees(), 
                        vehicleData.getPitchAngleDegrees());
                // 触发告警
                triggerAngleAlarm(vehicleData);
            }

            // 检查区域异常
            checkAreaAlarm(vehicleData);

        } catch (Exception e) {
            log.error("Failed to process vehicle data", e);
        }
    }

    /**
     * 触发速度告警
     */
    private void triggerSpeedAlarm(UWBVehicleData vehicleData) {
        // 实现速度告警逻辑
        log.warn("Speed alarm triggered for VID: {}, Speed: {}km/h", 
                vehicleData.getVid(), vehicleData.getSpeedKmh());
    }

    /**
     * 触发角度告警
     */
    private void triggerAngleAlarm(UWBVehicleData vehicleData) {
        // 实现角度告警逻辑
        log.warn("Angle alarm triggered for VID: {}, Roll: {}°, Pitch: {}°", 
                vehicleData.getVid(), 
                vehicleData.getRollAngleDegrees(), 
                vehicleData.getPitchAngleDegrees());
    }

    /**
     * 检查区域告警
     */
    private void checkAreaAlarm(UWBVehicleData vehicleData) {
        try {
            // 检查是否在禁入区域
            if (isInRestrictedArea(vehicleData)) {
                log.warn("Vehicle in restricted area: VID={}, Location=({}, {})", 
                        vehicleData.getVid(), 
                        vehicleData.getLongitude(), 
                        vehicleData.getLatitude());
                triggerAreaAlarm(vehicleData);
            }

            // 检查是否偏离预定路线
            if (isOffRoute(vehicleData)) {
                log.warn("Vehicle off route: VID={}, Location=({}, {})", 
                        vehicleData.getVid(), 
                        vehicleData.getLongitude(), 
                        vehicleData.getLatitude());
                triggerRouteAlarm(vehicleData);
            }

        } catch (Exception e) {
            log.error("Failed to check area alarm for VID: {}", vehicleData.getVid(), e);
        }
    }

    /**
     * 检查是否在禁入区域
     */
    private boolean isInRestrictedArea(UWBVehicleData vehicleData) {
        // 这里可以实现具体的禁入区域检查逻辑
        // 例如：检查是否在军事禁区、危险区域等
        
        // 示例：检查是否在北京天安门广场附近（仅作演示）
        if (vehicleData.getCoordinateType() == UWBVehicleData.CoordinateType.LONGITUDE_LATITUDE) {
            return vehicleData.isInArea(116.39, 39.90, 116.40, 39.91);
        }
        
        return false;
    }

    /**
     * 检查是否偏离预定路线
     */
    private boolean isOffRoute(UWBVehicleData vehicleData) {
        // 这里可以实现具体的路线检查逻辑
        // 例如：检查是否偏离规划的行驶路线
        
        // 示例实现：检查是否在主要道路附近
        // 实际应用中需要根据具体的路线规划数据来判断
        return false;
    }

    /**
     * 触发区域告警
     */
    private void triggerAreaAlarm(UWBVehicleData vehicleData) {
        log.warn("Area alarm triggered for VID: {}, Location=({}, {})", 
                vehicleData.getVid(), 
                vehicleData.getLongitude(), 
                vehicleData.getLatitude());
    }

    /**
     * 触发路线告警
     */
    private void triggerRouteAlarm(UWBVehicleData vehicleData) {
        log.warn("Route alarm triggered for VID: {}, Location=({}, {})", 
                vehicleData.getVid(), 
                vehicleData.getLongitude(), 
                vehicleData.getLatitude());
    }

    /**
     * 获取设备最新位置
     */
    public UWBLocationDO getLatestLocation(int vid) {
        try {
            // return locationRepository.findLatestByVid(vid);
            return null; // 临时返回null，实际应该从数据库查询
        } catch (Exception e) {
            log.error("Failed to get latest location for VID: {}", vid, e);
            return null;
        }
    }

    /**
     * 获取设备历史轨迹
     */
    public List<UWBLocationDO> getLocationHistory(int vid, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // return locationRepository.findByVidAndTimeRange(vid, startTime, endTime);
            return null; // 临时返回null，实际应该从数据库查询
        } catch (Exception e) {
            log.error("Failed to get location history for VID: {}", vid, e);
            return null;
        }
    }

    /**
     * 获取在线设备列表
     */
    public List<UWBDeviceDO> getOnlineDevices() {
        try {
            // return deviceRepository.findOnlineDevices();
            return null; // 临时返回null，实际应该从数据库查询
        } catch (Exception e) {
            log.error("Failed to get online devices", e);
            return null;
        }
    }
}
