package com.wynprice.cafedafydd.common.netty.packets.packets.clientbound;

import com.wynprice.cafedafydd.common.Page;
import io.netty.buffer.ByteBuf;
import lombok.Value;

@Value
public class PacketDisplayScreen {
    private final Page page;

    public static void encode(PacketDisplayScreen packet, ByteBuf buf) {
        buf.writeInt(packet.page.ordinal());
    }

    public static PacketDisplayScreen decode(ByteBuf buf) {
        return new PacketDisplayScreen(Page.values()[buf.readInt() % Page.values().length]);
    }
}
