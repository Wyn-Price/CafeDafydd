package com.wynprice.cafedafydd.common.netty;

import com.wynprice.cafedafydd.common.netty.packets.PacketEntry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * The packet encoder. Used for outbound packets.
 * @see PacketRegistry
 */
public class NetworkDataEncoder extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        PacketEntry<Object> entry = PacketRegistry.INSTANCE.getEntry(msg);
        out.writeByte(entry.getIndex());
        entry.getEncoder().accept(msg, out);
    }

}
