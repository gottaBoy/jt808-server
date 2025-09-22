package org.yzh.protocol.uwb;

import io.github.yezhihao.protostar.SchemaManager;
import io.github.yezhihao.protostar.schema.RuntimeSchema;
import io.github.yezhihao.protostar.util.ArrayMap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * UWB消息编码器
 * 支持小端字节序编码
 * 
 * @author yzh
 */
@Slf4j
public class UWBMessageEncoder extends MessageToByteEncoder<UWBFrame> {

    private final SchemaManager schemaManager;
    private final ArrayMap<RuntimeSchema> frameSchemaMap;
    private final UWBCRC16 crc16 = new UWBCRC16();

    public UWBMessageEncoder() {
        this.schemaManager = new SchemaManager("org.yzh.protocol.uwb");
        this.frameSchemaMap = schemaManager.getRuntimeSchema(UWBFrame.class);
    }

    public UWBMessageEncoder(SchemaManager schemaManager) {
        this.schemaManager = schemaManager;
        this.frameSchemaMap = schemaManager.getRuntimeSchema(UWBFrame.class);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, UWBFrame frame, ByteBuf out) throws Exception {
        ByteBuf result = encode(frame);
        out.writeBytes(result);
        result.release();
    }

    /**
     * 编码UWB帧
     */
    public ByteBuf encode(UWBFrame frame) {
        try {
            // 计算数据长度（不包括CRC）
            int dataLength = calculateDataLength(frame);
            frame.setDataLength((byte) dataLength);

            // 编码帧数据（不包括CRC）
            ByteBuf frameBuffer = io.netty.buffer.Unpooled.buffer();
            @SuppressWarnings("unchecked")
            RuntimeSchema<UWBFrame> frameSchema = (RuntimeSchema<UWBFrame>) frameSchemaMap.get(0);
            frameSchema.writeTo(frameBuffer, frame);

            // 计算CRC
            byte[] frameData = new byte[frameBuffer.readableBytes()];
            frameBuffer.getBytes(0, frameData);
            int crc = crc16.calculate(frameData);
            frame.setCrc16(crc);

            // 重新编码包含CRC的完整帧
            frameBuffer.clear();
            frameSchema.writeTo(frameBuffer, frame);

            log.debug("Encoded UWB frame: VID={}, Length={}, CRC=0x{}", 
                     frame.getVehicleData() != null ? frame.getVehicleData().getVid() : 0,
                     dataLength, 
                     Integer.toHexString(crc).toUpperCase());

            return frameBuffer;

        } catch (Exception e) {
            log.error("Failed to encode UWB frame", e);
            throw new RuntimeException("Failed to encode UWB frame", e);
        }
    }

    /**
     * 计算数据长度（不包括CRC）
     */
    private int calculateDataLength(UWBFrame frame) {
        int length = 2; // 特征字
        length += 1; // 传输优先码
        length += 1; // 数据长度字段
        length += 4; // 目标地址
        length += 2; // 目标端口号
        length += 4; // 源地址
        length += 2; // 源端口号
        
        if (frame.getVehicleData() != null) {
            length += frame.getVehicleData().getDataLength();
        }
        
        return length;
    }
}
