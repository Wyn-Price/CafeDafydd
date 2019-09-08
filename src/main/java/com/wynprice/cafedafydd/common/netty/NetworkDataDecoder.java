package com.wynprice.cafedafydd.common.netty;

import com.wynprice.cafedafydd.common.netty.packets.PacketEntry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class NetworkDataDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        PacketEntry<?> entry = PacketRegistry.INSTANCE.getEntry(in.readInt());
        out.add(entry.getDecoder().apply(in));
    }
}
