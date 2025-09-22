package org.yzh.web.endpoint;

import io.github.yezhihao.netmc.core.annotation.Async;
import io.github.yezhihao.netmc.core.annotation.AsyncBatch;
import io.github.yezhihao.netmc.core.annotation.Endpoint;
import io.github.yezhihao.netmc.core.annotation.Mapping;
import io.github.yezhihao.netmc.session.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yzh.protocol.uwb.UWBFrame;
import org.yzh.protocol.uwb.UWBVehicleData;
import org.yzh.protocol.uwb.UWBUtils;
import org.yzh.web.model.entity.UWBDeviceDO;
import org.yzh.web.model.enums.SessionKey;
import org.yzh.web.service.UWBDataService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UWB定位数据消息处理端点
 * 
 * @author yzh
 */
@Slf4j
@Endpoint
@Component
@RequiredArgsConstructor
public class UWBEndpoint {

    private final UWBDataService uwbDataService;

    /**
     * 处理UWB定位数据帧
     * 支持异步批量处理以提高性能
     */
    @AsyncBatch(poolSize = 2, maxElements = 1000, maxWait = 500)
    @Mapping(types = 0x8001, desc = "UWB定位数据帧")
    public void handleUWBFrame(List<UWBFrame> frames) {
        log.info("Received {} UWB frames for batch processing", frames.size());
        
        for (UWBFrame frame : frames) {
            try {
                processUWBFrame(frame);
            } catch (Exception e) {
                log.error("Failed to process UWB frame: VID={}", 
                         frame.getVehicleData() != null ? frame.getVehicleData().getVid() : 0, e);
            }
        }
    }

    /**
     * 处理单个UWB定位数据帧
     */
    @Async
    @Mapping(types = 0x8002, desc = "UWB定位数据帧(单个)")
    public void handleSingleUWBFrame(UWBFrame frame, Session session) {
        log.debug("Received single UWB frame: VID={}, Source={}:{}", 
                 frame.getVehicleData() != null ? frame.getVehicleData().getVid() : 0,
                 frame.getSourceIP(), frame.getSourcePort());
        
        try {
            processUWBFrame(frame);
            
            // 更新会话信息
            if (frame.getVehicleData() != null) {
                updateSessionInfo(session, frame);
            }
            
        } catch (Exception e) {
            log.error("Failed to process single UWB frame", e);
        }
    }

    /**
     * 处理UWB定位数据帧的核心逻辑
     */
    private void processUWBFrame(UWBFrame frame) {
        UWBVehicleData vehicleData = frame.getVehicleData();
        if (vehicleData == null) {
            log.warn("UWB frame contains no vehicle data");
            return;
        }

        // 记录接收到的数据
        log.info("Processing UWB data: {}", vehicleData);

        // 数据验证
        if (!UWBUtils.validateVehicleData(vehicleData)) {
            log.warn("Invalid vehicle data: VID={}", vehicleData.getVid());
            return;
        }

        // 保存到数据库
        uwbDataService.saveVehicleData(vehicleData, frame);

        // 转发到目标地址（如果需要）
        if (shouldForward(frame)) {
            uwbDataService.forwardToTarget(frame);
        }

        // 触发业务逻辑
        uwbDataService.processVehicleData(vehicleData);
    }

    /**
     * 更新会话信息
     */
    private void updateSessionInfo(Session session, UWBFrame frame) {
        UWBVehicleData vehicleData = frame.getVehicleData();
        if (vehicleData == null) {
            return;
        }

        // 创建或更新设备信息
        UWBDeviceDO device = new UWBDeviceDO();
        device.setVid(vehicleData.getVid());
        device.setLastUpdateTime(LocalDateTime.now());
        device.setSourceIP(frame.getSourceIP());
        device.setSourcePort(frame.getSourcePort());
        device.setCoordinateX(vehicleData.getCoordinateX());
        device.setCoordinateY(vehicleData.getCoordinateY());
        device.setHeading(vehicleData.getHeading());
        device.setSpeed(vehicleData.getSpeed());

        session.setAttribute(SessionKey.UWBDevice, device);
    }


    /**
     * 判断是否需要转发
     */
    private boolean shouldForward(UWBFrame frame) {
        // 检查目标地址是否有效（不全为0）
        byte[] targetAddr = frame.getTargetAddress();
        for (byte b : targetAddr) {
            if (b != 0) {
                return true;
            }
        }
        return false;
    }
}
