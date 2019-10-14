package com.wynprice.cafedafydd.common.netty.packets.serverbound;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Value;

@Value
public class PacketRevertDatabase {
    private final String database;
    private final int backupId;

    public static void encode(PacketRevertDatabase packet, ByteBuf buf) {
        ByteBufUtils.writeString(packet.database, buf);
        buf.writeInt(packet.backupId);
    }

    public static PacketRevertDatabase decode(ByteBuf buf) {
        return new PacketRevertDatabase(ByteBufUtils.readString(buf), buf.readInt());
    }
}
