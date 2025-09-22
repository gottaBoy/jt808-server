package org.yzh.protocol.uwb;

import io.github.yezhihao.protostar.SchemaManager;
import io.github.yezhihao.protostar.schema.RuntimeSchema;
import io.github.yezhihao.protostar.util.ArrayMap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * UWB消息解码器
 * 支持小端字节序解码
 * 
 * @author yzh
 */
@Slf4j
public class UWBMessageDecoder extends ByteToMessageDecoder {

    private static final int SIGNATURE = 0x3C3C; // 特征字
    private static final int MIN_FRAME_LENGTH = 18; // 最小帧长度：特征字(2) + 优先码(1) + 长度(1) + 目标地址(4) + 目标端口(2) + 源地址(4) + 源端口(2) + CRC(2)
    private static final int MAX_FRAME_LENGTH = 255; // 最大帧长度

    private final SchemaManager schemaManager;
    private final ArrayMap<RuntimeSchema> frameSchemaMap;
    private final UWBCRC16 crc16 = new UWBCRC16();

    public UWBMessageDecoder() {
        this.schemaManager = new SchemaManager("org.yzh.protocol.uwb");
        this.frameSchemaMap = schemaManager.getRuntimeSchema(UWBFrame.class);
    }

    public UWBMessageDecoder(SchemaManager schemaManager) {
        this.schemaManager = schemaManager;
        this.frameSchemaMap = schemaManager.getRuntimeSchema(UWBFrame.class);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        UWBFrame frame = decode(in);
        if (frame != null) {
            out.add(frame);
        }
    }

    /**
     * 解码UWB帧
     */
    public UWBFrame decode(ByteBuf in) {
        while (in.readableBytes() >= MIN_FRAME_LENGTH) {
            // 查找特征字
            int signatureIndex = findSignature(in);
            if (signatureIndex == -1) {
                // 没有找到特征字，丢弃所有数据
                in.skipBytes(in.readableBytes());
                return null;
            }

            // 跳过特征字之前的数据
            if (signatureIndex > 0) {
                in.skipBytes(signatureIndex);
            }

            // 标记读取位置
            in.markReaderIndex();

            // 检查是否有足够的数据读取完整帧
            if (in.readableBytes() < MIN_FRAME_LENGTH) {
                in.resetReaderIndex();
                return null;
            }

            // 读取数据长度
            byte dataLength = in.getByte(in.readerIndex() + 3); // 特征字(2) + 优先码(1) + 长度(1)
            
            // 检查数据长度是否合理
            if (dataLength < MIN_FRAME_LENGTH || dataLength > MAX_FRAME_LENGTH) {
                log.warn("Invalid data length: {}, expected range: {} - {}", dataLength, MIN_FRAME_LENGTH, MAX_FRAME_LENGTH);
                in.skipBytes(1); // 跳过当前字节，继续查找下一个特征字
                continue;
            }

            // 检查是否有足够的数据读取完整帧
            if (in.readableBytes() < dataLength) {
                in.resetReaderIndex();
                return null;
            }

            // 读取完整帧数据
            byte[] frameData = new byte[dataLength];
            in.readBytes(frameData);

            try {
                // 验证CRC
                if (!verifyCRC(frameData)) {
                    log.warn("CRC verification failed for frame");
                    continue;
                }

                // 解码帧数据
                UWBFrame frame = decodeFrame(frameData);
                if (frame != null) {
                    return frame;
                }
            } catch (Exception e) {
                log.error("Failed to decode UWB frame", e);
            }
        }
        return null;
    }

    /**
     * 查找特征字位置
     */
    private int findSignature(ByteBuf in) {
        int readableBytes = in.readableBytes();
        for (int i = 0; i <= readableBytes - 2; i++) {
            if (in.getUnsignedShortLE(in.readerIndex() + i) == SIGNATURE) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 验证CRC校验码
     */
    private boolean verifyCRC(byte[] frameData) {
        if (frameData.length < 2) {
            return false;
        }

        // 计算CRC（不包括最后的CRC字段）
        byte[] dataWithoutCRC = new byte[frameData.length - 2];
        System.arraycopy(frameData, 0, dataWithoutCRC, 0, dataWithoutCRC.length);
        
        int calculatedCRC = crc16.calculate(dataWithoutCRC);
        
        // 读取帧中的CRC（小端序）
        int frameCRC = ((frameData[frameData.length - 1] & 0xFF) << 8) | 
                       (frameData[frameData.length - 2] & 0xFF);

        return calculatedCRC == frameCRC;
    }

    /**
     * 解码帧数据
     */
    private UWBFrame decodeFrame(byte[] frameData) {
        try {
            // 创建ByteBuf用于解码
            ByteBuf buffer = io.netty.buffer.Unpooled.wrappedBuffer(frameData);
            
            // 解码UWBFrame (使用版本0的schema)
            @SuppressWarnings("unchecked")
            RuntimeSchema<UWBFrame> frameSchema = (RuntimeSchema<UWBFrame>) frameSchemaMap.get(0);
            UWBFrame frame = frameSchema.newInstance();
            frameSchema.mergeFrom(buffer, frame, null);
            
            // 释放buffer
            buffer.release();
            
            return frame;
        } catch (Exception e) {
            log.error("Failed to decode UWB frame", e);
            return null;
        }
    }
}
