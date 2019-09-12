package com.wynprice.cafedafydd.common.netty.packets.serverbound;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Value;

@Value
public class PacketLogin {
    private final String username;
    private final String passwordHash;

    public static void encode(PacketLogin packet, ByteBuf buf) {
        ByteBufUtils.writeString(packet.username, buf);
        ByteBufUtils.writeString(packet.passwordHash, buf);
    }

    public static PacketLogin decode(ByteBuf buf) {
        return new PacketLogin(ByteBufUtils.readString(buf), ByteBufUtils.readString(buf));
    }
}
