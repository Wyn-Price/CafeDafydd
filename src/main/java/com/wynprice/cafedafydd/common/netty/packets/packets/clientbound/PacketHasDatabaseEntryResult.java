package com.wynprice.cafedafydd.common.netty.packets.packets.clientbound;

import io.netty.buffer.ByteBuf;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
public class PacketHasDatabaseEntryResult {
    private final boolean result;

    public static void encode(PacketHasDatabaseEntryResult packet, ByteBuf buf) {
        buf.writeBoolean(packet.result);
    }

    public static PacketHasDatabaseEntryResult decode(ByteBuf buf) {
        return new PacketHasDatabaseEntryResult(buf.readBoolean());
    }
}
