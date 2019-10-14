package com.wynprice.cafedafydd.common.netty.packets.serverbound;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Value;

@Value
public class PacketRequestBackupEntry {
    private final int requestID;
    private final String database;
    private final int headerID;

    public static void encode(PacketRequestBackupEntry packet, ByteBuf buf) {
        buf.writeInt(packet.requestID);
        ByteBufUtils.writeString(packet.database, buf);
        buf.writeInt(packet.headerID);
    }

    public static PacketRequestBackupEntry decode(ByteBuf buf) {
        return new PacketRequestBackupEntry(buf.readInt(), ByteBufUtils.readString(buf), buf.readInt());
    }
}
