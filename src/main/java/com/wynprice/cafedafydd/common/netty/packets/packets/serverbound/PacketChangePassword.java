package com.wynprice.cafedafydd.common.netty.packets.packets.serverbound;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Value;

@Value
public class PacketChangePassword {
    private final String currentPasswordHash;
    private final String newPasswordHash;

    public static void encode(PacketChangePassword packet, ByteBuf buf) {
        ByteBufUtils.writeString(packet.currentPasswordHash, buf);
        ByteBufUtils.writeString(packet.newPasswordHash, buf);
    }

    public static PacketChangePassword decode(ByteBuf buf) {
        return new PacketChangePassword(ByteBufUtils.readString(buf), ByteBufUtils.readString(buf));
    }

}
