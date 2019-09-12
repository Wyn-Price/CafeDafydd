package com.wynprice.cafedafydd.client.netty;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.common.Page;
import com.wynprice.cafedafydd.common.netty.NetworkDataDecoder;
import com.wynprice.cafedafydd.common.netty.NetworkDataEncoder;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.PacketLogout;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;

public class CafeDayfddClient {

    @Getter private final ClientNetworkHandler handler;

    private final ChannelFuture endpoint;

    public CafeDayfddClient(String address) {
        this.handler = new ClientNetworkHandler();
        this.endpoint = new Bootstrap()
            .group(new NioEventLoopGroup())
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline()
                        .addLast("encoder", new NetworkDataEncoder())
                        .addLast("decoder", new NetworkDataDecoder())
                        .addLast("handler", CafeDayfddClient.this.handler);
                }
            }).connect(address, 5671).syncUninterruptibly();
    }

    public void close() {
        this.endpoint.channel().close().syncUninterruptibly();
    }

    public void logout() {
        this.handler.setCurrentUsername(null);
        this.handler.sendPacket(new PacketLogout());
        CafeDafyddMain.showPage(Page.LOGIN_PAGE);
    }
}
