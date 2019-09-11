package com.wynprice.cafedafydd.common.netty.packets.packets.clientbound;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Value;

@Value
public class PacketConfirmLogin {
    private final String username;

    public static void encode(PacketConfirmLogin packet, ByteBuf buf) {
        ByteBufUtils.writeString(packet.username, buf);
    }

    public static PacketConfirmLogin decode(ByteBuf buf) {
        return new PacketConfirmLogin(ByteBufUtils.readString(buf));
    }
}
