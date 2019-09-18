package com.wynprice.cafedafydd.common.netty.packets.serverbound;

import io.netty.buffer.ByteBuf;
import lombok.Value;

@Value
public class PacketStartSession {
    private final int computerID;

    public static void encode(PacketStartSession packet, ByteBuf buf) {
        buf.writeInt(packet.computerID);
    }

    public static PacketStartSession decode(ByteBuf buf) {
        return new PacketStartSession(buf.readInt());
    }
}
