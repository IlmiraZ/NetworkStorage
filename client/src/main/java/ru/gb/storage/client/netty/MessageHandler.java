package ru.gb.storage.client.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.gb.storage.commons.message.AbstractMessage;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private final CallBack callBack;

    public MessageHandler(CallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage msg) {
        callBack.call(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("NettyClient exception", cause);
        ctx.close();
    }
}
