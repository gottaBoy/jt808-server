package org.yzh.web.config;

import io.github.yezhihao.netmc.NettyConfig;
import io.github.yezhihao.netmc.Server;
import io.github.yezhihao.netmc.core.HandlerMapping;
import io.github.yezhihao.netmc.session.SessionManager;
import io.github.yezhihao.protostar.SchemaManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.yzh.protocol.uwb.UWBMessageDecoder;
import org.yzh.protocol.uwb.UWBMessageEncoder;
import org.yzh.web.endpoint.JTHandlerInterceptor;
import org.yzh.web.endpoint.JTSessionListener;
import org.yzh.web.endpoint.UWBMessageAdapter;
import org.yzh.web.model.enums.SessionKey;

/**
 * UWB服务器配置
 * 支持TCP、UDP协议
 * 
 * @author yzh
 */
@Order(Integer.MIN_VALUE + 1)
@Configuration
@ConditionalOnProperty(value = "jt-server.uwb.enabled", havingValue = "true", matchIfMissing = false)
public class UWBConfig {

    /**
     * UWB TCP服务器
     */
    @ConditionalOnProperty(value = "jt-server.uwb.tcp-port")
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server uwbTCPServer(HandlerMapping handlerMapping,
                              JTHandlerInterceptor handlerInterceptor,
                              SessionManager sessionManager,
                              JTProperties jtProperties) {
        UWBMessageAdapter adapter = new UWBMessageAdapter(new UWBMessageEncoder(), new UWBMessageDecoder());
        return NettyConfig.custom()
                .setIdleStateTime(jtProperties.getUwbIdleTimeout(), 0, 0)
                .setPort(jtProperties.getUwbTcpPort())
                .setMaxFrameLength(255) // UWB协议最大帧长度255字节
                .setDecoder(adapter)
                .setEncoder(adapter)
                .setHandlerMapping(handlerMapping)
                .setHandlerInterceptor(handlerInterceptor)
                .setSessionManager(sessionManager)
                .setName("UWB-TCP")
                .build();
    }

    /**
     * UWB UDP服务器
     */
    @ConditionalOnProperty(value = "jt-server.uwb.udp-port")
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server uwbUDPServer(HandlerMapping handlerMapping,
                              JTHandlerInterceptor handlerInterceptor,
                              SessionManager sessionManager,
                              JTProperties jtProperties) {
        UWBMessageAdapter adapter = new UWBMessageAdapter(new UWBMessageEncoder(), new UWBMessageDecoder());
        return NettyConfig.custom()
                .setIdleStateTime(jtProperties.getUwbIdleTimeout(), 0, 0)
                .setPort(jtProperties.getUwbUdpPort())
                .setMaxFrameLength(255) // UWB协议最大帧长度255字节
                .setDecoder(adapter)
                .setEncoder(adapter)
                .setHandlerMapping(handlerMapping)
                .setHandlerInterceptor(handlerInterceptor)
                .setSessionManager(sessionManager)
                .setName("UWB-UDP")
                .setEnableUDP(true)
                .build();
    }

    /**
     * UWB消息适配器
     */
    @Bean
    public UWBMessageAdapter uwbMessageAdapter(SchemaManager schemaManager) {
        UWBMessageEncoder messageEncoder = new UWBMessageEncoder(schemaManager);
        UWBMessageDecoder messageDecoder = new UWBMessageDecoder(schemaManager);
        return new UWBMessageAdapter(messageEncoder, messageDecoder);
    }

    /**
     * UWB会话管理器
     */
    @Bean
    public SessionManager uwbSessionManager(JTSessionListener sessionListener) {
        return new SessionManager(SessionKey.class, sessionListener);
    }
}
