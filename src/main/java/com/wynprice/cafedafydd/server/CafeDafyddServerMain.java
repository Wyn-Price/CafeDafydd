package com.wynprice.cafedafydd.server;

import com.wynprice.cafedafydd.common.netty.NetworkDataDecoder;
import com.wynprice.cafedafydd.common.netty.NetworkDataEncoder;
import com.wynprice.cafedafydd.server.database.Databases;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import javafx.application.Application;
import lombok.SneakyThrows;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * The server handler used for the setup of the server -> client connections.
 * This is also the server main class that gets invoked by the JVM
 */
public class CafeDafyddServerMain {

    private static ChannelFuture endpoint;

    public static void main(String[] args) {
        System.setProperty("logFilename", "server - " + DateTimeFormatter.ISO_INSTANT.format(Instant.now()));

        Application.launch(CafeDafyddServerApplication.class);

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        endpoint = new ServerBootstrap()
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) {
                    //Fixes weird issue with AdaptiveRecvByteBufAllocator whereas AdaptiveRecvByteBufAllocator#HandleImpl#guess would sometimes return not enough bytes.
                    ch.config().setRecvByteBufAllocator(new DefaultMaxBytesRecvByteBufAllocator());
                    ch.pipeline()
                        .addLast("decoder", new NetworkDataDecoder())
                        .addLast("encoder", new NetworkDataEncoder())
                        .addLast("handler", new ServerNetworkHandler());
                }
            })
            .group(bossGroup, workerGroup)
            .bind(5671).syncUninterruptibly();

        bootstrap();
    }

    public static void close() {
        endpoint.channel().close().syncUninterruptibly();
        Databases.close();
    }

    @SneakyThrows(ClassNotFoundException.class)
    private static void bootstrap() {
        Class.forName(ServerNetworkHandler.class.getName());
        Class.forName(Databases.class.getName());
    }
}
