package com.wynprice.cafedafydd.common.netty;

import com.wynprice.cafedafydd.common.netty.packets.packets.clientbound.PacketDisplayError;
import com.wynprice.cafedafydd.common.utils.NetworkConsumer;
import com.wynprice.cafedafydd.server.utils.PermissionException;
import io.netty.channel.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayDeque;
import java.util.Queue;

@Log4j2
@RequiredArgsConstructor
public class NetworkHandler extends SimpleChannelInboundHandler {

    private final NetworkConsumer networkConsumer;

    private final Queue<Object> handleQueue = new ArrayDeque<>();
    @Getter
    private Channel activeChannel;

    private Thread handleThread;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        this.handleQueue.add(msg);
    }

    protected void handlePacket(Object packet) {
        this.networkConsumer.accept(this, packet);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("Established Connection to " + ctx.channel().remoteAddress());
        this.activeChannel = ctx.channel();

        this.handleThread = new Thread(() -> {
            while (this.activeChannel != null) {
                synchronized (this.handleQueue) {
                    while(!this.handleQueue.isEmpty()) {
                        Object packet = this.handleQueue.poll();
                        try {
                            this.handlePacket(packet);
                        } catch (Exception e) {
                            log.error("Error while handling packet " + packet, e);
                        }
                    }
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    this.handleThread.interrupt();
                }
            }
        });
        this.handleThread.setName("Network Handler Thread");
        this.handleThread.setDaemon(true);
        this.handleThread.start();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("Connection to " + ctx.channel().remoteAddress() + " aborted");
        this.activeChannel = null;
    }

    public boolean isChannelOpen() {
        return this.activeChannel != null && this.activeChannel.isOpen();
    }

    public void sendPacket(Object msg) {
        if(this.isChannelOpen()) {
            if(this.activeChannel.eventLoop().inEventLoop()) {
                this.dispatchPacket(msg);
            } else {
                this.activeChannel.eventLoop().execute(() -> this.dispatchPacket(msg));
            }
        }
    }

    private void dispatchPacket(Object msg) {
        ChannelFuture future = this.activeChannel.writeAndFlush(msg);
        future.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

}
