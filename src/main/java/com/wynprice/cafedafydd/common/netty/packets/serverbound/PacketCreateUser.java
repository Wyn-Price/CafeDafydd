package com.wynprice.cafedafydd.common.netty.packets.serverbound;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Value;

@Value
public class PacketCreateUser {
    private final String username;
    private final String email;
    private final String passwordHash;
    private byte permissionCreatorLevel;

    public static void encode(PacketCreateUser packet, ByteBuf buf) {
        ByteBufUtils.writeString(packet.username, buf);
        ByteBufUtils.writeString(packet.email, buf);
        ByteBufUtils.writeString(packet.passwordHash, buf);
        buf.writeByte(packet.permissionCreatorLevel);
    }

    public static PacketCreateUser decode(ByteBuf buf) {
        return new PacketCreateUser(ByteBufUtils.readString(buf), ByteBufUtils.readString(buf), ByteBufUtils.readString(buf), buf.readByte());
    }
}
