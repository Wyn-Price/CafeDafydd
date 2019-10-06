package com.wynprice.cafedafydd.common.netty.packets.serverbound;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Value;

@Value
public class PacketEditDatabaseField {
    private final String database;
    private final int recordID;
    private final String field;
    private final String newValue;

    public static void encode(PacketEditDatabaseField packet, ByteBuf buf) {
        ByteBufUtils.writeString(packet.database, buf);
        buf.writeInt(packet.recordID);
        ByteBufUtils.writeString(packet.field, buf);
        ByteBufUtils.writeString(packet.newValue, buf);
    }

    public static PacketEditDatabaseField decode(ByteBuf buf) {
        return new PacketEditDatabaseField(
            ByteBufUtils.readString(buf),
            buf.readInt(),
            ByteBufUtils.readString(buf),
            ByteBufUtils.readString(buf)
        );
    }
}
