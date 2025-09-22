package org.yzh.web.endpoint;

import io.github.yezhihao.netmc.codec.MessageDecoder;
import io.github.yezhihao.netmc.codec.MessageEncoder;
import io.github.yezhihao.netmc.session.Session;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.extern.slf4j.Slf4j;
import org.yzh.protocol.uwb.UWBFrame;
import org.yzh.protocol.uwb.UWBMessageDecoder;
import org.yzh.protocol.uwb.UWBMessageEncoder;

/**
 * UWB消息适配器
 * 处理UWB消息的编码解码和会话管理
 * 
 * @author yzh
 */
@Slf4j
public class UWBMessageAdapter implements MessageEncoder<UWBFrame>, MessageDecoder<UWBFrame> {

    private final UWBMessageEncoder messageEncoder;
    private final UWBMessageDecoder messageDecoder;

    public UWBMessageAdapter(UWBMessageEncoder messageEncoder, UWBMessageDecoder messageDecoder) {
        this.messageEncoder = messageEncoder;
        this.messageDecoder = messageDecoder;
    }

    @Override
    public ByteBuf encode(UWBFrame message, Session session) {
        ByteBuf output = messageEncoder.encode(message);
        encodeLog(session, message, output);
        return output;
    }

    @Override
    public UWBFrame decode(ByteBuf input, Session session) {
        UWBFrame message = messageDecoder.decode(input);
        if (message != null) {
            // 设置会话信息（继承自JTMessage）
            message.setSession(session);
        }
        decodeLog(session, message, input);
        return message;
    }

    public void encodeLog(Session session, UWBFrame message, ByteBuf output) {
        if (log.isInfoEnabled()) {
            log.info("{}\n>>>>>-{},hex[{}]", session, message, ByteBufUtil.hexDump(output));
        }
    }

    public void decodeLog(Session session, UWBFrame message, ByteBuf input) {
        if (log.isInfoEnabled()) {
            log.info("{}\n<<<<<-{},hex[{}]", session, message, ByteBufUtil.hexDump(input, 0, input.writerIndex()));
        }
    }
}
