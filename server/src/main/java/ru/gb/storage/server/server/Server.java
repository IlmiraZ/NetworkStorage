package ru.gb.storage.server.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;
import ru.gb.storage.commons.handler.JsonDecoder;
import ru.gb.storage.commons.handler.JsonEncoder;
import ru.gb.storage.server.db.Database;
import ru.gb.storage.server.handler.ServerHandler;

@Slf4j
public class Server {
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        try {
            Database.openDB();
        } catch (Exception e) {
            log.error("Database open error", e);
            System.exit(0);
        }

        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap server = new ServerBootstrap();
            server
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
                                    new LengthFieldPrepender(3),
                                    new JsonDecoder(),
                                    new JsonEncoder(),
                                    new ServerHandler()
                            );
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = server.bind(port).sync();

            log.info("Server started...");

            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            Database.closeDB();
        }
    }

}
