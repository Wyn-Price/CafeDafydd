package com.wynprice.cafedafydd.common.netty.packets.packets.clientbound;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Value;

@Value
public class PacketLoginIncorrect {
    private final String reason;

    public static void encode(PacketLoginIncorrect packet, ByteBuf buf) {
        ByteBufUtils.writeString(packet.reason, buf);
    }

    public static PacketLoginIncorrect decode(ByteBuf buf) {
        return new PacketLoginIncorrect(ByteBufUtils.readString(buf));
    }
}
