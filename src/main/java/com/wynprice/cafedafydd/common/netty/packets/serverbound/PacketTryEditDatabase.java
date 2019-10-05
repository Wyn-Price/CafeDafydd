package com.wynprice.cafedafydd.common.netty.packets.serverbound;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import com.wynprice.cafedafydd.common.utils.NamedRecord;
import io.netty.buffer.ByteBuf;
import lombok.Value;

import java.util.stream.IntStream;

@Value
public class PacketTryEditDatabase {
    private final String database;
    private final int recordID;
    private final NamedRecord[] form;

    public PacketTryEditDatabase(String database, int recordID, NamedRecord... form) {
        this.database = database;
        this.recordID = recordID;
        this.form = form;
    }

    public static void encode(PacketTryEditDatabase packet, ByteBuf buf) {
        ByteBufUtils.writeString(packet.database, buf);
        buf.writeInt(packet.recordID);
        NamedRecord.write(packet.form, buf);
    }

    public static PacketTryEditDatabase decode(ByteBuf buf) {
        return new PacketTryEditDatabase(
            ByteBufUtils.readString(buf),
            buf.readInt(),
            NamedRecord.read(buf)
        );
    }
}
