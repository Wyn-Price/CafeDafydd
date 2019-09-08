package com.wynprice.cafedafydd.server;

import com.wynprice.cafedafydd.common.netty.NetworkDataDecoder;
import com.wynprice.cafedafydd.common.netty.NetworkDataEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CafeDafyddServerMain {

    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        new ServerBootstrap()
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) {
                    ch.pipeline()
                        .addLast("decoder", new NetworkDataDecoder())
                        .addLast("encoder", new NetworkDataEncoder())
                        .addLast("handler", new ServerNetworkHandler());
                }
            })
            .group(bossGroup, workerGroup)
            .bind(5671).syncUninterruptibly();

    }
}
