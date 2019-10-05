package com.wynprice.cafedafydd.common.netty.packets.serverbound;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import com.wynprice.cafedafydd.common.utils.NamedRecord;
import com.wynprice.cafedafydd.common.utils.RequestType;
import io.netty.buffer.ByteBuf;
import lombok.Value;

@Value
public class PacketGetDatabaseEntries {
    private final RequestType type;
    private final int requestID;
    private final String database;
    private final NamedRecord[] requestForm;

    public static void encode(PacketGetDatabaseEntries packet, ByteBuf buf) {
        buf.writeShort(packet.type.ordinal());
        buf.writeInt(packet.requestID);
        ByteBufUtils.writeString(packet.database, buf);
        NamedRecord.write(packet.requestForm, buf);
    }

    public static PacketGetDatabaseEntries decode(ByteBuf buf) {
        return new PacketGetDatabaseEntries(
            RequestType.values()[buf.readShort() % RequestType.values().length],
            buf.readInt(),
            ByteBufUtils.readString(buf),
            NamedRecord.read(buf)
        );
    }

}
