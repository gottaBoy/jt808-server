package org.yzh.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.yzh.commons.model.R;
import org.yzh.protocol.uwb.UWBFrame;
import org.yzh.protocol.uwb.UWBVehicleData;
import org.yzh.web.model.entity.UWBDeviceDO;
import org.yzh.web.model.entity.UWBLocationDO;
import org.yzh.web.service.UWBDataService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UWB定位数据HTTP接口控制器
 * 
 * @author yzh
 */
@Slf4j
@RestController
@RequestMapping("uwb")
@RequiredArgsConstructor
@Tag(name = "UWB定位数据接口", description = "UWB定位车载端数据管理接口")
public class UWBController {

    private final UWBDataService uwbDataService;

    /**
     * 接收UWB定位数据（HTTP方式）
     */
    @Operation(summary = "接收UWB定位数据", description = "通过HTTP接口接收UWB定位车载端数据")
    @PostMapping("data")
    public R<String> receiveUWBData(@RequestBody UWBVehicleData vehicleData,
                                   @Parameter(description = "源IP地址") @RequestParam(required = false) String sourceIP,
                                   @Parameter(description = "源端口号") @RequestParam(required = false) Integer sourcePort,
                                   @Parameter(description = "目标IP地址") @RequestParam(required = false) String targetIP,
                                   @Parameter(description = "目标端口号") @RequestParam(required = false) Integer targetPort) {
        try {
            log.info("Received UWB data via HTTP: {}", vehicleData);

            // 创建UWB帧
            UWBFrame frame = new UWBFrame();
            frame.setVehicleData(vehicleData);
            if (sourceIP != null) {
                frame.setSourceIP(sourceIP);
            }
            if (sourcePort != null) {
                frame.setSourcePort(sourcePort);
            }
            if (targetIP != null) {
                frame.setTargetIP(targetIP);
            }
            if (targetPort != null) {
                frame.setTargetPort(targetPort);
            }

            // 处理数据
            uwbDataService.saveVehicleData(vehicleData, frame);
            uwbDataService.processVehicleData(vehicleData);

            return R.success("UWB数据接收成功");
        } catch (Exception e) {
            log.error("Failed to receive UWB data via HTTP", e);
            return R.error("UWB数据接收失败: " + e.getMessage());
        }
    }

    /**
     * 获取设备最新位置
     */
    @Operation(summary = "获取设备最新位置", description = "根据VID获取设备最新位置信息")
    @GetMapping("location/latest/{vid}")
    public R<UWBLocationDO> getLatestLocation(@Parameter(description = "车载端序列号VID") @PathVariable Integer vid) {
        try {
            UWBLocationDO location = uwbDataService.getLatestLocation(vid);
            if (location != null) {
                return R.success(location);
            } else {
                return R.error("未找到设备位置信息");
            }
        } catch (Exception e) {
            log.error("Failed to get latest location for VID: {}", vid, e);
            return R.error("获取设备位置失败: " + e.getMessage());
        }
    }

    /**
     * 获取设备历史轨迹
     */
    @Operation(summary = "获取设备历史轨迹", description = "根据VID和时间范围获取设备历史轨迹")
    @GetMapping("location/history/{vid}")
    public R<List<UWBLocationDO>> getLocationHistory(
            @Parameter(description = "车载端序列号VID") @PathVariable Integer vid,
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        try {
            List<UWBLocationDO> locations = uwbDataService.getLocationHistory(vid, startTime, endTime);
            return R.success(locations);
        } catch (Exception e) {
            log.error("Failed to get location history for VID: {}", vid, e);
            return R.error("获取设备轨迹失败: " + e.getMessage());
        }
    }

    /**
     * 获取在线设备列表
     */
    @Operation(summary = "获取在线设备列表", description = "获取所有在线的UWB设备")
    @GetMapping("devices/online")
    public R<List<UWBDeviceDO>> getOnlineDevices() {
        try {
            List<UWBDeviceDO> devices = uwbDataService.getOnlineDevices();
            return R.success(devices);
        } catch (Exception e) {
            log.error("Failed to get online devices", e);
            return R.error("获取在线设备失败: " + e.getMessage());
        }
    }

    /**
     * 获取设备详细信息
     */
    @Operation(summary = "获取设备详细信息", description = "根据VID获取设备详细信息")
    @GetMapping("device/{vid}")
    public R<UWBDeviceDO> getDeviceInfo(@Parameter(description = "车载端序列号VID") @PathVariable Integer vid) {
        try {
            // 这里应该从数据库查询设备信息
            // UWBDeviceDO device = uwbDataService.getDeviceInfo(vid);
            // return R.success(device);
            return R.error("功能待实现");
        } catch (Exception e) {
            log.error("Failed to get device info for VID: {}", vid, e);
            return R.error("获取设备信息失败: " + e.getMessage());
        }
    }

    /**
     * 发送UWB数据到指定目标
     */
    @Operation(summary = "发送UWB数据", description = "向指定目标发送UWB数据")
    @PostMapping("send")
    public R<String> sendUWBData(@RequestBody UWBVehicleData vehicleData,
                                @Parameter(description = "目标IP地址") @RequestParam String targetIP,
                                @Parameter(description = "目标端口号") @RequestParam Integer targetPort) {
        try {
            // 创建UWB帧
            UWBFrame frame = new UWBFrame();
            frame.setVehicleData(vehicleData);
            frame.setTargetIP(targetIP);
            frame.setTargetPort(targetPort);

            // 发送数据
            uwbDataService.forwardToTarget(frame);

            return R.success("UWB数据发送成功");
        } catch (Exception e) {
            log.error("Failed to send UWB data", e);
            return R.error("UWB数据发送失败: " + e.getMessage());
        }
    }

    /**
     * 获取UWB服务状态
     */
    @Operation(summary = "获取UWB服务状态", description = "获取UWB服务的运行状态")
    @GetMapping("status")
    public R<Object> getServiceStatus() {
        try {
            // 返回服务状态信息
            return R.success("UWB服务运行正常");
        } catch (Exception e) {
            log.error("Failed to get service status", e);
            return R.error("获取服务状态失败: " + e.getMessage());
        }
    }
}
