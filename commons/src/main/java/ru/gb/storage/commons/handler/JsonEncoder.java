package ru.gb.storage.commons.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import ru.gb.storage.commons.message.AbstractMessage;

public class JsonEncoder extends MessageToByteEncoder<AbstractMessage> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    protected void encode(ChannelHandlerContext ctx, AbstractMessage msg, ByteBuf out) throws Exception {
        byte[] value = OBJECT_MAPPER.writeValueAsBytes(msg);
        out.writeBytes(value);
    }
}
