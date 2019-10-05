package com.wynprice.cafedafydd.common.netty.packets.serverbound;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import com.wynprice.cafedafydd.common.utils.NamedRecord;
import io.netty.buffer.ByteBuf;
import lombok.Value;

import java.util.stream.IntStream;

@Value
public class PacketHasDatabaseEntry {
    private final int requestID;
    private final String databaseFile;
    private final NamedRecord[] form;

    public static void encode(PacketHasDatabaseEntry packet, ByteBuf buf) {
        buf.writeInt(packet.requestID);
        ByteBufUtils.writeString(packet.databaseFile, buf);
        NamedRecord.write(packet.form, buf);
    }

    public static PacketHasDatabaseEntry decode(ByteBuf buf) {
        return new PacketHasDatabaseEntry(buf.readInt(), ByteBufUtils.readString(buf), NamedRecord.read(buf));
    }
}

