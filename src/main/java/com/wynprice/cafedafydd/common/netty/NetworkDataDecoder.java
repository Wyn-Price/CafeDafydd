package com.wynprice.cafedafydd.common.netty;

import com.wynprice.cafedafydd.common.netty.packets.PacketEntry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * The packet decoder. Used for inbound packets.
 * @see PacketRegistry
 */
public class NetworkDataDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        PacketEntry<?> entry = PacketRegistry.getEntry(in.readByte());
        out.add(entry.getDecoder().apply(in));
    }
}
