package com.wynprice.cafedafydd.common.netty.packets.serverbound;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Value;

@Value
public class PacketEditRecordDirect {
    private final String database;
    private final int recordID; //If -1, then a new record is added. Otherwise the record is removed.

    public static void encode(PacketEditRecordDirect packet, ByteBuf buf) {
        ByteBufUtils.writeString(packet.database, buf);
        buf.writeInt(packet.recordID);
    }

    public static PacketEditRecordDirect decode(ByteBuf buf) {
        return new PacketEditRecordDirect(
            ByteBufUtils.readString(buf),
            buf.readInt()
        );
    }
}
