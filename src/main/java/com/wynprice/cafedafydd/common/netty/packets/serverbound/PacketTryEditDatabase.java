package com.wynprice.cafedafydd.common.netty.packets.serverbound;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Value;

import java.util.stream.IntStream;

@Value
public class PacketTryEditDatabase {
    private final String database;
    private final int recordID;
    private final String[] form;

    public PacketTryEditDatabase(String database, int recordID, String... form) {
        this.database = database;
        this.recordID = recordID;
        this.form = form;
    }

    public static void encode(PacketTryEditDatabase packet, ByteBuf buf) {
        ByteBufUtils.writeString(packet.database, buf);
        buf.writeInt(packet.recordID);
        buf.writeShort(packet.form.length);
        for (String s : packet.form) {
            ByteBufUtils.writeString(s, buf);
        }
    }

    public static PacketTryEditDatabase decode(ByteBuf buf) {
        return new PacketTryEditDatabase(
            ByteBufUtils.readString(buf),
            buf.readInt(),
            IntStream.range(0, buf.readShort()).mapToObj(i -> ByteBufUtils.readString(buf)).toArray(String[]::new)
        );
    }
}
