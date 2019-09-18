package com.wynprice.cafedafydd.common.netty.packets.serverbound;

import io.netty.buffer.ByteBuf;
import lombok.Value;

@Value
public class PacketStopSession {
    private final int sessionID;

    public static void encode(PacketStopSession packet, ByteBuf buf) {
        buf.writeInt(packet.sessionID);
    }

    public static PacketStopSession decode(ByteBuf buf) {
        return new PacketStopSession(buf.readInt());
    }
}
