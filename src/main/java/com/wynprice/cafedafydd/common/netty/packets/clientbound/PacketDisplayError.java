package com.wynprice.cafedafydd.common.netty.packets.clientbound;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Value;

@Value
public class PacketDisplayError {
    private final String title;
    private final String reason;

    public static void encode(PacketDisplayError packet, ByteBuf buf) {
        ByteBufUtils.writeString(packet.title, buf);
        ByteBufUtils.writeString(packet.reason, buf);
    }

    public static PacketDisplayError decode(ByteBuf buf) {
        return new PacketDisplayError(ByteBufUtils.readString(buf), ByteBufUtils.readString(buf));
    }
}
