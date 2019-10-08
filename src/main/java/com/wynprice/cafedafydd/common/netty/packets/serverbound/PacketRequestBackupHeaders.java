package com.wynprice.cafedafydd.common.netty.packets.serverbound;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Value;

@Value
public class PacketRequestBackupHeaders {
    private final int requestID;
    private final String database;

    public static void encode(PacketRequestBackupHeaders packet, ByteBuf buf) {
        buf.writeInt(packet.requestID);
        ByteBufUtils.writeString(packet.database, buf);
    }

    public static PacketRequestBackupHeaders decode(ByteBuf buf) {
        return new PacketRequestBackupHeaders(buf.readInt(), ByteBufUtils.readString(buf));
    }
}
